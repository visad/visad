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

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Widget showing general and detailed progress.
 */
public class ProgressMonitor
  extends JFrame
{
  /**
   * Label used to track and display detailed progress information.
   */
  class DetailLabel
    extends JLabel
  {
    private String text;
    private boolean show;

    DetailLabel(String text, boolean show)
    {
      super(text);

      this.text = text;
      this.show = show;
    }

    /**
     * Indicate whether detailed progress is shown.
     *
     * @return true if detailed progress is shown.
     */
    final boolean isShown() { return show; }

    /**
     * Set new detailed progress text.
     *
     * @param text detailed progress text
     */
    public final void setText(String text)
    {
      this.text = text;

      if (show) {
        super.setText(text);
      }
    }

    /**
     * Toggle visibility of detailed progress text.
     */
    final void toggleShown()
    {
      show = !show;
      if (show) {
        super.setText(text);
      } else {
        super.setText("");
        this.invalidate();
      }
    }
  }

  // font used to draw text
  private Font labelFont;

  // internal widgets
  private JLabel phaseLabel;
  private DetailLabel detailLabel;
  private JCheckBox detailBox;

  /**
   * Create a progress monitor which uses a 12 pt Sans Serif font and
   * initially displays the detailed progress text.
   */
  public ProgressMonitor()
  {
    this(true);
  }

  /**
   * Create a progress monitor which uses a 12 pt Sans Serif font.
   *
   * @param showDetails <tt>true</tt> if details are initially displayed.
   */
  public ProgressMonitor(boolean showDetails)
  {
    this(new Font("sansserif", Font.PLAIN, 12), showDetails);
  }

  /**
   * Create a progress monitor which
   * initially displays the detailed progress text.
   *
   * @param labelFont the font used to draw all text
   */
  public ProgressMonitor(Font labelFont)
  {
    this(labelFont, true);
  }

  /**
   * Create a progress monitor.
   *
   * @param showDetails <tt>true</tt> if details are initially displayed.
   * @param labelFont the font used to draw all text
   */
  public ProgressMonitor(Font labelFont, boolean showDetails)
  {
    super("Installation Progress Monitor");

    //
    // compute label height & width
    //

    final String w40 = "WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW";
    final String longStr = w40 + w40 + w40;

    FontMetrics fm = getFontMetrics(labelFont);

    final int width = fm.stringWidth(w40);
    final int height = fm.getHeight();

    Dimension d = new Dimension(width, height);

    //
    // build UI elements
    //

    phaseLabel = new JLabel("Phase");
    phaseLabel.setFont(labelFont);
    phaseLabel.setMinimumSize(d);
    phaseLabel.setPreferredSize(d);
    phaseLabel.setMaximumSize(d);

    detailLabel = new DetailLabel("", showDetails);
    detailLabel.setFont(labelFont);
    detailLabel.setMinimumSize(d);
    detailLabel.setPreferredSize(d);
    detailLabel.setMaximumSize(d);

    detailBox = new JCheckBox("Show details", showDetails);
    detailBox.setFont(labelFont);
    detailBox.addItemListener(new ItemListener()
      {
        public void itemStateChanged(ItemEvent e)
        {
          detailLabel.toggleShown();
          if (detailLabel.isShown()) {
            pack();
          }
        }
      });

    //
    // Add UI elements
    //

    JPanel pane = new JPanel();
    pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
    pane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    pane.add(Box.createHorizontalStrut(10));
    pane.add(phaseLabel);
    pane.add(Box.createHorizontalStrut(10));
    pane.add(detailLabel);
    pane.add(Box.createHorizontalStrut(10));
    pane.add(detailBox);
    pane.add(Box.createHorizontalStrut(10));

    setContentPane(pane);
    pack();
  }

  /**
   * Indicate whether detailed progress is shown.
   *
   * @return true if detailed progress is shown.
   */
  public final boolean isDetailShown() { return detailLabel.isShown(); }

  /**
   * Set new detailed progress text.
   *
   * @param detail detailed progress text
   */
  public final void setDetail(String detail)
  {
    detailLabel.setText(detail);
  }

  /**
   * Set new progress phase text.
   *
   * @param detail progress phase text
   */
  public final void setPhase(String phase)
  {
    phaseLabel.setText(phase);
    detailLabel.setText("");
  }
}
