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
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import net.haefelingerit.flaka.util.Static;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Sequential;

/**
 * A wrapper that lets you run a set of tasks and optionally run a different set
 * of tasks if the first set fails and yet another set after the first one has
 * finished.
 * 
 * <p>
 * This mirrors Java's try/catch/finally.
 * </p>
 * 
 * <p>
 * The tasks inside of the required <code>&lt;try&gt;</code> element will be
 * run. If one of them should throw a
 * {@link org.apache.tools.ant.BuildException BuildException} several things can
 * happen:
 * </p>
 * 
 * <ul>
 * <li>If there is no <code>&lt;catch&gt;</code> block, the exception will be
 * passed through to Ant.</li>
 * 
 * <li>If the property attribute has been set, a property of the given name will
 * be set to the message of the exception.</li>
 * 
 * <li>If the reference attribute has been set, a reference of the given id will
 * be created and point to the exception object.</li>
 * 
 * <li>If there is a <code>&lt;catch&gt;</code> block, the tasks nested into it
 * will be run.</li>
 * </ul>
 * 
 * <p>
 * If a <code>&lt;finally&gt;</code> block is present, the task nested into it
 * will be run, no matter whether the first tasks have thrown an exception or
 * not.
 * </p>
 * 
 * <p>
 * <strong>Attributes:</strong>
 * </p>
 * 
 * <table>
 * <tr>
 * <td>Name</td>
 * <td>Description</td>
 * <td>Required</td>
 * </tr>
 * <tr>
 * <td>property</td>
 * <td>Name of a property that will receive the message of the exception that
 * has been caught (if any)</td>
 * <td>No</td>
 * </tr>
 * <tr>
 * <td>reference</td>
 * <td>Id of a reference that will point to the exception object that has been
 * caught (if any)</td>
 * <td>No</td>
 * </tr>
 * </table>
 * 
 * <p>
 * Use the following task to define the <code>&lt;trycatch&gt;</code> task
 * before you use it the first time:
 * </p>
 * 
 * <pre>
 * &lt;code&gt;
 *   &lt;taskdef name=&quot;trycatch&quot; 
 *            classname=&quot;net.sf.antcontrib.logic.TryCatchTask&quot; /&gt;
 * &lt;/code&gt;
 * </pre>
 * 
 * <h3>Crude Example</h3>
 * 
 * <pre>
 * &lt;code&gt;
 * &lt;trycatch property=&quot;foo&quot; reference=&quot;bar&quot;&gt;
 *   &lt;try&gt;
 *     &lt;fail&gt;Tada!&lt;/fail&gt;
 *   &lt;/try&gt;
 *   &lt;catch&gt;
 *     &lt;echo&gt;In &amp;lt;catch&amp;gt;.&lt;/echo&gt;
 *   &lt;/catch&gt;
 *   &lt;finally&gt;
 *     &lt;echo&gt;In &amp;lt;finally&amp;gt;.&lt;/echo&gt;
 *   &lt;/finally&gt;
 * &lt;/trycatch&gt;
 * &lt;echo&gt;As property: ${foo}&lt;/echo&gt;
 * &lt;property name=&quot;baz&quot; refid=&quot;bar&quot; /&gt;
 * &lt;echo&gt;From reference: ${baz}&lt;/echo&gt;
 * &lt;/code&gt;
 * </pre>
 * 
 * <p>
 * results in
 * </p>
 * 
 * <pre>
 * &lt;code&gt;
 *   [trycatch] Caught exception: Tada!
 *       [echo] In &lt;catch&gt;.
 *       [echo] In &lt;finally&gt;.
 *       [echo] As property: Tada!
 *       [echo] From reference: Tada!
 * &lt;/code&gt;
 * </pre>
 * 
 * @author merzedes
 * @since 1.0
 */

public class TryCatch extends Task
{
  final static public String REFERENCE = "trycatch.object";
  protected List trylist = new ArrayList();
  protected List catchlist = new ArrayList();
  protected List finallylist = new ArrayList();
  protected String property = null;
  protected String reference = REFERENCE;

  /**
   * A helper class implementing a catch clause.
   * 
   * The catch clause is a regular task container. It allows further for the
   * specification of a type and a pattern to catch a particular exception.
   * 
   * @author wh81752
   * 
   */
  public static final class CatchBlock extends Sequential
  {
    protected Pattern match = null;
    protected Pattern type = null;

    public CatchBlock()
    {
      super();
      setMatch("*");
      setType("*.BuildException");
    }

    /**
     * Set the name of the Java class to be catched.
     * 
     * By default, only exceptions of type BuildException are caught.
     */
    public void setType(String type)
    {
      String s = Static.trim2(type, null);
      if (s != null)
        this.type = Static.patterncompile(s, 0);
    }

    /**
     * The pattern expession to use to select a particular exception.
     */
    public void setMatch(String match)
    {
      String s = Static.trim2(match, null);
      if (s != null)
        this.match = Static.patterncompile(s, Pattern.DOTALL);
    }

    public Class loadClass(String s)
    {
      Class c = null;
      try
      {
        c = Thread.currentThread().getContextClassLoader().loadClass(s);
      } catch (Exception e)
      {
        /* ignore */
      }
      return c;
    }

    public boolean match(Throwable t)
    {
      boolean r = false;
      String clazz = t.getClass().getName();
      String mesg = t.getMessage();
      if (clazz == null)
        clazz = "";
      if (mesg == null)
        mesg = "";
      r = this.type.matcher(clazz).matches();
      if (r)
        r = this.match.matcher(mesg).matches();
      return r;
    }
  }

  /**
   * A a sequential task container to the list of tries.
   * 
   * @param seq
   */
  public void addTry(Sequential seq)
  {
    this.trylist.add(seq);
  }

  /**
   * Add a catch clause.
   * 
   * @param cb
   */
  public void addCatch(CatchBlock cb)
  {
    this.catchlist.add(cb);
  }

  /**
   * Adds a finally clause.
   * 
   * @param seq
   */
  public void addFinally(Sequential seq)
  {
    this.finallylist.add(seq);
  }

  /**
   * Sets the property attribute.
   */
  public void setProperty(String p)
  {
    this.property = Static.trim2(p, this.property);
  }

  /**
   * Sets the reference attribute.
   */
  public void setReference(String r)
  {
    this.reference = Static.trim2(r, this.reference);
  }

  protected void saveit(Throwable e)
  {
    if (this.property != null)
    {
      getProject().setProperty(this.property, e.getMessage());
    }
    if (this.reference != null)
    {
      getProject().addReference(this.reference, e);
    }
  }

  public void execute() throws BuildException
  {
    Throwable thrown = null;
    Iterator it;
    Sequential S;

    it = this.trylist.iterator();
    while (it.hasNext())
    {
      S = (Sequential) it.next();
      try
      {
        S.perform();
      } catch (Throwable e)
      {
        thrown = e;
        /* save exception for usage */
        saveit(e);
        break;
      }
    }
    if (thrown != null)
    {
      boolean executed = false;
      it = this.catchlist.iterator();
      while (it.hasNext() && !executed)
      {
        CatchBlock cb = (CatchBlock) it.next();
        if (cb.match(thrown))
        {
          try
          {
            executed = true;
            thrown = null;
            cb.perform();
          } catch (Throwable e)
          {
            thrown = e;
            break;
          }
        }
      }
    }

    it = this.finallylist.iterator();
    while (it.hasNext())
    {
      S = (Sequential) it.next();
      try
      {
        S.perform();
      } catch (Throwable e)
      {
        thrown = e;
        break;
      }
    }

    if (thrown != null)
    {
      throw new BuildException(thrown);
    }
  }

  static public void main(String[] args)
  {
    try
    {
      try
      {
        throw new NullPointerException();
      } catch (Exception e)
      {
        System.err.println("throwing catch ..");
        throw new BuildException("catch");
      } finally
      {
        if (true)
        {
          System.err.println("throwing fianlly ..");
          throw new BuildException("finally");
        }
      }
    } catch (Exception ex)
    {
      System.err.println(ex);
    }

  }

}
