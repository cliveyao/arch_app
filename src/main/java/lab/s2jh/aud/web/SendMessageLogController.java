package lab.s2jh.aud.web;

import javax.servlet.http.HttpServletRequest;

import lab.s2jh.aud.entity.SendMessageLog;
import lab.s2jh.aud.entity.SendMessageLog.SendMessageTypeEnum;
import lab.s2jh.aud.service.SendMessageLogService;
import lab.s2jh.core.annotation.MenuData;
import lab.s2jh.core.annotation.MetaData;
import lab.s2jh.core.service.BaseService;
import lab.s2jh.core.util.EnumUtils;
import lab.s2jh.core.web.BaseController;
import lab.s2jh.core.web.json.JsonViews;
import lab.s2jh.core.web.view.OperationResult;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.annotation.JsonView;

@MetaData("Send Message Records Management")
@Controller
@RequestMapping(value = "/admin/aud/send-message-log")
public class SendMessageLogController extends BaseController<SendMessageLog, Long> {

    @Autowired
    private SendMessageLogService sendMessageLogService;

    @Override
    protected BaseService<SendMessageLog, Long> getEntityService() {
        return sendMessageLogService;
    }

    @ModelAttribute
    public void prepareModel(HttpServletRequest request, Model model, @RequestParam(value = "id", required = false) Long id) {
        super.initPrepareModel(request, model, id);
    }

    @MenuData("Configuration Management : The system records : send message record")
    @RequiresPermissions("Configuration Management : The system records : send message record")
    @RequestMapping(value = "", method = RequestMethod.GET)
    public String index(Model model) {
        model.addAttribute("messageTypeMap", EnumUtils.getEnumDataMap(SendMessageTypeEnum.class));
        return "admin/aud/sendMessageLog-index";
    }

    @RequiresPermissions("Configuration Management : The system records : send message record")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    @JsonView(JsonViews.Admin.class)
    public Page<SendMessageLog> findByPage(HttpServletRequest request) {
        return super.findByPage(SendMessageLog.class, request);
    }

    @RequestMapping(value = "/edit-tabs", method = RequestMethod.GET)
    public String editTabs(HttpServletRequest request) {
        return "admin/aud/sendMessageLog-inputTabs";
    }

    //@RequiresPermissions("TODO {PATH}:SendMessageLog")
    @RequiresPermissions("Configuration Management : The system records : send message record")
    @RequestMapping(value = "/edit", method = RequestMethod.GET)
    public String editShow(Model model) {
        model.addAttribute("messageTypeMap", EnumUtils.getEnumDataMap(SendMessageTypeEnum.class));
        return "admin/aud/sendMessageLog-inputBasic";
    }

    @RequiresPermissions("Configuration Management : The system records : send message record")
    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult editSave(@ModelAttribute("entity") SendMessageLog entity) {
        return super.editSave(entity);
    }

    @RequiresPermissions("Configuration Management : The system records : send message record")
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult delete(@RequestParam("ids") Long... ids) {
        return super.delete(ids);
    }

}