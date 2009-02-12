package net.haefelingerit.flaka.tel;
import org.apache.tools.ant.Project;
public interface Binary
{
  public Binary reset(Project project);
  
  public boolean test_nt(String s, String v) throws Exception;
  public boolean test_ot(String s, String v) throws Exception;
  public boolean test_ef(String s, String v) throws Exception;
}
