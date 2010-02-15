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

import org.apache.tools.ant.util.GlobPatternMapper;

/**
 * Provides same functionality as Ant's GlobPatternMapper except * that it never
 * returns null. If there's no match then the * input filename <code>s</code>
 * gets returned. * * Ant behaves very strangely regarding Mappers. The idear
 * behind * Mappers is to map filenames. One would surely expect that non *
 * matching files are simply left intact. The original version of *
 * GlobPatternMapper however returns null, meaning that a non * matching loc is
 * removed from further processing. But that's * the job of a selector. *
 */

/**
 * 
 * @author merzedes
 * @since 1.0
 */
public class GlobMap extends GlobPatternMapper
{
  public String[] mapFileName(String s)
  {
    String[] r;
    r = super.mapFileName(s);
    if (r == null)
    {
      r = new String[] { s };
    }
    return r;
  }
}
