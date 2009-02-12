package net.haefelingerit.flaka;

import java.util.regex.Pattern;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Sequential;

public class IfDef extends Sequential
{
  protected String  var         = null;
  protected String  property    = null;
  protected String  target      = null;
  protected String  macro       = null;
  protected String  reference   = null;
  protected String  type        = null;
  protected String  task        = null;
  protected String  clazz       = null;
  protected String  taskormacro = null;

  /* negate */
  protected boolean not         = false;
  protected Pattern ifs         = Pattern.compile("\\s");

  public void setNot(boolean b) {
    this.not = b;
  }

  public void setNegate(boolean b) {
    this.not = b;
  }

  public void setInvert(boolean b) {
    this.not = b;
  }

  public void setIfs(String s) {
    this.ifs = Pattern.compile(s);
  }

  public void setVar(String s) {
    this.var = Static.trim2(s, this.var);
  }

  public void setProperty(String s) {
    this.property = Static.trim2(s, this.property);
  }

  public void setTarget(String s) {
    this.target = Static.trim2(s, this.target);
  }

  public void setMacro(String s) {
    this.macro = Static.trim2(s, this.macro);
  }

  public void setReference(String s) {
    this.reference = Static.trim2(s, this.reference);
  }

  public void setTask(String s) {
    this.task = Static.trim2(s, this.task);
  }

  public void setType(String s) {
    this.type = Static.trim2(s, this.type);
  }

  public void setClass(String s) {
    this.clazz = Static.trim2(s, this.clazz);
  }

  public void setTaskorMacro(String s) {
    this.taskormacro = Static.trim2(s, this.taskormacro);
  }

  interface Test
  {
    public boolean eval(String s);
  }

  /**
   * @param s
   *          not null
   * @param mytest
   *          not null
   */
  protected boolean eval(String s, Test mytest) {
    String[] args;
    boolean test;

    test = true;
    args = this.ifs.split(s);
    for (int i = 0; test && i < args.length; ++i) {
      /* do not test empty strings */
      if (args[i] != null && !Static.isEmpty(args[i]))
        test = mytest.eval(args[i]);
    }
    return args.length > 0 ? test : false;
  }

  public boolean eval() throws BuildException {
    final Project p; /* this variable must be final */
    boolean b;

    b = true;
    p = getProject();

    if (b && this.property != null) {
      b = eval(this.property, new Test() {
        public boolean eval(String s) {
          return Static.isproperty(p, s);
        }
      });
    }
    if (b && this.reference != null) {
      b = eval(this.reference, new Test() {
        public boolean eval(String s) {
          return Static.isreference(p, s);
        }
      });
    }
    if (b && this.target != null) {
      b = eval(this.target, new Test() {
        public boolean eval(String s) {
          return Static.istarget(p, s);
        }
      });
    }
    if (b && this.task != null) {
      b = eval(this.task, new Test() {
        public boolean eval(String s) {
          return Static.istask(p, s);
        }
      });
    }
    if (b && this.macro != null) {
      b = eval(this.macro, new Test() {
        public boolean eval(String s) {
          return Static.ismacro(p, s);
        }
      });
    }
    if (b && this.taskormacro != null) {
      b = eval(this.taskormacro, new Test() {
        public boolean eval(String s) {
          return Static.ismacroOrtask(p, s);
        }
      });
    }

    if (b && this.type != null) {
      b = eval(this.type, new Test() {
        public boolean eval(String s) {
          return p.getDataTypeDefinitions().get(s) != null;
        }
      });
    }
    if (b && this.var != null) {
      b = eval(this.var, new Test() {
        public boolean eval(String s) {
          return Static.isproperty(p, s) || Static.isreference(p, s);
        }
      });
    }
    if (b && this.clazz != null) {
      b = eval(this.var, new Test() {
        public boolean eval(String s) {
          return Static.isproperty(p, s) || Static.isreference(p, s);
        }
      });
    }

    /* negate if requested */
    if (this.not) {
      b = !b;
    }
    /* eventually we are done */
    return b;
  }

  public void exec() {
    super.execute();
  }

  public void execute() throws BuildException {
    if (eval()) {
      exec();
    }
  }
}
