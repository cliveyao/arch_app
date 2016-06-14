package s2jh.biz.shop.support.data;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import lab.s2jh.core.data.DatabaseDataInitializeProcessor;
import lab.s2jh.core.util.DateUtils;
import lab.s2jh.core.util.MockEntityUtils;
import lab.s2jh.module.auth.entity.User;
import lab.s2jh.module.auth.service.RoleService;
import lab.s2jh.module.auth.service.UserService;
import lab.s2jh.module.schedule.service.JobBeanCfgService;
import lab.s2jh.module.sys.entity.ConfigProperty;
import lab.s2jh.module.sys.service.ConfigPropertyService;
import lab.s2jh.module.sys.service.DataDictService;
import lab.s2jh.support.service.DynamicConfigService;

import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import s2jh.biz.shop.cons.BizConstant;
import s2jh.biz.shop.entity.Order;
import s2jh.biz.shop.entity.SiteUser;
import s2jh.biz.shop.service.OrderService;
import s2jh.biz.shop.service.SiteUserService;

import com.google.common.collect.Lists;

/**
 * Business data processor initialization
 */
@Component
public class BizDatabaseDataInitializeProcessor extends DatabaseDataInitializeProcessor {

    private final static Logger logger = LoggerFactory.getLogger(BizDatabaseDataInitializeProcessor.class);

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private ConfigPropertyService configPropertyService;

    @Autowired
    private DataDictService dataDictService;

    @Autowired
    private JobBeanCfgService jobBeanCfgService;

    @Autowired
    private SiteUserService siteUserService;

    @Autowired
    private OrderService orderService;

    @Override
    public void initializeInternal() {

        if (configPropertyService.findByPropKey(BizConstant.CFG_HTML_FAQ) == null) {
            ConfigProperty entity = new ConfigProperty();
            entity.setPropKey(BizConstant.CFG_HTML_FAQ);
            entity.setPropName("FAQ Copywriter");
            entity.setHtmlValue(getStringFromTextFile("faq.txt"));
            configPropertyService.save(entity);
        }

     // Create some demo mode simulated data
        if (DynamicConfigService.isDemoMode()) {

            logger.debug("Prepare data for DEMO mode...");


         // Get some random image data set
            List<String> randomImages = Lists.newArrayList();
            URL url = this.getClass().getResource("images");
            String fileName = url.getFile();
            Collection<File> files = FileUtils.listFiles(new File(fileName), null, false);
            for (File file : files) {
                randomImages.add("/files/mock/" + file.getName());
            }


         // If the table is empty then initialize the analog data ,
            if (isEmptyTable(SiteUser.class)) {
                //随机注册用户数量
                int cnt = MockEntityUtils.randomInt(10, 20);
                for (int i = 0; i < cnt; i++) {
                	// Random User Registration Date: Several days before the current system date
                    DateUtils.setCurrentDate(MockEntityUtils.randomDate(90, -7));


                 // Construct a random attribute values ​​populated user objects. After the general properties of random generation , the need for certain special service attribute set .
                    User user = MockEntityUtils.buildMockObject(User.class);
                    //基于当前循环流水号作为模拟数据账号
                    String seq = String.format("%03d", i);
                    user.setAuthUid("test" + seq);
                    user.setTrueName("Test Account" + seq);

                 // Set the value of the properties valid formats for email , otherwise it can not be defined by the entity @Email annotation verification
                    user.setEmail(user.getAuthUid() + "@s2jh4net.com");
                 // Call the service interface simulation data storage
                    userService.save(user, "123456");

                    SiteUser siteUser = MockEntityUtils.buildMockObject(SiteUser.class);
                    siteUser.setUser(user);
                 // Random Register Avatar Avatar
                    siteUser.setHeadPhoto(MockEntityUtils.randomCandidates(randomImages));
                    siteUserService.save(siteUser);


                 // Commit the current transaction data to simulate the actual situation step by step to create a business data
                    commitAndResumeTransaction();


                 // Stochastic Simulation single user
                    int orderCount = MockEntityUtils.randomInt(0, 5);
                    for (int j = 0; j < orderCount; j++) {
                    	// New transaction re- load the object query
                        siteUser = siteUserService.findOne(siteUser.getId());


                     // Constructor simulate order object
                        Order order = new Order();
                     // Analog Order No.
                        order.setOrderNo("O" + siteUser.getId() + j);
                        order.setSiteUser(siteUser);

                     // Simulate a single user at a random time after registration
                        DateUtils.setCurrentDate(new DateTime(siteUser.getUser().getUserExt().getSignupTime()).plusHours(
                                MockEntityUtils.randomInt(1, 240)).toDate());
                        orderService.submitOrder(order);

                     // Commit the current transaction data to simulate the actual situation step by step to create a business data
                        commitAndResumeTransaction();

                     // Random part of the order to pay
                        if (MockEntityUtils.randomBoolean()) {
                        	// New transaction re- load the object query
                            order = orderService.findOne(order.getId());

                         // Set the time of payment of the current single random order of time after 1-8 hours of time
                            Date randomTime = new DateTime(order.getSubmitTime()).plusHours(MockEntityUtils.randomInt(1, 8)).toDate();
                            DateUtils.setCurrentDate(randomTime);
                            orderService.payOrder(order);

                         // Commit the current transaction data to simulate the actual situation step by step to create a business data
                            commitAndResumeTransaction();
                        }
                    }
                }
            }
         // Commit the current transaction data to simulate the actual situation step by step to create a business data
            commitAndResumeTransaction();
        }
    }
}
