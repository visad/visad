package visad.collab;

import java.rmi.RemoteException;

public class CollabUtil
{
  public static final boolean isDisconnectException(RemoteException re)
  {
    return (re.detail instanceof java.io.EOFException ||
            re.detail instanceof java.net.SocketException ||
            re.detail instanceof java.net.ConnectException);
  }
}
