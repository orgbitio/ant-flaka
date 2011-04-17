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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.TaskContainer;

/**
 * A task to rescue variables.
 * 
 * @author merzedes
 * @since 1.0
 */
public class Rescue extends it.haefelinger.flaka.Task implements TaskContainer {
  /* items to protect */
  protected it.haefelinger.flaka.List vars;
  protected it.haefelinger.flaka.List properties;
  protected Map varhtab;
  protected Map ptyhtab;
  /** Optional Vector holding the nested tasks */
  protected Vector tasks = new Vector();

  public void addVars(it.haefelinger.flaka.List list) {
    this.vars = list;
    this.vars.setEl(false);
  }

  public void addProperties(it.haefelinger.flaka.List list) {
    this.properties = list;
    this.properties.setEl(false);
  }

  public void addTask(Task nestedTask) {
    this.tasks.add(nestedTask);
  }

  protected void rescue() {
    Project project;
    Iterator iter;
    String key;
    Object val;

    if (this.vars != null) {
      project = getProject();
      this.varhtab = new HashMap();
      iter = this.vars.eval().iterator();
      while (iter.hasNext()) {
        key = (String) iter.next();
        // TODO: references != vars
        val = project.getReference(key);
        this.varhtab.put(key, val);
      }
    }
    if (this.properties != null) {
      project = getProject();
      this.ptyhtab = new HashMap();
      iter = this.properties.eval().iterator();
      while (iter.hasNext()) {
        key = (String) iter.next();
        val = project.getProperty(key);
        this.ptyhtab.put(key, val);
      }
    }
  }

  protected void restore() {
    Project project;
    Iterator iter;
    String key;
    Object val;

    if (this.varhtab != null) {
      project = getProject();
      iter = this.varhtab.keySet().iterator();
      while (iter.hasNext()) {
        key = (String) iter.next();
        val = this.varhtab.get(key);
        Static.assign(project, key, val, Static.VARREF);
      }
    }
    if (this.ptyhtab != null) {
      project = getProject();
      iter = this.ptyhtab.keySet().iterator();
      while (iter.hasNext()) {
        key = (String) iter.next();
        val = this.ptyhtab.get(key);
        Static.assign(project, key, val, Static.WRITEPROPTY);
      }
    }
  }

  public void execute() throws BuildException {
    rescue();
    try {
      org.apache.tools.ant.Task task;
      Iterator iter;

      iter = this.tasks.iterator();
      while (iter.hasNext()) {
        task = (org.apache.tools.ant.Task) iter.next();
        task.perform();
      }
    } finally {
      restore();
    }
  }
}