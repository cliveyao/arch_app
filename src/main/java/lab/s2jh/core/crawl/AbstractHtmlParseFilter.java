package lab.s2jh.core.crawl;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import lab.s2jh.core.util.Digests;
import lab.s2jh.module.crawl.service.CrawlService;
import lab.s2jh.module.crawl.vo.WebPage;
import lab.s2jh.support.service.DynamicConfigService;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract1;
import net.sourceforge.tess4j.util.ImageHelper;
import net.sourceforge.tess4j.util.LoadLibs;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.html.dom.HTMLDocumentImpl;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.xml.utils.DOMBuilder;
import org.cyberneko.html.parsers.DOMFragmentParser;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.common.collect.Lists;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.sun.org.apache.xpath.internal.XPathAPI;

/**
 * 
 * @author EMAIL:s2jh-dev@hotmail.com , QQ:2414521719
 *
 */
public abstract class AbstractHtmlParseFilter implements CrawlParseFilter {

    protected static final Logger logger = LoggerFactory.getLogger("crawl.parse");

    private Pattern filterPattern;

    private static String imgSaveRootDir;

    protected CrawlService crawlService;

    protected DynamicConfigService dynamicConfigService;

    /**
     * Get a list of Node Based xpath
     * @param node
     * @param xpath
     * @return
     */
    protected NodeList selectNodeList(Node contextNode, String xpath) {
        try {
            if (contextNode != null && StringUtils.isNotBlank(xpath)) {
                xpath = convertXPath(xpath);
                return XPathAPI.selectNodeList(contextNode, xpath);
            }
        } catch (TransformerException e) {
            logger.warn("Bad 'xpath' expression [{}]", xpath);
        }
        return null;
    }

    /**
     * Get Node node based xpath
     * @param node
     * @param xpath
     * @return
     */
    protected Node selectSingleNode(Node contextNode, String xpath) {
        try {
            if (contextNode != null && StringUtils.isNotBlank(xpath)) {
                xpath = convertXPath(xpath);
                return XPathAPI.selectSingleNode(contextNode, xpath);
            }
        } catch (TransformerException e) {
            logger.warn("Bad 'xpath' expression [{}]", xpath);
        }
        return null;
    }

    /**
     * Based xpath -defined parse img element returns the full path URL string format
     * @param url Page URL , some elements of the img src as a relative path , through this url full URL path merge assembly image
     * @param contextNode
     * @param xpaths Multiple xpath string is mainly used for fault tolerance , and some page format is not uniform , it may be a desired image in xpath1, some in xpath2, given a list of possible multiple xpath , in order to find a matching loop terminates loop
     * @return Picture full path beginning with http URL
     */
    protected String getImgSrcValue(String url, Node contextNode, String... xpaths) {
        for (String xpath : xpaths) {
            Node node = selectSingleNode(contextNode, xpath);
            String imgUrl = null;
            if (node != null) {
                NamedNodeMap atrributes = node.getAttributes();
                Node attr = atrributes.getNamedItem("data-ks-lazyload");
                if (attr == null) {
                    attr = atrributes.getNamedItem("lazy-src");
                }
                if (attr == null) {
                    attr = atrributes.getNamedItem("src");
                }
                if (attr != null) {
                    imgUrl = attr.getTextContent();
                }
            }
            if (StringUtils.isNotBlank(imgUrl)) {
                return parseImgSrc(url, imgUrl);
            }
        }
        return "";
    }

    private String convertXPath(String xpath) {
        String[] paths = xpath.split("/");
        List<String> convertedPaths = Lists.newArrayList();
        for (String path : paths) {
            if ("text()".equalsIgnoreCase(path)) {
                convertedPaths.add(path.toLowerCase());
            } else if (path.indexOf("[") > -1) {
                String[] splits = StringUtils.split(path, "[");
                convertedPaths.add(splits[0].toUpperCase() + "[" + splits[1]);
            } else {
                convertedPaths.add(path.toUpperCase());
            }
        }

        String convertedPath = StringUtils.join(convertedPaths, "/");
        logger.trace("Converted XPath is: {}", convertedPath);
        return convertedPath;
    }

    /**
     * Returns text based content xpath position, or null if the element is not found
     * @param contextNode
     * @param xpath
     * @return
     */
    protected String getXPathValue(Node contextNode, String xpath) {
        return getXPathValue(contextNode, xpath, null);
    }

    /**
     * Location Based xpath return text content , if the element is not found then return to the default defaultVal
     * @param contextNode
     * @param xpath
     * @param defaultVal
     * @return
     */
    protected String getXPathValue(Node contextNode, String xpath, String defaultVal) {
        NodeList nodes = selectNodeList(contextNode, xpath);
        if (nodes == null || nodes.getLength() <= 0) {
            return defaultVal;
        }
        String txt = "";
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node instanceof Text) {
                txt += node.getNodeValue();
            } else {
                txt += node.getTextContent();
            }
        }
        return cleanInvisibleChar(txt);
    }

    /**
     * Based xpath returns the corresponding content in html format
     * @param contextNode
     * @param xpath
     * @return
     */
    protected String getXPathHtml(Node contextNode, String xpath) {
        Node node = selectSingleNode(contextNode, xpath);
        return asString(node);
    }

    /**
     * Location Based xpath correspond attr attribute content , or null if the element is not found
     * @param contextNode
     * @param xpath
     * @return
     */
    protected String getXPathAttribute(Node contextNode, String xpath, String attr) {
        Node node = selectSingleNode(contextNode, xpath);
        if (node != null) {
            NamedNodeMap atrributes = node.getAttributes();
            Node attrNode = atrributes.getNamedItem(attr);
            if (attrNode != null) {
                String text = attrNode.getTextContent();
                if (text != null) {
                    return text.trim();
                }
            }
        }
        return null;
    }

    /**
     * Location Based xpath correspond attr attribute content , or null if the element is not found
     * @param contextNode
     * @param xpath
     * @return
     */
    protected String getNodeAttribute(Node node, String attr) {
        if (node != null) {
            NamedNodeMap atrributes = node.getAttributes();
            Node attrNode = atrributes.getNamedItem(attr);
            if (attrNode != null) {
                return attrNode.getTextContent();
            }
        }
        return null;
    }

    /**
     * Location Based xpath correspond attr attribute content , or null if the element is not found
     * @param contextNode
     * @param xpath
     * @return
     */
    protected String getNodeText(Node node) {
        if (node != null) {
            String txt = "";
            if (node instanceof Text) {
                txt += node.getNodeValue();
            } else {
                txt += node.getTextContent();
            }
            return cleanInvisibleChar(txt);
        }
        return null;
    }

    protected String asString(Node node) {
        if (node == null) {
            return "";
        }
        try {
            StringWriter writer = new StringWriter();
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.transform(new DOMSource(node), new StreamResult(writer));
            String xml = writer.toString();
            xml = StringUtils.substringAfter(xml, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            xml = xml.trim();
            return xml;
        } catch (Exception e) {
            throw new IllegalArgumentException("error for parse node to string.", e);
        }
    }

    /**
     * Src attribute handle different image formats , returns a unified format http URL format images
     * @param url
     * @param imgSrc
     * @return
     */
    private String parseImgSrc(String url, String imgSrc) {
        if (StringUtils.isBlank(imgSrc)) {
            return "";
        }
        imgSrc = imgSrc.trim();
        // Remove the last link of the #
        imgSrc = StringUtils.substringBefore(imgSrc, "#");
        if (imgSrc.startsWith("http")) {
            return imgSrc;
        } else if (imgSrc.startsWith("/")) {
            if (url.indexOf(".com") > -1) {
                return StringUtils.substringBefore(url, ".com/") + ".com" + imgSrc;
            } else if (url.indexOf(".net") > -1) {
                return StringUtils.substringBefore(url, ".net/") + ".net" + imgSrc;
            } else {
                throw new RuntimeException("Undefined site domain suffix");
            }
        } else {
            return StringUtils.substringBeforeLast(url, "/") + "/" + imgSrc;
        }
    }

    /**
     * Clear unrelated invisible whitespace characters
     * @param str
     * @return
     */
    protected String cleanInvisibleChar(String str) {
        return cleanInvisibleChar(str, false);
    }

    /**
     * Clear unrelated invisible whitespace characters
     * @param str
     * @param includingBlank Whether to include removing whitespace characters within the text
     * @return
     */
    protected String cleanInvisibleChar(String str, boolean includingBlank) {
        if (str != null) {
            str = StringUtils.remove(str, (char) 160);
            if (includingBlank) {
            	// Common spaces
                str = StringUtils.remove(str, " ");
             // Em space
                str = StringUtils.remove(str, (char) 12288);
            }
            str = StringUtils.remove(str, "\r");
            str = StringUtils.remove(str, "\n");
            str = StringUtils.remove(str, "\t");
            str = StringUtils.remove(str, "\\s*");
            str = StringUtils.remove(str, "◆");
            str = StringUtils.remove(str, "�");
            str = str.trim();
        }
        return str;
    }

    /**
     * Clear Node node element unrelated
     * @param str
     * @return
     */
    protected void cleanUnusedNodes(Node doc) {
        cleanUnusedNodes(doc, "//STYLE");
        cleanUnusedNodes(doc, "//MAP");
        cleanUnusedNodes(doc, "//SCRIPT");
        cleanUnusedNodes(doc, "//script");
    }

    /**
     * Clear Node node element unrelated
     * @param str
     * @return
     */
    protected void cleanUnusedNodes(Node node, String xpath) {
        try {
            NodeList nodes = XPathAPI.selectNodeList(node, xpath);
            for (int i = 0; i < nodes.getLength(); i++) {
                Element element = (Element) nodes.item(i);
                element.getParentNode().removeChild(element);
            }
        } catch (DOMException e) {
            throw new IllegalStateException(e);
        } catch (TransformerException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Forced provided for custom filters when
     * @param filterPattern
     */
    public void setFilterPattern(Pattern filterPattern) {
        this.filterPattern = filterPattern;
    }

    /**
     *Analyzing url Meets Custom resolve matching rules
     * @param url
     * @return
     */
    private boolean isUrlMatchedForParse(String url) {
        if (filterPattern == null) {
            String regex = getUrlFilterRegex();
            if (StringUtils.isBlank(regex)) {
                return false;
            }
            filterPattern = Pattern.compile(regex);
        }
        if (filterPattern.matcher(url).find()) {
            return true;
        }
        return false;
    }

    @Override
    public DBObject filter(String url, WebPage webPage) throws Exception {

        //URL matching
        if (!isUrlMatchedForParse(url)) {
            logger.trace("Skipped {} as not match regex [{}]", this.getClass().getName(), getUrlFilterRegex());
            return null;
        }

        logger.info("Invoking parse  {} for url: {}", this.getClass().getName(), url);

        if (StringUtils.isBlank(webPage.getPageText())) {
            logger.warn("Skipped as no fetch data found for url: {}", url);
            return null;
        }

        String bizSiteName = webPage.getBizSiteName();
        if (StringUtils.isBlank(bizSiteName)) {
            bizSiteName = getSiteName(url);
        }
        String bizId = webPage.getBizId();
        if (StringUtils.isBlank(bizId)) {
            bizId = getPrimaryKey(url);
        }

        DBObject update = new BasicDBObject();
        update = filterInternal(url, webPage, update);

        if (update == null) {
            logger.trace("Skipped as no data parsed for url: {}", url);
            return null;
        }

        crawlService.saveParseDBObject(url, bizSiteName, bizId, update);
        return update;
    }

    protected DocumentFragment parse(String input) {
        return parse(new InputSource(new StringReader(input)), null);
    }

    /**
     * Similar known：http://www.jumeiglobal.com/deal/ht150312p1286156t1.html
     * Collect the required data elements in the textarea below , htmlunit when printXml textarea content will escape treatment , doc objects cause was not directly XPath location
     * So we need to advance textarea element content into separate DocumentFragment object, and then based on this document object data analysis
     * @see org.apache.nutch.parse.html.HtmlParser#parse
     * @param input
     * @return 
     * @throws Exception
     */
    protected DocumentFragment parse(InputSource input, String parserImpl) {
        try {
            if ("tagsoup".equalsIgnoreCase(parserImpl))
                return parseTagSoup(input);
            else
                return parseNeko(input);
        } catch (Exception e) {
            logger.warn("Parsing error: " + e.getMessage());
            logger.trace(e.getMessage(), e);
        }
        return null;
    }

    /**
     * @see org.apache.nutch.parse.html.HtmlParser#parseTagSoup
     */
    private DocumentFragment parseTagSoup(InputSource input) throws Exception {
        HTMLDocumentImpl doc = new HTMLDocumentImpl();
        DocumentFragment frag = doc.createDocumentFragment();
        DOMBuilder builder = new DOMBuilder(doc, frag);
        org.ccil.cowan.tagsoup.Parser reader = new org.ccil.cowan.tagsoup.Parser();
        reader.setContentHandler(builder);
        reader.setFeature(org.ccil.cowan.tagsoup.Parser.ignoreBogonsFeature, true);
        reader.setFeature(org.ccil.cowan.tagsoup.Parser.bogonsEmptyFeature, false);
        reader.setProperty("http://xml.org/sax/properties/lexical-handler", builder);
        reader.parse(input);
        return frag;
    }

    /**
     * @see org.apache.nutch.parse.html.HtmlParser#parseNeko
     */
    private DocumentFragment parseNeko(InputSource input) throws Exception {
        DOMFragmentParser parser = new DOMFragmentParser();
        try {
            parser.setFeature("http://cyberneko.org/html/features/scanner/allow-selfclosing-iframe", true);
            parser.setFeature("http://cyberneko.org/html/features/augmentations", true);
            parser.setProperty("http://cyberneko.org/html/properties/default-encoding", "UTF-8");
            parser.setFeature("http://cyberneko.org/html/features/scanner/ignore-specified-charset", true);
            parser.setFeature("http://cyberneko.org/html/features/balance-tags/ignore-outside-content", false);
            parser.setFeature("http://cyberneko.org/html/features/balance-tags/document-fragment", true);
            parser.setFeature("http://cyberneko.org/html/features/report-errors", logger.isTraceEnabled());
        } catch (SAXException e) {
        }
        // convert Document to DocumentFragment
        HTMLDocumentImpl doc = new HTMLDocumentImpl();
        doc.setErrorChecking(false);
        DocumentFragment res = doc.createDocumentFragment();
        DocumentFragment frag = doc.createDocumentFragment();
        parser.parse(input, frag);
        res.appendChild(frag);

        try {
            while (true) {
                frag = doc.createDocumentFragment();
                parser.parse(input, frag);
                if (!frag.hasChildNodes())
                    break;
                if (logger.isInfoEnabled()) {
                    logger.info(" - new frag, " + frag.getChildNodes().getLength() + " nodes.");
                }
                res.appendChild(frag);
            }
        } catch (Exception x) {
            logger.error("Failed with the following Exception: ", x);
        }
        return res;
    }

    private static ThreadLocal<ITesseract> tesseractInstanceThreadLocal;

    private static File tessDataFolder;

    private synchronized ITesseract buildTesseractInstance() {
        if (tesseractInstanceThreadLocal == null) {
            tesseractInstanceThreadLocal = new ThreadLocal<ITesseract>();

            tessDataFolder = LoadLibs.extractTessResources("tessdata"); // Maven build only; only English data bundled
            try {
                URL tessResourceUrl = AbstractHtmlParseFilter.class.getResource("/tesseract/chi_sim.traineddata");
                FileUtils.copyFileToDirectory(new File(tessResourceUrl.getPath()), tessDataFolder);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        ITesseract tesseractInstance = tesseractInstanceThreadLocal.get();
        if (tesseractInstance == null) {
            //ImageIO.scanForPlugins(); // for server environment
            //ITesseract instance = new Tesseract(); // JNA Interface Mapping
            tesseractInstance = new Tesseract1(); // JNA Direct Mapping

            tesseractInstance.setDatapath(tessDataFolder.getAbsolutePath());
            tesseractInstance.setLanguage("eng");
            tesseractInstanceThreadLocal.set(tesseractInstance);
        }
        return tesseractInstance;
    }

    private static String ocrImageDir;
    private static ITesseract tesseractInstance;

    private synchronized ITesseract buildTesseractSingleInstance() {
        if (tesseractInstance == null) {
            tessDataFolder = LoadLibs.extractTessResources("tessdata"); // Maven build only; only English data bundled
            try {
                URL tessResourceUrl = AbstractHtmlParseFilter.class.getResource("/tesseract/chi_sim.traineddata");
                FileUtils.copyFileToDirectory(new File(tessResourceUrl.getPath()), tessDataFolder);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            //ImageIO.scanForPlugins(); // for server environment
            //ITesseract instance = new Tesseract(); // JNA Interface Mapping
            tesseractInstance = new Tesseract1(); // JNA Direct Mapping

            tesseractInstance.setDatapath(tessDataFolder.getAbsolutePath());
            tesseractInstance.setLanguage("eng");

            File dir = new File(FileUtils.getTempDirectoryPath(), "crawl/ocr/");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            ocrImageDir = dir.getAbsolutePath();
        }
        return tesseractInstance;
    }

    public synchronized String processOCR(String url, String src, boolean chinese) {
        String result = null;
        File imageFile = null;

        logger.info("Processing OCR image for URL: {} src: {}", url, src);
        //Data initialization
        ITesseract tesseractInstance = buildTesseractSingleInstance();
        CloseableHttpResponse httpGetResponse = null;
        try {
            String imageFormat = null;
            String fileId = Digests.md5(src);
            File cachedFile = new File(ocrImageDir, fileId);
            if (cachedFile.exists()) {
                ImageInputStream iis = ImageIO.createImageInputStream(cachedFile);
                // Find all image readers that recognize the image format
                Iterator<ImageReader> iter = ImageIO.getImageReaders(iis);
                if (iter.hasNext()) {
                    // Use the first reader
                    ImageReader reader = iter.next();
                    imageFormat = reader.getFormatName();
                }
                // Close stream
                iis.close();
                logger.info("Using cached OCR image: {}, format: {}", cachedFile.getAbsolutePath(), imageFormat);
            } else {
                HttpGet httpGet = new HttpGet(src);
                logger.info("Fetching OCR image URL: {} src: {}", url, src);
                httpGetResponse = CrawlService.buildHttpClient().execute(httpGet);
                int statusCode = httpGetResponse.getStatusLine().getStatusCode();
                if (statusCode == HttpStatus.SC_OK) {
                    HttpEntity httpGetResponseEntity = httpGetResponse.getEntity();
                    ContentType contentType = ContentType.get(httpGetResponseEntity);
                    imageFormat = StringUtils.substringAfterLast(contentType.getMimeType(), "/");
                    logger.info("Save cached OCR image: {}, format: {}", cachedFile.getAbsolutePath(), imageFormat);
                    FileUtils.copyInputStreamToFile(httpGetResponseEntity.getContent(), cachedFile);
                } else {
                    logger.warn("HTTP ERROR StatusCode is {} URL: {} src: {}", statusCode, url, src);
                    return null;
                }
            }

            if (imageFormat != null) {
                BufferedImage inImg = ImageIO.read(cachedFile);
                // Image sharpening, the main factors affecting the recognition rate their own use dot matrix printer is writing incoherent , so sharpen but lower recognition rate
                // TextImage = ImageHelper.convertImageToBinary (textImage);
                // 5X magnification image , enhance the recognition rate ( many picture itself is not recognized , 5X magnification when you can easily identify , but to test the filter to the client computer configuration is low , Dot Matrix Printer incoherent problem here 5X magnification )
                BufferedImage newImage = ImageHelper.getScaledInstance(inImg, inImg.getWidth() * 5, inImg.getHeight() * 5);

                imageFile = new File(ocrImageDir, fileId + ".temp." + imageFormat);
                ImageIO.write(newImage, imageFormat, imageFile);

                if (chinese) {
                    tesseractInstance.setLanguage("chi_sim");
                    result = tesseractInstance.doOCR(imageFile);
                } else {
                    tesseractInstance.setLanguage("eng");
                    result = tesseractInstance.doOCR(imageFile);
                }
                if (result != null) {
                    result = result.trim();
                }
                logger.debug("OCR result: {} for URL: {} src: {}", result, url, src);
                return result;
            }
        } catch (Exception e) {
            logger.warn("Tesseract OCR Exception, URL: " + url + ", SRC: " + src, e);
        } finally {
            IOUtils.closeQuietly(httpGetResponse);
            if (imageFile != null) {
                imageFile.delete();
            }
        }
        return null;
    }

    protected String saveImage(String url, String src, DBObject parsedDBObject) {
        if (imgSaveRootDir == null) {

            if (dynamicConfigService != null) {
                imgSaveRootDir = dynamicConfigService.getString("cfg_crawl_image_save_root_dir");
            }

            if (StringUtils.isBlank(imgSaveRootDir)) {
                String OS = System.getProperty("os.name").toLowerCase();
                if (OS.indexOf("windows") > -1) {
                    imgSaveRootDir = "c:\\crawl\\images\\";
                } else {
                    imgSaveRootDir = "/crawl/images/";
                }
            }
        }

        CloseableHttpResponse httpGetResponse = null;
        try {
            HttpGet httpGet = new HttpGet(src);
            httpGetResponse = CrawlService.buildHttpClient().execute(httpGet);
            int statusCode = httpGetResponse.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                HttpEntity httpGetResponseEntity = httpGetResponse.getEntity();
                ContentType contentType = ContentType.get(httpGetResponseEntity);
                String format = StringUtils.substringAfterLast(contentType.getMimeType(), "/");
                DateTime now = new DateTime();
                String imagePath = now.getYear() + "/" + now.getMonthOfYear() + "/" + now.getDayOfMonth() + "/" + Digests.md5(src) + "." + format;
                logger.info("Save image file URL: {}, src: {}, path: {}", url, src, imagePath);
                FileUtils.copyInputStreamToFile(httpGetResponseEntity.getContent(), new File(imgSaveRootDir, imagePath));
                return imagePath;
            } else {
                logger.warn("HTTP ERROR StatusCode is {} URL: {} src: {}", statusCode, url, src);
            }
        } catch (Exception e) {
            logger.warn("Fetch Image Exception, URL: " + url + ", SRC: " + src, e);
        } finally {
            IOUtils.closeQuietly(httpGetResponse);
        }
        return null;
    }

    @Override
    public void setCrawlService(CrawlService crawlService) {
        this.crawlService = crawlService;
    }

    /**
     * Sets the current filter matches a URL parsing regular expressions
     * Only match url to resolve the current call processing logic
     * @return
     */
    public abstract String getUrlFilterRegex();

    /**
     * Set the current parse the data updated master key , it is possible to merge multiple Filter update the same object
     * @return
     */
    protected String getPrimaryKey(String url) {
        return url;
    }

    /**
     * Back page to your site name
     * @return
     */
    protected String getSiteName(String url) {
        return StringUtils.substringBefore(StringUtils.substringAfter(url, "://"), "/");
    }

    /**
     * Subclasses implement specific page data parsing logic
     * @return
     */
    public abstract DBObject filterInternal(String url, WebPage webPage, DBObject parsedDBObject) throws Exception;

    protected void putKeyValue(DBObject parsedDBObject, String key, Object value) {
        putKeyValue(parsedDBObject, key, value, false);
    }

    protected void putKeyValue(DBObject parsedDBObject, String key, Object value, boolean forceOverwrite) {
        if (StringUtils.isBlank(key)) {
            return;
        }
        key = key.trim();
        if (value == null) {
            if (forceOverwrite) {
                parsedDBObject.put(key, value);
            }
            return;
        }

        if (value instanceof String) {
            String str = ObjectUtils.toString(value);
            str = str.trim();
            if (StringUtils.isBlank(str) || "-".equals(str)) {
                if (!forceOverwrite) {
                    return;
                }
            }
            parsedDBObject.put(key, str);
        }

        parsedDBObject.put(key, value);
    }

    /**
     * Through the page content or DOM node may determine if a page for a failed attempt to reacquire the need to resolve , calling this interface to re-inject the current page
     * @param WebPage current page objects
     * @param Message Cause explanatory text
     */
    protected void injectParseFailureRetry(WebPage webPage, String message) {
        crawlService.injectParseFailureRetry(webPage, message);
    }
}
