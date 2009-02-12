package net.haefelingerit.flaka;

import java.io.ByteArrayInputStream;
import java.util.Properties;

import org.apache.tools.ant.BuildException;

public class Property extends org.apache.tools.ant.taskdefs.Property
{
  protected String text;

  /**
   * The name of the property to set.
   * 
   * @param name
   *          property name
   */

  public static String normalize(String text) {
    /*
     * replace every occurrance of '\' by '/', except '\\' which maps as '\'
     */
    char c, c1;
    StringBuffer buf = new StringBuffer();
    int len = text.length();

    for (int i = 0; i < len; ++i) {
      c = text.charAt(i);
      if (c != '\\') {
        buf.append(c);
        continue;
      }
      if (i + 1 >= len) {
        buf.append('/');
        continue;
      }
      c1 = text.charAt(i + 1);
      if (c1 != '\\') {
        buf.append('/');
        buf.append(c1);
        i += 1;
        continue;
      }
      // we have '\\' => '\'
      buf.append('\\');
      i += 1;
    }
    return buf.toString();
  }

  public void setText(String text) {
    this.text = normalize(text);
  }

  public String getText() {
    return this.text;
  }

  /**
   * set the property in the project to the value. if the task was give a file,
   * resource or env attribute here is where it is loaded
   */
  public void execute() throws BuildException {
    if (getProject() == null) {
      throw new IllegalStateException("project has not been set");
    }

    if (this.text != null) {
      byte[] buf = this.text.getBytes();
      ByteArrayInputStream S = new ByteArrayInputStream(buf);
      Properties P = new Properties();
      try {
        P.load(S);
        addProperties(P);
      }
      catch (Exception e) {
        Static.throwbx("setting properties failed", e);
        return;
      }
    } else {
      super.execute();
    }
  }
}
