package net.haefelingerit.flaka;

import net.haefelingerit.flaka.tel.Binary;
import net.haefelingerit.flaka.tel.BinaryImpl;
import net.haefelingerit.flaka.tel.TestEL;
import net.haefelingerit.flaka.tel.Unary;
import net.haefelingerit.flaka.tel.UnaryImpl;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectComponent;
import org.apache.tools.ant.taskdefs.condition.Condition;


public class Test extends ProjectComponent implements Condition
{
  protected String test  = null;
  protected char   quote = '\'';

  static private TestEL testel = null;
  
  
  static protected TestEL maketestel() {
    if (Test.testel==null) {
      Unary un = new UnaryImpl();
      Binary bi = new BinaryImpl();
      Test.testel = new TestEL(un,bi);
    }
    return Test.testel;
  }
  
  public void setTest(String s) {
    if (s != null)
      this.test = s.trim();
  }

  public void setQuote(String S) {
    String s = S;
    s = (s == null) ? null : s.trim();
    if (s != null) {
      this.quote = s.charAt(0);
    }
  }

  public boolean eval() throws BuildException {
    // no test evaluates to `false' (standard Python behaviour).
    if (this.test == null)
      return false;

    // execute test ..
    return eval(getProject(), this.test, this.quote);
  }

  public static boolean eval(Project project, String s, char quote) {
    boolean b = true;
    if (s != null && project != null) {
      try {
        b = maketestel().reset(project).eval(s, quote);
      }
      catch (Exception e) {
        throw new BuildException(e);
      }
    }
    return b;
  }

  public static boolean eval(Project project, String s) {
    /* evalute with standard quote character */
    return eval(project, s, '\'');
  }
}
