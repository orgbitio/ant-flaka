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

package it.haefelinger.flaka.dep;

import it.haefelinger.flaka.util.Static;

import java.io.File;
import java.util.regex.Pattern;


import org.apache.tools.ant.types.selectors.BaseSelector;

public class Select extends BaseSelector
{
  final static protected short GLOB = 1;
  final static protected short REGEX = 2;

  private boolean needsinit = true;

  protected boolean invert = false;
  protected int flags = Pattern.CASE_INSENSITIVE;
  protected boolean glob = false;

  protected String alias_regex = null;
  protected String scope_regex = null;
  protected String bname_regex = null;
  protected String type_regex = null;
  protected String gid_regex = null;
  protected String path_regex = null;
  protected String version_regex = null;

  protected Pattern alias = null;
  protected Pattern scope = null;
  protected Pattern bname = null;
  protected Pattern type = null;
  protected Pattern gid = null;
  protected Pattern path = null;
  protected Pattern version = null;

  protected String errmsg = null;
  /* The reference holding all known dependencies */
  protected String refid = "project.dependencies";

  /**
   * Use attribute <code>refid</code> to change the reference holding all known
   * dependencies.
   * 
   * @param s
   */
  public void setRefid(String s)
  {
    this.refid = Static.trim2(s, this.refid);
  }

  /**
   * Use attribute <code>ref</code> to change the reference holding all known
   * dependencies.
   * 
   * @param s
   */
  public void setRef(String s)
  {
    setRefid(s);
  }

  public void setGlob(boolean b)
  {
    this.glob = b;
  }

  public void setInvert(boolean b)
  {
    this.invert = b;
  }

  public void setCaseSensitive(boolean b)
  {
    if (b)
    {
      // turn CASE_INSENSITIVE off
      this.flags &= ~Pattern.CASE_INSENSITIVE;
    } else
    {
      // turn CASE_INSENSITIVE on
      this.flags |= Pattern.CASE_INSENSITIVE;
    }
  }

  public void setIgnore(boolean b)
  {
    setCaseSensitive(!b);
  }

  public void setIgnoreCase(boolean b)
  {
    setIgnore(b);
  }

  public void setAlias(String s)
  {
    this.alias_regex = Static.trim2(s, this.alias_regex);
  }

  public void setName(String s)
  {
    setAlias(s);
  }

  public void setScope(String s)
  {
    this.scope_regex = Static.trim2(s, this.scope_regex);
  }

  public void setBasename(String s)
  {
    this.bname_regex = Static.trim2(s, this.bname_regex);
  }

  public void setType(String s)
  {
    this.type_regex = Static.trim2(s, this.type_regex);
  }

  public void setGroupid(String s)
  {
    this.gid_regex = Static.trim2(s, this.gid_regex);
  }

  public void setGroup(String s)
  {
    setGroupid(s);
  }

  public void setGroupname(String s)
  {
    setGroupid(s);
  }

  public void setPath(String s)
  {
    this.path_regex = s;
  }

  public void setVersion(String s)
  {
    this.version_regex = Static.trim2(s, this.version_regex);
  }

  /**
   * Init this instance. Needs to be called before real execution, i.e
   * {@link #isSelected} takes place.
   * <p>
   * The method translates regular expressions given as string into precompiled
   * pattern objects.
   */

  protected void init()
  {
    if (this.needsinit)
    {
      /* precompile regular expressions */
      this.alias = compile(this.alias_regex);
      this.scope = compile(this.scope_regex);
      this.bname = compile(this.bname_regex);
      this.type = compile(this.type_regex);
      this.gid = compile(this.gid_regex);
      this.path = compile(this.path_regex);
      this.version = compile(this.version_regex);
      /* we are done */
      this.needsinit = false;
    }
  }

  /**
   * return <code>s</code> as reference.
   */

  protected Object getref(String s)
  {
    return getProject().getReference(s);
  }

  protected Object getref()
  {
    return getref(this.refid);
  }

  /**
   * compile a string a regular expression pattern. In case of an error, the
   * internal variable <code>errmsg</code> is set.
   * 
   * @return nil if <code>s</code> can't be translated.
   */

  final protected Pattern compile(String S)
  {
    String s = S;
    Pattern P = null;
    if (s != null && s.trim().length() > 0)
    {
      try
      {
        if (this.glob)
        {
          s = Static.patternAsRegex(s);
          P = Pattern.compile(s, this.flags);
        } else
        {
          P = Static.patterncompile(s, this.flags);
        }
      } catch (Exception e)
      {
        this.errmsg = e.toString();
      }
    }
    return P;
  }

  final protected Dependency[] getdeps()
  {
    Object ref;
    /* lookup reference */
    ref = getref();

    if (ref == null)
    {
      return null;
    }

    if (!(ref instanceof Dependency[]))
    {
      return null;
    }

    return (Dependency[]) ref;
  }

  final protected Dependency haveDependency(String filename)
  {
    Dependency[] deps = getdeps();

    if (deps != null && filename != null)
    {
      String fname;

      for (int i = 0; i < deps.length; ++i)
      {
        fname = deps[i].basename();
        if (fname.equals(filename))
          return deps[i];
      }
    }
    return null;
  }

  /**
   * Match a string against a (precompiled) regular expression.
   * 
   * @param regex
   *          if null, <code>true</code> is returned regardless of
   *          <code>s</code>'s value.
   * 
   * @param s
   *          if <code>null</code> then true is returned if regex is null as
   *          well, otherwise false.
   * 
   */

  final protected boolean match(Pattern regex, String s)
  {
    boolean b;

    b = true;
    /* a empty regex matches everything */
    if (regex == null)
      return true;

    /* a empty string is only matched by an empty regex */
    if (s == null)
      return false;

    /* execute the match */
    b = regex.matcher(s).matches();

    return b;
  }

  /**
   * Implements a selector which can be used to restrict a given fileset. When
   * used without any attributes, a loc in a loc set is selected if that loc is
   * the basename of a dependency. Further checks are carried out if given. For
   * example, if a regular expression has been set to match the scope of a
   * dependency, then the matching dependency must also match that regular
   * expression.
   */
  public boolean isSelected(File basedir, String filename, File file)
  {
    Dependency d;
    boolean r;
    if (this.needsinit)
      init();
    /* find a dependency matching this filename */
    d = haveDependency(filename);
    r = (d != null) ? match(d) : false;
    return this.invert ? !r : r;
  }

  public boolean hasattribs()
  {
    if (this.alias != null)
      return true;

    if (this.scope != null)
      return true;

    if (this.bname != null)
      return true;

    if (this.type != null)
      return true;

    if (this.gid != null)
      return true;

    if (this.path != null)
      return true;

    return false;
  }

  /**
   * @param d
   *          not null
   */
  protected boolean match(Dependency d)
  {
    boolean r = true;

    /* match if not attributes given */
    if (hasattribs() == false)
      return r;

    if (r && this.alias != null)
      r = match(this.alias, d.getAlias());

    if (r && this.scope != null)
    {
      String[] scope;
      scope = d.getScope();
      r = false;
      for (int i = 0; !r && i < scope.length; ++i)
        r = match(this.scope, scope[i]);
    }

    if (r && this.bname != null)
      r = match(this.bname, d.basename());

    if (r && this.type != null)
      r = match(this.type, d.getType());

    if (r && this.gid != null)
      r = match(this.gid, d.getGroupId());

    if (r && this.path != null)
      r = match(this.path, d.m1path());

    if (r && this.version != null)
      r = match(this.version, d.getVersion());

    return r;
  }

}
