package visad.util;

public class Delay {

  public static final int DEFAULT_DELAY = 50;

  public Delay() {
    this(DEFAULT_DELAY);
  }

  /** wait for millis milliseconds */
  public Delay(int millis) {
    wait(millis);
  }

  /** wait for millis milliseconds */
  public void wait(int millis) {
    try {
      synchronized(this) {
        super.wait(millis);
      }
    }
    catch (InterruptedException e) {
    }
  }

  public static void main(String[] args) { new Delay(10000); }
}
