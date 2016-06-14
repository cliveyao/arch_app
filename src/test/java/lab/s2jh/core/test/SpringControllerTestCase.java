package lab.s2jh.core.test;

import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Spring MVC Test of the Controller base class based on test .
 * @see Http://docs.spring.io/spring/docs/current/spring-framework-reference/html/testing.html#spring-mvc-test-framework
 *
 * Subclasses need to define the location applicationContext file , such as:
 * @ContextConfiguration(locations = { "classpath*:/context/spring-bpm.xml" })
 * 
 */
@ContextConfiguration(locations = { "classpath*:/spring-mvc.xml" })
@WebAppConfiguration
public abstract class SpringControllerTestCase extends SpringTransactionalTestCase {

    @Autowired
    private WebApplicationContext wac;

    protected MockMvc mockMvc;

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }
}
