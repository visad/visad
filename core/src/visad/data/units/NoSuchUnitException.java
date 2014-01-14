/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 */

package visad.data.units;

/**
 * Exception thrown when a unit specification can't be parsed because of an
 * unknown unit.
 */
public class NoSuchUnitException extends ParseException {

  /**
   * Constructs a {@code NoSuchUnitException} with no specified detail message.
   */
  public NoSuchUnitException() {
    super();
  }

  /**
   * Constructs a {@code NoSuchUnitException} with the specified detail message.
   *
   * @param message Detail message.
   */
  public NoSuchUnitException(String message) {
    super(message);
  }

  /**
   * Constructs a {@code NoSuchUnitException} with the specified detail message
   * and cause.
   *
   * @param message Detail message.
   * @param cause Cause of the exception. {@code null} indicates that the
   * cause is nonexistent or unknown.
   */
  public NoSuchUnitException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructs a {@code NoSuchUnitException} with the specified cause.
   *
   * @param cause Cause of the exception. {@code null} indicates that the
   * cause is nonexistent or unknown.
   */
  public NoSuchUnitException(Throwable cause) {
    super(cause);
  }
}
