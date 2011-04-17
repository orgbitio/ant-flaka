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

package it.haefelinger.flaka.util;

import it.haefelinger.flaka.Task;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.SocketFactory;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.tools.ant.BuildException;

/**
 * Utility class for configuring and testing JSSE for various JDKs
 * 
 * @author merzedes
 * @since 1.0
 */

public class InitSSL extends Task {
  final String TRUSTSTORE = "javax.net.ssl.trustStore";

  protected File truststore = null;
  protected boolean verifycert = true;
  protected boolean quiet = false;

  public void setTrustStore(File file) {
    if (file != null)
      this.truststore = file;
  }

  public void setVerifyCertificate(boolean b) {
    this.verifycert = b;
  }

  public void setQuiet(boolean b) {
    this.quiet = b;
  }

  static public class AntiX509TrustManager implements X509TrustManager {
    public X509Certificate[] getAcceptedIssuers() {
      return null;
    }

    public void checkServerTrusted(X509Certificate[] certs, String authType)
        throws CertificateException {
      return;
    }

    public void checkClientTrusted(X509Certificate[] certs, String authType)
        throws CertificateException {
      return;
    }
  }

  static public class SSLSocketFactory implements ProtocolSocketFactory {
    protected SSLContext sslcontext = null;

    public SSLSocketFactory(SSLContext ctx) {
      super();
      this.sslcontext = ctx;
    }

    protected SSLContext getSSLContext() {
      if (this.sslcontext == null)
        try {
          this.sslcontext = SSLContext.getInstance("SSL");
        } catch (Exception e) {
          System.err.println("*** unable to get security instance 'SSL' ..");
        }
      return this.sslcontext;
    }

    public Socket createSocket(String h, int p, InetAddress ch, int cp)
        throws IOException, UnknownHostException {
      return getSSLContext().getSocketFactory().createSocket(h, p, ch, cp);
    }

    public Socket createSocket(final String h, final int p,
        final InetAddress la, final int lp, final HttpConnectionParams params)
        throws IOException, UnknownHostException, ConnectTimeoutException {
      Socket S = null;
      if (params == null) {
        throw new IllegalArgumentException("Parameters may not be null");
      }
      int timeout = params.getConnectionTimeout();
      SocketFactory socketfactory = getSSLContext().getSocketFactory();
      if (timeout == 0)
        S = socketfactory.createSocket(h, p, la, lp);
      else {
        S = socketfactory.createSocket();
        SocketAddress localaddr = new InetSocketAddress(la, lp);
        SocketAddress remoteaddr = new InetSocketAddress(h, p);
        S.bind(localaddr);
        S.connect(remoteaddr, timeout);
      }
      return S;
    }

    public Socket createSocket(String h, int p) throws IOException,
        UnknownHostException {
      return getSSLContext().getSocketFactory().createSocket(h, p);
    }

    public Socket createSocket(Socket socket, String h, int p, boolean autoClose)
        throws IOException, UnknownHostException {
      return getSSLContext().getSocketFactory().createSocket(socket, h, p,
          autoClose);
    }

    public boolean equals(Object obj) {
      return ((obj != null) && obj.getClass().equals(SSLSocketFactory.class));
    }

    public int hashCode() {
      return SSLSocketFactory.class.hashCode();
    }
  }

  static public boolean isjava15() {
    boolean r = false;
    try {
      Class.forName("java.lang.annotation");
      r = true;
    } catch (Exception e) {
      r = false;
    }
    return r;
  }

  static public void install(TrustManager tm) throws Exception {
    // There's a problem (bug?) in Java 1.4 causing sc.init() to take a
    // very long time. Disabling installation of new trustmanager if
    // not 1.5 or newer. That's just fine cause 1.4 trustmanger accepts
    // self signed certificates.
    if (isjava15()) {
      SSLContext sc;
      sc = SSLContext.getInstance("SSL");
      sc.init(null, new TrustManager[] { tm }, null);
      /* register with standard HTTP implementation */
      HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

      /* register with Jakarta HTTPClient */
      Protocol https = new Protocol("https", new SSLSocketFactory(sc), 443);
      Protocol.registerProtocol("https", https);
    }
  }

  /**
   * Provide a full path to a keystore loc. This keystore will then be used for
   * * the verification of certificates provided by the server (assuming that
   * the * default TrustManager - which makes use of property
   * javax.net.ssl.trustStore - * is installed. * *
   * 
   * @return previous value of javax.net.ssl.trustStore
   */
  protected String useTrustStore(File f) {
    String v, p;

    if (f != null) {
      p = f.getAbsolutePath();

      /*
       * Check whether @f exists and would make sense. If appearing fishy, dump
       * warning messages but set keystore anyway.
       */

      if (!f.exists()) {
        System.err.println("*warning: trust store `" + p + "' does not exist.");
      }
      if (!f.isFile()) {
        System.err.println("*warning: trust store `" + p + "' is not a loc.");
      }
      if (!f.canRead()) {
        System.err.println("*warning: trust store `" + p + "' is unreadable.");
      }
      /* assign property - even if it appears invalid */
      v = System.setProperty(this.TRUSTSTORE, p);
    } else {
      /* remove trust store property */
      v = (String) System.getProperties().remove(this.TRUSTSTORE);
    }

    return v;
  }

  protected boolean hasTrustStore() {
    return System.getProperty(this.TRUSTSTORE, null) != null;
  }

  protected String getTrustStore() {
    return System.getProperty(this.TRUSTSTORE, null);
  }

  protected File getDefaultTrustStore() {
    final String fname = "depot.keystore"; /* default name */
    final String clazz = "it.haefelinger.flaka.Break"; /* search here .. */

    String path;
    File F = null;
    Class C = null;
    InputStream cin = null;

    /* retrieve loc from from package */
    try {
      C = Class.forName(clazz);
    } catch (Exception e) {
      error("unable to locate class `" + clazz + "' in classpath.");
      return null;
    }

    /* locate */
    cin = C.getResourceAsStream(fname);
    if (cin == null) {
      error("'" + fname + "' not found in \"flaka\".");
      return null;
    }

    /* create temporary loc */
    try {
      F = File.createTempFile("depot", null);
      F.deleteOnExit();
    } catch (Exception e) {
      error("unable to create a temporary loc", e);
      return null;
    }

    /* extract */
    path = F.getAbsolutePath();
    try {
      Static.writex(cin, path, false);
    } catch (Exception e) {
      F.delete();
      error("error while writing (temporary) loc '" + path + "'.", e);
      return null;
    }
    return F;
  }

  public void execute() throws BuildException {
    File ts;

    if (hasTrustStore()) {
      info("truststore already installed: " + this.TRUSTSTORE + "="
          + getTrustStore());
    } else {
      /* If no specific truststore given, use the default .. */
      ts = (this.truststore == null) ? getDefaultTrustStore() : this.truststore;

      if (ts == null) {
        info("no truststore available to initialize '" + this.TRUSTSTORE + "'.");
      } else {
        /* use my trust store */
        useTrustStore(ts);
      }
    }

    if (this.verifycert == false) {
      try {
        install(new AntiX509TrustManager());
        if (!this.quiet)
          warn("** certificate verification disabled **");
      } catch (Exception e) {
        System.err.println("*** error while installing trustmanager .."
            + e.getMessage());
        if (this.debug)
          e.printStackTrace(System.err);
        System.err.flush();
      }
    }
  }
}
