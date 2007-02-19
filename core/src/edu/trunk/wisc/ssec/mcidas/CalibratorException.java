package edu.wisc.ssec.mcidas;


/**
 * CalibratorException class is to handle exceptions when calibrating data.
 *
 * @author Bruce Flynn, SSEC
 */
public class CalibratorException extends McIDASException {

  /**
   * Constructs an CalibratorException with no specified detail message.
   */
  public CalibratorException() {super(); }

  /**
   * Constructs an CalibratorException with the specified detail message.
   *
   * @param  s  the detail message.
   */
  public CalibratorException(String s) {super(s); }

}
