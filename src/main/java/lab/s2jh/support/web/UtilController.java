package lab.s2jh.support.web;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lab.s2jh.core.annotation.MenuData;
import lab.s2jh.core.annotation.MetaData;
import lab.s2jh.core.exception.WebException;
import lab.s2jh.core.mq.BrokeredMessageListener;
import lab.s2jh.core.security.AuthUserDetails;
import lab.s2jh.core.service.Validation;
import lab.s2jh.core.util.DateUtils;
import lab.s2jh.core.util.Exceptions;
import lab.s2jh.core.util.ExtStringUtils;
import lab.s2jh.core.web.filter.WebAppContextInitFilter;
import lab.s2jh.core.web.util.ServletUtils;
import lab.s2jh.core.web.view.OperationResult;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.util.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import ch.qos.logback.classic.Level;

@Controller
@RequestMapping("/admin/util")
public class UtilController {

    private final static Logger logger = LoggerFactory.getLogger(UtilController.class);

    @Autowired
    private CacheManager cacheManager;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired(required = false)
    private BrokeredMessageListener brokeredMessageListener;

    @MenuData("Configuration Management: System Management: assistant management")
    @RequiresRoles(AuthUserDetails.ROLE_SUPER_USER)
    @RequestMapping(value = "", method = RequestMethod.GET)
    public String index() {
        return "admin/util/util-index";
    }

    @MetaData(value = "Refresh data cache")
    @RequiresRoles(AuthUserDetails.ROLE_SUPER_USER)
    @RequestMapping(value = "/cache-clear", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult dataEvictCache() {
        WebAppContextInitFilter.reset();
        if (cacheManager != null) {
            logger.info("Clearing EhCache cacheManager: {}", cacheManager.getName());
            String[] cacheNames = cacheManager.getCacheNames();
            for (String cacheName : cacheNames) {

            	// For Apache Shiro cache Ignore
                if (cacheName.indexOf(".authorizationCache") > -1) {
                    continue;
                }
                logger.debug(" - clearing cache： {}", cacheName);
                Ehcache cache = cacheManager.getEhcache(cacheName);
                cache.removeAll();
            }
            return OperationResult.buildSuccessResult("EhCache data cache flush operation was successful");
        }
        return OperationResult.buildFailureResult("Unknown Cache Manager");
    }

    @MetaData(value = "Dynamic Update Logger log level")
    @RequiresRoles(AuthUserDetails.ROLE_SUPER_USER)
    @RequestMapping(value = "/logger-update", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult loggerLevelUpdate(@RequestParam(value = "loggerName", required = false) String loggerName,
            @RequestParam("loggerLevel") String loggerLevel) {
        Assert.isTrue(false, "Analog exception");
        if (StringUtils.isBlank(loggerName)) {
            Validation.notDemoMode();
            loggerName = Logger.ROOT_LOGGER_NAME;
        }
        Logger logger = LoggerFactory.getLogger(loggerName);
        ch.qos.logback.classic.Logger logbackLogger = (ch.qos.logback.classic.Logger) logger;
        if (StringUtils.isNotBlank(loggerLevel)) {
            logbackLogger.setLevel(Level.toLevel(loggerLevel));
        }
        logger.info("Update logger {} to level {}", loggerName, loggerLevel);
        return OperationResult.buildSuccessResult("Dynamic Update Logger log level operation is successful");
    }

    @RequestMapping(value = "/validate", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> formValidation(Model model, @RequestParam("clazz") String clazz) {
        return ServletUtils.buildValidateRules(clazz);
    }

    @RequestMapping(value = "/validate/unique", method = RequestMethod.GET)
    @ResponseBody
    public boolean formValidationUnique(HttpServletRequest request, Model model, @RequestParam("clazz") String clazz) {
        String element = request.getParameter("element");
        Assert.notNull(element);

        String value = request.getParameter(element);
        if (!ExtStringUtils.hasChinese(value)) {
            value = ExtStringUtils.encodeUTF8(value);
        }

        Class<?> entityClass = ClassUtils.forName(clazz);
        String jql = "select id from " + entityClass.getName() + " where " + element + "=:value ";
        Query query = null;


     // Handle the extra added parameter , some data by two fields together determine uniqueness, can be supplemented by additional parameters provided
        String additionalName = request.getParameter("additional");
        if (StringUtils.isNotBlank(additionalName)) {
            String additionalValue = request.getParameter(additionalName);
            if (!ExtStringUtils.hasChinese(additionalValue)) {
                additionalValue = ExtStringUtils.encodeUTF8(additionalValue);
            }
            jql = jql + additionalName + "=:additionalValue ";
            query = entityManager.createQuery(jql);
            query.setParameter("value", value);
            query.setParameter("additionalValue", additionalValue);
        } else {
            query = entityManager.createQuery(jql);
            query.setParameter("value", value);
        }

        List<?> entities = query.getResultList();
        if (entities == null || entities.size() == 0) {// Not found duplicate data
            return true;
        } else {
            if (entities.size() == 1) {// Query to a duplicate data
                String id = request.getParameter("id");
                if (StringUtils.isNotBlank(id)) {
                    String entityId = ((Long) entities.get(0)).toString();
                    logger.debug("Check Unique Entity ID = {}", entityId);
                    if (id.equals(entityId)) {// Query the data is current update data , not already exist
                        return true;
                    } else {// Query the data is not current update data , count already exists
                        return false;
                    }
                } else {// Not available Sid primary key note is to create a record , the operator already exists
                    return false;
                }
            } else {
            	// Query to a redundant duplicate data , the data indicates that the database itself has a problem
                throw new WebException("error.check.unique.duplicate: " + element + "=" + value);
            }
        }
    }

    /**
     * Based jqGrid page data to achieve a common export Excel function
     * Note: This feature is only available data processing page , excluding paging support ; if you need to export data out of all the current query needs to achieve further
     */
    @RequiresRoles(AuthUserDetails.ROLE_MGMT_USER)
    @RequestMapping(value = "/grid/export", method = RequestMethod.POST)
    @ResponseBody
    public void gridExport(HttpServletRequest request, HttpServletResponse response) {
        try {
            String filename = request.getParameter("fileName");
            filename = new String(filename.getBytes("GBK"), "ISO-8859-1");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + filename);
            response.setContentType("application/vnd.ms-excel;charset=UTF-8");
            String exportDatas = request.getParameter("exportDatas");
            OutputStream os = response.getOutputStream();

            HSSFWorkbook wb = new HSSFWorkbook();// Create the Excel Workbook object
            HSSFSheet sheet = wb.createSheet(filename);  // Create an Excel worksheet object
            String[] rows = exportDatas.split("\n");
            for (int i = 0; i < rows.length; i++) {
                String row = rows[i];
                if (StringUtils.isNotBlank(row)) {
                    logger.trace("Row {}: {}", i, row);

                 // Create a row in the Excel sheet
                    HSSFRow hssfRow = sheet.createRow(i);
                    String[] cells = row.split("\t");
                    for (int j = 0; j < cells.length; j++) {
                        String cell = cells[j];

                     // Create an Excel cell
                        HSSFCell hssfCell = hssfRow.createCell(j);
                        hssfCell.setCellValue(cell);
                    }
                }
            }
            wb.write(os);
            IOUtils.closeQuietly(os);
        } catch (UnsupportedEncodingException e) {
            Exceptions.unchecked(e);
        } catch (IOException e) {
            Exceptions.unchecked(e);
        }
    }

    @RequestMapping(value = "/load-balance-test", method = RequestMethod.GET)
    public String loadBalanceTest() {
        return "admin/util/load-balance-test";
    }

    @MetaData(value = "Tampering with the system time update")
    @RequiresRoles(AuthUserDetails.ROLE_SUPER_USER)
    @RequestMapping(value = "/systime/setup", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult systimeSetup(@RequestParam(value = "time", required = true) String time) {
        DateUtils.setCurrentDate(DateUtils.parseMultiFormatDate(time));


     // To avoid forgetting to perform manual recovery operation , in the " temporary adjust the system time" action , by default N minutes after the forced recovery of the current system time .
        Runnable runnable = new Runnable() {
            public void run() {
                DateUtils.setCurrentDate(null);
                logger.info("Processed DateUtils.currentDate() reset to new Date()");
            }
        };
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.schedule(runnable, 5, TimeUnit.MINUTES);

        return OperationResult.buildSuccessResult("The system time has been adjusted for the interim :" + DateUtils.formatTime(DateUtils.currentDate()));
    }

    @MetaData(value = "System tamper recovery time")
    @RequiresRoles(AuthUserDetails.ROLE_SUPER_USER)
    @RequestMapping(value = "/systime/reset", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult systimeReset() {
        DateUtils.setCurrentDate(null);
        return OperationResult.buildSuccessResult("Time temporary adjustment system has been restored to the current system time :" + DateUtils.formatTime(DateUtils.currentDate()));
    }

    @MetaData(value = "Message Service Listener state switching")
    @RequiresRoles(AuthUserDetails.ROLE_SUPER_USER)
    @RequestMapping(value = "/brokered-message/listener-state", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult brokeredMessageState(@RequestParam(value = "state", required = true) String state) {
        Validation.isTrue(brokeredMessageListener != null, "BrokeredMessageListener undefined");
        if ("startup".equals(state)) {
            brokeredMessageListener.startup();
            return OperationResult.buildSuccessResult("Message Service listener is started");
        } else if ("shutdown".equals(state)) {
            brokeredMessageListener.shutdown();
            return OperationResult.buildSuccessResult("Message Service listener is closed");
        }
        return OperationResult.buildFailureResult("Unknown state parameters");
    }
}
