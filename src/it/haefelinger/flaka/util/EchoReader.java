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

import java.io.Reader;

public class EchoReader extends TextReader
{
  protected boolean firstline = true;
  public String shift = "";
  public char ic = '>';

  public EchoReader(Reader reader)
  {
    super(reader);
    this.skipempty = false;
  }

  public EchoReader(String text)
  {
    super(text);
    this.skipempty = false;
  }

  protected int lefttrim(String line, int lastindex)
  {
    int i;

    i = 0;
    /* strip whitespace */
    while (i <= lastindex && Character.isWhitespace(line.charAt(i)))
      i += 1;
    return i;
  }

  protected String indent(String line)
  {
    int i = lefttrim(line, line.length() - 1);
    int L = line.length();
    int n = 0;
    StringBuilder accu = new StringBuilder();

    /* replace indendation character */
    while (i < L && line.charAt(i) == this.ic)
    {
      i++;
      accu.append(this.shift);
    }
    n = L;
    if (line.endsWith("\\"))
      n = L - 1;
    accu.append(line, i, n);
    return accu.toString();
  }

  public String readLine()
  {
    String line, accu;
    int i, L;

    accu = null;
    line = next();

    while (line != null)
    {
      // If the first line contains only ws, it will be ignored. Otherwise,
      // take the first line as is.
      if (this.firstline)
      {
        this.firstline = false;
        if (line.matches("\\s*"))
        {
          line = next();
          continue;
        }
        if (line.endsWith("\\\\"))
        {
          accu = line.substring(0, line.length() - 1);
          line = null;
          continue;
        }
        if (line.endsWith("\\"))
        {
          accu = line.substring(0, line.length() - 1);
          line = next();
          continue;
        }
        accu = line;
        line = null;
        continue;
      }
      // Subsequent lines: strip leading whitespace.
      L = line.length();
      i = lefttrim(line, L - 1);

      if (accu == null)
        accu = "";

      /* replace indendation character */
      while (i < L && line.charAt(i) == this.ic)
      {
        accu += this.shift;
        i += 1;
      }
      if (line.endsWith("\\\\"))
      {
        accu += line.substring(i, L - 1);
        line = null;
        continue;
      }
      if (line.endsWith("\\"))
      {
        accu += line.substring(i, L - 1);
        line = next();
        continue;
      }
      accu += line.substring(i);
      line = null;
    }
    return accu;
  }
}
