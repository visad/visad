package visad.data.fits;

import java.util.Vector;
import java.util.Enumeration;

import visad.VisADException;

public class ExceptionStack
	extends VisADException
{
  Vector vec;

  public ExceptionStack()
  {
    super("Exception stack thrown");
    vec = new Vector();
  }

  public ExceptionStack(Exception e)
  {
    super("Exception stack thrown");
    vec = new Vector();
    vec.addElement(e);
  }

  public void addException(Exception e)
  {
    vec.addElement(e);
  }

  public int depth()
  {
    return vec.size();
  }

  public Enumeration exceptions()
  {
    return vec.elements();
  }

/*
  public String getMessage()
  {
    StringBuffer buf = new StringBuffer();

    Enumeration enum = vec.elements();
    while (enum.hasMoreElements()) {
      Exception e = (Exception )enum.nextElement();

      buf.append(e.getMessage());
      buf.append('\n');
    }

    // delete final newline
    buf.setLength(buf.length()-1);

    return buf.toString();
  }

  public String getLocalizedMessage()
  {
    StringBuffer buf = new StringBuffer();

    Enumeration enum = vec.elements();
    while (enum.hasMoreElements()) {
      Exception e = (Exception )enum.nextElement();

      buf.append(e.getLocalizedMessage());
      buf.append('\n');
    }

    // delete final newline
    buf.setLength(buf.length()-1);

    return buf.toString();
  }
*/

  public void printStackTrace()
  {
    printStackTrace(System.err);
  }

  public void printStackTrace(java.io.PrintStream ps)
  {
    Enumeration enum = vec.elements();
    while (enum.hasMoreElements()) {
      Exception e = (Exception )enum.nextElement();

      e.printStackTrace(ps);
    }
    super.printStackTrace(ps);
  }

  public void printStackTrace(java.io.PrintWriter pw)
  {
    Enumeration enum = vec.elements();
    while (enum.hasMoreElements()) {
      Exception e = (Exception )enum.nextElement();

      e.printStackTrace(pw);
    }
    super.printStackTrace(pw);
  }
}
