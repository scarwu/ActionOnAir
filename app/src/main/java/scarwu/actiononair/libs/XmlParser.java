/**
 * XML Parser
 *
 * @package     Action on Air
 * @author      ScarWu
 * @copyright   Copyright (c) 2017, ScarWu (http://scar.simcz.tw/)
 * @link        http://github.com/scarwu/ActionOnAir
 */

package scarwu.actiononair.libs;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class XmlParser {

    public static final XmlParser NULL_ELEMENT = new XmlParser();

    private static final String TAG = "AoA-" + XmlParser.class.getSimpleName();

    private String mTagName;

    private String mValue;

    private LinkedList<XmlParser> mChildElements;

    private Map<String, String> mAttributes;

    private XmlParser mParentElement;

    /**
     * Constructor. Creates new empty element.
     */
    public XmlParser() {
        mParentElement = null;
        mChildElements = new LinkedList<XmlParser>();
        mAttributes = new HashMap<String, String>();
        mValue = "";
    }

    private void setTagName(String name) {
        mTagName = name;
    }

    /**
     * Returns the tag name of this XML element.
     * 
     * @return tag name
     */
    public String getTagName() {
        return mTagName;
    }

    private void setValue(String value) {
        mValue = value;
    }

    /**
     * Returns the content value of this XML element.
     * 
     * @return content value
     */
    public String getValue() {
        return mValue;
    }

    /**
     * Returns the content value of this XML element as integer.
     * 
     * @param defaultValue returned value if this content value cannot be
     *            converted into integer.
     * @return integer value of this content or default value indicated by the
     *         parameter.
     */
    public int getIntValue(int defaultValue) {
        if (mValue == null) {
            return defaultValue;
        } else {
            try {
                return Integer.valueOf(mValue);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
    }

    private void putAttribute(String name, String value) {
        mAttributes.put(name, value);
    }

    /**
     * Returns a value of attribute in this XML element.
     * 
     * @param name attribute name
     * @param defaultValue returned value if a value of the attribute is not
     *            found.
     * @return a value of the attribute or the default value
     */
    public String getAttribute(String name, String defaultValue) {
        String ret = mAttributes.get(name);
        if (ret == null) {
            ret = defaultValue;
        }
        return ret;
    }

    /**
     * Returns a value of attribute in this XML element as integer.
     * 
     * @param name attribute name
     * @param defaultValue returned value if a value of the attribute is not
     *            found.
     * @return a value of the attribute or the default value
     */
    public int getIntAttribute(String name, int defaultValue) {
        String attrValue = mAttributes.get(name);
        if (attrValue == null) {
            return defaultValue;
        } else {
            try {
                return Integer.valueOf(attrValue);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
    }

    private void putChild(XmlParser childItem) {
        mChildElements.add(childItem);
        childItem.setParent(this);
    }

    /**
     * Returns a child XML element. If a child element is not found, returns an
     * empty element instead of null.
     * 
     * @param name name of child element
     * @return an element
     */
    public XmlParser findChild(String name) {

        for (final XmlParser child : mChildElements) {
            if (child.getTagName().equals(name)) {
                return child;
            }
        }
        return NULL_ELEMENT;
    }

    /**
     * Returns a list of child elements. If there is no child element, returns a
     * empty list instead of null.
     * 
     * @param name name of child element
     * @return a list of child elements
     */
    public List<XmlParser> findChildren(String name) {
        final List<XmlParser> tagItemList = new ArrayList<XmlParser>();
        for (final XmlParser child : mChildElements) {
            if (child.getTagName().equals(name)) {
                tagItemList.add(child);
            }
        }
        return tagItemList;
    }

    /**
     * Returns the parent element of this one.
     * 
     * @return the parent element.
     */
    public XmlParser getParent() {
        return mParentElement;
    }

    private void setParent(XmlParser parent) {
        mParentElement = parent;
    }

    /**
     * Checks to see whether this element is empty.
     * 
     * @return true if empty, false otherwise.
     */
    public boolean isEmpty() {
        return (mTagName == null);
    }

    /**
     * Parses XML data and returns the root element.
     * 
     * @param xmlPullParser parser
     * @return root element
     */
    public static XmlParser parse(XmlPullParser xmlPullParser) {

        XmlParser rootElement = XmlParser.NULL_ELEMENT;
        try {
            XmlParser parsingElement = XmlParser.NULL_ELEMENT;
            MAINLOOP: while (true) {
                switch (xmlPullParser.next()) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        final XmlParser childItem = new XmlParser();
                        childItem.setTagName(xmlPullParser.getName());
                        if (parsingElement == XmlParser.NULL_ELEMENT) {
                            rootElement = childItem;
                        } else {
                            parsingElement.putChild(childItem);
                        }
                        parsingElement = childItem;

                        // Set Attribute
                        for (int i = 0; i < xmlPullParser.getAttributeCount(); i++) {
                            parsingElement.putAttribute(xmlPullParser.getAttributeName(i),
                                    xmlPullParser
                                            .getAttributeValue(i));
                        }
                        break;
                    case XmlPullParser.TEXT:
                        parsingElement.setValue(xmlPullParser.getText());
                        break;
                    case XmlPullParser.END_TAG:
                        parsingElement = parsingElement.getParent();
                        break;
                    case XmlPullParser.END_DOCUMENT:
                        break MAINLOOP;
                    default:
                        break MAINLOOP;
                }
            }
        } catch (final XmlPullParserException e) {
            Log.e(TAG, "parseXml: XmlPullParserException.");
            rootElement = XmlParser.NULL_ELEMENT;
        } catch (final IOException e) {
            Log.e(TAG, "parseXml: IOException.");
            rootElement = XmlParser.NULL_ELEMENT;
        }
        return rootElement;
    }

    /**
     * Parses XML data and returns the root element.
     * 
     * @param xmlStr XML data
     * @return root element
     */
    public static XmlParser parse(String xmlStr) {
        if (xmlStr == null) {
            throw new NullPointerException("parseXml: input is null.");
        }
        try {
            XmlPullParser xmlPullParser = Xml.newPullParser();
            xmlPullParser.setInput(new StringReader(xmlStr));
            return parse(xmlPullParser);
        } catch (final XmlPullParserException e) {
            Log.e(TAG, "parseXml: XmlPullParserException occured.");
            return XmlParser.NULL_ELEMENT;
        }
    }
}
