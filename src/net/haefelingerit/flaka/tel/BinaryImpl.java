package net.haefelingerit.flaka.tel;

import org.apache.tools.ant.Project;

public class BinaryImpl implements Binary
{
  protected Project project;
  
  public Binary reset(Project project) {
    this.project = project;
    return this;
  }
  
  protected void debug(String msg) {
    if (this.project != null) {
      this.project.log(msg,Project.MSG_DEBUG);
    }
  }
  protected boolean test_not_supported(String s, String arg) throws Exception {
    int a = 1;
    if (a == 1)
      throw new Exception("error - test `" + s + " " + arg + "' not supported");
    return false;
  }
  /**
   * @param s
   * @param v
   * @return
   * @throws Exception
   */
  public boolean test_nt(String s, String v) throws Exception {
    return test_not_supported("-nt", "");
  }

 
  /**
   * @param s
   * @param v
   * @return
   * @throws Exception
   */
  public boolean test_ot(String s, String v) throws Exception {
    return test_not_supported("-ot", "");
  }

  /**
   * @param s
   * @param v
   * @return
   * @throws Exception
   */
  public boolean test_ef(String s, String v) throws Exception {
    return test_not_supported("-ef", "");
  }

  

}
