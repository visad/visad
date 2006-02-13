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

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;

import java.rmi.RemoteException;

import javax.swing.JPanel;

import visad.*;

import visad.bom.ImageRendererJ3D;

import visad.java3d.DisplayImplJ3D;

public class Test73
  extends TestSkeleton
{
  private String fileName;

  public Test73() { }

  public Test73(String[] args)
    throws RemoteException, VisADException
  {
    super(args);
  }

  public int checkKeyword(String testName, int argc, String[] args)
  {
    if (fileName == null) {
      fileName = args[argc];
    } else {
      System.err.println(testName + ": Ignoring extra filename \"" +
                         args[argc] + "\"");
    }

    return 1;
  }

  public String keywordUsage()
  {
    return super.keywordUsage() + " file";
  }

  public boolean finalizeArgs(String mainName)
  {
    if (fileName == null) {
      System.err.println(mainName + ": No filename specified!");
      return false;
    }

    return true;
  }

  DisplayImpl[] setupServerDisplays()
    throws RemoteException, VisADException
  {
    DisplayImpl[] dpys = new DisplayImpl[1];
    dpys[0] = new DisplayImplJ3D("display", DisplayImplJ3D.APPLETFRAME);
    return dpys;
  }

  void setupServerData(LocalDisplay[] dpys)
    throws RemoteException, VisADException
  {
    // load image in Java 1.2 compliant manner
    Image img = Toolkit.getDefaultToolkit().getImage(fileName);
    JPanel obs = new JPanel();
    MediaTracker tracker = new MediaTracker(obs);
    tracker.addImage(img, 0);
    try {
      tracker.waitForAll();
    }
    catch (InterruptedException exc) { exc.printStackTrace(); }
    int w = img.getWidth(obs);
    int h = img.getHeight(obs);

    // create BufferedImage of "TYPE_3BYTE_RGB" (TYPE_CUSTOM)
    // this type of BufferedImage is efficient with ImageFlatField.grabBytes
    int dataType = DataBuffer.TYPE_BYTE;
    ColorModel colorModel = new ComponentColorModel(
      ColorSpace.getInstance(ColorSpace.CS_sRGB),
      false, false, ColorModel.TRANSLUCENT, dataType);
    byte[][] data = new byte[3][w * h];
    SampleModel model = new BandedSampleModel(dataType, w, h, data.length);
    DataBuffer buffer = new DataBufferByte(data, data[0].length);
    WritableRaster raster = Raster.createWritableRaster(model, buffer, null);
    BufferedImage image = new BufferedImage(colorModel, raster, false, null);

    // paint image into buffered image
    Graphics gr = image.createGraphics();
    gr.drawImage(img, 0, 0, obs);
    gr.dispose();
    gr = null;

    // convert image to VisAD object
    RealType x = RealType.getRealType("x");
    RealType y = RealType.getRealType("y");
    RealTupleType xy = new RealTupleType(x, y);
    int num = image.getRaster().getNumBands();
    RealType r = null, g = null, b = null, v = null;
    MathType range = null;
    if (num == 3) {
      r = RealType.getRealType("r");
      g = RealType.getRealType("g");
      b = RealType.getRealType("b");
      range = new RealTupleType(r, g, b);
    }
    else if (num == 1) {
      v = RealType.getRealType("value");
      range = v;
    }
    else {
      System.err.println("Image has unsupported # of bands (" + num + ")");
      System.exit(1);
    }
    FunctionType type = new FunctionType(xy, range);
    Integer2DSet set = new Integer2DSet(xy, w, h);
    ImageFlatField ff = new ImageFlatField(type, set);
    ff.setImage(image);

    dpys[0].addMap(new ScalarMap(x, Display.XAxis));
    dpys[0].addMap(new ScalarMap(y, Display.YAxis));
    if (num == 3) {
      dpys[0].addMap(new ScalarMap(r, Display.Red));
      dpys[0].addMap(new ScalarMap(g, Display.Green));
      dpys[0].addMap(new ScalarMap(b, Display.Blue));
    }
    else {
      dpys[0].addMap(new ScalarMap(v, Display.RGB));
    }
    dpys[0].getGraphicsModeControl().setTextureEnable(true);
    DataReferenceImpl ref = new DataReferenceImpl("ref");
    ref.setData(ff);
    dpys[0].addReferences(new ImageRendererJ3D(),
      new DataReference[] {ref}, null);
  }

  String getFrameTitle() { return "ImageFlatField with ImageRendererJ3D"; }

  public String toString() {
    return " file_name: ImageFlatField with ImageRendererJ3D";
  }

  public static void main(String[] args)
    throws RemoteException, VisADException
  {
    new Test73(args);
  }
}
