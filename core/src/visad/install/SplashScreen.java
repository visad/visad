/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2015 Bill Hibbard, Curtis Rueden, Tom
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Toolkit;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JWindow;

/**
 * a simple splash screen that displays an image with a black border.
 */
public class SplashScreen
  extends JWindow
{
  private static final int BORDER_WIDTH = 5;
  private static final int WIDTH_PAD = 2;
  private static final int HEIGHT_PAD = 2;

  private ImageIcon image;
  private String[] names, values;
  private Font textFont;

  private int nameLen;
  private int  lineHeight;
  private int textX, textY, textWidth, textHeight;

  /**
   * Construct a splash screen which displays the specified image.
   *
   * @param imageName the image file name.
   */
  public SplashScreen(String imageName)
  {
    this(new ImageIcon(imageName), null, null);
  }

  /**
   * Construct a splash screen which displays the specified image.
   *
   * @param image the image.
   */
  public SplashScreen(ImageIcon image)
  {
    this(image, null, null);
  }

  /**
   * Construct a splash screen which displays the specified text.
   *
   * @param names list of names.
   * @param values list of values.
   */
  public SplashScreen(String[] names, String[] values)
  {
    this((ImageIcon )null, names, values);
  }

  /**
   * Construct a splash screen with both an image and some text.
   *
   * @param imageName the image file name.
   * @param names list of names.
   * @param values list of values.
   */
  public SplashScreen(String imageName, String[] names, String[] values)
  {
    this(new ImageIcon(imageName), names, values);
  }

  /**
   * Construct a splash screen with both an image and some text.
   *
   * @param image the image.
   * @param names list of names.
   * @param values list of values.
   */
  public SplashScreen(ImageIcon image, String[] names, String[] values)
  {
    this.image = image;
    this.names = names;
    this.values = values;

    textFont = new Font("sansserif", Font.PLAIN, 12);

    setContentPane(createSplash());

    pack();
    center();
  }

  private void center()
  {
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension splashSize = getContentPane().getPreferredSize();

    int x = (screenSize.width - splashSize.width) / 2;
    int y = (screenSize.height - splashSize.height) / 2;

    setBounds(x, y, splashSize.width, splashSize.height);
  }

  private JPanel createSplash()
  {
    JPanel panel = new JPanel();

    Dimension d;
    if (image == null) {
      d = new Dimension(400, 200);
    } else {
      d = new Dimension(image.getIconWidth() + (BORDER_WIDTH * 2),
                        image.getIconHeight() + (BORDER_WIDTH * 2));
    }

    panel.setMinimumSize(d);
    panel.setPreferredSize(d);
    panel.setMaximumSize(d);

    return panel;
  }

  private void drawTextArea(Graphics g)
  {
    // blank out the text area
    g.setColor(Color.white);
    g.fillRect(textX, textY, textWidth, textHeight);

    // fill in text area
    g.setColor(Color.black);
    int yPos = textY + (lineHeight - HEIGHT_PAD);
    for (int i = 0; i < values.length; i++) {
      if (values[i] != null) {
        g.drawString(names[i], textX+WIDTH_PAD, yPos);
        g.drawString(values[i], textX+WIDTH_PAD+nameLen+WIDTH_PAD, yPos);
        yPos += lineHeight;
      }
    }
  }

private void dumpFonts()
{
  java.awt.GraphicsEnvironment ge;
  ge = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment();

  Font[] allFonts = ge.getAllFonts();
  for (int i = 0; i < allFonts.length; i++) {
    System.out.println("#"+i+": " + allFonts[i]);
  }
}

  private boolean initializeTextArea(Graphics g)
  {
    if (names == null || values == null) {
      return false;
    }

    java.awt.FontMetrics fm = g.getFontMetrics();

    int numLines, valueLen;
    numLines = nameLen = valueLen = 0;
    for (int i = 0; i < values.length; i++) {
      if (values[i] != null) {
        int l = fm.stringWidth(names[i]);
        if (l > nameLen) {
          nameLen = l;
        }

        l = fm.stringWidth(values[i]);
        if (l > valueLen) {
          valueLen = l;
        }

        numLines++;
      }
    }

    // if there's nothing to write, we're done
    if (valueLen == 0) {
      return false;
    }

    int lineWidth = nameLen + WIDTH_PAD + valueLen;
    lineHeight = fm.getHeight() + HEIGHT_PAD;

    // compute the text area size
    textWidth = WIDTH_PAD + lineWidth + WIDTH_PAD;
    textHeight = HEIGHT_PAD + numLines * lineHeight;
    textX = 20;
    textY = getHeight() - (20 + textHeight);

    return true;
  }

  public void paint(Graphics g)
  {
    g.setFont(textFont);

    boolean drawText = initializeTextArea(g);

    final int width = getWidth();
    final int height = getHeight();

    // load either the image or a magenta blob
    if (image != null) {
      g.drawImage(image.getImage(), BORDER_WIDTH, BORDER_WIDTH,
                  getBackground(), this);
    } else {
      g.setColor(Color.magenta); 
      g.fillRect(0, 0, width, height);
    }

    // draw a black border around the image
    g.setColor(Color.black);
    g.fillRect(0, 0, width, BORDER_WIDTH);
    g.fillRect(0, 0, BORDER_WIDTH, height);
    g.fillRect(width-BORDER_WIDTH, BORDER_WIDTH,
               BORDER_WIDTH, height-BORDER_WIDTH);
    g.fillRect(BORDER_WIDTH, height-BORDER_WIDTH,
               width-BORDER_WIDTH, BORDER_WIDTH);

    if (drawText) {
      drawTextArea(g);
    }
  }

  public void setTextFont(Font f) { textFont = f; }

  public static final void main(String[] args)
  {
    Font f = null;
    if (args.length > 0) {
      int size = 12;
      if (args.length > 1) {
        try {
          size = Integer.parseInt(args[1]);
        } catch (NumberFormatException nfe) {
          System.err.println("Bad font size \"" + args[1] + "\"");
          System.exit(1);
        }
      }

      f = new Font(args[0], Font.PLAIN, size);
    }

    ImageIcon img = new ImageIcon("visad-splash.jpg");
    String[] names = new String[] { "Name:", "Machine:" };
    String[] values = new String[] { "dglo", "hyde.ssec.wisc.edu" };

    SplashScreen ss;

    ss = new SplashScreen(img);
    if (f != null) ss.setTextFont(f);
    ss.setVisible(true);
    try { Thread.sleep(7000); } catch (InterruptedException ie) { }
    ss.setVisible(false);

    ss = new SplashScreen(names, values);
    if (f != null) ss.setTextFont(f);
    ss.setVisible(true);
    try { Thread.sleep(7000); } catch (InterruptedException ie) { }
    ss.setVisible(false);

    ss = new SplashScreen(img, names, values);
    if (f != null) ss.setTextFont(f);
    ss.setVisible(true);
    try { Thread.sleep(7000); } catch (InterruptedException ie) { }
    ss.setVisible(false);

    System.exit(0);
  }
}
