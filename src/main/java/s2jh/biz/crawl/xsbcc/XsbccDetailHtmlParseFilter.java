package s2jh.biz.crawl.xsbcc;

import lab.s2jh.core.crawl.AbstractHtmlParseFilter;
import lab.s2jh.module.crawl.vo.WebPage;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;

import com.mongodb.DBObject;

public class XsbccDetailHtmlParseFilter extends AbstractHtmlParseFilter {

    @Override
    public DBObject filterInternal(String url, WebPage webPage, DBObject parsedDBObject) {
        String pageText = webPage.getPageText();
        DocumentFragment doc = parse(pageText);

        Node node = selectSingleNode(doc, "//DIV[@class='rcon']");
        if (node != null) {
            String content = node.getTextContent();
            putKeyValue (parsedDBObject, "company name", StringUtils.substringBetween (content, "the company name:", "English name:"));
            putKeyValue (parsedDBObject, "English name", StringUtils.substringBetween (content, "English name:", "Registered address:"));
            putKeyValue (parsedDBObject, "registered address", StringUtils.substringBetween (content, "Registered address:", "legal representative:"));
            putKeyValue (parsedDBObject, "legal representative", StringUtils.substringBetween (content, "legal representative:", "company secretaries:"));
            putKeyValue (parsedDBObject, "company secretaries", StringUtils.substringBetween (content, "the company secretaries:" ,"registered capital (million):"));
            putKeyValue (parsedDBObject, "the registered capital (million)", StringUtils.substringBetween (content, "the registered capital (million):", "Category:"));
            putKeyValue (parsedDBObject, "Category", StringUtils.substringBetween (content, "Classification:", "listing date:"));
            putKeyValue (parsedDBObject, "listing date", StringUtils.substringBetween (content, "listing date:", "Website:"));
            putKeyValue (parsedDBObject, "Website", StringUtils.substringBetween (content, "the company Web site:", "transfer mode:"));
            putKeyValue (parsedDBObject, "transfer mode", StringUtils.substringBetween (content, "transfer mode:", "hosted by the broker:"));
            putKeyValue (parsedDBObject, "sponsored by brokerage", StringUtils.substringBetween (content, "sponsored by the broker:", "\n \n"));
            return parsedDBObject;
        } else {
            return null;

        }

    }

    @Override
    public String getUrlFilterRegex() {
        return "^http://www.xsbcc.com/company/\\d+.html$";
    }
}
