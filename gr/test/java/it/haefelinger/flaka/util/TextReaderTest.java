package it.haefelinger.flaka.util;

import junit.framework.TestCase;

public class TextReaderTest extends TestCase {

  public TextReaderTest(String name) {
    super(name);
  }

  protected void setUp() throws Exception {
    super.setUp();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void test_stripcomments() {

    // examples of comment lines (a line starting with ;). The comment line
    // remover should remove *each* comment line. Thus the expected outcome
    // of this text is the empty string.
    String text = ";\n" + ";;\n" + "\t;\n" + " ;\n" + "; \n" + ";; \n"
        + "\t; \n" + " ; \n" + ";a\n" + ";;a\n" + "\t;a\n" + " ;a\n" + "";
    TextReader tr = new TextReader(text).setCL(";").setIC(";");
    String out = tr.stripcomments();
    assertEquals("", out);
  }

}
