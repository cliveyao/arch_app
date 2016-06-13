/**
 * Copyright (c) 2012
 */
package lab.s2jh.core.web.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import lab.s2jh.core.context.SpringContextHolder;
import lab.s2jh.support.service.DynamicConfigService;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 * Through a filter based on object initialization request and record the current application context path cache
 */
public class WebAppContextInitFilter implements Filter {

    private final Logger logger = LoggerFactory.getLogger(WebAppContextInitFilter.class);

    private static String WEB_CONTEXT_FULL_URL = null;

    private static String WEB_CONTEXT_REAL_PATH = null;

    @Override
    public void init(FilterConfig arg0) throws ServletException {
        logger.debug("Invoking WebAppContextInitFilter init method...");
    }

    @Override
    public void destroy() {
        logger.debug("Invoking WebAppContextInitFilter destroy method...");
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse rep, FilterChain chain) throws IOException, ServletException {
        if (WEB_CONTEXT_FULL_URL == null) {
            HttpServletRequest request = (HttpServletRequest) req;
            DynamicConfigService dynamicConfigService = SpringContextHolder.getBean(DynamicConfigService.class);
            String contextPath = dynamicConfigService.getString("context.full.path");
            if (StringUtils.isBlank(contextPath)) {
                StringBuffer sb = new StringBuffer();
                sb.append(request.getScheme()).append("://").append(request.getServerName());
                sb.append(request.getServerPort() == 80 ? "" : ":" + request.getServerPort());
                sb.append(request.getContextPath());
                contextPath = sb.toString();
            }
         // Full context path of the current application , generally used for e-mail, text messaging and other needs of the assembly with the full access path
            WEB_CONTEXT_FULL_URL = contextPath;
         // Set the current directory to the root WEB_ROOT configuration property in order to get a simple Service operating environment to get the application root directory under WEB-INF Related Resources
            WEB_CONTEXT_REAL_PATH = request.getServletContext().getRealPath("/");
            logger.info("Init setup WebApp Context Info: ", WEB_CONTEXT_FULL_URL);
            logger.info(" - WEB_CONTEXT_FULL_URL: {}", WEB_CONTEXT_FULL_URL);
            logger.info(" - WEB_CONTEXT_REAL_PATH: {}", WEB_CONTEXT_REAL_PATH);
        }
        chain.doFilter(req, rep);
    }

    public static String getInitedWebContextFullUrl() {
        if (WEB_CONTEXT_FULL_URL == null) {
            if (DynamicConfigService.isDemoMode()) {
                return "http://runing.at.demo.mode";
            } else {
                Assert.notNull(WEB_CONTEXT_FULL_URL, "WEB_CONTEXT_FULL_URL must NOT null");
            }
        }
        return WEB_CONTEXT_FULL_URL;
    }

    public static String getInitedWebContextRealPath() {
        return WEB_CONTEXT_REAL_PATH;
    }

    public static void reset() {
        WEB_CONTEXT_FULL_URL = null;
        WEB_CONTEXT_REAL_PATH = null;
    }
}
