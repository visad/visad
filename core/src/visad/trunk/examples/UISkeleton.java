/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1999 Bill Hibbard, Curtis Rueden, Tom
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

  Component getSpecialComponent(DisplayImpl[] dpys)
	throws VisADException, RemoteException
  {
    return null;
  }

  String getFrameTitle() { return "VisAD generic user interface"; }

  void setupUI(DisplayImpl[] dpys)
	throws VisADException, RemoteException
  {
    JPanel big_panel = new JPanel();

    Component special = getSpecialComponent(dpys);
    if (special != null) {
      big_panel.setLayout(new BorderLayout());
      big_panel.add("Center", special);
    } else {
      big_panel.setLayout(new BoxLayout(big_panel, BoxLayout.X_AXIS));
      big_panel.setAlignmentY(JPanel.TOP_ALIGNMENT);
      big_panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);

      for (int i = 0; i < dpys.length; i++) {
	Component comp = dpys[i].getComponent();
	big_panel.add(comp);
      }
    }

    JFrame jframe = new JFrame(getFrameTitle() + getClientServerTitle());
    jframe.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
    });
    jframe.setContentPane(big_panel);
    jframe.pack();
    jframe.setVisible(true);
  }
}
