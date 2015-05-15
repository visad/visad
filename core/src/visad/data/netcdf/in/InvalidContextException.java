/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: InvalidContextException.java,v 1.1 1998-09-23 17:31:32 steve Exp $
 */
package visad.data.netcdf.in;

import visad.VisADException;

/**
 * Exception thrown when the I/O context is invalid for an operation.
 */
public class InvalidContextException extends VisADException {

  /**
   * Constructs a {@code InvalidContextException} with no specified detail 
   * message.
   */
  public InvalidContextException() {
    super();
  }

  /**
   * Constructs a {@code InvalidContextException} with the specified detail 
   * message.
   *
   * @param message Detail message.
   */
  public InvalidContextException(String message) {
    super(message);
  }

  /**
   * Constructs a {@code InvalidContextException} with the specified detail 
   * message and cause.
   *
   * @param message Detail message.
   * @param cause Cause of the exception. {@code null} indicates that the
   * cause is nonexistent or unknown.
   */
  public InvalidContextException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructs a {@code InvalidContextException} with the specified cause.
   *
   * @param cause Cause of the exception. {@code null} indicates that the
   * cause is nonexistent or unknown.
   */
  public InvalidContextException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructs a {@code InvalidContextException} with a given {@code Context}
   * and cause.
   *
   * @param context Exception context. Should not be {@code null}.
   */
  public InvalidContextException(Context context) {
    super(context.toString());
  }

  /**
   * Constructs a {@code InvalidContextException} with the specified context 
   * and cause.
   *
   * @param context Exception context. Should not be {@code null}.
   * @param cause Cause of the exception. {@code null} indicates that the
   * cause is nonexistent or unknown.
   */
  public InvalidContextException(Context context, Throwable cause) {
    super(context.toString(), cause);
  }
}
