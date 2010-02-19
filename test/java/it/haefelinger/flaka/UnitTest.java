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

import it.haefelinger.flaka.el.EL;
import it.haefelinger.flaka.el.Functions;
import it.haefelinger.flaka.util.Static;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.tools.ant.Project;

public class UnitTest extends TestCase
{
  static String lt = "<";
  static String gt = ">";
  static String eq = "=";

  protected EL el;

  // static private void
  // log(String msg)
  // {
  // System.out.println(msg);
  // }
  //  
  public UnitTest(String name)
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
    return new TestSuite(UnitTest.class);
  }

  protected void setUp()
  {
    Project project = new Project();
    this.el = new EL(project);
  }

  void vercmp(String va, String op, String vb)
  {
    int r;
    String s;

    r = Static.vercmp(va, vb);
    s = "=";

    if (r != 0)
    {
      s = (r > 0) ? ">" : "<";
    }

    if (s.equals(op) == false)
    {
      fail("vercmp(" + va + "," + op + "," + vb + ") => " + s);
    } else
    {
      assertTrue(true);
    }
  }

  void getstem(String s, String stem)
  {
    String r;

    r = Static.getstem(s);

    if (r == null)
    {
      if (stem != null)
      {
        fail("getstem(" + s + "," + stem + ") => null");
      }
      return;
    }

    if (stem == null)
    {
      fail("getstem(" + s + ",null) => " + r);
      return;
    }

    if (!r.equals(stem))
    {
      fail("getstem(" + s + "," + stem + ") => " + r);
    }
    return;
  }

  void jar2var(String s, String expected)
  {
    String r;

    r = Static.jar2var(s);

    if (r == null)
    {
      if (expected != null)
      {
        fail("jar2var(" + s + "," + expected + ") => null");
      }
      return;
    }
    if (expected == null)
    {

      fail("jar2var(" + s + ",null) => " + r);
      return;
    }
    if (!r.equals(expected))
    {
      fail("jar2var(" + s + "," + expected + ") => " + r);
    }
    return;
  }

  /* xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx */
  public void test_Static_vercmp_01()
  {
    vercmp("1.0", eq, "1.0");
  }

  public void test_Static_vercmp_02()
  {
    vercmp("1.0", gt, "0.9");
  }

  public void test_Static_vercmp_03()
  {
    vercmp("1.0", eq, "1.0");
  }

  public void test_Static_vercmp_04()
  {
    vercmp("1.0", lt, "1.1");
  }

  public void test_Static_vercmp_05()
  {
    vercmp("1.0", gt, "0.9.9");
  }

  public void test_Static_vercmp_06()
  {
    vercmp("1.0", eq, "1.0.0");
  }

  public void test_Static_vercmp_07()
  {
    vercmp("1.0", lt, "1.0.1");
  }

  public void test_Static_vercmp_08()
  {
    vercmp("1.0.0", gt, "0.9");
  }

  public void test_Static_vercmp_09()
  {
    vercmp("1.0.0", eq, "1.0");
  }

  public void test_Static_vercmp_10()
  {
    vercmp("1.0.0", lt, "1.1");
  }

  public void test_Static_vercmp_11()
  {
    vercmp("1.0.0", gt, "0.9.9");
  }

  public void test_Static_vercmp_12()
  {
    vercmp("1.0.0", eq, "1.0.0");
  }

  public void test_Static_vercmp_13()
  {
    vercmp("1.0.0", lt, "1.0.1");
  }

  public void test_Static_vercmp_14()
  {
    vercmp("1.0", gt, "0.9a");
  }

  public void test_Static_vercmp_15()
  {
    vercmp("1.0", lt, "1.0a");
  }

  public void test_Static_vercmp_16()
  {
    vercmp("1.0", lt, "1.1a");
  }

  public void test_Static_vercmp_17()
  {
    vercmp("1.0", gt, "0.9.9a");
  }

  public void test_Static_vercmp_18()
  {
    vercmp("1.0", lt, "1.0.0a");
  }

  public void test_Static_vercmp_19()
  {
    vercmp("1.0", lt, "1.0.1a");
  }

  public void test_Static_vercmp_20()
  {
    vercmp("1.0", gt, "0.99");
  }

  public void test_Static_vercmp_21()
  {
    vercmp("1.0", lt, "1.09");
  }

  public void test_Static_vercmp_22()
  {
    vercmp("1.0", lt, "1.19");
  }

  public void test_Static_vercmp_23()
  {
    vercmp("1.0", gt, "0.9.99");
  }

  public void test_Static_vercmp_24()
  {
    vercmp("1.0", lt, "1.0.09");
  }

  public void test_Static_vercmp_25()
  {
    vercmp("1.0", lt, "1.0.19");
  }

  /* :::::::::::::::::::::: getstem :::::::::::::::::::::::::: */
  public void test_Static_getstem_01()
  {
    getstem(null, null);
  }

  public void test_Static_getstem_02()
  {
    getstem("", null);
  }

  public void test_Static_getstem_03()
  {
    getstem(" ", null);
  }

  public void test_Static_getstem_04()
  {
    getstem("-", "-");
  }

  public void test_Static_getstem_05()
  {
    getstem(".jar", null);
  }

  public void test_Static_getstem_06()
  {
    getstem("-.jar", "-");
  }

  public void test_Static_getstem_07()
  {
    getstem("-1.jar", "-1");
  }

  public void test_Static_getstem_08()
  {
    getstem("1.jar", "1");
  }

  public void test_Static_getstem_09()
  {
    getstem("1-.jar", "1-");
  }

  public void test_Static_getstem_10()
  {
    getstem("12.jar", "12");
  }

  public void test_Static_getstem_11()
  {
    getstem("12-.jar", "12-");
  }

  public void test_Static_getstem_12()
  {
    getstem("1-2.jar", "1");
  }

  public void test_Static_getstem_13()
  {
    getstem("-12.jar", "-12");
  }

  public void test_Static_getstem_14()
  {
    getstem(" -12.jar", "-12");
  }

  public void test_Static_getstem_15()
  {
    getstem("a.jar", "a");
  }

  public void test_Static_getstem_16()
  {
    getstem("a1.jar", "a");
  }

  public void test_Static_getstem_17()
  {
    getstem("a1-.jar", "a1-");
  }

  public void test_Static_getstem_18()
  {
    getstem("a-1.jar", "a");
  }

  public void test_Static_getstem_19()
  {
    getstem("-a1.jar", "-a");
  }

  /* :::::::::::::::::::::: jar2var :::::::::::::::::::::::::: */
  public void test_Static_jar2var_01()
  {
    /* test the null argument */
    jar2var(null, "");
  }

  public void test_Static_jar2var_02()
  {
    /* test for empty arguments .. */
    jar2var("", "");
    jar2var(" ", "");
    jar2var("\t ", "");
    jar2var("\r", "");
  }

  public void test_Static_jar2var_03()
  {
    /* test standard case */
    jar2var("log4j-1.2.8.jar", "LOG4J");
    jar2var("LOG4J-1.2.8.jar", "LOG4J");
    jar2var("LoG4j-1.2.8.jar", "LOG4J");
  }

  public void test_Static_jar2var_04()
  {
    /* test artifacts names having non letter|digit */
    jar2var("commons-lang-1.2.8.jar", "COMMONS_LANG");
    jar2var("COMMONS-lang-1.2.8.jar", "COMMONS_LANG");
    jar2var("commons-LANG-1.2.8.jar", "COMMONS_LANG");
    jar2var("commOns-lanG-1.2.8.jar", "COMMONS_LANG");

    jar2var("commons_lang-1.2.8.jar", "COMMONS_LANG");
    jar2var("COMMONS:lang-1.2.8.jar", "COMMONS_LANG");
    jar2var("commons?LANG-1.2.8.jar", "COMMONS_LANG");
    jar2var("commOns<lanG-1.2.8.jar", "COMMONS_LANG");
  }

  public void test_Static_jar2var_05()
  {
    /* testing a couple of unusual artifact names */
    jar2var(".jar", "");
    jar2var("-.jar", "_");
    jar2var("-1.jar", "_1");
    jar2var("1.jar", "1");
    jar2var("1-.jar", "1_");
    jar2var("12.jar", "12");
    jar2var("12-.jar", "12_");
    jar2var("1-2.jar", "1");
    jar2var("-12.jar", "_12");
    jar2var(" -12.jar", "_12");
    jar2var("a.jar", "A");
    jar2var("a1.jar", "A");
    jar2var("a1-.jar", "A1_");
    jar2var("a-1.jar", "A");
    jar2var("-a1.jar", "_A");
    jar2var("a4-1.jar", "A4");
    jar2var("a_4-1.jar", "A_4");
  }

  public void test_Static_jar2var_06()
  {
    /* test full path names */
    jar2var("/.jar", "");
    jar2var("/-.jar", "_");
    jar2var("/-1.jar", "_1");
    jar2var("/1.jar", "1");
    jar2var("/1-.jar", "1_");
    jar2var("/12.jar", "12");
    jar2var("/12-.jar", "12_");
    jar2var("/1-2.jar", "1");
    jar2var("/-12.jar", "_12");
    jar2var("/ -12.jar", "_12");
    jar2var("/a.jar", "A");
    jar2var("/a1.jar", "A");
    jar2var("/a1-.jar", "A1_");
    jar2var("/a-1.jar", "A");
    jar2var("/-a1.jar", "_A");
  }

  public void test_Static_jar2var_07()
  {
    /* test full path names */
    jar2var("./.jar", "");
    jar2var("./-.jar", "_");
    jar2var("./-1.jar", "_1");
    jar2var("./1.jar", "1");
    jar2var("./1-.jar", "1_");
    jar2var("./12.jar", "12");
    jar2var("./12-.jar", "12_");
    jar2var("./1-2.jar", "1");
    jar2var("./-12.jar", "_12");
    jar2var("./ -12.jar", "_12");
    jar2var("./a.jar", "A");
    jar2var("./a1.jar", "A");
    jar2var("./a1-.jar", "A1_");
    jar2var("./a-1.jar", "A");
    jar2var("./-a1.jar", "_A");
  }

  public void test_Static_jar2var_08()
  {
    /* test full path names */
    jar2var("a-b/.jar", "");
    jar2var("a-b/-.jar", "_");
    jar2var("a-b/-1.jar", "_1");
    jar2var("a-b/1.jar", "1");
    jar2var("a-b/1-.jar", "1_");
    jar2var("a-b/12.jar", "12");
    jar2var("a-b/12-.jar", "12_");
    jar2var("a-b/1-2.jar", "1");
    jar2var("a-b/-12.jar", "_12");
    jar2var("a-b/ -12.jar", "_12");
    jar2var("a-b/a.jar", "A");
    jar2var("a-b/a1.jar", "A");
    jar2var("a-b/a1-.jar", "A1_");
    jar2var("a-b/a-1.jar", "A");
    jar2var("a-b/-a1.jar", "_A");
  }

  public void test_Static_jar2var_09()
  {
    jar2var("BSIJNI_40", "BSIJNI_40");
    jar2var("BSIJNI_40.jar", "BSIJNI_");
    jar2var("a_1.jar", "A_");
  }

  void _expect(String glob, String regex)
  {
    String r = null;
    try
    {
      r = Static.patternAsRegex(glob);
      if ((r != null) && r.equals(regex) == false)
      {
        fail("glob2regex(" + glob + ") => `" + r + "': expected `" + regex + "'.");
      }

    } catch (Exception e)
    {
      fail("glob2regex(" + glob + ") => exception `" + e + "'.");
    }
  }

  public void test_glob2regex_01()
  {
    _expect("*", ".*");
  }

  public void test_glob2regex_02()
  {
    _expect("*.*", ".*\\..*");
  }

  public void test_glob2regex_03()
  {
    _expect("", "");
  }

  public void test_glob2regex_04()
  {
    _expect("?", ".");
  }

  public void test_glob2regex_05()
  {
    _expect(".", "\\.");
  }

  public void test_glob2regex_06()
  {
    _expect("[^]", "[\\^]");
  }

  public void test_glob2regex_07()
  {
    _expect("[^a]", "[\\^a]");
  }

  public void test_glob2regex_08()
  {
    _expect("[!a]", "[^a]");
  }

  public void test_glob2regex_09()
  {
    _expect("[!]", "\\[\\!\\]");
  }

  void expect(String orig, String expected)
  {
    String s = "";

    try
    {
      s = Functions.quote(orig);
      if (s.equals(expected) == false)
      {
        fail("Functions.quote(\"" + orig + "\")=>\"" + s + "\", expected \"" + expected + "\"");
      }
    } catch (Exception e)
    {
      fail("Functions.quote(\"" + orig + "\") throws unexpected exception: " + e);
    }
  }

  public void test_el_0x01()
  {
    try
    {
      assertEquals("\\d", this.el.tostr("\\d"));
    } catch (Exception e)
    {
      fail("unexptected el exception: " + e);
    }
  }

  public void test_el_0x02()
  {
    try
    {
      assertEquals("\\d", this.el.tostr("#{'\\\\d'}"));
    } catch (Exception e)
    {
      fail("unexptected el exception: " + e);
    }
  }

  public void test_quote_0x01()
  {
    expect("", "");
  }

  public void test_quote_0x02()
  {
    expect("'", "'");
  }

  public void test_quote_0x03()
  {
    expect("\\'", "\\'");
  }

  public void test_quote_0x04()
  {
    expect("\\", "\\");
  }

  public void test_quote_0x05()
  {
    expect("\\\\", "\\\\");
  }

  public void test_quote_0x06()
  {
    expect("\\a", "\\\\a");
  }

  public void test_quote_0x07()
  {
    expect("a\\", "a\\");
  }

  public void test_quote_0x08()
  {
    expect("a'", "a'");
  }

  public void test_quote_0x09()
  {
    expect("a\\'", "a\\'");
  }

  public void test_quote_0x0a()
  {
    expect("a\\\\", "a\\\\");
  }

  public void test_quote_0x0b()
  {
    expect("ab", "ab");
  }

  public void test_quote_0x0c()
  {
    expect("'a", "'a");
  }

  public void test_quote_0x0d()
  {
    expect("\\'", "\\'");
  }

  public void test_quote_0x0e()
  {
    expect("\"", "\"");
  }

  public void test_quote_0x0f()
  {
    expect("a\"", "a\"");
  }

}
