//
//  WeatherSymbols.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2018 Bill Hibbard, Curtis Rueden, Tom
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

package visad.meteorology;

import visad.*;
import visad.util.HersheyFont;
import visad.java2d.DisplayImplJ2D;
import javax.swing.JFrame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Utility class to create shapes for weather symbols.  Shapes are
 * stroke generated from the "wmo" HersheyFont.
 * @see "<a href="http://www.wmo.ch">WMO Manual on Codes</a> for a 
 *       description of WMO weather codes."
 */
public class WeatherSymbols {

  private static HersheyFont wmoFont = new HersheyFont("wmo");
  private static final int numMetSymbols = 205;

  private static VisADLineArray[] metSymbols = 
      new VisADLineArray[numMetSymbols];


  /** starting index of present weather symbols in the whole list */
  private final static int PRESENTWX_INDEX = 0;
  /** starting index of low cloud symbols in the whole list */
  private final static int LOCLD_INDEX = 104;
  /** starting index of mid cloud symbols in the whole list */
  private final static int MIDCLD_INDEX = 113;
  /** starting index of high cloud symbols in the whole list */
  private final static int HICLD_INDEX = 122;
  /** starting index of pressure tendency symbols in the whole list */
  private final static int TNDCY_INDEX = 142;
  /** starting index of cloud coverage symbols in the whole list */
  private final static int SKY_INDEX = 131;
  /** starting index of icing symbols in the whole list */
  private final static int TURB_INDEX = 151;
  /** starting index of icing symbols in the whole list */
  private final static int ICING_INDEX = 160;
  /** starting index of misc symbols in the whole list */
  private final static int MISC_INDEX = 171;

  /** Number of WMO weather symbols */
  public final static int PRESENTWX_NUM = 104;

  /** Number of low cloud symbols */
  public final static int LOCLD_NUM = 9;

  /** Number of mid cloud symbols */
  public final static int MIDCLD_NUM  = 9;

  /** Number of high cloud symbols */
  public final static int HICLD_NUM = 9;

  /** Number of pressure tendency symbols */
  public final static int TNDCY_NUM = 9;

  /** Number of cloud coverage symbols */
  public final static int SKY_NUM = 11;

  /** Number of icing symbols */
  public final static int ICING_NUM = 9;

  /** Number of turbulence symbols */
  public final static int TURB_NUM = 11;

  /** Number of miscellaneous symbols */
  public final static int MISC_NUM = 34;

  /** Number of lightning symbols */
  public final static int LIGHTNING_NUM = 2;

  private static VisADLineArray[] lightningSymbols = 
      new VisADLineArray[LIGHTNING_NUM];

  static {

    // set up WMO symbols
    double[] start = { 0, -.5, 0 };
    double[] base = { 1, 0, 0 };
    double[] up = { 0, 1., 0 };
    try {
      for (int i = 0; i < numMetSymbols; i++) {
        metSymbols[i] = 
          PlotText.render_font( 
            new String(new byte[] {(byte) (i+32)}, 0), wmoFont, 
                                      start, base, up, true);
      }
    } catch (Exception excp) {
       System.err.println("Unable to intialize symbols properly");
    }

      // set up lightning symbols
    VisADLineArray flash = new VisADLineArray();
    flash.coordinates = new float[]
    { 0.25f, 0.8f, 0.0f,   -0.25f,  0.0f,  0.0f,
     -0.25f, 0.0f, 0.0f,    0.25f, -0.8f,  0.0f,
      0.0f, -0.8f, 0.0f,    0.25f, -0.8f,  0.0f,
      0.25f,-0.8f, 0.0f,    0.25f, -0.55f, 0.0f};
    flash.vertexCount = flash.coordinates.length / 3;
    lightningSymbols[0] = flash;

    flash = new VisADLineArray();
    flash.coordinates = new float[]
    { -0.25f, 0.8f, 0.0f,    0.25f,  0.0f,  0.0f,
       0.25f, 0.0f, 0.0f,   -0.25f, -0.8f,  0.0f,
       0.0f, -0.8f, 0.0f,   -0.25f, -0.8f,  0.0f,
      -0.25f,-0.8f, 0.0f,   -0.25f, -0.55f, 0.0f};
    flash.vertexCount = flash.coordinates.length / 3;
    lightningSymbols[1] = flash;
  }

  /**
   * Default constructor.
   */
  public WeatherSymbols() {}

  /**
   * Get the array of shapes corresponding to the
   * WMO present weather codes, plus the GEMPAK extensions.
   * @return the array of shapes
   */
  public static VisADLineArray[] getPresentWeatherSymbols() {
    return subsetArray(metSymbols, PRESENTWX_INDEX, PRESENTWX_NUM);
  }

  /**
   * Get the array of shapes corresponding to the
   * WMO present weather codes, plus the GEMPAK extensions.
   * @return the array of shapes
   */
  public static VisADLineArray[] getAllMetSymbols() {
    return subsetArray(metSymbols, 0, numMetSymbols);
  }

  /**
   * Get the shape corresponding to a particular present weather code.
   * The number of codes are too numerous to list here.
   * @param  wxCode  weather code
   * @return shape for code
   */
  public static VisADLineArray getPresentWeatherSymbol(int wxCode) {
    if (wxCode < 0 || wxCode >= PRESENTWX_NUM) {
      throw new IllegalArgumentException( "unknown weather symbol: " + wxCode);
    }
    return getVLAClone(metSymbols, PRESENTWX_INDEX+wxCode);
  }

  /**
   * Get the array of shapes corresponding to the
   * pressure tendency symbol codes.
   * @return the array of shapes
   * @see #getPressureTendencySymbol for the indices
   */
  public static VisADLineArray[] getPressureTendencySymbols() {
    return subsetArray(metSymbols, TNDCY_INDEX, TNDCY_NUM);
  }

  /**
   * Get the shape corresponding to a particular pressure
   * tendency code.  Codes are:
   * <pre>
   *   0  -  rising then falling
   *   1  -  rising then steady; or rising, then rising more slowly
   *   2  -  rising steadily or unsteadily
   *   3  -  falling or steady, then rising; or rising, 
   *           then rising more quickly
   *   4  -  steady, same as 3 hours ago
   *   5  -  falling then rising, same or lower than 3 hours ago
   *   6  -  falling then steady; or falling, then falling more slowly
   *   7  -  falling steadily, or unsteadily
   *   8  -  steady or rising, then falling; or falling, 
   *           then falling more quickly
   * </pre>
   * @param  tendencyCode tendency code to use
   * @return  corresponding shape
   */
  public static VisADLineArray getPressureTendencySymbol(int tendencyCode) {
    if (tendencyCode < 0 || tendencyCode >= TNDCY_NUM) {
       throw new IllegalArgumentException(
             "unknown pressure tendency symbol: " + tendencyCode);
    }
    return getVLAClone(metSymbols, TNDCY_INDEX+tendencyCode);
  }

  /**
   * Get the array of shapes corresponding to the cloud coverage codes.
   * @return the array of shapes
   * @see #getCloudCoverageSymbol(int) for codes
   */
  public static VisADLineArray[] getCloudCoverageSymbols() {
    return subsetArray(metSymbols, SKY_INDEX, SKY_NUM);
  }

  /**
   * Look up the symbol directly in the full array
   *
   * @param index array index
   * @return The symbol
   */
  public static VisADLineArray getSymbol(int index) {
      if(index<0 || index>= metSymbols.length) {
       throw new IllegalArgumentException(
             "bad symbol index: " + index);
      }
      return getVLAClone(metSymbols,index);
  }


  /**
   * Get the shape corresponding to a total sky cover code.
   * Codes are:
   * <pre>
   *   0  -  No clouds
   *   1  -  Less than one-tenth or one-tenth
   *   2  -  two-tenths or three-tenths
   *   3  -  four-tenths
   *   4  -  five-tenths
   *   5  -  six-tenths
   *   6  -  seven-tenths or eight-tenths
   *   7  -  nine-tenths or overcast with openings
   *   8  -  completely overcast
   *   9  -  sky obscured
   *  10  -  missing
   * </pre>
   * @param   ccCode     cloud coverage code to use
   * @return  corresponding shape
   */
  public static VisADLineArray getCloudCoverageSymbol(int ccCode) {
    if (ccCode < 0 || ccCode >= SKY_NUM) {
       throw new IllegalArgumentException(
             "unknown cloud coverage symbol: " + ccCode);
    }
    return getVLAClone(metSymbols, SKY_INDEX+ccCode);
  }

  /**
   * Get the array of shapes corresponding to the low cloud codes.
   * @return the array of shapes
   * @see #getLowCloudSymbol(int) for codes
   */
  public static VisADLineArray[] getLowCloudSymbols() {
    return subsetArray(metSymbols, LOCLD_INDEX, LOCLD_NUM);
  }

  /**
   * Get the shape corresponding to a particular low cloud code.
   * Codes are:
   * <pre>
   *   1  -  Cu of fair weather, little vertical development,
   *            seemingly flattened
   *   2  -  Cu of considerable development, generally towering,
   *            with or without other Cu or Sc bases all at same level
   *   3  -  Cb with tops lacking clear cut outlines, but distinctly
   *            no cirriform or anvil-shaped; with or without Cu, Sc, or St
   *   4  -  Sc formed by spreading out of Cu; Cu often present also
   *   5  -  Sc not formed by spreading out of Cu
   *   6  -  St or Fs or both, but no Fs of bad weather
   *   7  -  Fs and/or Fc of bad weather (scud)
   *   8  -  Cu and Sc (not formed by spreading out of Cu) with bases 
   *            at different levels
   *   9  -  Cb having a clearly fibrous (cirriform) top, often anvil-shaped,
   *            with or without Cu, Sc, St, or scud
   * </pre>
   * @param   lcCode     low cloud code to use
   * @return  corresponding shape
   */
  public static VisADLineArray getLowCloudSymbol(int lcCode) {
    if (lcCode < 1 || lcCode > LOCLD_NUM) {
       throw new IllegalArgumentException(
           "unknown low cloud symbol: " + lcCode);
    }
    return getVLAClone(metSymbols, LOCLD_INDEX+lcCode-1);
  }

  /**
   * Get the array of shapes corresponding to the mid cloud codes.
   * @return the array of shapes
   * @see #getMidCloudSymbol(int) for codes
   */
  public static VisADLineArray[] getMidCloudSymbols() {
    return subsetArray(metSymbols, MIDCLD_INDEX, MIDCLD_NUM);
  }

  /**
   * Get the shape corresponding to a particular mid level cloud code.
   * Codes are:
   * <pre>
   *   1  -  Thin As (most of cloud layer semi-transparent)
   *   2  -  Thick As, greater part sufficiently dense to hide sun 
   *           (or moon), or Ns
   *   3  -  Thin Ac, mostly semi-transparent; cloud elements not changing
   *           much and at a single level
   *   4  -  Thin Ac in patches; cloud elements continually changing and/or
   *           occurring at more than one level
   *   5  -  Thin Ac in bands or in a layer gradually spreading over sky
   *           and usually thickening as a whole
   *   6  -  Ac formed by spreading out of Cu
   *   7  -  Double-layered Ac, or a thick layer of Ac, not increasing;
   *           or Ac with As and/or Ns
   *   8  -  Ac in the form of Cu-shaped tufts or Ac with turrets
   *   9  -  Ac of a chaotic sky, usually at different levels; patches of
   *           dense Ci are usually present also
   * </pre>
   * @param   mcCode     mid cloud code to use
   * @return  corresponding shape
   */
  public static VisADLineArray getMidCloudSymbol(int mcCode) {
    if (mcCode < 1 || mcCode > MIDCLD_NUM) {
       throw new IllegalArgumentException(
           "unknown mid cloud symbol: " + mcCode );
    }
    return getVLAClone(metSymbols, MIDCLD_INDEX+mcCode-1);
  }

  /**
   * Get the array of shapes corresponding to the high cloud codes.
   * @return the array of shapes
   * @see #getHighCloudSymbol(int) for codes
   */
  public static VisADLineArray[] getHighCloudSymbols() {
    return subsetArray(metSymbols, HICLD_INDEX, HICLD_NUM);
  }

  /**
   * Get the shape corresponding to a particular high cloud code.
   * Codes are:
   * <pre>
   *   1  -  Filaments of Ci, or "mares tails", scattered and not increasing
   *   2  -  Dense Ci in patches or twisted sheaves, usually not increasing,
   *           sometimes like remains of Cb; or towers or tufts
   *   3  -  Dense Ci, often anvil shaped derived from or associated with Cb
   *   4  -  Ci, often hook shaped, spreading over the sky and usually
   *           thickening as a whole
   *   5  -  Ci and Cs, often in converging bands, or Cs alone; generally
   *           overspreading and growing denser; the continuous layer 
   *           not reaching 45 degrees altitude
   *   6  -  Ci and Cs, often in converging bands, or Cs alone; generally
   *           overspreading and growing denser; the continuous layer 
   *           exceeding 45 degrees altitude
   *   7  -  Veil of Cs covering the entire sky
   *   8  -  Cs not increasing and not covering the entire sky
   *   9  -  Cc alone or Cc with some Ci or Cs, but the Cc being the main
   *           cirriform cloud
   * </pre>
   * @param   hcCode     high cloud code to use
   * @return  corresponding shape
   */
  public static VisADLineArray getHighCloudSymbol(int hcCode) {
    if (hcCode < 1 || hcCode > HICLD_NUM) {
       throw new IllegalArgumentException(
           "unknown high cloud symbol: " + hcCode );
    }
    return getVLAClone(metSymbols, HICLD_INDEX+hcCode-1);
  }

  /**
   * Get the array of shapes corresponding to the icing codes.
   * @return the array of shapes
   * @see #getIcingSymbol(int) for codes
   */
  public static VisADLineArray[] getIcingSymbols() {
    return subsetArray(metSymbols, ICING_INDEX, ICING_NUM);
  }

  /**
   * Get the shape corresponding to a particular icing symbol code.
   * Codes are:
   * <pre>
   *   0  -  No icing
   *   1  -  Trace icing
   *   2  -  Trace to light icing
   *   3  -  Light icing
   *   4  -  Light to moderate icing
   *   5  -  Moderate icing
   *   6  -  Moderate to heavy icing
   *   7  -  Heavy or moderate to severe icing
   *   8  -  Severe icing
   *   9  -  Light superstructure icing
   *  10  -  Heavy superstructure icing
   * </pre>
   * @param   icingCode     icing code to use
   * @return  corresponding shape
   */
  public static VisADLineArray getIcingSymbol(int icingCode) {
    if (icingCode < 0 || icingCode >= ICING_NUM) {
       throw new IllegalArgumentException(
           "unknown icing symbol: " + icingCode );
    }
    return getVLAClone(metSymbols, ICING_INDEX+icingCode);
  }

  /**
   * Get the array of shapes corresponding to the turbulence codes.
   * @return the array of shapes
   * @see #getTurbulenceSymbol(int) for codes
   */
  public static VisADLineArray[] getTurbulenceSymbols() {
    return subsetArray(metSymbols, TURB_INDEX, TURB_NUM);
  }

  /**
   * Get the shape corresponding to a particular turbulence symbol code.
   * Codes are:
   * <pre>
   *   0  -  No turbulence
   *   1  -  Light turbulence
   *   2  -  Light turbulence
   *   3  -  Light to moderate turbulence
   *   4  -  Moderate turbulence
   *   5  -  Moderate to severe turbulence
   *   6  -  Severe turbulence
   *   7  -  Extreme turbulence
   *   8  -  Extreme turbulence
   * </pre>
   * @param   turbCode     turbulence code to use
   * @return  corresponding shape
   */
  public static VisADLineArray getTurbulenceSymbol(int turbCode) {
    if (turbCode < 0 || turbCode >= TURB_NUM) {
       throw new IllegalArgumentException(
           "unknown turbulence symbol: " + turbCode );
    }
    return getVLAClone(metSymbols, TURB_INDEX+turbCode);
  }


  /**
   * Get the array of shapes corresponding to the miscellaneous codes.
   * @return the array of shapes
   * @see #getMiscSymbol(int) for codes
   */
  public static VisADLineArray[] getMiscSymbols() {
    return subsetArray(metSymbols, MISC_INDEX, MISC_NUM);
  }

  /**
   * Get the shape corresponding to a particular miscellaneous symbol.
   * Codes are:
   * <pre>
   *   0  -  Square (outline)            16 - Tropical Storm (NH)
   *   1  -  Square (filled)             17 - Hurricane (NH)
   *   2  -  Circle (outline)            18 - Tropical Storm (SH)
   *   3  -  Circle (filled)             19 - Hurricane (SH)
   *   4  -  Triangle (outline)          20 - Triangle with antenna
   *   5  -  Triangle (filled)           21 - Mountain obscuration
   *   6  -  Diamond (outline)           22 - Slash
   *   7  -  Diamond (filled)            23 - Storm Center
   *   8  -  Star (outline)              24 - Tropical Depression
   *   9  -  Star (filled)               25 - Tropical Cyclone
   *  10  - High Pressure (outline)      26 - Flame
   *  11  - Low Pressure (outline)       27 - "X" Cross
   *  12  - High Pressure (filled)       28 - Low pressure with X (outline)
   *  13  - Low Pressure (filled)        29 - Low pressure with X (filled)
   *  14  - Plus sign                    30 - Tropical Storm (NH)
   *  15  - Minus sign                   31 - Tropical Storm (SH)
   *  32  - Volcanic activity            33 - Blowing spray
   * </pre>
   * @param   miscCode     miscellaneous code to use
   * @return  corresponding shape
   */
  public static VisADLineArray getMiscSymbol(int miscCode) {
    if (miscCode < 0 || miscCode >= MISC_NUM) {
       throw new IllegalArgumentException(
           "unknown turbulence symbol: " + miscCode );
    }
    return getVLAClone(metSymbols, MISC_INDEX+miscCode);
  }

  /**
   * Get the array of shapes corresponding to lightning symbols
   * @return the array of shapes
   * @see #getLightningSymbol(int) for codes
   */
  public static VisADLineArray[] getLightningSymbols() {
    return subsetArray(lightningSymbols, 0, LIGHTNING_NUM);
  }

  /**
   * Get the shape corresponding to a particular lightning code.
   * Codes are:
   * <pre>
   *   0  -  Negative flash
   *   1  -  Positive flash
   * </pre>
   * @param   lghtCode     lightning code to use
   * @return  corresponding shape
   */
  public static VisADLineArray getLightningSymbol(int lghtCode) {
    if (lghtCode < 0 || lghtCode >= LIGHTNING_NUM) {
       throw new IllegalArgumentException(
           "unknown lightning symbol: " + lghtCode );
    }
    return getVLAClone(lightningSymbols, lghtCode);
  }

  private static VisADLineArray[] subsetArray(VisADLineArray[] array,
                                              int start, int number) {
    VisADLineArray[] retArray = new VisADLineArray[number];
    for (int i = 0; i < number; i++) {
      retArray[i] = (VisADLineArray) array[start+i].clone();
    }
    return retArray;
  }

  private static VisADLineArray getVLAClone(VisADLineArray[] array, int index) {
    return (VisADLineArray) array[index].clone();
  }

  public static void main( String[] args) throws Exception {

    DisplayImpl display = new DisplayImplJ2D("display");
    display.getDisplayRenderer().setBoxOn(false);
    double[] matrix = display.getProjectionControl().getMatrix();
    matrix[0] = 1.25;
    matrix[3] = -1.25;
    display.getProjectionControl().setMatrix(matrix);
    display.addMap(new ScalarMap(RealType.YAxis, Display.YAxis));
    display.addMap(new ScalarMap(RealType.XAxis, Display.XAxis));
    float[][] values = new float[3][220];
    int l = 0;
    for (int x = 0; x < 11; x++) {
      for (int y = 0; y < 20; y++) {
        values[0][l] = -1.f + y/10.f;
        values[1][l] = 1.f - x/4.f;
        values[2][l] = l++;
      }
    }
    Gridded3DSet set =
      new Gridded3DSet(RealTupleType.SpatialCartesian3DTuple, values, l);
    ScalarMap shapeMap = new ScalarMap(RealType.ZAxis, Display.Shape);
    display.addMap(shapeMap);
    ShapeControl sc = (ShapeControl) shapeMap.getControl();
    sc.setShapeSet(new Integer1DSet(l));
    sc.setShapes(WeatherSymbols.getAllMetSymbols());
    sc.setScale(0.1f);
    DataReference ref = new DataReferenceImpl("ref");
    ref.setData(set);
    display.addReference(ref);
    JFrame frame = new JFrame("Weather Symbol Plot Test");
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });
    frame.getContentPane().add(display.getComponent());
    frame.pack();
    frame.setSize(500, 500);
    frame.setVisible(true);

  }
}
