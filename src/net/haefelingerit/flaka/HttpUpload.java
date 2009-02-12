package net.haefelingerit.flaka;

import java.io.File;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.params.HttpMethodParams;

public class HttpUpload
{
  // protected static final Pattern REGEX_P;
  // protected static final Pattern FSIZE_P;
  protected static final Pattern SUBMI_P;

  // protected static String DEPOTBASE;
  protected static String        ENDPOINT;
  protected static String        TIMEOUT;
  protected static String        TESTONLY;
  protected static String        USER;
  protected static String        PASSWD;

  protected Properties           param = new Properties();
  protected boolean              debug = false;

  public HttpUpload() {
    reset();
  }

  static
  {
    /* default timeout */
    TIMEOUT = "5000"; /* 5 seconds */

    /* default endpoint */
    ENDPOINT = "http://haefelingerit.net/upload";

    /* default test mode */
    TESTONLY = "false";

    /* default test mode */
    USER = "alibaba";

    /* default test mode */
    PASSWD = "sesame";

 
    SUBMI_P =
      Pattern
          .compile("(?:Accepted into depot|Submission would have been stored) as"
              + "\\s+([^\\s<]+)");
  }

  public void reset() {
    this.param.clear();
    set("testonly", TESTONLY);
    set("endpoint", ENDPOINT);
    set("timeout", TIMEOUT);
    set("testonly", TESTONLY);
    set("user", USER);
    set("passwd", PASSWD);
  }

  /**
   * Get value of attribute
   * 
   * @name.
   * 
   * @param name
   *          not null
   * @param otherwise
   *          not null
   * @return value of attribute
   * @name or
   * @otherwise if not available.
   */
  public String get(String name, String otherwise) {
    return this.param.getProperty(name, otherwise);
  }

  public String set(String name, String value) {
    String before;

    before = this.param.getProperty(name);
    if (value != null)
    {
      this.param.put(name, value);
    }
    else
    {
      /* remove key */
      if (before != null)
        this.param.remove(name);
    }
    return before;
  }

  protected static void syslog(String s) {
    System.err.println(s);
    System.err.flush();
  }

  public void setDebug(boolean b) {
    this.debug = b;
    HttpUpload.debug(b);
  }

  static protected void debug(boolean b) {
    /* set debug properties */
    System.setProperty("org.apache.commons.logging.Log",
        "org.apache.commons.logging.impl.SimpleLog");

    System.setProperty("org.apache.commons.logging.simplelog.showdatetime",
        "true");

    System.setProperty(
        "org.apache.commons.logging.simplelog.log.httpclient.wire.header",
        b ? "debug" : "info");

    System
        .setProperty(
            "org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient",
            b ? "debug" : "info");
  }

  static private String getp(String p) {
    String s = System.getProperties().getProperty(p);
    if (s == null && !p.equals("httpupload.debug"))
    {
      System.err.println("error: required property `" + p + "' not set.");
      System.exit(1);
    }
    return s != null ? s.trim() : null;
  }

  static private String getp(String p, String otherwise) {
    String s = System.getProperties().getProperty(p);
    return (s == null) ? otherwise : s.trim();
  }

  private String passwd() {
    return get("passwd", HttpUpload.PASSWD);
  }

  private String user() {
    return get("user", HttpUpload.USER);
  }

  static private FilePart filepart(File file) {
    FilePart p = null;
    try
    {
      p = new FilePart("jarfile", file);
    }
    catch (Exception e)
    {
      System.err.println("error reading file `" + file.getName() + "'.");
      System.exit(1);
    }
    return p;
  }

  private void setError(String msg) {
    set("errmsg", msg);
  }

  public String getError() {
    return get("errmsg", null);
  }

  protected static void xmlattr(StringBuffer buf, String key, String val) {
    if (buf != null && key != null && val != null
        && val.matches("\\s*") == false)
    {
      buf.append(" ");
      buf.append(key);
      buf.append("=\"");
      buf.append(val); // tbd: escaple non XML chars here ..
      buf.append("\"");
    }
  }

  protected static void xmldata(StringBuffer buf, String elm, String val) {
    if (buf != null && elm != null && val != null
        && val.matches("\\s*") == false)
    {
      buf.append("<");
      buf.append(elm);
      buf.append("><![CDATA[");
      buf.append(val);
      buf.append("]]></");
      buf.append(elm);
      buf.append(">");
    }
  }

  protected static String getResponseFrom(HttpMethod meth) {
    String r = null;
    try
    {
      r = meth.getResponseBodyAsString();
    }
    catch (Exception e)
    {
      syslog("error reading HTTP response .." + e.getMessage());
    }
    if (r == null)
    {
      syslog("* empty response seen ..");
      r = "";
    }
    return r;
  }

  /**
   * Evaluate whether uploading went well or failed.
   * 
   * A upload on Jdepot may fail cause a connection could not be established or
   * cause there was an application specific problem (like artifact exits
   * already or Manifest does not contain required or wrong attributes).
   * 
   * @param meth
   *          not null
   * @return true if all went well
   */
  protected boolean eval(HttpMethod meth) {
    Matcher regex;
    String response = null;
    StringBuffer buf = new StringBuffer("");
    String errmsg = null;
    String errtyp = null; // no error

    buf.append("<upload");
    xmlattr(buf, "testonly", get("testonly", TESTONLY));
    xmlattr(buf, "endpoint", get("endpoint", ENDPOINT));
    xmlattr(buf, "timeout", get("timeout", TIMEOUT));
    xmlattr(buf, "user", get("user", USER));
    xmlattr(buf, "file", get("filepath", null));
    xmlattr(buf, "size", get("filesize", null));

    /* If there was already an error, handle it now */
    if (getError() != null)
    {
      syslog(getError());
      /* must be a transport error at this point */
      xmlattr(buf, "error", "transport-error");
      buf.append(">");
      xmldata(buf, "error", errmsg);
      buf.append("</upload>");
      set("xmlbuf", buf.toString());
      return false;
    }

    /* Handle any HTTP error */
    if (meth.getStatusCode() / 100 != 2)
    {
      int stat;
      String line;

      stat = meth.getStatusCode();
      line = meth.getStatusLine().toString();

      errmsg = line + " [" + HttpStatusText.explain(stat) + "]";
      setError(errmsg);
      xmlattr(buf, "error", "http-error");
      buf.append("</upload>");
      xmldata(buf, "error", errmsg);
      set("xmlbuf", buf.toString());
      return false;
    }

    /* Fetch response text from server and run a check */
    response = getResponseFrom(meth);
    /* save response for later usage */
    this.set("resbuf", response);

    /* We assume that everything went well, if respone text contains an
     * acceptance message.
     */
    regex = SUBMI_P.matcher(response);

    /* do we have a confirmation message? */
    if (regex.find() == false)
    {
      // scan for errors? No - this should be handled on another layer.
      errtyp = "storage-error";
      errmsg = "unknown storage error";
      this.setError(errmsg);
      xmlattr(buf, "error", errtyp);
      buf.append(">");
      xmldata(buf, "error", errmsg);
      /* wrap entire response in any case */
      xmldata(buf, "cdata", response);
      buf.append("</upload>");
      set("xmlbuf", buf.toString());
      return false;
    }

    /* Finally it's looking good ... */
    String g1 = regex.group(1);
    xmlattr(buf, "href", g1);
    buf.append(">");
    /* wrap entire response in any case */
    xmldata(buf, "cdata", response);
    /* finish */
    buf.append("</upload>");
    set("xmlbuf", buf.toString());
    return true;
  }

  protected void setcred(HttpClient client) {
    AuthScope scope;
    UsernamePasswordCredentials creds;
    scope = new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT);
    creds = new UsernamePasswordCredentials(user(), passwd());
    client.getState().setCredentials(scope, creds);
  }

  protected void settimeout(HttpClient client) {
    String s = get("timeout", HttpUpload.TIMEOUT);
    try
    {
      int timeout;
      timeout = Integer.parseInt(s);
      /* set default timeout */
      client.getHttpConnectionManager().getParams().setConnectionTimeout(
          timeout);
    }
    catch (Exception e)
    {
      syslog("* unable to set standard connection timeout to `" + s + "'.");
    }
  }

  protected boolean exec(HttpMethod meth) {
    HttpClient client;
    boolean rc = false;

    /* exec http method */
    try
    {
      client = new HttpClient();
      settimeout(client);
      setcred(client);
      client.executeMethod(meth);
      rc = (meth.getStatusCode() / 100) == 2;
      if (rc == false)
      {
        setError(meth.getStatusLine().toString());
      }
    }
    catch (java.net.UnknownHostException ex)
    {
      set("errmsg", "unable to resolve host `" + ex.getMessage() + "'.");
    }
    catch (Exception ex)
    {
      setError(ex.getClass().getName() + " " + ex.getMessage());
      if (this.debug)
      {
        System.err.println("*** excepting seen while uploading ..");
        ex.printStackTrace(System.err);
        System.err.println("<<*>>");
      }
    }
    return rc;
  }

  public boolean upload() {
    File file = null;
    String endpoint = get("endpoint", HttpUpload.ENDPOINT);
    String testonly = get("testonly", HttpUpload.TESTONLY);
    String timeout = get("timeout", HttpUpload.TIMEOUT);
    String filepath = get("filepath", null);
    String logpath = get("logpath", null);
    String standard = get("standard", "1.0");

    syslog("endpoint: " + endpoint);
    syslog("testonly: " + testonly);
    syslog("timeouts: " + timeout);
    syslog("filepath: " + filepath);
    syslog("logpath : " + logpath);
    syslog("standard: " + standard);

    PostMethod filePost = null;
    boolean rc;

    try
    {
      /* new game */
      rc = false;
      setError(null);
      set("logmsg", "");

      if (testonly == null || testonly.matches("\\s*false\\s*"))
      {
        testonly = "x-do-not-test";
      }
      else
      {
        testonly = "test";
      }

      if (filepath == null || filepath.matches("\\s*"))
      {
        set("logmsg", "empty property `filepath', nothing to do.");
        return true;
      }

      /* file to upload */
      file = new File(filepath);
      if (file.exists() == false)
      {
        setError("file `" + file.getPath() + "' does not exist.");
        return false;
      }
      if (file.isFile() == false)
      {
        setError("file `" + file.getPath() + "' exists but not a file.");
        return false;
      }
      if (file.canRead() == false)
      {
        setError("file `" + file.getPath() + "' can't be read.");
        return false;
      }

      set("filesize",""+file.length());
      
      /* create HTTP method */
      filePost = new PostMethod(endpoint);

      Part[] parts =
        {
            new StringPart(testonly, "(opaque)"), filepart(file)
        };

      filePost.getParams().setBooleanParameter(
          HttpMethodParams.USE_EXPECT_CONTINUE, false);

      filePost.setRequestEntity(new MultipartRequestEntity(parts, filePost
          .getParams()));

      /* execute method */
      rc = exec(filePost) && eval(filePost);
    }
    finally
    {
      /* release resources in just any case */
      if (filePost != null)
        filePost.releaseConnection();
    }
    return rc;
  }

  public static void main(String[] args) {
    HttpUpload httpclient = null;
    if (args.length <= 0)
    {
      System.err.println("usage: <prog> file [file ..]");
      System.exit(1);
    }

    debug(true);

    httpclient = new HttpUpload();
    httpclient.set("endpoint", getp("httpupload.endpoint"));
    httpclient.set("category", getp("httpupload.category"));
    httpclient.set("testonly", getp("httpupload.testonly", "false"));
    httpclient.set("timeout", getp("httpupload.timeout"));
    httpclient.set("logpath", getp("httpupload.logpath"));
    httpclient.set("user", getp("httpupload.user"));
    httpclient.set("passwd", getp("httpupload.passwd"));

    if (getp("httpupload.debug") != null)
      debug(true);

    for (int i = 0; i < args.length; ++i)
    {
      httpclient.set("filepath", args[i]);
      syslog("uploading file `" + args[i] + "'..");
      if (!httpclient.upload())
      {
        syslog("upload failed `" + httpclient.getError() + "'");
      }
      else
      {
        System.out.println(httpclient.get("xmlbuf", ""));
      }
    }

    return;
  }

}
