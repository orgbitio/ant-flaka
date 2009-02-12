package net.haefelingerit.flaka;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.ComponentHelper;
import org.apache.tools.ant.Location;
import org.apache.tools.ant.Project;

public class Task extends org.apache.tools.ant.Task
{
  public boolean debug = false;

  public void setDebug(boolean b) {
    this.debug = b;
  }

  final public Project project() throws BuildException {
    Project P;
    P = super.getProject();
    return P;
  }

  public void init() {
    super.init();
    Static.setProject(project());
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
    return s == null ? null : project().getReference(s);
  }

  final protected Object setref(String s, Object ref) {
    Object r = null;
    if (s != null) {
      r = getref(s);
      project().addReference(s, ref);
    }
    return r;
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
    Project P = project();
    return P != null ? P.getProperty(s) : null;
  }

  final public void logo(String msg, int width) {
    log(Static.logo(msg, width));
  }

  final public void logo(String msg) {
    log(Static.logo(msg));
  }

  final protected void throwbx(String s) {
    Static.throwbx(s);
  }

  final protected void throwbx(boolean c, String s) {
    if (c == true) {
      Static.throwbx(s);
    }
  }

  final protected void throwbx(String s, Exception e) {
    Static.throwbx(s, e);
  }

  final public void log(String msg) {
    if (msg != null) {
      Static.log(getProject(), msg);
    }
  }

  final protected void debug(String msg) {
    if (msg != null) {
      Static.debug(getProject(), msg);
    }
  }

  final protected void debug(String msg, Exception e) {
    if (msg != null) {
      Static.debug(getProject(), msg, e);
    }
  }

  final protected void verbose(String msg) {
    if (msg != null) {
      Static.verbose(getProject(), msg);
    }
  }

  protected void warn(String msg) {
    if (msg != null) {
      StringBuffer buf;
      Location where;
      String bname;
      int line;
      int col;

      where = getLocation();
      bname = new File(where.getFileName()).getName();
      line = where.getLineNumber();
      col = where.getColumnNumber();
      buf = new StringBuffer();
      buf.append(bname);
      buf.append(':');
      buf.append(line);
      buf.append(':');
      buf.append(col);
      buf.append("[");
      buf.append(getTaskName());
      buf.append("]: ");
      buf.append(msg);
      Static._log_(getProject(), buf.toString(), Project.MSG_WARN);
    }
  }

  protected void warn(String msg, Exception e) {
    if (msg != null && e != null) {
      Static._log_(getProject(), msg + ", got " + e.getMessage(),
          Project.MSG_WARN);
    }
  }

  final protected void warning(String msg) {
    warn(msg);
  }

  final protected void error(String msg) {
    if (msg != null) {
      Static._log_(getProject(), msg, Project.MSG_ERR);
    }
  }

  final protected void error(String msg, Exception e) {
    if (msg != null && e != null) {
      Static._log_(getProject(), msg + ", got " + e.getMessage(),
          Project.MSG_ERR);
    }
  }

  final protected void info(String msg) {
    if (msg != null) {
      super.log(msg, Project.MSG_INFO);
    }
  }

  final public ComponentHelper getcomph() {
    return Static.getcomph(project());
  }

  /** shortcut to create a component */
  final public Object getcomp(String s) {
    return Static.getcomp(project(), s);
  }

  /** shortcut to get a component's class */
  final public Object getclass(String s) {
    return Static.getclass(project(), s);
  }

  /** check whether we are a target */
  final public boolean istarget(String s) {
    return Static.istarget(project(), s);
  }

  /** check whether we are a macro (and not a task) */
  final public boolean ismacro(String s) {
    return Static.ismacro(project(), s);
  }

  /** check whether we are a task (and not a macro) */
  final public boolean istask(String s) {
    return Static.istask(project(), s);
  }

  /** check whether we are a task or macro) */
  final public boolean ismacroOrtask(String s) {
    return Static.ismacroOrtask(project(), s);
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

  final protected void unset(String[] property) {
    Object obj;
    obj = getref("ant.PropertyHelper");
    for (int i = 0; i < property.length; ++i) {
      Static.htabremove(obj, "properties", property[i]);
      Static.htabremove(obj, "userProperties", property[i]);
    }
  }

  final protected void unset(String property) {
    String[] tmp = { property };
    unset(tmp);
  }

}
