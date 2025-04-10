package controller.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.gluonhq.emoji.Emoji;
import com.gluonhq.emoji.EmojiData;
import com.gluonhq.emoji.util.TextUtils;

import javafx.scene.Node;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class Markup {
  // [^\*]\*[^\r\n\t\f\v\* ][^\*]+?[^\r\n\t\f\v\* ]\*[^\*]
  private final static String boldRegex = "\\*\\*\\S.+?\\S\\*\\*";
  private final static String italicRegex =
      "(?:^|[^\\*])\\*[^\\r\\n\\t\\f\\v\\* ].*?[^\\r\\n\\t\\f\\v\\* ]\\*(?:[^\\*]|$)";

  public static boolean isBold(String s) {
    return Pattern.matches("^" + boldRegex + "$", s);
  }

  public static boolean containsMarkup(String s) {
    return Pattern.matches(".*" + boldRegex + ".*", s)
        || Pattern.matches(".*" + italicRegex + ".*", s);
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

  public static List<Node> markup(String s, Font font) {

    String unicodeText = createUnicodeText(s);

    List<Node> flowNodes =
        TextUtils.convertToTextAndImageNodes(unicodeText, font.getSize() + font.getSize() / 4);

    ArrayList<Node> splitFlowNodes = new ArrayList<>();

    flowNodes.stream().forEach(n -> {
      if (Text.class.isInstance(n) && Markup.containsMarkup(((Text) n).getText())) {
        splitFlowNodes.addAll(Markup.splitOnMarkup(((Text) n).getText()).stream().map((str) -> {
          Text t = new Text(str);
          return t;
        }).collect(Collectors.toList()));
      } else {
        splitFlowNodes.add(n);
      }
    });

    splitFlowNodes.stream().filter(Text.class::isInstance).forEach(n -> {
      Text t = (Text) n;
      if (Markup.isBold(t.getText().strip())) {
        t.setText(" " + t.getText().substring(2, t.getText().length() - 2) + " ");
        t.setFont(Font.font(font.getFamily(), FontWeight.BOLD, font.getSize()));
      } else if (Markup.isItalic(t.getText().strip())) {
        t.setText(" " + t.getText().substring(2, t.getText().length() - 2) + " ");
        t.setFont(Font.font(font.getFamily(), FontPosture.ITALIC, font.getSize()));
      } else {
        t.setFont(font);
      }
    });

    return splitFlowNodes;

  }

  private static String createUnicodeText(String nv) {
    StringBuilder unicodeText = new StringBuilder();
    String[] words = nv.split(" ");
    for (String word : words) {
      if (word.length() > 2 && word.charAt(word.length() - 1) == ':' && word.charAt(0) == ':') {
        Optional<Emoji> optionalEmoji =
            EmojiData.emojiFromShortName(word.substring(1, word.length() - 1));
        unicodeText.append(optionalEmoji.isPresent() ? optionalEmoji.get().character() : word);
        unicodeText.append(" ");
      } else {
        unicodeText.append(word);
        unicodeText.append(" ");
      }
    }
    return unicodeText.toString();
  }
}
