package io.github.lujian213.eggfund.utils;

import org.openqa.selenium.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;

import javax.xml.xpath.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class LocalWebElement implements WebElement {
    private static XPathFactory fac = XPathFactory.newInstance();
    private static final String XPATH_PREFIX = "By.xpath: ";
    private Element node = null;

    public LocalWebElement(Element element) {
        this.node = element;
    }

    public LocalWebElement(String html) {
        try (StringReader reader = new StringReader(html)) {
            Tidy tidy = new Tidy();
            tidy.setDocType("auto");
            tidy.setXHTML(true);
            Document tidyDOM = tidy.parseDOM(reader, null);
            node = tidyDOM.getDocumentElement();
        }
    }

    public LocalWebElement(File file) throws IOException {
        try (InputStream is = new FileInputStream(file)) {
            Tidy tidy = new Tidy();
            tidy.setDocType("auto");
            tidy.setXHTML(true);
            Document tidyDOM = tidy.parseDOM(is, null);
            node = tidyDOM.getDocumentElement();
        }
    }

    public LocalWebElement(InputStream is) {
        Tidy tidy = new Tidy();
        tidy.setDocType("auto");
        tidy.setXHTML(true);
        Document tidyDOM = tidy.parseDOM(is, null);
        node = tidyDOM.getDocumentElement();
    }

    @Override
    public <X> X getScreenshotAs(OutputType<X> arg0) throws WebDriverException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void click() {
        throw new UnsupportedOperationException();
    }

    @Override
    public WebElement findElement(By by) {
        if (by instanceof By.ByXPath) {
            XPath xPath = fac.newXPath();
            try {
                String xPathStr = by.toString().substring(XPATH_PREFIX.length());
                XPathExpression expr = xPath.compile(xPathStr);
                Element ele = (Element) expr.evaluate(node, XPathConstants.NODE);
                if (ele != null)
                    return new LocalWebElement(ele);
                else
                    return null;
            } catch (XPathExpressionException e) {
                throw new RuntimeException("Bad xpath [" + by + "]");
            }
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public List<WebElement> findElements(By by) {
        if (by instanceof By.ByXPath) {
            XPath xPath = fac.newXPath();
            try {
                String xPathStr = by.toString().substring(XPATH_PREFIX.length());
                XPathExpression expr = xPath.compile(xPathStr);
                NodeList nodeList = (NodeList) expr.evaluate(node, XPathConstants.NODESET);
                List<WebElement> ret = new ArrayList<>();
                for (int i = 1; i <= nodeList.getLength(); i++) {
                    Element ele = (Element) nodeList.item(i - 1);
                    ret.add(new LocalWebElement(ele));
                }
                return ret;
            } catch (XPathExpressionException e) {
                throw new RuntimeException("Bad xpath [" + by + "]");
            }
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public String getAttribute(String attr) {
        return node.getAttribute(attr);
    }

    @Override
    public String getCssValue(String arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Point getLocation() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Rectangle getRect() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Dimension getSize() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getTagName() {
        return node.getTagName();
    }

    @Override
    public String getText() {
        return getNodeText(node);
    }

    @Override
    public boolean isDisplayed() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEnabled() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSelected() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendKeys(CharSequence... arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void submit() {
        throw new UnsupportedOperationException();
    }

    protected static String getNodeText(Element ele) {
        NodeList children = ele.getChildNodes();
        for (int i = 1; i <= children.getLength(); i++) {
            Node node = children.item(i - 1);
            if (node.getNodeType() == Node.TEXT_NODE) {
                return node.getNodeValue();
            }
        }

        return "";
    }
}