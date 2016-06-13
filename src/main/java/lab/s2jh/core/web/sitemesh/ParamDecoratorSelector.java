package lab.s2jh.core.web.sitemesh;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.sitemesh.DecoratorSelector;
import org.sitemesh.content.Content;
import org.sitemesh.webapp.WebAppContext;

/**
 * Dynamic positioning decorator choice based on header parameters and parameter values ​​decorator 's request
 * If decorator parameter has a value, it returns "/ WEB-INF / views / layouts /" + decorator + ".jsp" as the target template page decoration
 */
public class ParamDecoratorSelector implements DecoratorSelector<WebAppContext> {

    private DecoratorSelector<WebAppContext> defaultDecoratorSelector;

    public ParamDecoratorSelector(DecoratorSelector<WebAppContext> defaultDecoratorSelector) {
        this.defaultDecoratorSelector = defaultDecoratorSelector;
    }

    public String[] selectDecoratorPaths(Content content, WebAppContext context) throws IOException {
        // build decorator based on the request
        HttpServletRequest request = context.getRequest();
        String decorator = null;
     // First value from header header
        decorator = request.getHeader("decorator");
        if (StringUtils.isBlank(decorator)) {
        	// Not to be taken from the parameter values
            decorator = request.getParameter("decorator");
        }
        if (StringUtils.isNotBlank(decorator)) {
        	// Returns the corresponding parameter values ​​in accordance with the following path decorative template jsp page
            return new String[] { "/WEB-INF/views/layouts/" + decorator + ".jsp" };
        }

        // Otherwise, fallback to the standard configuration
        return defaultDecoratorSelector.selectDecoratorPaths(content, context);
    }
}