
//
// ErrorDialog.java
//

package visad.ss;

// AWT classes
import java.awt.Frame;
import com.sun.java.swing.JOptionPane;

/** The ErrorDialog class displays error messages in dialog boxes. */
public class ErrorDialog extends JOptionPane {
  Frame Parent;
  String Title;

  public ErrorDialog(Frame parent, String title) {
    super("", ERROR_MESSAGE);
    Parent = parent;
    if (title != null) Title = title;
    else Title = "Error";
  }

  public ErrorDialog(Frame parent) {
    this(parent, null);
  }

  public ErrorDialog(String title) {
    this(null, title);
  }

  public ErrorDialog() {
    this(null, null);
  }

  public void showError(String msg) {
    setMessage(msg);
    createDialog(Parent, Title).show();
  }
}
