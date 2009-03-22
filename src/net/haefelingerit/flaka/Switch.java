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

package net.haefelingerit.flaka;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.haefelingerit.flaka.util.Static;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Sequential;

/**
 * @author merzedes
 * @since 1.0
 */
public class Switch extends Task
{
  protected String value;
  protected List cases = new ArrayList();
  protected Sequential defaultcase;
  protected String var;
  protected int flags;
  protected boolean find = false;

  public void setVar(String s)
  {
    this.var = Static.trim3(getProject(), s, this.var);
  }

  
 
  public void setIgnoreCase(boolean b)
  {
    this.flags = Static.bitset(this.flags,Pattern.CASE_INSENSITIVE,b);
  }

  public void setDotAll(boolean b)
  {
    this.flags = Static.bitset(this.flags,Pattern.DOTALL,b);
  }

  public void setUnixLines(boolean b)
  {
    this.flags = Static.bitset(this.flags,Pattern.UNIX_LINES,b);
  }

  public void setComments(boolean b)
  {
    this.flags = Static.bitset(this.flags,Pattern.COMMENTS,b);
  }

  public void setMultiLine(boolean b)
  {
    this.flags = Static.bitset(this.flags,Pattern.MULTILINE,b);
  }

  public void setFind(boolean b)
  {
    this.find = b;
  }

  public void setValue(String value)
  {
    this.value = Static.trim3(getProject(), value, this.value);
  }

  /** Case class */
  static protected final class Match extends Sequential
  {
    public String var;
    public String regexstr = null;
    public int flags;
    public boolean find;
    public boolean debug;
    public boolean ispattern = false;

    @SuppressWarnings("unused")
    private Match()
    {
      /* make sure this one is not used */
    }

    public Match(String var)
    {
      super();
      this.var = var;
    }

    public void setRE(String value)
    {
      this.regexstr = Static.trim3(getProject(), value, this.regexstr);
      this.ispattern = false;
    }

    public void setPat(String value)
    {
      this.regexstr = Static.trim3(getProject(), value, this.regexstr);
      this.ispattern = true;
    }

    public void setFind(boolean b)
    {
      this.find = b;
    }

    public void setIgnoreCase(boolean b)
    {
      this.flags = Static.bitset(this.flags,Pattern.CASE_INSENSITIVE,b);
    }

    public void setDotAll(boolean b)
    {
      this.flags = Static.bitset(this.flags,Pattern.DOTALL,b);
    }

    public void setUnixLines(boolean b)
    {
      this.flags = Static.bitset(this.flags,Pattern.UNIX_LINES,b);
    }

    public void setComments(boolean b)
    {
      this.flags = Static.bitset(this.flags,Pattern.COMMENTS,b);
    }

    public void setMultiLine(boolean b)
    {
      this.flags = Static.bitset(this.flags,Pattern.MULTILINE,b);
    }

    protected boolean match(Pattern regex, String value)
    {
      boolean r;
      Matcher M;

      /* match it */
      M = regex.matcher(value);
      r = this.find ? M.find() : M.matches();
      if (r)
      {
        Static.assign(getProject(), this.var, M, Static.VARREF);
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

  public Switch.Match createMatches() throws BuildException
  {
    Switch.Match res = new Switch.Match(this.var);
    res.flags = this.flags;
    res.find = this.find;
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
    Match c;
    Sequential s;

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
    for (int i = 0; !b && i < this.cases.size(); i++)
    {
      c = (Match) this.cases.get(i);
      b = c.tryvalue(this.value);
    }
    s = b ? c : this.defaultcase;
    if (s != null)
      s.perform();
  }

}
