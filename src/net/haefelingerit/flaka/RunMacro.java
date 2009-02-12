package net.haefelingerit.flaka;

import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.RuntimeConfigurable;
import org.apache.tools.ant.taskdefs.MacroInstance;
import org.apache.tools.ant.taskdefs.PreSetDef;

/**
 * A task allowing the dynamic execution of a macro or task.
 * 
 * @author wh81752
 * @since 2.0
 */

public class RunMacro extends Task
{
  protected String  name = "";
  protected boolean fail = false;
  protected List    args = null; /* list of arguments */

  /**
   * The name of the macro to execute.
   * 
   * @param s
   */
  public void setName(String s) {
    this.name = Static.trim2(s, this.name);
  }

  /**
   * Fail if macro does not exist.
   * 
   * @param b
   */
  public void setFail(boolean b) {
    this.fail = b;
  }

  public boolean getFail() {
    return this.fail;
  }

  /** nested element <code>param</code> */
  public Param createParam() {
    return createArg();
  }

  /** nested element <code>attribute</code> */
  public Param createAttribute() {
    return createArg();
  }

  /** nested element <code>arg</code> */
  public Param createArg() {
    Param P;
    P = new Param();
    addarg(P);
    return P;
  }

  protected List getargs() {
    if (this.args == null)
      this.args = new ArrayList();
    return this.args;
  }

  protected void addarg(Object obj) {
    getargs().add(obj);
  }

  protected class Param
  {
    protected String k;
    protected String v;

    public Param() {
      this.k = null;
      this.v = null;
    }

    public void setName(String k) {
      this.k = Static.trim2(k, this.k);
    }

    public void setValue(String v) {
      this.v = Static.trim2(v, this.v);
    }
  }

  protected void onerror(String s) {
    if (this.fail)
      throwbx(s);
    else
      verbose("warning: " + s);
  }

  protected void runmacro(String m, Object[] args) {
    Param P;
    Object obj;

    obj = getcomp(m);
    if (obj == null) {
      onerror("`" + m + "' neither marco nor task.");
      return;
    }

    /*
     * Check whether it's a presetdef. If so then Project.createTask() fails
     * with ClassCastException (1.6.5). In such a way we need to create the
     * object like shown below ..
     */
    if (obj instanceof PreSetDef.PreSetDefinition) {
      PreSetDef.PreSetDefinition psd;
      psd = (PreSetDef.PreSetDefinition) obj;
      obj = psd.createObject(getProject());
    } else {
      /* try to create task */
      obj = getProject().createTask(m);
      if (obj == null) {
        /* this should not happen - anyhow, we check again */
        onerror("`" + m + "' neither marco nor task.");
        return;
      }
    }

    if (obj instanceof MacroInstance) {
      MacroInstance M;
      M = (MacroInstance) obj;
      for (int i = 0; i < args.length; ++i) {
        if (args[i] instanceof Param) {
          P = (Param) args[i];
          M.setDynamicAttribute(P.k, P.v);
        }
      }
      M.execute();
      return;
    }

    if (obj instanceof org.apache.tools.ant.Task) {
      RuntimeConfigurable rtc;
      org.apache.tools.ant.Task T;

      T = (org.apache.tools.ant.Task) obj;
      rtc = T.getRuntimeConfigurableWrapper();
      for (int i = 0; i < args.length; ++i) {
        if (args[i] instanceof Param) {
          P = (Param) args[i];
          rtc.setAttribute(P.k, P.v);
        }
      }
      T.execute();
      return;
    }

    onerror("`" + m + "' neither marco nor task.");
    return;
  }

  public void execute() throws BuildException {
    Object[] args = getargs().toArray();
    String[] name = this.name.split("\\s+");

    for (int i = 0; i < name.length; ++i)
      runmacro(name[i], args);
  }
}
