package visad;

/**
 * Display activity handler supplied to
 * {@link DisplayImpl#addActivityHandler(ActivityHandler)
 *   DisplayImpl.addActivityHandler}.<br>
 * <br>
 * A trivial implementation which toggles between two Data objects
 * in a Display would be:<br>
 * <br>
 * <code><pre>
 *    class SwitchGIFs implements ActivityHandler
 *    {
 *      SwitchGIFs(LocalDisplay d) { toggle(d, true); }
 *      public void busyDisplay(LocalDisplay d) { toggle(d, false); }
 *      public void idleDisplay(LocalDisplay d) { toggle(d, true); }
 *      private void toggle(LocalDisplay d, boolean first) {
 *        java.util.Vector v = d.getRenderers();
 *        ((DataRenderer )v.get(0)).toggle(first);
 *        ((DataRenderer )v.get(1)).toggle(!first);
 *      }
 *    }
 * </pre></code>
 */
public interface ActivityHandler
{
  /**
   * Method called when the Display becomes busy.
   *
   * @param dpy Busy Display.
   */
  void busyDisplay(LocalDisplay dpy);
  /**
   * Method called after the Display has been idle long enough.
   *
   * @param dpy Idle Display.
   */
  void idleDisplay(LocalDisplay dpy);
}
