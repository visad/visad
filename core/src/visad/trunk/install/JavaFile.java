package visad.install;

import java.io.File;

public class JavaFile
  extends File
{
  private String fullString;
  private int major, minor;
  private boolean versionSet;

  public JavaFile(String path)
  {
    this(new File(path));
  }

  public JavaFile(File path)
  {
    this(path.getParent(), path.getName());
  }

  public JavaFile(String path, String name)
  {
    this(new File(path), name);
  }

  public JavaFile(File path, String name)
  {
    super(path, name);

    major = minor = -1;
    versionSet = false;
  }

  public String getFullString() { return fullString; }
  public int getMajor() { return major; }
  public int getMinor() { return minor; }

  public void setVersion(String fullString, int major, int minor)
  {
    this.fullString = fullString;
    this.major = major;
    this.minor = minor;
    versionSet = true;
  }
}
