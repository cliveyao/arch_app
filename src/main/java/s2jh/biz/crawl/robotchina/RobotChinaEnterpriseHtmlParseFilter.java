package s2jh.biz.crawl.robotchina;

import lab.s2jh.module.crawl.vo.WebPage;

import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;

import com.mongodb.DBObject;

public class RobotChinaEnterpriseHtmlParseFilter extends RobotChinaBaseHtmlParseFilter {

    @Override
    public String getUrlFilterRegex() {
        return "^http://[^\\.]+.robot-china.com/$";
    }

    @Override
    public DBObject filterInternal(String url, WebPage webPage, DBObject parsedDBObject) throws Exception {
        String pageText = webPage.getPageText();
        DocumentFragment doc = parse(pageText);

       
        Node contact = selectSingleNode(doc, "//*[@id='menu']//a[contains(SPAN,'Contact information')]");
        if (contact != null) {
            String href = getNodeAttribute(contact, "href");
            webPage.addOutlink(href);
        }

        
        Node sell = selectSingleNode(doc, "//*[@id='menu']//a[contains(SPAN,'product')]");
        if (sell != null) {
            String href = getNodeAttribute(sell, "href");
            webPage.addOutlink(href);
        }

        
        Node introduce = selectSingleNode(doc, "//*[@id='menu']//a[contains(SPAN,'Company Profile')]");
        if (introduce != null) {
            String href = getNodeAttribute(introduce, "href");
            webPage.addOutlink(href);
        }

        return null;
    }
}
