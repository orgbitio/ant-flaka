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

package it.haefelinger.flaka;

import it.haefelinger.flaka.util.Static;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;


import org.apache.tools.ant.types.selectors.BaseSelector;

/**
 * 
 * @author merzedes
 * @since 1.0
 */
public class IsText extends BaseSelector
{
  /** upper limit of characters to investige. */
  protected long limit = -1;

  /** invert selection */
  protected boolean invert = false;

  /** set limit */
  public void setLimit(long n)
  {
    this.limit = n;
  }

  public void setInvertMatch(boolean b)
  {
    this.invert = b;
  }

  public void setInvert(boolean b)
  {
    this.invert = b;
  }

  /**
   * * Implements a selector to restrict a given fileset to contain * "textual"
   * files only. * *
   */

  public boolean isSelected(File basedir, String filename, File file)
  {
    String path;
    InputStream S;
    boolean retv = false;

    if (file == null || basedir == null || filename == null)
    {
      debug("isText: some `nil' arguments seen, return `false'");
      return false;
    }

    path = file.getAbsolutePath();

    if (file.isDirectory())
    {
      debug("`" + path + "` is a directory, return `false'");
      return false;
    }

    if (!file.canRead())
    {
      debug("`" + path + "` is not readable, return `false'");
      return false;
    }

    S = open(file);
    if (S == null)
    {
      debug("unable to open `" + path + "`, return `false'");
      return false;
    }

    try
    {
      retv = istext(S, this.limit);
      debug("istext('" + path + "') = " + retv);
      retv = this.invert ? !retv : retv;
    } catch (Exception e)
    {
      debug("error while reading from `" + path + "`", e);
    } finally
    {
      if (!close(S))
        debug("unable to close `" + path + "` (error ignored).");
    }
    return retv;
  }

  protected boolean istext(InputStream S, long max) throws Exception
  {
    int c;
    boolean b;

    c = S.read();
    b = true;

    if (max < 0)
    {
      while (b && c != -1)
      {
        b = Static.istext((char) c);
        c = S.read();
      }
    } else
    {
      for (long i = 0; b && i < max; ++i)
      {
        b = Static.istext((char) c);
        c = S.read();
      }
    }
    return b;
  }

  protected boolean isbinary(InputStream S, long max) throws Exception
  {
    return istext(S, max) ? false : true;
  }

  protected InputStream open(File file)
  {
    InputStream retv = null;
    try
    {
      retv = new FileInputStream(file);
      retv = new BufferedInputStream(retv);
    } catch (Exception e)
    {
      /* ignore */
    }
    return retv;
  }

  protected boolean close(InputStream S)
  {
    boolean b = true;
    try
    {
      if (S != null)
        S.close();
    } catch (Exception e)
    {
      b = false;
    }
    return b;
  }

  protected void debug(String msg)
  {
    Static.debug(getProject(), "istext: " + msg);
  }

  protected void debug(String msg, Exception e)
  {
    Static.debug(getProject(), "istext: " + msg, e);
  }
}
