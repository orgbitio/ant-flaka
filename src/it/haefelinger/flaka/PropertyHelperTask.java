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

import it.haefelinger.flaka.prop.PropertyHelper10;
import it.haefelinger.flaka.prop.PropertyHelper12;
import it.haefelinger.flaka.util.Static;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.MagicNames;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.PropertyHelper;

/**
 * Replace property handler with one able to handle EL references.
 * 
 * @author merzedes
 * @since 1.0
 * 
 */
public class PropertyHelperTask extends Task
{
  protected String clazzname = "Helper10";

  public void setClassname(String clazzname)
  {
    this.clazzname = Static.trim3(getProject(), clazzname, this.clazzname);
  }

  @SuppressWarnings("deprecation")
  public void execute() throws BuildException
  {
    Project project;
    PropertyHelper ph,current;
 
    /* get my project */
    project = getProject();

    /* get current property helper */
    current = org.apache.tools.ant.PropertyHelper.getPropertyHelper(project);

    if (this.clazzname.equalsIgnoreCase("Helper12"))
    {
      ph = new PropertyHelper12();
      ph.setProject(project);
    }
    else
    {
      ph = new PropertyHelper10();
      ph.setProject(project);
      ph.setNext(current);
    }

    /* install my property handler */
    project.getReferences().put(MagicNames.REFID_PROPERTY_HELPER, ph);
  }

}
