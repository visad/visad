package visad;

/**
 * The interface for objects which need to be informed
 * when a remote data source goes away.
 */
public interface RemoteSourceListener
{
  /**
   * A remote <tt>Data</tt> object is no longer available.
   *
   * @param name The name of the <tt>Data</tt> object.
   */
  void dataSourceLost(String name);

  /**
   * A remote collaboration peer is no longer available.
   */
  void collabSourceLost(int connectionID);
}
