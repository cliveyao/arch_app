package s2jh.biz.crawl.cy8;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lab.s2jh.module.crawl.vo.WebPage;

import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mongodb.DBObject;

public class Cy8NavigationHtmlParseFilter extends Cy8BaseHtmlParseFilter {

    @Override
    public DBObject filterInternal(String url, WebPage webPage, DBObject parsedDBObject) {
        String pageText = webPage.getPageText();
        DocumentFragment doc = parse(pageText);


        //Home  Get the franchise joined the navigation links
        NodeList nodeList = selectNodeList(doc, "//DIV[@class='kcjm_dh']//LI");
        if (nodeList != null && nodeList.getLength() > 0) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                String href = getXPathAttribute(node, "./A", "href");


             // Inside the dessert franchise navigation is categorized links , in order to avoid an endless loop
                String regEx = "^http://www.cy8.com.cn/[a-z]+/\\d+$";
                Pattern pattern = Pattern.compile(regEx);
                Matcher matcher = pattern.matcher(href);
                if (matcher.find()) {
                    webPage.addOutlink(href);
                }
            }
        }

        return null;
    }

    @Override
    public String getUrlFilterRegex() {
        return "^http://www.cy8.com.cn/[a-z]+/?$";
    }
}
