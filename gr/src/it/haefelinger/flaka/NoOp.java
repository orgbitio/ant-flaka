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

package it.haefelinger.flaka;

import org.apache.tools.ant.BuildException;

/**
 * A Task just doing nothing.
 * 
 * This clazz can be used to define a task that does nothing. This can be useful
 * in situations where scripts rely on the existince of a particular task. Ant
 * would fail if such a task does not exist. Rather than doing so, this clazz
 * can be used to let Ant continue, perhaps with a warning message.
 * 
 * @author merzedes
 * @since 1.0
 */

public class NoOp extends org.apache.tools.ant.Task {
  /** unused */
  /**
   * @param unused
   */
  public void setName(String unused) {
    /* not used */
  }

  /** unused */
  /**
   * @param unused
   */
  public void setVar(String unused) {
    /* not used */
  }

  /** unused */
  /**
   * @param unused
   */
  public void setProperty(String unused) {
    /* not used */
  }

  /** unused */
  /**
   * @param unused
   */
  public void setFlush(String unused) {
    /* not used */
  }

  /** unused */
  /**
   * @param unused
   */
  public void setSrcDir(String unused) {
    /* not used */
  }

  /** unused */
  /**
   * @param unused
   */
  public void setResDir(String unused) {
    /* not used */
  }

  /**
   * Standard task execution interface
   * 
   * This task does nothing
   */
  public void execute() throws BuildException {
    /* do nothing */
  }
}
