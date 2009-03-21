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

package net.haefelingerit.flaka;

import java.io.File;

import net.haefelingerit.flaka.util.Static;

import org.apache.tools.ant.BuildException;

public class CvsStat extends Task
{
  private String stem = "cvsstat.";
  private File path = null;
  protected boolean fail = false;
  protected boolean preserve = false;

  public void setArg(File x)
  {
    this.path = x;
  }

  public void setPath(File x)
  {
    this.path = x;
  }

  public void setFile(File x)
  {
    this.path = x;
  }

  public void setFail(boolean b)
  {
    this.fail = b;
  }

  public void setPreserve(boolean b)
  {
    this.preserve = b;
  }

  /* The stem to be used for properties */
  public void setStem(String x)
  {
    this.stem = Static.trim2(x, this.stem);
    /* make sure that new stem ends with "." */
    if (!this.stem.endsWith("."))
      this.stem += ".";
  }

  public void execute() throws BuildException
  {
    // Let's first unset all variables we are going to
    // assign later.
    htabrm("error");
    if (this.preserve == false)
    {
      htabrm("cvsfile");
      htabrm("cvsrev");
      htabrm("cvsdate");
      htabrm("cvsstag");
      htabrm("cvstag");
      htabrm("cvsdir");
      htabrm("cvsroot");
    }

    if (this.path == null)
    {
      String s = this.getProperty("ant.file");
      if (Static.isEmpty(s))
      {
        handleError("use 'path' to specify loc argument.");
        return;
      }
      this.path = new File(s);
    }
    statCvsProperties();
  }

  private void htabput(String K, String v)
  {
    String k = this.stem + K;
    if (this.debug)
      System.err.println("set property: `" + k + "'='" + v + "'");
    // do not set propery if already set
    getProject().setNewProperty(k, v);
  }

  private void htabrm(String K)
  {
    String k = this.stem + K;
    if (this.debug)
      System.err.println("removing property: `" + k + "'");
    Static.unset(getProject(), k);
  }

  protected void handleError(String s)
  {
    /* If an error occurs, property stem.error is set */
    htabput("error", s);
    if (this.fail)
      throw new BuildException(s);
    if (this.debug)
    {
      System.err.println("**error: " + s);
    } else
      debug(s);
  }

  protected void statCvsProperties() throws BuildException
  {
    /* set and override properties */
    int i, j;
    String s;
    File file = this.path;
    File dir, cvs, cvsfile;
    String[] buf;

    // check whether loc exists ..
    if (!file.exists())
    {
      handleError("this loc does not exists: " + file.getPath());
      return;
    }
    dir = file.getParentFile();
    if (dir == null)
    {
      handleError("parent dir does not exists: " + file.getPath());
      return;
    }
    cvs = new File(dir, "CVS");
    if (!cvs.exists())
    {
      handleError("CVS directory not available: " + cvs.getPath());
      return;
    }
    if (!cvs.isDirectory())
    {
      handleError("CVS exists but not a directory: " + cvs.getPath());
      return;
    }
    cvsfile = new File(cvs, "Entries");
    if (!cvsfile.canRead())
    {
      handleError("unable to read : " + cvsfile.getPath());
      return;
    }

    /* Read content of CVS/Entries */
    String entries = Static.readlines(cvsfile);
    if (entries == null)
    {
      handleError("unknown error while reading: " + cvsfile.getPath());
      return;
    }
    /* control output */
    debug(entries);

    /* get loc's basename */
    String name = file.getName();

    /* find in entries the line that starts with "/<loc>/" */
    String[] line = Static.bufread(entries);

    for (i = 0; i < line.length; ++i)
    {
      String[] col = Static.split(line[i], "/");

      if (col == null)
        continue;

      if (col.length < 2)
      {
        continue;
      }
      if (col[1] == null)
        continue;

      if (col[1].equals(name))
      {
        htabput("cvsfile", name);
        if (col.length > 2 && !Static.isEmpty(col[2]))
          htabput("cvsrev", col[2]);
        if (col.length > 3 && !Static.isEmpty(col[3]))
          htabput("cvsdate", col[3]);
        if (col.length > 5 && !Static.isEmpty(col[5]))
        {
          String tag = col[5].substring(1);
          htabput("cvstag", tag);
          htabput("cvsstag", tag);
        }
        break;
      }
    }

    if (i >= line.length)
    {
      handleError("unable to find `" + name + "' in `" + cvsfile.getPath() + "'");
      return;
    }

    /* figure name of the module */
    cvsfile = new File(cvs, "Repository");
    if (!cvsfile.canRead())
    {
      handleError("unable to read : " + cvsfile.getPath());
    } else
    {
      buf = Static.bufread(cvsfile);
      for (j = 0; j < buf.length; ++j)
      {
        if (!buf[j].equals(""))
        {
          htabput("cvsdir", buf[j]);
          break;
        }
      }
    }
    /* figure the name of the CVS repository */
    cvsfile = new File(cvs, "Root");
    if (!cvsfile.canRead())
    {
      handleError("unable to read : " + cvsfile.getPath());
    } else
    {
      buf = Static.bufread(cvsfile);
      for (j = 0; j < buf.length; ++j)
      {
        if (!buf[j].equals(""))
        {
          htabput("cvsroot", buf[j]);
          break;
        }
      }
    }
    /*
     * This loc seems to be only present if a folder is checked out with a tag.
     * If given, we set 'cvstag' - but not cvsstag.
     */
    cvsfile = new File(cvs, "Tag");
    if (!cvsfile.canRead())
    {
      debug("(ignore) unable to read " + cvsfile.getPath());
    } else
    {
      buf = Static.bufread(cvsfile);
      if (buf.length <= 0)
      {
        debug("CVS/Tag appears to be empty: " + cvsfile.getPath());
      } else
      {
        for (j = 0; j < buf.length; ++j)
        {
          s = buf[j].trim();
          if (!s.equals(""))
          {
            char c = s.charAt(0);
            if (c == 'T')
            {
              /* branch tag */
              /* we do not treat a branch tag as regular tag */
              // htabput(P,H,stem + "cvstag",s.substring(1));
              break;
            }
            if (c == 'D')
            {
              /* date tag */
              // htabput(P,H,stem + "cvstag",s.substring(1));
              break;
            }
            if (c == 'N')
            {
              /* non branch tag */
              htabput("cvstag", s.substring(1));
              break;
            }
            /* should not happen according to CVS manual */
            debug("malformed entry found in: `" + cvsfile.getPath() + "'");
            htabput("cvstag", s.substring(1));
            break;
          }
        }
      }
    }
  }
}
