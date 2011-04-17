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

import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Sequential;

/**
 * 
 * @author merzedes
 * @since 1.0
 */
public class Choose extends Task {
  protected List whenlist;
  protected Sequential otherwise;

  protected List caselist() {
    if (this.whenlist == null) {
      this.whenlist = new ArrayList();
    }
    return this.whenlist;
  }

  public void addWhen(When task) {
    if (task != null)
      caselist().add(task);
  }

  public void addUnless(Unless task) {
    if (task != null)
      caselist().add(task);
  }

  public void addOtherwise(Sequential task) throws BuildException {
    if (task == null) {
      return;
    }
    if (this.otherwise != null) {
      throwbx("<otherwise/> clause already used.");
      return;
    }
    this.otherwise = task;
    return;
  }

  public void addDefault(Sequential task) throws BuildException {
    if (task == null) {
      return;
    }
    if (this.otherwise != null) {
      throwbx("<otherwise/> clause already used.");
      return;
    }
    this.otherwise = task;
    return;
  }

  public void execute() throws BuildException {
    When when;
    /*
     * If we do not have some 'when' conditions but we have an otherwise we
     * execute the otherwise, otherwise we return silently.
     */
    if (this.whenlist == null) {
      if (this.otherwise != null) {
        this.otherwise.execute();
      }
      return;
    }

    /* execute the very fist 'when' that evaluates to 'true' */
    for (int i = 0; i < this.whenlist.size(); ++i) {
      when = (When) this.whenlist.get(i);
      if (when.eval()) {
        when.exec();
        return;
      }
    }

    /* otherwise execute the otherwise task */
    if (this.otherwise != null) {
      this.otherwise.execute();
    }
    return;
  }
}
