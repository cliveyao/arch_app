package s2jh.biz.crawl.yibiao;

import lab.s2jh.module.crawl.vo.WebPage;

import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;

import com.mongodb.DBObject;

public class YibiaoEnterpriseHtmlParseFilter extends YibiaoBaseHtmlParseFilter {

    @Override
    public DBObject filterInternal(String url, WebPage webPage, DBObject parsedDBObject) {
        String pageText = webPage.getPageText();
        DocumentFragment doc = parse(pageText);

        
        Node introduce = selectSingleNode(doc, "//div[@id='menu']//a[SPAN='Company Profile']");
        String introduceURL = getXPathAttribute(introduce, "./", "href");
        webPage.addOutlink(introduceURL);

        
        Node contact = selectSingleNode(doc, "//div[@id='menu']//a[SPAN='Contact information']");
        String contactURL = getXPathAttribute(contact, "./", "href");
        webPage.addOutlink(contactURL);
        return null;
    }

    @Override
    public String getUrlFilterRegex() {
        return "^http://www.21yibiao.com/com/\\w+/?$";
    }
}
