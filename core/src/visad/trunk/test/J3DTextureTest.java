package visad.test;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import javax.media.j3d.Appearance;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.Geometry;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.Group;
import javax.media.j3d.ImageComponent;
import javax.media.j3d.ImageComponent2D;
import javax.media.j3d.LineAttributes;
import javax.media.j3d.PointAttributes;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.QuadArray;
import javax.media.j3d.RenderingAttributes;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Switch;
import javax.media.j3d.Texture;
import javax.media.j3d.Texture2D;
import javax.media.j3d.TextureAttributes;
import javax.swing.JFrame;
import javax.vecmath.Point3d;

import visad.util.Util;

import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;
import com.sun.j3d.utils.universe.SimpleUniverse;
import com.sun.j3d.utils.universe.ViewingPlatform;

/**
 * Creates 2 textures, adds them to a switch, and changes the switch's active
 * child on a timer. This is a Java3D implementation without any VisAD classes.
 */
public class J3DTextureTest extends Canvas3D {

  public static final float BACK2D = -0.01f;
  
  private BranchGroup scene;
  private Switch swit;
  
  public J3DTextureTest(int size) throws IOException {
    
    super(SimpleUniverse.getPreferredConfiguration());
    createSceneGraph(size);
    SimpleUniverse uverse = new SimpleUniverse(this);
    ViewingPlatform vp = uverse.getViewingPlatform();
    vp.setNominalViewingTransform();
    OrbitBehavior orbit = new OrbitBehavior(uverse.getCanvas());
    BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 1.0);
    orbit.setSchedulingBounds(bounds);
    vp.setViewPlatformBehavior(orbit);
    uverse.addBranchGraph(scene);
    
    Timer timer = new Timer();
    timer.scheduleAtFixedRate(new TimerTask() {
      int i = 0;
      public void run() {
        int idx = i++ % 3;
        if (idx < 2) {
          swit.setWhichChild(idx);
          System.err.println("Switched to child " + idx);
        } else {
          swit.setWhichChild(Switch.CHILD_NONE);
          System.err.println("Switched to NONE");
        }
        
      }
    }, 0, 2000);
  }
  
  private void setGeometryCapabilities(GeometryArray array) {
    array.setCapability(GeometryArray.ALLOW_COLOR_READ);
    array.setCapability(GeometryArray.ALLOW_COORDINATE_READ);
    array.setCapability(GeometryArray.ALLOW_COUNT_READ);
    array.setCapability(GeometryArray.ALLOW_FORMAT_READ);
    array.setCapability(GeometryArray.ALLOW_NORMAL_READ);
    array.setCapability(GeometryArray.ALLOW_TEXCOORD_READ);
  }
  
  private Appearance makeAppearance(Geometry geometry) {

    Appearance appearance = new Appearance();
    appearance.setCapability(Appearance.ALLOW_COLORING_ATTRIBUTES_READ);
    appearance.setCapability(Appearance.ALLOW_LINE_ATTRIBUTES_READ);
    appearance.setCapability(Appearance.ALLOW_MATERIAL_READ);
    appearance.setCapability(Appearance.ALLOW_POINT_ATTRIBUTES_READ);
    appearance.setCapability(Appearance.ALLOW_POLYGON_ATTRIBUTES_READ);
    appearance.setCapability(Appearance.ALLOW_RENDERING_ATTRIBUTES_READ);
    appearance.setCapability(Appearance.ALLOW_TEXGEN_READ);
    appearance.setCapability(Appearance.ALLOW_TEXTURE_ATTRIBUTES_READ);
    appearance.setCapability(Appearance.ALLOW_TEXTURE_READ);
    // appearance.setCapability(Appearance.ALLOW_TEXTURE_UNIT_STATE_READ);
    appearance.setCapability(Appearance.ALLOW_TRANSPARENCY_ATTRIBUTES_READ);

    LineAttributes line = new LineAttributes();
    line.setCapability(LineAttributes.ALLOW_ANTIALIASING_READ);
    line.setCapability(LineAttributes.ALLOW_PATTERN_READ);
    line.setCapability(LineAttributes.ALLOW_WIDTH_READ);
    int pattern = LineAttributes.PATTERN_SOLID;
    line.setLinePattern(pattern);
    appearance.setLineAttributes(line);

    PointAttributes point = new PointAttributes();
    point.setCapability(PointAttributes.ALLOW_ANTIALIASING_READ);
    point.setCapability(PointAttributes.ALLOW_SIZE_READ);
    appearance.setPointAttributes(point);

    PolygonAttributes polygon = new PolygonAttributes();
    polygon.setCapability(PolygonAttributes.ALLOW_CULL_FACE_READ);
    polygon.setCapability(PolygonAttributes.ALLOW_MODE_READ);
    polygon.setCapability(PolygonAttributes.ALLOW_NORMAL_FLIP_READ);
    polygon.setCapability(PolygonAttributes.ALLOW_OFFSET_READ);
    polygon.setCullFace(PolygonAttributes.CULL_NONE);

    appearance.setPolygonAttributes(polygon);

    RenderingAttributes rendering = new RenderingAttributes();
    rendering.setCapability(RenderingAttributes.ALLOW_ALPHA_TEST_FUNCTION_READ);
    rendering.setCapability(RenderingAttributes.ALLOW_ALPHA_TEST_VALUE_READ);
    rendering.setCapability(RenderingAttributes.ALLOW_DEPTH_ENABLE_READ);

    rendering.setDepthBufferEnable(true);
    appearance.setRenderingAttributes(rendering);

    return appearance;
  }
  
  private void createSceneGraph(int size) throws IOException {
    
    scene = new BranchGroup();
    scene.setCapability(Group.ALLOW_CHILDREN_WRITE);
    
    swit = new Switch();
    swit.setCapability(Switch.ALLOW_SWITCH_READ);
    swit.setCapability(Switch.ALLOW_SWITCH_WRITE);
    swit.setCapability(BranchGroup.ALLOW_DETACH);
    swit.setCapability(Group.ALLOW_CHILDREN_READ);
    swit.setCapability(Group.ALLOW_CHILDREN_WRITE);
    swit.setCapability(Switch.ALLOW_CHILDREN_WRITE);
    
    textureToGroup(swit, makeGeometryArray(), makeImage(255,0,0,255, size, size));
    textureToGroup(swit, makeGeometryArray(), makeImage(0,0,255,255, size, size));

    scene.addChild(swit);
    scene.compile();
  }
  
  private GeometryArray makeGeometryArray() {

    float width = .5f;
    float height = .5f;
    
    float[] grid = new float[]{
      -width, -height, 0f,
       width, -height, 0f,
       width,  height, 0f,
      -width,  height, 0f
    };
    
    float[] texCoord = new float[] {
       -width, -height,
        width, -height,
        width,  height,
       -width,  height
    };
    
    QuadArray array = new QuadArray(4, 
        GeometryArray.BY_REFERENCE | 
        GeometryArray.COORDINATES | 
        GeometryArray.TEXTURE_COORDINATE_2);
    setGeometryCapabilities(array);
    array.setCoordRefFloat(grid);
    array.setTexCoordRefFloat(0, texCoord);

    return array;
  }
  
  private BufferedImage makeImage(int r, int b, int g, int a, int width, int height) {
    System.err.println(String.format("Image color: RGBA(%s,%s,%s,%s)", r,g,b,a));
    
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
    
    int pxl = 0;
    pxl = 255 << 24;
    pxl += r << 16;
    pxl += g << 8;
    pxl += b;
    
    int[] pxls = new int[image.getWidth()*image.getHeight()];
    for (int i=0; i<pxls.length; i++) {
      pxls[i] = pxl;
    }
    
    image.setRGB(0, 0, image.getWidth(), image.getHeight(), pxls, 0, width);
    
    return image;
  }
  
  public void textureToGroup(Group group, GeometryArray geometry, BufferedImage image) {
    
    Appearance appearance = makeAppearance(geometry);
    TextureAttributes texture_attributes = new TextureAttributes();

    texture_attributes.setTextureMode(TextureAttributes.REPLACE);

    texture_attributes.setPerspectiveCorrectionMode(TextureAttributes.NICEST);
    appearance.setTextureAttributes(texture_attributes);

    Texture2D texture = new Texture2D(Texture.BASE_LEVEL, Texture.RGBA, image.getWidth(), image.getHeight());
    
    System.err.println(String.format("Image width:%s height:%s", image.getWidth(), image.getHeight()));
    System.err.println(String.format("Texture width:%s height:%s", texture.getWidth(), texture.getHeight()));
    System.err.println();
    
    texture.setCapability(Texture.ALLOW_IMAGE_READ);
    ImageComponent2D image2d = new ImageComponent2D(ImageComponent.FORMAT_RGBA, image);
    image2d.setCapability(ImageComponent.ALLOW_IMAGE_READ);
    texture.setImage(0, image2d);

    texture.setMinFilter(Texture.BASE_LEVEL_POINT);
    texture.setMagFilter(Texture.BASE_LEVEL_POINT);
    texture.setEnable(true);

    Shape3D shape = new Shape3D(geometry, appearance);
    shape.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
    shape.setCapability(Shape3D.ALLOW_APPEARANCE_READ);
    appearance.setTexture(texture);
    appearance.setCapability(Appearance.ALLOW_TEXTURE_READ);

//    group.addChild(shape);
    BranchGroup branch = new BranchGroup();
    branch.setCapability(BranchGroup.ALLOW_DETACH);
    branch.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
    branch.addChild(shape);
    if (group.numChildren() > 0) {
      group.insertChild(branch, 0);
    } else {
      group.addChild(branch);
    }
  }
  
  public static void main(String[] args) throws IOException {
    
    int size = 0;
    try {
      size = Integer.parseInt(args[0]);
    } catch (Exception e) {
      System.err.println("You must provide an image size");
      System.exit(1);
    }
    
    JFrame frame = new JFrame("Texture Test Canvas");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setLayout(new BorderLayout());
    
    J3DTextureTest canvas = new J3DTextureTest(size);
    Util.printJ3DProperties(canvas);
    frame.add(canvas);
    frame.setSize(800, 600);
    frame.setVisible(true);
  }
}
