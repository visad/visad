//
// ImageRendererJ3D.java
//

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

package visad.bom;

import visad.*;
import visad.java3d.*;
import visad.util.VisADSlider;
import visad.util.Delay;
import visad.data.netcdf.Plain;

import javax.media.j3d.*;

import java.rmi.*;
import java.io.IOException;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
   ImageRendererJ3D is the VisAD class for fast loading of images
   and image sequences under Java3D.

   WARNING - when reUseFrames is true during doTransform()
   this DataREnderer makes these assumptions:
   1. That the images in a new time sequence are identical to
   any images at the same time in a previous sequence.
   2. That the image sequence defines the entire animation
   sampling.<P>
*/
public class ImageRendererJ3D extends DefaultRendererJ3D {

  private MathType image_sequence_type, image_type;
  private MathType image_sequence_type2, image_type2;

  private boolean reUseFrames = false;

  /** this DataRenderer supports fast loading of images and image
      sequences for DisplayImplJ3D */
  public ImageRendererJ3D () {
    super();
    try {
      image_type =
        MathType.stringToType("((ImageElement, ImageLine) -> ImageValue)");
      image_sequence_type = new FunctionType(RealType.Time, image_type);
      image_type2 =
        MathType.stringToType("((ImageElement, ImageLine) -> (ImageValue))");
      image_sequence_type2 = new FunctionType(RealType.Time, image_type2);
    }
    catch (VisADException e) {
      throw new VisADError(e.getMessage());
    }
  }

  public ShadowType makeShadowFunctionType(
         FunctionType type, DataDisplayLink link, ShadowType parent)
         throws VisADException, RemoteException {
    return new ShadowImageFunctionTypeJ3D(type, link, parent);
  }

  public void setReUseFrames() {
    reUseFrames = true;
  }

  public boolean getReUseFrames() {
    return reUseFrames;
  }

  public BranchGroup doTransform() throws VisADException, RemoteException { 
    BranchGroup branch = getBranch();
    if (branch == null) {
      branch = new BranchGroup();
      branch.setCapability(BranchGroup.ALLOW_DETACH);
      branch.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
      branch.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
      branch.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
    }
    DataDisplayLink link = getLinks()[0];
    ShadowTypeJ3D type = (ShadowTypeJ3D) link.getShadow();
 
    // initialize valueArray to missing
    int valueArrayLength = getDisplay().getValueArrayLength();
    float[] valueArray = new float[valueArrayLength];
    for (int i=0; i<valueArrayLength; i++) {
      valueArray[i] = Float.NaN;
    }
 
    Data data = link.getData();
    if (data == null) {
      branch = null;
      addException(
        new DisplayException("Data is null: DefaultRendererJ3D.doTransform"));
    }
    else {
      // check MathType of non-nul data, to make sure it is a single-band
      // image or a sequence of single-band images
      MathType mtype = link.getType();
      if (image_sequence_type.equalsExceptName(mtype) ||
          image_sequence_type2.equalsExceptName(mtype)) {
      }
      else if (image_type.equalsExceptName(mtype) ||
               image_type2.equalsExceptName(mtype)) {
      }
      else {
        reUseFrames = false;
        throw new BadMappingException("must be image or image sequence");
      }
      link.start_time = System.currentTimeMillis();
      link.time_flag = false;
      type.doTransform(branch, data, valueArray,
                       link.getDefaultValues(), this);
    }
    link.clearData();
    reUseFrames = false;
    return branch;
  }

  /** render three image frames for times = 0, 1, 2, wait
      ten seconds, then change to times = 1, 2, 3
      type 'java visad.bom.ImageRendererJ3D' to run this test */
  public static void main(String args[])
         throws VisADException, RemoteException, IOException {

    int step = 1000;
    if (args.length > 0) {
      try {
        step = Integer.parseInt(args[0]);
      }
      catch(NumberFormatException e) {
        step = 1000;
      }
    }
    if (step < 1) step = 1;

    // create a netCDF reader
    Plain plain = new Plain();

    // open a netCDF file containing an image sequence and adapt
    // it to a Field Data object
    Field raw_image_sequence = null;
    try {
      raw_image_sequence = (Field) plain.open("images.nc");
    }
    catch (IOException exc) {
      String s = "To run this example, the images.nc file must be "
        +"present in\nthe current directory."
        +"You can obtain this file from:\n"
        +"  ftp://www.ssec.wisc.edu/pub/visad-2.0/images.nc.Z";
      System.out.println(s);
      System.exit(0);
    }

    // just take first half of raw_image_sequence
    FunctionType image_sequence_type =
      (FunctionType) raw_image_sequence.getType();
    Set raw_set = raw_image_sequence.getDomainSet();
    float[][] raw_times = raw_set.getSamples();
    int raw_len = raw_times[0].length;
    if (raw_len != 4) {
      throw new VisADException("wrong number of images in sequence");
    }
    int len = 3;
    double[][] times = new double[1][len];
    for (int i=0; i<len; i++) times[0][i] = raw_times[0][i];
    Gridded1DDoubleSet set =
      new Gridded1DDoubleSet(raw_set.getType(), times, len);
    Field image_sequence = new FieldImpl(image_sequence_type, set);
    for (int i=0; i<len; i++) {
      image_sequence.setSample(i, raw_image_sequence.getSample(i));
    }

    // create a DataReference for image sequence
    final DataReference image_ref = new DataReferenceImpl("image");
    image_ref.setData(image_sequence);

    // create a Display using Java3D
    DisplayImpl display = new DisplayImplJ3D("image display");
    // create a Display using Java2D
    // DisplayImpl display = new DisplayImplJ2D("image display");

    // extract the type of image and use
    // it to determine how images are displayed
    FunctionType image_type =
      (FunctionType) image_sequence_type.getRange();
    RealTupleType domain_type = image_type.getDomain();
    // map image coordinates to display coordinates
    display.addMap(new ScalarMap((RealType) domain_type.getComponent(0),
                                 Display.XAxis));
    display.addMap(new ScalarMap((RealType) domain_type.getComponent(1),
                                 Display.YAxis));
    // map image brightness values to RGB (default is grey scale)
    display.addMap(new ScalarMap((RealType) image_type.getRange(),
                                 Display.RGB));
    RealType hour_type =
      (RealType) image_sequence_type.getDomain().getComponent(0);
    ScalarMap animation_map = new ScalarMap(hour_type, Display.Animation);
    display.addMap(animation_map);
    AnimationControl animation_control =
      (AnimationControl) animation_map.getControl();
    animation_control.setStep(step);

    // link the Display to image_ref
    ImageRendererJ3D renderer = new ImageRendererJ3D();
    display.addReferences(renderer, image_ref);
    // display.addReference(image_ref);

    // create JFrame (i.e., a window) for display and slider
    JFrame frame = new JFrame("ImageRendererJ3D test");
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {System.exit(0);}
    });

    // create JPanel in JFrame
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setAlignmentY(JPanel.TOP_ALIGNMENT);
    panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    frame.getContentPane().add(panel);

    // add display to JPanel
    panel.add(display.getComponent());

    // set size of JFrame and make it visible
    frame.setSize(500, 500);
    frame.setVisible(true);

    // wait 10 seconds
    new Delay(10000);

    // tell renderer to resue frames in its scene graph
    renderer.setReUseFrames();

    // substitute a new image sequence for the old one
    for (int i=0; i<len; i++) times[0][i] = raw_times[0][i + 1];
    set = new Gridded1DDoubleSet(raw_set.getType(), times, len);
    image_sequence = new FieldImpl(image_sequence_type, set);
    for (int i=0; i<len; i++) {
      image_sequence.setSample(i, raw_image_sequence.getSample(i + 1));
    }

    image_ref.setData(image_sequence);
  }

}

