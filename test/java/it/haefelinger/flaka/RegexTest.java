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

  public void testMatcher01()
  {
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
    if (t.find())
    {
      n = t.end(1) - t.start(1);
      assertTrue(n >= 0);
    }
    else
    {
      n = s.end() - s.start();
      assertTrue(n == prefix.length());
    }

    String out;
    // Remove leading whitespace characters.
    out = s.replaceFirst("");
    if (n > 0)
    {
      // Compile a pattern on the fly.
      // Matches a newline followed by {1,n} characters. Such a character
      // must a whitespace character except a newline.
      // Is there a way to express this as a difference operation? Such
      // as {\s - \n} ??
      Pattern U = Pattern.compile("\\n[ \\t\\x0B\\f\\r]{1," + n + "}");

      Matcher u = U.matcher(out);
      out = u.replaceAll("\n");
    }
    assertNotNull(out);
    return out;
  }

  public void testRE01()
  {
    String text = "\n" + "\n" + "  ;a \\\n" + " b \\\n" + "   c \\\n" + "    d \\";

    String expect = ";a \\\n" + "b \\\n" + " c \\\n" + "  d \\";

    String out = prettyfy(text);
    assertTrue(out.contentEquals(expect));
  }

  public void testRE02()
  {
    String text = "  ;a \\\n" + " b \\\n" + "   c \\\n" + "    d \\";

    String expect = ";a \\\n" + "b \\\n" + " c \\\n" + "  d \\";

    String out = prettyfy(text);
    assertTrue(out.contentEquals(expect));
  }

  public void testRE03()
  {
    String text = "    \n  ;a\n b\n   c";
    String expect = ";a\nb\n c";
    String out = prettyfy(text);
    assertTrue(out.contentEquals(expect));
  }

  public void testRE04()
  {
    String text = " \n   \n  ;a\n b\n   c";
    String expect = ";a\nb\n c";
    String out = prettyfy(text);
    assertTrue(out.contentEquals(expect));
  }

  public void testRE05()
  {
    String text = "";
    String expect = "";
    String out = prettyfy(text);
    assertTrue(out.contentEquals(expect));
  }

  public void testRE06()
  {
    String text = "\n\n";
    String expect = "";
    String out = prettyfy(text);
    assertTrue(out.contentEquals(expect));
  }

  public void testRE07()
  {
    String text = "\n \n \n \n      ;\n\n";
    String expect = ";\n\n";
    String out = prettyfy(text);
    assertTrue(out.contentEquals(expect));
  }

  /**
   * Test resolving of continuation lines using regular expressions.
   */

  public void testCont01()
  {
    String text = "\n" + "\n" + "  ;a \\\n" + " b \\\n" + "   c \\\n" + "    d \\";

    String expect = "\n\n  ;a  b    c     d ";

    // something line "\ nl" is a cont. line. But how about if the
    // last character shall be a "\"? Then it mus be escaped. Thus
    // "\ \ nl" shall not match. This requires in addition, that
    // something like "\ ?" is "?".

    Pattern S = Pattern.compile("\\\\\\n|\\\\$");
    Matcher s = S.matcher(text);
    String out = s.replaceAll("");
    assertEquals(expect, out);
  }

  /**
   * 
   */
  public void testDollar()
  {
    Pattern P;
    Matcher m;
    String s;
    int c;

    // The next series demonstrates, that dollar ($) matches the end of
    // input and additionally (??) a newline before end-of-input. Any
    // other newline chars are not matched. Note that $ is supposed to
    // match a position before ..

    P = Pattern.compile("$");

    // Matches \eof
    s = "";
    m = P.matcher(s);
    c = 0;
    while (m.find())
    {
      c = c + 1;
    }
    assertEquals(1, c);

    // Matches \eol and then \eof
    s = "foobar\n";
    m = P.matcher(s);
    c = 0;
    while (m.find())
    {
      c = c + 1;
    }
    assertEquals(2, c);

    // Matches the last \eol and \eof
    s = "foo\nbar\n";
    m = P.matcher(s);
    c = 0;
    while (m.find())
    {
      c = c + 1;
      switch (c)
      {
        case 1:
          // The last \eol
          assertEquals(7, m.end());
          break;
        case 2:
          // The \eof
          assertEquals(8, m.end());
          break;
      }
    }
    assertEquals(2, c);

    // As we see later, ^ can't match after the last character
    // cause there is no position to go. However, as we learn
    // now, $ can match very well before ..
    s = "\n";
    m = P.matcher(s);
    c = 0;
    while (m.find())
    {
      c = c + 1;
      switch (c)
      {
        case 1:
          // matches position before \eol, well that must then be
          // \begin-of-input. How can this be?
          assertEquals(0, m.end());
          break;
        case 2:
          // matches before \eof
          assertEquals(1, m.end());
          break;
      }
    }
    assertEquals(2, c);

    s = "foo\r\nbar";
    m = P.matcher(s);
    c = 0;
    while (m.find())
    {
      c = c + 1;
    }
    assertEquals(1, c);
  }

  /**
   * 
   */
  public void testMultilineDollar()
  {
    Pattern P;
    Matcher m;
    String s;
    int c;

    // Multiline Dollar matches indeed any newline sequence
    // in addition to \eof. See below.
    
    P = Pattern.compile("(?m)$");

    // Matches \eof
    s = "";
    m = P.matcher(s);
    c = 0;
    while (m.find())
    {
      c = c + 1;
      switch (c)
      {
        case 1:
          // The last \eol
          assertEquals(0, m.end());
          break;
      }
    }
    assertEquals(1, c);

    // Matches \eol and then \eof
    s = "foobar\n";
    m = P.matcher(s);
    c = 0;
    while (m.find())
    {
      c = c + 1;
      switch (c)
      {
        case 1:
          // The last \eol
          assertEquals(6, m.end());
          break;
        case 2:
          // The last \eol
          assertEquals(7, m.end());
          break;
      }
    }
    assertEquals(2, c);

    // Matches the last \eol and \eof
    s = "foo\nbar\n";
    m = P.matcher(s);
    c = 0;
    while (m.find())
    {
      c = c + 1;
      switch (c)
      {
        case 1:
          // The last \eol
          assertEquals(3, m.end());
          break;
        case 2:
          // The \eof
          assertEquals(7, m.end());
          break;
        case 3:
          // The \eof
          assertEquals(8, m.end());
          break;
      }
    }
    assertEquals(3, c);

 
    s = "\n";
    m = P.matcher(s);
    c = 0;
    while (m.find())
    {
      c = c + 1;
      switch (c)
      {
        case 1:
          // matches before first \eol -> how is that possible
          // given that ^ can't match *after* the last \eol???
          assertEquals(0, m.end());
          break;
        case 2:
          // matches before \eof 
          assertEquals(1, m.end());
          break;
      }
    }
    assertEquals(2, c);

    s = "a\n";
    m = P.matcher(s);
    c = 0;
    while (m.find())
    {
      c = c + 1;
      switch (c)
      {
        case 1:
          // matches the position *before* first \eol
          assertEquals(1, m.end());
          break;
        case 2:
          // matches the position *before* \eof
          assertEquals(2, m.end());
          break;
      }
    }
    assertEquals(2, c);
    
    
    s = "foo\r\nbar";
    m = P.matcher(s);
    c = 0;
    while (m.find())
    {
      c = c + 1;
    }
    assertEquals(2, c);
  }

  /**
   * Test caret's (^) behaviour in a Regex.
   * 
   */
  public void testCaret()
  {
    Pattern P;
    Matcher m;
    String s;
    int c;

    // The following serious demonstrates, that a caret matches
    // at the input's begin. It does not, as Pattern's javadoc
    // states, match at the begin of a line. We can make ^ match
    // after any newline in multiline mode, see the next series
    // for details.

    // ^ matches at begin of input
    P = Pattern.compile("^");
    s = "foobar";
    m = P.matcher(s);
    c = 0;
    while (m.find())
    {
      c = c + 1;
    }
    assertEquals(1, c);

    // ^ matches at begin of input
    s = "foo\nbar";
    m = P.matcher(s);
    c = 0;
    while (m.find())
    {
      c = c + 1;
    }
    assertEquals(1, c);

    s = "foo\nbar\n";
    m = P.matcher(s);
    c = 0;
    while (m.find())
    {
      c = c + 1;
    }
    assertEquals(1, c);

    // Multiline mode
    //
    // We can change the meaning of ^ to match after any newline and
    // at the begin of input by going to multiline mode.
    // HOWEVER:
    // It is not quite right to say that we match after any newline:
    // A special case is a newline followed by end-of-input. Notice
    // that boundary characters do not match characters but positions.
    // Still, there must be a valid position after the last \eol. One
    // could argue that \eof is such a character - while Java's regex
    // engine and other do not treat \eof as virtual character. Thus
    // there is no position after the \eol\eof and thus this newline
    // is NOT matched.
    // This is all demonstrated by the next series...

    P = Pattern.compile("(?m)^");

    // Matches at the input's begin.
    s = "foobar";
    m = P.matcher(s);
    c = 0;
    while (m.find())
    {
      c = c + 1;
    }
    assertEquals(1, c);

    // Here we would assume, that ^ also matches after the final newline
    // character. However, it does not cause there is *no* position after
    // the last newline ..
    s = "foobar\n";
    m = P.matcher(s);
    c = 0;
    while (m.find())
    {
      c = c + 1;
    }
    assertEquals(1, c);

    // Compare this with the previous test. Now there is something after
    // the final newline, and consequently we should end up with two
    // matches.
    s = "foobar\n ";
    m = P.matcher(s);
    c = 0;
    while (m.find())
    {
      c = c + 1;
    }
    assertEquals(2, c);

    // This should also match 2 times ..
    s = "foo\nbar\n";
    m = P.matcher(s);
    c = 0;
    while (m.find())
    {
      c = c + 1;
    }
    assertEquals(2, c);

    // This should then match 3 times ..
    s = "foo\nbar\n ";
    m = P.matcher(s);
    c = 0;
    while (m.find())
    {
      c = c + 1;
    }
    assertEquals(3, c);
  }

  public void testZ()
  {
    Pattern P;
    Matcher m;
    String s;
    int c;

    // \Z behaves like "$" in regular mode, regarless of any mode,
    // see also test case for multiline \Z below.
    P = Pattern.compile("\\Z");

    // Matches \eof
    s = "";
    m = P.matcher(s);
    c = 0;
    while (m.find())
    {
      c = c + 1;
    }
    assertEquals(1, c);

    // Matches \eol and then \eof
    s = "foobar\n";
    m = P.matcher(s);
    c = 0;
    while (m.find())
    {
      c = c + 1;
    }
    assertEquals(2, c);

    // Matches the last \eol and \eof
    s = "foo\nbar\n";
    m = P.matcher(s);
    c = 0;
    while (m.find())
    {
      c = c + 1;
      switch (c)
      {
        case 1:
          // The last \eol
          assertEquals(7, m.end());
          break;
        case 2:
          // The \eof
          assertEquals(8, m.end());
          break;
      }
    }
    assertEquals(2, c);

    // As we see later, ^ can't match after the last character
    // cause there is no position to go. However, as we learn
    // now, $ can match very well before ..
    s = "\n";
    m = P.matcher(s);
    c = 0;
    while (m.find())
    {
      c = c + 1;
      switch (c)
      {
        case 1:
          // matches begin-of-input
          assertEquals(0, m.end());
          break;
        case 2:
          // matches the position *before* \eol (that would then
          // be -1 according to me) plus one (from end()) should
          // give '0' while we end up in '1'. Why? no clue.
          assertEquals(1, m.end());
          break;
      }
    }
    assertEquals(2, c);
  }

  public void testMultilineZ()
  {
    Pattern P;
    Matcher m;
    String s;
    int c;

    // \Z behaves like "$" in regular mode, regarless of any mode.
    P = Pattern.compile("(?m)\\Z");

    // Matches \eof
    s = "";
    m = P.matcher(s);
    c = 0;
    while (m.find())
    {
      c = c + 1;
    }
    assertEquals(1, c);

    // Matches \eol and then \eof
    s = "foobar\n";
    m = P.matcher(s);
    c = 0;
    while (m.find())
    {
      c = c + 1;
    }
    assertEquals(2, c);

    // Matches the last \eol and \eof
    s = "foo\nbar\n";
    m = P.matcher(s);
    c = 0;
    while (m.find())
    {
      c = c + 1;
      switch (c)
      {
        case 1:
          // The last \eol
          assertEquals(7, m.end());
          break;
        case 2:
          // The \eof
          assertEquals(8, m.end());
          break;
      }
    }
    assertEquals(2, c);

    // As we see later, ^ can't match after the last character
    // cause there is no position to go. However, as we learn
    // now, $ can match very well before ..
    s = "\n";
    m = P.matcher(s);
    c = 0;
    while (m.find())
    {
      c = c + 1;
      switch (c)
      {
        case 1:
          // matches begin-of-input
          assertEquals(0, m.end());
          break;
        case 2:
          // matches the position *before* \eol (that would then
          // be -1 according to me) plus one (from end()) should
          // give '0' while we end up in '1'. Why? no clue.
          assertEquals(1, m.end());
          break;
      }
    }
    assertEquals(2, c);
  }
}
