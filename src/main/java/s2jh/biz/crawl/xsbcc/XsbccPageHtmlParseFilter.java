package s2jh.biz.crawl.xsbcc;

import lab.s2jh.core.crawl.AbstractHtmlParseFilter;
import lab.s2jh.module.crawl.vo.WebPage;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class XsbccPageHtmlParseFilter extends AbstractHtmlParseFilter {

    @Override
    public DBObject filterInternal(String url, WebPage webPage, DBObject parsedDBObject) {
        String pageText = webPage.getPageText();

        String rowsText = StringUtils.substringAfter(pageText, "<tr><td>");
        if (StringUtils.isBlank(rowsText)) {
            injectParseFailureRetry(webPage, "Unresolved valid data , trying to crawl again resolve");
            return null;
        }

        String pageData = "<html><body><table><tr><td>" + rowsText + "</table></body></html>";
        DocumentFragment df = parse(pageData);
        NodeList nodeList = selectNodeList(df, "//TR");
        if (nodeList != null && nodeList.getLength() > 0) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                Node codeNode = selectSingleNode(node, "./td[2]/a");
                String code = codeNode.getTextContent().trim();
                DBObject outlinkParsedDBObject = new BasicDBObject();
                putKeyValue (outlinkParsedDBObject, "securities code", code);
                putKeyValue (outlinkParsedDBObject, "the securities referred to", getXPathValue (node, "./td[3]"));
                putKeyValue (outlinkParsedDBObject, "transfer mode", getXPathValue (node, "./td[4]"));
                putKeyValue (outlinkParsedDBObject, "previous closing price (yuan / share)", getXPathValue (node, "./td[5]"));
                putKeyValue (outlinkParsedDBObject, "a recent transaction price (yuan / share)", getXPathValue (node, "./td[6]"));
                putKeyValue (outlinkParsedDBObject, "Turnover (million)", getXPathValue (node, "./td[7]"));
                putKeyValue (outlinkParsedDBObject, "Volume (shares)", getXPathValue (node, "./td[8]"));
                putKeyValue (outlinkParsedDBObject, "Change", getXPathValue (node, "./td[9]"));
                putKeyValue (outlinkParsedDBObject, "up down", getXPathValue (node, "./td[10]"));
                putKeyValue (outlinkParsedDBObject, "earnings", getXPathValue (node, "./td[11]"));
                putKeyValue (outlinkParsedDBObject, "listing the time", getXPathValue (node, "./td[12]"));
                putKeyValue (outlinkParsedDBObject, "industry", getXPathValue (node, "./td[13]"));
                putKeyValue (outlinkParsedDBObject, "area", getXPathValue (node, "./td[14]"));
                putKeyValue (outlinkParsedDBObject, "broker", getXPathValue (node, "./td[15]"));

                webPage.addOutlink (getNodeAttribute (codeNode, "href"), null, (String) outlinkParsedDBObject.get ( "securities referred"), getSiteName (url),
                        (String) outlinkParsedDBObject.get ( "ticker"), outlinkParsedDBObject);

                // Main indicators data: http: //stockpage.10jqka.com.cn/basic/834334/main.txt
                webPage.addOutlink ( "http://stockpage.10jqka.com.cn/basic/" + code + "/main.txt", null, (String) outlinkParsedDBObject.get ( "securities referred"),
                        getSiteName (url), (String) outlinkParsedDBObject.get ( "ticker"), outlinkParsedDBObject);

                // Balance Sheet: http: //stockpage.10jqka.com.cn/basic/834334/debt.txt
                webPage.addOutlink ( "http://stockpage.10jqka.com.cn/basic/" + code + "/debt.txt", null, (String) outlinkParsedDBObject.get ( "securities referred"),
                        getSiteName (url), (String) outlinkParsedDBObject.get ( "ticker"), outlinkParsedDBObject);

                // Shareholders' equity: http: //stockpage.10jqka.com.cn/834334/holder/
                webPage.addOutlink ( "http://stockpage.10jqka.com.cn/" + code + "/ holder /", null, (String) outlinkParsedDBObject.get ( "securities referred"),
                        getSiteName (url), (String) outlinkParsedDBObject.get ( "ticker"), outlinkParsedDBObject);

                // Three board information
                webPage.addOutlink ( "http://www.sanban18.com/stock/" + code + "/profile.html", null, (String) outlinkParsedDBObject.get ( "securities referred"),
                        getSiteName (url), (String) outlinkParsedDBObject.get ( "ticker"), outlinkParsedDBObject);            }

            //注入下一页
            String pager = StringUtils.substringBetween(url, "pn=", "&");
            if (StringUtils.isBlank(pager)) {
                pager = "1";
            }
            int nextPage = Integer.valueOf(pager) + 1;
            String outlinkURL = "http://www.xsbcc.com/common/company.htm?r=0.1807033922486062&pn=" + nextPage
                    + "&ru=&uid=&hy=&addr=&quan=&tm1=&tm2=&se=&tp=0&tz=&gao=&phoneemail=13124705728&od=txtno%20desc";
            webPage.addOutlink(outlinkURL, true);
        }

        return null;
    }

    @Override
    public String getUrlFilterRegex() {
        return "^http://www.xsbcc.com/common/company.htm\\?r=0.1807033922486062&pn=[0-9]+&ru=&uid=&hy=&addr=&quan=&tm1=&tm2=&se=&tp=0&tz=&gao=&phoneemail=13124705728&od=txtno%20desc$";
    }
}
