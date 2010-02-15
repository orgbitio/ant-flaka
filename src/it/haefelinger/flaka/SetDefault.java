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

import java.util.Hashtable;


import org.apache.tools.ant.BuildException;

/**
 * @author merzedes
 * @since 1.0
 */
public class SetDefault extends Task
{
  protected String target = null;
  protected boolean fail = false;
  protected boolean override = false;

  /**
   * The name of the default target.
   * 
   * @param s
   */
  public void setName(String s)
  {
    this.target = Static.trim2(s, this.target);
  }

  public String getName()
  {
    return this.target;
  }

  /**
   * Whether to fail if the target does not exist.
   * 
   * @param b
   */
  public void setFail(boolean b)
  {
    this.fail = b;
  }

  public boolean getFail()
  {
    return this.fail;
  }

  /**
   * Whether to override an existing default target
   * 
   * @param b
   */
  public void setOverride(boolean b)
  {
    this.override = b;
  }

  protected boolean hastarget(String s)
  {
    Hashtable H = getProject().getTargets();
    return H.containsKey(s);
  }

  public void execute() throws BuildException
  {
    String s;

    if (this.target == null)
    {
      debug("no default target given to set");
      return;
    }

    s = getProject().getDefaultTarget();
    if (!Static.isEmpty(s) && this.override == false)
    {
      debug("default target already set to `" + s + "' (ignored)");
      return;
    }

    if (hastarget(this.target))
    {
      getProject().setDefault(this.target);
    } else
    {
      String m;
      m = "target `" + this.target + "' does not exist.";
      if (this.fail)
      {
        throwbx(m);
      } else
      {
        verbose(m + " (ignored)");
      }
    }
  }
}
