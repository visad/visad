package visad.install;

import java.awt.BorderLayout;
import java.awt.Dimension;

import java.io.File;

import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import javax.swing.border.TitledBorder;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import javax.swing.filechooser.FileFilter;

class FileListAccessory
  extends JPanel
{
  private JList fileList;

  FileListAccessory(String sectionName, File[] list)
  {
    setPreferredSize(new Dimension(200, 50));

    setBorder(new TitledBorder(sectionName));

    setLayout(new BorderLayout());

    fileList = new JList(list);
    fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    add(new JScrollPane(fileList));
  }

  public void addListSelectionListener(ListSelectionListener listener)
  {
    fileList.addListSelectionListener(listener);
  }

  public void removeListSelectionListener(ListSelectionListener listener)
  {
    fileList.removeListSelectionListener(listener);
  }
}

public class ChooserList
  extends JFileChooser
  implements ListSelectionListener
{
  public ChooserList()
  {
    this((File[] )null);
  }

  public ChooserList(ArrayList list)
  {
    this((File[] )(list == null ? null : list.toArray(new File[list.size()])));
  }

  public ChooserList(File[] list)
  {
    super();

    if (list != null) {
      updateSelectedFile(list[0]);

      FileListAccessory accessory = new FileListAccessory("Found", list);
      accessory.addListSelectionListener(this);

      setAccessory(accessory);
    }
  }

  public void valueChanged(ListSelectionEvent evt)
  {
    if (!evt.getValueIsAdjusting()) {
      JList source = (JList )evt.getSource();
      File sel = (File )source.getSelectedValue();

      updateSelectedFile(sel);

      // refresh
      invalidate();
      repaint();
    }
  }

  private void updateSelectedFile(File file)
  {
    // make sure filter doesn't exclude this file
    FileFilter filter = getFileFilter();
    if (filter != null) {
      if (!filter.accept(file)) {
        setFileFilter(getAcceptAllFileFilter());
      }
    }

    // point to the appropriate directory
    File parent = file.getParentFile();
    if (parent != null) {
      setCurrentDirectory(parent);
    }

    // clear out the current choice
    setSelectedFile(null);

    // set the new choice
    setSelectedFile(file);
  }
}
