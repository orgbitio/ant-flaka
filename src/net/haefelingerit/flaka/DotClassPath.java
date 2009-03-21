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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.haefelingerit.flaka.dep.Dependency;
import net.haefelingerit.flaka.util.Static;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.AbstractFileSet;
import org.apache.tools.ant.types.FileSet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A task to execute operations on a .classpath loc.
 * 
 * This task implements opertations like append, remove and query on a
 * .classpath loc.
 * 
 * @author <a href="mailto:flaka (at) haefelingerit (dot) net">Wolfgang
 *         H&auml;felinger</a>
 */

public class DotClassPath extends Task
{
  /** internal list of (logical) classpath entries */
  protected List list = new ArrayList();
  /** the loc to operate on */
  protected File file = new File(".classpath");
  /** my base folder */
  protected File base = null;
  /** Eclipse's workspace */
  protected File wsfolder = null;
  /** sort content */
  protected boolean sort = true;
  /** operation */
  protected char opc = 'u'; /* u=update,r=remove,q=query */
  /** dump content */
  protected boolean echo = false;
  /** query result var */
  protected String qvar = null;
  /** dependencies to be used */
  protected String refid = "project.dependencies";
  /** query logic */
  protected int logic = DotClassPath.AND;

  static final public int AND = 0;
  static final public int OR = 1;

  /**
   * Use this attribute to specify the query logic. By default each entry must
   * match. When changing to "or", it is sufficient that one entry matches.
   * 
   * @param s
   *          Shall not be null. The string value will be normalized, then
   *          tested for the following values in a case insensitive manner:
   *          "and", "&" and "&&" equals to logical and, while "or","|" or "||"
   *          gives the logical or. If the given values does not match, the
   *          default value gets applied.
   */

  public void setLogic(String s)
  {
    if (s.matches("(?i)\\s*(?:and|&|&&)\\s*"))
    {
      this.logic = DotClassPath.AND;
      return;
    }
    if (s.matches("(?i)\\s*(?:or|\\||\\|\\|)\\s*"))
    {
      this.logic = DotClassPath.OR;
      return;
    }
  }

  /**
   * Use attribute refid to specify another variable holding an array of
   * Dependencies. This dependencies are used to figure out the project ( if
   * known) of a lib entry. Default: project.dependencies.
   * 
   * @param s
   *          null allowed
   */
  public void setRefId(String s)
  {
    this.refid = Static.trim2(s, this.refid);
  }

  /**
   * This attribute will be set if a query evaluates to true. If a name is
   * given, property ${name} is set to true. Note that the property is not set
   * if already defined. There is no default value.
   * 
   * @param s
   *          The name of the property to set if a query evaluates to true.
   */
  public void setVar(String s)
  {
    this.qvar = Static.trim2(s, this.qvar);
  }

  /**
   * Use this attribute to dump the content of the .classpath loc on stdout. By
   * default, content is not dumped.
   * 
   * @param b
   *          If true, dump the content of .classpath.
   */
  public void setEcho(boolean b)
  {
    this.echo = b;
  }

  /**
   * Use this attribute to specify the operation to execute. Use 'a' or 'u' to
   * execute an update command, use 'r' for removal and use 'q' for an query.
   * Default value is 'a'.
   * 
   * @param s
   */
  public void setOp(String s)
  {
    String p = Static.trim2(s, null);
    if (p != null)
    {
      this.opc = p.charAt(0);
    }
  }

  /**
   * Use this attribute to set the loc to operate on. By default, .classpath is
   * used.
   * 
   * @param loc
   *          The loc to operate on.
   */
  public void setFile(File file)
  {
    this.file = file;
  }

  /**
   * Use this attribute to specify the base directory. By default, the base
   * directory is the folder containing the build loc.
   * 
   * @param loc
   *          The base folder.
   */
  public void setBase(File file)
  {
    this.base = file;
  }

  /**
   * Use this attribute to specify the workspace folder of Eclipse. If not
   * given, then the workspace folder will be the parent of base directory.
   * 
   * @param loc
   *          Eclipse workspace folder.
   */
  public void setWsFolder(File file)
  {
    this.wsfolder = file;
  }

  /**
   * Use this attribute to sort the final .classpath. This attribute has no
   * effect when executing a query.
   */
  public void setSort(boolean b)
  {
    this.sort = b;
  }

  /**
   * This attribute is kept for legacy reasons.
   * 
   * @deprecated
   * @param b
   */
  public void setUnique(boolean b)
  {
    /* deprecated but kept for legacy reasons */
    debug("usage of attribute 'unique' is deprecated");
  }

  /**
   * This attribute is kept for legacy reasons.
   * 
   * @deprecated
   * @param b
   */
  public void setValidate(boolean b)
  {
    /* deprecated but kept for legacy reasons */
    debug("usage of attribute 'validate' is deprecated");
  }

  /**
   * This attribute is kept for legacy reasons.
   * 
   * @deprecated
   * @param b
   */
  public void setFlush(boolean b)
  {
    /* deprecated but kept for legacy reasons */
    debug("usage of attribute 'flush' is deprecated");
  }

  /**
   * This attribute is kept for legacy reasons.
   * 
   * @deprecated
   * @param b
   */

  /**
   * @param s
   */
  public void setSrcDir(String s)
  {
    /* deprecated but kept for legacy reasons */
    debug("usage of attribute 'srcdir' is deprecated");
  }

  /**
   * This attribute is kept for legacy reasons.
   * 
   * @deprecated
   * @param b
   */

  /**
   * @param s
   */
  public void setResDir(String s)
  {
    /* deprecated but kept for legacy reasons */
    debug("usage of attribute 'resdir' is deprecated");
  }

  /**
   * A helper function to add an XML like attribute to a string buffer.
   * 
   * @param buf
   *          not null
   * @param name
   *          (attribute name)
   * @param v
   *          (attribute value)
   */
  static protected void attr2buf(StringBuffer buf, String v, String name)
  {
    if (name != null && v != null)
    {
      buf.append(" ");
      buf.append(name);
      buf.append("=\"");
      buf.append(v);
      buf.append("\"");
    }
  }

  /**
   * Interface representing a logical classpath entry. Such a logical entry is
   * for example a fileset which, when being evaluated (@see #eval), resolves
   * into "real" classpath entries (@see #ClassPathEntry).
   */
  public interface LogicalClassPathEntry
  {
    public String toString();

    public void eval(List bucket);
  }

  /**
   * A class representing a "real" classpathentry element.
   */
  final public class ClassPathEntry implements LogicalClassPathEntry, Comparable
  {
    protected String path;
    protected String kind;
    protected String excluding;
    protected String output, sourcepath;
    protected String protec, epop;
    protected Dependency dependency;

    public void setKind(String s)
    {
      this.kind = Static.trim2(s, null);
    }

    public void setPath(String s)
    {
      this.path = Static.trim2(s, null);
    }

    public void setExcluding(String s)
    {
      this.excluding = Static.trim2(s, null);
    }

    public void setOutput(String s)
    {
      this.output = Static.trim2(s, null);
    }

    public void setSourcePath(String s)
    {
      this.sourcepath = Static.trim2(s, null);
    }

    public void setProtected(String s)
    {
      this.protec = Static.trim2(s, null);
    }

    public void setEpop(String s)
    {
      this.epop = Static.trim2(s, null);
    }

    /**
     * This method is used to represent a entry for debugging purposes. Do not
     * use this method to generate XML (or risk to get a not wellformed XML
     * document).
     */
    public String toString()
    {
      StringBuffer b;
      b = new StringBuffer();
      b.append("{classpathentry:");
      attr2buf(b, this.kind, "kind");
      attr2buf(b, this.path, "path");
      attr2buf(b, this.excluding, "excluding");
      attr2buf(b, this.sourcepath, "sourcepath");
      attr2buf(b, this.protec, "protected");
      b.append("}");
      return b.toString();
    }

    /**
     * Match a regular expression or pattern against a given value.
     * 
     * A regular expression is given, if regpat's value starts and ends with the
     * very same character (after trimming). In such a case, the first and last
     * character do not form an integral part of the regular expr.
     * 
     * If first and last character differ, then regpat is considered a pattern.
     * A pattern is a simplified regular expression, easier to use but also less
     * powerfull.
     * 
     * 
     * Examples: Regular expressions: /.+/ or %(a|b)% Patterns: *.jar *.*
     * 
     * @param regpat
     *          maybe null
     * @param value
     *          maybe null
     * @return true if matching
     */

    protected boolean match(String id, String regpat, String value)
    {
      boolean r;
      r = match(regpat, value);
      if (DotClassPath.this.debug)
        System.err.print("id=" + id + " regpat:" + regpat + " value:" + value + ":" + r);
      return r;
    }

    protected boolean match(String Regpat, String value)
    {
      boolean r = false;
      String regpat = Regpat;
      /*
       * if first and last char of regpat are equal, then regpat is a true
       * regular expression (except those two chars), otherwise regpat is a
       * pattern expression.
       */

      if (regpat == null)
      {
        r = value == null;
        return r;
      }
      if (value == null)
      {
        r = false;
        return r;
      }

      int sz = regpat.length();

      /* handle trivial case where regpat is empty */
      if (sz <= 0)
      {
        r = value.length() == sz;
      }

      /* regpat not empty here */
      if (sz <= 2)
        regpat = Static.patternAsRegex(regpat);
      else
      {
        char c1, c2;

        c1 = regpat.charAt(0);
        c2 = regpat.charAt(sz - 1);

        if (c1 != c2)
          regpat = Static.patternAsRegex(regpat);
        else
          regpat = regpat.substring(1, sz - 1);
      }

      /* compare by regular expression */
      r = value.matches(regpat);
      return r;
    }

    /**
     * Predicate to check whether a classpath entry is protected.
     * 
     * A entry is considered protected if having an attribute named protected
     * and a value of either true or 1 (ignoring whitespace).
     * 
     * @return true if protected.
     */
    protected boolean isprotected()
    {
      boolean r = false;
      if (this.protec != null)
      {
        r = this.protec.matches("\\s*(1|true)\\s*");
      }
      return r;
    }

    /**
     * Check whether this classpath entry matches a given one.
     * 
     * The values of this classpath entry are interpreted as regular expressions
     * or patterns.
     * 
     * @param c
     * @return true if this entry matches a given one. If c is null, false is
     *         returned.
     */
    public boolean match(ClassPathEntry c)
    {
      boolean r = false;
      r = c != null && (this.kind != null ? match("k", this.kind, c.kind) : true)
          && (this.path != null ? match("p", this.path, c.path) : true)
          && (this.excluding != null ? match("e", this.excluding, c.excluding) : true)
          && (this.output != null ? match("o", this.output, c.output) : true)
          && (this.sourcepath != null ? match("s", this.sourcepath, c.sourcepath) : true)
          && (this.epop != null ? match("P", this.epop, c.epop) : true);
      if (DotClassPath.this.debug)
      {
        System.err.println("matching:" + this + "," + c + ":" + r);
      }
      return r;
    }

    protected String get(String kind)
    {
      if (kind.equals("kind"))
        return this.kind;
      if (kind.equals("path"))
        return this.path;
      if (kind.equals("excluding"))
        return this.excluding;
      if (kind.equals("output"))
        return this.output;
      if (kind.equals("sourcepath"))
        return this.sourcepath;
      if (kind.equals("epop"))
        return this.epop;
      return null;
    }

    /**
     * Check whether this entry and another entry are equal when comparing
     * attribute kind only.
     * 
     * This method handles attribute path is a special way, i.e different path
     * variations are taken into account by trying to compare using the
     * canonical path representation. If not possible, then a standard
     * comparison is tried.
     * 
     * @param other
     *          not null
     * @param kind
     *          not null
     * @return true if equal
     */
    protected boolean iseq(ClassPathEntry other, String kind)
    {
      boolean r = false;
      String ts = get(kind);
      String os = other.get(kind);

      if (ts == null || os == null)
        return (ts == os);

      if (ts.equals(os))
        return true;

      if (kind.equals("path"))
      {
        File tf, of;
        tf = new File(ts);
        of = new File(os);
        try
        {
          r = tf.getCanonicalPath().equals(of.getCanonicalPath());
        } catch (Exception e)
        {
          if (DotClassPath.this.debug)
            System.err.println("**exception: comparing paths via absolute path names ..");
          r = tf.equals(of);
        }
      }
      return r;
    }

    /**
     * Test whether the src and lib attribute are equal. This might be the case
     * if the path of src points to a folder containing the source code of
     * project P and if lib's path contains an artefact generated by that
     * project.
     * 
     * @param lib
     * @param src
     * @return true if attribute src and lib are should be treated equal
     */
    protected boolean cmplibsrc(ClassPathEntry lib, ClassPathEntry src)
    {
      boolean r = false;

      if (lib.path == null || src.path == null)
        return (lib.path == src.path);
      return r;
    }

    /**
     * Test whether this classpath equals a given object.
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj)
    {
      ClassPathEntry other = (ClassPathEntry) obj;
      if (iseq(other, "kind") && iseq(other, "path"))
        return true;
      if (cmplibsrc(this, other) || cmplibsrc(other, this))
        return true;
      return false;
    }

    /**
     * Test whether a classpath entry is less, equal or larger than a given
     * object.
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object obj)
    {
      int r;
      ClassPathEntry other = (ClassPathEntry) obj;

      /* If we are equal, return 0 */
      if (equals(obj))
        return 0;
      /* Invariant: this.xxx != other.xxx */
      if (this.kind == null || other.kind == null)
        return this.kind == null ? -1 : 1;

      r = this.kind.compareTo(other.kind);
      if (r != 0)
        return r;

      if (this.path == null || other.path == null)
        return this.path == null ? -1 : 1;
      r = this.path.compareTo(other.path);
      if (r != 0)
        return r;
      if (this.excluding == null || other.excluding == null)
      {
        r = (this.excluding == other.excluding) ? 0 : -1;
        r = (r == 0) ? r : (this.excluding == null) ? -1 : 1;
        return r;
      }
      r = this.excluding.compareTo(other.excluding);
      return r;
    }

    /**
     * Evaluate this classpath entry and add the evaluated instance to a given
     * list.
     * 
     * This evaluation step decorates this entry with a dependency if this entry
     * is known to match a dependency in the array given by refid. If the
     * dependency is known to be a EPO project, then attribute epop is also
     * decorated.
     * 
     * @param bucket
     *          not null
     */
    public void eval(List bucket)
    {
      if (this.path != null)
      {
        try
        {
          Dependency[] deps;
          String basename;
          deps = (Dependency[]) getProject().getReference(DotClassPath.this.refid);
          basename = new File(this.path).getName();
          for (int i = 0; (deps != null) && i < deps.length; ++i)
          {
            String s = deps[i].basename();
            if (s != null && s.equals(basename))
            {
              this.dependency = deps[i];
              break;
            }
          }
        } catch (ClassCastException e0)
        {
          if (DotClassPath.this.debug)
            System.err.println("variable \"" + DotClassPath.this.refid
                + "\" not pointing to dependency array object");
        } catch (Exception e)
        {
          if (DotClassPath.this.debug)
            System.err.println("**exception: " + e);
        }
      }
      bucket.add(this);
    }
  }

  /**
   * To be used to create a list of 'lib->path' entries
   */
  final public class FileSetEntry implements LogicalClassPathEntry
  {
    public AbstractFileSet fileset;

    public FileSetEntry(AbstractFileSet fileset)
    {
      this.fileset = fileset;
    }

    public String toString()
    {
      StringBuffer sb;
      ArrayList bucket;

      bucket = new ArrayList();
      sb = new StringBuffer();

      this.eval(bucket);
      for (int i = 0; i < bucket.size(); i++)
      {
        sb.append("\n  ");
        sb.append(bucket.get(i).toString());
      }
      return sb.toString();
    }

    /**
     * Evaluate this fileset and create instance of type
     * 
     * @Entry for each resolved loc. Each
     * @Entry instance will be appended to
     * @bucket.
     * 
     * @param bucket
     *          not null, resolved entries appended
     */
    public void eval(List bucket)
    {
      ClassPathEntry c;
      DirectoryScanner S;
      String[] files;
      String base;

      S = this.fileset.getDirectoryScanner();
      files = S.getIncludedFiles();
      base = S.getBasedir().getAbsolutePath().replace('\\', '/');

      for (int i = 0; i < files.length; i++)
      {
        c = new ClassPathEntry();
        c.setKind("lib");
        c.setPath(base + "/" + files[i]);
        c.eval(bucket);
      }
    }
  }

  public ClassPathEntry createClassPathEntry() throws BuildException
  {
    ClassPathEntry entry = new ClassPathEntry();
    this.list.add(entry);
    return entry;
  }

  /**
   * Returns current base folder given by property <tt>basedir</tt>. This
   * property is set by Ant on starting up and is the folder containing the
   * build script being executed.
   */

  protected File basedir()
  {
    return new File(getProject().getProperty("basedir"));
  }

  /* allows to add just an arbitrary fileset */
  public void addFileSet(FileSet item) throws BuildException
  {
    FileSetEntry entry;
    if (item != null)
    {
      entry = new FileSetEntry(item);
      this.list.add(entry);
    }
  }

  /**
   * Iterates over each logical classpath entries and evaluates each entry into
   * a physical entries. Those physical entries are appended to given bucket
   * list.
   */
  protected void eval(List bucket)
  {
    LogicalClassPathEntry entry;

    if (bucket != null)
    {
      for (int j = 0; j < this.list.size(); ++j)
      {
        entry = (LogicalClassPathEntry) this.list.get(j);
        entry.eval(bucket);
      }
    }
  }

  /**
   * This method will render a string representation of all physical classpath
   * entries. It will not render a .classpath loc nor does it update or touch
   * any loc. It's main purpose is for debugging.
   */
  public String toString()
  {
    LogicalClassPathEntry entry;
    StringBuffer buf;
    List bucket;
    String s;

    buf = new StringBuffer();
    bucket = new ArrayList();

    /* Collect entries */
    eval(bucket);

    for (int i = 0; i < bucket.size(); ++i)
    {
      entry = (ClassPathEntry) bucket.get(i);
      s = entry.toString();
      if (s != null && s.trim().length() > 0)
      {
        buf.append("\n  ");
        buf.append(s);
      }
    }
    return buf.toString();
  }

  /**
   * * Shall return true for a dependency created by an internal project.
   */
  /**
   * @param d
   * @return
   */
  static boolean isInternal(ClassPathEntry d)
  {
    return false;
  }

  /**
   * * Shall return the name of the project which generated dependency
   * 
   * @d.
   */
  /**
   * @param d
   * @return
   */
  static String projectname(ClassPathEntry d)
  {
    return null;
  }

  /**
   * Iterates of each child element of root, turing each into a classpath entry
   * and adding such an entry to bucket.
   */
  protected void collect(Element root, List bucket)
  {
    if (root != null && bucket != null)
    {
      Element e;
      NodeList kids;
      ClassPathEntry c;

      kids = root.getChildNodes();
      for (int i = 0; i < kids.getLength(); ++i)
      {
        try
        {
          e = (Element) kids.item(i);
          if (!e.getTagName().equals("classpathentry"))
            continue;
          c = new ClassPathEntry();
          if (e.hasAttribute("kind"))
          {
            c.setKind(e.getAttribute("kind"));
          }
          if (e.hasAttribute("path"))
          {
            c.setPath(e.getAttribute("path"));
          }
          if (e.hasAttribute("excluding"))
          {
            c.setExcluding(e.getAttribute("excluding"));
          }
          if (e.hasAttribute("output"))
          {
            c.setOutput(e.getAttribute("output"));
          }
          if (e.hasAttribute("sourcepath"))
          {
            c.setSourcePath(e.getAttribute("sourcepath"));
          }
          if (e.hasAttribute("protected"))
          {
            c.setProtected(e.getAttribute("protected"));
          }
          c.eval(bucket);
        } catch (Exception ex)
        {
          /* ignore */
        }
      }
    }
  }

  protected void update(ClassPathEntry c, Element e)
  {
    if (e != null && c != null)
    {
      if (c.kind != null)
      {
        e.setAttribute("kind", c.kind);
      }
      if (c.path != null)
      {
        String path = c.path.replace('\\', '/');
        if (this.base != null)
        {
          String base;
          try
          {
            base = this.base.getCanonicalPath();
          } catch (Exception ex)
          {
            base = this.base.getAbsolutePath();
          }
          base = base.replace('\\', '/') + "/";
          if (path.startsWith(base))
            path = path.substring(base.length());
        }
        e.setAttribute("path", path);
      }
      if (c.excluding != null)
      {
        e.setAttribute("excluding", c.excluding);
      }
      if (c.output != null)
      {
        e.setAttribute("output", c.output);
      }
      if (c.sourcepath != null)
      {
        e.setAttribute("sourcepath", c.sourcepath);
      }
      if (c.protec != null)
      {
        e.setAttribute("protected", c.protec);
      }
      if (c.epop != null)
      {
        e.setAttribute("epop", c.epop);
      }
    }
  }

  /**
   * Check whether this classpath entry is acceptable for Eclipse.
   * 
   * Eclipse is rather fuzzy about entries listed.
   * 
   * @param c
   *          not null
   * @return true if well formed.
   */

  protected boolean good(ClassPathEntry c)
  {
    File file = null;
    boolean r = true;

    /* A entry without a path element appears not acceptable. */
    if (c.path == null || c.path.matches("\\s*"))
      return false;

    /* Every entry must have a kind */
    if (c.kind == null || c.kind.matches("\\s*"))
      return false;

    if (!c.kind.equals("con"))
    {
      file = new File(c.path);
      r = file.exists();
      // Entries starting with a "/" are interpreted by Eclipse as being
      // relative
      // to the workspace. If no workspace folder is given, we assume that the
      if (!r && this.wsfolder != null)
      {
        file = new File(this.wsfolder, c.path);
        r = file.exists();
      }
      if (!r && this.base != null)
      {
        file = new File(this.base, "..");
        file = new File(file, c.path);
        r = file.exists();
      }
    }
    if (!r && this.debug)
    {
      System.err.println("** classpath entry invalid: " + c);
    }
    return r;
  }

  protected boolean contains(List Q, ClassPathEntry c)
  {
    boolean r = false;
    if (Q.contains(c))
    {
      if (this.debug)
      {
        int idx = Q.indexOf(c);
        Object obj = Q.get(idx);
        System.err.println("entry " + c + " already seen (" + obj + ")");
      }
      r = true;
    }
    return r;
  }

  /**
   * * Update XML element by adding classpath entries. * *
   * 
   * @param root
   *          can be null
   */
  protected void opUpdate(List clazzpath) throws BuildException
  {
    if (clazzpath != null && this.list != null && this.list.size() > 0)
    {
      ClassPathEntry c;

      for (int i = 0; i < this.list.size(); i++)
      {
        c = (ClassPathEntry) this.list.get(i);
        if (good(c) && !contains(clazzpath, c))
        {
          clazzpath.add(c);
        }
      }
    }
  }

  protected void opQuery(List clazzpath) throws BuildException
  {
    boolean m = true;

    if (this.debug)
    {
      String logic = "unkown";
      switch (this.logic)
      {
        case AND:
        {
          logic = "and";
          break;
        }
        case OR:
        {
          logic = "or";
          break;
        }
      }
      System.err.println("applying query logic: " + logic);
    }

    if (clazzpath != null && this.list != null)
    {
      ClassPathEntry c, x;
      boolean b;

      for (int i = 0; i < this.list.size(); ++i)
      {
        x = (ClassPathEntry) this.list.get(i);
        m = false;
        for (int j = 0; !m && j < clazzpath.size(); ++j)
        {
          c = (ClassPathEntry) clazzpath.get(j);
          b = x.match(c);
          if (this.debug)
            System.err.println("matching:" + x + "," + c + ":" + b);
          m = m || b;
        }
        if ((m && this.logic == OR) || (!m && this.logic == AND))
          break;
      }
    }
    if (m)
    {
      getProject().setProperty(this.qvar, "true");
    }
  }

  protected void opRemove(List clazzpath) throws BuildException
  {
    Object[] array;
    ClassPathEntry c, x;
    boolean b;

    array = clazzpath.toArray();
    clazzpath.clear();

    for (int i = 0; i < array.length; ++i)
    {
      c = (ClassPathEntry) array[i];
      b = false;

      for (int j = 0; !b && j < this.list.size(); ++j)
      {
        x = (ClassPathEntry) this.list.get(j);
        b = x.match(c);
        if (this.debug)
        {
          System.err.println("matching:" + x + "," + c + ":" + b);
        }
      }

      /* if matching but protected, do not remove */
      if (b && c.isprotected())
      {
        if (this.debug)
        {
          System.err.println("not removing protected entry:" + c);
        }
        clazzpath.add(c);
      }
      if (!b)
        clazzpath.add(c);
      else
      {
        if (this.debug)
        {
          System.err.println("removing entry:" + c);
        }
      }
    }
  }

  public void execute() throws BuildException
  {
    Project P;
    Element root = null;
    Document d;
    Element e;
    ClassPathEntry c;

    P = getProject();

    /* Set my basedir if not set */
    if (this.base == null)
    {
      this.base = basedir();
    }

    /* Validate */
    switch (this.opc)
    {
      case 'u':
      case 'a':
      case 'r':
        break;
      case 'q':
        if (this.qvar == null)
        {
          warn("variable name required for query operation");
          return;
        }
        break;
      default:
        warn("operation '" + this.opc + "' unsupported");
        return;
    }

    /* Load XML document */
    root = Static.loadxml(P, this.file);
    if (root == null)
      return;

    /* Translate .classpath into list of classpath entries */
    List clazzpath = new ArrayList();
    collect(root, clazzpath);

    /* evaluate own body */
    List bucket;
    bucket = new ArrayList();
    eval(bucket);
    this.list = bucket;

    switch (this.opc)
    {
      case 'u':
      case 'a':
        opUpdate(clazzpath);
        break;
      case 'r':
        opRemove(clazzpath);
        break;
      case 'q':
        opQuery(clazzpath);
        break;
    }

    switch (this.opc)
    {
      case 'a':
      case 'u':
      case 'r':
      {
        d = root.getOwnerDocument();
        Node nl = d.createTextNode("\n");

        if (this.sort)
          Collections.sort(clazzpath);

        /* remove all elements */
        while (root.hasChildNodes())
        {
          root.removeChild(root.getFirstChild());
        }
        /* update root with my elements */
        for (int i = 0; i < clazzpath.size(); ++i)
        {
          c = (ClassPathEntry) clazzpath.get(i);
          e = d.createElement("classpathentry");
          update(c, e);
          root.appendChild(d.createTextNode("\n  "));
          root.appendChild(e);
        }
        if (clazzpath.size() > 0)
          root.appendChild(nl);

        // Flush my node as XML document to loc
        Static.flushxml(P, root, this.file);
      }
    }

    if (this.echo)
      Static.cat(P, this.file);
  }
}
