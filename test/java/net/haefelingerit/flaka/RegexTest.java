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

package net.haefelingerit.flaka;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class RegexTest extends TestCase
{
  Pattern p = Pattern.compile("^\\s*[^#\\s]", Pattern.MULTILINE);

  public RegexTest(String name)
  {
    super(name);
  }

  public static void main(String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }

  public static Test suite()
  {
    /*
     * using reflection to setup test cases, i.e. each method matching pattern
     * "test*" will be a test case.
     */
    return new TestSuite(RegexTest.class);
  }

  public void testFind01()
  {
    String t1 = "L";
    Matcher m1 = this.p.matcher(t1);
    assertTrue(m1.find());
  }

  public void testFind02()
  {
    String t2 = "\nL";
    Matcher m2 = this.p.matcher(t2);
    assertTrue(m2.find());
  }

  public void testFind03()
  {
    String t3 = "\n# comment\nL";
    Matcher m3 = this.p.matcher(t3);
    assertTrue(m3.find());
  }

  public void testFind04()
  {
    String t4 = "\n# comment\n\tL";
    Matcher m4 = this.p.matcher(t4);
    assertTrue(m4.find());
  }
}
