package visad.data.visad.object;

public class BinarySize
{
  private int size;

  public BinarySize() { reset(); }

  public final void add(int size)
  {
    if (this.size != -1) {
      if (size == -1) {
        this.size = -1;
      } else {
        this.size += size;
      }
    }
  }

  public final int get() { return size; }

  public final void reset() { size = 0; }

  public final void set(int newSize) { size = newSize; }
}
