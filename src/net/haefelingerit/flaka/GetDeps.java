package net.haefelingerit.flaka;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import net.haefelingerit.flaka.dep.Dependency;
import net.haefelingerit.flaka.dep.Reader;
import net.haefelingerit.flaka.dep.Retriever;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileList;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;


public class GetDeps extends Task
{
  protected List   list = new ArrayList();
  protected String src  = null;
  protected File   dstdir;
  protected String stem = "deps.";
  protected File   baseline;
  protected URL[]  depotlist;

  public GetDeps() {
    super();
  }

  /**
   * Change the Baseline to use when resolving aliased dependencies.
   * 
   * @param f
   */
  public void setBaseline(File f) {
    this.baseline = f;
  }

  /**
   * Sets the location of the dependencies descriptor (XML file)
   * 
   * @param src
   */
  public void setSrc(String src) {
    this.src = src;
  }

  /**
   * Sets the local file destination
   * 
   * @param file
   */
  public void setDst(File file) {
    this.dstdir = file;
  }

  /**
   * Sets the remote repository
   * 
   * @param string
   */
  public void setDepotList(String s) {
    this.depotlist = toURLs(s);
  }

  public String getDepotList() {
    StringBuffer buf = new StringBuffer();
    for (int i = 0; i < this.depotlist.length; ++i) {
      if (i > 0)
        buf.append(',');
      buf.append(this.depotlist[i].toString());
    }
    return buf.toString();
  }

  public void setStem(String x) {
    String s = x;
    if (s == null)
      return;
    if (!s.endsWith("."))
      s += ".";
    /* stem ends in "." */
    this.stem = s;
  }

  protected String getStem() {
    return this.stem;
  }

  protected Dependency get(int i) {
    Dependency r = null;
    try {
      r = (Dependency) this.list.get(i);
    }
    catch (Exception e) {
      /* ignore */
    }
    return r;
  }

  public int size() {
    return this.list.size();
  }

  public Iterator iterator() {
    return this.list.iterator();
  }

  public Dependency[] toArray() {
    int i;
    Iterator it;
    Dependency[] buf;

    buf = new Dependency[this.size()];
    it = this.iterator();

    for (i = 0; i < buf.length; ++i)
      buf[i] = (Dependency) it.next();
    return buf;
  }

  public Path toPath() {
    Project P = project();
    Path path = new Path(P);
    Path p;
    String s;
    Dependency[] buf = toArray();
    Dependency d;

    for (int i = 0; i < buf.length; ++i) {
      d = buf[i];
      s = this.dstdir.toString();
      s += File.separator;
      s += d.basename();
      p = new Path(P, s);
      path.add(p);
    }
    return path;
  }

  public FileSet toFileSet() {
    Dependency d;
    FileSet FS = new FileSet();
    Iterator i = this.iterator();
    while (i.hasNext()) {
      String s;
      d = (Dependency) i.next();
      s = d.basename();
      FS.createInclude().setName(s);
    }
    return FS;
  }

  public FileList toFileList() {
    FileList FL = new FileList();
    String s = null;
    Iterator i = this.list.iterator();
    Dependency d;
    for (; i.hasNext();) {
      d = (Dependency) i.next();
      if (s == null)
        s = d.basename();
      else
        s += "," + d.basename();
    }
    FL.setDir(new File("."));
    FL.setFiles(s);
    return FL;
  }

  /** Resolve dependencies (i.e. try to download them ..) */
  private void retrieve() {
    Retriever retriever;
    /* check whether mandatory attribs are present and make sense */

    /* make 'em' */
    retriever = new Retriever(getProject());

    debug("local lib dir is `" + this.dstdir + "'");
    retriever.setLocalDir(this.dstdir);

    /* how ugly .. */
    retriever.setRepositories(this.depotlist);

    retriever.retrieve(this.toArray());
  }

  /**
   * Parses the string list of URLs given
   * 
   * @return a List of URLs in String form
   * @throws BuildException
   *           if ??
   */
  static public URL[] toURLs(String repostr) throws BuildException {
    if (repostr == null || repostr.trim().length() == 0) {
      /* no or empty repository -> empty url list */
      return null;
    }
    /* split string by seperator */
    String[] cand = repostr.split(",");
    ArrayList list = new ArrayList();

    for (int i = 0; i < cand.length; ++i) {
      String repo;

      repo = cand[i].trim();
      if (repo.equals("")) {
        continue;
      }

      /* check - must be a valid url .., otherwise we skip */
      try {
        URL url = new URL(repo);
        list.add(url);
      }
      catch (MalformedURLException e) {
        /* we also try this whether we can make a URL */
        try {
          URL url = new URL("file://localhost/" + repo);
          list.add(url);
        }
        catch (MalformedURLException e2) {
          Static.throwbx("URL `" + repo + "' is malformed", e);
        }
      }
    }
    URL[] R = new URL[list.size()];
    for (int j = 0; j < R.length; ++j)
      R[j] = (URL) list.get(j);
    return R;
  }

  protected void makerefids() {
    /* get stem (i.e. prefix) used for all created reference ids */
    String stm = getStem();

    /* deps as classpath suitable to for target <javac> etc */
    setid(stm + "classpath", toPath());
    /* deps as set of (local) files */
    setid(stm + "fileset", toFileSet());
    /* deps as list of (local) files */
    setid(stm + "filelist", toFileList());
    /* the deps object itself */
    setid(stm + "object", toArray());

    /* scan all scopes and assign deps to each scopes */
    /* then create a patternset for each scope */
  }

  private int scandeps(String src, Collection list) {
    String[] path;
    /* split by comma */
    path = src.split(",");
    return scandeps(path, list);
  }

  /**
   * Fetches dependency declarations from a list of files. The list of files is
   * given in argument <code>path</code> as array of strings. Trailing or
   * leading whitespace will be ignored during processing.
   * 
   * Each string is tried in the order imposed by <code>path</code> and if a
   * string is not a file (File.isFile()) or does not exist, then that string
   * will be simply ignored.
   * 
   * Scanning for further dependencies stops if a declaration is found (or if
   * all files are tried). Scanned dependencies are added to list using the
   * Collection.add() method.
   * 
   * @param src
   *          allowed to be null
   * @param list
   *          allowed to be null
   * 
   * @return the last index used in <code>path</code> to scan for
   *         dependencies. If no dependencies could be found, then index
   *         returned will be path.length. If path is null, -1 will be returned.
   */
  private int scandeps(String[] path, Collection list) {
    int i; /* index */

    if (path == null) {
      return -1;
    }
    for (i = 0; i < path.length; ++i) {
      File F;
      ArrayList L;
      Dependency D;

      F = new File(path[i]);
      L = new ArrayList();
      if (scandeps(F, L) > 0) {
        if (list != null) {
          Object[] buf = L.toArray();
          for (int j = 0; j < buf.length; ++j) {
            D = (Dependency) buf[j];
            D.setFile(F);
            list.add(D);
          }
        }
        return i;
      }
    }
    return i;
  }

  private int scandeps(File file, Collection list) {
    int size; /* remember collection's length */

    size = list.size();
    if (file.exists() && file.isFile()) {
      String name;
      InputStream input = null;

      name = file.getAbsolutePath();
      debug("scanning dependencies from `" + name + "' ..");

      try {
        input = new FileInputStream(file);
      }
      catch (Exception e) {
        debug("error while reading `" + name + "' (ignored)", e);
        return 0;
      }
      try {
        /* parse a stream */
        Reader.parse(input, list);
      }
      catch (Exception e) {
        debug("error reading dependencies from `" + name + "' (ignored)", e);
        return 0;
      }
    }

    return (list.size() > size) ? (list.size() - size) : 0;
  }

  /**
   * defines execution of ant task
   */
  public void execute() {
    int i;
    String s;
    Project P;
    /* get project */
    P = project();

    /* use current working directory */
    if (this.dstdir == null) {
      this.dstdir = new File(".");
    }
    if (!this.dstdir.isDirectory()) {
      throwbx("error: attribute \"dst\" does not specify directory as expected: "
          + this.dstdir.getName());
    }
    if (this.baseline != null) {
      if (this.baseline.isFile() == false) {
        throwbx("error: attribute `baseline' - file does not exist: `"
            + this.baseline.getPath() + "'");
      }
    }

    if (this.depotlist == null) {
      this.depotlist = toURLs(getProperty("depot.csv"));
    }

    if (this.depotlist == null) {
      throwbx("error: no retrieval repository - use 'depotlist'");
    }

    /*
     * If no source file is given, use the current build file. The current build
     * file is given by evaluating Ant's system property ant.file.
     */
    if (this.src == null) {
      this.src = getProperty("ant.file");
    }

    scandeps(this.src, this.list);

    if (size() == 0) {
      makerefids();
      return;
    }

    debug("there are " + size() + " dependencies in stock.");

    if (this.baseline != null) {
      Properties Baseline = null;
      FileInputStream fin;

      try {
        fin = new FileInputStream(this.baseline);
        Baseline = new Properties();
        Baseline.load(fin);
        /* evaluate all properties */
        Eval.eval(Baseline, P);
      }
      catch (Exception ex) {
        s = this.baseline.getPath();
        throwbx("error while loading Baseline `" + s + "', got `" + ex + "'");
      }

      /*
       * When building a snapshot, the version of an internal dependency shall *
       * be SNAPSHOT regardless of the Baseline. This clashes with the default *
       * implementation used so far. For a smooth transition, the new behaviour *
       * will be enabled if property "updatemode" is set to snapshot.
       */

      boolean snupdate = false;
      String epop, vers, msg;

      s = this.getProject().getProperty("updatemode");
      if (s != null && s.equals("snapshot")) {
        msg = "update mode set to \"snapshot\" ..";
        info(msg);
        snupdate = true;
      }

      if (snupdate) {
        for (i = 0; i < size(); ++i) {
          epop = null;
          vers = null;

          s = get(i).getAlias();
          if (s != null && Baseline != null) {
            vers = get(i).getVersion();
            epop = Baseline.getProperty(s + ".epop", null);
            if (vers == null && epop != null) {
              msg = s + " (made by " + epop + ") => version=SNAPSHOT";
              info(msg);
              get(i).setVersion("SNAPSHOT");
            }
          }
        }
      }

      /* We need to resolve each dependency against the Baseline. */
      for (i = 0; i < size(); ++i) {
        s = get(i).getAlias();
        if (s != null) {
          debug("resolving dependency `" + s + "' ..");
          get(i).resolve(Baseline);
        }
      }
    }

    /*
     * Having resolved each dependency, we check whether the dependeny * is
     * valid. This is exactly the case if "basename()" is not "".
     */
    int baddeps = 0;

    for (i = 0; i < size(); ++i) {
      s = get(i).basename();
      if (s == null || s.trim().equals("")) {
        String alias;

        /*
         * This dependency is not valid. If this dependency has an alias * name
         * report that there's no such alias in the Baseline.
         */

        alias = get(i).getAlias();
        if (alias != null) {
          Static.error(project(), "dependency `" + alias
              + "' not found in Baseline.");
        } else {
          Static.error(project(), "empty dependency found.");
        }
        baddeps += 1;
      }
    }

    if (baddeps > 0) {
      throwbx("unable to continue because of " + baddeps
          + " unresolved dependencie(s).");
    }

    retrieve();

    /* display retrieval result */
    int found = 0;
    for (i = 0; i < size(); ++i) {
      Dependency dep = get(i);
      switch (dep.getStatus()) {
        case Retriever.EXISTS_CURRENT:
          if (dep.getLocalFile() == null)
            throwbx("`" + dep.basename() + "' has no local file attched ..");
          found++;
          break;
        case Retriever.RETRIEVED_REMOTE:
          found++;
          break;
        case Retriever.RETRIEVED_LOCAL_CACHE:
          found++;
          break;
        case Retriever.NOT_RETRIEVED:
          break;
        case Retriever.NOT_RETRIEVED_NOT_FOUND:
          break;
        case Retriever.NOT_RETRIEVED_EXISTS_STALE:
          found++;
          break;
        case Retriever.ILLEGAL_STATE:
          break;
      }
    }
    if (found != size()) {
      String pfx = "     ";
      error("\n\n");
      error(pfx
          + "__DEPENDENCY_RETRIEVAL_ERROR____________________________________________________\n");
      error(pfx + "Failed to retrieve " + (size() - found) + "/" + size()
          + " dependencies from:");
      for (i = 0; i < this.depotlist.length; ++i) {
        error(pfx + "[" + i + "]" + " " + this.depotlist[i].toString());
      }
      error("\n");

      String depotlist = getDepotList();
      Static.throwbx("found " + found + "/" + size() + " dependencies from "
          + depotlist);
    }

    /* create reference ids for further usage .. */
    makerefids();
  }
}