import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
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

    // construct a 3-D display
    DisplayImpl display = new DisplayImplJ3D("display");

    // read McIDAS AREA file
    AreaAdapter areaAdapter = new AreaAdapter(args[0]);
    Data image = areaAdapter.getData();

    // get type of image radiance
    FunctionType imageFunctionType = (FunctionType) image.getType();
    RealType radianceType = (RealType)
      ((RealTupleType) imageFunctionType.getRange()).getComponent(0);

    // define display coordinates
    display.addMap(new ScalarMap(RealType.Latitude, Display.Latitude));
    display.addMap(new ScalarMap(RealType.Longitude, Display.Longitude));
    ScalarMap rgbMap = new ScalarMap(radianceType, Display.RGB);
    display.addMap(rgbMap);

    // read McIDAS map file
    BaseMapAdapter baseMapAdapter = new BaseMapAdapter(args[1]);
    Data map = baseMapAdapter.getData();

    // link map to display
    DataReference maplinesRef = new DataReferenceImpl("MapLines");
    maplinesRef.setData(map);
    ConstantMap[] maplinesConstantMap = new ConstantMap[]
      {new ConstantMap(1.002, Display.Radius),
       new ConstantMap(0.0, Display.Blue)};
    display.addReference(maplinesRef, maplinesConstantMap);

    // link image to display
    DataReferenceImpl imageRef = new DataReferenceImpl("ImageRef");
    imageRef.setData(image);
    display.addReferences(new ImageRendererJ3D(), imageRef);

    // create frame (window) on screen
    JFrame frame = new JFrame("Satellite Display");
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });

    // add 3-D display to panel in frame
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.add(display.getComponent());

    // construct color widget for image radiances
    LabeledColorWidget lcw =
      new LabeledColorWidget(new ColorMapWidget(rgbMap, false));

    // add color widget to panel in frame
    JPanel sub_panel = new JPanel();
    sub_panel.setMaximumSize(new Dimension(500, 150));
    sub_panel.setBorder(new CompoundBorder(new EtchedBorder(),
                        new EmptyBorder(5, 5, 5, 5)));
    sub_panel.add(lcw);
    panel.add(sub_panel);

    // finish off frame
    frame.getContentPane().add(panel);
    frame.setSize(500, 700);
    frame.setVisible(true);

    // and now exit - leaving display, color widget,
    // map and image to take care of themselves
  }
}

