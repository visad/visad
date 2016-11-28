package visad.test;

import java.awt.BorderLayout;
import java.rmi.RemoteException;

import org.jogamp.java3d.BranchGroup;
import javax.swing.JFrame;
import javax.swing.JPanel;

import visad.AnimationControl;
import visad.CoordinateSystem;
import visad.GridCoordinateSystem;
import visad.DataReference;
import visad.DataReferenceImpl;
import visad.Display;
import visad.DisplayImpl;
import visad.Field;
import visad.FieldImpl;
import visad.FlatField;
import visad.FunctionType;
import visad.Integer1DSet;
import visad.Linear2DSet;
import visad.Gridded2DSet;
import visad.RealTupleType;
import visad.RealType;
import visad.ScalarMap;
import visad.VisADException;
import visad.bom.ImageRendererJ3D;
import visad.java3d.DisplayImplJ3D;
import visad.java3d.DisplayRendererJ3D;
import visad.util.Util;

/**
 * Simple test for <code>AnimationControl</code> and <code>ImageRendererJ3D</code>.
 */
public class ImageAnimationTest extends JPanel {

  ImageRendererJ3D imgRend;

  public static final float[] makeSinusoidalSamples(int lenX, int lenY, int waveNum) {
    float[] samples = new float[lenX*lenY];
    int index = 0;
    double PI = Math.PI;
    for (int ii = 0; ii < lenX; ii++) {
      for (int jj = 0; jj < lenY; jj++) {
        samples[index] = (float) (
            Math.sin((waveNum*PI) / 100 * jj) *
            Math.sin((waveNum*PI) / 100 * ii));
        index++;
      }
    }
    return samples;
  }
  
  protected float[][] makeSamples(int size, int num) {
    float[][] samples = new float[num][size^2];
    for (int i=0; i<num; i++)
      samples[i] = makeSinusoidalSamples(size, size, i);
    return samples;
  }
  
  public ImageAnimationTest(int num, int size) throws VisADException, RemoteException {
    
    // (Time -> ((x, y) -> val))
    RealTupleType pxlA = new RealTupleType(RealType.getRealType("x_a"), RealType.getRealType("y_a"));
    //-Linear2DSet imgSet = new Linear2DSet(pxl, 0, size-1, size, 0, size-1, size); 
    Linear2DSet set = new Linear2DSet(RealTupleType.Generic2D, 0, size-1, size, 0, size-1, size); 
    float[][] locs = set.getSamples();
    Gridded2DSet locs_set = new Gridded2DSet(pxlA, locs, size, size); 
    GridCoordinateSystem gcs = new GridCoordinateSystem(locs_set);


    //-RealTupleType pxl = new RealTupleType(RealType.getRealType("x"), RealType.getRealType("y"));
    RealTupleType pxl = new RealTupleType(RealType.getRealType("x"), RealType.getRealType("y"), gcs, null);
    Linear2DSet imgSet = new Linear2DSet(pxl, 0, size-1, size, 0, size-1, size); 
    FunctionType pxlVal = new FunctionType(pxl, RealType.getRealType("val"));
    FunctionType timePxlVal = new FunctionType(RealType.Time, pxlVal);
    


    Field timeFld = new FieldImpl(timePxlVal, new Integer1DSet(RealType.Time, num));
    
    float[][] samples = makeSamples(size, num);
    
    for (int i = 0; i < samples.length; i++) {
      FlatField imgFld = new FlatField(pxlVal, imgSet);

      imgFld.setSamples(new float[][]{samples[i]}, false);
      
      // set image for time
      timeFld.setSample(i, imgFld, false);
    }
    System.out.println("makeSamples done");
    
    DataReference ref = new DataReferenceImpl("image");
    ref.setData(timeFld);
    
    DisplayImpl display = new DisplayImplJ3D("image display");
    imgRend = new ImageRendererJ3D();
    System.err.println("Renderer:"+imgRend.toString());

    //-display.addMap(new ScalarMap(RealType.getRealType("x"), Display.XAxis));
    //-display.addMap(new ScalarMap(RealType.getRealType("y"), Display.YAxis));
    display.addMap(new ScalarMap(RealType.getRealType("x_a"), Display.XAxis));
    display.addMap(new ScalarMap(RealType.getRealType("y_a"), Display.YAxis));
    display.addMap(new ScalarMap(RealType.getRealType("val"), Display.RGB));
    
    ScalarMap aniMap = new ScalarMap(RealType.Time, Display.Animation);
    display.addMap(aniMap);
    AnimationControl aniCtrl = (AnimationControl) aniMap.getControl();
    aniCtrl.setStep(500);
    aniCtrl.setOn(true);
    display.addReferences(imgRend, ref);
    
    setLayout(new BorderLayout());
    add(display.getComponent());
    
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    //-BranchGroup scene = ((DisplayRendererJ3D) imgRend.getDisplayRenderer()).getRoot();
    //-Util.printSceneGraph(scene);
  }

  public static void main(String[] args) throws RemoteException, VisADException {
    
    int num = 0;
    int size = 0;
    try {
      num = Integer.parseInt(args[0]);
      size = Integer.parseInt(args[1]);
    } catch (Exception e) {
      System.err.println("USAGE: ImageAnimationTest <numImgs> <size>");
      System.exit(1);
    }
    
    Util.printJ3DProperties(null);
    
    JFrame frame = new JFrame("Image Animation Test");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setLayout(new BorderLayout());
    final ImageAnimationTest aniTest = new ImageAnimationTest(num, size);
    frame.add(aniTest);
    frame.setSize(600, 600);
    frame.setVisible(true);

  }
}
