package visad;

import java.rmi.RemoteException;

/**
 * Interface for objects which wish to receive VisAD messages.
 */
public interface MessageListener
{
  /**
   * Receive a general VisAD message broadcast via
   * <tt>DisplayImpl.sendMessage()</tt>
   *
   * @param msg The message
   */
  void receiveMessage(MessageEvent msg)
    throws RemoteException;
}
