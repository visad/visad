
//
// UniverseBuilderJ3D.java
//

/*
   copied from Sun's Java 3D API Specification. version 1.0
*/

package visad.java3d;

import java.lang.reflect.Method;

import javax.media.j3d.*;
import javax.vecmath.*;

public class UniverseBuilderJ3D extends Object {

    // User-specified canvas
    private Canvas3D canvas;

    // Scene graph elements that the user may want access to
    private VirtualUniverse universe;
    private Locale locale;
    TransformGroup vpTrans;
    View view;
    private BranchGroup vpRoot;
    private ViewPlatform vp;

    /**
     * The {@link VirtualUniverse} method that releases all allocated resources.
     * This method has been available since Java 3D 1.2.
     */
    private static final Method  REMOVE_ALL_LOCALES;
    private static final Method  REMOVE_ALL_CANVAS3DS;
    private static final Class[] NIL_CLASS_ARRAY;

    static {
      NIL_CLASS_ARRAY = new Class[0];
      Method method = null;
      try {
        method = Class.forName("javax.media.j3d.VirtualUniverse")
          .getMethod("removeAllLocales", NIL_CLASS_ARRAY);
      }
      catch (Exception ex) {      }
      REMOVE_ALL_LOCALES = method;
      method = null;
      try {
        method = Class.forName("javax.media.j3d.View")
          .getMethod("removeAllCanvas3Ds", NIL_CLASS_ARRAY);
      }
      catch (Exception ex) {      }
      REMOVE_ALL_CANVAS3DS = method;
    }

    public UniverseBuilderJ3D(Canvas3D c) {
      canvas = c;

      // Establish a virtual universe, with a single hi-res Locale
      universe = new VirtualUniverse();
      locale = new Locale(universe);

      // Create a PhysicalBody and Physical Environment object
      PhysicalBody body = new PhysicalBody();
      PhysicalEnvironment environment = new PhysicalEnvironment();

      // Create a View and attach the Canvas3D and the physical
      // body and environment to the view.
      view = new View();
      view.addCanvas3D(c);
      view.setPhysicalBody(body);
      view.setPhysicalEnvironment(environment);

      // Create a branch group node for the view platform
      vpRoot = new BranchGroup();
      vpRoot.setCapability(BranchGroup.ALLOW_DETACH);
      vpRoot.setCapability(Group.ALLOW_CHILDREN_READ);

      // Create a ViewPlatform object, and its associated
      // TransformGroup object, and attach it to the root of the
      // subgraph.  Attach the view to the view platform.
      Transform3D t = new Transform3D();
      t.set(new Vector3f(0.0f, 0.0f, 2.0f));
      vp = new ViewPlatform();
      vpTrans = new TransformGroup(t);
      vpTrans.setCapability(Group.ALLOW_CHILDREN_READ);
      vpTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
      vpTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

      vpTrans.addChild(vp);
      vpRoot.addChild(vpTrans);

      view.attachViewPlatform(vp);

      // Attach the branch graph to the universe, via the Locale.
      // The scene graph is now live!
      locale.addBranchGraph(vpRoot);
    }

    public void addBranchGraph(BranchGroup bg) {
      if (locale != null) locale.addBranchGraph(bg);
    }

    public void destroy() {
      view.removeCanvas3D(canvas);
      // according to Kelvin Chung, 26 Apr 2000, this should work
      // but it throws a NullPointerException
      // view.attachViewPlatform(null);
      if (REMOVE_ALL_CANVAS3DS != null) {
        try {
          REMOVE_ALL_CANVAS3DS.invoke(view, NIL_CLASS_ARRAY);
        }
        catch (Exception ex) {
            throw new RuntimeException("Assertion failure: " + ex);
        }
      }
      // in Java3D 1.3.1
      // Viewer.setViewingPlatform(null);
      if (REMOVE_ALL_LOCALES != null) {
        try {
          REMOVE_ALL_LOCALES.invoke(universe, NIL_CLASS_ARRAY);
        }
        catch (Exception ex) {
            throw new RuntimeException("Assertion failure: " + ex);
        }
      }
      // in Java3D 1.3.1
      // Viewer.clearViewerMap();

      canvas = null;
      universe = null;
      locale = null;
      vpTrans = null;
      view = null;
      vpRoot = null;
      vp = null;
    }
}

