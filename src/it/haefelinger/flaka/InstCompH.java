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
import org.apache.tools.ant.Project;

/**
 * A simple task which installs or deinstalls a specialized * ComponentHelper.
 * This specialized version is supposed to get * rid of some anoying log
 * messages. Installation may fail if * your Ant does not support all required
 * methods. * *
 * 
 * 
 * @author merzedes
 * @since 1.0
 * @deprecated
 */

public class InstCompH extends Task
{
  protected boolean install = true;
  protected boolean fail = false;

  public void setInstall(boolean b)
  {
    this.install = b;
  }

  public void setUnInstall(boolean b)
  {
    this.install = !b;
  }

  public void setFail(boolean b)
  {
    this.fail = b;
  }

  protected void warnordie(String msg) throws BuildException
  {
    if (this.fail)
      throwbx(msg);
    else
      verbose(msg);
  }

  public void execute() throws BuildException
  {
    Project P;

    P = getProject();
    if (this.install)
    {
      if (!CompH.install(P))
      {
        warnordie("failed to install customized component helper.");
      }
    } else
    {
      if (!CompH.uninstall(P))
      {
        warnordie("failed to uninstall customized component helper.");
      }
    }
  }
}
