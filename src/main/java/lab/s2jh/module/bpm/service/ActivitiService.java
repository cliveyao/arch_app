package lab.s2jh.module.bpm.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import lab.s2jh.core.security.AuthContextHolder;
import lab.s2jh.module.bpm.BpmTrackable;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@Service
@Transactional
public class ActivitiService {

    private final static Logger logger = LoggerFactory.getLogger(ActivitiService.class);

    public static final String BPM_ENTITY_VAR_NAME = "entity";

    public static final String BPM_INITIATOR_VAR_NAME = "initiator";

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired(required = false)
    private RuntimeService runtimeService;

    @Autowired(required = false)
    private TaskService taskService;

    @Autowired(required = false)
    private FormService formService;

    @Autowired(required = false)
    private RepositoryService repositoryService;

    @Autowired(required = false)
    private IdentityService identityService;

    @Autowired(required = false)
    private HistoryService historyService;

    @Autowired(required = false)
    private ProcessEngineFactoryBean processEngine;

    /**
     * Construction based natural key process instance diagram
     * @param bizKey
     * @return
     */
    public InputStream buildProcessImageByBizKey(String bizKey) {
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceBusinessKey(bizKey).singleResult();
        return buildProcessImageByProcessInstance(processInstance);
    }

    /**
     * Based ProcessInstanceId removal process
     * @param bizKey
     * @return
     */
    public void deleteProcessInstanceByProcessInstanceId(String processInstanceId, String message) {
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        deleteProcessInstanceByProcessInstance(processInstance, message);
    }

    /**
     *Based on natural key removal process
     * @param bizKey
     * @return
     */
    public void deleteProcessInstanceByEntity(BpmTrackable entity) {
        entity.setActiveTaskName("END");
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceBusinessKey(entity.getBpmBusinessKey())
                .singleResult();
        deleteProcessInstanceByProcessInstance(processInstance, "Casecade by entity delete [" + entity.getBpmBusinessKey() + "]");
    }

    /**
     * Based on natural key removal process
     * @param bizKey
     * @return
     */
    public void deleteProcessInstanceByBizKey(String bizKey, String message) {
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceBusinessKey(bizKey).singleResult();
        deleteProcessInstanceByProcessInstance(processInstance, message);
    }

    /**
     * Based processInstance removal process
     * @param bizKey
     * @return
     */
    public void deleteProcessInstanceByProcessInstance(ProcessInstance processInstance, String message) {
        if (processInstance != null) {
        	// Try-catch process , resulting in an updated version avoid dirty data get left unable to delete the object processing failures
            try {
                Object val = runtimeService.getVariable(processInstance.getProcessInstanceId(), BPM_ENTITY_VAR_NAME);
                if (val != null && val instanceof BpmTrackable) {
                    BpmTrackable entity = (BpmTrackable) val;
                    entity.setActiveTaskName("END");
                    entityManager.persist(entity);
                }
            } catch (Exception e) {
                logger.warn(e.getMessage(), e);
            }
            identityService.setAuthenticatedUserId(AuthContextHolder.getAuthSysUserUid());
            runtimeService.deleteProcessInstance(processInstance.getId(), message);
        }
    }

    /**
     * Based on Business Process instance ID process instance diagram
     * @param processInstanceId
     * @return
     */
    public InputStream buildProcessImageByProcessInstanceId(String processInstanceId) {
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        return buildProcessImageByProcessInstance(processInstance);
    }

    private InputStream buildProcessImageByProcessInstance(ProcessInstance processInstance) {
        if (processInstance == null) {
            return null;
        }
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processInstance.getProcessDefinitionId());
        ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) repositoryService.getProcessDefinition(processInstance
                .getProcessDefinitionId());
        List<String> activeActivityIds = runtimeService.getActiveActivityIds(processInstance.getProcessInstanceId());
     // Use spring injection engine Please use the following line of code
        Context.setProcessEngineConfiguration(processEngine.getProcessEngineConfiguration());

        List<String> highLightedFlows = getHighLightedFlows(processDefinition, processInstance.getProcessInstanceId());

        InputStream imageStream = processEngine
                .getProcessEngineConfiguration()
                .getProcessDiagramGenerator()
                .generateDiagram(bpmnModel, "png", activeActivityIds, highLightedFlows,
                        processEngine.getProcessEngineConfiguration().getActivityFontName(),
                        processEngine.getProcessEngineConfiguration().getLabelFontName(),
                        processEngine.getProcessEngineConfiguration().getClassLoader(), 1.0);

        return imageStream;
    }

    private List<String> getHighLightedFlows(ProcessDefinitionEntity processDefinition, String processInstanceId) {
        List<String> historicActivityInstanceList = new ArrayList<String>();
        List<String> highLightedFlows = new ArrayList<String>();

        List<HistoricActivityInstance> historicActivityInstances = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId).orderByHistoricActivityInstanceStartTime().asc().list();

        for (HistoricActivityInstance hai : historicActivityInstances) {
            historicActivityInstanceList.add(hai.getActivityId());
        }

        // add current activities to list
        List<String> highLightedActivities = runtimeService.getActiveActivityIds(processInstanceId);
        historicActivityInstanceList.addAll(highLightedActivities);

        // activities and their sequence-flows
        getHighLightedFlows(processDefinition.getActivities(), highLightedFlows, historicActivityInstanceList);

        return highLightedFlows;
    }

    private void getHighLightedFlows(List<ActivityImpl> activityList, List<String> highLightedFlows, List<String> historicActivityInstanceList) {
        for (ActivityImpl activity : activityList) {
            if (activity.getProperty("type").equals("subProcess")) {
                // get flows for the subProcess
                getHighLightedFlows(activity.getActivities(), highLightedFlows, historicActivityInstanceList);
            }

            if (historicActivityInstanceList.contains(activity.getId())) {
                List<PvmTransition> pvmTransitionList = activity.getOutgoingTransitions();
                for (PvmTransition pvmTransition : pvmTransitionList) {
                    String destinationFlowId = pvmTransition.getDestination().getId();
                    if (historicActivityInstanceList.contains(destinationFlowId)) {
                        highLightedFlows.add(pvmTransition.getId());
                    }
                }
            }
        }
    }

    /**
     * Based process instance natural key running queries
     * @param Entity processes the business object
     * @return
     */
    public ProcessInstance findRunningProcessInstance(BpmTrackable entity) {
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceBusinessKey(entity.getBpmBusinessKey())
                .singleResult();
        return processInstance;
    }

    /**
     * Query business objects currently active task name
     * @param BizKey start the process of natural key
     * @return
     */
    public String findActiveTaskNames(String bizKey) {
        Assert.notNull(bizKey);
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceBusinessKey(bizKey).singleResult();
     // Process has ended , the direct return null
        if (processInstance == null) {
            return "END";
        }
        List<String> ids = runtimeService.getActiveActivityIds(processInstance.getId());
        ProcessDefinitionEntity pde = (ProcessDefinitionEntity) repositoryService.getProcessDefinition(processInstance.getProcessDefinitionId());
        List<ActivityImpl> activityImpls = pde.getActivities();
        List<String> activeActs = Lists.newArrayList();
        for (ActivityImpl activityImpl : activityImpls) {
            if (ids.contains(activityImpl.getId())) {
                activeActs.add(ObjectUtils.toString(activityImpl.getProperty("name")));
            }
        }
        return StringUtils.join(activeActs, ",");
    }

    /**  
     *According to the current task ID, the query may reject the task node
     *
     * @param TaskId current task ID
     */
    public List<ActivityImpl> findBackActivities(String taskId) {
        List<ActivityImpl> rtnList = iteratorBackActivity(taskId, findActivitiImpl(taskId, null), new ArrayList<ActivityImpl>(),
                new ArrayList<ActivityImpl>());
        return reverList(rtnList);
    }

    /**  
     * Returns targeting the active node
     *
     * @param TaskId current task ID
     * @param ActivityId return node activity ID
     * @param Variables stored in the process parameters
     * @throws Exception  
     */
    public void backActivity(String taskId, String activityId, Map<String, Object> variables) {
        Assert.notNull(activityId, "Back target process activity id required");

     // Find all the nodes in parallel tasks at the same time rejected
        List<Task> taskList = findTaskListByKey(findProcessInstanceByTaskId(taskId).getId(), findTaskById(taskId).getTaskDefinitionKey());
        for (Task task : taskList) {
            commitProcess(task.getId(), variables, activityId);
        }
    }

    /**  
     * Returns targeting the active node
     *
     * @param TaskId current task ID
     * @param ActivityId return node activity ID
     * @throws Exception  
     */
    public void backActivity(String taskId, String activityId) {
        backActivity(taskId, activityId, null);
    }

    /**  
     * Empty specify the active node flows
     *
     * @param ActivityImpl active node
     * @return Node flow collection
     */
    private List<PvmTransition> clearTransition(ActivityImpl activityImpl) {
    	// Store the current node all the flow of temporary variables
        List<PvmTransition> oriPvmTransitionList = new ArrayList<PvmTransition>();
     // Get the current node to all flows stored in the temporary variable , and then emptied
        List<PvmTransition> pvmTransitionList = activityImpl.getOutgoingTransitions();
        for (PvmTransition pvmTransition : pvmTransitionList) {
            oriPvmTransitionList.add(pvmTransition);
        }
        pvmTransitionList.clear();

        return oriPvmTransitionList;
    }

    /**  
     *@param taskId current task ID
     * @param Variables Process variables
     * @param ActivityId process to implementation task node ID <br> this parameter is empty , the default for commit
     * @throws Exception  
     */
    private void commitProcess(String taskId, Map<String, Object> variables, String activityId) {
        if (variables == null) {
            variables = new HashMap<String, Object>();
        }
     // Jump node is empty , the default commit operation
        if (StringUtils.isEmpty(activityId)) {
            taskService.complete(taskId, variables);
        } else {// Process steering operation
            turnTransition(taskId, activityId, variables);
        }
    }

    /**  
     * Abort the process ( direct approval by the privileged people , etc. )
     *   
     * @param taskId  
     */
    public void endProcess(String taskId) {
        ActivityImpl endActivity = findActivitiImpl(taskId, "end");
        commitProcess(taskId, null, endActivity.getId());
    }

    /**  
     * According to flow into a collection of tasks , accessing the latest inflow task node
     *
     * @param ProcessInstance process instance
     * @param TempList flows into a collection of tasks
     * @return  
     */
    private ActivityImpl filterNewestActivity(ProcessInstance processInstance, List<ActivityImpl> tempList) {
        while (tempList.size() > 0) {
            ActivityImpl activity_1 = tempList.get(0);
            HistoricActivityInstance activityInstance_1 = findHistoricUserTask(processInstance, activity_1.getId());
            if (activityInstance_1 == null) {
                tempList.remove(activity_1);
                continue;
            }

            if (tempList.size() > 1) {
                ActivityImpl activity_2 = tempList.get(1);
                HistoricActivityInstance activityInstance_2 = findHistoricUserTask(processInstance, activity_2.getId());
                if (activityInstance_2 == null) {
                    tempList.remove(activity_2);
                    continue;
                }

                if (activityInstance_1.getEndTime().before(activityInstance_2.getEndTime())) {
                    tempList.remove(activity_1);
                } else {
                    tempList.remove(activity_2);
                }
            } else {
                break;
            }
        }
        if (tempList.size() > 0) {
            return tempList.get(0);
        }
        return null;
    }

    /**  
     * Get active node ID according to the task ID and node <br>
     *
     * @param TaskId task ID
     * @param ActivityId active node ID <br> If null or "" , the default query the current active node <br> If "end", the end of the query nodes <br> 
     *   
     * @return  
     * @throws Exception  
     */
    private ActivityImpl findActivitiImpl(String taskId, String activityId) {
    	// Get the process definition
        ProcessDefinitionEntity processDefinition = findProcessDefinitionEntityByTaskId(taskId);

     // Get the current active node ID
        if (StringUtils.isEmpty(activityId)) {
            activityId = findTaskById(taskId).getTaskDefinitionKey();
        }

     // According to the process definition, to obtain the end nodes of the process instance
        if (activityId.toUpperCase().equals("END")) {
            for (ActivityImpl activityImpl : processDefinition.getActivities()) {
                List<PvmTransition> pvmTransitionList = activityImpl.getOutgoingTransitions();
                if (pvmTransitionList.isEmpty()) {
                    return activityImpl;
                }
            }
        }

     // A node ID, to obtain the corresponding active node
        ActivityImpl activityImpl = ((ProcessDefinitionImpl) processDefinition).findActivity(activityId);

        return activityImpl;
    }

    /**  
     * Latest record query specified task node
     *
     * @param ProcessInstance process instance
     * @param activityId  
     * @return  
     */
    private HistoricActivityInstance findHistoricUserTask(ProcessInstance processInstance, String activityId) {
        HistoricActivityInstance rtnVal = null;
     // Query the current approval process instances end node history
        List<HistoricActivityInstance> historicActivityInstances = historyService.createHistoricActivityInstanceQuery().activityType("userTask")
                .processInstanceId(processInstance.getId()).activityId(activityId).finished().orderByHistoricActivityInstanceEndTime().desc().list();
        if (historicActivityInstances.size() > 0) {
            rtnVal = historicActivityInstances.get(0);
        }

        return rtnVal;
    }

    /**  
     * According to the current node , whether parallel query output flows end , if the parallel end , the assembly corresponding parallel start ID
     *
     * @param ActivityImpl current node
     * @return  
     */
    private String findParallelGatewayId(ActivityImpl activityImpl) {
        List<PvmTransition> incomingTransitions = activityImpl.getOutgoingTransitions();
        for (PvmTransition pvmTransition : incomingTransitions) {
            TransitionImpl transitionImpl = (TransitionImpl) pvmTransition;
            activityImpl = transitionImpl.getDestination();
            String type = (String) activityImpl.getProperty("type");
            if ("parallelGateway".equals(type)) {// Parallel route
                String gatewayId = activityImpl.getId();
                String gatewayType = gatewayId.substring(gatewayId.lastIndexOf("_") + 1);
                if ("END".equals(gatewayType.toUpperCase())) {
                    return gatewayId.substring(0, gatewayId.lastIndexOf("_")) + "_start";
                }
            }
        }
        return null;
    }

    /**  
     *Get process definition based on the task ID
     *
     * @param TaskId
     * Task ID
     * @return  
     * @throws Exception  
     */
    private ProcessDefinitionEntity findProcessDefinitionEntityByTaskId(String taskId) {
    	// Get the process definition
        return (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService).getDeployedProcessDefinition(findTaskById(taskId)
                .getProcessDefinitionId());
    }

    /**  
     * Get the corresponding process instance based on the task ID
     *
     * @param TaskId task ID
     * @return  
     * @throws Exception  
     */
    private ProcessInstance findProcessInstanceByTaskId(String taskId) {
    	// Find the process instance
        return runtimeService.createProcessInstanceQuery().processInstanceId(findTaskById(taskId).getProcessInstanceId()).singleResult();
    }

    /**  
     * According to the task get task instance ID
     *
     * @param TaskId task ID
     * @return  
     * @throws Exception  
     */
    private TaskEntity findTaskById(String taskId) {
        return (TaskEntity) taskService.createTaskQuery().taskId(taskId).singleResult();
    }

    /**  
     *All similar set of tasks based on task and process instance ID key value query
     *   
     * @param processInstanceId  
     * @param key  
     * @return  
     */
    private List<Task> findTaskListByKey(String processInstanceId, String key) {
        return taskService.createTaskQuery().processInstanceId(processInstanceId).taskDefinitionKey(key).list();
    }

    /**  
     * Iteration loop process tree , check the current task node node rebuttable
     *
     * @param TaskId current task ID
     * @param CurrActivity current active node
     * @param RtnList storage node-set rollback
     * @param TempList temporary storage node-set ( stored iteration process userTask sibling node )
     * @return Node-set rollback
     */
    private List<ActivityImpl> iteratorBackActivity(String taskId, ActivityImpl currActivity, List<ActivityImpl> rtnList, List<ActivityImpl> tempList) {
    	// Query process definition generation process tree
        ProcessInstance processInstance = findProcessInstanceByTaskId(taskId);

     // The current flowing into the source node
        List<PvmTransition> incomingTransitions = currActivity.getIncomingTransitions();
     // Conditional branch node set , userTask node traversal is complete, iterate over this collection , the query conditional branch node corresponding userTask
        List<ActivityImpl> exclusiveGateways = new ArrayList<ActivityImpl>();
     // Set parallel nodes , userTask node traversal is complete, iterate over this collection , query parallelism node node corresponding userTask
        List<ActivityImpl> parallelGateways = new ArrayList<ActivityImpl>();
     // Traverse all the current node inflow path
        for (PvmTransition pvmTransition : incomingTransitions) {
            TransitionImpl transitionImpl = (TransitionImpl) pvmTransition;
            ActivityImpl activityImpl = transitionImpl.getSource();
            String type = (String) activityImpl.getProperty("type");
            /**  
             *Parallel node configuration requirements : <br>
             * Must be paired , and the requirements are configured node ID : XXX_start ( Start ), XXX_end ( end )  
             */
            if ("parallelGateway".equals(type)) {// Parallel route   
                String gatewayId = activityImpl.getId();
                String gatewayType = gatewayId.substring(gatewayId.lastIndexOf("_") + 1);
                if ("START".equals(gatewayType.toUpperCase())) {// Parallel start , stop recursion
                    return rtnList;
                } else {// End parallel temporarily stores this node , the end of this cycle , iterative set of query nodes corresponding userTask
                    parallelGateways.add(activityImpl);
                }
            } else if ("startEvent".equals(type)) {// Start node , stop the recursion  
                return rtnList;
            } else if ("userTask".equals(type)) {// User tasks
                tempList.add(activityImpl);
            } else if ("exclusiveGateway".equals(type)) {// Branch routes , this temporary store node , the end of this cycle , iterative set of query nodes corresponding userTask
                currActivity = transitionImpl.getSource();
                exclusiveGateways.add(currActivity);
            }
        }

        /**  
         * Iterative conditional branch collections, queries corresponding node userTask
         */
        for (ActivityImpl activityImpl : exclusiveGateways) {
            iteratorBackActivity(taskId, activityImpl, rtnList, tempList);
        }

        /**  
         * Iterative parallel collections, queries corresponding node userTask 
         */
        for (ActivityImpl activityImpl : parallelGateways) {
            iteratorBackActivity(taskId, activityImpl, rtnList, tempList);
        }

        /**  
         * According to the same level userTask collection , filtering nodes recent
         */
        currActivity = filterNewestActivity(processInstance, tempList);
        if (currActivity != null) {
            // Check whether the current node is the end flows to parallel and parallel to obtain a starting point ID    
            String id = findParallelGatewayId(currActivity);
            if (StringUtils.isEmpty(id)) {// Parallel start ID is empty , this node is not parallel flows end , in line with the conditions rejected , this storage node    
                rtnList.add(currActivity);
            } else {// Query the current node according to the parallel starting point ID, then the corresponding iterative query task node userTask    
                currActivity = findActivitiImpl(taskId, id);
            }

         // Clear this iteration Temporary collection
            tempList.clear();
         // Execute the next iteration
            iteratorBackActivity(taskId, currActivity, rtnList, tempList);
        }
        return rtnList;
    }

    /**  
     * Restore specified active node flows
     *
     * @param ActivityImpl active node
     * @param OriPvmTransitionList original flow collection node  
     */
    private void restoreTransition(ActivityImpl activityImpl, List<PvmTransition> oriPvmTransitionList) {
    	// Clear existing flow
        List<PvmTransition> pvmTransitionList = activityImpl.getOutgoingTransitions();
        pvmTransitionList.clear();
     // Restore previous flow
        for (PvmTransition pvmTransition : oriPvmTransitionList) {
            pvmTransitionList.add(pvmTransition);
        }
    }

    /**  
     * Reverse sort list collection , displayed in order to facilitate dismissed node
     *   
     * @param list  
     * @return  
     */
    private List<ActivityImpl> reverList(List<ActivityImpl> list) {
        List<ActivityImpl> rtnList = new ArrayList<ActivityImpl>();
        // Since iteration duplicate data , eliminate duplicate 
        for (int i = list.size(); i > 0; i--) {
            if (!rtnList.contains(list.get(i - 1)))
                rtnList.add(list.get(i - 1));
        }
        return rtnList;
    }

    /**  
     * Process steering operation
     *
     * @param TaskId current task ID
     * @param ActivityId target node task ID
     * @param Variables Process variables
     * @throws Exception  
     */
    private void turnTransition(String taskId, String activityId, Map<String, Object> variables) {
    	// Current node  
        ActivityImpl currActivity = findActivitiImpl(taskId, null);
     // Clear the current flow
        List<PvmTransition> oriPvmTransitionList = clearTransition(currActivity);

     // Create a new flow
        TransitionImpl newTransition = currActivity.createOutgoingTransition();
     // Target node    
        ActivityImpl pointActivity = findActivitiImpl(taskId, activityId);
     // Set the target node of the new flows
        newTransition.setDestination(pointActivity);

     // Perform tasks steering
        taskService.complete(taskId, variables);
     // Delete the target node of the new inflows
        pointActivity.getIncomingTransitions().remove(newTransition);

     // Restore previous flow
        restoreTransition(currActivity, oriPvmTransitionList);
    }

    /**
     *Start the latest version of the process in accordance with the process definition instance Key
     * @param processDefinitionKey
     * @param businessKey
     * @param variables
     * @return
     */
    public void startProcessInstanceByKey(String processDefinitionKey, String businessKey, Map<String, Object> variables) {
        identityService.setAuthenticatedUserId(AuthContextHolder.getAuthSysUserUid());
        runtimeService.startProcessInstanceByKey(processDefinitionKey, businessKey, variables);
    }

    /**
     * Start the latest version of the process in accordance with the process definition instance Key
     * @param processDefinitionKey
     * @param businessKey
     * @param entity
     * @return
     */
    public void startProcessInstanceByKey(String processDefinitionKey, BpmTrackable entity) {
        identityService.setAuthenticatedUserId(AuthContextHolder.getAuthSysUserUid());
        Map<String, Object> variables = Maps.newHashMap();
     // Append the current entity object is added to the process variable
        variables.put(BPM_ENTITY_VAR_NAME, entity);
        runtimeService.startProcessInstanceByKey(processDefinitionKey, entity.getBpmBusinessKey(), variables);
        String activeTaskNames = findActiveTaskNames(entity.getBpmBusinessKey());
        entity.setActiveTaskName(activeTaskNames);
    }

    /**
     * mission accomplished
     * @param taskId
     * @param variables
     * @return
     */
    public void completeTask(String taskId, Map<String, Object> variables) {
        identityService.setAuthenticatedUserId(AuthContextHolder.getAuthSysUserUid());
        if (variables != null && variables.size() > 0) {
            taskService.setVariablesLocal(taskId, variables);
        }
        BpmTrackable entity = (BpmTrackable) taskService.getVariable(taskId, BPM_ENTITY_VAR_NAME);
        taskService.complete(taskId, variables);
        if (entity != null) {
            entity = entityManager.find(entity.getClass(), entity.getId());
            String activeTaskNames = findActiveTaskNames(entity.getBpmBusinessKey());
            entity.setActiveTaskName(activeTaskNames);
            entityManager.persist(entity);
        }
    }

    /**
     * Forms-based data to complete the task
     * @param taskId
     * @param formProperties
     * @return
     */
    public void submitTaskFormData(String taskId, Map<String, String> formProperties) {
        identityService.setAuthenticatedUserId(AuthContextHolder.getAuthSysUserUid());
        BpmTrackable entity = (BpmTrackable) taskService.getVariable(taskId, BPM_ENTITY_VAR_NAME);
        formService.submitTaskFormData(taskId, formProperties);
        if (entity != null) {
            entity = entityManager.find(entity.getClass(), entity.getId());
            String activeTaskNames = findActiveTaskNames(entity.getBpmBusinessKey());
            entity.setActiveTaskName(activeTaskNames);
            entityManager.persist(entity);
        }
    }

    /**
     * Discover user-specified task
     * @param AuthenticatedUserId login account , AuthContextHolder.getAuthSysUserUid ()
     * @param BizKey business key
     * @param TaskDefinitionKey Task definition ID Key
     * @return 
     */
    public Task findTask(String authenticatedUserId, String bizKey, String taskDefinitionKey) {
        return taskService.createTaskQuery().taskAssignee(authenticatedUserId).processInstanceBusinessKey(bizKey)
                .taskDefinitionKey(taskDefinitionKey).singleResult();
    }
}
