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

import org.apache.tools.ant.BuildException;

/**
 * A task allowing the dynamic execution of a target.
 * 
 * 
 * @author merzedes
 * @since 1.0
 */

public class RunTarget extends Task {
  protected String name = null;
  protected boolean fail = false;

  /**
   * The name of the target to execute
   * 
   * @param s
   */
  public void setName(String s) {
    this.name = Static.trim2(s, this.name);
  }

  public String getName() {
    return this.name;
  }

  /**
   * Whether to fail if target does not exist.
   * 
   * @param b
   */
  public void setFail(boolean b) {
    this.fail = b;
  }

  public boolean getFail() {
    return this.fail;
  }

  protected void onerror(String s) {
    if (this.fail)
      throwbx(s);
    else
      verbose("warning: " + s);
  }

  public void execute() throws BuildException {
    if (this.name == null) {
      onerror("attribute `name' missing.");
      return;
    }

    if (Static.istarget(getProject(), this.name)) {
      getProject().executeTarget(this.name);
    } else {
      onerror("`" + this.name + "' not a target.");
    }
    return;
  }
}
