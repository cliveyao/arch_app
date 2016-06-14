package lab.s2jh.support.web;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import lab.s2jh.core.annotation.MenuData;
import lab.s2jh.core.annotation.MetaData;
import lab.s2jh.core.cons.GlobalConstant;
import lab.s2jh.core.pagination.GroupPropertyFilter;
import lab.s2jh.core.pagination.PropertyFilter;
import lab.s2jh.core.pagination.PropertyFilter.MatchType;
import lab.s2jh.core.security.AuthContextHolder;
import lab.s2jh.core.security.AuthUserDetails;
import lab.s2jh.core.security.PasswordService;
import lab.s2jh.core.security.ShiroJdbcRealm;
import lab.s2jh.core.web.captcha.ImageCaptchaServlet;
import lab.s2jh.core.web.filter.WebAppContextInitFilter;
import lab.s2jh.core.web.view.OperationResult;
import lab.s2jh.module.auth.entity.SignupUser;
import lab.s2jh.module.auth.entity.User;
import lab.s2jh.module.auth.entity.User.AuthTypeEnum;
import lab.s2jh.module.auth.service.SignupUserService;
import lab.s2jh.module.auth.service.UserService;
import lab.s2jh.module.sys.entity.NotifyMessage;
import lab.s2jh.module.sys.entity.UserMessage;
import lab.s2jh.module.sys.service.MenuService;
import lab.s2jh.module.sys.service.NotifyMessageService;
import lab.s2jh.module.sys.service.UserMessageService;
import lab.s2jh.module.sys.vo.NavMenuVO;
import lab.s2jh.support.service.DynamicConfigService;
import lab.s2jh.support.service.MailService;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.util.CollectionUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.common.collect.Lists;

@Controller
public class AdminController {

    @Autowired
    private MenuService menuService;

    @Autowired
    private UserService userService;

    @Autowired
    private SignupUserService signupUserService;

    @Autowired
    private PasswordService passwordService;

    @Autowired
    private MailService mailService;

    @Autowired
    private DynamicConfigService dynamicConfigService;

    @Autowired
    private NotifyMessageService notifyMessageService;

    @Autowired
    private UserMessageService userMessageService;

    @Autowired(required = false)
    private ShiroJdbcRealm shiroJdbcRealm;

    @Autowired(required = false)
    private AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor;

    @RequiresRoles(AuthUserDetails.ROLE_MGMT_USER)
    @RequestMapping(value = "/admin/dashboard", method = RequestMethod.GET)
    public String dashboard(Model model) {
        return "admin/dashboard";
    }

    /**
     *Calculations show that user login menu data
     */
    @RequestMapping(value = "/admin/menus", method = RequestMethod.GET)
    @ResponseBody
    public List<NavMenuVO> navMenu(HttpSession session) {
        User user = AuthContextHolder.findAuthUser();

         // If not logged in direct return empty
        if (user == null) {
            return Lists.newArrayList();
        }
        return menuService.processUserMenu(user);
    }

    @RequestMapping(value = "/admin/password/forget", method = RequestMethod.GET)
    public String forgetPasswordShow(Model model) {
        model.addAttribute("mailServiceEnabled", mailService.isEnabled());
        return "admin/pub/password-forget";
    }

    @RequestMapping(value = "/admin/password/forget", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult forgetPasswordSave(HttpServletRequest request, @RequestParam("uid") String uid, @RequestParam("captcha") String captcha) {
        if (!ImageCaptchaServlet.validateResponse(request, captcha)) {
            return OperationResult.buildFailureResult("The verification code is incorrect. Please re-enter");
        }
        User user = userService.findByAuthTypeAndAuthUid(AuthTypeEnum.SYS, uid);
        if (user == null) {
            user = userService.findByProperty("email", uid);
        }
        if (user == null) {
            return OperationResult.buildFailureResult("Match the account information is not found, please contact the administrator Processing");
        }
        String email = user.getEmail();
        if (StringUtils.isBlank(email)) {
            return OperationResult.buildFailureResult("The current account is not set registered mail , please contact the administrator to set the mailbox after this operation");
        }

        userService.requestResetPassword(WebAppContextInitFilter.getInitedWebContextFullUrl(), user);
        return OperationResult.buildSuccessResult("Forgot password Request successfully processed . Reset Password message has been sent to :" + email);
    }

    @RequestMapping(value = "/admin/password/reset", method = RequestMethod.GET)
    public String restPasswordShow(Model model) {
        return "admin/pub/password-reset";
    }

    @RequestMapping(value = "/admin/password/reset", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult resetPasswordSave(HttpServletRequest request, HttpServletResponse response, @RequestParam("uid") String uid,
            @RequestParam("code") String code, @RequestParam("newpasswd") String newpasswd, RedirectAttributes redirectAttributes) throws IOException {
        User user = userService.findByAuthTypeAndAuthUid(AuthTypeEnum.SYS, uid);
        if (user != null) {
            if (code.equals(user.getUserExt().getRandomCode())) {
                //user.setRandomCode(null);
                // Update the password expiration date of six months after the
                user.setCredentialsExpireTime(new DateTime().plusMonths(6).toDate());
                userService.save(user, newpasswd);
                return OperationResult.buildSuccessResult("Password reset is successful, you can immediately use the new system it set the password").setRedirect("/admin/login");
            } else {
                return OperationResult.buildFailureResult("Verification code is incorrect or expired , please try to retrieve cryptographic operations");
            }
        }
        return OperationResult.buildFailureResult("operation failed");
    }

    @RequestMapping(value = "/admin/signup", method = RequestMethod.GET)
    public String signupShow(Model model) {
        model.addAttribute("mailServiceEnabled", mailService.isEnabled());
        return "admin/pub/signup";
    }

    @RequestMapping(value = "/admin/signup", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult signupSave(HttpServletRequest request, @RequestParam("captcha") String captcha, @ModelAttribute("entity") SignupUser entity) {
        if (!ImageCaptchaServlet.validateResponse(request, captcha)) {
            return OperationResult.buildFailureResult("The verification code is incorrect. Please re-enter");
        }
        if (dynamicConfigService.getBoolean(GlobalConstant.cfg_mgmt_signup_disabled, false)) {
            return OperationResult.buildFailureResult("Write system development account registration function , if in doubt please contact the administrator");
        }
        signupUserService.signup(entity, request.getParameter("password"));
        return OperationResult.buildSuccessResult("registration success. Administrators need to wait for approval before to log on through .");
    }
    
    /**
     *Verify phone Uniqueness
     * <p>
     * Business list of input parameters :
     * <ul>
     * <li> <b> mobile </ b> phone number </ li>
     * </ul>
     * </p>
     * @param request
     * @return
     */
    @RequestMapping(value = "/admin/signup/unique/mobile", method = RequestMethod.GET)
    @ResponseBody
    public boolean authMobileUnique(HttpServletRequest request) {
        String mobile = request.getParameter("mobile");
        if (!CollectionUtils.isEmpty(userService.findByMobile(mobile))) {
            return false;
        }
        if (!CollectionUtils.isEmpty(signupUserService.findByMobile(mobile))) {
            return false;
        }
        return true;
    }

    /**
     * Uniqueness verification email
     * <p>
     * Business list of input parameters :
     * <ul>
     * <li> <b> email </ b> email </ li>
     * </ul>
     * </p>
     * @param request
     * @return
     */
    @RequestMapping(value = "/admin/signup/unique/email", method = RequestMethod.GET)
    @ResponseBody
    public boolean authEmailUnique(HttpServletRequest request) {
        String email = request.getParameter("email");
        if (!CollectionUtils.isEmpty(userService.findByEmail(email))) {
            return false;
        }
        if (!CollectionUtils.isEmpty(signupUserService.findByEmail(email))) {
            return false;
        }
        return true;
    }

    /**
     * Uniqueness verification email
     * <p>
     * Business list of input parameters :
     * <ul>
     * <li> <b> email </ b> email </ li>
     * </ul>
     * </p>
     * @param request
     * @return
     */
    @RequestMapping(value = "/admin/signup/unique/uid", method = RequestMethod.GET)
    @ResponseBody
    public boolean authUidUnique(HttpServletRequest request) {
        String authUid = request.getParameter("authUid");
        if (userService.findByAuthUid(authUid) != null) {
            return false;
        }
        if (signupUserService.findByAuthUid(authUid) != null) {
            return false;
        }
        return true;
    }

    @MenuData("Personal information : Bulletin message")
    @RequiresRoles(value = AuthUserDetails.ROLE_MGMT_USER)
    @RequestMapping(value = "/admin/profile/notify-message", method = RequestMethod.GET)
    public String notifyMessageIndex() {
        return "admin/profile/notifyMessage-index";
    }

    @MetaData("Announcement Message List")
    @RequiresRoles(value = AuthUserDetails.ROLE_MGMT_USER)
    @RequestMapping(value = "/admin/profile/notify-message-list", method = RequestMethod.GET)
    public String notifyMessageList(HttpServletRequest request, Model model) {
        User user = AuthContextHolder.findAuthUser();
        List<NotifyMessage> notifyMessages = null;
        String readed = request.getParameter("readed");
        if (StringUtils.isBlank(readed)) {
            notifyMessages = notifyMessageService.findStatedEffectiveMessages(user, "web_admin", null);
        } else {
            notifyMessages = notifyMessageService.findStatedEffectiveMessages(user, "web_admin",
                    BooleanUtils.toBoolean(request.getParameter("readed")));
        }
        model.addAttribute("notifyMessages", notifyMessages);
        return "admin/profile/notifyMessage-list";
    }

    @MetaData("News bulletin read")
    @RequiresRoles(value = AuthUserDetails.ROLE_MGMT_USER)
    @RequestMapping(value = "/admin/profile/notify-message-view/{messageId}", method = RequestMethod.GET)
    public String notifyMessageView(@PathVariable("messageId") Long messageId, Model model) {
        User user = AuthContextHolder.findAuthUser();
        NotifyMessage notifyMessage = notifyMessageService.findOne(messageId);
        notifyMessageService.processUserRead(notifyMessage, user);
        model.addAttribute("notifyMessage", notifyMessage);
        return "admin/profile/notifyMessage-view";
    }

    @MenuData("Personal Information : Personal Message")
    @RequiresRoles(value = AuthUserDetails.ROLE_MGMT_USER)
    @RequestMapping(value = "/admin/profile/user-message", method = RequestMethod.GET)
    public String userMessageIndex() {
        return "admin/profile/userMessage-index";
    }

    @MetaData("Personal message list")
    @RequiresRoles(value = AuthUserDetails.ROLE_MGMT_USER)
    @RequestMapping(value = "/admin/profile/user-message-list", method = RequestMethod.GET)
    public String userMessageList(HttpServletRequest request, Model model) {
        User user = AuthContextHolder.findAuthUser();
        Pageable pageable = PropertyFilter.buildPageableFromHttpRequest(request);
        GroupPropertyFilter groupFilter = GroupPropertyFilter.buildFromHttpRequest(UserMessage.class, request);
        groupFilter.append(new PropertyFilter(MatchType.EQ, "targetUser", user));
        String readed = request.getParameter("readed");
        if (StringUtils.isNotBlank(readed)) {
            if (BooleanUtils.toBoolean(request.getParameter("readed"))) {
                groupFilter.append(new PropertyFilter(MatchType.NN, "firstReadTime", Boolean.TRUE));
            } else {
                groupFilter.append(new PropertyFilter(MatchType.NU, "firstReadTime", Boolean.TRUE));
            }
        }
        Page<UserMessage> pageData = userMessageService.findByPage(groupFilter, pageable);
        model.addAttribute("pageData", pageData);
        return "admin/profile/userMessage-list";
    }

    @MetaData("Read a personal message")
    @RequiresRoles(value = AuthUserDetails.ROLE_MGMT_USER)
    @RequestMapping(value = "/admin/profile/user-message-view/{messageId}", method = RequestMethod.GET)
    public String userMessageView(@PathVariable("messageId") Long messageId, Model model) {
        User user = AuthContextHolder.findAuthUser();
        UserMessage userMessage = userMessageService.findOne(messageId);
        userMessageService.processUserRead(userMessage, user);
        model.addAttribute("userMessage", userMessage);
        return "admin/profile/userMessage-view";
    }
}
