package lab.s2jh.module.crawl.filter;

import lab.s2jh.core.crawl.AbstractHtmlParseFilter;
import lab.s2jh.module.crawl.vo.Outlink;
import lab.s2jh.module.crawl.vo.WebPage;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;

import com.mongodb.DBObject;

public class WebSiteContactInfoStartHtmlParseFilter extends AbstractHtmlParseFilter {

    @Override
    public DBObject filterInternal(String url, WebPage webPage, DBObject parsedDBObject) {
        String pageText = webPage.getPageText();
        DocumentFragment df = parse(pageText);

        Node node = selectSingleNode(df, "//A[contains(text(),'contact us')]");
        if (node == null) {
            node = selectSingleNode(df, "//A[contains(text(),'Contact information')]");
        }
        if (node != null) {
            Node hrefAttr = node.getAttributes().getNamedItem("href");
            if (hrefAttr != null) {
                String href = hrefAttr.getTextContent();
                Outlink outlink = webPage.addOutlink(href, WebSiteContactInfoDetailHtmlParseFilter.class);
                if (outlink != null) {
                    parsedDBObject.put("Contact URL", outlink.getUrl());
                }
            }
        }
        String title = webPage.getTitle();
        if (StringUtils.isBlank(title)) {
            title = getXPathValue(df, "//TITLE");
            title = StringUtils.substringBefore(title, "-");
        }
        parsedDBObject.put("Site title", title);
        parsedDBObject.put("Site link", url);
        return parsedDBObject;
    }

    @Override
    public String getUrlFilterRegex() {
        return null;
    }
}
