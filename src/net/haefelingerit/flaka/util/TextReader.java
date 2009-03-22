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

package net.haefelingerit.flaka.util;

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

  protected boolean ignore(String line)
  {
    return this.comment.matcher(line).find();
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

  /**
   * Return the next line but comment lines, supports continuation lines.
   */

  /*
   * (non-Javadoc)
   * 
   * @see java.io.BufferedReader#readLine()
   */
  public String readLine()
  {
    String line;
    String accu;

    accu = null;
    line = next();

    while (accu == null && line != null)
    {
      accu = "";

      /* Continuation line */
      while (this.continuation && line != null && line.endsWith("\\") && !line.endsWith("\\\\"))
      {
        accu += line.substring(0, line.length() - 1);
        line = next();
      }

      /* Add last line */
      accu += line;
      if (this.skipempty && accu.matches("\\s*"))
      {
        accu = null;
        line = next();
      }
    }
    return accu;
  }

}
