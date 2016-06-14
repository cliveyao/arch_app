package lab.s2jh.support.web;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import lab.s2jh.core.annotation.MetaData;
import lab.s2jh.core.cons.GlobalConstant;
import lab.s2jh.core.security.AuthContextHolder;
import lab.s2jh.core.security.AuthUserDetails;
import lab.s2jh.core.security.JcaptchaFormAuthenticationFilter;
import lab.s2jh.core.web.captcha.ImageCaptchaServlet;
import lab.s2jh.core.web.filter.WebAppContextInitFilter;
import lab.s2jh.core.web.util.ServletUtils;
import lab.s2jh.core.web.view.OperationResult;
import lab.s2jh.module.auth.entity.User;
import lab.s2jh.module.auth.service.UserService;
import lab.s2jh.module.sys.entity.NotifyMessage.NotifyMessagePlatformEnum;
import lab.s2jh.module.sys.service.NotifyMessageService;
import lab.s2jh.module.sys.service.SmsVerifyCodeService;
import lab.s2jh.module.sys.service.UserMessageService;
import lab.s2jh.module.sys.service.UserProfileDataService;
import lab.s2jh.support.service.DynamicConfigService;
import lab.s2jh.support.service.SmsService;
import lab.s2jh.support.service.SmsService.SmsMessageTypeEnum;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Maps;

@Controller
public class IndexController {

    @Autowired
    private UserService userService;

    @Autowired
    private SmsVerifyCodeService smsVerifyCodeService;

    @Autowired
    private DynamicConfigService dynamicConfigService;

    @Autowired
    private NotifyMessageService notifyMessageService;

    @Autowired
    private UserMessageService userMessageService;

    @Autowired
    private SmsService smsService;

    @Autowired
    private UserProfileDataService userProfileDataService;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public String defaultIndex() {
        return "w";
    }

    @RequestMapping(value = "/unauthorized", method = RequestMethod.GET)
    public String unauthorizedUrl(HttpServletRequest request, Model model) {
        model.addAttribute("readFileUrlPrefix", ServletUtils.getReadFileUrlPrefix());
        return "error/403";
    }

    @RequiresRoles(AuthUserDetails.ROLE_MGMT_USER)
    @RequestMapping(value = "/admin", method = RequestMethod.GET)
    public String adminIndex(HttpServletRequest request, Model model) {
        model.addAttribute("baiduMapAppkey", dynamicConfigService.getString("baidu_map_appkey"));
        model.addAttribute("buildVersion", dynamicConfigService.getString("build_version"));
        model.addAttribute("buildTimetamp", dynamicConfigService.getString("build_timestamp"));
        User user = AuthContextHolder.findAuthUser();
        model.addAttribute("layoutAttributes", userProfileDataService.findMapDataByUser(user));
        model.addAttribute("readFileUrlPrefix", ServletUtils.getReadFileUrlPrefix());
        model.addAttribute("globalProperties", dynamicConfigService.getAllPrperties());
        return "admin/index";
    }

    @RequiresRoles(AuthUserDetails.ROLE_MGMT_USER)
    @RequestMapping(value = "/admin/", method = RequestMethod.GET)
    public String adminIndexMore(HttpServletRequest request, Model model) {
        return adminIndex(request, model);
    }

    @RequestMapping(value = "/{source}/login", method = RequestMethod.GET)
    public String adminLogin(Model model, @PathVariable("source") String source) {
        model.addAttribute("buildVersion", dynamicConfigService.getString("build_version"));
        model.addAttribute("buildTimetamp", dynamicConfigService.getString("build_timestamp"));

     // Function switch Self Registry Account
        model.addAttribute("mgmtSignupEnabled", !dynamicConfigService.getBoolean(GlobalConstant.cfg_mgmt_signup_disabled, true));
        return source + "/login";
    }

    /** 
     * <H3> APP Interface : Log . </ H3>
     *
     * <P>
     * Business list of input parameters :
     * <Ul>
     * <Li> <b> username </ b> Account </ li>
     * <Li> <b> password </ b> Password </ li>
     * <Li> <b> uuid </ b> to uniquely identify a device or application </ li>
     * </ Ul>
     * </ P>
     *
     * <P>
     * Business output parameter list :
     * <Ul>
     * <Li> <b> token </ b> The login random token Token, currently set valid for six months . APP after taking this token value stored in the persistence of the present application , in the follow-up visit or the next time the application to reopen this token in the form attached to the HTTP Header Request information : ACCESS-TOKEN = {token} </ li>
     * </ Ul>
     * </ P>
     *
     * @return {@link OperationResult} General Standard Architecture
     * 
     */
    @RequestMapping(value = "/app/login", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult appLogin(HttpServletRequest request, Model model) {

    	// Get Certified exception class name
        AuthenticationException ae = (AuthenticationException) request.getAttribute(FormAuthenticationFilter.DEFAULT_ERROR_KEY_ATTRIBUTE_NAME);
        if (ae == null) {
            Map<String, Object> datas = Maps.newHashMap();
            datas.put("token", AuthContextHolder.getAuthUserDetails().getAccessToken());
            return OperationResult.buildSuccessResult(datas);
        } else {
            OperationResult result = OperationResult.buildFailureResult(ae.getMessage());
            Boolean captchaRequired = (Boolean) request.getAttribute(JcaptchaFormAuthenticationFilter.KEY_AUTH_CAPTCHA_REQUIRED);
            Map<String, Object> datas = Maps.newHashMap();
            datas.put("captchaRequired", captchaRequired);
            datas.put("captchaImageUrl", WebAppContextInitFilter.getInitedWebContextFullUrl() + "/pub/jcaptcha.servlet");
            result.setData(datas);
            return result;
        }
    }

    /**
     * PC site log fails , to the login screen. Forms / login POST request will first be processed Shiro interception , after the authentication failure will trigger a call to this method
     * @param Source Log sources , @see SourceUsernamePasswordToken.AuthSourceEnum
     * @param request
     * @param model
     * @return
     */
    @RequestMapping(value = "/{source}/login", method = RequestMethod.POST)
    public String loginFailure(@PathVariable("source") String source, HttpServletRequest request, Model model) {
        model.addAttribute("buildVersion", dynamicConfigService.getString("build_version"));
        model.addAttribute("buildTimetamp", dynamicConfigService.getString("build_timestamp"));

     // Function switch Self Registry Account
        model.addAttribute("mgmtSignupEnabled", !dynamicConfigService.getBoolean(GlobalConstant.cfg_mgmt_signup_disabled, true));

     // Get Certified exception class name
        AuthenticationException ae = (AuthenticationException) request.getAttribute(FormAuthenticationFilter.DEFAULT_ERROR_KEY_ATTRIBUTE_NAME);
        if (ae != null) {
            model.addAttribute("error", ae.getMessage());
            return source + "/login";
        } else {
            return "redirect:/" + source;
        }

    }

    /** 
     * <H3> APP Interface : send SMS verification code . </ H3>
     * <P> interface to send a verification code text message to all phone numbers from , but may in extreme cases through the global system parameters Close Send SMS feature to open the phone. </ P>
     * <P> This interface is primarily for open registration verification code function , if the call end function can be clear to send text messages to registered users, such as retrieve your password feature, please use / user-sms-code interfaces < / p>
     *
     *
     * <P>
     * Business list of input parameters :
     * <Ul>
     * <Li> <b> mobile </ b> phone number </ li>
     * </ Ul>
     * </ P>
     *
     * @return {@link OperationResult} General Standard Architecture
     * 
     */
    @RequestMapping(value = "/send-sms-code/{mobile}", method = RequestMethod.GET)
    @ResponseBody
    public OperationResult sendSmsCode(@PathVariable("mobile") String mobile, HttpServletRequest request) {

        String captcha = request.getParameter("code");
        if (StringUtils.isNotBlank(captcha)) {
            boolean result = ImageCaptchaServlet.validateResponse(request, captcha);
            if (!result) {
                return OperationResult.buildFailureResult("Image verification code is not correct");
            }
        }

        String code = smsVerifyCodeService.generateSmsCode(request, mobile, false);
        String msg = "Your operating verification code is :" + code + ". Do not offer to messages you receive a verification code to anyone. As a non- operating , please ignore this message.";
        String errorMessage = smsService.sendSMS(msg, mobile, SmsMessageTypeEnum.VerifyCode);
        if (StringUtils.isBlank(errorMessage)) {
            return OperationResult.buildSuccessResult();
        } else {
            return OperationResult.buildFailureResult(errorMessage);
        }
    }

    /** 
     * <H3> APP Interface : Only the platform has been verified phone number to send SMS verification code . </ H3>
     * <P> This interface is mainly applied to the authenticated successfully registered users via SMS text messages , such as retrieve password function , please use other open registration function / send-sms-code interfaces </ p>
     * <P>
     * Business list of input parameters :
     * <Ul>
     * <Li> <b> mobile </ b> phone number </ li>
     * </ Ul>
     * </ P>
     *
     * @return {@link OperationResult} General Standard Architecture
     * 
     */
    @RequestMapping(value = "/user-sms-code/{mobile}", method = RequestMethod.GET)
    @ResponseBody
    public OperationResult userSmsCode(@PathVariable("mobile") String mobile, HttpServletRequest request) {
        String code = smsVerifyCodeService.generateSmsCode(request, mobile, true);
        String msg = "Your operating verification code is :" + code + ". Do not offer to messages you receive a verification code to anyone. As a non- operating , please ignore this message.";
        String errorMessage = smsService.sendSMS(msg, mobile, SmsMessageTypeEnum.VerifyCode);
        if (StringUtils.isBlank(errorMessage)) {
            return OperationResult.buildSuccessResult();
        } else {
            return OperationResult.buildFailureResult(errorMessage);
        }
    }

    /**
     * 
     * @param platform 平台
     * @return
     */
    @MetaData("The user does not read the number of announcements")
    @RequestMapping(value = "/notify-message/count", method = RequestMethod.GET)
    @ResponseBody
    public OperationResult notifyMessageCount(HttpServletRequest request) {
        User user = AuthContextHolder.findAuthUser();
        String platform = request.getParameter("platform");
        if (StringUtils.isBlank(platform)) {
            platform = NotifyMessagePlatformEnum.web_admin.name();
        }

        return OperationResult.buildSuccessResult(notifyMessageService.findCountToRead(user, platform));
    }

    @MetaData("The user does not read the number of messages")
    @RequestMapping(value = "/user-message/count", method = RequestMethod.GET)
    @ResponseBody
    public OperationResult userMessageCount() {
        User user = AuthContextHolder.findAuthUser();
        if (user != null) {
            return OperationResult.buildSuccessResult(userMessageService.findCountToRead(user));
        } else {
            return OperationResult.buildSuccessResult(0);
        }
    }
}
