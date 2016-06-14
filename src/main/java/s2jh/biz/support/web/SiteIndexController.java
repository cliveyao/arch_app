package s2jh.biz.support.web;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;

import lab.s2jh.core.exception.ServiceException;
import lab.s2jh.core.service.BaseService;
import lab.s2jh.core.util.ImageUtils;
import lab.s2jh.core.web.BaseController;
import lab.s2jh.core.web.filter.WebAppContextInitFilter;
import lab.s2jh.core.web.util.ServletUtils;
import lab.s2jh.core.web.view.OperationResult;
import lab.s2jh.module.auth.entity.User;
import lab.s2jh.module.auth.entity.User.AuthTypeEnum;
import lab.s2jh.module.auth.service.UserService;
import lab.s2jh.module.sys.service.SmsVerifyCodeService;
import lab.s2jh.support.service.DynamicConfigService;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import s2jh.biz.shop.entity.SiteUser;
import s2jh.biz.shop.service.SiteUserService;

import com.google.common.collect.Maps;

@Controller
@RequestMapping(value = "/w")
public class SiteIndexController extends BaseController<SiteUser, Long> {

    private final Logger logger = LoggerFactory.getLogger(SiteIndexController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private SiteUserService siteUserService;

    @Autowired
    private SmsVerifyCodeService smsVerifyCodeService;

    @Autowired
    private DynamicConfigService dynamicConfigService;

    @Override
    protected BaseService<SiteUser, Long> getEntityService() {
        return siteUserService;
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    public String wwwIndex(Model model) {
        return "w/index";
    }

    @RequestMapping(value = "/password/reset", method = RequestMethod.GET)
    public String restPasswordShow(Model model) {
        return "w/password-reset";
    }

    @RequestMapping(value = "/password/reset", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult passwordResetSmsValidate(HttpServletRequest request, SiteUser entity, Model model, @RequestParam("mobile") String mobile,
            @RequestParam("smsCode") String smsCode, @RequestParam(value = "newpasswd", required = false) String newpasswd) {
        if (smsVerifyCodeService.verifySmsCode(request, mobile, smsCode)) {
            User user = userService.findByAuthTypeAndAuthUid(AuthTypeEnum.SYS, mobile);
            if (user == null) {
                return OperationResult.buildFailureResult("Number is not registered", "NoUser");
            }
            if (StringUtils.isBlank(newpasswd)) {
                return OperationResult.buildSuccessResult("SMS verification code verification is successful", "SmsOK");
            } else {

            	// Update the password expiration date of six months after the
                user.setCredentialsExpireTime(new DateTime().plusMonths(6).toDate());
                userService.save(user, newpasswd);
                return OperationResult.buildSuccessResult("Password reset is successful, you can immediately use the new system it set the password", "ResetOK");
            }
        } else {
            return OperationResult.buildFailureResult("SMS verification code is not correct");
        }
    }

    @RequestMapping(value = "/image/upload", method = RequestMethod.GET)
    public String imageUploadShow() {
        return "w/imageUpload";
    }

    private static final String TEMP_PHOTO_FILE_PATH = "/temp/photo/";

    @RequestMapping(value = "/image/upload/temp", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult imageUploadTemp(@RequestParam("photo") CommonsMultipartFile photo, HttpServletRequest request) {
        if (photo != null && !photo.isEmpty()) {
            try {
                String rootDir = WebAppContextInitFilter.getInitedWebContextRealPath();
                String fileName = UUID.randomUUID() + "_" + photo.getOriginalFilename();
                File dir = new File(rootDir + TEMP_PHOTO_FILE_PATH);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                String photoFilePath = TEMP_PHOTO_FILE_PATH + fileName;
                logger.debug("Saving file: {}", rootDir + photoFilePath);
                File photoFile = new File(rootDir + photoFilePath);
                photo.transferTo(photoFile);

                BufferedImage bi = ImageIO.read(photoFile);
                int srcWidth = bi.getWidth();  // The width of the source image
                int srcHeight = bi.getHeight();   // Height of source image

                Map<String, Object> userdata = Maps.newHashMap();
                userdata.put("width", srcWidth);
                userdata.put("height", srcHeight);
                userdata.put("src", photoFilePath);
                return OperationResult.buildSuccessResult("Image uploaded successfully", userdata);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return OperationResult.buildFailureResult("Image upload failed");
    }

    @RequestMapping(value = "/image/crop", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult imageCrop(HttpServletRequest request, @RequestParam("bigImage") String bigImage,
            @RequestParam(value = "x", required = false) Integer x, @RequestParam(value = "y", required = false) Integer y,
            @RequestParam(value = "w", required = false) Integer w, @RequestParam(value = "h", required = false) Integer h,
            @RequestParam(value = "size", required = false) Integer size) throws IOException {
        try {
            String rootDir = WebAppContextInitFilter.getInitedWebContextRealPath();
            String bigImagePath = rootDir + bigImage;

         // Determine whether you need to perform the trimming process
            if (x != null && w != null && w > 0) {
            	// Crop image
                ImageUtils.cutImage(bigImagePath, bigImagePath, x, y, w, h);
                if (size != null) {

                	// Zoom to uniform size
                    ImageUtils.zoomImage(bigImagePath, bigImagePath, size, size);
                }
            }
            File photoFile = new File(bigImagePath);
            String path = ServletUtils.writeUploadFile(new FileInputStream(photoFile), photoFile.getName(), photoFile.length());
            if (StringUtils.isNotBlank(path)) {
                return OperationResult.buildSuccessResult("Photos submitted successfully", path);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return OperationResult.buildFailureResult("Image processing failed");
    }

    @RequestMapping(value = "/image/upload/kind-editor", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> imageUpload(HttpServletRequest request, @RequestParam("imgFile") CommonsMultipartFile fileUpload) {
        Map<String, Object> retMap = Maps.newHashMap();
        try {
            if (fileUpload != null && !fileUpload.isEmpty()) {
                String path = ServletUtils.writeUploadFile(fileUpload.getInputStream(), fileUpload.getOriginalFilename(), fileUpload.getSize());
                if (StringUtils.isNotBlank(path)) {
                    String imgViewUrlPrefix = ServletUtils.getReadFileUrlPrefix();
                    retMap.put("error", 0);
                    retMap.put("url", imgViewUrlPrefix + path);
                    retMap.put("path", path);
                    return retMap;
                }
            }
        } catch (IOException e) {
            throw new ServiceException("Upload file error", e);
        }
        retMap.put("error", 1);
        retMap.put("message", "Image processing failed");
        return retMap;
    }

    @RequestMapping(value = "/file/upload/single", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult singleFileUpload(HttpServletRequest request, @RequestParam("fileUpload") CommonsMultipartFile fileUpload) {
        try {
            if (fileUpload != null && !fileUpload.isEmpty()) {
                String path = ServletUtils.writeUploadFile(fileUpload.getInputStream(), fileUpload.getOriginalFilename(), fileUpload.getSize());
                if (StringUtils.isNotBlank(path)) {
                    return OperationResult.buildSuccessResult("File submitted successfully", path);
                }
            }
        } catch (IOException e) {
            throw new ServiceException("Upload file error", e);
        }
        return OperationResult.buildFailureResult("File failed");
    }
}
