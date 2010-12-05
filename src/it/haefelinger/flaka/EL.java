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

import it.haefelinger.flaka.prop.IFPropertyHelper;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.PropertyHelper;

/**
 * Enable or disable EL on the fly.
 * 
 * Enable would only work if property handler is installed. Otherwise a build
 * exception will be thrown.
 * 
 * @author merzedes
 * @since 1.0
 * 
 */
public class EL extends Task
{
  protected boolean enable = true;

  public void setEnable(boolean b)
  {
    this.enable = b;
  }

  public void execute() throws BuildException
  {
    Project project;
    PropertyHelper current;

    /* get my project */
    project = getProject();

    /* get current property helper */
    current = org.apache.tools.ant.PropertyHelper.getPropertyHelper(project);

    if (current instanceof IFPropertyHelper)
    {
      IFPropertyHelper ph = (IFPropertyHelper) current;
      ph.enable(this.enable);
    }
    else
    {
      // must be Flaka property handler.
      throw new BuildException("unable to handle EL on non-Flaka property handler.");
    }

  }

}
