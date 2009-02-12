package net.haefelingerit.flaka;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Sequential;

/**
 * @author wh81752
 * 
 */
public class Switch extends Task
{
  protected String     value     = "";
  protected List       cases     = new ArrayList();
  protected Sequential defaultcase;
  protected String     stem      = "switch";

  protected boolean    igcase    = true;
  protected boolean    dotall    = false;
  protected boolean    unixlines = false;
  protected boolean    comments  = false;
  protected boolean    multiline = false;
  protected boolean    find      = false;

  public void setStem(String s) {
    this.stem = Static.trim2(s, this.stem);
  }

  public void setIgnoreCase(boolean b) {
    this.igcase = b;
  }

  public void setFind(boolean b) {
    this.find = b;
  }

  public void setDotAll(boolean b) {
    this.dotall = b;
  }

  public void setUnixLines(boolean b) {
    this.unixlines = b;
  }

  public void setComments(boolean b) {
    this.comments = b;
  }

  public void setMultiLine(boolean b) {
    this.multiline = b;
  }

  public void setValue(String value) {
    this.value = Static.trim2(value, this.value);
  }

  /** Case class */
  public final class Case extends Sequential
  {
    protected String  caseval   = null;
    protected boolean shellglob = false;
    protected String  cstem;
    protected boolean viamatch  = false;
    /* flags */
    protected boolean igcase    = Switch.this.igcase;
    protected boolean dotall    = Switch.this.dotall;
    protected boolean unixlines = Switch.this.unixlines;
    protected boolean comments  = Switch.this.comments;
    protected boolean multiline = Switch.this.multiline;
    protected boolean find      = Switch.this.find;

    public Case(String stem) {
      super();
      setStem(stem);
    }

    public void setValue(String value) {
      this.caseval = value;
      this.viamatch = false;
    }

    public void setMatch(String value) {
      this.caseval = value;
      this.viamatch = true;
    }

    public void setIgnoreCase(boolean b) {
      this.igcase = b;
    }

    public void setFind(boolean b) {
      this.find = b;
    }

    public void setDotAll(boolean b) {
      this.dotall = b;
    }

    public void setUnixLines(boolean b) {
      this.unixlines = b;
    }

    public void setComments(boolean b) {
      this.comments = b;
    }

    public void setMultiLine(boolean b) {
      this.multiline = b;
    }

    public void setShellGlob(boolean b) {
      this.shellglob = b;
    }

    public void setGlob(boolean b) {
      this.shellglob = b;
    }

    public void setStem(String S) {
      String s = S;
      s = Static.trim2(s, this.cstem);
      if (!s.endsWith(".")) {
        s += '.';
      }
      this.cstem = s;
    }

    public int flags() {
      int flags = 0;
      if (this.igcase) {
        flags |= Pattern.CASE_INSENSITIVE;
      }
      if (this.dotall) {
        flags |= Pattern.DOTALL;
      }
      if (this.multiline) {
        flags |= Pattern.MULTILINE;
      }
      if (this.comments) {
        flags |= Pattern.COMMENTS;
      }
      if (this.unixlines) {
        flags |= Pattern.UNIX_LINES;
      }
      return flags;
    }

    protected boolean match(Pattern regex, String value, boolean find) {
      int i;
      boolean r = false;
      Matcher M;

      /* match it */
      M = regex.matcher(value);
      r = find ? M.find() : M.matches();

      set(this.cstem + "p", M.pattern().pattern());
      set(this.cstem + "n", "" + M.groupCount());
      set(this.cstem + "v", value);

      if (r) {
        for (i = 0; i <= M.groupCount(); ++i) {
          set(this.cstem + "g" + i, M.group(i));
          set(this.cstem + "s" + i, "" + M.start(i));
          set(this.cstem + "e" + i, "" + M.end(i));
        }
      } else {
        set(this.cstem + "g0", "" + value);
        set(this.cstem + "s0", "" + 0);
        set(this.cstem + "e0", "" + value.length());
      }
      if (Switch.this.debug) {
        String pattern = M.pattern().pattern();
        System.err.println("matching regex/pat |" + this.caseval
            + "| against |" + value + "| using regex |" + pattern + "| => " + r);
      }
      return r;
    }

    /**
     * @param value
     *          not null
     */
    public boolean legacymatch(String value) {
      boolean r = false;
      Pattern regex;
      int f;
      String s;

      f = flags();
      s = this.caseval;
      if (this.shellglob)
        s = Static.patternAsRegex(this.caseval);
      try {
        regex = Pattern.compile(s, f);
        r = match(regex, value, true);
      }
      catch (Exception e) {
        if (Switch.this.debug) {
          System.err.println("** exception seen: " + e);
        }
      }
      return r;
    }

    /**
     * @param value
     *          not null
     */
    public boolean match(String value) {
      boolean r = false;
      Pattern P = null;

      P = Static.patterncompile(this.caseval, flags());
      if (P != null)
        r = match(P, value, this.find);
      return r;
    }

    /**
     * @param var
     * @param val
     */
    private void set(String var, String val) {
      if (var != null) {
        unset(var);
        if (val != null) {
          getProject().setProperty(var, val);
        }
      }
    }
  }

  /**
   * @return
   * @throws BuildException
   */
  public Switch.Case createCase() throws BuildException {
    if (this.stem == null || this.stem.length() <= 0
        || this.stem.matches("^\\s*$"))
      throw new BuildException("bad stem `" + this.stem + "'");
    Switch.Case res = new Switch.Case(this.stem);
    this.cases.add(res);
    return res;
  }

  /**
   * @param res
   * @throws BuildException
   */
  public void addDefault(Sequential res) throws BuildException {
    if (this.defaultcase != null)
      throw new BuildException("cannot specify multiple default cases");
    this.defaultcase = res;
  }

  /**
   * @param res
   * @throws BuildException
   */
  public void addOtherwise(Sequential res) throws BuildException {
    addDefault(res);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.tools.ant.Task#execute()
   */
  public void execute() throws BuildException {
    boolean b;
    Case c;
    Sequential s;

    /* we need a value to do something .. */
    if (this.value == null) {
      debug("no switch value given ..");
      return;
    }

    if (this.cases.size() == 0 && this.defaultcase == null) {
      debug("no switch cases given ..");
      return;
    }

    b = false;
    s = null;
    c = null;
    for (int i = 0; !b && i < this.cases.size(); i++) {
      c = (Case) this.cases.get(i);
      b = c.viamatch ? c.match(this.value) : c.legacymatch(this.value);
    }
    s = b ? c : this.defaultcase;
    if (s != null)
      s.perform();
  }

}
