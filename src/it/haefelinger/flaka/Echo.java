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

/**
 * 
 */
package it.haefelinger.flaka;

import it.haefelinger.flaka.util.Static;
import it.haefelinger.flaka.util.TextReader;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

/**
 * 
 * @author merzedes
 * @since 1.0
 */
public class Echo extends org.apache.tools.ant.taskdefs.Echo
{
  protected boolean debug = true;
  protected TextReader tr;

  public Echo()
  {
    super();
    this.tr = new TextReader();
    this.tr.setSkipEmpty(false);
    this.tr.setResolveContLines(true);
    this.tr.setSkipws(true);
  }

  public void setDebug(boolean b)
  {
    this.debug = b;
  }

  /**
   * Set the character (sequence) identifying a comment line. A line having only
   * whitespace characters before that character sequence will be ignored. In
   * order to turn this feature off, provide the empty string or a string
   * consisting of whitespace characters only.
   * 
   * The default comment sequence is <code>;</code>.
   * 
   * @param s
   *          not null
   */
  public void setComment(String s)
  {
    this.tr.setCL(s);
  }

  public void setCL(String s)
  {
    this.tr.setCL(s);
  }
  
  public void setIC(String s)
  {
    this.tr.setIC(s);
  }
  
  /**
   * Set the shift attribute.
   * 
   * When set, then every line dumped will be prefixed by shome shift
   * characters.
   * 
   * By default shifting is disabled.
   * 
   * The general format of this attribute is <code>\d+(.*)</code>.
   * 
   * 
   * 
   * @param s
   */
  public void setShift(String s)
  {
    this.tr.setShift(s);
  }

  public void setCont(boolean b) {
    this.tr.setResolveContLines(b);
  }
  
  public void execute() throws BuildException
  {
    String t;
    Project p;

    t = this.message;
    p = this.getProject();

    if (t != null)
    {
      t = this.tr.setText(t).read();

      /* resolve all Ant properties ${ } */
      if (t != null)
        t = p.replaceProperties(t);

      /* resolve all EL references #{ ..} */
      if (t != null)
        t = Static.elresolve(p, t);

      // Unescape escaped characters
      if (t != null)
        t = TextReader.unescape(t);
    }
    if (t == null)
      t = this.tr.getShift();

    /* let my parent handle this */
    this.message = t;
    super.execute();
  }

}
