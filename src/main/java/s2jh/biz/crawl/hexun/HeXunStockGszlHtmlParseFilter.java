package s2jh.biz.crawl.hexun;

import lab.s2jh.module.crawl.vo.WebPage;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mongodb.DBObject;

/**
 * Data analysis and stock news : Company Profile
 */
public class HeXunStockGszlHtmlParseFilter extends HeXunStockBaseHtmlParseFilter {

    @Override
    public DBObject filterInternal(String url, WebPage webPage, DBObject parsedDBObject) {
        String pageText = webPage.getPageText();
        DocumentFragment df = parse(pageText);
      //basic information
        {
            NodeList nodeList = selectNodeList(df, "/html/body/div[5]/div[8]/div[1]/div[1]/table/tbody/tr");
            if (nodeList != null && nodeList.getLength() > 0) {
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node node = nodeList.item(i);
                    NodeList childNodes = node.getChildNodes();
                    putKeyValue(parsedDBObject, childNodes.item(1).getTextContent(), childNodes.item(3).getTextContent());
                }
            }
        }

     // Securities Information
        {
            NodeList nodeList = selectNodeList(df, "/html/body/div[5]/div[8]/div[2]/div[1]/table/tbody/tr");
            if (nodeList != null && nodeList.getLength() > 0) {
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node node = nodeList.item(i);
                    NodeList childNodes = node.getChildNodes();
                    putKeyValue(parsedDBObject, childNodes.item(1).getTextContent(), childNodes.item(3).getTextContent());
                }
            }
        }

     // Business Information
        {
            NodeList nodeList = selectNodeList(df, "/html/body/div[5]/div[8]/div[1]/div[2]/table/tbody/tr");
            if (nodeList != null && nodeList.getLength() > 0) {
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node node = nodeList.item(i);
                    NodeList childNodes = node.getChildNodes();
                    putKeyValue(parsedDBObject, childNodes.item(1).getTextContent(), childNodes.item(3).getTextContent());
                }
            }
        }

      //Contact information
        {
            NodeList nodeList = selectNodeList(df, "/html/body/div[5]/div[8]/div[2]/div[2]/table/tbody/tr");
            if (nodeList != null && nodeList.getLength() > 0) {
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node node = nodeList.item(i);
                    NodeList childNodes = node.getChildNodes();
                    putKeyValue(parsedDBObject, childNodes.item(1).getTextContent(), childNodes.item(3).getTextContent());
                }
            }
        }


      //Business Scope
        putKeyValue(parsedDBObject, "经营范围", getXPathValue(df, "/html/body/div[5]/div[8]/div[1]/div[3]/p"));


      //Company Profile
        putKeyValue(parsedDBObject, "公司简介", getXPathValue(df, "/html/body/div[5]/div[8]/div[2]/div[3]/p"));

        return parsedDBObject;
    }

    @Override
    public String getUrlFilterRegex() {
        return "^http://stockdata.stock.hexun.com/gszl/[a-zA-z0-9]{6,}.shtml$";
    }

    @Override
    protected String getPrimaryKey(String url) {
        return StringUtils.substringBefore(StringUtils.substringAfter(url, "http://stockdata.stock.hexun.com/gszl/"), ".shtml").replaceAll("\\D", "");
    }
}
