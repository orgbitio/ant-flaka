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
  protected Pattern comment = makepattern(";");
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
  public TextReader setProject(Project project) {
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
    if (s != null && s.matches("\\s*")==false)
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
  
  public TextReader setComment(String comment)
  {
    this.comment = makepattern(Static.trim2(comment,null));
    return this;
  }
  public TextReader setComment(Pattern comment)
  {
    this.comment = comment;
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

  public String getText() {
    return this.text;
  }
   /**
   * A small helper function generating a regular expression pattern.
   * 
   * The pattern generated is meant to define the begin of a comment
   * line. When using this pattern, use the Pattern.search() method
   * instead of match.
   * 
   * The pattern holds essentially the following regular expression
   * <pre>
   * ^\s*\Q{s}\E
   * </pre>
   * where {s} denotes parameter <code>s</code> and where <code>\Q</code>
   * means: treat upcoming chars literally until reaching <code>\E</code>.
   * In other words, meta regular expression characters like <code>*</code>
   * and the like are not honoured.
   * 
   * @param s null allowed
   * @return null if s is null, otherwise proper RE pattern.
   */
  static public Pattern makepattern(String s)
  {
    Pattern P;
    
    if (s == null)
      P = null;
    else 
    {
      try
      {
        String p;
        p = "^\\s*" + Pattern.quote(s);
        P = Pattern.compile(p);
      } catch (Exception e)
      {
        /* This can't happen cause the RE above is valid 
         * regardless of s's value.
         */
        P = null;
      }
    }
    return P;
  }

  /**
   * A method checking whether a input line shall be ignored or not.
   * 
   * The given line is expected to hold any character but newline terminators.
   * Such a line is typically given using the readLine() method of a Reader
   * class. 
   * 
   * A line is ignored if 
   * <ol>
   * <li>being a comment line</li>
   * <li>empty</li>
   * </ol>
   * However, checks wheather a comment line are only executed if a comment
   * pattern has been installed. Empty lines are removed if attribute skipempty
   * has been set to true.
   * 
   * @param line not null
   * @return true if line to be ignored
   */
  protected boolean ignore(String line)
  {
    if (this.comment != null && this.comment.matcher(line).find()) 
        return true;
    if (this.skipempty && line.matches("\\s*"))
      return true;
    return false;
  }


  
  /**
   * A helper method to strip unwanted whitespace from an input text.
   * 
   * This method is expected to be called on a input text consiting of
   * multiple lines.
   * 
   * @param text
   * @return
   */
  final static public String stripws(String text)
  {
    Pattern S,T,U;
    Matcher s,t,u;
    String out,prefix;
    int n = 0;
    // Match all ws at input's begin (includes \n)
    // TODO: make me static
    S = Pattern.compile("^\\s*");
    // Match everthing after last \n (which must exist)
    // TODO: make me static
    T = Pattern.compile("\\n([^\\n]*)$");
    
    // create match object on input and execute RE on it.
    s = S.matcher(text);
    s.find();

    // The matching sequence - must always find something. 
    // TODO: Possible to avoid taking a substring??
    prefix = s.group();
    t = T.matcher(prefix);
    if (t.find()) {
      n = t.end(1) - t.start(1);
    }
    else
    {
      n = s.end() - s.start();
    } 
      
    // Remove leading whitespace characters.
    out = s.replaceFirst("");
    if (n>0)
    {
      // Compile a pattern on the fly. 
      // Matches a newline followed by {1,n} characters. Such a character
      // must a whitespace character except a newline. 
      // Is there a way to express this as a difference operation? Such
      // as {\s - \n} ??
      U = Pattern.compile("\\n[ \\t\\x0B\\f\\r]{1,"+n+"}");
      u  = U.matcher(out);
      out = u.replaceAll("\n");
    }
    return out;
  } 
  
  /**
   * A helper function to resolve continuation lines.
   * 
   * @param text
   * @return
   */
  final public static String resolvecontlines(String text)
  {
    // TODO: move S into static 
    Pattern S = Pattern.compile("(^|[^\\\\])(\\\\\\n|\\\\$)");
    Matcher s = S.matcher(text);
    String out = s.replaceAll("");
    return out;
  }
  
 
  final public static BufferedReader tobufreader(String text)
  {
    return new BufferedReader(new StringReader(text));
  }
  
  
  
  
  
  /*
   * Read the next line. Ignores comment lines and empty lines if desired.
   * 
   * @see java.io.BufferedReader#readLine()
   */
  /**
   * @return
   */
  public String readLine()
  {
    String line;
    if (this.bufreader == null)
    {
      /* massage */
      // if skipws is on, strip out unwanted whitespace stuff.
      if (this.skipws)
        this.text = TextReader.stripws(this.text);
      
      // if resolve continuation lines is on, merge continuation lines 
      if (this.continuation)
        this.text = TextReader.resolvecontlines(this.text);
             
      /* initialize buffered reader */
      this.bufreader = TextReader.tobufreader(this.text);
    }
    try {
      while ( (line = this.bufreader.readLine())!=null && this.ignore(line)) {
        // read another line
      }
      if (line != null)
      {
        /* resolve all Ant properties ${ } */
        line = this.project.replaceProperties(line);
      
        /* resolve all EL references #{ ..} */
        line = Static.elresolve(this.project,line);
      
        if (line != null && this.shift != null)
          line = this.shift + line;
      }
    }
    catch(IOException e)
    {
      line = null;
    }
    
    return line;
  }

  public String read() 
  {
    String line;
    String accu = null;
    
    while ((line = this.readLine())!=null)
    {
      if (accu == null)
        accu = line;
      else {
        accu += "\n";
        accu += line;
      }
    }
    return accu;
  }
  
}
