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
  
  
  static boolean refind(String text)
  {
    Pattern T = Pattern.compile("\\n([^\\n]*)$");
    Matcher t = T.matcher(text);
    boolean b = t.find();
    return b;
  }
  
  public void testMatcher01() {
    boolean b = refind("\n \n   \n;\n\n");
    assertTrue(b);
  }
  
  
  
  static String prettyfy(String text)
  {
    // Match all ws at input's begin (includes \n)
    Pattern S = Pattern.compile("^\\s*");
    // Match everthing after last \n (which must exist)
    Pattern T = Pattern.compile("\\n([^\\n]*)$");
    Matcher s = S.matcher(text);
    // must allways match
    assertTrue(s.find()); 
    // The matching sequence 
    // Possible to avoid taking a substring??
    String prefix = s.group();
    int n = -1;
    Matcher t = T.matcher(prefix);
    if (t.find()) {
       n = t.end(1) - t.start(1);
      assertTrue(n>=0);
    }
    else
    {
      n = s.end() - s.start();
      assertTrue(n == prefix.length());
    } 
    
    String out;
    // Remove leading whitespace characters.
    out = s.replaceFirst("");
    if (n>0)
    {
      // Compile a pattern on the fly. 
      // Matches a newline followed by {1,n} characters. Such a character
      // must a whitespace character except a newline. 
      // Is there a way to express this as a difference operation? Such
      // as {\s - \n} ??
      Pattern U = Pattern.compile("\\n[ \\t\\x0B\\f\\r]{1,"+n+"}");
    
      Matcher u  = U.matcher(out);
      out = u.replaceAll("\n");
    }
    assertNotNull(out);
    return out;
  }
  
  
  public void testRE01() 
  {
    String text = 
      "\n" + 
      "\n" +
      "  ;a \\\n" +
      " b \\\n" +
      "   c \\\n" +
      "    d \\"
      ;
    
    String expect = 
      ";a \\\n" +
      "b \\\n" +
      " c \\\n" +
      "  d \\"
      ;
    
    String out = prettyfy(text);
    assertTrue(out.contentEquals(expect));
  }
  
  public void testRE02() 
  {
    String text = 
      "  ;a \\\n" +
      " b \\\n" +
      "   c \\\n" +
      "    d \\"
      ;
    
    String expect = 
      ";a \\\n" +
      "b \\\n" +
      " c \\\n" +
      "  d \\"
      ;
    
    String out = prettyfy(text);
    assertTrue(out.contentEquals(expect));
  }
  public void testRE03() 
  {
    String text =   "    \n  ;a\n b\n   c";
    String expect = ";a\nb\n c"; 
    String out = prettyfy(text);
    assertTrue(out.contentEquals(expect));
  }
  public void testRE04() 
  {
    String text =   " \n   \n  ;a\n b\n   c";
    String expect = ";a\nb\n c"; 
    String out = prettyfy(text);
    assertTrue(out.contentEquals(expect));
  }
  public void testRE05() 
  {
    String text =   "";
    String expect = ""; 
    String out = prettyfy(text);
    assertTrue(out.contentEquals(expect));
  }
  public void testRE06() 
  {
    String text =   "\n\n";
    String expect = ""; 
    String out = prettyfy(text);
    assertTrue(out.contentEquals(expect));
  }
  public void testRE07() 
  {
    String text =   "\n \n \n \n      ;\n\n";
    String expect = ";\n\n"; 
    String out = prettyfy(text);
    assertTrue(out.contentEquals(expect));
  }
  
  /**
   * Test resolving of continuation lines using regular expressions.
   */
  
  public void testCont01()
  {
    String text = 
      "\n" + 
      "\n" +
      "  ;a \\\n" +
      " b \\\n" +
      "   c \\\n" +
      "    d \\"
      ;
    
    String expect =  "\n\n  ;a  b    c     d ";
    
    // something line "\ nl" is a cont. line. But how about if the
    // last character shall be a "\"? Then it mus be escaped. Thus
    // "\ \ nl" shall not  match.  This requires in addition, that
    // something like "\ ?" is "?".
    
    
    Pattern S = Pattern.compile("\\\\\\n|\\\\$");
    Matcher s = S.matcher(text);
    String out = s.replaceAll("");
    assertEquals(expect, out);
  }
  
  
  static String commentlineRemover(String text)
  {
    // MULTILINE: This should change the meaning of ^ and $ to match the
    // line termination character as well (as begin and end of input). 
    // However: ^ and $ are boundaries, i.e. they do not match. Instead
    // the matched content is *after* ^ and *before* $.
    // TODO: ws characters
    
    // TODO: bad, removing comment lines via a Regex does not work out.
    Pattern S = Pattern.compile("(\\n|^)[ \\t]*;.*|\\n\\z",Pattern.MULTILINE);
    Matcher s = S.matcher(text);
    String out = s.replaceAll("");
    return out;
  }
  
  public void testComment01() {
    // examples of comment lines (a line starting with ;). The comment line
    // remover should remove *each* comment line. Thus the expected outcome
    // of this text is the empty string.
    String text = 
      ";\n" + 
      ";;\n" +
      "\t;\n" +
      " ;\n" +
      "; \n" + 
      ";; \n" +
      "\t; \n" +
      " ; \n" +
      ";a\n" + 
      ";;a\n" +
      "\t;a\n" +
      " ;a\n" +
      ""
      ;
    
    String expect =  "";
    String out = commentlineRemover(text);
    assertEquals(expect, out);
  }
 
}
