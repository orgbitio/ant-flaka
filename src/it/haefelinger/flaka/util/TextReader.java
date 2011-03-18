/*
 * Copyright (c) 2009 Haefelinger IT 
 *
 * Licensed  under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required  by  applicable  law  or  agreed  to in writing, 
 * software distributed under the License is distributed on an "AS 
 * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied.
 
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */

package it.haefelinger.flaka.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class to read and massage text.
 * 
 * Text read from an element needs to undergo certain changes before
 * it can be further processed. Typical changes are:
 * <ol>
 * <li>remove comment lines
 * <li>merge continuation lines
 * <li>ignore empty lines
 * <li>strip unwanted leading whitespace 
 * </ol>
 * This class shall handle this. Each of the changes can be turned on
 * individually. By default, no changes are done.
 * 
 * However, even if no changes are applied, the input text may differ
 * from the output. That is because the output is accumulated by 
 * reading line by line. When reading a line, newline terminators are
 * honoured but eventually replaced by a uniform "\n". If the input 
 * ends with a newline, that newline is missing. This <it>feature</it>
 * has its routes in the underlying Reader class. That class allows to 
 * read line by line but whatever the newline character is, it's just
 * swallowed by that class.
 * 
 * After having used readLine() or read(), this object becomes dirty,
 * i.e. setting attributes may or may not taken into account. Setting
 * the following attributes are known to work:
 * <ol>
 * <li>setSkipEmpty</li>
 * <li>setComment</li>
 * </ol>
 * 
 * @author geronimo
 *
 */
/**
 * @author geronimo
 * 
 */
public class TextReader {
  static protected HashMap TheCache;
  protected String cs = ";";
  protected String ics = ";";
  protected boolean skipempty = true;
  protected boolean cl = true;
  protected String text;
  protected boolean ws = false;
  protected String shift;
  protected BufferedReader bufreader;

  static {
    TheCache = new HashMap();
  }

  public TextReader(String text) {
    this.setText(text);
  }

  public TextReader() {
    this.setText("");
  }

  public TextReader setText(String text) {
    this.text = text == null ? "" : text;
    return this;
  }

  public TextReader setSkipws(boolean b) {
    this.ws = b;
    return this;
  }

  public TextReader setShift(String s) {
    this.shift = null;
    if (s != null && s.matches("\\s*") == false) {
      Pattern P;
      Matcher p;

      P = makeregex("\\s*(\\d+)(.*)");
      p = P.matcher(s);

      if (p.matches()) {
        int times = Integer.parseInt(p.group(1));
        String what = p.group(2);
        StringBuilder accu = new StringBuilder();
        if (what.length() < 1)
          what = " ";
        for (int i = 0; i < times; ++i)
          accu.append(what);
        this.shift = accu.toString();
      }
    }
    return this;
  }

  public String getShift() {
    return this.shift == null ? "" : this.shift;
  }

  public String getText() {
    return this.text;
  }

  public TextReader setCL(String s) {
    String t = s.trim();
    if (t.matches("\\s*") == false) {
      this.cs = t;
    } else {
      this.cs = null;
    }
    return this;
  }

  public TextReader setIC(String s) {
    String t = s.trim();
    if (t.matches("\\s*") == false) {
      this.ics = t;
    } else {
      this.ics = null;
    }
    return this;
  }

  public TextReader setResolveContLines(boolean b) {
    this.cl = b;
    return this;
  }

  public TextReader setSkipEmpty(boolean b) {
    this.skipempty = b;
    return this;
  }

  protected boolean ignore(String line) {
    return this.skipempty && line.matches("\\s*") ? true : false;
  }

  static final protected Pattern makeregex(String regex) {
    Pattern P;
    P = (Pattern) TheCache.get(regex);
    if (P == null) {
      P = Pattern.compile(regex);
      TheCache.put(regex, P);
      P = (Pattern) TheCache.get(regex);
    }
    return P;
  }

  /**
   * A helper method to strip unwanted whitespace from an input text.
   * 
   * This method is expected to be called on a input text consiting of multiple
   * lines.
   * 
   * @param text
   * @return not null
   */
  final static public String stripws(String text) {
    Pattern P;
    Matcher m;
    String t;
    int e, n;

    // Compile a regex matching all whitespace starting from the begin of
    // input till the first non-whitespace character. Note, that '\s'
    // matches EOL as well as classical whitespace.

    P = makeregex("^[ \\t\\f]*(?:\\n|\\r\\n)(\\s*)");

    // Create matcher object, then test whether basic regex matches. If not
    // then there is no \eol between begin-of-input and first non-ws char -
    // in which case we do not strip any ws on purpose.
    m = P.matcher(text);
    if (m.find() == false) {
      return text;
    }

    // Calculate the number of ws characters between first non-ws character
    // and \eol before that character. Such a character must exist, other-
    // wise regex 'P' could not have matched.

    e = m.end();
    n = 0;
    while (e > 0) {
      switch (text.charAt(--e)) {
      case '\n':
        break;
      default:
        n = n + 1;
      }
    }

    // Remove initial ws including \eol.
    t = m.replaceFirst("$1");

    if (n > 0) {
      // Compile a pattern on the fly that matches a newline followed by
      // at most n whitespace characters (but newline).
      P = makeregex("(?m)(^|\\A)[ \\t\\f]{1," + n + "}");
      m = P.matcher(t);
      t = m.replaceAll("");
    }

    // Eventually remove trailing whitespace
    // Warning: do not use '$' to denote ONLY the end of input - this can
    // be tricky. Cause '$' also matches the *last* \eol before \eof. Better
    // to use \z cause always means \eof. See also unit cases for further
    // details.
    P = makeregex("\\n[ \\t\\f]*\\z");
    m = P.matcher(t);
    t = m.replaceAll("");
    return t;
  }

  final public static String resolvecontlines1(String text) {
    String t;
    Pattern P1, P2;
    Matcher M1, M2;
    P1 = makeregex("(?m)([^\\\\]|\\A)\\\\(\\n[ \\t|\\f]*|\\z)");
    P2 = makeregex("(?m)\\\\\\\\(\\n|\\z)");
    M1 = P1.matcher(text);
    t = M1.replaceAll("$1");
    M2 = P2.matcher(t);
    t = M2.replaceAll("\\\\$1");
    return t;
  }

  final protected String stripcomments() {
    String s;
    String p;
    Pattern S;
    Matcher m;

    s = this.text;
    if (this.cs != null) {
      p = String.format("(?m)^[ \\t\\f]*%s.*(\\r|\\r\\n|\\n|\\z)",
          Pattern.quote(this.cs));
      S = makeregex(p);
      m = S.matcher(this.text);
      s = m.replaceAll("");
    }

    if (this.ics != null) {
      // A naive approach to handle ';' as the begin of a comment. Test
      // '\;' and '\\;' ..
      // Step 1: remove all unescaped inline comments till \eol
      p = String.format("(?m)([^\\\\])%s.*$", Pattern.quote(this.ics));
      S = makeregex(p);
      m = S.matcher(s);
      s = m.replaceAll("$1");
      // Step 2: handle escaped inline comment sequence, i.e. turn
      // '\;' into ';'. Ignore on purpose any escape sequence before
      // this escaped sequence.
      p = String.format("(?m)\\\\(%s.*)$", Pattern.quote(this.ics));
      S = makeregex(p);
      m = S.matcher(s);
      s = m.replaceAll("$1");
    }
    return s;
  }

  protected void init() {
    if (this.bufreader == null) {
      // if resolve cl lines is on, merge cl lines
      if (this.cl)
        this.text = TextReader.resolvecontlines1(this.text);

      // strip comments
      this.text = stripcomments();

      // if ws is on, strip out unwanted whitespace stuff.
      if (this.ws)
        this.text = TextReader.stripws(this.text);

      /* initialize buffered reader */
      this.bufreader = new BufferedReader(new StringReader(this.text));
    }
  }

  /*
   * Read the next line. Ignores comment lines and empty lines if desired.
   * 
   * @see java.io.BufferedReader#readLine()
   */
  public String readLine() {
    String line;

    init();
    try {
      line = this.bufreader.readLine();
      while (line != null && this.ignore(line)) {
        line = this.bufreader.readLine();
      }
    } catch (IOException e) {
      line = null;
    }
    if (line != null && this.shift != null) {
      line = this.shift + line;
    }
    return line;
  }

  public String read() {
    String r;
    final char[] buf = new char[1024];
    final StringBuilder b = new StringBuilder();
    int n;

    init();
    try {
      while ((n = this.bufreader.read(buf)) >= 0) {
        b.append(buf, 0, n);
      }
    } catch (Exception e) {
      /* can't happen */
    }
    r = b.toString();
    if (this.shift != null) {
      // replace all but last \n ..
      Matcher m = makeregex("\n\\z").matcher(r);
      if (m.find()) {
        r = r.substring(0, m.start());
        r = r.replaceAll("\n", "\n" + this.shift);
        r = r + "\n";
      } else {
        r = r.replaceAll("\n", "\n" + this.shift);
      }
      r = this.shift + r;
    }
    return r;
  }

}
