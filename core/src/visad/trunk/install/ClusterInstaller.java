package visad.install;

import java.io.File;

public class ClusterInstaller
{
  private Runtime runtime = null;
  private String[] argList = null;

  private String cPush;

  public ClusterInstaller(String cPush)
  {
    this.cPush = cPush;
  }

  public boolean push(String fileStr)
  {
    if (runtime == null) {
      runtime = Runtime.getRuntime();
    }

    if (argList == null) {
      argList = new String[] { cPush, null };
    }

    argList[1] = fileStr;

    try {
      return (runtime.exec(argList).waitFor() == 0 ? true : false);
    } catch (Exception e) {
      return false;
    }
  }
}
