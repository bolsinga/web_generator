package com.bolsinga.itunes;

import java.util.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

class ParserHandler extends DefaultHandler {
  public Map<String, Object> plist;

  private Stack<AbstractMap.SimpleEntry<String, Object>> mapEntryStack = new Stack<AbstractMap.SimpleEntry<String, Object>>();

  private Stack<String> elementStack = new Stack<String>();

  private Stack<Map<String, Object>> dictStack = new Stack<Map<String, Object>>();

  private Stack<Boolean> parsingArrayStack = new Stack<Boolean>();

  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
    this.elementStack.push(qName);

    if ("array".equals(qName)) {
      if (this.parsingArrayStack.empty()) {
        this.mapEntryStack.pop(); // Remove the key for this array.
      }
      this.parsingArrayStack.push(Boolean.TRUE);
    } else {
      if (this.parsingArrayStack.empty()) {
        if ("plist".equals(qName)) {
          // Nothing
        } else if ("dict".equals(qName)) {
          Map<String, Object> dictionary = new LinkedHashMap<String, Object>();
          this.dictStack.push(dictionary);
        } else if ("key".equals(qName)) {
          // Nothing
        } else {
          if ("data".equals(qName) ||
              "date".equals(qName) ||
              "false".equals(qName) ||
              "integer".equals(qName) ||
              "string".equals(qName) ||
              "true".equals(qName)) {
                // Nothing
          } else {
            throw new SAXException("Unknown element: " + qName);
          }
        }
      }
    }
  }

  public void endElement(String uri, String localName, String qName) throws SAXException {
    this.elementStack.pop();

    if ("array".equals(qName)) {
      this.parsingArrayStack.pop();
    } else {
      if (this.parsingArrayStack.empty()) {
        if ("plist".equals(qName)) {
          plist = dictStack.pop();
        } else if ("dict".equals(qName)) {
          AbstractMap.SimpleEntry<String, Object> currentMapEntry = currentMapEntry();
          if (currentMapEntry != null) {
            if (currentMapEntry.getValue() != null) {
              throw new SAXException("Can't set dictionary value for: " + currentMapEntry.toString());
            }
            Map<String, Object> endedDict = dictStack.pop();
            currentMapEntry.setValue(endedDict);

            // Now pop it
            currentMapEntry = this.mapEntryStack.pop();

            // need to re-access the new current dict
            currentDict().put(currentMapEntry.getKey(), currentMapEntry.getValue());
          } else {
            // A-OK
          }
        } else if ("key".equals(qName)) {
          // Nothing
        } else {
          if ("data".equals(qName) ||
              "date".equals(qName) ||
              "integer".equals(qName) ||
              "string".equals(qName)) {
            AbstractMap.SimpleEntry<String, Object> mapEntry = this.mapEntryStack.pop();
            currentDict().put(mapEntry.getKey(), mapEntry.getValue());
          } else if ("true".equals(qName) || "false".equals(qName)) {
            AbstractMap.SimpleEntry<String, Object> mapEntry = this.mapEntryStack.pop();
            mapEntry.setValue(qName);
            currentDict().put(mapEntry.getKey(), mapEntry.getValue());
          } else {
            throw new SAXException("Unknown element: " + qName);
          }
        }
      }
    }
  }

  public void characters(char[] ch, int start, int length) throws SAXException {
      String value = new String(ch, start, length);

      if (this.parsingArrayStack.empty()) {
        if ("key".equals(elementStack.peek())) {
          AbstractMap.SimpleEntry<String, Object> mapEntry = new AbstractMap.SimpleEntry<String, Object>(value, null);
          this.mapEntryStack.push(mapEntry);
        } else {
          AbstractMap.SimpleEntry<String, Object> currentMapEntry = currentMapEntry();
          String currentValue = (String)currentMapEntry.getValue();
          if (currentValue != null) {
            StringBuffer sb = new StringBuffer(currentValue);
            sb.append(value);
            currentMapEntry.setValue(sb.toString());
          } else {
            currentMapEntry.setValue(value);
          }
        }
      }
  }

  private Map<String, Object> currentDict() {
    if (this.dictStack.empty()) {
      return null;
    } else {
      return this.dictStack.peek();
    }
  }

  private AbstractMap.SimpleEntry<String, Object> currentMapEntry() {
    if (this.mapEntryStack.empty()) {
      return null;
    } else {
      return this.mapEntryStack.peek();
    }
  }
}
