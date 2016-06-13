/**
 * Copyright (c) 2012
 */
package lab.s2jh.core.web.filter;

import java.io.IOException;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import lab.s2jh.core.web.util.ServletUtils;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Printout HTTP request information , generally used for development and debugging
 * Production logging level is set to be higher than INFO shield debugging output
 */
public class HttpRequestLogFilter implements Filter {

    private final Logger logger = LoggerFactory.getLogger(HttpRequestLogFilter.class);

    @Override
    public void init(FilterConfig arg0) throws ServletException {
        logger.debug("Invoking HttpRequestLogFilter init method...");
    }

    @Override
    public void destroy() {
        logger.debug("Invoking HttpRequestLogFilter destroy method...");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse reponse, FilterChain chain) throws IOException, ServletException {
        if (logger.isInfoEnabled()) {
            HttpServletRequest req = (HttpServletRequest) request;

            String uri = req.getRequestURI();
         // Static resource skip
            if (uri == null || uri.endsWith(".js") || uri.endsWith(".css") || uri.endsWith(".gif") || uri.endsWith(".png") || uri.endsWith(".jpg")
                    || uri.endsWith(".woff") || uri.endsWith(".ico")) {
                chain.doFilter(request, reponse);
                return;
            }

         // Extract verbose parameter identifies whether or not to enable verbose output
            boolean verbose = logger.isTraceEnabled() || BooleanUtils.toBoolean(req.getParameter("verbose"));

            Map<String, String> dataMap = ServletUtils.buildRequestInfoDataMap(req, verbose);
            StringBuilder sb = new StringBuilder("HTTP Request Info:");
            for (Map.Entry<String, String> me : dataMap.entrySet()) {
                sb.append(StringUtils.rightPad("\n" + me.getKey(), 50) + " : " + me.getValue());
            }
            logger.info(sb.toString());
        }
        chain.doFilter(request, reponse);
    }
}
