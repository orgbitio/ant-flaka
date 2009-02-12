/* --*- java -*-- */
// :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 51 Franklin St, Fifth Floor, Boston,
// MA 02110-1301 USA
//
// Copyright 2006 Wolfgang Haefelinger
// :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
package net.haefelingerit.flaka.tel;

import java.io.File;

import net.haefelingerit.flaka.Static;

import org.apache.tools.ant.Project;


public class UnaryImpl implements Unary
{
  private Project project;

  public Unary reset(Project project) {
    this.project = project;
    return this;
  }
  
  protected void debug(String msg) {
    if (this.project != null) {
      this.project.log(msg,Project.MSG_DEBUG);
    }
  }
  
  /* Return true if OP is one of the test command's unary operators. */
  public boolean isunary(String s) {
    if (s.charAt(0) != '-')
      return false;
    switch (s.charAt(1)) {
      case 'a':
      case 'b':
      case 'c':
      case 'd':
      case 'e':
      case 'f':
      case 'g':
      case 'h':
      case 'k':
      case 'n':
      case 'o':
      case 'p':
      case 'r':
      case 's':
      case 't':
      case 'u':
      case 'w':
      case 'x':
      case 'z':
      case 'G':
      case 'L':
      case 'O':
      case 'S':
      case 'N':
      case 'P':
      case 'R':
      case 'T':
      case 'M':
      case 'm':
      case 'i':
      case 'C':
      case 'E':
        return true;
    }

    return false;
  }

  
  protected boolean test_not_supported(String s, String arg) throws Exception {
    int a = 1;
    if (a == 1)
      throw new Exception("error - test `" + s + " " + arg + "' not supported");
    return false;
  }

  
  public boolean test_x(String s) throws Exception {
    /* FILE exists and is executable */
    return test_not_supported("-x", s);
  }

  public boolean test_O(String s) throws Exception {
    /* FILE exists and is owned by the effective user ID */
    return test_not_supported("-0", s);
  }

  public boolean test_G(String s) throws Exception {
    /* FILE exists and is owned by the effective group ID */
    return test_not_supported("-G", s);
  }

  public boolean test_f(String s) throws Exception {
    /* FILE exists and is a regular file */
    return toFile(s).isFile();
  }

  /**
   * Turn a string into a File.
   * 
   * This method takes care of relative paths. Being relative, the arguments
   * must be interpreted in terms of the current project's base directory and
   * not according to the current working directory (given by the JVM).
   */
  public File toFile(String s) {
    File f = new File(s.trim());
    if (f.isAbsolute() == false) {
      f = this.project.getBaseDir();
      f = new File(f,s);
    }
    return f;
  }
  public boolean test_d(String s) throws Exception {
    if (s==null || s.length()==0)
      return false;
    return toFile(s).isDirectory();  
  }

  public boolean test_s(String s) throws Exception {
    // TODO: refactor me => Static.
    /* FILE exists and has a size greater than zero */
    File f = toFile(s);
    // TODO: test behaviour on folders, symlinks, other file types.
    return f.exists() && f.length() > 0; /* directories ?? */
  }

  public boolean test_S(String s) throws Exception {
    /* FILE exists and is a socket */
    return test_not_supported("-S", s);
  }

  public boolean test_c(String s) throws Exception {
    /* FILE exists and is character special */
    return test_not_supported("-c", s);
  }

  public boolean test_b(String s) throws Exception {
    /* FILE exists and is block special */
    return test_not_supported("-b", s);
  }

  public boolean test_p(String s) throws Exception {
    /* FILE exists and is a named pipe */
    return test_not_supported("-p", s);
  }

  public boolean test_h(String s) throws Exception {
    /* FILE exists and is a symbolic link (same as -L) */
    return test_not_supported("-h", s);
  }

  public boolean test_u(String s) throws Exception {
    /* FILE exists and its set-user-ID bit is se */
    return test_not_supported("-u", s);
  }

  public boolean test_g(String s) throws Exception {
    /* FILE exists and is set-group-ID */
    return test_not_supported("-g", s);
  }

  public boolean test_k(String s) throws Exception {
    /* FILE exists and has its sticky bit set */
    return test_not_supported("-k", s);
  }

  public boolean test_n(String s) throws Exception {
    return s.length() != 0;
  }

  public boolean test_z(String s) throws Exception {
    boolean b = (s.length() == 0);
    return b;
  }

 
  public boolean test_e(String s) throws Exception {
    return toFile(s).exists();
  }

  public boolean test_r(String s) throws Exception {
    return toFile(s).canRead();
  }

  public boolean test_w(String s) throws Exception {
    return toFile(s).canWrite();
  }
  
  public boolean test_P(String s) throws Exception {
    return Static.isproperty(this.project, s);
  }
  
  /**
   * Test whether a property does not exist or is empty (matches an empty string).
   * @param s The property to be tested.
   * @return true of property does not exist or contains whitespace only
   * @throws Exception
   */
  public boolean test_E(String s) throws Exception {
    return Static.isEmpty(this.project.getProperty(s));
   }

  public boolean test_R(String s) throws Exception {
    return Static.isreference(this.project, s);
  }

  public boolean test_T(String s) throws Exception {
    return Static.istarget(this.project, s);
  }

  public boolean test_M(String s) throws Exception {
    return Static.ismacroOrtask(this.project, s);
  }

  public boolean test_m(String s) throws Exception {
    return Static.ismacro(this.project, s);
  }

  public boolean test_t(String s) throws Exception {
    return Static.istask(this.project, s);
  }

  public boolean test_i(String s) throws Exception {
    boolean b = true;
    try {
      Integer.parseInt(s);
    }
    catch (Exception e) {
      b = false;
    }
    return b;
  }

  public boolean test_C(String s) throws Exception {
    ClassLoader cl;
    boolean b;

    b = false;
    try {
      cl = this.getClass().getClassLoader();
      if (cl != null) {
        Class.forName(s, true, cl);
      } else {
        Class.forName(s);
      }
      b = true;
    }
    catch (Exception e) {
      /* ignore */
    }
    return b;
  }

}
