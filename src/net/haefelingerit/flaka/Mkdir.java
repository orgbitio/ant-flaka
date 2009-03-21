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
import org.apache.tools.ant.BuildException;

public class Mkdir extends Task
{

  private static final int MKDIR_RETRY_SLEEP_MILLIS = 10;

  protected File dir;
  protected boolean verbose = false;

  public void execute() throws BuildException
  {
    if (this.dir == null)
    {
      debug("mkdir called without `dir' attribute set");
      return;
    }

    if (this.dir.isDirectory())
    {
      /* exists already */
      return;
    }

    if (this.dir.exists())
    {
      String s = this.dir.getAbsolutePath();
      throwbx("unable to create `" + s + "', exists and is not a directory.");
    }

    /* try to create directory */
    mkdirs(this.dir);

    if (this.dir.isDirectory())
    {
      if (this.verbose)
        log("directory `" + this.dir.getPath() + "' created.");
    } else
    {
      String s = "failed to create `" + this.dir.getAbsolutePath() + "'";
      throwbx(s);
    }
  }

  /**
   * the directory to create; not required.
   * 
   * @param dir
   *          the directory to be made.
   */
  public void setDir(File dir)
  {
    this.dir = dir;
  }

  public void setVerbose(boolean b)
  {
    this.verbose = b;
  }

  /**
   * Attempt to fix possible race condition when creating directories on WinXP.
   * If the mkdirs does not work, wait a little and try again.
   */

  private boolean mkdirs(File f)
  {
    if (!f.mkdirs())
    {
      try
      {
        Thread.sleep(MKDIR_RETRY_SLEEP_MILLIS);
        return f.mkdirs();
      } catch (InterruptedException ex)
      {
        return f.mkdirs();
      }
    }
    return true;
  }
}
