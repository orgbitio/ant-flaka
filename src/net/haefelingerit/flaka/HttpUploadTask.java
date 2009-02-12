package net.haefelingerit.flaka;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;

public class HttpUploadTask extends Task
{
  protected File       rcfile;
  protected HttpUpload uploader;
  protected List       filesets;
  protected File       logdir;
  protected String     acceptpat;
  protected String     errorpat;

  public HttpUploadTask() {
    this.uploader = new HttpUpload();
    this.filesets = new ArrayList();
    this.logdir = null;
  }

  public void setAcceptIf(String s) {
    this.acceptpat = Static.trim2(s,this.acceptpat);
  }
  public void setErrorIf(String s) {
    this.errorpat = Static.trim2(s,this.errorpat);
  }
  
  public void setLogdir(File logdir) {
    this.logdir = logdir;
  }

  public void setEndpoint(String S) {
    String s = Static.trim2(S, null);
    if (s != null)
      this.uploader.set("endpoint", s);
  }

  public void setTest(boolean b) {
    this.uploader.set("testonly", b ? "true" : "false");
  }

  public void setCategory(String S) {
    String s = Static.trim2(S, null);
    if (s != null)
      this.uploader.set("category", s);
  }

  public void setRcFile(File file) {
    this.rcfile = file;
  }

  public void setDebug(boolean b) {
    this.uploader.setDebug(b);
  }

  public void addFileset(FileSet set) {
    if (set != null)
    {
      this.filesets.add(set);
    }
  }

  public boolean isTest() {
    return this.uploader.get("testonly", "").matches("\\s*true\\s*");
  }

  public String[] eval(FileSet fs) throws BuildException {
    DirectoryScanner ds;
    File dir;
    String[] files;

    if (fs == null)
      return null;

    ds = fs.getDirectoryScanner(getProject());
    dir = fs.getDir(getProject());
    files = ds.getIncludedFiles();

    for (int i = 0; i < files.length; ++i)
    {
      String s;

      s = files[i];
      files[i] = new File(dir, s).getAbsolutePath();
    }

    return files;
  }

  // The method executing the task
  public void execute() throws BuildException {
    String s;

    if (this.debug)
    {
      /* enable debug output */
      HttpUpload.debug(true);
    }

    if (this.filesets == null)
    {
      throwbx("this task must be used with at least on <fileset>.");
    }

    /* read username and password from a hidden RC file */
    if (this.rcfile != null)
    {
      String[] words;

      s = this.rcfile.getAbsolutePath();
      if (!this.rcfile.exists())
      {
        throwbx("file `" + s + "' does not exist.");
      }
      /* read username and password from this file */
      s = Static.readlines(this.rcfile);
      if (s == null)
      {
        throwbx("unable to read from file `" + s + "'.");
        return;
      }

      /* split into words */
      words = s.split("\\s+");

      if (words == null || words.length < 2)
      {
        throwbx("syntax error while parsing file `" + s + "' -  too few words.");
        return;
      }

      /* format: username password */
      this.uploader.set("user", words[0]);
      this.uploader.set("passwd", words[1]);
    }

    boolean errorseen = false;

    /* TODO: not used right now ..*/
    this.uploader.set("accept-if",this.acceptpat);
    
    for (int i = 0; i < this.filesets.size(); ++i)
    {
      String[] files;

      files = eval((FileSet) this.filesets.get(i));

      if (files == null)
        continue;

      for (int j = 0; j < files.length; ++j)
      {
        File f;
        boolean rc;

        f = new File(files[j]);
        this.uploader.set("filepath", f.getPath());
        rc = this.uploader.upload();

        if (this.logdir != null)
        {
          String buf;
          String name = f.getPath().replaceAll("/|\\.\\.|\\\\|:|\\.","_");
          buf = this.uploader.get("xmlbuf", null);
          savebuf(buf, "upload-" + name + ".xml");
          buf = this.uploader.get("resbuf", null);
          savebuf(buf, "upload-" + name + ".txt");
        }
        if (!rc)
        {
          String errmsg;

          errorseen = true;
          errmsg = this.uploader.getError();
          if (isTest())
            info(errmsg);
          else
            throwbx(errmsg);
        }
        else
        {
          info("file `" + this.uploader.get("filepath", "") + "' uploaded.");
        }
      }
    }
    if (errorseen)
    {
      throwbx("error(s) seen while uploading files.");
    }
  }

  /**
   * Save content of buffer buf into logdir using filename fname.
   * 
   * @param buf
   * @param fname
   * @return true if no error occured
   */
  private boolean savebuf(String buf, String fname) {
    boolean good = true;
    FileOutputStream fo = null;
    File u;

    if (this.logdir != null && buf != null && fname != null)
    {
      u = new File(this.logdir, fname);
      this.debug("writing upload log report: " + u.getAbsolutePath());
      try
      {
        fo = new FileOutputStream(u);
        fo.write(buf.getBytes());
      }
      catch (Exception e)
      {
        good = false;
        this.error("unable to write upload log report: ", e);
      }
      finally
      {
        Static.close(fo);
      }
    }
    return good;
  }
}
