package lab.s2jh.module.crawl.service;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import lab.s2jh.core.crawl.CrawlLoginFilter;
import lab.s2jh.core.crawl.CrawlParseFilter;
import lab.s2jh.core.dao.mongo.MongoDao;
import lab.s2jh.core.util.ChineseUtils;
import lab.s2jh.core.util.Exceptions;
import lab.s2jh.core.util.ThreadUtils;
import lab.s2jh.core.web.view.OperationResult;
import lab.s2jh.module.crawl.vo.CrawlConfig;
import lab.s2jh.module.crawl.vo.Outlink;
import lab.s2jh.module.crawl.vo.WebPage;
import lab.s2jh.support.service.DynamicConfigService;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.shiro.util.ClassUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * Core  crawl resolution service interface. Reference Apache Nutch similar design to the realization of ideas , specifically refer to the official Nutch official information .
 * 可参考 @see https://github.com/xautlx/nutch-ajax/blob/master/document/Apache_Nutch_Solr_Solution_with_AJAX_support.md
 */
@Service
public class CrawlService {

    private static final Logger logger = LoggerFactory.getLogger("crawl.service");

    /** In order to simplify the query to avoid null value processing , date-related field initialization values ​​start date */
    private Date INIT_DATE = new DateTime(0).toDate();

    /** After fetch, parse and so failed , maximum number of attempts , if the counter exceeds this value will not continue trying to crawl or parsing */
    private static final int RETRY_TIMES = 5;

    /** The maximum number of requests turned 302*/
    private static final int MAX_REDIRECT_TIMES = 5;

    @Autowired
    private MongoDao mongoDao;

    /** Crawl main thread begins execution time*/
    private Date crawlStartTime;

    /** Total number of pages crawled resolution counter */
    private AtomicInteger pages = new AtomicInteger(0);

    /** Crawl parsing thread execution flag */
    private boolean running;

    /** Crawl parsing thread pool actuator */
    private ThreadPoolTaskExecutor executor;

    /** The default User-Agent request header */
    public final static String User_Agent = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:40.0) Gecko/20100101 Firefox/40.0";

    public final static String Crawl_Fetch_Data_Collection = "crawl_fetch_data";

    public final static String Crawl_Parse_Data_Collection = "crawl_parse_data";

    public final static String Crawl_Failure_Data_Collection = "crawl_failure_data";

    public final static String Default_Charset_UTF8 = "utf-8";

    /**HTTP Client singleton request object */
    private static CloseableHttpClient httpClientInstance;
    /** HTTP Client singleton connection pool manager object */
    private static PoolingHttpClientConnectionManager poolConnManager;

    public static synchronized CloseableHttpClient buildHttpClient() {
        if (httpClientInstance == null) {
            RequestConfig config = RequestConfig.custom().setConnectionRequestTimeout(100000).setSocketTimeout(100000).setConnectTimeout(100000)
                    .setCookieSpec(CookieSpecs.DEFAULT).setRedirectsEnabled(false).build();

            poolConnManager = new PoolingHttpClientConnectionManager();

           // Total maximum number of concurrent connections
            poolConnManager.setMaxTotal (300);
           // Maximum number of concurrent connections a single site
            poolConnManager.setDefaultMaxPerRoute(100);

            httpClientInstance = HttpClients.custom().setConnectionManager(poolConnManager).setDefaultRequestConfig(config).build();
        }
        if (logger.isDebugEnabled()) {
            if (poolConnManager != null && poolConnManager.getTotalStats() != null) {
                logger.debug("HttpClient pool stats: " + poolConnManager.getTotalStats().toString());
            }
        }
        return httpClientInstance;
    }

    /** Page parsing filter set */
    private List<CrawlParseFilter> crawlParseFilters;

    public synchronized List<CrawlParseFilter> buildCrawlParseFilters() {
        if (crawlParseFilters == null) {
            crawlParseFilters = Lists.newArrayList();


         // Spring-based inheritance rules to get all analytical filter set list
            Set<BeanDefinition> beanDefinitions = Sets.newHashSet();
            ClassPathScanningCandidateComponentProvider scan = new ClassPathScanningCandidateComponentProvider(false);
         // Implement CrawlParseFilter Interface
            scan.addIncludeFilter(new AssignableTypeFilter(CrawlParseFilter.class));
            beanDefinitions.addAll(scan.findCandidateComponents("lab.s2jh.**"));
            beanDefinitions.addAll(scan.findCandidateComponents("s2jh.biz.**"));

            for (BeanDefinition beanDefinition : beanDefinitions) {
                Class<?> beanClass = ClassUtils.forName(beanDefinition.getBeanClassName());
                CrawlParseFilter crawlParseFilter = (CrawlParseFilter) ClassUtils.newInstance(beanClass);
                crawlParseFilter.setCrawlService(this);
                crawlParseFilters.add(crawlParseFilter);
            }
        }
        return crawlParseFilters;
    }

    /** Log processing filter set*/
    private List<CrawlLoginFilter> crawlLoginFilters;

    private synchronized List<CrawlLoginFilter> buildCrawlLoginFilters() {
        if (crawlLoginFilters == null) {
            crawlLoginFilters = Lists.newArrayList();


         // Spring-based inheritance rules to get all analytical filter set list
            Set<BeanDefinition> beanDefinitions = Sets.newHashSet();
            ClassPathScanningCandidateComponentProvider scan = new ClassPathScanningCandidateComponentProvider(false);
         // Implement CrawlLoginFilter Interface
            scan.addIncludeFilter(new AssignableTypeFilter(CrawlLoginFilter.class));
            beanDefinitions.addAll(scan.findCandidateComponents("lab.s2jh.**"));
            beanDefinitions.addAll(scan.findCandidateComponents("s2jh.biz.**"));

            for (BeanDefinition beanDefinition : beanDefinitions) {
                Class<?> beanClass = ClassUtils.forName(beanDefinition.getBeanClassName());
                CrawlLoginFilter crawlLoginFilter = (CrawlLoginFilter) ClassUtils.newInstance(beanClass);
                crawlLoginFilters.add(crawlLoginFilter);
            }
        }
        return crawlLoginFilters;
    }

    /**
     * Get crawling data collection object instance
     * @return
     */
    private DBCollection buildFetchObjectCollection() {
        return mongoDao.getDB().getCollection(Crawl_Fetch_Data_Collection);
    }

    /**
     * Get analysis data collection object instance
     * @return
     */
    private DBCollection buildParseObjectCollection() {
        return mongoDao.getDB().getCollection(Crawl_Parse_Data_Collection);
    }

    /**
     * Get notification data collection object instance
     * @return
     */
    private DBCollection buildFailureObjectCollection() {
        return mongoDao.getDB().getCollection(Crawl_Failure_Data_Collection);
    }

    /**
     * Construct objects inject injection
     * @param dbColl
     * @param url
     * @param crawlConfig
     * @return
     */
    private DBObject buildInjectDBObject(DBCollection dbColl, String url, CrawlConfig crawlConfig) {

    	// Cleaning meaningless character behind #
        url = StringUtils.substringBefore(url, "#").trim();

     // Constructor for data objects $ set operations
        DBObject item = new BasicDBObject(CrawlParseFilter.URL, url);
        if (dbColl.findOne(new BasicDBObject(CrawlParseFilter.URL, url), new BasicDBObject(CrawlParseFilter.URL, 1)) == null) {
            item.put("generateTime", INIT_DATE);
            item.put("fetchTouchTime", INIT_DATE);
            item.put("parseTime", null);
            item.put("fetchTime", null);
            item.put("httpStatus", -1);
            item.put("fetchFailureTimes", 0);
        }
        item.put("injectTime", new Date());

     // Fetch each time you start a thread as a batch , to avoid interference
        item.put("batchId", crawlConfig.getBatchId());
        return item;
    }

    private void injectSeeds(String url, CrawlConfig crawlConfig) {
        if (StringUtils.isBlank(url)) {
            return;
        }
        DBCollection dbColl = buildFetchObjectCollection();
        DBObject item = buildInjectDBObject(dbColl, url, crawlConfig);
        item.put("injectSeed", true);
        item.put("sourceUrl", url);

     // Force Recrawl seed URL parsing
        item.put("fetchTime", null);
        item.put("parseTime", null);
        logger.info("Inject Seed: {} ", url);
        dbColl.update(new BasicDBObject(CrawlParseFilter.URL, url), new BasicDBObject("$set", item), true, false);
    }

    public void injectOutlink(Outlink outlink, CrawlConfig crawlConfig, String sourceUrl) {
        String url = outlink.getUrl();
        if (StringUtils.isBlank(url)) {
            return;
        }
        DBCollection dbColl = buildFetchObjectCollection();
        DBObject item = buildInjectDBObject(dbColl, url, crawlConfig);
        item.put("injectSeed", false);
        item.put("sourceUrl", sourceUrl);

        // Specified outlink specific parser
        if (outlink.getCrawlParseFilters() != null) {
            item.put("crawlParseFilters", outlink.getCrawlParseFilters());
        }

     // Initialize outlink packet identification
        if (outlink.getBizSiteName() != null) {
            item.put("bizSiteName", outlink.getBizSiteName());
        }
     // Initialization packet ID identifier outlink
        if (outlink.getBizId() != null) {
            item.put("bizId", outlink.getBizId());
        }
        if (outlink.getTitle() != null) {
            item.put("title", outlink.getTitle());
        }

     // This identification is mandatory outlink analytical type, such as dynamic page URL
        if (outlink.isForceRefetch()) {
        	// Force Recrawl resolve
            item.put("fetchTime", null);
            item.put("parseTime", null);
        }
        logger.info("Inject Outlink: {} ", outlink);
        dbColl.update(new BasicDBObject(CrawlParseFilter.URL, url), new BasicDBObject("$set", item), true, false);


     // Initialize outlink analysis data
        if (outlink.getParsedDBObject() != null) {
            DBObject update = new BasicDBObject();
            update.put(CrawlParseFilter.PARSE_INLINK_URL, sourceUrl);
            update.putAll(outlink.getParsedDBObject());
            saveParseDBObject(outlink.getUrl(), outlink.getBizSiteName(), outlink.getBizId(), update);
        }
    }

    public void saveParseDBObject(String url, String siteName, String bizId, DBObject update) {
        Assert.notNull(siteName, "Analytical data retention" + CrawlParseFilter.SITE_NAME + "Property Value can not be null");
        Assert.notNull(bizId, "Analytical data retention " + CrawlParseFilter.ID + " Property Value can not be null");

        update.put("解析时间", new Date());
        update.put(CrawlParseFilter.SITE_NAME, siteName);
        update.put(CrawlParseFilter.ID, bizId);

        BasicDBObject query = new BasicDBObject("$and", new BasicDBObject[] { new BasicDBObject(CrawlParseFilter.SITE_NAME, siteName),
                new BasicDBObject(CrawlParseFilter.ID, bizId) });

        synchronized (this) {
            DBObject parsedDBObject = buildParseObjectCollection().findOne(query);


         // Parse the data merge multiple pages into one service data records aggregated business data source URL address list
            if (parsedDBObject == null) {
                update.put(CrawlParseFilter.PARSE_FROM_URLS, url);
            } else {
                Object urls = parsedDBObject.get(CrawlParseFilter.PARSE_FROM_URLS);
                Set<Object> parsedFromUrls = Sets.newLinkedHashSet();
                if (urls instanceof BasicDBList) {
                    parsedFromUrls.addAll(Sets.newHashSet(((BasicDBList) urls).iterator()));
                } else {
                    String preURLs = (String) parsedDBObject.get(CrawlParseFilter.PARSE_FROM_URLS);
                    parsedFromUrls.addAll(Sets.newHashSet(StringUtils.split(preURLs, "\n,")));
                }
                parsedFromUrls.add(url);
                update.put(CrawlParseFilter.PARSE_FROM_URLS, StringUtils.join(parsedFromUrls, ","));
            }

            logger.debug("Save Parsed Data for URL {} is: {}", url, update);
            buildParseObjectCollection().update(query, new BasicDBObject("$set", update), true, false);
        }
    }

    /**
     * Page Fetch handle the exception , then re- injected into the next attempt to re- fetch and parse, 
     * until more than the number of retries is terminated
     * @param webPage
     */
    private void injectFetchFailureRetry(String url, int statusCode, String result, String fetchFailureException) {
        DBCollection dbColl = buildFetchObjectCollection();
        DBObject item = dbColl.findOne(new BasicDBObject(CrawlParseFilter.URL, url),
                new BasicDBObject(CrawlParseFilter.URL, 1).append("fetchFailureTimes", 1));

        Integer fetchFailureTimes = (Integer) item.get("fetchFailureTimes");
        if (fetchFailureTimes == null) {
            fetchFailureTimes = 0;
        }

        fetchFailureTimes = fetchFailureTimes + 1;
        DBObject update = new BasicDBObject();
        update.put("fetchFailureTimes", fetchFailureTimes);
        update.put("fetchFailureException", fetchFailureException);
        update.put("fetchTouchTime", new Date());
        update.put("httpStatus", statusCode);
        update.put("httpResponse", result);

        if (fetchFailureTimes > RETRY_TIMES) {
            logger.debug("Skipped fetch retry due to limit: {} , fetchFailureTimes: {}", url, fetchFailureTimes);
        } else {
            update.put("injectTime", new Date());
            update.put("generateTime", INIT_DATE);
            update.put("fetchTouchTime", INIT_DATE);
            update.put("parseTime", null);
            update.put("fetchTime", null);
            logger.info("Inject fetch retry: {} , fetchFailureTimes: {}", url, fetchFailureTimes);
        }
        buildFetchObjectCollection().update(new BasicDBObject(CrawlParseFilter.URL, url), new BasicDBObject("$set", update));
    }

    /**
     * Page calls Parse filter handle the exception , then re- injected into the next attempt to 
     * re- fetch and parse, until more than the number of retries is terminated
     * @param webPage
     */
    public void injectParseFailureRetry(WebPage webPage, String parseFailureException) {
        DBCollection dbColl = buildFetchObjectCollection();
        String url = webPage.getUrl();
        DBObject item = dbColl.findOne(new BasicDBObject(CrawlParseFilter.URL, url),
                new BasicDBObject(CrawlParseFilter.URL, 1).append("parseFailureTimes", 1));

        Integer parseFailureTimes = (Integer) item.get("parseFailureTimes");
        if (parseFailureTimes == null) {
            parseFailureTimes = 0;
        }

        parseFailureTimes = parseFailureTimes + 1;
        DBObject update = new BasicDBObject();
        update.put("parseFailureTimes", parseFailureTimes);
        update.put("parseFailureException", parseFailureException);

        if (parseFailureTimes > RETRY_TIMES) {
            logger.debug("Skipped parse retry due to limit: {} , parseFailureTimes: {}", url, parseFailureTimes);
        } else {
            update.put("injectTime", new Date());
            update.put("generateTime", INIT_DATE);
            update.put("fetchTouchTime", INIT_DATE);
            update.put("parseTime", null);
            update.put("fetchTime", null);
            logger.info("Inject parse retry: {} , parseFailureTimes: {}", url, parseFailureTimes);
        }

        buildFetchObjectCollection().update(new BasicDBObject(CrawlParseFilter.URL, url), new BasicDBObject("$set", update));
    }

    /**
     * Update generated URL to be crawled set list
     * @param crawlConfig
     * @param seedMode
     * @return 
     */
    public synchronized int generator(CrawlConfig crawlConfig, boolean seedMode) {

    	// In order to avoid excessive consumption of presentation environments bandwidth and storage resources and limit the total number of processing
        if (DynamicConfigService.isDemoMode() && pages.get() > 100) {
            logger.info("Skipped generator as running DEMO mode.");
            return 0;
        }

        DBCollection dbColl = buildFetchObjectCollection();

        //http://www.chaolv.com/about/contact.html  http://shkangdexin.b2b.hc360.com/    http://4001671615ylj.b2b.hc360.com/  http://huxinsheng1969.b2b.hc360.com/shop/show.html
        BasicDBObject query = null;
        if (seedMode) {

        	// If a seed URL starts executing, always take the current batch execution data generation seed URL to be crawled URL list, and exclude more than limiting the number of crawl
            query = new BasicDBObject("generateTime", new BasicDBObject("$lt", crawlConfig.getStartTime()));
            query.append("batchId", crawlConfig.getBatchId());
            query.append("fetchFailureTimes", new BasicDBObject("$lte", RETRY_TIMES));
        } else {

        	// If no seed URL starts execution, take all URL listing status as a non- 200 list of URL to be crawled , crawled over and exclude limiting the number of
            query = new BasicDBObject("generateTime", new BasicDBObject("$lt", crawlConfig.getStartTime()));
            query.append("$or", new BasicDBObject[] { new BasicDBObject("httpStatus", new BasicDBObject("$gt", 200)),
                    new BasicDBObject("httpStatus", new BasicDBObject("$lt", 200)) });
            query.append("fetchFailureTimes", new BasicDBObject("$lte", RETRY_TIMES));
        }

        DBCursor cur = dbColl.find(query, new BasicDBObject(CrawlParseFilter.URL, 1).append("httpStatus", 1).append("fetchFailureTimes", 1))
                .sort(new BasicDBObject("injectTime", 1)).limit(100);
        //logger.debug("MongoDB query explain: {}", cur.explain());

        int count = 0;
        while (running && cur.hasNext()) {
            DBObject item = cur.next();
            String url = (String) item.get(CrawlParseFilter.URL);
            DBObject update = new BasicDBObject();
            update.put("batchId", crawlConfig.getBatchId());
            update.put("generateTime", new Date());
            logger.info("Generator: {} , HttpStatus: {} ,fetchFailureTimes: {}", url, item.get("httpStatus"), item.get("fetchFailureTimes"));

            buildFetchObjectCollection().update(new BasicDBObject(CrawlParseFilter.URL, url), new BasicDBObject("$set", update));
            count++;
        }
        return count;
    }

    /**
     * Crawl URL list to be extracted , multi-threaded manner URL crawl and analysis processing
     * @param crawlConfig
     */
    public void fetcher(CrawlConfig crawlConfig) {
        DBCollection dbColl = buildFetchObjectCollection();

        BasicDBObject query = new BasicDBObject();
     // Query generation time is greater than the start time for this reptile
        query.append("generateTime", new BasicDBObject("$gt", crawlConfig.getStartTime()));

     // Query the current batch
        query.append("batchId", crawlConfig.getBatchId());

     // Fetching less than this crawl start time ( this has climbed to exclude records )
        query.append("fetchTouchTime", new BasicDBObject("$lt", crawlConfig.getStartTime()));

        DBCursor cur = dbColl.find(query).sort(new BasicDBObject("generateTime", 1)).limit(100);
        //logger.debug("MongoDB query explain: {}", cur.explain());

        logger.debug("Thread pool executor stat: [{}/{}].", executor.getActiveCount(), executor.getCorePoolSize());
        while (running && cur.hasNext()) {
            DBObject item = cur.next();
            int activeThreads = executor.getActiveCount();

         // If the current number of active threads saturated, wait idle threads
            while (activeThreads >= executor.getCorePoolSize()) {
                ThreadUtils.sleepOneSecond();
                logger.info("Thread pool executor full [active: {}], waiting...", activeThreads);
                activeThreads = executor.getActiveCount();
            }

         // If there is idle threads in the thread pool Submit
            executor.execute(new FetcherThread(this, item, crawlConfig));
        }
    }

    /**
     * Reptile service startup entry
     * @param crawlConfig
     * @param urls
     */
    public FutureTask<Integer> startup(final CrawlConfig crawlConfig, final String... urls) {
        running = true;


     // Fetch each time you start a thread as a batch , to avoid interference
        if (crawlConfig.getBatchId() == null) {
            crawlConfig.setBatchId(new Date().getTime());
        }

        DBCollection coll = buildFetchObjectCollection();


     // To improve query performance , relevant attributes crawling data collection to add indexes, constraints

             // URL unique constraint
        coll.createIndex(new BasicDBObject(CrawlParseFilter.URL, 1), new BasicDBObject("unique", true).append("background", true));


     // Generator Query Index
        coll.createIndex(new BasicDBObject("generateTime", 1).append("batchId", 1).append("fetchFailureTimes", 1).append("injectTime", 1),
                new BasicDBObject("background", true));
        coll.createIndex(new BasicDBObject("generateTime", 1).append("httpStatus", 1).append("fetchFailureTimes", 1).append("injectTime", 1),
                new BasicDBObject("background", true));


     // Fetcher Query Index
        coll.createIndex(new BasicDBObject("generateTime", 1).append("batchId", 1).append("fetchTouchTime", 1), new BasicDBObject("background", true));


     // Add an index to resolve data collection related attributes , constraints
        buildParseObjectCollection().createIndex(new BasicDBObject(CrawlParseFilter.SITE_NAME, 1).append(CrawlParseFilter.ID, 1),
                new BasicDBObject("unique", true).append("background", true));


     // Add an index to resolve data collection related attributes , constraints
        buildFailureObjectCollection().createIndex(new BasicDBObject(CrawlParseFilter.URL, 1),
                new BasicDBObject("unique", true).append("background", true));


     // Initialize the thread pool to set parameters
        executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(crawlConfig.getThreadNum());
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("Crawl-");
        executor.initialize();

        logger.info("Prepare to startup crawl thread with config: {}", crawlConfig);
        crawlStartTime = new Date();

        FutureTask<Integer> future = new FutureTask<Integer>(new Runnable() {

            /** Asynchronous execution of the main thread of continuous sleep number of seconds , if it exceeds a certain threshold to exit the main thread */
            int sleepSeconds = 0;

            /** Last records generated URL list to be crawling time , to maintain a certain distance before the two generator control operation, control the rhythm of the main thread loop */
            Date lastGenerateTime;

            @Override
            public void run() {
                //convertLongToDate("fetchTouchTime");

                //Depending on whether the provision of seed collection url parameter set identifier
                boolean seedMode = true;
                if (urls != null && urls.length > 0) {
                    if (logger.isInfoEnabled()) {
                        for (String url : urls) {
                            logger.info(" - Seed URL: {}", url);
                        }
                    }
                    for (String url : urls) {
                        injectSeeds(url, crawlConfig);
                    }
                } else {
                    seedMode = false;
                    logger.info(" - NO Seed URL");
                }

                do {
                    Date now = new Date();

                    // To maintain a certain distance before the two generator control operation, control the rhythm of the main thread loop
                    if (lastGenerateTime != null && (now.getTime() - lastGenerateTime.getTime() < 2 * 1000)) {
                        ThreadUtils.sleepOneSecond();
                        continue;
                    }
                    lastGenerateTime = now;


                 // Call generator generates an interface update URL set to be crawling , affect the number of records returned
                    int count = generator(crawlConfig, seedMode);
                    if (count == 0) {

                    	// If it returns the number of records to be crawling to zero , there may be other threads being executed has not added a new outlink, then wait a short sleep
                        ThreadUtils.sleepOneSecond();

                     // Continuous and cumulative sleep counter , if it reaches a certain threshold then the new URL has no need to be addressed , the main thread to terminate reptiles
                        sleepSeconds++;
                        logger.info("Crawl thread sleep {} seconds for more generate URL.", sleepSeconds);
                    } else {

                    	// If you generate a URL to be crawled data collection , continuous sleep resets the counter and call crawl Interface
                        sleepSeconds = 0;
                        fetcher(crawlConfig);
                    }
                } while (running && sleepSeconds < 30);


             // Reset counter
                pages = new AtomicInteger(0);

                logger.info("Crawl thread terminated at {} , start from {}", new Date(), crawlStartTime);
            }
        }, null);


     // Reptile asynchronous execution of the main thread
        new Thread(future).start();

        return future;
    }

    /**
     * Update flag run , forced termination notice the main thread and all other reptiles crawl analytic child threads
     */
    public void shutdown() {
        running = false;
    }

    /**
     *Crawl processing threads to achieve
     */
    private class FetcherThread implements Runnable {

        private CrawlService crawlService;

        private DBObject item;

        private CrawlConfig crawlConfig;

        public FetcherThread(CrawlService crawlService, DBObject item, CrawlConfig crawlConfig) {
            this.crawlService = crawlService;
            this.crawlConfig = crawlConfig;
            this.item = item;
        }

        /**
         *In response to the contents of string to do the conversion process is compatible with the 
         *default converted to utf-8, by comparing the information on the page charset reasonable 
         *character set conversion process
         * @param url
         * @param responseEntity
         * @return
         * @throws Exception
         */
        private String responseEntityToString(String url, HttpEntity responseEntity) throws Exception {
            byte[] responseBytes = EntityUtils.toByteArray(responseEntity);


         // Extract character set information from the response headers and content
            String charset = null;
            final ContentType contentType = ContentType.get(responseEntity);
            if (contentType != null) {
                Charset contentTypeCharset = contentType.getCharset();
                if (contentTypeCharset != null) {
                    charset = contentTypeCharset.name();
                }
            }
            String result = null;
            if (StringUtils.isBlank(charset)) {
                result = new String(responseBytes, Default_Charset_UTF8);
            } else {
                result = new String(responseBytes, charset);
            }

            //<meta content="text/html; charset=gb2312" http-equiv="Content-Type">
            if (StringUtils.isBlank(charset)) {
                charset = StringUtils.substringBetween(result, "charset=", "\"");
            }
            //<meta charset="UTF-8">
            if (StringUtils.isBlank(charset)) {
                charset = StringUtils.substringBetween(result, "charset=\"", "\"");
            }
            if (StringUtils.isNotBlank(charset)) {
                charset = charset.trim().toLowerCase();
                if (!charset.equals(Default_Charset_UTF8.toLowerCase())) {

                	// Response meta elements and some pages are not consistent , in order to deal with such circumstances is a title Chinese garbled judgment was confirmed to be garbled and then transcoded
                    String title = StringUtils.substringBetween(result, "<title>", "</title>");
                    if (StringUtils.isNotBlank(title) && ChineseUtils.isMessyCode(title)) {
                        logger.info("HTML Charset convert from {} to {} for URL: {}", Default_Charset_UTF8, charset, url);
                        result = new String(responseBytes, charset);
                    }
                }
            }
            return result;
        }

        @Override
        public void run() {
            DBCollection fetchObjectCollection = buildFetchObjectCollection();
            String url = (String) item.get(CrawlParseFilter.URL);
            DBObject urlQuery = new BasicDBObject(CrawlParseFilter.URL, url);
            DBObject fetchUpdate = new BasicDBObject("fetchTouchTime", new Date());

            int statusCode = -1;
            String result = null;
            String exception = null;
            String redirectUrl = null;
            Integer httpStatus = (Integer) item.get("httpStatus");
            Date fetchTime = (Date) item.get("fetchTime");

         // If not crawled , crawled , or failure , or is forced to re- crawl
            if (httpStatus == null || httpStatus != 200 || fetchTime == null || crawlConfig.isForceRefetch()) {
                logger.info("Fetching: {}", url);

                Date now = new Date();


             // Some sites limit the frequency of continuous access , according to a simple test to find a reasonable artificial crawling interval value , control is maintained between the two URL request sufficient interval
                if (crawlConfig.getFetchMinInterval() > 0) {
                    if (crawlConfig.getLastFetchTime() != null) {
                        int seconds = Long.valueOf((now.getTime() - crawlConfig.getLastFetchTime().getTime()) / 1000).intValue();
                        if (crawlConfig.getFetchMinInterval() > seconds) {
                            ThreadUtils.sleepSeconds(crawlConfig.getFetchMinInterval() - seconds);
                            logger.debug("Thread sleep {} seconds for submit FetcherThread according to crawlConfig.fetchMinInterval",
                                    crawlConfig.getFetchMinInterval());
                        }
                    }
                    crawlConfig.setLastFetchTime(now);
                }


             // First set of filter -based login , login pretreatment , HttpClient automatic cookie handling is maintained between each request
                List<CrawlLoginFilter> loginFilters = buildCrawlLoginFilters();
                Map<String, String> requestHeaders = Maps.newHashMap();
                requestHeaders.put("User-Agent", User_Agent);
                requestHeaders.put("Connection", "close");
                for (CrawlLoginFilter loginFilter : loginFilters) {
                    loginFilter.filter(url);
                }

                CloseableHttpResponse httpGetResponse = null;
                HttpEntity httpGetResponseEntity = null;
                try {
                	// First the url GET request processing
                    HttpGet httpGet = new HttpGet(url);
                    for (Map.Entry<String, String> header : requestHeaders.entrySet()) {
                        httpGet.addHeader(header.getKey(), header.getValue());
                    }
                    httpGetResponse = buildHttpClient().execute(httpGet);
                    statusCode = httpGetResponse.getStatusLine().getStatusCode();


                 // If it is 301 or 302 steering, the steering is extracted URL address
                    if (statusCode == HttpStatus.SC_MOVED_TEMPORARILY || statusCode == HttpStatus.SC_MOVED_PERMANENTLY) {
                        Header[] headers = httpGetResponse.getHeaders("Location");
                        if (headers.length > 0) {
                            redirectUrl = headers[0].getValue();
                        }
                    } else {
                        httpGetResponseEntity = httpGetResponse.getEntity();
                        result = responseEntityToString(url, httpGetResponseEntity);
                    }
                } catch (Exception e) {
                    statusCode = -1;
                    exception = Exceptions.getStackTraceAsString(e);
                } finally {
                    EntityUtils.consumeQuietly(httpGetResponseEntity);
                    IOUtils.closeQuietly(httpGetResponse);
                }

             // 301 / 302 Steering, secondary initiation request
                int redirectTimes = 0;
                while ((statusCode == HttpStatus.SC_MOVED_TEMPORARILY || statusCode == HttpStatus.SC_MOVED_PERMANENTLY)
                        && redirectTimes < MAX_REDIRECT_TIMES) {
                    logger.info("Try GET redirect URL: {}", redirectUrl);
                    redirectTimes++;
                    try {
                        HttpGet httpGet = new HttpGet(redirectUrl);
                        for (Map.Entry<String, String> header : requestHeaders.entrySet()) {
                            httpGet.addHeader(header.getKey(), header.getValue());
                        }
                        httpGetResponse = buildHttpClient().execute(httpGet);
                        statusCode = httpGetResponse.getStatusLine().getStatusCode();


                     // If it is turned 302 , is extracted steering URL address
                        if (statusCode == HttpStatus.SC_MOVED_TEMPORARILY) {
                            Header[] headers = httpGetResponse.getHeaders("Location");
                            if (headers.length > 0) {
                                redirectUrl = headers[0].getValue();
                            }
                        } else {
                            httpGetResponseEntity = httpGetResponse.getEntity();
                            result = responseEntityToString(url, httpGetResponseEntity);
                        }
                    } catch (Exception e) {
                        statusCode = -1;
                        exception = Exceptions.getStackTraceAsString(e);
                    } finally {
                        EntityUtils.consumeQuietly(httpGetResponseEntity);
                        IOUtils.closeQuietly(httpGetResponse);
                    }
                }


             // GET is not supported , try POST
                if (statusCode == HttpStatus.SC_METHOD_NOT_ALLOWED) {
                    logger.info("Try to use POST as GET not allowed for URL: {}", statusCode, url);
                    CloseableHttpResponse httpPostResponse = null;
                    HttpEntity httpPostResponseEntity = null;
                    try {
                        HttpPost httpPost = new HttpPost(url);
                        for (Map.Entry<String, String> header : requestHeaders.entrySet()) {
                            httpPost.addHeader(header.getKey(), header.getValue());
                        }
                     // POST request is generally in the form of JSON paging processing
                        StringEntity entity = new StringEntity("{}", Default_Charset_UTF8);
                        entity.setContentEncoding(Default_Charset_UTF8);
                        entity.setContentType("application/json");
                        httpPost.setEntity(entity);
                        httpPostResponse = buildHttpClient().execute(httpPost);
                        statusCode = httpPostResponse.getStatusLine().getStatusCode();

                        httpPostResponseEntity = httpPostResponse.getEntity();
                        result = EntityUtils.toString(httpPostResponseEntity, Default_Charset_UTF8);
                    } catch (Exception e) {
                        statusCode = -1;
                        exception = Exceptions.getStackTraceAsString(e);
                    } finally {
                        EntityUtils.consumeQuietly(httpPostResponseEntity);
                        IOUtils.closeQuietly(httpPostResponse);
                    }
                }

                fetchUpdate.put("httpStatus", statusCode);
                fetchUpdate.put("httpResponse", result);

                if (statusCode != HttpStatus.SC_OK) {
                    logger.warn("HTTP ERROR StatusCode is {} for URL: {}", statusCode, url);
                    logger.trace("HTTP response for URL {} is:\n{}", url, result);


                 // Page Fetch handle the exception , then re- injected into the next attempt to re- fetch and parse, after exceeding the threshold will no longer initiate this url http request
                    injectFetchFailureRetry(url, statusCode, result, exception);
                } else {
                    fetchUpdate.put("fetchFailureTimes", 0);
                    fetchUpdate.put("httpResponse", result);
                    fetchUpdate.put("fetchTime", new Date());
                    logger.info("HTTP Fetch is OK[{}] for URL: {}", statusCode, url);
                    fetchObjectCollection.update(urlQuery, new BasicDBObject("$set", fetchUpdate));
                }
            } else {
                logger.info("Skipped fetch [{}] as last fetched time: {}", url, fetchTime);
                statusCode = (Integer) item.get("httpStatus");
                result = (String) item.get("httpResponse");
                fetchObjectCollection.update(urlQuery, new BasicDBObject("$set", fetchUpdate));
            }


         // Analytical Processing
            Date parseTime = (Date) item.get("parseTime");

         // If not parsed or force to resolve
            if (parseTime == null || crawlConfig.isForceReparse()) {

            	// Only 200 state response analysis processing
                if (statusCode == HttpStatus.SC_OK) {
                    logger.info("Parsing for URL: {}", url);

                 // Get all valid analytical filter
                    List<CrawlParseFilter> parseFilters = buildCrawlParseFilters();


                 // Get the current URL parsing data if there is a specified filter set , if there is the specified filter set , otherwise follow all parser
                    BasicDBList dbCrawlParseFilters = (BasicDBList) item.get("crawlParseFilters");
                    if (dbCrawlParseFilters != null && dbCrawlParseFilters.size() > 0) {
                        parseFilters = Lists.newArrayList();
                        for (Object dbItem : dbCrawlParseFilters) {
                            CrawlParseFilter crawlParseFilter = (CrawlParseFilter) ClassUtils.newInstance(dbItem.toString());
                            crawlParseFilter.setCrawlService(crawlService);
                            crawlParseFilter.setFilterPattern(Pattern.compile("^http.*"));
                            parseFilters.add(crawlParseFilter);
                        }
                    }

                    WebPage webPage = new WebPage();
                    webPage.setUrl(url);
                    webPage.setBizSiteName((String) item.get("bizSiteName"));
                    webPage.setBizId((String) item.get("bizId"));
                    webPage.setTitle((String) item.get("title"));

                    DBObject parsedDBObject = null;
                    try {

                    	// Call -by parsing filter , if it returns a valid analysis target is mainly extracted packet identification information and ID
                        for (CrawlParseFilter parseFilter : parseFilters) {
                            webPage.setPageText(result);
                            DBObject filterParsedDBObject = parseFilter.filter(url, webPage);
                            if (filterParsedDBObject != null) {
                                parsedDBObject = filterParsedDBObject;
                                webPage.setBizSiteName((String) parsedDBObject.get(CrawlParseFilter.SITE_NAME));
                                webPage.setBizId((String) parsedDBObject.get(CrawlParseFilter.ID));
                            }
                        }

                     // Extract all resolve additional outlink collection , processing injection next cycle
                        int outlinksSize = webPage.getOutlinks().size();
                        List<String> outlinks = Lists.newArrayList();
                        if (outlinksSize > 0) {
                            for (Outlink outlink : webPage.getOutlinks().values()) {
                                if (StringUtils.isBlank(outlink.getBizSiteName())) {
                                    outlink.setBizSiteName(webPage.getBizSiteName());
                                }
                                if (StringUtils.isBlank(outlink.getBizId())) {
                                    outlink.setBizId(webPage.getBizId());
                                }
                                outlinks.add(outlink.getUrl());
                                injectOutlink(outlink, crawlConfig, url);
                            }
                        } else {

                        	// If the current URL after treatment, neither return parsed service data , there is no output outlink, then warn warning this meaningless process url
                            if (parsedDBObject == null) {
                                logger.warn("NO Output after all filter for URL: {}", url);
                            }
                        }


                     // Incremental update to resolve property -related
                        DBObject update = new BasicDBObject();
                        update.put("bizSiteName", webPage.getBizSiteName());
                        update.put("bizId", webPage.getBizId());
                        update.put("title", webPage.getTitle());
                        update.put("outlinks", outlinks);
                        update.put("parseTime", new Date());
                        update.put("parseFailureTimes", 0);
                        update.put("parseFailureException", null);
                        fetchObjectCollection.update(urlQuery, new BasicDBObject("$set", update));
                    } catch (Exception e) {

                    	// Exception resolution process , inject try to fetch and parse again
                        logger.error("Parse exception for: " + url, e);
                        injectParseFailureRetry(webPage, Exceptions.getStackTraceAsString(e));
                    }
                } else {
                    logger.debug("Skipped parse [{}] as HTTP status code is  {}", url, statusCode);
                }
            } else {

            	// If you do not need to resolve the process , directly before the next injection cycle outlink collection process
                logger.debug("Skipped parse [{}] as last parsed time: {}", url, parseTime);
                BasicDBList dbList = (BasicDBList) item.get("outlinks");
                if (dbList != null) {
                    for (Object dbItem : dbList) {
                        Outlink outlink = new Outlink();
                        outlink.setUrl(dbItem.toString());
                        injectOutlink(outlink, crawlConfig, url);
                    }
                }
            }


         // The exception record moved to a designated collection for fast query
            DBCollection failureObjectCollection = buildFailureObjectCollection();
            DBObject fetchItem = fetchObjectCollection.findOne(urlQuery,
                    new BasicDBObject().append("parseFailureTimes", 1).append("fetchFailureTimes", 1));
            Integer fetchFailureTimes = (Integer) fetchItem.get("fetchFailureTimes");
            Integer parseFailureTimes = (Integer) fetchItem.get("parseFailureTimes");
            DBObject failureItem = failureObjectCollection.findOne(urlQuery, urlQuery);
         // If 0 indicates failure are treated successfully removed
            if ((fetchFailureTimes == null || fetchFailureTimes == 0 || statusCode == HttpStatus.SC_OK)
                    && (parseFailureTimes == null || parseFailureTimes == 0)) {
                if (failureItem != null) {
                    failureObjectCollection.remove(failureItem);
                }
            } else {
                DBObject upset = fetchObjectCollection.findOne(urlQuery);
                upset.put("id", url);
                failureObjectCollection.update(urlQuery, upset, true, false);
            }


         // Cumulative counter , and print some important tracking information
            pages.incrementAndGet();
            if (logger.isInfoEnabled()) {
                long elapsed = (System.currentTimeMillis() - crawlStartTime.getTime()) / 1000;
                float avgPagesSec = (float) pages.get() / elapsed;
                logger.info("Total fetched and parsed " + pages.get() + " pages, " + elapsed + " seconds, avg " + avgPagesSec + " pages/s ,thread ["
                        + executor.getActiveCount() + "/" + executor.getCorePoolSize() + "]");
            }
        }
    }

    /**
     * Query a list of all as a seed implanted in the URL from MongoDB
     * @return
     */
    public List<DBObject> findSeedURLs() {
        List<DBObject> seedURLs = Lists.newArrayList();
        DBCollection dbColl = buildFetchObjectCollection();
        BasicDBObject query = new BasicDBObject("injectSeed", Boolean.TRUE);
        BasicDBObject keys = new BasicDBObject();
        keys.put(CrawlParseFilter.URL, 1);
        keys.put("injectTime", 1);
        DBCursor cur = dbColl.find(query, keys).sort(new BasicDBObject("injectTime", 0));
        while (cur.hasNext()) {
            DBObject item = cur.next();
            seedURLs.add(item);
        }
        return seedURLs;
    }

    public void convertLongToDate(String name) {
        DBCollection coll = buildFetchObjectCollection();
        int cnt = 1;
        DBCursor cur = coll.find(new BasicDBObject(name, new BasicDBObject("$type", 16)), new BasicDBObject(CrawlParseFilter.URL, 1).append(name, 1));
        int total = cur.count();
        while (cur.hasNext()) {
            DBObject item = cur.next();
            String url = (String) item.get(CrawlParseFilter.URL);
            DBObject update = new BasicDBObject();
            Integer time = (Integer) item.get(name);
            if (time < 0) {
                time = 0;
            }
            update.put(name, new Date(time));
            logger.debug("{}/{}. Convert type for: {}", cnt++, total, url);
            coll.update(new BasicDBObject(CrawlParseFilter.URL, url), new BasicDBObject("$set", update));
        }
    }

    public List<String> getAllSiteNameList() {
        DBCollection coll = buildParseObjectCollection();
        List<String> bizSiteNameList = coll.distinct("Site packet");
        return bizSiteNameList;
    }

    public OperationResult generateThreadStart(String bizSiteName) {
        new Thread(new GenerateThread(this, bizSiteName)).start();
        return OperationResult.buildSuccessResult("File being generated , please wait refresh");
    }

    private class GenerateThread implements Runnable {
        private CrawlService crawlService;
        private String bizSiteName;

        public GenerateThread(CrawlService crawlService, String bizSiteName) {
            this.crawlService = crawlService;
            this.bizSiteName = bizSiteName;
        }

        @Override
        public void run() {
            crawlService.generateFile(bizSiteName);
        }
    }

    public String generateFile(String bizSiteName) {
        try {
            DBCollection coll = buildParseObjectCollection();
            List<String> bizSiteNameList = coll.distinct("Site packet");

            if (CollectionUtils.isEmpty(bizSiteNameList)) {
                return null;
            }

            Set<String> bizSiteNameSet = Sets.newHashSet();
            bizSiteNameSet.addAll(bizSiteNameList);

            BasicDBObject query = null;
            if (StringUtils.isNotBlank(bizSiteName)) {
                bizSiteName = bizSiteName.trim();
                if (!bizSiteNameSet.contains(bizSiteName)) {
                    return null;
                } else {
                    query = new BasicDBObject("Site packet", bizSiteName);
                    DBCursor dBCursor = coll.find(query);
                    return exportXls(dBCursor, bizSiteName);
                }
            } else {
                List<String> files = Lists.newArrayList();
                for (String siteName : bizSiteNameList) {
                    query = new BasicDBObject("Site packet", siteName);
                    DBCursor dBCursor = coll.find(query);
                    files.add(exportXls(dBCursor, siteName));
                }
                return StringUtils.join(files, ",");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String exportXls(DBCursor dBCursor, String bizSiteName) throws Exception {
        if (dBCursor.count() <= 0) {
            dBCursor.close();
            return null;
        }

     // Copy DBCursor
        DBCursor dBCursorClone = dBCursor.copy();

     // Calculate Header
        Set<String> titleSet = Sets.newHashSet();
        while (dBCursor.hasNext()) {
            BasicDBObject dBObject = (BasicDBObject) dBCursor.next();
            Set<String> keySet = dBObject.keySet();
            titleSet.addAll(keySet);
        }
        dBCursor.close();

        if (CollectionUtils.isEmpty(titleSet)) {
            return null;
        }
        List<String> titleList = Lists.newArrayList(titleSet);

        HSSFWorkbook wb = new HSSFWorkbook();// Create the Excel Workbook object
        HSSFSheet sheet = wb.createSheet();// Create an Excel worksheet object    

        HSSFRow titleRow = sheet.createRow(0);// Create a table header
        for (int i = 0; i < titleList.size(); i++) {
            HSSFCell hssfCell = titleRow.createCell(i);
            hssfCell.setCellValue(titleList.get(i));
        }

        int rowIndex = 1;
        HSSFRow hssfRow = null;
        while (dBCursorClone.hasNext()) {
            BasicDBObject dBObject = (BasicDBObject) dBCursorClone.next();
            hssfRow = sheet.createRow(rowIndex);
            for (int i = 0; i < titleList.size(); i++) {
                HSSFCell hssfCell = hssfRow.createCell(i);
                String cell = dBObject.getString(titleList.get(i));
                if (StringUtils.isEmpty(cell)) {
                    cell = "";
                }
                hssfCell.setCellValue(cell);
            }
            rowIndex++;
        }
        dBCursorClone.close();
        //
        String crawlExportDir = FileUtils.getTempDirectoryPath() + File.separator + "crawl" + File.separator + "export";
        File directory = new File(crawlExportDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String exportFilePath = crawlExportDir + File.separator + bizSiteName + ".xls";
        logger.debug("Save {} crawl Excel export data to file: {}", bizSiteName, exportFilePath);
        FileOutputStream fileOutputStream = new FileOutputStream(exportFilePath);

        wb.write(fileOutputStream);
        fileOutputStream.flush();
        fileOutputStream.close();

        return exportFilePath;
    }
}
