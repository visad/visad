
//
// UniverseBuilderJ3D.java
//

/*
   copied from Sun's Java 3D API Specification. version 1.0
*/

package visad.java3d;

import visad.*;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.event.*;

import javax.media.j3d.*;
import javax.vecmath.*;

public class UniverseBuilderJ3D extends Object {

    // User-specified canvas
    Canvas3D canvas;

    // Scene graph elements that the user may want access to
    VirtualUniverse         universe;
    Locale                  locale;
    TransformGroup          vpTrans;
    View                    view;

    public UniverseBuilderJ3D(Canvas3D c) {
        this.canvas = c;

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
        BranchGroup vpRoot = new BranchGroup();

        // Create a ViewPlatform object, and its associated
        // TransformGroup object, and attach it to the root of the
        // subgraph.  Attach the view to the view platform.
        Transform3D t = new Transform3D();
        t.set(new Vector3f(0.0f, 0.0f, 2.0f));
        ViewPlatform vp = new ViewPlatform();
        vpTrans = new TransformGroup(t);
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
        locale.addBranchGraph(bg);
    }

}
