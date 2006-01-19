/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2006 Bill Hibbard, Curtis Rueden, Tom
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

/**
 * An auxiliary widget which displays a pre-built list of file selections.
 */
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

  public void setListData(File[] list)
  {
    fileList.setListData(list);
  }
}

/**
 * A JFileChooser widget which includes a pre-built list of choices.
 */
public class ChooserList
  extends JFileChooser
  implements ListSelectionListener
{
  private FileListAccessory accessory;

  public ChooserList()
  {
    accessory = null;
  }

  /**
   * Set the list of choices.
   *
   * @param list list of File objects
   */
  public void setList(File[] list)
  {
    if (list == null) {
      setAccessory(null);
      updateSelectedFile(null);
    }else {
      updateSelectedFile(list[0]);

      if (accessory == null) {
        accessory = new FileListAccessory("Found", list);
      } else {
        accessory.setListData(list);
      }

      accessory.addListSelectionListener(this);

      setAccessory(accessory);
    } 
  }

  /**
   * Update widget to point to the selected file.
   */
  private final void updateSelectedFile(File file)
  {
    if (file != null) {

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
    }

    // set the new choice
    setSelectedFile(file);
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
}
