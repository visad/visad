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

public class ProgressMonitor
  extends JFrame
{
  class DetailLabel
    extends JLabel
  {
    private String text;
    private boolean show;

    public DetailLabel(String text, boolean show)
    {
      super(text);

      this.text = text;
      this.show = show;
    }

    public final boolean isShown() { return show; }

    public final void setText(String text)
    {
      this.text = text;

      if (show) {
        super.setText(text);
      }
    }

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

  private Font labelFont;

  private JLabel phaseLabel;
  private DetailLabel detailLabel;
  private JCheckBox detailBox;

  public ProgressMonitor()
  {
    this(true);
  }

  public ProgressMonitor(boolean showDetails)
  {
    this(new Font("sansserif", Font.PLAIN, 12), showDetails);
  }

  public ProgressMonitor(Font labelFont)
  {
    this(labelFont, true);
  }

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

  public final boolean isDetailShown() { return detailLabel.isShown(); }

  public final void setDetail(String detail)
  {
    detailLabel.setText(detail);
  }

  public final void setPhase(String phase)
  {
    phaseLabel.setText(phase);
    detailLabel.setText("");
  }

  private final void toggleShowDetails()
  {
    detailLabel.toggleShown();
  }
}
