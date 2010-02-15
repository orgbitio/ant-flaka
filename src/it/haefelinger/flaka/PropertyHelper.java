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

import it.haefelinger.flaka.util.PropertyHandler;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

/**
 * Replace property handler with one able to handle EL references.
 * 
 * @author merzedes
 * @since 1.0
 * 
 */
public class PropertyHelper extends Task
{
  protected Object handler = null;

  public void execute() throws BuildException
  {
    Project project;
    PropertyHandler ph;
    org.apache.tools.ant.PropertyHelper current;
    String ANT_HELPER_REFID = "ant.PropertyHelper";

    /* get my project */
    project = getProject();
  
    /* get current property helper */
    current = org.apache.tools.ant.PropertyHelper.getPropertyHelper(project);
    
    /* create new property helper */
    ph = new PropertyHandler(current);
    /* install my property handler */
    project.getReferences().put(ANT_HELPER_REFID, ph);
  }

}
