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

import java.io.File;


import org.apache.tools.ant.Project;

public class MatchingTask extends org.apache.tools.ant.taskdefs.MatchingTask
{
  public boolean debug;
  
  public void setDebug(boolean b) {
    this.debug = b;
  }
  
  final public void debug(String msg) {
    if (this.debug) {
      System.err.println(msg);
      return;
    }
    if (msg != null) {
      this.getProject().log(msg,Project.MSG_DEBUG);
    }
  }
  
  final public void debug(String msg, Exception e) {
    if (this.debug) {
      System.err.println(msg + ":" + e);
      return;
    }
    if (msg != null) {
      Static.debug(getProject(), msg, e);
    }
  }
  
  final protected void setid(String id, Object obj) {
    Project P = this.getProject();
    if (P != null && id != null && id.trim().length() > 0) {
      debug("set reference `" + id + "' in current project");
      P.addReference(id, obj);
    }
  }

  final protected void makeref(String id, Object obj) {
    setid(id, obj);
  }

  final protected void makevar(String id, Object obj) {
    setid(id, obj);
  }
  /**
   * Reference object denoted by string <b>s</b>.
   * 
   * This method is a wrapper around method
   * {@link org.apache.tools.ant.Project#getReference(String)} allowing to pass
   * a null string as well. If null is passed, a null object is returned.
   * 
   * @param s
   *          may be null
   * @return object denoted by <code>s</code> or null if not existing.
   */
  final public Object getref(String s) {
    return s == null ? null : getProject().getReference(s);
  }
  /**
   * A convenient method to retrieve a project property.
   * 
   * @param s
   *          might be null
   * @return value of property <code>s</code> or null if such a property does
   *         not exist or if no project is associated with this task.
   */
  final public String getProperty(String s) {
    Project P = getProject();
    return P != null ? P.getProperty(s) : null;
  }
  
  
  final public File toFile(String s) {
    File f = null;
    if (s != null)
      s = s.trim();
    if (s ==null || s.equals(""))
      return this.getProject().getBaseDir();
    
    f = new File(s);
    if (f.isAbsolute() == false) 
    {
      f = this.getProject().getBaseDir();
      f = new File(f,s);
    }
    return f;
  }
  
}
