package visad.install;

import java.io.File;
import java.io.IOException;

public class ClusterInstaller
{
  private Runtime runtime = null;
  private String[] argList = null;

  private String cPush;

  public ClusterInstaller(String cPush)
  {
    this.cPush = cPush;
  }

  public Process push(String target)
    throws IOException
  {
    return push(target, target);
  }

  public Process push(String source, String target)
    throws IOException
  {
    if (runtime == null) {
      runtime = Runtime.getRuntime();
    }

    if (argList == null) {
      argList = new String[] { cPush, null, null };
    }

    argList[1] = source;
    argList[2] = target;

    return runtime.exec(argList);
  }
}
