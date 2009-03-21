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

import net.haefelingerit.flaka.util.Static;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

public class Require extends Task
{
  /* This is the minimal Ant version requird to make Flaka happy */
  protected String arg = "1.7.0";

  public Require()
  {
    super();
  }

  /**
   * Set the minimum Ant version required.
   * 
   * @param x
   */
  public void setAtLeast(String x)
  {
    this.arg = Static.trim2(x, this.arg);
  }

  private String getversion(Project P)
  {
    String s;
    String[] x;

    s = P.getProperty("ant.version");
    if (s == null)
      return s;

    x = s.split("\\s+");
    for (int i = 0; i < x.length; ++i)
    {
      if (x[i].equals("version"))
      {
        try
        {
          s = x[i + 1];
        } catch (Exception e)
        {
          s = null;
        }
        return s;
      }
    }
    return null;
  }

  public void execute() throws BuildException
  {
    int c;
    String v;
    Project P = getProject();

    if (this.arg == null)
    {
      return;
    }

    v = getversion(P);
    if (v == null || v.trim().equals(""))
      Static.throwbx("suspicious ant - there's no property named `ant.version'");

    c = Static.vercmp(this.arg, v);

    Static.debug(P, "compared " + this.arg + " with " + v + ": " + c);

    if (c > 0)
    {
      /*
       * minimal required version is greater than the version used, throw
       * appropriate exception to report this build failure
       */
      Static.throwbx("Ant " + this.arg + " is required for this build, while you are "
          + "using Ant " + v + ".");
    }
  }
}
