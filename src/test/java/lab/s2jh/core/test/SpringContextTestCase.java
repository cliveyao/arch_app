package lab.s2jh.core.test;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

/**
 *Spring's dependency injection JUnit4 support integration testing base class , compared to Spring primordia class name shorter.
 *
 * Subclasses need to define the location applicationContext file , such as:
 * @ContextConfiguration(locations = { "/applicationContext-test.xml" })
 * 
 * @author calvin
 */
@ActiveProfiles("test")
public abstract class SpringContextTestCase extends AbstractJUnit4SpringContextTests {
}
