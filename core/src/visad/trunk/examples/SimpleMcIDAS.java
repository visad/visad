import java.awt.event.*;
import javax.swing.*;
import java.io.IOException;
import java.rmi.RemoteException;
import visad.*;
import visad.util.*;
import visad.data.mcidas.*;
import visad.java3d.*;
import visad.bom.*;

public class SimpleMcIDAS {

  // run 'java SimpleMcIDAS AREA2001 OUTLSUPW'
  public static void main (String[] args)
         throws VisADException, RemoteException, IOException {

    DisplayImpl display = new DisplayImplJ3D("display");
    display.addMap(new ScalarMap(RealType.Latitude, Display.Latitude));
    display.addMap(new ScalarMap(RealType.Longitude, Display.Longitude));

    AreaAdapter areaAdapter = new AreaAdapter(args[0]);
    FlatField image = areaAdapter.getData();

    FunctionType imageFunctionType = (FunctionType) image.getType();
    RealTupleType imageRangeType = 
      (RealTupleType) imageFunctionType.getRange();

    ScalarMap rgbMap =
      new ScalarMap((RealType) imageRangeType.getComponent(0), Display.RGB);
    display.addMap(rgbMap);

    DataReferenceImpl imageRef = new DataReferenceImpl("ImageRef");
    imageRef.setData(image);

    BaseMapAdapter baseMapAdapter = new BaseMapAdapter(args[1]);
    Data map = baseMapAdapter.getData();
    DataReference maplinesRef = new DataReferenceImpl("MapLines");
    maplinesRef.setData(map);
    ConstantMap[] maplinesConstantMap = new ConstantMap[]
      {new ConstantMap(1.001, Display.Radius),
       new ConstantMap(0.0, Display.Blue)};

    display.addReference(maplinesRef, maplinesConstantMap);
    display.addReferences(new ImageRendererJ3D(), imageRef);

    JFrame frame = new JFrame("Satellite Display");
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });

    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.add(display.getComponent());
    panel.add(new LabeledColorWidget(new ColorMapWidget(rgbMap, false)));
    frame.getContentPane().add(panel);
    frame.setSize(500, 700);
    frame.setVisible(true);
  }
}

