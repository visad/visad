package visad.data.amanda;

import java.awt.Component;

import java.rmi.RemoteException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import visad.ControlEvent;
import visad.ControlListener;
import visad.PlotText;
import visad.ScalarMap;
import visad.ValueControl;
import visad.VisADException;

import visad.data.amanda.BaseTrack;
import visad.data.amanda.Event;

import visad.util.VisADSlider;

public class TrackWidget
  extends JPanel
  implements ControlListener
{
  private Event event;

  private int trackIndex;

  private JLabel lengthLabel, energyLabel;

  public TrackWidget(ScalarMap map)
    throws RemoteException, VisADException
  {
    super();

    ValueControl ctl = (ValueControl )map.getControl();
    ctl.addControlListener(this);

    this.event = null;
    this.trackIndex = (int )ctl.getValue();

    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

    Component labels = buildLabels();

    VisADSlider trackSlider = new VisADSlider(map, true, true);
    trackSlider.hardcodeSizePercent(110); // leave room for label changes

    add(trackSlider);
    add(labels);
  }

  public Component buildLabels()
  {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

    lengthLabel = new JLabel("WWWWWW.WWWW");
    lengthLabel.setMinimumSize(lengthLabel.getSize());
    energyLabel = new JLabel("WWWWWW.WWWW");
    energyLabel.setMinimumSize(energyLabel.getSize());

    panel.add(Box.createHorizontalGlue());
    panel.add(new JLabel("Length: "));
    panel.add(lengthLabel);
    panel.add(Box.createHorizontalGlue());
    panel.add(new JLabel("  Energy: "));
    panel.add(energyLabel);
    panel.add(Box.createHorizontalGlue());

    return panel;
  }

  public void controlChanged(ControlEvent evt)
  {
    ValueControl ctl = (ValueControl )evt.getControl();

    trackIndex = (int )ctl.getValue();

    if (event == null) {
      trackChanged(null);
    } else {
      trackChanged(event.getTrack(trackIndex));
    }
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

  public void setEvent(Event evt)
  {
    this.event = evt;
    if (event == null) {
      trackChanged(null);
    } else {
      trackChanged(event.getTrack(trackIndex));
    }
  }

  private void trackChanged(BaseTrack track)
  {
    if (track == null) {
      lengthLabel.setText("");
      energyLabel.setText("");
    } else {
      lengthLabel.setText(floatString(track.getLength()));
      energyLabel.setText(floatString(track.getEnergy()));
    }

    this.invalidate();
  }
}
