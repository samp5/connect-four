package controller.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Markup {
  // [^\*]\*[^\r\n\t\f\v\* ][^\*]+?[^\r\n\t\f\v\* ]\*[^\*]
  private final static String boldRegex = "\\*\\*\\S.+?\\S\\*\\*";
  private final static String italicRegex = "(?:^|[^\\*])\\*[^\\r\\n\\t\\f\\v\\* ].*?[^\\r\\n\\t\\f\\v\\* ]\\*(?:[^\\*]|$)";

  public static boolean isBold(String s) {
    return Pattern.matches("^" + boldRegex + "$", s);
  }

  public static boolean containsMarkup(String s) {
    return Pattern.matches(".*" + boldRegex + ".*", s) || Pattern.matches(".*" + italicRegex + ".*", s);
  }

  private static Collection<String> splitOn(String s, Pattern p) {
    ArrayList<String> ret = new ArrayList<>();
    Matcher matcher = p.matcher(s);
    int prev = 0;

    while (matcher.find()) {
      String prevText = s.substring(prev, matcher.start());
      String match = matcher.group();

      prev = matcher.end();

      ret.add(prevText);
      ret.add(match);
    }

    ret.add(s.substring(prev));
    return ret;
  }

  public static Collection<String> splitOnMarkup(String s) {
    ArrayList<String> ret = new ArrayList<>();
    var boldList = Markup.splitOn(s, Pattern.compile(boldRegex));
    Pattern italicPat = Pattern.compile(italicRegex);
    for (String el : boldList) {
      ret.addAll(Markup.splitOn(el, italicPat));
    }
    return ret;
  }

  public static boolean isItalic(String s) {
    return Pattern.matches("^" + italicRegex + "$", s);
  }
}
