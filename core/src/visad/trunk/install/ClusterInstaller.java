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

  public boolean push(String target)
  {
    return push(target, target);
  }

  public boolean push(String source, String target)
  {
    if (runtime == null) {
      runtime = Runtime.getRuntime();
    }

    if (argList == null) {
      argList = new String[] { cPush, null, null };
    }

    argList[1] = source;
    argList[2] = target;

    try {
      return (runtime.exec(argList).waitFor() == 0 ? true : false);
    } catch (Exception e) {
      return false;
    }
  }
}
