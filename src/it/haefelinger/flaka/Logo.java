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
import it.haefelinger.flaka.util.TextReader;

import org.apache.tools.ant.BuildException;

/**
 * 
 * @author merzedes
 * @since 1.0
 */
public class Logo extends Task
{
  protected String text = "";
  protected String chr = ":";
  protected int width = 80;
  protected TextReader tr = new TextReader();

  public void addText(String s)
  {
    this.tr.addText(s);
  }

  public void setChr(String s)
  {
    this.chr = Static.trim2(s, this.chr);
  }

  public void setWidth(int s)
  {
    this.width = s;
  }

  public void execute() throws BuildException
  {
    this.tr.setComment(";");
    this.tr.setSkipws(true);
    this.tr.setResolveContLines(true);
    this.tr.setProject(getProject());
    this.tr.setSkipEmpty(false);
    String line = this.tr.readLine();
    System.out.println(Static.mkchrseq(this.chr,this.width));
    int w = this.chr.length();
    while(line != null)
    {
      System.out.print(this.chr);
      System.out.print(Static.center(line,this.width - 2 * w," "));
      System.out.print(this.chr);
      System.out.println();
      line = this.tr.readLine();
    }
    System.out.println(Static.mkchrseq(this.chr,this.width));
    System.out.flush();
  }
}
