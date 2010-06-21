package visad.data;

import visad.VisADError;

public class FlatFieldCacheError extends VisADError {
  public FlatFieldCacheError(String message, Throwable cause) {
    super(message);
    this.initCause(cause);
  }
}
