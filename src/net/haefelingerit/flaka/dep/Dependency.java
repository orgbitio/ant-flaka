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

package net.haefelingerit.flaka.dep;

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

import org.apache.tools.ant.Project;

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
  final static public int UDM_REGULAR = 0;
  final static public int UDM_SNAPSHOT = 1;
  final static public int UDM_SNAPSHOT_STRICT = 2;
  // status codes
  // This dependency is in it's initial state, no artefact is associated.
  public static final short UNKOWN = 0;
  // This dependency needs to be resolved against a Baseline, no artefact
  // associated.
  public static final short UNRESOLVED = 1;
  // This dependency has been retrieved from a remote location; artefact
  // is associated
  public static final short RETRIEVED_REMOTE = 2;
  // The dep was found in the file cache
  public static final short RETRIEVED_CACHE = 3;
  // The dependency exists locally and is up-to-date
  public static final short ISCURRENT = 4;
  // The dependency could not be found either on the network or
  // in the file cache or file directory
  public static final short NOTFOUND = 5;
  // The dependency exists but cannot be
  // downloaded from the given URL
  public static final short IOERROR_REMOTE = 6;
  // The dependency exists in a cache but cannot be
  // copied over
  public static final short IOERROR_CACHE = 7;
  public static final short ILLEGAL_STATE = 8;

  /* The status of this dependency regarding retrieval */
  protected short stat = UNKOWN;
  /* The logical name of this dependency (if any) */
  protected String alias = null;
  /* The revision/version */
  protected String rev;
  /* The type */
  protected String type;
  /* The group */
  protected String group;
  /* The name */
  protected String name;
  /* The alternative name-rev for irregular typed names */
  protected String jar;
  /* The usage scopes of this dependency */
  protected String[] scope = { "compile" };
  /* Additional properties */
  protected Map props = null;
  /* The materialized dependency */
  protected File file = null;
  /* The location of this dependency's declaration */
  protected String loc = null;
  /* The alternative location from where to get the artifact */
  protected String alt;
  /* The url used for remote retrieval */
  protected URL url;
  /* The update mode */
  public byte mode = UDM_REGULAR;
  /* Error message */
  public String errmsg = null;
  /* This is the task which created this dependency */
  public Project proj;

  public Dependency(Project proj)
  {
    super();
    this.proj = proj;
  }

  public Object clone()
  {
    Dependency d = new Dependency(this.proj);
    d.alias = this.alias;
    d.stat = this.stat;
    d.rev = this.rev;
    d.type = this.type;
    d.group = this.group;
    d.name = this.name;
    d.jar = this.jar;
    d.scope = this.scope;
    d.props = this.props;
    d.file = this.file;
    d.loc = this.loc;
    d.alt = this.alt;
    d.url = this.url;
    d.mode = this.mode;
    d.errmsg = this.errmsg;
    return d;
  }

  public void setStatus(short x)
  {
    this.stat = x;
  }

  public short getStatus()
  {
    return this.stat;
  }

  public String getAlias()
  {
    return this.alias;
  }

  public void setAlias(String S)
  {
    String s = S;
    if (s == null)
      return;
    s = s.trim();
    if (s.length() <= 0)
      return;
    /* we don't normalize here, we take the alias as is */
    this.alias = s;
    this.stat = UNRESOLVED;
  }

  /**
   * @return alt
   */
  public URL getURL()
  {
    return this.url;
  }

  public void setURL(URL url)
  {
    this.url = url;
  }

  /**
   * @return alt
   */
  public String getAlt()
  {
    return this.alt;
  }

  /**
   * @return version
   */
  public String getVersion()
  {
    return this.rev;
  }

  /**
   * @param string
   */
  public void setUrl(String string)
  {
    this.alt = string;
  }

  /**
   * @param string
   */
  public void setVersion(String string)
  {
    this.rev = string;
  }

  /**
   * @return the type (or extension) of the dependency
   */
  public String getType()
  {
    return this.type;
  }

  /**
   * Sets the type (or extension) of the dependency
   * 
   * @param string
   */
  public void setType(String string)
  {
    this.type = string;
  }

  /**
   * @return artifact name
   */
  public String getArtifactId()
  {
    return this.name;
  }

  /**
   * @return group name
   */
  public String getGroupId()
  {
    return this.group;
  }

  /**
   * @return jar
   */
  public String getJar()
  {
    return this.jar;
  }

  /**
   * @param string
   */
  public void setArtifactId(String string)
  {
    this.name = string;
  }

  /**
   * @param string
   */
  public void setGroupId(String string)
  {
    this.group = string;
  }

  /**
   * @param string
   */
  public void setJar(String string)
  {
    this.jar = string;
  }

  public String[] getScope()
  {
    return this.scope;
  }

  public void setScope(String s)
  {
    if (s != null)
    {
      this.scope = s.split("\\s");
    }
  }

  /**
   * @return loc where this dependency has been declared
   */
  public String getLocation()
  {
    return this.loc;
  }

  /**
   * @param x
   *          this dependency has been declared in loc <code>x</code>.
   */
  public void setLocation(String x)
  {
    this.loc = x;
  }

  public void setFile(File file)
  {
    this.file = file;
  }

  public File getFile()
  {
    return this.file;
  }

  /*
   * Returns the basename of the calculated 'loc name'. Example: Assume that we
   * have a dependency for
   * 
   * log4j/jars/log4j-1.2.8.jar
   * 
   * Then basename() would return
   * 
   * log4j-1.2.8.jar
   */
  public String basename()
  {
    String s;

    /*
     * According to Maven's behaviour, "<jar>" overrides any other setting.
     */
    if (this.jar != null)
    {
      return this.jar;
    }
    s = null;
    if (this.name != null)
      s = this.name;
    if (s == null)
      s = "?";
    if (this.rev != null)
    {
      s += "-";
      s += this.rev;
    }
    s += ".";
    if (this.type != null)
      s += this.type;
    else
      s += "jar";
    return s;
  }

  /**
   * Returns the path expected on the remote repository
   * 
   * @return the path which will be used on a remote repository
   */
  public String m1path()
  {
    String s;
    String b = basename();

    /* must have either id or (groupd and (artifact or jar)) */
    if (this.group == null || b == null || b.equals(""))
      return null;

    s = "/";
    if (this.group != null)
      s += this.group;
    else
      s += "?";
    s += "/";
    if (this.type != null)
      s += this.type;
    else
      s += "jar";
    s += "s/";
    s += b;
    return s;
  }

  /**
   * Returns the path expected on the remote repository
   * 
   * @return the path which will be used on a remote repository
   */
  public String m2path()
  {
    String s;
    String b = basename();

    /* must have either id or (group and (artifact or jar)) */
    if (this.group == null || b == null || b.equals(""))
      return null;

    s = "/";
    if (this.group != null)
      s += this.group.replace('.', '/');
    else
      s = "?";
    s += "/";
    if (this.name != null)
    {
      s += this.name;
      s += "/";
    }
    if (this.rev != null)
    {
      s += this.rev;
      s += "/";
    }
    s += basename();
    return s;
  }

  public String toString()
  {
    return tostring(false);
  }

  public String toAliased()
  {
    return tostring(true);
  }

  protected String tostring(boolean aliased)
  {
    String s;

    s = "<dependency";

    if (aliased && this.getAlias() != null)
    {
      s += " alias=\"";
      s += this.getAlias();
      s += "\"";
    }
    s += ">\n";

    if (this.group != null)
      s += "<groupId>" + this.group + "</groupId>\n";
    if (this.jar != null)
      s += "<jar>" + this.jar + "</jar>\n";
    if (this.name != null)
      s += "<artifactId>" + this.name + "</artifactId>\n";
    if (this.rev != null)
      s += "<version>" + this.rev + "</version>\n";
    if (this.type != null)
      s += "<type>" + this.type + "</type>\n";
    else
      s += "<type>jar</type>\n";
    if (this.alt != null)
      s += "<url>" + this.alt + "</url>\n";
    if (this.scope != null)
    {
      s += "<scope>";
      for (int i = 0; i < this.scope.length; ++i)
      {
        s += this.scope[i];
        if (i + 1 < this.scope.length)
        {
          s += " ";
        }
      }
      s += "</scope>\n";
    }
    if (this.props != null && this.props.size() > 0)
    {
      Object keyset[] = this.props.keySet().toArray();
      s += "<properties>\n";
      for (int i = 0; i < keyset.length; ++i)
      {
        Object k, v;
        String ks, vs;
        k = keyset[i];
        try
        {
          v = this.props.get(k);
          ks = k.toString();
          vs = v.toString();
          s += "<" + ks + ">" + vs + "</" + ks + ">\n";
        } catch (Exception e)
        {
          this.proj.log("problems while dumping a dependency property.", Project.MSG_DEBUG);
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
  public String putProperty(String key, String value)
  {
    if (key == null)
    {
      return null;
    }
    if (this.props == null)
      this.props = new Properties();
    return (String) this.props.put(key, value);
  }

  /**
   * Returns a property value
   * 
   * @param key
   *          the property key to retrieve
   * @return a property value, or null if not found
   */
  public String getProperty(String key)
  {
    if (key == null || this.props == null)
    {
      return null;
    }
    return (String) this.props.get(key);
  }

  /**
   * Checks this Dep against another dependency for equality
   */
  public boolean equals(Object other)
  {
    if (other == null)
    {
      return false;
    }
    if (!this.getClass().equals(other.getClass()))
    {
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

  public int resolve(Properties P)
  {
    int c = 0;
    /* we can't resolve without properties */
    if (P == null)
      return c;

    /* we can't resolve without have an "alias" for this dependency. */
    if (this.alias == null)
      return c;

    if (this.group == null)
    {
      this.group = P.getProperty(this.alias + ".path", null);
      c += (this.group == null) ? 0 : 1;
    }
    if (this.name == null)
    {
      this.name = P.getProperty(this.alias + ".name", null);
      c += (this.name == null) ? 0 : 1;
    }
    if (this.rev == null)
    {
      this.rev = P.getProperty(this.alias + ".vers", null);
      c += (this.rev == null) ? 0 : 1;
    }
    if (this.alt == null)
    {
      this.alt = P.getProperty(this.alias + ".url", null);
      c += (this.alt == null) ? 0 : 1;
    }
    if (this.type == null)
    {
      this.type = P.getProperty(this.alias + ".type", null);
      c += (this.type == null) ? 0 : 1;
    }
    if (this.jar == null)
    {
      this.jar = P.getProperty(this.alias + ".jar", null);
      c += (this.jar == null) ? 0 : 1;
    }

    /*
     * We have not been able to resolve the type of this dependency via
     * properties. In this case we try to derive the type from the alias name
     * (should have the format "<alias>.<type>".
     */

    if (this.type == null)
    {
      int index;
      this.type = "";

      index = this.alias.lastIndexOf('.');
      if (index >= 0)
      {
        try
        {
          /* index+1 could be out-of-range */
          this.type = this.alias.substring(index + 1);
        } catch (Exception e)
        {
          // do nothing
        }
      }
      /* if we can't derive a type, fall back to "jar" */
      this.type = this.type.trim();
      if (this.type.length() <= 0)
      {
        this.type = "jar";
      }
    }

    /* This dependency is resolved, set proper status now */
    this.setStatus(UNKOWN);
    return c;
  }

}
