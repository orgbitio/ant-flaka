package net.haefelingerit.flaka;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.tools.ant.types.selectors.BaseSelector;

public class IsText extends BaseSelector
{
  /** upper limit of characters to investige. */
  protected long    limit  = -1;

  /** invert selection */
  protected boolean invert = false;

  /** set limit */
  public void setLimit(long n) {
    this.limit = n;
  }

  public void setInvertMatch(boolean b) {
    this.invert = b;
  }

  public void setInvert(boolean b) {
    this.invert = b;
  }

  /**
   * * Implements a selector to restrict a given fileset to contain * "textual"
   * files only. * *
   * 
   * @param basedir
   *          directory containing <code>filename</code> *
   * @param filename
   *          name of file *
   * @param file
   *          the file as File object * *
   * @return true if <code>file</code> is a textual file.
   */

  public boolean isSelected(File basedir, String filename, File file) {
    String path;
    InputStream S;
    boolean retv = false;

    if (file == null || basedir == null || filename == null) {
      debug("isText: some `nil' arguments seen, return `false'");
      return false;
    }

    path = file.getAbsolutePath();

    if (file.isDirectory()) {
      debug("`" + path + "` is a directory, return `false'");
      return false;
    }

    if (!file.canRead()) {
      debug("`" + path + "` is not readable, return `false'");
      return false;
    }

    S = open(file);
    if (S == null) {
      debug("unable to open `" + path + "`, return `false'");
      return false;
    }

    try {
      retv = istext(S, this.limit);
      debug("istext('" + path + "') = " + retv);
      retv = this.invert ? !retv : retv;
    }
    catch (Exception e) {
      debug("error while reading from `" + path + "`", e);
    }
    finally {
      if (!close(S))
        debug("unable to close `" + path + "` (error ignored).");
    }
    return retv;
  }

  protected boolean istext(InputStream S, long max) throws Exception {
    int c;
    boolean b;

    c = S.read();
    b = true;

    if (max < 0) {
      while (b && c != -1) {
        b = Static.istext((char) c);
        c = S.read();
      }
    } else {
      for (long i = 0; b && i < max; ++i) {
        b = Static.istext((char) c);
        c = S.read();
      }
    }
    return b;
  }

  protected boolean isbinary(InputStream S, long max) throws Exception {
    return istext(S, max) ? false : true;
  }

  protected InputStream open(File file) {
    InputStream retv = null;
    try {
      retv = new FileInputStream(file);
      retv = new BufferedInputStream(retv);
    }
    catch (Exception e) {
      /* ignore */
    }
    return retv;
  }

  protected boolean close(InputStream S) {
    boolean b = true;
    try {
      if (S != null)
        S.close();
    }
    catch (Exception e) {
      b = false;
    }
    return b;
  }

  protected void debug(String msg) {
    Static.debug(getProject(), "istext: " + msg);
  }

  protected void debug(String msg, Exception e) {
    Static.debug(getProject(), "istext: " + msg, e);
  }
}
