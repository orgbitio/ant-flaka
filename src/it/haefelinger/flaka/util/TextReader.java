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
import java.io.Reader;
import java.io.StringReader;
import java.util.regex.Pattern;

public class TextReader extends BufferedReader
{
  protected Pattern comment;
  public boolean skipempty = true;
  public int lineno = 0;
  public boolean continuation = true;

  public TextReader(Reader reader)
  {
    super(reader);
    setComment(";");
  }

  public TextReader(String text)
  {
    this(new StringReader(text));
  }

  public TextReader setComment(String comment)
  {
    makeregex(Static.trim2(comment, ";"));
    return this;
  }

  public TextReader setContinuation(boolean b)
  {
    this.continuation = b;
    return this;
  }

  public TextReader setSkipEmpty(boolean b)
  {
    this.skipempty = b;
    return this;
  }

  protected void makeregex(String s)
  {
    try
    {
      String regex;
      regex = "^\\s*" + Pattern.quote(s);
      this.comment = Pattern.compile(regex);
    } catch (Exception e)
    {
      /* TODO: error */
      // this.debug("error compiling regex '"+s+"'", e);
    }
  }

  /**
   * Ignore this line if 
   * (a) empty (contains ws only)
   * (b) matches a comment line
   * @param line not null
   * @return true if line to be ignored
   */
  protected boolean ignore(String line)
  {
    if (this.comment != null && this.comment.matcher(line).find()) 
        return true;
    if (line.matches("\\s*"))
      return true;
    return false;
  }

  /**
   * Read next line from underlying stream, skipping (continued) comment lines
   */
  public String next()
  {
    String line;
    try
    {
      line = super.readLine();
      this.lineno += 1;
      /* comment ? */
      if (line != null && ignore(line))
      {
        /* ignore this lines */
        while (line != null && line.endsWith("\\") && !line.endsWith("\\\\"))
        {
          line = super.readLine();
          this.lineno += 1;
        }
        /* move on (save to call if EOF already seen?? => yes) */
        line = super.readLine();
        this.lineno += 1;
      }
    } catch (IOException ioe)
    {
      // TODO: send debug message
      line = null;
    }
    return line;
  }

  protected String _read_() throws IOException {
    // Notice that readLine() returns null if EOF has been reached. It
    // does not throw an IO exception being called on a stream having
    // seen EOF.
    String line = super.readLine();
    if (line != null)
      this.lineno += 1;
    return line;
  }
  
  /**
   * Read next line from underlying stream. This method 
   * is the essential low level method which supporting continuation 
   * lines. 
   * Notice that neither empty lines nor comments are ignored. This
   * must be done by the callee. 
   */
  protected String _next_()
  {
    String accu = null;
    try
    {
      String line = _read_();
      while (line != null && line.endsWith("\\") && !line.endsWith("\\\\"))
      {
        if (accu == null)
          accu = "";
        accu += line.substring(0, line.length() - 1);
        // Special case
        // "\ EOF"  , "\ NL EOF"
        // reading next line throws IO exception. In that case do not throw
        // away the accu.
        line = _read_();
      }
      if (line != null) {
        if (accu == null)
          accu = "";
        accu += line;
      }
    } catch (IOException ioe)
    {
      // TODO: send debug message
      /* do nothing here */
    }
    return accu;
  }
  
  
  
  /*
   * Read the next line. Ignores comment lines and empty lines if desired.
   * 
   * @see java.io.BufferedReader#readLine()
   */
  public String readLine()
  {
    String line;

    line = _next_();
    while (line != null && ignore(line))
    {
      line = _next_();
    }
    return line;
  }

}
