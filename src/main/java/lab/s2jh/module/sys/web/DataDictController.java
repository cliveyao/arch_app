package lab.s2jh.module.sys.web;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import lab.s2jh.core.annotation.MenuData;
import lab.s2jh.core.annotation.MetaData;
import lab.s2jh.core.pagination.GroupPropertyFilter;
import lab.s2jh.core.pagination.PropertyFilter;
import lab.s2jh.core.pagination.PropertyFilter.MatchType;
import lab.s2jh.core.service.BaseService;
import lab.s2jh.core.web.BaseController;
import lab.s2jh.core.web.view.OperationResult;
import lab.s2jh.module.sys.entity.DataDict;
import lab.s2jh.module.sys.service.DataDictService;

import org.apache.commons.collections.CollectionUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/admin/sys/data-dict")
public class DataDictController extends BaseController<DataDict, Long> {

    @Autowired
    private DataDictService dataDictService;

    @Override
    protected BaseService<DataDict, Long> getEntityService() {
        return dataDictService;
    }

    @MenuData("Configuration Management: System Administration : Data Dictionary")
    @RequiresPermissions("Configuration Management: System Administration : Data Dictionary")
    @RequestMapping(value = "", method = RequestMethod.GET)
    public String index(Model model) {
        return "admin/sys/dataDict-index";
    }

    @Override
    protected void appendFilterProperty(GroupPropertyFilter groupPropertyFilter) {
        if (groupPropertyFilter.isEmptySearch()) {
            groupPropertyFilter.forceAnd(new PropertyFilter(MatchType.NU, "parent", true));
        }
        super.appendFilterProperty(groupPropertyFilter);
    }

    @RequiresPermissions("Configuration Management: System Administration : Data Dictionary")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public Page<DataDict> findByPage(HttpServletRequest request) {
        return super.findByPage(DataDict.class, request);
    }

    @RequiresPermissions("Configuration Management: System Administration : Data Dictionary")
    @RequestMapping(value = "/edit", method = RequestMethod.GET)
    public String editShow() {
        return "admin/sys/dataDict-inputBasic";
    }

    @RequiresPermissions("Configuration Management: System Administration : Data Dictionary")
    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult editSave(@ModelAttribute("entity") DataDict entity, Model model) {
        return super.editSave(entity);
    }

    @RequiresPermissions("Configuration Management: System Administration : Data Dictionary")
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult delete(HttpServletRequest request, @ModelAttribute("entity") DataDict entity) {

    	// Parameter is determined based on whether the user submits a request has been confirmed , if otherwise subsequent business logic validation and feedback suggests that if confirmed directly by
        if (postNotConfirmedByUser(request)) {
            List<DataDict> children = entity.getChildren();

         // User feedback information to be confirm confirm confirm OK if the user is automatically initiated the request again
            if (CollectionUtils.isNotEmpty(children)) {
                return OperationResult.buildConfirmResult("If you delete the current project will recursively delete all the children");
            }
        }
        return super.delete(entity.getId());
    }

    @RequiresUser
    @ModelAttribute
    public void prepareModel(HttpServletRequest request, Model model, @RequestParam(value = "id", required = false) Long id) {
        super.initPrepareModel(request, model, id);
    }

    @MetaData(value = "Cascade sub-data collection")
    @RequestMapping(value = "/children", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, String> children(HttpServletRequest request, @RequestParam(value = "id") Long id) {
        return dataDictService.findMapDataById(id);
    }
}
