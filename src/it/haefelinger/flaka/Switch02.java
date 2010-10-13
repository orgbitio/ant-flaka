/*
 * Copyright (c) 2009,2010 Haefelinger IT 
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

import it.haefelinger.flaka.el.MatcherBean;
import it.haefelinger.flaka.util.Static;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Sequential;

/**
 * Switch (02) - a revisited task for pattern matching.
 * 
 * @author merzedes
 * @since 1.2
 */
public class Switch02 extends Task
{
  protected String value;
  protected List cases = new ArrayList();
  protected Sequential defaultcase;
  protected String var;
  protected int flags;

  public void setVar(String s)
  {
    this.var = Static.trim3(getProject(), s, this.var);
  }

  public void setIgnoreCase(boolean b)
  {
    this.flags = Static.bitset(this.flags, Pattern.CASE_INSENSITIVE, b);
  }

  public void setDotAll(boolean b)
  {
    this.flags = Static.bitset(this.flags, Pattern.DOTALL, b);
  }

  public void setUnixLines(boolean b)
  {
    this.flags = Static.bitset(this.flags, Pattern.UNIX_LINES, b);
  }

  public void setComments(boolean b)
  {
    this.flags = Static.bitset(this.flags, Pattern.COMMENTS, b);
  }

  public void setMultiLine(boolean b)
  {
    this.flags = Static.bitset(this.flags, Pattern.MULTILINE, b);
  }

  public void setValue(String value)
  {
    /* provide empty string if value would resolve to null after trimming */
    this.value = Static.trim3(getProject(), value, "");
  }

  /** Case class */
  static protected final class RE extends Case
  {
    public String regexstr = null;
    public boolean literally = true;

    public void setExpr(String value)
    {
      this.regexstr = Static.trim3(getProject(), value, this.regexstr);
    }

    public void setLiterally(boolean b)
    {
      this.literally = b;
    }

    /**
     * Try this value against this clause.
     */
    public boolean tryvalue(String value)
    {
      Pattern P = null;
      boolean r = false;

      if (this.regexstr != null)
      {
        try
        {
          P = Pattern.compile(this.regexstr, this.flags);
        } catch (Exception e)
        {
          if (this.literally)
          {
            Static.warning(this.getProject(), "illegal regex '" + this.regexstr
                + "'. Taking expr literally..");
            P = Pattern.compile(Pattern.quote(this.regexstr), this.flags);
          } else
          {
            throw new BuildException(e);
          }
        }
        r = find(P, value);
      }
      return this.not ? !r : r;
    }
  }

  /** Case class */
  static protected final class GLOB extends Case
  {
    public String regexstr = null;

    public void setExpr(String value)
    {
      this.regexstr = Static.trim3(getProject(), value, this.regexstr);
    }

    /**
     * Try this value against this clause.
     */
    public boolean tryvalue(String value)
    {
      Pattern P = null;
      boolean r = false;

      if (this.regexstr != null)
      {
        String glob = Static.patternAsRegex(this.regexstr);
        P = Pattern.compile(glob, this.flags);
        r = matches(P, value);
      }
      return this.not ? !r : r;
    }
  }

  /**
   * Case - represents a case statement
   * 
   * A case statement performs a comparison against the switch value. Such a
   * comparison could be base on regular expression, glob matching or basic
   * string comparison or .. you name it.
   * 
   * This class acts as base class.
   * 
   * @author geronimo
   * 
   */
  static public abstract class Case extends Sequential
  {
    public boolean debug;
    public String var;
    public int flags;
    public boolean not = false;

    public void setDebug(boolean debug)
    {
      this.debug = debug;
    }

    public void setVar(String s)
    {
      this.var = Static.trim3(getProject(), s, this.var);
    }

    public void setIgnoreCase(boolean b)
    {
      this.flags = Static.bitset(this.flags, Pattern.CASE_INSENSITIVE, b);
    }

    public void setDotAll(boolean b)
    {
      this.flags = Static.bitset(this.flags, Pattern.DOTALL, b);
    }

    public void setUnixLines(boolean b)
    {
      this.flags = Static.bitset(this.flags, Pattern.UNIX_LINES, b);
    }

    public void setComments(boolean b)
    {
      this.flags = Static.bitset(this.flags, Pattern.COMMENTS, b);
    }

    public void setMultiLine(boolean b)
    {
      this.flags = Static.bitset(this.flags, Pattern.MULTILINE, b);
    }

    public void setNot(boolean b)
    {
      this.not = b;
    }

    protected boolean matches(Pattern regex, String value)
    {
      boolean r;
      Matcher M;

      M = regex.matcher(value);
      r = M.matches();
      if (r && this.var != null)
      {
        Static.assign(getProject(), this.var, new MatcherBean(M, 0), Static.VARREF);
      }
      if (this.debug)
      {
        String pattern = M.pattern().pattern();
        System.err.println("matching regex/pat |" + pattern + "| against |" + value + "| => " + r);
      }
      return r;
    }

    protected boolean find(Pattern regex, String value)
    {
      boolean r;
      Matcher M;

      M = regex.matcher(value);
      r = M.find();
      if (r && this.var != null)
      {
        Static.assign(getProject(), this.var, new MatcherBean(M, 0), Static.VARREF);
      }
      if (this.debug)
      {
        String pattern = M.pattern().pattern();
        System.err.println("matching regex/pat |" + pattern + "| against |" + value + "| => " + r);
      }
      return r;
    }

    public boolean tryvalue(@SuppressWarnings("unused") String value)
    {
      return false;
    }
  }

  /**
   * EQ Shall allow for textual comparison
   * */
  static protected class CMP extends Case
  {

    public String eq;
    public String lt;
    public String gt;

    public void setEq(String value)
    {
      this.eq = Static.trim3(getProject(), value, "");
    }

    public void setLt(String value)
    {
      this.lt = Static.trim3(getProject(), value, "");
    }

    public void setGt(String value)
    {
      this.gt = Static.trim3(getProject(), value, "");
    }

    /**
     * Try this value against this clause.
     */
    public boolean tryvalue(String value)
    {
      boolean r;

      if (this.eq == null && this.lt == null && this.gt == null)
        return this.not ? true : false;

      r = false;

      if (!r && this.eq != null)
      {
        if ((this.flags & Pattern.CASE_INSENSITIVE) != 0)
        {
          r = this.eq.compareToIgnoreCase(value) == 0;
        } else
        {
          r = this.eq.compareTo(value) == 0;
        }
      }
      if (!r && this.lt != null)
      {
        if ((this.flags & Pattern.CASE_INSENSITIVE) != 0)
        {
          r = value.compareToIgnoreCase(this.lt) < 0;
        } else
        {
          r = value.compareTo(this.lt) < 0;
        }
      }
      if (!r && this.gt != null)
      {
        if ((this.flags & Pattern.CASE_INSENSITIVE) != 0)
        {
          r = value.compareToIgnoreCase(this.gt) > 0;
        } else
        {
          r = value.compareTo(this.gt) > 0;
        }
      }
      return this.not ? !r : r;
    }
  }

  /** Case class */
  static protected final class Match extends Case
  {
    public String regexstr = null;
    public boolean ispattern = false;

    public void setRE(String value)
    {
      this.regexstr = Static.trim3(getProject(), value, this.regexstr);
      this.ispattern = false;
    }

    public void setPat(String value)
    {
      setGlob(value);
    }

    public void setGlob(String value)
    {
      this.regexstr = Static.trim3(getProject(), value, this.regexstr);
      this.ispattern = true;
    }

    protected boolean match(Pattern regex, String value)
    {
      boolean r;
      Matcher M;

      /* match it */
      M = regex.matcher(value);
      r = M.find();
      if (r && this.var != null)
      {
        Static.assign(getProject(), this.var, new MatcherBean(M, 0), Static.VARREF);
      }
      if (this.debug)
      {
        String pattern = M.pattern().pattern();
        System.err.println("matching regex/pat |" + pattern + "| against |" + value + "| => " + r);
      }
      return r;
    }

    /**
     * Try this value against this clause.
     */
    public boolean tryvalue(String value)
    {
      Pattern P = null;

      if (this.regexstr == null)
        return false;

      if (!this.ispattern)
      {
        try
        {
          P = Pattern.compile(this.regexstr, this.flags);
        } catch (Exception e)
        {
          P = Pattern.compile(Pattern.quote(this.regexstr), this.flags);
        }
      } else
      {
        String glob = Static.patternAsRegex(this.regexstr);
        P = Pattern.compile(glob, this.flags);
      }
      return match(P, value);
    }
  }

  public Switch02.RE createRE() throws BuildException
  {
    Switch02.RE res = new Switch02.RE();
    res.var = this.var;
    res.flags = this.flags;
    res.debug = this.debug;
    this.cases.add(res);
    return res;
  }

  public Switch02.GLOB createGLOB() throws BuildException
  {
    Switch02.GLOB res = new Switch02.GLOB();
    res.var = this.var;
    res.flags = this.flags;
    res.debug = this.debug;
    this.cases.add(res);
    return res;
  }

  public Switch02.CMP createCMP() throws BuildException
  {
    Switch02.CMP res = new Switch02.CMP();
    res.var = this.var;
    res.flags = this.flags;
    res.debug = this.debug;
    this.cases.add(res);
    return res;
  }

  /** deprecated */
  public Switch02.Match createMatches() throws BuildException
  {
    Switch02.Match res = new Switch02.Match();
    res.var = this.var;
    res.flags = this.flags;
    res.debug = this.debug;
    this.cases.add(res);
    return res;
  }

  /**
   * @param res
   * @throws BuildException
   */
  public void addDefault(Sequential res) throws BuildException
  {
    this.defaultcase = res;
  }

  /**
   * @param res
   * @throws BuildException
   */
  public void addOtherwise(Sequential res) throws BuildException
  {
    addDefault(res);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.tools.ant.Task#execute()
   */
  public void execute() throws BuildException
  {
    boolean b;
    Case c;
    Sequential s;
    String v;

    /* we need a value to do something .. */
    if (this.value == null)
    {
      debug("no switch value..");
      return;
    }

    if (this.cases.size() == 0 && this.defaultcase == null)
    {
      debug("no switch cases given ..");
      return;
    }

    b = false;
    c = null;
    /* resolve EL refs in input */
    v = Static.elresolve(getProject(), this.value);

    /* try each match case until success */
    for (int i = 0; !b && i < this.cases.size(); i++)
    {
      c = (Case) this.cases.get(i);
      b = c.tryvalue(v);
    }
    s = b ? c : this.defaultcase;
    if (s != null)
      s.perform();
  }

}
