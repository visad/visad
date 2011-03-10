/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2011 Bill Hibbard, Curtis Rueden, Tom
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

package visad.data.amanda;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;

import java.rmi.RemoteException;

import javax.swing.JLabel;
import javax.swing.JPanel;

import visad.ControlEvent;
import visad.ControlListener;
import visad.DataReferenceImpl;
import visad.FieldImpl;
import visad.PlotText;
import visad.ScalarMap;
import visad.ScalarMapControlEvent;
import visad.ScalarMapEvent;
import visad.ScalarMapListener;
import visad.ValueControl;
import visad.VisADException;

import visad.util.VisADSlider;

public class TrackWidget
  extends JPanel
  implements ControlListener, ScalarMapListener
{
  private ScalarMap map;
  private DataReferenceImpl ref;

  private Event event;

  private int trackIndex;

  private JLabel lengthLabel, energyLabel;

  public TrackWidget(ScalarMap map, DataReferenceImpl ref)
    throws RemoteException, VisADException
  {
    super();

    ValueControl ctl = (ValueControl )map.getControl();
    ctl.addControlListener(this);

    this.map = map;
    this.ref = ref;
    this.event = null;
    this.trackIndex = (int )ctl.getValue();

    setLayout(new BorderLayout());

    Component labels = buildLabels();

    VisADSlider trackSlider = new VisADSlider(map, true, true);
    trackSlider.hardcodeSizePercent(110); // leave room for label changes

    add(trackSlider, BorderLayout.NORTH);
    add(labels, BorderLayout.SOUTH);
  }

  public Component buildLabels()
  {
    JPanel panel = new JPanel();
    panel.setLayout(new FlowLayout());

    lengthLabel = new JLabel("WWWWWW.WWWW");
    lengthLabel.setMinimumSize(lengthLabel.getSize());
    energyLabel = new JLabel("WWWWWW.WWWW");
    energyLabel.setMinimumSize(energyLabel.getSize());

    panel.add(new JLabel("  Length: "));
    panel.add(lengthLabel);
    panel.add(new JLabel("  Energy: "));
    panel.add(energyLabel);

    return panel;
  }

  private void changeControl(ValueControl ctl)
  {
    if (event == null) {
      trackChanged(null);
    } else {
      trackIndex = (int )ctl.getValue();

      trackChanged(event.getTrack(trackIndex));
    }
  }

  public void controlChanged(ControlEvent evt)
  {
    changeControl((ValueControl )evt.getControl());
  }

  public void controlChanged(ScalarMapControlEvent evt)
  {
    changeControl((ValueControl )evt.getControl());
  }

  private static final String floatString(float val)
  {
    if (val == Float.POSITIVE_INFINITY) {
      return "inf";
    } else if (val == Float.NEGATIVE_INFINITY) {
      return "-inf";
    } else if (val == Float.NaN) {
      return "?";
    }

    return PlotText.shortString(val);
  }

  public void mapChanged(ScalarMapEvent evt)
  {
    System.err.println(evt);
  }

  public void setEvent(Event evt)
    throws RemoteException, VisADException
  {
    this.event = evt;
    if (event == null) {
      trackChanged(null);
    } else {
      map.setRange(0.0, (double )event.getNumberOfTracks());
      trackChanged(event.getTrack(trackIndex));
    }
  }

  private void trackChanged(BaseTrack track)
  {
    final FieldImpl trackSeq;

    if (track == null) {
      lengthLabel.setText("");
      energyLabel.setText("");

      trackSeq = BaseTrack.missing;
    } else {
      lengthLabel.setText(floatString(track.getLength()));
      energyLabel.setText(floatString(track.getEnergy()));

      trackSeq = event.makeTrackSequence(trackIndex);
    }

    try {
      ref.setData(trackSeq);
    } catch (RemoteException re) {
      re.printStackTrace();
    } catch (VisADException ve) {
      ve.printStackTrace();
    }

    this.invalidate();
  }
}
