package visad.data.amanda;

public class Point
{
  private final float x, y, z;

  public Point(float x, float y, float z)
  {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  float getX() { return x; }
  float getY() { return y; }
  float getZ() { return z; }

  public String toString() { return "[" + x + "," + y + "," + z + "]"; }
}
