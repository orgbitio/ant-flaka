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

/*
 * Copyright 2001-2005 The Apache Software Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this loc except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 */
package it.haefelinger.flaka.type;

import it.haefelinger.flaka.util.Static;

import java.util.Enumeration;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * A set of filters to be applied to something.
 * 
 * A filter set may have begintoken and endtokens defined.
 * 
 * *
 * 
 * @author merzedes
 * @since 1.0
 */
public class FilterSet extends org.apache.tools.ant.types.FilterSet
{
  public class Props
  {
    /* empty class */
  }

  public void grep(String regexpr)
  {
    String[] R;
    Pattern P;
    Matcher M;
    Enumeration E;
    LinkedList L;

    R = null;
    P = Pattern.compile(regexpr);
    L = new LinkedList();
    E = getProject().getProperties().keys();

    while (E.hasMoreElements())
    {
      String k;

      k = (String) E.nextElement();
      M = P.matcher(k);

      if (M.matches() == false)
      {
        continue;
      }
      L.add(k);
    }

    R = new String[L.size()];
    for (int i = 0; i < L.size(); ++i)
      R[i] = (String) L.get(i);
    L = null;
    addtokens(R);
  }

  protected void addtokens(String[] name)
  {
    if (name != null && name.length > 0)
    {
      String k, v;
      for (int i = 0; i < name.length; ++i)
      {
        k = name[i];
        v = getProject().getProperty(k);
        v = getProject().replaceProperties(v);
        addtoken(k, v);
      }
    }
  }

  protected void addtoken(String K, String V)
  {
    String k = K;
    String v = V;
    if (k == null)
    {
      return;
    }
    k = k.trim();
    if (k.length() <= 0)
    {
      return;
    }
    if (v == null)
      v = "";
    super.addFilter(k, v);
  }

  /** support for element </code>properties</code> */
  public Props createProperties()
  {
    String s;
    s = Static.patternAsRegex("*");
    grep(s);
    return new Props();
  }
}
