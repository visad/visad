//
// ImageRendererJ3D.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2009 Bill Hibbard, Curtis Rueden, Tom
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

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.rmi.RemoteException;

import javax.media.j3d.BranchGroup;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

import visad.AnimationControl;
import visad.BadMappingException;
import visad.CoordinateSystem;
import visad.CachingCoordinateSystem;
import visad.InverseLinearScaledCS;
import visad.Data;
import visad.DataDisplayLink;
import visad.DataReference;
import visad.DataReferenceImpl;
import visad.Display;
import visad.DisplayException;
import visad.DisplayImpl;
import visad.DisplayRealType;
import visad.DisplayTupleType;
import visad.Field;
import visad.FieldImpl;
import visad.FlatField;
import visad.FunctionType;
import visad.Gridded1DDoubleSet;
import visad.MathType;
import visad.RealTupleType;
import visad.RealType;
import visad.ScalarMap;
import visad.ScalarType;
import visad.Set;
import visad.ShadowType;
import visad.ShadowRealType;
import visad.ShadowRealTupleType;
import visad.ShadowFunctionOrSetType;
import visad.VisADError;
import visad.VisADException;
import visad.data.netcdf.Plain;
import visad.java3d.DefaultRendererJ3D;
import visad.java3d.DisplayImplJ3D;
import visad.java3d.ShadowTypeJ3D;
import visad.java3d.VisADBranchGroup;
import visad.java3d.VisADImageNode;
import visad.util.Delay;

/**
   ImageRendererJ3D is the VisAD class for fast loading of images
   and image sequences under Java3D.

   WARNING - when reUseFrames is true during doTransform()
   ImageRendererJ3D makes these assumptions:
   1. That the images in a new time sequence are identical to
   any images at the same time in a previous sequence.
   2. That the image sequence defines the entire animation
   sampling.<P>
*/
public class ImageRendererJ3D extends DefaultRendererJ3D {

  // FOR DEVELOPMENT PURPOSES //////////////////////////////////
  private static final int DEF_IMG_TYPE;

  //GEOMETRY/COLORBYTE REUSE LOGIC VARIABLES (STARTS HERE)
  private int last_curve_size = -1;
  private float last_zaxis_value = Float.NaN;
  private float last_alpha_value = Float.NaN;
  private long last_data_hash_code = -1;
  //GEOMETRY/COLORBYTE REUSE LOGIC VARIABLES (ENDS HERE)



  static {
    String val = System.getProperty("visad.java3d.8bit", "false");
    if (Boolean.parseBoolean(val)) {
      DEF_IMG_TYPE = BufferedImage.TYPE_BYTE_GRAY;
      System.err.println("WARN: 8bit enabled via system property");
    } else {
      DEF_IMG_TYPE = BufferedImage.TYPE_4BYTE_ABGR;
    }
  }
  //////////////////////////////////////////////////////////////
  
  // MathTypes that data must equalsExceptNames()
  private static MathType image_sequence_type, image_type;
  private static MathType image_sequence_type2, image_type2;
  private static MathType image_sequence_type3, image_type3;

  // initialize above MathTypes
  static {
    try {
      image_type = MathType.stringToType(
        "((ImageElement, ImageLine) -> ImageValue)");
      image_sequence_type = new FunctionType(RealType.Time, image_type);
      image_type2 = MathType.stringToType(
        "((ImageElement, ImageLine) -> (ImageValue))");
      image_sequence_type2 = new FunctionType(RealType.Time, image_type2);
      image_type3 = MathType.stringToType(
        "((ImageElement, ImageLine) -> (Red, Green, Blue))");
      image_sequence_type3 = new FunctionType(RealType.Time, image_type3);
    }
    catch (VisADException e) {
      throw new VisADError(e.getMessage());
    }
  }

  /** determine whether the given MathType is usable with ImageRendererJ3D */
  public static boolean isImageType(MathType type) {
    return (image_sequence_type.equalsExceptName(type) ||
            image_sequence_type2.equalsExceptName(type) ||
            image_sequence_type3.equalsExceptName(type) ||
            image_type.equalsExceptName(type) ||
            image_type2.equalsExceptName(type) ||
            image_type3.equalsExceptName(type));
  }

  /** @deprecated Use isRendererUsable(MathType, ScalarMap[]) instead. */
  public static void verifyImageRendererUsable(MathType type, ScalarMap[] maps)
    throws VisADException
  {
    isRendererUsable(type, maps);
  }

  /** determine whether the given MathType and collection of ScalarMaps
      meets the criteria to use ImageRendererJ3D. Throw a VisADException
      if ImageRenderer cannot be used, otherwise return true. */
  public static boolean isRendererUsable(MathType type, ScalarMap[] maps)
    throws VisADException
  {
    RealType time = null;
    RealTupleType domain = null;
    RealTupleType range = null;
    RealType x = null, y = null;
    RealType rx = null, ry = null; // WLH 19 July 2000
    RealType r = null, g = null, b = null;
    RealType rgb = null;

    // must be a function
    if (!(type instanceof FunctionType)) {
      throw new VisADException("Not a FunctionType");
    }
    FunctionType function = (FunctionType) type;
    RealTupleType functionD = function.getDomain();
    MathType functionR = function.getRange();

    // time function
    if (function.equalsExceptName(image_sequence_type) ||
      function.equalsExceptName(image_sequence_type2) ||
      function.equalsExceptName(image_sequence_type3))
    {
      // strip off time RealType
      time = (RealType) functionD.getComponent(0);
      function = (FunctionType) functionR;
      functionD = function.getDomain();
      functionR = function.getRange();
    }

    // ((ImageLine, ImageElement) -> ImageValue)
    // ((ImageLine, ImageElement) -> (ImageValue))
    // ((ImageLine, ImageElement) -> (Red, Green, Blue))
    if (function.equalsExceptName(image_type) ||
        function.equalsExceptName(image_type2) ||
        function.equalsExceptName(image_type3)) {
      domain = function.getDomain();
      MathType rt = function.getRange();
      if (rt instanceof RealType) {
        range = new RealTupleType((RealType) rt);
      }
      else if (rt instanceof RealTupleType) {
        range = (RealTupleType) rt;
      } 
      else {
        // illegal MathType
        throw new VisADException("Illegal RangeType");
      }
    }
    else {
      // illegal MathType
      throw new VisADException("Illegal MathType");
    }

    // extract x and y from domain
    x = (RealType) domain.getComponent(0);
    y = (RealType) domain.getComponent(1);

    // WLH 19 July 2000
    CoordinateSystem cs = domain.getCoordinateSystem();
    if (cs != null) {
      RealTupleType rxy = cs.getReference();
      rx = (RealType) rxy.getComponent(0);
      ry = (RealType) rxy.getComponent(1);
    }

    // extract colors from range
    int dim = range.getDimension();
    if (dim == 1) rgb = (RealType) range.getComponent(0);
    else { // dim == 3
      r = (RealType) range.getComponent(0);
      g = (RealType) range.getComponent(1);
      b = (RealType) range.getComponent(2);
    }

    // verify that collection of ScalarMaps is legal
    boolean btime = (time == null);
    boolean bx = false, by = false;
    boolean brx = false, bry = false; // WLH 19 July 2000
    boolean br = false, bg = false, bb = false;
    boolean dbr = false, dbg = false, dbb = false;
    Boolean latlon = null;
    DisplayRealType spatial = null;

    for (int i=0; i<maps.length; i++) {
      ScalarMap m = maps[i];
      ScalarType md = m.getScalar();
      DisplayRealType mr = m.getDisplayScalar();
      boolean ddt = md.equals(time);
      boolean ddx = md.equals(x);
      boolean ddy = md.equals(y);
      boolean ddrx = md.equals(rx);
      boolean ddry = md.equals(ry);
      boolean ddr = md.equals(r);
      boolean ddg = md.equals(g);
      boolean ddb = md.equals(b);
      boolean ddrgb = md.equals(rgb);

      // animation mapping
      if (ddt) {
        if (btime) throw new VisADException("Multiple Time mappings");
        if (!mr.equals(Display.Animation)) {
          throw new VisADException(
            "Time mapped to something other than Animation");
        }
        btime = true;
      }

      // spatial mapping
      else if (ddx || ddy || ddrx || ddry) {
        if (ddx && bx || ddy && by || ddrx && brx || ddry && bry) {
          throw new VisADException("Duplicate spatial mappings");
        }
        if (((ddx || ddy) && (brx || bry)) ||
            ((ddrx || ddry) && (bx || by))) {
          throw new VisADException("reference and non-reference spatial mappings");
        }
        RealType q = (ddx ? x : null);
        if (ddy) q = y;
        if (ddrx) q = rx;
        if (ddry) q = ry;

        boolean ll;
        if (mr.equals(Display.XAxis) || mr.equals(Display.YAxis) ||
          mr.equals(Display.ZAxis))
        {
          ll = false;
        }
        else if (mr.equals(Display.Latitude) || mr.equals(Display.Longitude) ||
          mr.equals(Display.Radius))
        {
          ll = true;
        }
        else throw new VisADException("Illegal domain mapping");

        if (latlon == null) {
          latlon = new Boolean(ll);
          spatial = mr;
        }
        else if (latlon.booleanValue() != ll) {
          throw new VisADException("Multiple spatial coordinate systems");
        }
        // two mappings to the same spatial DisplayRealType are not allowed
        else if (spatial == mr) {
          throw new VisADException(
            "Multiple mappings to the same spatial DisplayRealType");
        }

        if (ddx) bx = true;
        else if (ddy) by = true;
        else if (ddrx) brx = true;
        else if (ddry) bry = true;
      }

      // rgb mapping
      else if (ddrgb) {
        if (br || bg || bb) {
          throw new VisADException("Duplicate color mappings");
        }
        if (rgb == null ||
            !(mr.equals(Display.RGB) || mr.equals(Display.RGBA))) {
          throw new VisADException("Illegal RGB/RGBA mapping");
        }
        dbr = dbg = dbb = true;
        br = bg = bb = true;
      }

      // color mapping
      else if (ddr || ddg || ddb) {
        if (rgb != null) throw new VisADException("Illegal RGB mapping");
        RealType q = (ddr ? r : (ddg ? g : b));
        if (mr.equals(Display.Red)) dbr = true;
        else if (mr.equals(Display.Green)) dbg = true;
        else if (mr.equals(Display.Blue)) dbb = true;
        else throw new VisADException("Illegal color mapping");

        if (ddr) br = true;
        else if (ddg) bg = true;
        else bb = true;
      }

      // illegal ScalarMap involving this MathType
      else if (ddt || ddx || ddy || ddrx || ddry ||
        ddr || ddg || ddb || ddrgb)
      {
        throw new VisADException("Illegal mapping: " + m);
      }
    }

    // return true if all conditions for ImageRendererJ3D are met
    if (!(btime && ((bx && by) || (brx && bry)) &&
          br && bg && bb && dbr && dbg && dbb)) {
      throw new VisADException("Insufficient mappings");
    }
    return true;
  }

  // flag to indicate:
  // 1. That the images in a new time sequence are identical to
  //    any images at the same time in a previous sequence.
  // 2. That the image sequence defines the entire animation
  //    sampling.<P>
  private boolean reUseFrames = false;

  private int suggestedBufImgType = DEF_IMG_TYPE;
  
  private boolean setSetOnReUseFrames = true;

  private VisADImageNode imagesNode = null;

  private boolean lastByRef = false;


  public static boolean isByRefUsable(DataDisplayLink link, ShadowType shadow) throws VisADException, RemoteException {
      ShadowFunctionOrSetType shadowType = (ShadowFunctionOrSetType) shadow.getAdaptedShadowType();
  
      CoordinateSystem dataCoordinateSystem = null;

      FieldImpl field = (FieldImpl) link.getData();
      if (!(field instanceof FlatField)) {
        shadowType = (ShadowFunctionOrSetType) shadowType.getRange();
        FlatField fltField = (FlatField) field.getSample(0);
        dataCoordinateSystem = fltField.getDomainCoordinateSystem();
      }
      else {
        dataCoordinateSystem = ((FlatField)field).getDomainCoordinateSystem();
        return true;
      }

      ShadowRealType[] DomainComponents = shadowType.getDomainComponents();
      ShadowRealTupleType Domain = shadowType.getDomain();
      ShadowRealTupleType domain_reference = Domain.getReference();
      ShadowRealType[] DC = DomainComponents;

      if (domain_reference != null &&
        domain_reference.getMappedDisplayScalar()) {
        DC = shadowType.getDomainReferenceComponents();
      }

      DisplayTupleType spatial_tuple = null;
      for (int i=0; i<DC.length; i++) {
        java.util.Enumeration maps =
          DC[i].getSelectedMapVector().elements();
        ScalarMap map = (ScalarMap) maps.nextElement();
        DisplayRealType real = map.getDisplayScalar();
        spatial_tuple = real.getTuple();
      }

      CoordinateSystem coord = spatial_tuple.getCoordinateSystem();

      if (coord instanceof CachingCoordinateSystem) {
        coord = ((CachingCoordinateSystem)coord).getCachedCoordinateSystem();
      }

      boolean useLinearTexture = false;
      if (coord instanceof InverseLinearScaledCS) {
        InverseLinearScaledCS invCS = (InverseLinearScaledCS)coord;
        useLinearTexture = (invCS.getInvertedCoordinateSystem()).equals(dataCoordinateSystem);
      }
    
      return useLinearTexture;
  }

  // factory for ShadowFunctionType that defines unique behavior
  // for ImageRendererJ3D
  public ShadowType makeShadowFunctionType(
         FunctionType type, DataDisplayLink link, ShadowType parent)
         throws VisADException, RemoteException {
    return new ShadowImageFunctionTypeJ3D(type, link, parent);
  }

  /**
   * Toggle the re-using of frames when a new image or set of images
   * is set in the datareference.<P>
   * <B>WARNING</B> - when reUseFrames is true during doTransform()
   * ImageRendererJ3D makes these assumptions:
   * <OL>
   * <LI>That the images in a new time sequence are identical to
   *     any images at the same time in a previous sequence.
   * <LI>That the image sequence defines the entire animation
   *     sampling.<P>
   * </OL>
   */
  public void setReUseFrames(boolean reuse) {
    reUseFrames = reuse;
  }

  /**
   * Suggest to the underlying shadow type the buffered image type
   * to use.
   * 
   * <b>Experimental</b>: This may changed or removed in future releases.
   */
  public void suggestBufImageType(int imageType) {
    switch (imageType) {
    case BufferedImage.TYPE_3BYTE_BGR:
    case BufferedImage.TYPE_4BYTE_ABGR:
    case BufferedImage.TYPE_BYTE_GRAY:
//    case BufferedImage.TYPE_USHORT_GRAY:
      break;
    default:
      throw new IllegalArgumentException("unsupported image type");
    }
    this.suggestedBufImgType = imageType;
  }
  
  /**
   * Get the image type. 
   * @return The buffered image type used to render the image.
   */
  int getSuggestedBufImageType() {
    return suggestedBufImgType;
  }

  public void setImageNode(VisADImageNode node) {
    this.imagesNode = node;
  }

  public VisADImageNode getImageNode() {
    return this.imagesNode;
  }
  
  /**
   * Turn on the reusing of frames
   * @deprecated - use setReUseFrames(boolean reuse)
   */
  public void setReUseFrames() {
    setReUseFrames(true);
  }

  public boolean getReUseFrames() {
    return reUseFrames;
  }

  public void setSetSetOnReUseFrames(boolean ss) {
    setSetOnReUseFrames = ss;
  }

  public boolean getSetSetOnReUseFrames() {
    return setSetOnReUseFrames;
  }

  // logic to allow ShadowImageFunctionTypeJ3D to 'mark' missing frames
  private VisADBranchGroup vbranch = null;

  public void clearScene() {
    vbranch = null;
    super.clearScene();
  }

  void setVisADBranch(VisADBranchGroup branch) {
    vbranch = branch;
  }

  void markMissingVisADBranch() {
    if (vbranch != null) vbranch.scratchTime();
  }
  // end of logic to allow ShadowImageFunctionTypeJ3D to 'mark' missing frames

  public BranchGroup doTransform() throws VisADException, RemoteException {

    DataDisplayLink[] Links = getLinks();
    if (Links == null || Links.length == 0) {
      return null;
    }

    DataDisplayLink link = Links[0];
    ShadowTypeJ3D type = (ShadowTypeJ3D) link.getShadow();
    boolean doByRef = false;
    if (isByRefUsable(link, type) && ShadowType.byReference) {
      doByRef = true;
      type = new ShadowImageByRefFunctionTypeJ3D(link.getData().getType(), link, null, 
                     ((ShadowFunctionOrSetType)type.getAdaptedShadowType()).getInheritedValues(),
                          (ShadowFunctionOrSetType)type.getAdaptedShadowType(), type.getLevelOfDifficulty());
    }

    BranchGroup branch = null;
    if ((lastByRef && doByRef) || (!lastByRef && !doByRef)) { 
      branch = getBranch();
    }
    lastByRef = doByRef;

    if (branch == null) {
      branch = new BranchGroup();
      branch.setCapability(BranchGroup.ALLOW_DETACH);
      branch.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
      branch.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
      branch.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
    }


    // initialize valueArray to missing
    int valueArrayLength = getDisplay().getValueArrayLength();
    float[] valueArray = new float[valueArrayLength];
    for (int i=0; i<valueArrayLength; i++) {
      valueArray[i] = Float.NaN;
    }

    Data data;
    try {
      data = link.getData();
    } catch (RemoteException re) {
      if (visad.collab.CollabUtil.isDisconnectException(re)) {
        getDisplay().connectionFailed(this, link);
        removeLink(link);
        return null;
      }
      throw re;
    }

    if (data == null) {
      branch = null;
      addException(
        new DisplayException("Data is null: DefaultRendererJ3D.doTransform"));
    }
    else {
      // check MathType of non-null data, to make sure it is a single-band
      // image or a sequence of single-band images
      MathType mtype = link.getType();
      if (!isImageType(mtype)) {
        throw new BadMappingException("must be image or image sequence");
      }
      link.start_time = System.currentTimeMillis();
      link.time_flag = false;
      vbranch = null;
      // transform data into a depiction under branch
	long t1 = System.currentTimeMillis();
      try {
	if (type instanceof ShadowImageByRefFunctionTypeJ3D) { //GEOMETRY/COLORBYTE REUSE LOGIC Only for ByRef for Time being
		if (checkAction()) { //This generally decides whether at all retransformation is required or not.
	        	type.doTransform(branch, data, valueArray,
                         	link.getDefaultValues(), this);
		}
	} else {	//Not byRef (ShadowImageFunctionTypeJ3D)
		type.doTransform(branch, data, valueArray,
                         link.getDefaultValues(), this);
	}
      } catch (RemoteException re) {
        if (visad.collab.CollabUtil.isDisconnectException(re)) {
          getDisplay().connectionFailed(this, link);
          removeLink(link);
          return null;
        }
        throw re;
      }
	long t2 = System.currentTimeMillis();
	//System.err.println("Time taken:" + (t2-t1));
    }
    link.clearData();

    return branch;
  }

  public Object clone() {
    return new ImageRendererJ3D();
  }

  /** run 'java visad.bom.ImageRendererJ3D len step'
      to test animation behavior of ImageRendererJ3D
      renders a loop of len at step ms per frame
      then updates loop by deleting first time and adding a new last time */
  public static void main(String args[])
         throws VisADException, RemoteException, IOException {

    int step = 1000;
    int len = 3;
    if (args.length > 0) {
      try {
        len = Integer.parseInt(args[0]);
      }
      catch(NumberFormatException e) {
        len = 3;
      }
    }
    if (len < 1) len = 1;
    if (args.length > 1) {
      try {
        step = Integer.parseInt(args[1]);
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
      // raw_image_sequence = (Field) plain.open("images256x256.nc");
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
    float raw_span = (4.0f / 3.0f) * (raw_times[0][3] - raw_times[0][0]);

    double[][] times = new double[1][len];
    for (int i=0; i<len; i++) {
      times[0][i] = raw_times[0][i % raw_len] + raw_span * (i / raw_len);
    }
    Gridded1DDoubleSet set =
      new Gridded1DDoubleSet(raw_set.getType(), times, len);
    Field image_sequence = new FieldImpl(image_sequence_type, set);
    for (int i=0; i<len; i++) {
      image_sequence.setSample(i, raw_image_sequence.getSample(i % raw_len));
    }

    // create a DataReference for image sequence
    final DataReference image_ref = new DataReferenceImpl("image");
    image_ref.setData(image_sequence);

    // create a Display using Java3D
    DisplayImpl display = new DisplayImplJ3D("image display");

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
    animation_control.setOn(true);

/*
    // link the Display to image_ref
    ImageRendererJ3D renderer = new ImageRendererJ3D();
    display.addReferences(renderer, image_ref);
    // display.addReference(image_ref);
*/

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

    System.out.println("first animation sequence");
    // link the Display to image_ref
    ImageRendererJ3D renderer = new ImageRendererJ3D();
    display.addReferences(renderer, image_ref);
    // display.addReference(image_ref);

    // wait 4 * len seconds
    new Delay(len * 4000);

    // substitute a new image sequence for the old one
    for (int i=0; i<len; i++) {
      times[0][i] =
        raw_times[0][(i + 1) % raw_len] + raw_span * ((i + 1) / raw_len);
    }
    set = new Gridded1DDoubleSet(raw_set.getType(), times, len);
    FieldImpl new_image_sequence = new FieldImpl(image_sequence_type, set);
    for (int i=0; i<len; i++) {
      new_image_sequence.setSample(i,
        raw_image_sequence.getSample((i + 1) % raw_len));
    }

    System.out.println("second animation sequence");

    // tell renderer to resue frames in its scene graph
    renderer.setReUseFrames(true);
    image_ref.setData(new_image_sequence);
  }


//GEOMETRY/COLORBYTE REUSE UTILITY METHODS (STARTS HERE)
  public int getLastCurveSize() {
	return last_curve_size;
  }

  public void setLastCurveSize(int csize) {
	last_curve_size = csize;
  }

  public float getLastZAxisValue() {
	return last_zaxis_value;
  }
  public void setLastZAxisValue(float zaxis_value) {
	  last_zaxis_value = zaxis_value;
  }

  public float getLastAlphaValue() {
	return last_alpha_value;
  }

  public void setLastAlphaValue(float alpha) {
        last_alpha_value = alpha;
  }

  public long getLastDataHashCode() {
	return last_data_hash_code;
  } 

  public void setLastDataHashCode(long lastdata_hashcode) {
	last_data_hash_code = lastdata_hashcode;
  }
//GEOMETRY/COLORBYTE REUSE UTILITY METHODS (ENDS HERE)

}

