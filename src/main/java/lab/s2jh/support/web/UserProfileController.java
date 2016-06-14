package lab.s2jh.support.web;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import lab.s2jh.core.annotation.MenuData;
import lab.s2jh.core.annotation.MetaData;
import lab.s2jh.core.security.AuthContextHolder;
import lab.s2jh.core.security.AuthUserDetails;
import lab.s2jh.core.service.Validation;
import lab.s2jh.core.web.view.OperationResult;
import lab.s2jh.module.auth.entity.User;
import lab.s2jh.module.auth.service.UserService;
import lab.s2jh.module.sys.entity.UserProfileData;
import lab.s2jh.module.sys.service.NotifyMessageService;
import lab.s2jh.module.sys.service.UserMessageService;
import lab.s2jh.module.sys.service.UserProfileDataService;
import lab.s2jh.support.service.MailService;

import org.apache.shiro.authz.annotation.RequiresRoles;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class UserProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private NotifyMessageService notifyMessageService;

    @Autowired
    private UserMessageService userMessageService;

    @Autowired
    private MailService mailService;

    @Autowired
    private UserProfileDataService userProfileDataService;

    @MenuData("Personal Information : Personal Configuration")
    @RequiresRoles(value = AuthUserDetails.ROLE_MGMT_USER)
    @RequestMapping(value = "/admin/profile/edit", method = RequestMethod.GET)
    public String profileLayoutShow(@ModelAttribute("user") User user, Model model) {
        user.addExtraAttributes(userProfileDataService.findMapDataByUser(user));
        return "admin/profile/profile-edit";
    }

    @RequiresRoles(AuthUserDetails.ROLE_MGMT_USER)
    @RequestMapping(value = "/admin/profile/layout", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult profileLayoutSave(@ModelAttribute("user") User user) {
        Map<String, Object> extraAttributes = user.getExtraAttributes();
        for (Map.Entry<String, Object> me : extraAttributes.entrySet()) {
            String code = me.getKey();
            UserProfileData entity = userProfileDataService.findByUserAndCode(user, code);
            if (entity == null) {
                entity = new UserProfileData();
                entity.setUser(user);
            }
            entity.setCode(code);
            entity.setValue(me.getValue().toString());
            userProfileDataService.save(entity);
        }
        return OperationResult.buildSuccessResult("Interface layout configuration parameters saved successfully");
    }

    @RequiresRoles(AuthUserDetails.ROLE_MGMT_USER)
    @RequestMapping(value = "/admin/profile/password", method = RequestMethod.GET)
    public String modifyPasswordShow(Model model) {
        model.addAttribute("mailServiceEnabled", mailService.isEnabled());
        return "admin/profile/password-edit";
    }

    @RequiresRoles(AuthUserDetails.ROLE_MGMT_USER)
    @RequestMapping(value = "/admin/profile/password", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult modifyPasswordSave(HttpServletRequest request, @RequestParam("oldpasswd") String oldpasswd,
            @RequestParam("newpasswd") String newpasswd) {
        User user = AuthContextHolder.findAuthUser();
        String encodedPasswd = userService.encodeUserPasswd(user, oldpasswd);
        if (!encodedPasswd.equals(user.getPassword())) {
            return OperationResult.buildFailureResult("Original password is incorrect, please re-enter");
        } else {
            Validation.notDemoMode();

         // Update the password expiration date of six months after the
            user.setCredentialsExpireTime(new DateTime().plusMonths(6).toDate());
            userService.save(user, newpasswd);
            return OperationResult.buildSuccessResult("Password change is successful, the next logon with the new password");
        }
    }

    @MetaData("Password expiration forced reset - Display")
    @RequiresRoles(value = AuthUserDetails.ROLE_MGMT_USER)
    @RequestMapping(value = "/admin/profile/credentials-expire", method = RequestMethod.GET)
    public String profileCredentialsExpireShow() {
        return "admin/profile/credentials-expire";
    }

    @MetaData("Password expiration forced reset - Update")
    @RequiresRoles(value = AuthUserDetails.ROLE_MGMT_USER)
    @RequestMapping(value = "/admin/profile/credentials-expire", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult profileCredentialsExpireSave(@RequestParam("newpasswd") String newpasswd) {
        User user = AuthContextHolder.findAuthUser();
        Validation.notDemoMode();

     // Update the password expiration date of six months after the
        user.setCredentialsExpireTime(new DateTime().plusMonths(6).toDate());
        userService.save(user, newpasswd);
        return OperationResult.buildSuccessResult("Password change is successful, the next logon with the new password").setRedirect("/admin");
    }

    @ModelAttribute
    public void prepareModel(Model model) {
        User user = AuthContextHolder.findAuthUser();
        model.addAttribute("user", user);
    }
}
