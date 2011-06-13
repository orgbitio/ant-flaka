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

import it.haefelinger.flaka.util.Static;
import it.haefelinger.flaka.el.EL;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

/**
 * Load EL functions into EL instance.
 * 
 * @author merzedes
 * @since 1.3
 * 
 */
public class ELLoad extends Task {
  protected String clazz;
  
  /**
   * The classname given be prefixed by ':'.
   * @param clazz
   */
  public void setClazz(String clazz) {
    this.clazz = clazz;
  }

  public void execute() throws BuildException {
    Project project = this.getProject();
    EL el = Static.el(project);
    
    try {
      el.sourceFunctions(this.clazz);
    } catch (SecurityException e) {
      throw new BuildException(e);
    } catch (ClassNotFoundException e) {
      throw new BuildException(e);
    }
  }

}
