package lab.s2jh.core.web.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import lab.s2jh.core.annotation.MetaData;
import lab.s2jh.core.context.SpringContextHolder;
import lab.s2jh.core.security.AuthContextHolder;
import lab.s2jh.core.util.DateUtils;
import lab.s2jh.core.util.UidUtils;
import lab.s2jh.core.web.filter.WebAppContextInitFilter;
import lab.s2jh.core.web.json.DateJsonSerializer;
import lab.s2jh.core.web.json.DateTimeJsonSerializer;
import lab.s2jh.core.web.json.ShortDateTimeJsonSerializer;
import lab.s2jh.support.service.DynamicConfigService;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.util.ClassUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import ch.qos.logback.classic.ClassicConstants;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class ServletUtils {

    private final static Logger logger = LoggerFactory.getLogger(ServletUtils.class);

 // \ B is the word boundary ( attached to the two spaced logical ( alphabetic characters and non-alphabetic characters ) between ) ,
    // String at compile time are transcoded once it is "\\ b"
    // \ B word is the internal logic interval ( interval logical connection for the two letters between characters )   
    static String phoneReg = "\\b(ip(hone|od)|android|opera m(ob|in)i" + "|windows (phone|ce)|blackberry"
            + "|s(ymbian|eries60|amsung)|p(laybook|alm|rofile/midp" + "|laystation portable)|nokia|fennec|htc[-_]"
            + "|mobile|up.browser|[1-4][0-9]{2}x[1-4][0-9]{2})\\b";
    static String tableReg = "\\b(ipad|tablet|(Nexus 7)|up.browser" + "|[1-4][0-9]{2}x[1-4][0-9]{2})\\b";
 // Mobile devices are the matching : end phone , tablet
    static java.util.regex.Pattern phonePat = java.util.regex.Pattern.compile(phoneReg, java.util.regex.Pattern.CASE_INSENSITIVE);
    static java.util.regex.Pattern tablePat = java.util.regex.Pattern.compile(tableReg, java.util.regex.Pattern.CASE_INSENSITIVE);

    /**
     * Request Parameters obtained with the same prefix , copy from spring WebUtils.
     *
     * Return the results Parameter name prefix has been removed .
     */
    public static Map<String, Object> buildParameters(ServletRequest request) {
        Enumeration<?> paramNames = request.getParameterNames();
        Map<String, Object> params = new TreeMap<String, Object>();
        String prefix = "search_";
        while ((paramNames != null) && paramNames.hasMoreElements()) {
            String paramName = (String) paramNames.nextElement();
            if ("".equals(prefix) || paramName.startsWith(prefix)) {
                String unprefixed = paramName.substring(prefix.length());
                String[] values = request.getParameterValues(paramName);
                if ((values == null) || (values.length == 0)) {
                    // Do nothing, no values found at all.
                } else if (values.length > 1) {
                    params.put(unprefixed, values);
                } else {
                    String val = values[0];
                    if (StringUtils.isNotBlank(val)) {
                        params.put(unprefixed, val);
                    }
                }
            }
        }
        return params;
    }

    /**
     * Download the file object rendering based on the response file
     * @param response
     * @param file
     */
    public static void renderFileDownload(HttpServletResponse response, File file) {
        OutputStream output = null;
        try {
        	
            String encodedfileName = new String(file.getName().getBytes("UTF-8"), "ISO8859-1");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedfileName + "\"");

            output = response.getOutputStream();
            FileUtils.copyFile(file, output);
            response.flushBuffer();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(output);
        }
    }

    private static Map<String, Map<String, Object>> entityValidationRulesMap = Maps.newHashMap();

    /**
     * Get validation rules based on a hash calculated identify Construction
     * @param id
     * @return
     */
    public static Map<String, Object> buildValidateRules(String entityClazz) {
        Map<String, Object> nameRules = entityValidationRulesMap.get(entityClazz);
        try {
        	// The development model for the timely entry into force of the revised calculation each comments
            if (nameRules == null || DynamicConfigService.isDevMode()) {
                nameRules = Maps.newHashMap();
                entityValidationRulesMap.put(entityClazz, nameRules);
                Class<?> clazz = ClassUtils.forName(entityClazz);

                Assert.notNull(clazz, "验证缓存数据错误");
                Set<Field> fields = Sets.newHashSet(clazz.getDeclaredFields());
                clazz = clazz.getSuperclass();
                while (!clazz.equals(Object.class)) {
                    fields.addAll(Sets.newHashSet(clazz.getDeclaredFields()));
                    clazz = clazz.getSuperclass();
                }

                for (Field field : fields) {
                    if (Modifier.isStatic(field.getModifiers()) || !Modifier.isPrivate(field.getModifiers())
                            || Collection.class.isAssignableFrom(field.getType())) {
                        continue;
                    }
                    String name = field.getName();
                    if ("id".equals(name) || "version".equals(name)) {
                        continue;
                    }
                    Map<String, Object> rules = Maps.newHashMap();

                    // TODO process optimization nested property
                    // If the entity is an object type , the general form element name is defined as entity.id, so the extra is added to the id attribute validation rules
                    // If (PersistableEntity.class.isAssignableFrom (field.getType ())) {
                    // NameRules.put (name + ".id", rules);
                    // }

                    MetaData metaData = field.getAnnotation(MetaData.class);
                    if (metaData != null) {
                        String tooltips = metaData.tooltips();
                        if (StringUtils.isNotBlank(tooltips)) {
                            rules.put("tooltips", tooltips);
                        }
                    }

                    Class<?> retType = field.getType();
                    Column column = field.getAnnotation(Column.class);

                    if (column != null) {
                        if (retType != Boolean.class && column.nullable() == false) {
                            rules.put("required", true);
                        }
                        if (column.unique() == true) {
                            rules.put("unique", true);
                        }
                        if (column.updatable() == false) {
                            rules.put("readonly", true);
                        }
                        if (column.length() > 0 && retType == String.class && field.getAnnotation(Lob.class) == null) {
                            rules.put("maxlength", column.length());
                        }
                    }

                    JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
                    if (joinColumn != null) {
                        if (joinColumn.nullable() == false) {
                            rules.put("required", true);
                        }
                    }

                    if (retType == Date.class) {
                        Temporal temporal = field.getAnnotation(Temporal.class);
                        if (temporal != null && temporal.value().equals(TemporalType.TIMESTAMP)) {
                            rules.put("timestamp", true);
                        } else {
                            rules.put("date", true);
                        }

                        DateTimeFormat dateTimeFormat = field.getAnnotation(DateTimeFormat.class);
                        if (dateTimeFormat != null) {
                            if (DateUtils.DEFAULT_DATE_FORMAT.equals(dateTimeFormat.pattern())) {
                                rules.put("date", true);
                            } else if (DateUtils.DEFAULT_TIME_FORMAT.equals(dateTimeFormat.pattern())) {
                                rules.put("timestamp", true);
                            } else if (DateUtils.SHORT_TIME_FORMAT.equals(dateTimeFormat.pattern())) {
                                rules.put("shortTimestamp", true);
                            } else {
                                rules.put("date", true);
                            }
                        }

                        JsonSerialize jsonSerialize = field.getAnnotation(JsonSerialize.class);
                        if (jsonSerialize != null) {
                            if (DateJsonSerializer.class == jsonSerialize.using()) {
                                rules.put("date", true);
                            } else if (DateTimeJsonSerializer.class == jsonSerialize.using()) {
                                rules.put("timestamp", true);
                                rules.remove("date");
                            } else if (ShortDateTimeJsonSerializer.class == jsonSerialize.using()) {
                                rules.put("shortTimestamp", true);
                                rules.remove("date");
                            }
                        }
                    } else if (retType == BigDecimal.class) {
                        rules.put("number", true);
                    } else if (retType == Integer.class || retType == Long.class) {
                        rules.put("digits", true);
                    }

                    Size size = field.getAnnotation(Size.class);
                    if (size != null) {
                        if (size.min() > 0) {
                            rules.put("minlength", size.min());
                        }
                        if (size.max() < Integer.MAX_VALUE) {
                            rules.put("maxlength", size.max());
                        }
                    }

                    Pattern pattern = field.getAnnotation(Pattern.class);
                    if (pattern != null) {
                        rules.put("regex", pattern.regexp());
                    }

                    if (rules.size() > 0) {
                        nameRules.put(name, rules);

                    }
                }

            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return nameRules;
    }

    public static Map<String, String> buildRequestInfoDataMap(HttpServletRequest request, boolean verbose) {

        Map<String, String> dataMap = Maps.newLinkedHashMap();

        // Request the relevant parameters, properties and other data assembled
        dataMap.put("req.user", AuthContextHolder.getAuthUserDisplay());
        dataMap.put("req.method", request.getMethod());
        dataMap.put(ClassicConstants.REQUEST_REQUEST_URI, request.getRequestURI());
        dataMap.put(ClassicConstants.REQUEST_USER_AGENT_MDC_KEY, request.getHeader("User-Agent"));

        String clientId = request.getHeader("X-Forwarded-For");
        if (StringUtils.isBlank(clientId)) {
            clientId = request.getRemoteHost();
        }
        dataMap.put("req.clientId", clientId);

        if (verbose) {
            dataMap.put(ClassicConstants.REQUEST_QUERY_STRING, request.getQueryString());
            dataMap.put("req.contextPath", request.getContextPath());
            dataMap.put(ClassicConstants.REQUEST_REMOTE_HOST_MDC_KEY, request.getRemoteHost());
            dataMap.put("req.remotePort", String.valueOf(request.getRemotePort()));
            dataMap.put("req.remoteUser", request.getRemoteUser());
            dataMap.put("req.localAddr", request.getLocalAddr());
            dataMap.put("req.localName", request.getLocalName());
            dataMap.put("req.localPort", String.valueOf(request.getLocalPort()));
            dataMap.put("req.serverName", request.getServerName());
            dataMap.put("req.serverPort", String.valueOf(request.getServerPort()));
            dataMap.put(ClassicConstants.REQUEST_X_FORWARDED_FOR, request.getHeader("X-Forwarded-For"));
            dataMap.put(ClassicConstants.REQUEST_REQUEST_URL, request.getRequestURL().toString());
        }

        Enumeration<?> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = (String) paramNames.nextElement();
            String paramValue = StringUtils.join(request.getParameterValues(paramName), ",");
            if (paramValue != null && paramValue.length() > 100) {
                paramValue = paramValue.substring(0, 100) + "...";
            }
            dataMap.put("req.param[" + paramName + "]", paramValue);
        }

        if (request instanceof MultipartHttpServletRequest) {
        	// Transition to MultipartHttpRequest
            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
         // Get file upload ( according to the front desk to get the file upload name names )
            MultiValueMap<String, MultipartFile> multiValueMap = multipartRequest.getMultiFileMap();
            for (String key : multiValueMap.keySet()) {
                dataMap.put("req.part[" + key + "]", multiValueMap.getFirst(key).getName());
            }
        }

        if (verbose) {
            Enumeration<?> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = (String) headerNames.nextElement();
                dataMap.put("req.header[" + headerName + "]", request.getHeader(headerName));
            }

            Enumeration<?> attrNames = request.getAttributeNames();
            while (attrNames.hasMoreElements()) {
                String attrName = (String) attrNames.nextElement();
                Object attrValue = request.getAttribute(attrName);
                if (attrValue == null) {
                    attrValue = "NULL";
                }
                String attr = attrValue.toString();
                if (attr != null && attr.toString().length() > 100) {
                    attr = attr.substring(0, 100) + "...";
                }
                dataMap.put("req.attr[" + attrName + "]", attr);
            }

            HttpSession session = request.getSession(false);
            if (session != null) {
                Enumeration<?> sessionAttrNames = session.getAttributeNames();
                while (sessionAttrNames.hasMoreElements()) {
                    String attrName = (String) sessionAttrNames.nextElement();
                    Object attrValue = session.getAttribute(attrName);
                    if (attrValue == null) {
                        attrValue = "NULL";
                    }
                    String attr = attrValue.toString();
                    if (attr != null && attr.toString().length() > 100) {
                        attr = attr.toString().substring(0, 100) + "...";
                    }
                    dataMap.put("session.attr[" + attrName + "]", attr);
                }
            }
        }
        return dataMap;
    }

    private static String readFileUrlPrefix;

    /**
     * File Display URL prefix
     * @return
     */
    public static String getReadFileUrlPrefix() {
        if (readFileUrlPrefix == null) {
            DynamicConfigService dynamicConfigService = SpringContextHolder.getBean(DynamicConfigService.class);
            readFileUrlPrefix = dynamicConfigService.getString("read_file_url_prefix");
            if (StringUtils.isBlank(readFileUrlPrefix)) {
                readFileUrlPrefix = WebAppContextInitFilter.getInitedWebContextFullUrl();
            }
        }
        return readFileUrlPrefix;
    }

    /**
     * The URL parsing process , if http heading directly returned, otherwise add the file access path prefix
     * @return
     */
    public static String parseReadFileUrl(String url) {
        if (StringUtils.isBlank(url)) {
            return null;
        }
        if (url.startsWith("http") || url.startsWith("itms-services://")) {
            return url;
        }
        return getReadFileUrlPrefix() + url;
    }

    /**
     * Based on the URL / segmentation , the path of the Chinese part of the UTF8 encoding and then assembled to make return
     * @return
     */
    public static String encodeUtf8Url(String url) {
        if (url == null) {
            return null;
        }
        String[] splits = url.split("/");
        List<String> urls = Lists.newArrayList();
        try {
            for (String split : splits) {
                if (StringUtils.isNotBlank(split)) {
                    urls.add(URLEncoder.encode(split, "UTF-8"));
                } else {
                    urls.add("");
                }
            }
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);
        }
        return StringUtils.join(urls, "/");
    }

    private static String staticFileUploadDir;

    /**
     *Get file upload root directory : write_upload_file_dir take priority parameter value , if not defined then take webapp / upload
     * @return Return to image relative path access
     */
    public static String writeUploadFile(InputStream fis, String name, long length) {
        if (staticFileUploadDir == null) {
            DynamicConfigService dynamicConfigService = SpringContextHolder.getBean(DynamicConfigService.class);
            staticFileUploadDir = dynamicConfigService.getString("write_upload_file_dir");
            if (StringUtils.isBlank(staticFileUploadDir)) {
                staticFileUploadDir = WebAppContextInitFilter.getInitedWebContextRealPath();
            }
            if (staticFileUploadDir.endsWith(File.separator)) {
                staticFileUploadDir = staticFileUploadDir.substring(0, staticFileUploadDir.length() - 1);
            }
            logger.info("Setup file upload root dir:  {}", staticFileUploadDir);
        }

     // Simple approach with UUID as the primary key , each will create a file upload objects and data recording , easy to manage , but the presence of the same file repeat save situation
        String id = UidUtils.UID();

     // Add date packet processing , on the one hand to facilitate visually see upload date information for batch processing, on the other hand the number of rational grouping control directory hierarchy and avoid excessive single catalog file
        DateTime now = new DateTime();
        StringBuilder sb = new StringBuilder();
        int year = now.getYear();
        sb.append("/" + year);
        String month = "";
        int monthOfYear = now.getMonthOfYear();
        if (monthOfYear < 10) {
            month = "0" + monthOfYear;
        } else {
            month = "" + monthOfYear;
        }
        String day = "";
        int dayOfMonth = now.getDayOfMonth();
        if (dayOfMonth < 10) {
            day = "0" + dayOfMonth;
        } else {
            day = "" + dayOfMonth;
        }
        sb.append("/" + month);
        sb.append("/" + day);
        Assert.notNull(id, "id is required to buildInstance");
        sb.append("/" + id);

        String path = "/upload/" + sb + "/" + name;
        String fullPath = staticFileUploadDir + path;
        logger.debug("Saving upload file: {}", fullPath);
        try {
            FileUtils.copyInputStreamToFile(fis, new File(fullPath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return path;

    }

    public static boolean isMobileAndroidClient(HttpServletRequest request) {
        String android = request.getParameter("_android_");
        if (BooleanUtils.toBoolean(android)) {
            return true;
        }
        String userAgent = request.getHeader("user-agent");
        if (StringUtils.isBlank(userAgent)) {
            return false;
        }
        if (userAgent.toLowerCase().contains("android")) {
            return true;
        }
        return false;
    }

    public static boolean isMobileIOSClient(HttpServletRequest request) {
        String ios = request.getParameter("_ios_");
        if (BooleanUtils.toBoolean(ios)) {
            return true;
        }
        String userAgent = request.getHeader("user-agent");
        if (StringUtils.isBlank(userAgent)) {
            return false;
        }
        userAgent = userAgent.toLowerCase();
        if (userAgent.contains("iphone") || userAgent.contains("ipod") || userAgent.contains("ipad")) {
            return true;
        }
        return false;
    }

    public static boolean isMobileClient(HttpServletRequest request) {
    	// Special parameter handling
        String mobile = request.getParameter("_mobile_");
        if (BooleanUtils.toBoolean(mobile)) {
            return true;
        }
        String userAgent = request.getHeader("USER-AGENT");
        if (null == userAgent) {
            userAgent = "";
        }
     // Match
        Matcher matcherPhone = phonePat.matcher(userAgent);
        Matcher matcherTable = tablePat.matcher(userAgent);
        if (matcherPhone.find() || matcherTable.find()) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isMicroMessengerClient(HttpServletRequest request) {
    	// Special parameter handling
        String mobile = request.getParameter("_weixin_");
        if (BooleanUtils.toBoolean(mobile)) {
            return true;
        }
        String userAgent = request.getHeader("user-agent");
        if (StringUtils.isBlank(userAgent)) {
            return false;
        }
        userAgent = userAgent.toLowerCase();
        if (userAgent.contains("micromessenger")) {
            return true;
        }

        return false;
    }
}
