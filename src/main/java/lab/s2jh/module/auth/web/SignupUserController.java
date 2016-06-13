package lab.s2jh.module.auth.web;

import javax.servlet.http.HttpServletRequest;

import lab.s2jh.core.annotation.MenuData;
import lab.s2jh.core.service.BaseService;
import lab.s2jh.core.service.Validation;
import lab.s2jh.core.web.BaseController;
import lab.s2jh.core.web.EntityProcessCallbackHandler;
import lab.s2jh.core.web.view.OperationResult;
import lab.s2jh.module.auth.entity.SignupUser;
import lab.s2jh.module.auth.entity.User;
import lab.s2jh.module.auth.service.RoleService;
import lab.s2jh.module.auth.service.SignupUserService;
import lab.s2jh.module.auth.service.UserService;
import lab.s2jh.support.service.DynamicConfigService;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresUser;
import org.joda.time.DateTime;
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
@RequestMapping(value = "/admin/auth/signup-user")
public class SignupUserController extends BaseController<SignupUser, Long> {

    @Autowired
    private UserService userService;

    @Autowired
    private SignupUserService signupUserService;

    @Autowired
    private DynamicConfigService dynamicConfigService;

    @Autowired
    private RoleService roleService;

    @Override
    protected BaseService<SignupUser, Long> getEntityService() {
        return signupUserService;
    }

    @RequiresUser
    @ModelAttribute
    public void prepareModel(HttpServletRequest request, Model model, @RequestParam(value = "id", required = false) Long id) {
        super.initPrepareModel(request, model, id);
    }

    @MenuData("Configuration Management: Rights Management : Registered User Management")
    @RequiresPermissions("Configuration Management: Rights Management : Registered User Management")
    @RequestMapping(value = "", method = RequestMethod.GET)
    public String index(Model model) {
        return "admin/auth/signupUser-index";
    }

    @RequiresPermissions("Configuration Management: Rights Management : Registered User Management")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public Page<SignupUser> findByPage(HttpServletRequest request) {
        return super.findByPage(SignupUser.class, request);
    }

    @RequiresPermissions("Configuration Management: Rights Management : Registered User Management")
    @RequestMapping(value = "/audit", method = RequestMethod.GET)
    public String auditShow(Model model, @ModelAttribute("entity") SignupUser entity) {
        User user = new User();
        user.setMgmtGranted(true);
        //The default password expires after six months , when a user logs mandatory password reset
        user.setCredentialsExpireTime(new DateTime().plusMonths(6).toDate());
        entity.setUser(user);
        model.addAttribute("roles", roleService.findAllCached());
        return "admin/auth/signupUser-audit";
    }

    @RequiresPermissions("Configuration Management: Rights Management : Registered User Management")
    @RequestMapping(value = "/audit", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult auditSave(@ModelAttribute("entity") SignupUser entity, Model model) {
        Validation.notDemoMode();
        signupUserService.auditNewUser(entity);
        return OperationResult.buildSuccessResult("Data storage processing is completed", entity);
    }

    @RequiresPermissions("Configuration Management: Rights Management : Registered User Management")
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult delete(@RequestParam("ids") Long... ids) {
        Validation.notDemoMode();
        return super.delete(ids, new EntityProcessCallbackHandler<SignupUser>() {
            @Override
            public void processEntity(SignupUser entity) throws EntityProcessCallbackException {
                if (entity.getAuditTime() != null) {
                    throw new EntityProcessCallbackException("We have reviewed the data can not be deleted");
                }
            }
        });
    }
}
