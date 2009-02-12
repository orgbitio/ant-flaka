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

import net.haefelingerit.flaka.Static;

import org.apache.tools.ant.Project;


public class TestEL
{

  final static int   TEST_TRUE    = 0;
  final static int   TEST_FALSE   = 1;
  final static int   TEST_FAILURE = 2;

  protected int      pos;             /*
                                       * The offset of the current argument in
                                       * ARGV.
                                       */
  protected int      argc;            /*
                                       * The number of arguments present in
                                       * ARGV.
                                       */
  protected String[] argv;            /* The argument list. */

  
  protected Project  project;
  protected Unary    unary;
  protected Binary   binary;
  
  public TestEL(Unary unary,Binary binary) {
    this.unary = unary;
    this.binary = binary;
    reset(null);
  }

  public TestEL reset(Project project) {
    // prepare for new test run.
    // TODO: implement me
    this.pos = 0;
    this.argc = 0;
    this.argv = null;
    this.project = project;
    this.unary.reset(project);
    this.binary.reset(project);
    return this;
  }
  
  
  /**
   * @param msg
   */
   protected void debug(String msg) {
    if (this.project != null) {
      this.project.log(msg,Project.MSG_DEBUG);
    }
    else {
      // TODO: use Log4j logger? Or standard Java logger
    }
  }

  protected String testexpr() {
    String s = "";
    for (int i = 0; i < this.argv.length; ++i) {
      s += " " + this.argv[i];
    }
    return s.trim();
  }

  protected void syntax_error(String format) throws Exception {
    throw new Exception("syntax error in `" + testexpr() + "' - " + format);
  }

 
  protected void beyond() throws Exception {
    syntax_error("missing argument after `" + this.argv[this.argc - 1] + "'");
  }

  protected void advance() throws Exception {
    this.pos += 1;
    if (this.pos >= this.argc)
      beyond();
  }

  protected void advance_unchecked() {
    this.pos += 1;
  }

  protected void unary_advance() throws Exception {
    advance();
    this.pos += 1;
  }

  public static boolean ISBLANK(char c) throws Exception {
    return Character.isWhitespace(c);
  }

  static protected boolean STREQ(String a, String b) throws Exception {
    return a.equals(b);
  }

  protected boolean STREQ(String b) throws Exception {
    return toofar() ? false : this.argv[this.pos].equals(b);
  }

  protected boolean STREQ(int index, String b) throws Exception {
    return toofar(index) ? false : this.argv[this.pos + index].equals(b);
  }

  /* Return true if S is one of the test command's binary operators. */
  protected boolean binop(String s) throws Exception {
    return ((STREQ(s, "=")) || (STREQ(s, "!=")) || (STREQ(s, "-nt"))
        || (STREQ(s, "-ot")) || (STREQ(s, "-ef")) || (STREQ(s, "-eq"))
        || (STREQ(s, "-ne")) || (STREQ(s, "-lt")) || (STREQ(s, "-le"))
        || (STREQ(s, "-gt")) || (STREQ(s, "-ge")));
  }

  protected boolean good(int index) {
    return ((this.pos + index) < this.argc);
  }

  protected boolean good() {
    return good(0);
  }

  protected boolean toofar() {
    return (this.pos >= this.argc);
  }

  protected boolean toofar(int index) {
    return ((this.pos + index) >= this.argc);
  }

  protected char getc(int i) throws Exception {
    return this.argv[this.pos].charAt(i);
  }

  /*****************************************************************************
   * return true if there's a char at position 'i' in current string. *
   ****************************************************************************/
  protected boolean hasgetc(int i) throws Exception {
    int n;
    n = this.argv[this.pos].length();
    return (i < n);
  }

  protected boolean hasgetc() throws Exception {
    return hasgetc(0);
  }

  /*****************************************************************************
   * return true if there's no char at position 'i' in current string. *
   ****************************************************************************/
  protected boolean nogetc(int i) throws Exception {
    return !(hasgetc(i));
  }

  protected boolean nogetc() throws Exception {
    return !(hasgetc());
  }

  protected String get(int index) throws Exception {
    return this.argv[this.pos + index];
  }

  protected String get() throws Exception {
    return get(0);
  }

  

  protected void where(String func) {
    int i;
    String s;
    s = "";
    i = 0;

    while (i < this.argv.length) {
      if (i == this.pos)
        s += "[";
      s += this.argv[i];
      if (i == this.pos)
        s += "]";
      s += " ";
      i += 1;
    }
    if (this.pos >= this.argv.length)
      s += "[$]";
    String C = this.getClass().getName();
    debug(C + ":" + func + ": `" + s + "'");
  }

  /*
   * term - parse a term and return 1 or 0 depending on whether the term
   * evaluates to true or false, respectively.
   * 
   * term ::= '-'('h'|'d'|'f'|'r'|'s'|'w'|'c'|'b'|'p'|'u'|'g'|'k') filename
   * '-'('L'|'x') filename '-t' int '-'('z'|'n') string string string ('!='|'=')
   * string <int> '-'(eq|ne|le|lt|ge|gt) <int> file '-'(nt|ot|ef) file '('
   * <expr> ')' int ::= '-l' string positive and negative integers
   */
  
  protected boolean term() throws Exception {
    boolean value = false;
    boolean negated = false;

    where("term");

    /* Deal with leading `not's. */
    while (STREQ("!")) {
      advance();
      negated = !negated;
    }

    if (toofar())
      beyond();

    /* A paren-bracketed argument. */
    if (STREQ("(")) {
      int nargs;

      advance();

      for (nargs = 1; STREQ(nargs, ")") == false; nargs++) {
        if (nargs == 4) {
          nargs = this.argc - this.pos;
          break;
        }
      }
      value = posixtest(nargs);
      if (toofar())
        syntax_error("')' expected");

      if (STREQ(")") == false)
        syntax_error("')' expected, found `" + get() + "'");

      advance_unchecked();
    }
    /* Are there enough arguments left that this could be dyadic? */
    else if (4 <= this.argc - this.pos && STREQ("-l") && binop(get(2))) {
      value = binary_operator(true);
    } else if (3 <= this.argc - this.pos && binop(get(1))) {
      value = binary_operator(false);
    }
    /* It might be a switch type argument. */
    else if (getc(0) == '-' && hasgetc(1) && nogetc(2)) {
      if (this.unary.isunary(get()))
        value = eval_unary_op();
      else
        syntax_error("`" + get() + "': unary operator expected");
    } else {
      value = hasgetc();
      advance_unchecked();
    }

    if (negated) {
      value = !value;
    }
    return value;
  }

  protected boolean binary_operator(boolean l_is_l) throws Exception {
    String op;
    /* Is the right integer expression of the form '-l string'? */
    boolean r_is_l;

    if (l_is_l)
      advance_unchecked();

    r_is_l = false;
    op = get(1);

    if ((this.pos + 1 < this.argc - 2) && STREQ(2, "-l")) {
      r_is_l = true;
      advance_unchecked();
    }

    /* for numerical test */
    if (STREQ(op, "-lt") || STREQ(op, "-le") || STREQ(op, "-gt")
        || STREQ(op, "-ge") || STREQ(op, "-eq") || STREQ(op, "-ne")) {
      debug("op = " + op + " l_is_l=" + l_is_l + ", r_is_l=" + r_is_l);

      int L, R;
      char c1, c2;

      if (l_is_l) {
        String left = get(-1);
        L = left.length();
        debug("left=`" + left + "' [" + L + "]");
      } else {
        L = 0;
        try {
          L = Integer.parseInt(get().trim());
        }
        catch (Exception e) {
          syntax_error("integer expression expected `" + get() + "'");
          return false;
        }
      }
      if (r_is_l) {
        String right = get(2);
        R = right.length();
        debug("right=`" + right + "' [" + R + "]");
      } else {
        R = 0;
        try {
          R = Integer.parseInt(get(2).trim());
        }
        catch (Exception e) {
          syntax_error("integer expression expected `" + get(2) + "'");
          return false;
        }
      }

      c1 = op.charAt(1);
      c2 = op.charAt(2);
      this.pos += 3;
      if (c1 == 'l') {
        return c2 == 'e' ? L <= R : L < R;
      }
      if (c1 == 'g') {
        debug(L + " " + op + " " + R);
        return c2 == 'e' ? L >= R : L > R;
      }
      return c2 == 'q' ? L == R : L != R;
    }

    /* non numerical */
    if (STREQ(op, "-nt")) {
      this.pos += 3;
      if (l_is_l | r_is_l)
        syntax_error("-nt does not accept -l");
      return this.binary.test_nt(get(), get(2));
    }

    if (STREQ(op, "-ef")) {
      /* ef - hard link? */
      this.pos += 3;
      if (l_is_l | r_is_l)
        syntax_error("-ef does not accept -l");
      return this.binary.test_ef(get(), get(2));
    }

    if (STREQ(op, "-ot")) {
      /* ot - older than */
      this.pos += 3;
      if (l_is_l | r_is_l)
        syntax_error("-ot does not accept -l");
      return this.binary.test_ot(get(), get(2));
    }

    if (STREQ(op, "=")) {
      boolean value = STREQ(get(2));
      this.pos += 3;
      return value;
    }

    if (STREQ(op, "!=")) {
      boolean value = !STREQ(get(2));
      this.pos += 3;
      return value;
    }

    syntax_error("unknown binary operator `" + op + "'");
    return false;
  }

  protected boolean eval_unary_op() throws Exception {
    switch (getc(1)) {
      case 'a': /* file exists in the file system? */
      case 'e':
        unary_advance();
        return this.unary.test_e(get(-1));

      case 'r': /* file is readable? */
        unary_advance();
        return this.unary.test_r(get(-1));

      case 'w': /* File is writable? */
        unary_advance();
        return this.unary.test_w(get(-1));

      case 'x': /* File is executable? */
        unary_advance();
        return this.unary.test_x(get(-1));

      case 'O': /* File is owned by you? */
        unary_advance();
        return this.unary.test_O(get(-1));

      case 'G': /* File is owned by your group? */
        unary_advance();
        return this.unary.test_G(get(-1));

      case 'f': /* File is a file? */
        unary_advance();
        return this.unary.test_f(get(-1));

      case 'd': /* File is a directory? */
        unary_advance();
        return this.unary.test_d(get(-1));

      case 's': /* File has something in it? */
        unary_advance();
        return this.unary.test_s(get(-1));

      case 'S': /* File is a socket? */
        unary_advance();
        return this.unary.test_S(get(-1));

      case 'c': /* File is character special? */
        unary_advance();
        return this.unary.test_c(get(-1));

      case 'b': /* File is block special? */
        unary_advance();
        return this.unary.test_b(get(-1));

      case 'p': /* File is a named pipe? */
        unary_advance();
        return this.unary.test_p(get(-1));

      case 'L': /* Same as -h */
      case 'h': /* File is a symbolic link? */
        unary_advance();
        return this.unary.test_h(get(-1));

      case 'u': /* File is setuid? */
        unary_advance();
        return this.unary.test_u(get(-1));

      case 'g': /* File is setgid? */
        unary_advance();
        return this.unary.test_g(get(-1));

      case 'k': /* File has sticky bit set? */
        unary_advance();
        return this.unary.test_k(get(-1));

      case 't': /* File (fd) is a terminal? */
        unary_advance();
        return this.unary.test_t(get(-1));

      case 'n': /* True if arg has some length. */
        unary_advance();
        return this.unary.test_n(get(-1));

      case 'z': /* True if arg has no length. */
        unary_advance();
        return this.unary.test_z(get(-1));
        
      case 'P':
        unary_advance();
        return this.unary.test_P(get(-1));

      case 'R':
        unary_advance();
        return this.unary.test_R(get(-1));

      case 'T':
        unary_advance();
        return this.unary.test_T(get(-1));

      case 'M':
        unary_advance();
        return this.unary.test_M(get(-1));

      case 'm':
        unary_advance();
        return this.unary.test_m(get(-1));

      case 'i':
        unary_advance();
        return this.unary.test_i(get(-1));

      case 'C':
        unary_advance();
        return this.unary.test_C(get(-1));
        
      case 'E':
        unary_advance();
        return this.unary.test_E(get(-1));
        
    }
    return false;
  }

  /*
   * and: term term '-a' and
   */
  protected boolean and() throws Exception {
    boolean value = true;

    for (;;) {
      value &= term();
      if (!STREQ("-a"))
        break;
      advance_unchecked();
    }
    return value;
  }

  /*
   * or: and and '-o' or
   */
  protected boolean or() throws Exception {
    boolean value = false;

    for (;;) {
      value |= and();
      if (!STREQ("-o"))
        return value;
      advance_unchecked();
    }
  }

  /*
   * expr: or
   */
  protected boolean expr() throws Exception {
    where("expr");
    if (toofar())
      beyond();
    return or(); /* Same with this. */
  }

 
  protected boolean one_argument() throws Exception {
    where("one_argument");
    boolean b;
    String s;
    s = get();
    b = !STREQ("");
    this.pos += 1;
    debug("one_argument: `" + s + "' => " + b);
    return b;
  }

  protected boolean two_arguments() throws Exception {
    where("two_arguments");
    boolean value = false;

    if (STREQ("!")) {
      advance_unchecked();
      value = !one_argument();
    } else if (getc(0) == '-' && hasgetc(1) && nogetc(2)) {
      if (this.unary.isunary(get()))
        value = eval_unary_op();
      else
        syntax_error("`" + get() + "': unary operator expected");
    } else
      beyond();
    return (value);
  }

  protected boolean three_arguments() throws Exception {
    where("three_arguments");

    if (binop(get(1)))
      return binary_operator(false);

    if (STREQ("!")) {
      advance();
      return !two_arguments();
    }

    if (STREQ("(") && STREQ(2, ")")) {
      boolean value;
      advance_unchecked();
      value = one_argument();
      advance_unchecked();
      return value;
    }

    if (STREQ(1, "-a") || STREQ(1, "-o"))
      return expr();

    syntax_error("`" + get(1) + "': binary operator expected");
    return false;
  }

  /* This is an implementation of a Posix.2 proposal by David Korn. */
  protected boolean posixtest(int nargs) throws Exception {
    boolean value = false;
    debug("posixtest(" + nargs + ")");
    where("posixtest");
    switch (nargs) {
      case 1:
        value = one_argument();
        break;

      case 2:
        value = two_arguments();
        break;

      case 3:
        value = three_arguments();
        break;

      case 4:
        if (STREQ("!")) {
          advance();
          value = !three_arguments();
          break;
        }
        if (STREQ("(") && STREQ(3, ")")) {
          advance_unchecked();
          value = two_arguments();
          advance_unchecked();
          break;
        }
        break;
      default:
        if (nargs <= 0)
          syntax_error("internal error ..");
        value = expr();
    }

    return (value);
  }

 


  public static int nextc(String s, int offset, char quote) throws Exception {
    char c;
    int i;
    for (i = offset; i < s.length(); ++i) {
      c = s.charAt(i);
      if (c == '\\') {
        i += 1;
        continue;
      }
      if (c == quote)
        break;
    }
    return i >= s.length() ? -1 : i;
  }

  public static int skipws(String s, int offset) {
    int i = offset;
    try {
      while (ISBLANK(s.charAt(i)))
        i += 1;
    }
    catch (Exception e) {
      /* ignore */
    }
    return i;
  }

  public static String a2buf(String buf, char c) {
    String s = buf;
    if (s == null)
      s = "";
    return s + c;
  }

  public static String a2buf(String buf, String str) {
    String s = buf;
    if (s == null)
      s = "";
    return s + str;
  }

  public static String unescape(String s, int I, int j) throws Exception {
    String buf = "";
    int i = I;
    char c;
    while (i < j) {
      c = s.charAt(i);
      if (c == '\\') {
        i += 1;
        if (i < j)
          c = s.charAt(i);
        else
          throw new Exception("unclosed escape sequence");
      }
      buf += c;
      i += 1;
    }
    return buf;
  }

  public boolean eval(String[] argv) throws Exception {
    boolean r = false;

    if (argv.length > 0) {
      /* init */
      this.pos = 0;
      this.argv = argv;
      this.argc = argv.length;

      /* evaluate args */
      r = posixtest(argv.length);

      /* it's a mistake to have arguments left */
      if (this.pos != argv.length)
        syntax_error("too many arguments");
    }
    return r;
  }

  public boolean eval(String s, char quote) throws Exception {
    return eval(Static.split0x1(s, quote));
  }


}
