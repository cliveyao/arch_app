package lab.s2jh.core.crawl;

import java.util.regex.Pattern;

import lab.s2jh.module.crawl.service.CrawlService;
import lab.s2jh.module.crawl.vo.WebPage;

import com.mongodb.DBObject;

public interface CrawlParseFilter {

    public final static String URL = "URL";

    public final static String ID = "Business logo";

    public final static String PARSE_INLINK_URL = "Injection source";

    public final static String PARSE_FROM_URLS = "Analytical Sources";

    public final static String SITE_NAME = "Site packet";

    public final static String PAGE_TITLE = "page title";

    public final static String OUTLINKS = "outlinks";

    /**
     * Dynamic settings for custom filters when
     * @param filterPattern
     */
    void setFilterPattern(Pattern filterPattern);

    void setCrawlService(CrawlService crawlService);

    DBObject filter(String url, WebPage webPage) throws Exception;
}
