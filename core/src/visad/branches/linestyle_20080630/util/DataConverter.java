//
// DataConverter.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2008 Bill Hibbard, Curtis Rueden, Tom
Rink, Dave Glowacki, Steve Emmerson, Tom Whittaker, Don Murray, and
Tommy Jasmin.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Library General Public
License as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Library General Public License for more details.

You should have received a copy of the GNU Library General Public
License along with this library; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
MA 02111-1307, USA
*/

package visad.util;

import visad.*;
import visad.data.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.Vector;
import javax.swing.*;

/**
 * DataConverter provides a simple GUI for converting data
 * between VisAD-supported formats, using the data form of choice.
 */
public class DataConverter {

  // -- CONSTANTS --

  protected static final ExtensionFileFilter FILTER =
    new ExtensionFileFilter("class", "Java classes");


  // -- FIELDS --

  protected Vector forms;
  protected DataImpl data;
  protected File lastDir;

  protected JFrame frame;
  protected JTextArea area;
  protected JScrollPane scroll;
  protected JButton read, write;


  // -- CONSTRUCTORS --

  public DataConverter() { this(new File("../data"), "visad"); }

  public DataConverter(File dir, String prefix) {
    forms = new Vector();
    findForms(dir, prefix);
    doGUI();
  }


  // -- HELPER METHODS --

  protected void doGUI() {
    frame = new JFrame("VisAD Data Converter");
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) { System.exit(0); }
    });
    JPanel pane = new JPanel();
    pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
    frame.setContentPane(pane);

    area = new JTextArea("VisAD Data Converter\n\n");
    area.setEditable(false);
    scroll = new JScrollPane(area);
    pane.add(scroll);

    final JPopupMenu readMenu = new JPopupMenu();
    read = new JButton("Import");
    read.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        popup(read, readMenu);
      }
    });

    final JPopupMenu writeMenu = new JPopupMenu();
    write = new JButton("Export");
    write.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        popup(write, writeMenu);
      }
    });

    for (int i=0; i<forms.size(); i++) {
      final FormNode form = (FormNode) forms.elementAt(i);
      StringBuffer label = new StringBuffer(form.getClass().getName());
      String[] suffixes = getSuffixes(form);
      if (suffixes != null) {
        label.append(" (*.");
        label.append(suffixes[0]);
        for (int j=1; j<suffixes.length; j++) {
          if (j == 2 && j <= suffixes.length - 3) {
            // too many suffixes; abbreviate them
            label.append(", ...");
            j = suffixes.length - 3;
            continue;
          }
          label.append(", *.");
          label.append(suffixes[j]);
        }
        label.append(")");
      }
      String s = label.toString();

      JMenuItem readItem = new JMenuItem(s);
      readItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) { importData(form); }
      });
      readMenu.add(readItem);

      JMenuItem writeItem = new JMenuItem(s);
      writeItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) { exportData(form); }
      });
      writeMenu.add(writeItem);
    }

    JPanel p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    p.add(read);
    p.add(write);
    pane.add(p);

    frame.setSize(500, 300);
    Util.centerWindow(frame);
    frame.show();
  }

  protected void popup(JButton button, JPopupMenu menu) {
    Dimension b = read.getSize();
    Point loc = button.getLocationOnScreen();
    Dimension s = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension m = menu.getPreferredSize();
    int xpos = s.width - loc.x - m.width - b.width - 2;
    int ypos = s.height - loc.y - m.height - 30;
    if (xpos > 0) xpos = 0;
    if (ypos > 0) ypos = 0;
    menu.show(button, b.width + xpos, ypos);
  }

  protected void importData(FormNode form) {
    final FormNode loader = form;
    final File file = doDialog(form, false);
    if (file == null) return;
    Thread t = new Thread(new Runnable() {
      public void run() {
        setBusy(true);
        print("Importing " + file.getPath() + ": ");
        try {
          data = loader.open(file.getPath());
          if (data == null) println("Data is null");
          else {
            println("OK");
            println(data.getType().prettyString());
          }
        }
        catch (Throwable tt) {
          println("Error");
          println(tt);
        }
        setBusy(false);
      }
    });
    t.start();
  }

  protected void exportData(FormNode form) {
    final FormNode saver = form;
    final File file = doDialog(form, true);
    if (file == null) return;
    Thread t = new Thread(new Runnable() {
      public void run() {
        setBusy(true);
        print("Exporting " + file.getPath() + ": ");
        try {
          saver.save(file.getPath(), data, true);
          println("OK");
        }
        catch (Throwable tt) {
          println("Error");
          println(tt);
        }
        setBusy(false);
      }
    });
    t.start();
  }

  protected File doDialog(FormNode form, boolean save) {
    JFileChooser chooser = lastDir == null ?
      new JFileChooser() : new JFileChooser(lastDir);
    String[] suffixes = getSuffixes(form);
    if (suffixes != null) {
      chooser.setFileFilter(new ExtensionFileFilter(
        suffixes, form.getClass().getName()));
    }
    int retVal = save ? chooser.showSaveDialog(frame) :
      chooser.showOpenDialog(frame);
    lastDir = chooser.getCurrentDirectory();
    return retVal == JFileChooser.APPROVE_OPTION ?
      chooser.getSelectedFile() : null;
  }

  protected String[] getSuffixes(FormNode form) {
    if (!(form instanceof FormFileInformer)) return null;
    FormFileInformer ffi = (FormFileInformer) form;
    String[] suffixes = ffi.getDefaultSuffixes();
    return suffixes == null || suffixes.length == 0 ||
      suffixes[0].trim().equals("") ? null : suffixes;
  }

  protected void setBusy(boolean busy) {
    read.setEnabled(!busy);
    write.setEnabled(!busy);
    frame.setCursor(busy ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) :
      Cursor.getDefaultCursor());
  }

  protected void print(String text) {
    area.append(text);
    JViewport view = scroll.getViewport();
    Dimension size = view.getViewSize();
    view.setViewPosition(new Point(0, size.height));
  }

  protected void println(String text) { print(text + "\n\n"); }
  protected void println() { print("\n\n"); }
  protected void println(Throwable t) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    t.printStackTrace(pw);
    pw.close();
    print(sw.toString() + "\n\n");
  }

  protected void findForms(File f, String prefix) {
    if (f.isDirectory()) {
      File[] list = f.listFiles(FILTER);
      String name = f.getName();
      String pre = "".equals(prefix) ? name : prefix + "." + name;
      for (int i=0; i<list.length; i++) findForms(list[i], pre);
    }
    else {
      try {
        String name = f.getName();
        String qual = "".equals(prefix) ? name : prefix + "." + name;
        qual = qual.substring(0, qual.length() - 6);
        Class c = Class.forName(qual);
        boolean isForm = false;
        for (Class q=c; q!=Object.class && q!=null; q=q.getSuperclass()) {
          if (q == FormNode.class) {
            isForm = true;
            break;
          }
        }
        if (isForm) {
          FormNode form = null;
          try { form = (FormNode) c.newInstance(); }
          catch (InstantiationException exc) { }
          catch (IllegalAccessException exc) { }
          catch (IllegalArgumentException exc) { }
          try {
            Constructor con = c.getConstructor(new Class[] {String.class});
            form = (FormNode) con.newInstance(new Object[] {name});
          }
          catch (NoSuchMethodException exc) { }
          catch (SecurityException exc) { }
          catch (InstantiationException exc) { }
          catch (IllegalAccessException exc) { }
          catch (IllegalArgumentException exc) { }
          catch (InvocationTargetException exc) { }
          if (form != null) forms.add(form);
        }
      }
      catch (ClassNotFoundException exc) { }
      catch (NoClassDefFoundError err) { }
      catch (UnsatisfiedLinkError err) { }
    }
  }


  // -- MAIN METHOD --

  /** Run 'java visad.util.DataConverter' to convert some data. */
  public static void main(String[] args) { new DataConverter(); }

}
