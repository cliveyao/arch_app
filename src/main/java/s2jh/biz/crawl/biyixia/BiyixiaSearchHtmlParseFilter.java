package s2jh.biz.crawl.biyixia;

import lab.s2jh.module.crawl.vo.WebPage;

import com.mongodb.DBObject;

public class BiyixiaSearchHtmlParseFilter extends BiyixiaBaseHtmlParseFilter {

    @Override
    public DBObject filterInternal(String url, WebPage webPage, DBObject parsedDBObject) {

    	// TODO briefly injected into 25 pages , available from late optimized for dynamic AJAX
        for (int i = 1; i <= 25; i++) {
            webPage.addOutlink("http://self-media.biyixia.com/node/" + i);
        }
        return null;
    }

    @Override
    public String getUrlFilterRegex() {
        return "^http://self-media.biyixia.com/?$";
    }
}
