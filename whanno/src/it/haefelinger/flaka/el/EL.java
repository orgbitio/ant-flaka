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

package it.haefelinger.flaka.el;

import it.haefelinger.flaka.util.Static;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.el.ELException;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;

import org.apache.tools.ant.Project;

import de.odysseus.el.ExpressionFactoryImpl;
import de.odysseus.el.tree.Tree;
import de.odysseus.el.tree.TreeBuilderException;
import de.odysseus.el.tree.TreeStore;
import de.odysseus.el.tree.impl.Builder;
import de.odysseus.el.tree.impl.Cache;
import de.odysseus.el.tree.impl.Parser;
import de.odysseus.el.tree.impl.Scanner;
import de.odysseus.el.tree.impl.Scanner.ScanException;
import de.odysseus.el.tree.impl.Scanner.Symbol;
import de.odysseus.el.tree.impl.ast.AstComposite;
import de.odysseus.el.tree.impl.ast.AstEval;
import de.odysseus.el.tree.impl.ast.AstNode;
import de.odysseus.el.tree.impl.ast.AstNull;
import de.odysseus.el.tree.impl.ast.AstText;
import java.lang.reflect.*;

/**
 * This class is the entry point for EL evaluation.
 * 
 * @author geronimo
 * 
 */
public final class EL {
  Context context = null;
  ExpressionFactory factory = null;
  boolean debug = false;
  Project project = null; // TODO: remove me

  @SuppressWarnings("unused")
  private EL() {/* unused */
  }

  public EL(Project project) {
    init(project);
  }

  static public class MyParser extends Parser {
    protected List<AstNode> list = new ArrayList<AstNode>();
    protected List<Exception> errors = new ArrayList<Exception>();

    public MyParser(Builder builder, String expression) {
      super(builder, expression);
    }

    protected Scanner createScanner(String expression) {
      return new MyScanner(expression);
    }

    protected boolean sym(Symbol s) {
      return getToken().getSymbol() == s;
    }

    /*
     * A customized version of eval to allow references to empty EL expressions
     * like #{}.
     * 
     * Such empty references evaluate the same as #{null}.
     * 
     * Parameter <code>required</code> indicates whether we require '#{' or ${.
     * Is there a situatio where '}' is not required?
     * 
     * Parameter <code>deferred</code> indicates which reference type to expect;
     * either #{..} or ${..}.
     * 
     * (non-Javadoc)
     * 
     * @see de.odysseus.el.tree.impl.Parser#eval(boolean, boolean)
     */
    protected void exprref() {
      int start;

      // remember where we are
      start = this.scanner.getPosition();

      try {
        if (sym(Symbol.START_EVAL_DYNAMIC)) {
          /*
           * Eat up everything till and inclusive '}'. We have to do this via
           * consumeToken() to advance the scanner's position.
           */
          trytoken();

          if (sym(Symbol.EOF) || sym(Symbol.END_EVAL)) {
            int len, end, delta, size;
            String text;

            // We have do do some math here to calculate the
            // position of '}'.
            len = this.scanner.getInput().length();
            // This is how much the scanner may have advanced
            // meanwhile ..
            delta = this.scanner.getPosition() - start;
            // This token's size (don't use length of image)
            size = getToken().getSize();
            // Make sure to avoid overflows .. (should not happen)
            end = Math.min(start + delta + size, len);
            text = this.scanner.getInput().substring(start, end);
            // Turn ${..} into a regular text field.
            AstText node = new AstText(text);
            this.list.add(node);
            return;
          }

          /* serious internal error here, someone broke the contract */
          syntaxerror("expected (<}>|<EOF>)", start);
          recover();
          return;
        }

        if (sym(Symbol.START_EVAL_DEFERRED)) {
          AstNode node;
          trytoken();

          /*
           * If the next token is "}", then we have found an empty #{} reference
           * and we simply ignore this empty reference.
           */
          if (sym(Symbol.END_EVAL)) {
            return;
          }

          /*
           * Otherwise we are at the start of an expression and we are going to
           * consume it now.
           */
          try {
            node = new AstEval(expr(true), true);
          } catch (Exception e) {
            handleEx(e);
            recover();
            return;
          }

          if (!sym(Symbol.END_EVAL)) {
            /* we should have seen this by now .. */
            syntaxerror("expected " + q("}"), start);
            recover();
            return;
          }

          this.list.add(node);
          return;
        }

        /* serious error here, cause we expect here "#{..}" or "${..}" */
        syntaxerror("expected " + q("${}") + " or " + q("#{}"), start);
        recover();
      } finally {
        trytoken();
      }
    }

    /**
     * Recover from a syntax error while inside a EL reference.
     * 
     * To recover from a syntatical error switch scanner into ignoring mode. In
     * this mode, the scanner will simply move forward until character '}' is
     * seen.
     */
    protected void recover() {
      MyScanner scanner;

      scanner = (MyScanner) this.scanner;
      scanner.recover = true;
      while (!(sym(Symbol.END_EVAL) || sym(Symbol.EOF)))
        trytoken();
    }

    protected String q(String s) {
      return "`" + (s == null ? "" : s) + "'";
    }

    protected void syntaxerror(String expected, int start) {
      String input, substring, near;
      int end;
      final int history = 5;
      final int window = 20;

      input = this.scanner.getInput();
      end = Math.min(start + window, input.length());
      substring = input.substring(start, end);
      if (start - history >= 0)
        substring = ".." + input.substring(start - history, start) + substring;
      // Make that message fit's into a single line
      substring = substring.replaceAll("(?m:)\\n", "\\\\n");
      near = input.substring(start, Math.min(start + 3, input.length()));

      TreeBuilderException tbe;
      tbe = new TreeBuilderException(substring, 0, "", "", expected + " near "
          + q(near));
      this.errors.add(tbe);
    }

    protected Tree maketree(AstNode node, boolean deferred) {
      return new Tree(node, getFunctions(), getIdentifiers(), deferred);
    }

    /**
     * Try to read a TEXT node.
     * 
     * This method is similar to super.text() except that any exceptions thrown
     * will end up in the list of errors.
     * 
     * If current token is TEXT, then appropirate node is created and added to
     * the internal node list. In addition, the next token will be consumed from
     * the stream.
     */
    protected void trytext() {
      try {
        AstNode node = null;
        if ((node = text()) != null)
          this.list.add(node);
      } catch (Exception e) {
        handleEx(e);
      }
    }

    /**
     * A method to consume a plain token.
     * 
     * This method is similar to <code>consumeToken</code>. The main difference
     * is that all exceptions thrown are treated as errors (end up in internal
     * error list). This is not the case for ScanExceptions, cause the scanner
     * is not supposed to throw any exceptions at all. Instead the scanner is
     * supposed to create a EOF token in case of problems.
     */
    protected void trytoken() {
      try {
        consumeToken();
      } catch (ScanException se) {
        // this is a serious error!!
        System.err.println("serious internal error");
        System.exit(1);
      } catch (ParseException pe) {
        handleEx(pe);
      } catch (Exception e) {
        handleEx(e);
      }
    }

    protected void handleEx(Exception e) {
      this.errors.add(e);
    }

    public Tree maketree() {

      Tree tree;
      int sz;

      this.list.clear();
      this.errors.clear();

      /* kickstart */
      trytoken();

      /* read text (if any) */
      trytext();

      /* (expr text?) */
      while (!sym(Symbol.EOF)) {
        exprref();
        trytext();
      }

      sz = this.list.size();
      switch (sz) {
      case 0: {
        AstNode t;
        t = new AstNull();
        tree = maketree(t, false);
        break;
      }
      case 1: {
        AstNode e;
        e = this.list.get(0);
        tree = maketree(e, true);
        break;
      }
      default: {
        AstComposite composite = createAstComposite(this.list);
        tree = maketree(composite, true);
      }
      }
      return tree;
    }

  }

  static public class MyScanner extends Scanner {
    public boolean recover = false;

    public MyScanner(String expression) {
      super(expression);
    }

    public String substring(int pos) {
      String input = this.getInput();
      return input.substring(pos);
    }

    protected int forward() {
      int p, l;

      p = this.getPosition();
      l = this.getInput().length();

      // TODO:
      /*
       * what about a '}' in a string? We need to take care whether in a string
       * or not. We are in a string if we have seen either ' or " once and not
       * \' or \".
       */
      while (p < l && ('}' != this.getInput().charAt(p))) {
        p += 1;
        /* counting, that's all I want */
      }
      if (p < l) {
        // eat '}'
        p += 1;
      }
      return p;
    }

    /**
     * string token
     */
    protected Token nextString() throws ScanException {
      int i, l, pos;
      char quote, c;
      Token token;

      pos = this.getPosition();
      this.builder.setLength(0);
      quote = this.getInput().charAt(pos);
      i = pos + 1;
      l = this.getInput().length();
      token = null;

      while (i < l) {
        c = this.getInput().charAt(i++);
        if (c == '\\') {
          if (i == l)
            throw new ScanException(pos, "unterminated string", quote
                + " or \\");
          c = this.getInput().charAt(i++);
          if (c == '\\' || c == quote)
            this.builder.append(c);
          else {
            this.builder.append('\\');
            this.builder.append(c);
          }
          continue;
        }
        if (c == quote) {
          token = token(Symbol.STRING, this.builder.toString(), i - pos);
          break;
        }
        /* default */
        this.builder.append(c);
      }

      if (token == null)
        throw new ScanException(pos, "unterminated string",
            String.valueOf(quote));
      return token;
    }

    protected Token nextText() throws ScanException {
      this.builder.setLength(0);
      int i = this.getPosition();
      int l = this.getInput().length();
      boolean escaped = false;
      while (i < l) {
        char c = this.getInput().charAt(i);
        switch (c) {
        case '\\':
          // If we are in state 'escaped' then take the previous
          // character,
          // which must be '\\' literally while staying in state
          // 'escaped'.
          // Otherwise:
          // Just consume character '\' but do not take a decision
          // what to
          // do by now. It depends on the next character: being `#` or
          // `$`,
          // then there is good chance that it starts an eval-expr.
          // See
          // handling of those two chars below.
          if (escaped) {
            this.builder.append('\\');
          } else {
            escaped = true;
          }
          break;
        case '#':
          // case '$':
          // Let's first handle the regular case where we at the start
          // of '#{'
          // or '${'. If the current character is escaped, then add
          // '#{' and
          // '${' as literal-expr. Since the escape character is
          // meaningfull
          // for us here, we *consume* it, i.e. it will *not* be part
          // of the
          // literal-expr. If we are not escaped, then our current
          // literal-expr ends before the current character.
          if (i + 1 < l && this.getInput().charAt(i + 1) == '{') {
            if (escaped) {
              this.builder.append(c);
              escaped = false;
              break;
            }
            // we reached the end of the current literal-expr. Bail
            // out ..
            return makeText(i - this.getPosition());
          }
          // Handle the case where we have '#c' or '$c' and c is any
          // other
          // character than '{' or c is the EOF (or more appropriate,
          // EOI, the
          // end-of-input). Now, if we have '\#c' then we leave this
          // as it is
          // cause the escape char is not meaningful for us.
          if (escaped) {
            this.builder.append('\\');
            escaped = false;
          }
          this.builder.append(c);
          break;
        default:
          // Handle '\c', where c stands for any character except '\'.
          // Then,
          // append oth characters and mark the next character on the
          // stream
          // as not escaped.
          if (escaped) {
            this.builder.append('\\');
            escaped = false;
          }
          // Append character in any case.
          this.builder.append(c);
        }
        i++;
      }
      // Handle situation '\<EOF>`. Here we have seen '\' so far and
      // marked it
      // as escaped. Then we consumed EOF which terminates the loop. In
      // that
      // case we need to make sure that our character will made it.
      if (escaped) {
        this.builder.append('\\');
      }
      return makeText(i - this.getPosition());
    }

    protected Token makeText(int length) {
      String buf = this.builder.toString();
      return token(Symbol.TEXT, buf, length);
    }

    protected Token nextToken() throws ScanException {
      String text;
      Token token;
      token = getToken();

      try {
        if (this.recover
            || (token != null && token.getSymbol() == Symbol.START_EVAL_DYNAMIC)) {
          int p, l;
          Symbol s;

          // move forward
          p = forward();
          l = this.getInput().length();
          s = p < l ? Symbol.END_EVAL : Symbol.EOF;
          text = this.getInput().substring(this.getPosition(), Math.min(p, l));
          token = this.token(s, text, text.length());
          this.recover = false;
        } else {
          token = super.nextToken();
        }
      } catch (ScanException se) {
        // Scan exceptions abort scanning. Rather than throwing an
        // exception, we
        // generate the EOF token. This allows the caller to consume
        // tokens
        // until EOF is seen.
        token = this.token(Symbol.EOF, "", 0);
        // TODO: print scan exception on log screen?
        //
      }
      return token;
    }
  }

  static public class MyBuilder extends Builder {
    private static final long serialVersionUID = 6510509119020544874L;

    public MyBuilder() {
      super(Builder.Feature.VARARGS);
    }

    /**
     * Parse expression.
     */
    public Tree build(String expression) {
      Tree tree = null;
      MyParser parser;

      parser = new MyParser(this, expression);
      tree = parser.maketree();
      if (!parser.errors.isEmpty()) {
        for (@SuppressWarnings("unused")
        Exception ex : parser.errors) {
          // TODO: print on debug stream ..
          // System.err.println(ex.getMessage());
        }
      }
      return tree;
    }

  }

  ExpressionFactory makefactory() {
    final String tc_key = "de.odysseus.el.misc.TypeConverter";
    final String tc_val = "it.haefelinger.flaka.el.TypeConverter";
    ExpressionFactory ef = null;
    Properties P = new Properties();
    P.setProperty(tc_key, tc_val);
    P.setProperty("javax.el.varArgs", "true");
    Builder builder = new MyBuilder();
    TreeStore store = new TreeStore(builder, new Cache(10));
    TypeConverter tc = new it.haefelinger.flaka.el.TypeConverter();
    ef = new ExpressionFactoryImpl(store, tc);
    return ef;
  }

  Context makecontext() {
    Resolver res;
    res = new Resolver(this.project);
    res.debug = this.debug;
    return new Context(res);
  }

  protected void init(Project project) {
    this.project = project; // TODO: remove me
    this.factory = makefactory();
    this.context = makecontext();
    // TODO: add project to context

    // variables e, pi
    vardef("e", new Double(Math.E), double.class);
    vardef("pi", new Double(Math.PI), double.class);

    // functions sin, cos, tan, exp, log, abs, sqrt, min, max, pow
    funcdef("sin", Math.class, "sin", double.class);
    funcdef("cos", Math.class, "cos", double.class);
    funcdef("tan", Math.class, "tan", double.class);
    funcdef("exp", Math.class, "exp", double.class);
    funcdef("log", Math.class, "log", double.class);
    funcdef("abs", Math.class, "abs", double.class);
    funcdef("sqrt", Math.class, "sqrt", double.class);
    funcdef2("min", Math.class, "min", double.class, double.class);
    funcdef2("max", Math.class, "max", double.class, double.class);
    funcdef2("pow", Math.class, "pow", double.class, double.class);
    funcdef0("rand", Math.class, "random");

    funcdef("typeof", Functions.class, "typeof", Object.class);
    funcdef("nativetype", Functions.class, "nativetype", Object.class);
    funcdefv("file", Functions.class, "file", Object[].class);
    funcdef("size", Functions.class, "size", Object.class);
    funcdef("sizeof", Functions.class, "size", Object.class);
    funcdef("nullp", Functions.class, "isnil", Object.class);

    // string functions
    funcdefv("concat", Functions.class, "concat", Object[].class);

    funcdefv("list", Functions.class, "list", Object[].class);
    funcdefv("append", Functions.class, "append", Object[].class);

    funcdefv("split", Functions.class, "split", Object[].class);
    funcdef("split_ws", Functions.class, "split_ws", Object.class);
    funcdefv("replace", Functions.class, "replace", Object[].class);
    funcdef("trim", Functions.class, "trim", Object.class);
    funcdef("ltrim", Functions.class, "ltrim", Object.class);
    funcdef("rtrim", Functions.class, "rtrim", Object.class);
    funcdef1v("format", Functions.class, "format", String.class);
    funcdef1v("join", Functions.class, "join", String.class);
    funcdef2("matches", Functions.class, "matches", Object.class, Object.class);
    funcdef2("glob", Functions.class, "glob", Object.class, Object.class);
    
    // funcdef2("cons", Functions.class, "cons", Object.class,List.class);
    // funcdef("car", Functions.class, "car", List.class);
    // funcdef("cdr", Functions.class, "cdr", List.class);

    funcdef("q", Functions.class, "quote", String.class);
  }

  void vardef(String name, Object obj, Class clazz) {
    ValueExpression ve;
    ve = this.factory.createValueExpression(obj, clazz);
    this.context.setVariable(name, ve);
  }

  void funcdef0(String name, Class clazz, String func) {
    try {
      Method method;
      method = clazz.getMethod(func);
      this.context.setFunction("", name, method);
    } catch (NoSuchMethodException nsm) {
      Static.error(this.project, "no such method:" + nsm);
    }
  }

  void funcdef(String name, Class clazz, String func, Class arg) {
    try {
      Method method;
      method = clazz.getMethod(func, arg);
      this.context.setFunction("", name, method);
    } catch (NoSuchMethodException nsm) {
      Static.error(this.project, "no such method:" + nsm);
    }
  }

  void funcdef2(String name, Class clazz, String func, Class arg1, Class arg2) {
    try {
      Method method;
      method = clazz.getMethod(func, arg1, arg2);
      this.context.setFunction("", name, method);
    } catch (NoSuchMethodException nsm) {
      Static.error(this.project, "no such method:" + nsm);
    }
  }

  void funcdefv(String name, Class clazz, String func, Class arg) {
    try {
      Method method;
      method = clazz.getMethod(func, new Class[] { arg });
      this.context.setFunction("", name, method);
    } catch (NoSuchMethodException nsm) {
      Static.error(this.project, "no such method:" + nsm);
    }
  }

  void funcdef1v(String name, Class clazz, String func, Class arg) {
    try {
      Method method;
      method = clazz.getMethod(func, new Class[] { arg, Object[].class });
      this.context.setFunction("", name, method);
    } catch (NoSuchMethodException nsm) {
      Static.error(this.project, "no such method:" + nsm);
    }
  }

  /**
   * Evaluate <code>expr</code> according to EL rules.
   * 
   * The rules are: If expr is <code>#{..}</code> evaluate expr into an instance
   * of <code>clazz</code>. Otherwise, if there are leading or trailing
   * characters, evaluate expr into a string. This is done by evaluating all
   * embedded #{..} expressions into objects of <code>clazz</code> in a first
   * step and finally stringizing and concatening all objects into the final
   * string.
   */
  Object eval(String expr, Class clazz) {
    Object obj = null;
    ValueExpression ve = null;
    // TODO: necessary?
    if (expr == null || expr.matches("\\s*"))
      return null;
    try {
      ve = this.factory.createValueExpression(this.context, expr, clazz);
      obj = ve.getValue(this.context);
    } catch (TreeBuilderException tbe) {
      // TODO: error handling
      // System.err.println(tbe.getMessage());
    } catch (ELException ele) {
      // System.err.println(ele.getMessage());
    }
    return obj;
  }

  public Object toobj(String expr) {
    return eval(expr, Object.class);
  }

  public File tofile(String expr) {
    Object obj;
    obj = eval(expr, File.class);
    return (File) obj;
  }

  public String tostr(String expr) {
    Object obj;
    obj = eval(expr, String.class);
    if (obj == null)
      return "";
    if (!(obj instanceof String))
      return obj.toString();
    if (obj.getClass().isArray()) {
      String[] arr = (String[]) obj;
      return Arrays.toString(arr);
    }
    return (String) obj;
  }

  public boolean tobool(String expr) {
    Object obj;
    obj = eval(expr, Boolean.class);
    return obj instanceof Boolean ? ((Boolean) obj).booleanValue() : false;
  }

  
  public EL sourceFunctions(String clazz) throws SecurityException, ClassNotFoundException {
    
    int passed = 0, failed = 0;
    String ns = "";
    
    if (clazz == null)
      return this;
    clazz = clazz.trim();
    // bail out if only no classname is given.
    if (clazz.length()==0 || clazz.matches("[^:]*:$"))
      return this;
    
    int p = clazz.indexOf(':');
    switch(p) {
      case -1: break;
      case 0 : clazz = clazz.substring(1); break;
      default: {
        ns = clazz.substring(0,p);
        clazz = clazz.substring(p+1);
      }
    }
    
    for (Method m : Class.forName(clazz).getMethods()) {
       if (m.isAnnotationPresent(ELFunction.class)) {
          try {
            // function with one argument of type Object.
            String name = m.getName();
            this.context.setFunction(ns, name, m);
            passed++;
          } catch (Throwable ex) {
             System.out.printf("something failed while loading %s: %s \n", m, ex.getCause());
             failed++;
          }
       }
    }
    System.out.printf("Loaded %d/%d functions", passed, (passed+failed));
    return this;
  }
  
  public static void main(String[] args) {
    // try to invoke Functions.list() via reflection.
    try {
      Method method;
      method = Functions.class
          .getMethod("list", new Class[] { Object[].class });
      Object obj;
      Object[] argv = { new Object[] { "foo", "bar" } };
      obj = method.invoke(null, argv);
      List L = (List) obj;
      Iterator I = L.iterator();
      while (I.hasNext()) {
        // System.err.println("=>" + I.next());
      }
    } catch (Exception e) {
      // System.err.println("error: " + e);
    }
  }

}