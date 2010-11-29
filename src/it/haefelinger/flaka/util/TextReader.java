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
import java.text.ParsePosition;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tools.ant.Project;

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
public class TextReader
{
  protected String comment = ";";
  protected boolean skipempty = true;
  protected boolean continuation = true;
  protected String text;
  protected boolean skipws = false;
  protected String shift;
  protected BufferedReader bufreader;
  protected Project project;
  protected boolean resolve = false;

  public TextReader(String text)
  {
    this.setText(text);
  }

  public TextReader()
  {
    this.setText("");
  }

  public TextReader setProject(Project project)
  {
    this.project = project;
    return this;
  }

  public TextReader setText(String text)
  {

    if (text == null)
      this.text = "";
    else
      this.text = text;
    return this;
  }

  public void addText(String text)
  {
    this.setText(this.text + text);
  }

  public TextReader setSkipws(boolean b)
  {
    this.skipws = b;
    return this;
  }

  public TextReader setShift(String s)
  {
    this.shift = null;
    if (s != null && s.matches("\\s*") == false)
    {
      Pattern P;
      Matcher p;

      P = Pattern.compile("\\s*(\\d+)(.*)");
      p = P.matcher(s);

      if (p.matches())
      {
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

  public String getShift()
  {
    return this.shift == null ? "" : this.shift;
  }

  public TextReader setComment(String comment)
  {
    this.comment = Static.trim2(comment, null);
    return this;
  }

  public TextReader setResolveContLines(boolean b)
  {
    this.continuation = b;
    return this;
  }

  public TextReader setSkipEmpty(boolean b)
  {
    this.skipempty = b;
    return this;
  }

  public String getText()
  {
    return this.text;
  }

  protected boolean ignore(String line)
  {
    return this.skipempty && line.matches("\\s*") ? true : false;
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
  final static public String stripws(String text)
  {
    Pattern S, T, U;
    Matcher s, t, u;
    String out, prefix;
    int n = 0;

    // Compile a regex matching all whitespace starting from the begin of
    // input till the first non-whitespace character. Note, that '\s'
    // matches EOL as well as classical whitespace.
    S = Pattern.compile("^\\s*");

    // Compile a regex matching all characters after the last EOL.
    T = Pattern.compile("\\n([^\\n]*)$");

    // create match object on input and execute RE on it.
    s = S.matcher(text);
    s.find();

    // Apply regex T on the prefix. The length of the match will determine
    // the number of characters to strip off.
    prefix = s.group();
    t = T.matcher(prefix);
    if (t.find())
    {
      n = t.end(1) - t.start(1);
    }
    else
    {
      n = s.end() - s.start();
    }

    // Remove leading whitespace characters.
    //out = s.replaceFirst("");
    out = text.replaceFirst("\\A[ \\t\\f]+\n","\n");
    //out = out.replaceFirst("\n","");
    if (n > 0)
    {
      // Compile a pattern on the fly.
      // Matches a newline followed by {1,n} characters. Such a character
      // must a whitespace character except a newline.
      // Is there a way to express this as a difference operation? Such
      // as {\s - \n} ??
      U = Pattern.compile("(\\n|\\A)[ \\t\\f]{1," + n + "}");
      u = U.matcher(out);
      out = u.replaceAll("\n");
    }
    return out;
  }

  final public static String resolvecontlines2(String text)
  {
    char c;
    int i, j, n;
    StringBuffer b;
    ParsePosition p;

    i = 0;
    n = text.length();
    p = new ParsePosition(0);
    b = new StringBuffer();

    while (i < n)
    {
      // remember where we are and advance until '\' or EOF.
      j = i;
      while (i < n && (c = text.charAt(i)) != '\\')
        ++i;
      // copy
      b.append(text, j, i);
      // bail out if EOF
      if (i >= n)
        break;
      // text[i] == '\'
      j = i;
      p.setIndex(j + 1);
      if (lookahead(text, n, p))
      {
        // we are at the start of a cont line sequence which means, that
        // parse position is either at EOF or EOL. We skip current '\'
        // and copy any remaining '\'.
        i = p.getIndex();
        b.append(text, j + 1, i);
        // skip eol or eof
        if (i >= n)
          continue;
        c = text.charAt(i);
        // ignore EOL
        if (c == '\n' || c == '\r')
        {
          i = i + 1;
          if (c == '\r' && i < n && text.charAt(i) == '\n')
            i = i + 1;
        }
        // ignore whitespace after EOL
        while (i < n && TextReader.isspace(text, i))
          ++i;
      }
      else
      {
        i = p.getIndex();
        b.append(text, j, i);
      }
    }
    return b.toString();
  }

  /**
   * A helper function to define what <em>whitespace</em> means in this context.
   * 
   * @param c
   * @return
   */
  static public final boolean isspace(String text, int i)
  {
    char c = text.charAt(i);
    return (c == ' ' || c == '\t' || c == '\f') ? true : false;
  }

  /**
   * A function to lookahead the end of a continuation line.
   * 
   * @param text
   * @param n
   * @param p
   * @return true, if the looked ahead sequence is
   */
  final static public boolean lookahead(String text, int n, ParsePosition p)
  {
    int i, j, d;
    char c;

    // advance over all '\'
    i = p.getIndex();
    j = i;

    // Preconditon: text[i-1] == '\'
    if (i > n)
    {
      return true;
    }

    while (i < n && text.charAt(i) == '\\')
      i++;

    d = i - j;
    p.setIndex(i);

    // Handle EOF
    if (i >= n)
    {
      return d % 2 == 0;
    }

    // Handle EOL
    c = text.charAt(i);
    if (c == '\n' || c == '\r')
    {
      return (d % 2) == 0;
    }

    // not a cont line
    p.setIndex(i);
    return false;
  }

  final public static BufferedReader tobufreader(String text)
  {
    return new BufferedReader(new StringReader(text));
  }

  final public static String stripcomment(String comment, String text)
  {
    int i, j, n;
    String pat = String.format("^[ \t\f]*%s.*$", comment);
    Pattern S = Pattern.compile(pat, Pattern.MULTILINE);
    Matcher m = S.matcher(text);
    StringBuffer b;
    i = 0;
    b = new StringBuffer();
    n = text.length();
    while (i < n && m.find(i))
    {
      j = m.start();
      b.append(text, i, j);
      i = m.end() + 1;
    }
    if (i <= n)
      b.append(text, i, n);
    return b.toString();
  }

  protected void init()
  {
    if (this.bufreader == null)
    {
      // strip comments
      this.text = TextReader.stripcomment(this.comment, this.text);

      // if skipws is on, strip out unwanted whitespace stuff.
      if (this.skipws)
        this.text = TextReader.stripws(this.text);
 
      // if resolve continuation lines is on, merge continuation lines
      if (this.continuation)
        this.text = TextReader.resolvecontlines2(this.text);

      /* initialize buffered reader */
      this.bufreader = TextReader.tobufreader(this.text);
    }
  }

  /*
   * Read the next line. Ignores comment lines and empty lines if desired.
   * 
   * @see java.io.BufferedReader#readLine()
   */
  public String readLine()
  {
    String line;

    init();
    try
    {
      while ((line = this.bufreader.readLine()) != null && this.ignore(line))
      {
        // read another line
      }
      if (line != null)
      {
        /* resolve all Ant properties ${ } */
        line = this.project.replaceProperties(line);

        /* resolve all EL references #{ ..} */
        line = Static.elresolve(this.project, line);

        if (line != null && this.shift != null)
          line = this.shift + line;
      }
    }
    catch (IOException e)
    {
      line = null;
    }

    return line;
  }

  public String read()
  {
    String r;
    final char[] buf = new char[1024];
    final StringBuffer b = new StringBuffer();
    int n;
    
    init();
    try
    {
      while ((n = this.bufreader.read(buf)) >= 0)
      {
        b.append(buf, 0, n);
      }
    }
    catch (Exception e)
    {
      /* can't happen */
    }
    r = b.toString();
    // trim trailing whitespace
    r = r.replaceAll("[ \\t\\f]+\\z","");
    // trim leading newline
    r = r.replaceFirst("\\A\n","");
    if (this.shift!=null) {
      // replace all but last \n ..
      Matcher m = Pattern.compile("\n\\z").matcher(r);
      if (m.find()) {
        r = r.substring(0,m.start());
        r = r.replaceAll("\n","\n" + this.shift);
        r = r + "\n";
      } else {
        r = r.replaceAll("\n","\n" + this.shift);
      }
      r = this.shift + r;
    }
    /* resolve all Ant properties ${ } */
    r = this.project.replaceProperties(r);

    /* resolve all EL references #{ ..} */
    r = Static.elresolve(this.project, r);
    return r;
  }

  /**
   * Unescape escaped characters.
   * 
   * This function replaces characters escaped by backslash with the character
   * itself. Thus
   * 
   * <pre>
   * \c  becomes c
   * \\  becomes \
   * \\c becomes \c
   * 
   * <pre>
   * &#064;param text
   * @return
   */
  static public String unescape(String text)
  {
    String r = null;
    int n;

    n = text.length();
    switch (n)
    {
      case 0:
        r = text;
        break;
      default:
      {
        StringBuffer b;
        char c;

        b = new StringBuffer();
        for (int i = 0; i < n; ++i)
        {
          c = text.charAt(i);
          if (c != '\\' || i + 1 > n)
            b.append(c);
          else
          {
            char p;
            p = text.charAt(i + 1); // look ahead
            // handle '\?'
            b.append(p);
            i = i + 1;
          }
        }
        r = b.toString();
      }
    }
    return r;
  }

}
