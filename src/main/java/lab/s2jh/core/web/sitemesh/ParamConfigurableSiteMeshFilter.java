package lab.s2jh.core.web.sitemesh;

import org.sitemesh.DecoratorSelector;
import org.sitemesh.builder.SiteMeshFilterBuilder;
import org.sitemesh.config.ConfigurableSiteMeshFilter;
import org.sitemesh.webapp.WebAppContext;

/**
 * Extended achieve implantation dynamic positioning decorator decorator based request parameter value selector
 */
public class ParamConfigurableSiteMeshFilter extends ConfigurableSiteMeshFilter {

    protected void applyCustomConfiguration(SiteMeshFilterBuilder builder) {
    	// Get the original default configuration decorated selector
        DecoratorSelector<WebAppContext> defaultDecoratorSelector = builder.getDecoratorSelector();
     // Assign custom decorative selector , the rule does not match the custom calling the default selector Get
        builder.setCustomDecoratorSelector(new ParamDecoratorSelector(defaultDecoratorSelector));
    }
}
