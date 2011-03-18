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

import java.io.File;

import org.apache.tools.ant.Location;
import org.apache.tools.ant.Project;

/**
 * @author merzedes
 * @since 1.0
 */
public class Task extends org.apache.tools.ant.Task {
  public boolean el = true;
  public boolean debug = false;

  public void setEl(boolean b) {
    this.el = b;
  }

  public void setDebug(boolean b) {
    this.debug = b;
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

  final protected void throwbx(String s) {
    Static.throwbx(s);
  }

  final public File toFile(String s) {
    return Static.toFile(this.getProject(), s);
  }

  final protected void throwbx(String s, Exception e) {
    Static.throwbx(s, e);
  }

  final public void log(String msg) {
    if (msg != null) {
      Static.log(getProject(), msg);
    }
  }

  final public void debug(String msg) {
    if (this.debug) {
      System.err.println(msg);
      return;
    }
    if (msg != null) {
      Static.debug(getProject(), msg);
    }
  }

  final public void debug(String msg, Exception e) {
    if (msg != null) {
      Static.debug(getProject(), msg, e);
    }
  }

  final public void verbose(String msg) {
    if (msg != null) {
      Static.verbose(getProject(), msg);
    }
  }

  @SuppressWarnings("boxing")
  final public void warn(String msg) {
    if (msg != null) {
      Location where;
      String bname, tname;
      String buf;
      int line;
      int col;

      where = getLocation();
      bname = new File(where.getFileName()).getName();
      tname = this.getTaskName();
      line = where.getLineNumber();
      col = where.getColumnNumber();
      buf = String.format("%s:%s:%s[%s]: %s", bname, line, col, tname, msg);
      Static._log_(getProject(), buf.toString(), Project.MSG_WARN);
    }
  }

  public void warn(String msg, Exception e) {
    if (msg != null && e != null) {
      Static._log_(getProject(), msg + ", got " + e.getMessage(),
          Project.MSG_WARN);
    }
  }

  final public void error(String msg) {
    if (msg != null) {
      Static._log_(getProject(), msg, Project.MSG_ERR);
    }
  }

  final public void error(String msg, Exception e) {
    if (msg != null && e != null) {
      Static._log_(getProject(), msg + ", got " + e.getMessage(),
          Project.MSG_ERR);
    }
  }

  final public void info(String msg) {
    if (msg != null) {
      super.log(msg, Project.MSG_INFO);
    }
  }

}
