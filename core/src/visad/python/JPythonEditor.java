//
// JPythonEditor.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2021 Bill Hibbard, Curtis Rueden, Tom
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

package visad.python;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import visad.VisADException;
import visad.util.*;

/** An editor for writing and executing JPython code in Java runtime. */
public class JPythonEditor extends CodeEditor {

  /** text to be prepended to all JPython documents */
  private static final String PREPENDED_TEXT =
    "from visad.python.JPythonMethods import *";

  /** monospaced font to use for error message reporting */
  private static final Font MONO_FONT = new Font("Monospaced", Font.PLAIN, 11);

  /** wrapper for PythonInterpreter object */
  protected RunJPython python = null;

  /** flag indicating whether to warn before auto-saving */
  protected boolean warnBeforeSave = true;
  
  /** flag indicating whether to launch scripts in a separate process */
  protected boolean runSeparate = true;

  /** run menu item */
  private JMenuItem runItem;


  /** runs the given command in a separate process */
  public static String[] runCommand(String cmd)
    throws IOException, VisADException
  {
    ArrayList list = new ArrayList();
    Process proc = Runtime.getRuntime().exec(cmd);

    // capture program output
    InputStream istr = proc.getInputStream();
    BufferedReader br = new BufferedReader(new InputStreamReader(istr));
    String str;
    while ((str = br.readLine()) != null) list.add(str);

    // wait for command to terminate
    try { proc.waitFor(); }
    catch (InterruptedException e) { e.printStackTrace(); }

    br.close();

    // check exit value
    if (proc.exitValue() != 0) {
      throw new VisADException("exit value was non-zero");
    }

    // return output as a list of strings
    return (String[]) list.toArray(new String[0]);
  }


  /** constructs a JPythonEditor */
  public JPythonEditor() throws VisADException {
    this(null);
  }

  /** constructs a JPythonEditor containing text from the given filename */
  public JPythonEditor(String filename) throws VisADException {
    super(filename);
    python = new RunJPython();


  }

  /** Create and initialize the the file chooser */
  protected JFileChooser doMakeFileChooser() {
      JFileChooser tmpFileChooser = super.doMakeFileChooser();
      // add JPython files to file chooser dialog box
      tmpFileChooser.addChoosableFileFilter(
          new ExtensionFileFilter("py", "JPython source code"));
      return tmpFileChooser;
  }


  /** adjusts the line number of the given error to match the displayed text,
      and highlights that line of code to indicate the source of error */
  private String handleError(String err, String filename) {
    // convert eol character sequences to newlines
    StringTokenizer st = new StringTokenizer(err, "\r\n");
    StringBuffer sbuf = new StringBuffer(err.length());
    while (st.hasMoreTokens()) {
      String line = st.nextToken();
      // convert tabs to spaces
      int tab = line.indexOf("\t");
      while (tab >= 0) {
        line = line.substring(0, tab) + "        " + line.substring(tab + 1);
        tab = line.indexOf("\t");
      }
      sbuf.append(line + "\n");
    }
    String nerr = sbuf.toString();

    // adjust error's line number to match displayed line numbers
    String toLine = filename + "\", ";
    int index = nerr.indexOf(toLine + "line ");
    if (index >= 0) {
      index += toLine.length() + 5;
      int nline = nerr.indexOf("\n", index);
      int lineNum = -1;
      try {
        lineNum = Integer.parseInt(nerr.substring(index, nline));
      }
      catch (NumberFormatException exc) { }
      lineNum--;
      if (lineNum >= 1) {
        highlightLine(lineNum);
        nerr = nerr.substring(0, index) + lineNum + nerr.substring(nline);
      }
    }

    // set up error message dialog
    Component c = getRootPane().getParent();
    final JDialog dialog =
      c instanceof Dialog ?
        new JDialog((Dialog) c, "JPython script error", true) :
      c instanceof Frame ?
        new JDialog((Frame) c, "JPython script error", true) :
        new JDialog((Frame) null, "JPython script error", true);

    // create dialog components
    JLabel label = new JLabel("An error in the script occurred:");
    label.setAlignmentX(JLabel.CENTER_ALIGNMENT);
    JTextArea area = new JTextArea(nerr, 16, 80);
    area.setEditable(false);
    area.setFont(MONO_FONT);
    area.setWrapStyleWord(true);
    JButton button = new JButton("OK");

    // define dialog actions
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        // dismiss dialog box
        dialog.hide();
      }
    });

    // do dialog layout
    JPanel p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
    p.add(label);
    p.add(area);
    p.add(button);
    dialog.setContentPane(p);

    // pop up dialog with error message
    dialog.pack();
    dialog.show();

    return nerr;
  }

  /** executes a line of JPython code */
  public void exec(String line) throws VisADException {
    python.exec(line);
  }

  /** executes the document as JPython source code */
  public void execfile(String filename) throws VisADException {
    try {
      python.execfile(filename);
    }
    catch (VisADException exc) {
      String error = handleError(exc.getMessage(), filename);
      throw new VisADException(error);
    }
  }

  /** returns a string containing the text of the document */
  public String getText() {
    return PREPENDED_TEXT +
      System.getProperty("line.separator") + super.getText();
  }

  /** sets the text of the document to the current string */
  public void setText(String text) {
    if (text.startsWith(PREPENDED_TEXT)) {
      // strip off prepended text
      text = text.substring(PREPENDED_TEXT.length()).trim();
    }
    super.setText(text);
  }

  /** sets whether editor should warn user before auto-saving */
  public void setWarnBeforeSave(boolean warn) { warnBeforeSave = warn; }

  /** sets whether editor should run scripts in a separate process */
  public void setRunSeparateProcess(boolean separate) {
    runSeparate = separate;
  }
  
  /** sets the run menu item */
  public void setRunItem(JMenuItem run) { runItem = run; }

  /** executes the JPython script */
  public void run() {
    if (hasChanged()) {
      if (warnBeforeSave) {
        int ans = JOptionPane.showConfirmDialog(this,
          "A save is required before execution. Okay to save?",
          "VisAD JPython Editor", JOptionPane.YES_NO_OPTION);
        if (ans != JOptionPane.YES_OPTION) return;
      }
      boolean success = saveFile();
      if (!success) return;
    }
    Thread t = new Thread(new Runnable() {
      public void run() {
        String name = getFilename();
        if (runSeparate) {
          String[] out;
          try {
            out = runCommand("java visad.python.RunJPython " + name);
          }
          catch (IOException exc) {
            if (DEBUG) exc.printStackTrace();
          }
          catch (VisADException exc) {
            if (DEBUG) exc.printStackTrace();
            String error = handleError(exc.getMessage(), name);
          }
        }
        else {
          runItem.setEnabled(false);
          runItem.setText("Running...");
          try {
            execfile(name);
          }
          catch (VisADException exc) {
            if (DEBUG) exc.printStackTrace();
          }
          runItem.setText("Run");
          runItem.setEnabled(true);
        }
      }
    });
    t.start();
  }

  /** compiles the JPython script to a Java class */
  public void compile() throws VisADException {
    throw new VisADException("Not yet implemented!");
  }
  
}
