package com.bolsinga.site;

import java.util.*;
import java.util.regex.*;

// Looks for markup in the HTML text of the data that pages will show wrongly, and returns a warning
// for each. Warnings don't stop the run.
//  - A tag that isn't in HTML 4.01 Strict, which the pages declare. Text such as Set<String> in a
//    code sample is read as a tag and disappears, and <strike> is only in HTML 4.01 Transitional.
//  - An href or src that starts or ends with whitespace.
class MarkupCheck {
  // HTML 4.01 Strict elements.
  private static final Set<String> STRICT = new HashSet<String>(Arrays.asList(
    "a", "abbr", "acronym", "address", "area", "b", "base", "bdo", "big", "blockquote", "body", "br",
    "button", "caption", "cite", "code", "col", "colgroup", "dd", "del", "dfn", "div", "dl", "dt",
    "em", "fieldset", "form", "h1", "h2", "h3", "h4", "h5", "h6", "head", "hr", "html", "i", "img",
    "input", "ins", "kbd", "label", "legend", "li", "link", "map", "meta", "noscript", "object", "ol",
    "optgroup", "option", "p", "param", "pre", "q", "samp", "script", "select", "small", "span",
    "strong", "style", "sub", "sup", "table", "tbody", "td", "textarea", "tfoot", "th", "thead",
    "title", "tr", "tt", "ul", "var"));

  // HTML 4.01 Transitional elements that Strict leaves out, and what to use instead.
  private static final Map<String, String> TRANSITIONAL = new HashMap<String, String>();
  static {
    TRANSITIONAL.put("strike", "<del>");
    TRANSITIONAL.put("s", "<del>");
    TRANSITIONAL.put("u", "<ins> or CSS");
    TRANSITIONAL.put("center", "CSS");
    TRANSITIONAL.put("font", "CSS");
    TRANSITIONAL.put("basefont", "CSS");
    TRANSITIONAL.put("applet", "<object>");
    TRANSITIONAL.put("iframe", "<object> or a link");
    TRANSITIONAL.put("dir", "<ul>");
    TRANSITIONAL.put("menu", "<ul>");
    TRANSITIONAL.put("isindex", "<form>");
  }

  private static final Pattern TAG = Pattern.compile("</?([A-Za-z][A-Za-z0-9]*)[^>]*>");
  private static final Pattern URL = Pattern.compile("\\b(href|src)\\s*=\\s*([\"'])([^\"']*)\\2", Pattern.CASE_INSENSITIVE);

  static List<String> warnings(final com.bolsinga.music.data.Music music, final com.bolsinga.diary.data.Diary diary) {
    List<String> warnings = new ArrayList<String>();

    for (com.bolsinga.music.data.Show show : music.getShows()) {
      MarkupCheck.check(show.getID(), show.getComment(), warnings);
    }
    for (com.bolsinga.diary.data.Entry entry : diary.getEntries()) {
      MarkupCheck.check(entry.getID() + " title", entry.getTitle(), warnings);
      MarkupCheck.check(entry.getID(), entry.getComment(), warnings);
    }
    MarkupCheck.check("diary statics", diary.getStatic(), warnings);
    MarkupCheck.check("diary header", diary.getHeader(), warnings);
    MarkupCheck.check("diary friends", diary.getFriends(), warnings);
    MarkupCheck.check("diary colophon", diary.getColophon(), warnings);

    return warnings;
  }

  private static void check(final String where, final List<String> texts, final List<String> warnings) {
    if (texts == null) {
      return;
    }
    for (String text : texts) {
      MarkupCheck.check(where, text, warnings);
    }
  }

  private static void check(final String where, final String text, final List<String> warnings) {
    if (text == null) {
      return;
    }

    // One warning per tag name in each text.
    Set<String> seen = new HashSet<String>();
    Matcher m = TAG.matcher(text);
    while (m.find()) {
      String name = m.group(1).toLowerCase();
      if (STRICT.contains(name) || !seen.add(name)) {
        continue;
      }
      StringBuilder sb = new StringBuilder();
      sb.append(where);
      sb.append(": <");
      sb.append(m.group(1));
      sb.append(">");
      String instead = TRANSITIONAL.get(name);
      if (instead != null) {
        sb.append(" isn't in HTML 4.01 Strict; use ");
        sb.append(instead);
      } else {
        sb.append(" isn't an HTML tag, so it won't show; write &lt;");
        sb.append(m.group(1));
        sb.append("&gt; to show it as text");
      }
      warnings.add(sb.toString());
    }

    m = URL.matcher(text);
    while (m.find()) {
      String url = m.group(3);
      if (!url.equals(url.strip())) {
        StringBuilder sb = new StringBuilder();
        sb.append(where);
        sb.append(": ");
        sb.append(m.group(1));
        sb.append(" has whitespace around ");
        sb.append(url.strip());
        warnings.add(sb.toString());
      }
    }
  }
}
