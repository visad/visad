import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.rmi.RemoteException;

import visad.DisplayImpl;
import visad.VisADException;

public abstract class UISkeleton
	extends TestSkeleton
{
  public UISkeleton() { }

  public UISkeleton(String args[])
	throws VisADException, RemoteException
  {
    super(args);
  }

  Component getSpecialComponent() { return null; }

  String getFrameTitle() { return "VisAD generic user interface"; }

  void setupUI(DisplayImpl[] dpys)
	throws VisADException, RemoteException
  {
    JPanel big_panel = new JPanel();

    Component special = getSpecialComponent();
    if (special != null) {
      big_panel.setLayout(new BorderLayout());
      big_panel.add("Center", special);
    } else {
      big_panel.setLayout(new BoxLayout(big_panel, BoxLayout.X_AXIS));
      big_panel.setAlignmentY(JPanel.TOP_ALIGNMENT);
      big_panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);

      for (int i = 0; i < dpys.length; i++) {
	big_panel.add(dpys[i].getComponent());
      }
    }

    JFrame jframe = new JFrame(getFrameTitle());
    jframe.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
    });
    jframe.setContentPane(big_panel);
    jframe.pack();
    jframe.setVisible(true);
  }
}
