package net.haefelingerit.flaka.dep;

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;


// Regarding Maven (1.02) element <jar> within a dependency:
// The docmentation states that <jar> is used to construct the
// artifact name (without stating what the "name" is supposed to be.
// It turns out that "name" is supposed to be the "basename". When
// playing around with dependencies, Maven 1.0.2 behaves like shown
// in the examples below.
// 
// <dependency>
// <jar>junit-3.8.1.jar</jar> Illegal state
// </dependency>
//
// <dependency>
// <groupId>junit</groupId>
// <jar>junit-3.8.1.jar</jar> Illegal state
// </dependency>
//
// <dependency>
// <groupId>junit</groupId>
// <version>3.8.1</version> Illegal state
// </dependency>
//
// <dependency>
// <groupId>junit</groupId>
// <artifactId>junit</artifactId>
// <jar>junit-3.8.1.jar</jar> Good.
// </dependency>
//
// <dependency>
// <groupId>junit</groupId>
// <artifactId>WHATEVER</artifactId>
// <jar>junit-3.8.1.jar</jar> Good (artifactId, is
// </dependency> overridden).
//
// <dependency>
// <groupId>NO-SUCH-GROUP</groupId>
// <artifactId>WHATEVER</artifactId>
// <jar>junit-3.8.1.jar</jar> Fails to resolve.
// </dependency>
//
// <dependency>
// <groupId>junit</groupId>
// <artifactId>WHATEVER</artifactId>
// <jar>junit-3.8.1</jar> Fails to resolve.
// <type>jar</jar>
// </dependency>
//
// <dependency>
// <groupId>junit</groupId>
// <artifactId>WHATEVER</artifactId>
// <jar>junit-3.8.1.jar</jar> Good.
// <type>jar</jar>
// </dependency>
//
// <dependency>
// <groupId>junit</groupId>
// <artifactId>WHATEVER</artifactId>
// <jar>junit-3.8.1.jar</jar> Fails to resolve.
// <type>war</jar>
// </dependency>
//
// <dependency>
// <groupId>junit</groupId>
// <artifactId>WHATEVER</artifactId>
// <jar>junit-3.8.1.jar</jar> Good (version is
// <type>jar</jar> ignored).
// <version>xxx</version>
// </dependency>
//
public class Dependency
{
  protected short    stat        = 0;            /* current status */
  protected String   alias       = null;         
  protected String   _version;
  protected String   _id;
  protected String   _url;
  protected String   _type;
  protected String   _groupid;
  protected String   _artifactid;
  protected String   _jar;
  protected String[] _scope      = { "compile" };
  protected Map      _properties = new TreeMap(); // properties
  protected URL      _remote;                    // used for display purposes
  protected File     _local      = null;
  protected File     file        = null;

  public void setStatus(short x) {
    this.stat = x;
  }

  public short getStatus() {
    return this.stat;
  }

  public String getAlias() {
    return this.alias;
  }

  public void setAlias(String S) {
    String s = S;
    if (s == null)
      return;
    s = s.trim();
    if (s.length() <= 0)
      return;
    /* we don't normalize here, we take the alias as is */
    this.alias = s;
  }

 

  /**
   * @return id
   */
  public String getId() {
    return this._id;
  }

  /**
   * @return url
   */
  public String getUrl() {
    return this._url;
  }

  /**
   * @return version
   */
  public String getVersion() {
    return this._version;
  }

  /**
   * @param string
   */
  public void setId(String string) {
    this._id = string;
  }

  /**
   * @param string
   */
  public void setUrl(String string) {
    this._url = string;
  }

  /**
   * @param string
   */
  public void setVersion(String string) {
    this._version = string;
  }

  /**
   * @return the type (or extension) of the dependency
   */
  public String getType() {
    return this._type;
  }

  /**
   * Sets the type (or extension) of the dependency
   * 
   * @param string
   */
  public void setType(String string) {
    this._type = string;
  }

  /**
   * @return artifact name
   */
  public String getArtifactId() {
    return this._artifactid;
  }

  /**
   * @return group name
   */
  public String getGroupId() {
    return this._groupid;
  }

  /**
   * @return jar
   */
  public String getJar() {
    return this._jar;
  }

  /**
   * @param string
   */
  public void setArtifactId(String string) {
    this._artifactid = string;
  }

  /**
   * @param string
   */
  public void setGroupId(String string) {
    this._groupid = string;
  }

  /**
   * @param string
   */
  public void setJar(String string) {
    this._jar = string;
  }

  public String[] getScope() {
    return this._scope;
  }

  public void setScope(String s) {
    if (s != null) {
      this._scope = s.split("\\s");
    }
  }

  /**
   * @return file where this dependency has been declared
   */
  public File getFile() {
    return this.file;
  }

  /**
   * @param x
   *          this dependency has been declared in file <code>x</code>.
   */
  public void setFile(File x) {
    this.file = x;
  }

  /**
   * @param file
   */
  public void setLocalFile(File file) {
    this._local = file;
  }

  public File getLocalFile() {
    return this._local;
  }

  /*
   * Returns the basename of the calculated 'file name'. Example: Assume that we
   * have a dependency for
   * 
   * log4j/jars/log4j-1.2.8.jar
   * 
   * Then basename() would return
   * 
   * log4j-1.2.8.jar
   */
  public String basename() {
    String s;

    /*
     * According to Maven's behaviour, "<jar>" overrides any other setting.
     */
    if (this._jar != null) {
      return this._jar;
    }
    s = null;
    if (this._artifactid != null)
      s = this._artifactid;

    if (s == null)
      s = this._id;

    /***************************************************************************
     * we need to have either this._artifactid or this.id to * keep on going.
     * Having neither of them is actually a * illegal dependency, however we
     * cope with it by retur- ing an empty string. *
     **************************************************************************/
    if (s == null)
      return "";

    if (this._version != null) {
      s += "-";
      s += this._version;
    }
    if (this._type != null) {
      s += ".";
      s += this._type;
    } else {
      s += ".jar";
    }
    return s;
  }

  /**
   * Returns the path expected on the remote repository
   * 
   * @return the path which will be used on a remote repository
   */
  public String depotpath() {
    String s;

    /* must have either id or (groupd and (artifact or jar)) */
    if (this._id == null && (this._groupid == null || basename().equals("")))
      return null;

    s = "/";
    if (this._groupid != null)
      s += this._groupid;
    else
      s += this._id;
    s += "/";
    if (this._type != null)
      s += this._type;
    else
      s += "jar";
    s += "s/";
    s += basename();
    return s;
  }

  /**
   * Returns the path expected on the remote repository
   * 
   * @return the path which will be used on a remote repository
   */
  public String depotpath2dotnull() {
    String s;

    /* must have either id or (groupd and (artifact or jar)) */
    if (this._id == null && (this._groupid == null || basename().equals("")))
      return null;

    s = "/";
    if (this._groupid != null)
      s += this._groupid.replace('.', '/');
    else {
      if (this._id != null)
        s += this._id.replace('.', '/');
    }
    s += "/";
    if (this._artifactid != null) {
      s += this._artifactid;
      s += "/";
    }
    if (this._version != null) {
      s += this._version;
      s += "/";
    }
    s += basename();
    return s;
  }

  public String toString() {
    return tostring(false);
  }

  public String toAliased() {
    return tostring(true);
  }

  protected String tostring(boolean aliased) {
    String s;

    s = "<dependency";

    if (aliased && this.getAlias() != null) {
      s += " alias=\"";
      s += this.getAlias();
      s += "\"";
    }
    s += ">\n";

    if (this._id != null)
      s += "<id>" + this._id + "</id>\n";
    if (this._groupid != null)
      s += "<groupId>" + this._groupid + "</groupId>\n";
    if (this._jar != null)
      s += "<jar>" + this._jar + "</jar>\n";
    if (this._artifactid != null)
      s += "<artifactId>" + this._artifactid + "</artifactId>\n";
    if (this._version != null)
      s += "<version>" + this._version + "</version>\n";
    if (this._type != null)
      s += "<type>" + this._type + "</type>\n";
    else
      s += "<type>jar</type>\n";
    if (this._url != null)
      s += "<url>" + this._url + "</url>\n";
    if (this._scope != null) {
      s += "<scope>";
      for (int i = 0; i < this._scope.length; ++i) {
        s += this._scope[i];
        if (i + 1 < this._scope.length) {
          s += " ";
        }
      }
      s += "</scope>\n";
    }
    if (this._properties.size() > 0) {
      Object keyset[] = this._properties.keySet().toArray();
      s += "<properties>\n";
      for (int i = 0; i < keyset.length; ++i) {
        Object k, v;
        String ks, vs;
        k = keyset[i];
        try {
          v = this._properties.get(k);
          ks = k.toString();
          vs = v.toString();
          s += "<" + ks + ">" + vs + "</" + ks + ">\n";
        }
        catch (Exception e) {
          debug("problems while dumping a dependency property.");
          s += "<!-- error on getting a property -->\n";
        }
      }
      s += "</properties>\n";
    }
    s += "</dependency>\n";
    return s;
  }

  /**
   * Sets a property value
   * 
   * @param key
   *          the property key to set
   * @param value
   *          the property value to set
   * @return the property value just set (can be null)
   */
  public String putProperty(String key, String value) {
    if (key == null) {
      return null;
    }
    return (String) this._properties.put(key, value);
  }

  /**
   * Returns a property value
   * 
   * @param key
   *          the property key to retrieve
   * @return a property value, or null if not found
   */
  public String getProperty(String key) {
    if (key == null) {
      return null;
    }
    return (String) this._properties.get(key);
  }

  public int numProperties() {
    return this._properties.size();
  }

  /**
   * @return Returns the downloadSource.
   */
  public URL getDownloadSource() {
    return this._remote;
  }

  /**
   * @param downloadSource
   *          The downloadSource to set.
   */
  public void setDownloadSource(URL downloadSource) {
    this._remote = downloadSource;
  }

  /**
   * Checks this Dep against another dependency for equality
   */
  public boolean equals(Object other) {
    if (other == null) {
      return false;
    }
    if (!this.getClass().equals(other.getClass())) {
      return false;
    }
    Dependency otherDep = (Dependency) other;
    return this.basename().equals(otherDep.basename());
  }

  // protected String eval(Project P,String v)
  // {
  // return P.replaceProperties(v);
  // }

  /**
   * Resolve properties within this dependency ..
   * 
   * @param P
   *          project's properties are used to resolve this dependency.
   * @return number of properties resolved (>=0).
   */

  public int resolve(Properties P) {
    int c = 0;
    /* we can't resolve without properties */
    if (P == null)
      return c;

    /* we can't resolve without have an "alias" for this dependency. */
    if (this.alias == null)
      return c;

    if (this._groupid == null) {
      this._groupid = P.getProperty(this.alias + ".path", null);
      c += (this._groupid == null) ? 0 : 1;
    }
    if (this._artifactid == null) {
      this._artifactid = P.getProperty(this.alias + ".name", null);
      c += (this._artifactid == null) ? 0 : 1;
    }
    if (this._version == null) {
      this._version = P.getProperty(this.alias + ".vers", null);
      c += (this._version == null) ? 0 : 1;
    }
    if (this._url == null) {
      this._url = P.getProperty(this.alias + ".url", null);
      c += (this._url == null) ? 0 : 1;
    }
    if (this._type == null) {
      this._type = P.getProperty(this.alias + ".type", null);
      c += (this._type == null) ? 0 : 1;
    }
    if (this._jar == null) {
      this._jar = P.getProperty(this.alias + ".jar", null);
      c += (this._jar == null) ? 0 : 1;
    }
    if (this._id == null) {
      this._id = P.getProperty(this.alias + ".id", null);
      c += (this._id == null) ? 0 : 1;
    }
 

    /*
     * We have not been able to resolve the type of this * dependency via
     * properties. In this case we try to * derive the type from the alias name
     * (should have * the format "<alias>.<type>".
     */

    if (this._type == null) {
      int index;
      this._type = "";

      index = this.alias.lastIndexOf('.');
      if (index >= 0) {
        try {
          /* index+1 could be out-of-range */
          this._type = this.alias.substring(index + 1);
        }
        catch (Exception e) {
          // do nothing
        }
      }
      /* if we can't derive a type, fall back to "jar" */
      this._type = this._type.trim();
      if (this._type.length() <= 0) {
        this._type = "jar";
      }
    }

    return c;
  }

  protected void debug(String s) {
    System.err.println("error: " + s);
  }
}
