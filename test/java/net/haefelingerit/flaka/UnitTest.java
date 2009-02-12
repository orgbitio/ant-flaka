package net.haefelingerit.flaka;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.haefelingerit.flaka.tel.TestEL;

public class UnitTest extends TestCase 
{ 
  static String lt = "<";
  static String gt = ">";
  static String eq = "=";

  static private void 
  log(String msg)
  {
    System.out.println(msg);
  }
  
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
    /* using reflection to setup test cases, i.e. each method matching
    ** pattern "test*" will be a test case. 
    **/
    return new TestSuite(UnitTest.class);
  }


  void vercmp(String va,String op, String vb)
  {
    int r;
    String s;
    
    r = Static.vercmp(va,vb);
    s = "=";
    
    if(r != 0) {
      s = (r>0) ? ">" : "<";
    }
    
    if(s.equals(op) == false)
    {
      fail("vercmp(" + va + "," + op + "," + vb + ") => " + s);
    }
    else
    {
      assertTrue(true);
    }
  }

  void getstem(String s,String stem)
  {
    String r;
    
    r = Static.getstem(s);

    if(r == null)
    {
      if(stem != null) {
        fail("getstem(" + s + "," + stem + ") => null");
      }
      return;
    }
    
    if(stem == null)
    {
     fail("getstem(" + s + ",null) => " + r);
     return;
    }

    if(!r.equals(stem))
    {
      fail("getstem(" + s + "," + stem + ") => " + r);
    }
    return;
  }

  void jar2var(String s,String expected)
  {
    String r;
    
    r = Static.jar2var(s);
    
    if(r == null)
    {
      if(expected != null) {
        fail("jar2var(" + s + "," + expected + ") => null");
      }
      return;
    }
    if(expected == null)
    {
      
      fail("jar2var(" + s + ",null) => " + r);
      return;
    }
    if(!r.equals(expected))
    {
      fail("jar2var(" + s + "," + expected + ") => " + r);
    }
    return;
  }
  
  /*xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx*/
  public void test_Static_vercmp_01()
  {
    vercmp("1.0",eq,"1.0");
  }
  public void test_Static_vercmp_02()
  {
    vercmp("1.0",gt,"0.9");
  }
  public void test_Static_vercmp_03()
  {
    vercmp("1.0",eq,"1.0");
  }
  public void test_Static_vercmp_04()
  {
    vercmp("1.0",lt,"1.1");
  }
  public void test_Static_vercmp_05()
  {
    vercmp("1.0",gt,"0.9.9");
  }
  public void test_Static_vercmp_06()
  {
    vercmp("1.0",eq,"1.0.0");
  }
  public void test_Static_vercmp_07()
  {
    vercmp("1.0",lt,"1.0.1");
  }

  public void test_Static_vercmp_08()
  {
    vercmp("1.0.0",gt,"0.9");
  }
  public void test_Static_vercmp_09()
  {
    vercmp("1.0.0",eq,"1.0");
  }
  public void test_Static_vercmp_10()
  {
    vercmp("1.0.0",lt,"1.1");
  }
  public void test_Static_vercmp_11()
  {
    vercmp("1.0.0",gt,"0.9.9");
  }
  public void test_Static_vercmp_12()
  {
    vercmp("1.0.0",eq,"1.0.0");
  }
  public void test_Static_vercmp_13()
  {
    vercmp("1.0.0",lt,"1.0.1");
  }
   
  public void test_Static_vercmp_14()
  {
    vercmp("1.0",gt,"0.9a");
  }
  public void test_Static_vercmp_15()
  {
    vercmp("1.0",lt,"1.0a");
  }
  public void test_Static_vercmp_16()
  {
    vercmp("1.0",lt,"1.1a");
  }
  public void test_Static_vercmp_17()
  {
    vercmp("1.0",gt,"0.9.9a");
  }
  public void test_Static_vercmp_18()
  {
    vercmp("1.0",lt,"1.0.0a");
  }
  public void test_Static_vercmp_19()
  {
    vercmp("1.0",lt,"1.0.1a");
  }
   
  public void test_Static_vercmp_20()
  {
    vercmp("1.0",gt,"0.99");
  }
  public void test_Static_vercmp_21()
  {
    vercmp("1.0",lt,"1.09");
  }
  public void test_Static_vercmp_22()
  {
    vercmp("1.0",lt,"1.19");
  }
  public void test_Static_vercmp_23()
  {
    vercmp("1.0",gt,"0.9.99");
  }
  public void test_Static_vercmp_24()
  {
    vercmp("1.0",lt,"1.0.09");
  }
  public void test_Static_vercmp_25()
  {
    vercmp("1.0",lt,"1.0.19");
  }

  /* :::::::::::::::::::::: getstem :::::::::::::::::::::::::: */
  public void test_Static_getstem_01()
  {
    getstem(null,null);
  }
  public void test_Static_getstem_02()
  {
    getstem("",null);
  }
  public void test_Static_getstem_03()
  {
    getstem(" ",null);
  }
  public void test_Static_getstem_04()
  {
    getstem("-","-");
  }
  public void test_Static_getstem_05()
  {
    getstem(".jar",null);
  }
  public void test_Static_getstem_06()
  {
    getstem("-.jar","-");
  }
  public void test_Static_getstem_07()
  {
    getstem("-1.jar","-1");
  }
  public void test_Static_getstem_08()
  {
    getstem("1.jar","1");
  }
  public void test_Static_getstem_09()
  {
    getstem("1-.jar","1-");
  }
  public void test_Static_getstem_10()
  {
    getstem("12.jar","12");
  }
  public void test_Static_getstem_11()
  {
    getstem("12-.jar","12-");
  }
  public void test_Static_getstem_12()
  {
    getstem("1-2.jar","1");
  }
  public void test_Static_getstem_13()
  {
    getstem("-12.jar","-12");
  }
  public void test_Static_getstem_14()
  {
    getstem(" -12.jar","-12");
  }
  public void test_Static_getstem_15()
  {
    getstem("a.jar","a");
  }
  public void test_Static_getstem_16()
  {
    getstem("a1.jar","a");
  }
  public void test_Static_getstem_17()
  {
    getstem("a1-.jar","a1-");
  }
  public void test_Static_getstem_18()
  {
    getstem("a-1.jar","a");
  }
  public void test_Static_getstem_19()
  {
    getstem("-a1.jar","-a");
  }  

  /* :::::::::::::::::::::: jar2var :::::::::::::::::::::::::: */
  public void test_Static_jar2var_01()
  {
    /* test the null argument */
    jar2var(null,"");
  }
  public void test_Static_jar2var_02()
  {
    /* test for empty arguments .. */
    jar2var(""   ,"");
    jar2var(" "  ,"");
    jar2var("\t ","");
    jar2var("\r" ,"");
  }
  public void test_Static_jar2var_03()
  {
    /* test standard case */
    jar2var("log4j-1.2.8.jar","LOG4J");
    jar2var("LOG4J-1.2.8.jar","LOG4J");
    jar2var("LoG4j-1.2.8.jar","LOG4J");
  }
  public void test_Static_jar2var_04()
  {
    /* test artifacts names having non letter|digit */
    jar2var("commons-lang-1.2.8.jar","COMMONS_LANG");
    jar2var("COMMONS-lang-1.2.8.jar","COMMONS_LANG");
    jar2var("commons-LANG-1.2.8.jar","COMMONS_LANG");
    jar2var("commOns-lanG-1.2.8.jar","COMMONS_LANG");

    jar2var("commons_lang-1.2.8.jar","COMMONS_LANG");
    jar2var("COMMONS:lang-1.2.8.jar","COMMONS_LANG");
    jar2var("commons?LANG-1.2.8.jar","COMMONS_LANG");
    jar2var("commOns<lanG-1.2.8.jar","COMMONS_LANG");
  }
  public void test_Static_jar2var_05()
  {
    /* testing a couple of unusual artifact names */
    jar2var(".jar","");
    jar2var("-.jar","_");
    jar2var("-1.jar","_1");
    jar2var("1.jar","1");
    jar2var("1-.jar","1_");
    jar2var("12.jar","12");
    jar2var("12-.jar","12_");
    jar2var("1-2.jar","1");
    jar2var("-12.jar","_12");
    jar2var(" -12.jar","_12");
    jar2var("a.jar","A");
    jar2var("a1.jar","A");
    jar2var("a1-.jar","A1_");
    jar2var("a-1.jar","A");
    jar2var("-a1.jar","_A");
    jar2var("a4-1.jar","A4");
    jar2var("a_4-1.jar","A_4");
  }  
  
  public void test_Static_jar2var_06()
  {
    /* test full path names */
    jar2var("/.jar","");
    jar2var("/-.jar","_");
    jar2var("/-1.jar","_1");
    jar2var("/1.jar","1");
    jar2var("/1-.jar","1_");
    jar2var("/12.jar","12");
    jar2var("/12-.jar","12_");
    jar2var("/1-2.jar","1");
    jar2var("/-12.jar","_12");
    jar2var("/ -12.jar","_12");
    jar2var("/a.jar","A");
    jar2var("/a1.jar","A");
    jar2var("/a1-.jar","A1_");
    jar2var("/a-1.jar","A");
    jar2var("/-a1.jar","_A");
  }  
  public void test_Static_jar2var_07()
  {
    /* test full path names */
    jar2var("./.jar","");
    jar2var("./-.jar","_");
    jar2var("./-1.jar","_1");
    jar2var("./1.jar","1");
    jar2var("./1-.jar","1_");
    jar2var("./12.jar","12");
    jar2var("./12-.jar","12_");
    jar2var("./1-2.jar","1");
    jar2var("./-12.jar","_12");
    jar2var("./ -12.jar","_12");
    jar2var("./a.jar","A");
    jar2var("./a1.jar","A");
    jar2var("./a1-.jar","A1_");
    jar2var("./a-1.jar","A");
    jar2var("./-a1.jar","_A");
  }  
  public void test_Static_jar2var_08()
  {
    /* test full path names */
    jar2var("a-b/.jar","");
    jar2var("a-b/-.jar","_");
    jar2var("a-b/-1.jar","_1");
    jar2var("a-b/1.jar","1");
    jar2var("a-b/1-.jar","1_");
    jar2var("a-b/12.jar","12");
    jar2var("a-b/12-.jar","12_");
    jar2var("a-b/1-2.jar","1");
    jar2var("a-b/-12.jar","_12");
    jar2var("a-b/ -12.jar","_12");
    jar2var("a-b/a.jar","A");
    jar2var("a-b/a1.jar","A");
    jar2var("a-b/a1-.jar","A1_");
    jar2var("a-b/a-1.jar","A");
    jar2var("a-b/-a1.jar","_A");
  }  
  public void test_Static_jar2var_09()
  {
    jar2var("BSIJNI_40","BSIJNI_40");
    jar2var("BSIJNI_40.jar","BSIJNI_");
    jar2var("a_1.jar","A_");
  }  

  static public boolean evaluate(String[] argv) throws Exception {
    return new TestEL(null,null).eval(argv);
  }

  static public boolean evaluate(String s, char quote) throws Exception {
    return new TestEL(null,null).eval(Static.split0x1(s, quote));
  }

  
  
  private void good(String s)
  {
    String m;
    boolean b;

    m = "got false";
    b = false;

    try {
      b = evaluate(s,'\'');
    }
    catch(Exception ex)
    {
      m = "throws excption `" + ex + "'";
      b = false;
    }
    if(b == false)
    {
      fail("test `" + s + "' => expected true, " + m + ".");
    }
  }

  private void wrong(String s)
  {
    String m;
    boolean b;

    log(s);

    m = "got true";
    b = true;

    try {
      b = evaluate(s,'\'');
    }
    catch(Exception ex)
    {
      m = "throws excption `" + ex + "'";
      b = true;
    }
    if(b == true)
    {
      fail("test `" + s + "' => expected false, " + m + ".");
    }
  }
  
  private void synerr(String s)
  {
    boolean b;

    b = false;

    try {
      evaluate(s,'\'');
    }
    catch(Exception ex)
    {
      b = true;
    }
    if(b == false)
    {
      fail("test `" + s + "' => expected to throw an exception.");
    }
  }
  
  

  public void test_jtest_01() { good(  "-f build.xml"); }
  public void test_jtest_02() { good(  "-d ."); }
  public void test_jtest_03() { good(  "-d .."); }
  public void test_jtest_04() { good(  "-e ."); }
  public void test_jtest_05() { good(  "-e .."); }
  public void test_jtest_06() { good(  "-e build.xml"); }
  public void test_jtest_07() { synerr("-e . -f . "); }
  public void test_jtest_08() { wrong(  "-e . -a -f . "); }
  public void test_jtest_09() { good(  "-e . -a -d . "); }
  public void test_jtest_10() { good(  "-e . -a -d . -a -d ..  "); }
  public void test_jtest_11() { wrong(  "-e . -a -d . -a -f ..  "); }
  public void test_jtest_12() { good(  "-e . -a -d . -a -e ..  "); }
  public void test_jtest_13() { wrong(  "5 -gt 6  "); }
  public void test_jtest_14() { wrong(  "5 -gt 5  "); }
  public void test_jtest_15() { good(  "5 -gt 4  "); }
  public void test_jtest_16() { good(  "5 -ge 4  "); }
  public void test_jtest_17() { good(  "5 -ge 5  "); }
  public void test_jtest_18() { wrong(  "5 -ge 6  "); }
  public void test_jtest_19() { wrong(  "a = b  "); }
  public void test_jtest_20() { good(  "a = a  "); }
  public void test_jtest_21() { wrong(  "a != a  "); }
  public void test_jtest_22() { good(  "a != b  "); }
  public void test_jtest_23() { good(  "-l x -ge -l y  "); }
  public void test_jtest_24() { wrong(  "-l x -gt -l y  "); }
  public void test_jtest_25() { wrong(  "-l x -lt -l y  "); }
  public void test_jtest_26() { wrong( "-f ."); }
  public void test_jtest_27() { wrong( "-f .."); }
  public void test_jtest_28() { synerr("-1 build.xml"); }
  public void test_jtest_29() { synerr("-z -a -d ."); }
  public void test_jtest_30() { good("-z '' -a -d ."); }
  public void test_jtest_31() { good("-z '' "); }
  public void test_jtest_32() { good("-z"); }
  public void test_jtest_33() { good("-z "); }
  public void test_jtest_34() { wrong("-z x"); }
  public void test_jtest_35() { wrong("-z ' '"); }

  public void test_jtest_40() { synerr("-n -a -d ."); }
  public void test_jtest_41() { wrong("-n '' -a -d ."); }
  public void test_jtest_42() { wrong("-n '' "); }
  public void test_jtest_43() { good("-n"); }  
  public void test_jtest_44() { good("-n "); }
  public void test_jtest_45() { good("-n x"); }
  public void test_jtest_46() { good("-n ' '"); }

  public void test_jtest_50() { synerr("(-n x -a -n y)"); }
  public void test_jtest_51() { synerr("(-n x -o -n y)"); }
  public void test_jtest_52() { good("( -n x -a -n y )"); }
  public void test_jtest_53() { good("( -n x -o -n y )"); }
  public void test_jtest_54() { good("( -n x -a ( -n y ) )"); }
  // fails ..
  //public void test_jtest_55() { good("( ( -n x ) -a ( -n y ) )"); }
  

  void split(String s,String[] expd)
  {
    String[] argv = new String[] {};
    boolean eq;
    String r = "";
    String E = "";
    int i;

    for(i=0;i< expd.length;++i)
    {
      E += "[" + expd[i] + "] "; 
    }
 
    eq = false;
    try {
      argv = Static.split0x1(s,'\'');
    }
    catch(Exception e)
    {
      r  = e.toString();
      fail("error in splitting `" + s + "', got " + r);
    }
    
    if(argv == null) {
      fail("error in splitting `" + s + "', got (null)");
      return;
    }

    for(i=0;i< argv.length;++i)
      r += "[" + argv[i] + "] "; 
    
    if(argv.length != expd.length) 
      fail("error in splitting `" + s + "', got " + r + " expected " + E);
    
    eq = true;
    for(i = 0;eq && i< expd.length;++i)
      eq = argv[i].equals(expd[i]);

    if(eq == false)
    {
      fail("error in splitting `" + s + "', got " + r+ " expected " + E);
    }
  }
  
  public void test_jtest_split() 
  {
    String   argv;
    String[] expd;

    argv = "-z ''";
    expd = new String[] { "-z","" };
    split(argv,expd);

    argv = "-z ' '";
    expd = new String[] { "-z"," "};
    split(argv,expd);

    argv = "-z''";
    expd = new String[] {"-z" };
    split(argv,expd);

    argv = "-z' '";
    expd = new String[] {"-z "};
    split(argv,expd);

    argv = "\\'a";
    expd = new String[] {"'a"};
    split(argv,expd);

    argv = "\\a";
    expd = new String[] {"a"};
    split(argv,expd);


    argv = "'a b c' d e";          // [a b c]  [d]      [e]
    expd = new String[] { "a b c","d","e" };
    split(argv,expd);

    argv = "'ab\\'c' '\\\\' d";       // [ab"c]    [\]      [d]
    expd = new String[] { "ab'c","\\","d" };
    split(argv,expd);

    argv = "a\\\\\\b d'e f'g h";      // [a\b]    [de fg]  [h]
    expd = new String[] { "a\\b","de fg","h" };
    split(argv,expd);

    argv = "a\\\\\\'b c d";          // [a\"b]    [c]      [d]
    expd = new String[] {"a\\'b","c","d" };
    split(argv,expd);

    argv = "a\\\\\\\\'b c' d e";      // [a\\b c]  [d]      [e]
    expd = new String[] { "a\\\\b c","d","e" };
    split(argv,expd);

  }

  void _expect(String glob,String regex)
  {
    String r = null;
    try {
      r = Static.patternAsRegex(glob);
      if((r!=null) && r.equals(regex) == false) {
        fail("glob2regex(" + glob + ") => `" + r + "': expected `" + regex + "'.");
      }

    }
    catch(Exception e)
    {
       fail("glob2regex(" + glob + ") => exception `" + e + "'.");
    }
  }

  public void test_glob2regex_01() { _expect( "*",     ".*"       ); }
  public void test_glob2regex_02() { _expect( "*.*",   ".*\\..*"  ); }
  public void test_glob2regex_03() { _expect( "",      ""         ); }
  public void test_glob2regex_04() { _expect( "?",     "."        ); }
  public void test_glob2regex_05() { _expect( ".",     "\\."      ); }
  public void test_glob2regex_06() { _expect( "[^]",   "[\\^]"    ); }
  public void test_glob2regex_07() { _expect( "[^a]",  "[\\^a]"   ); }
  public void test_glob2regex_08() { _expect( "[!a]",  "[^a]"     ); }
  public void test_glob2regex_09() { _expect( "[!]",   "\\[\\!\\]"); }
}


