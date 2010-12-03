package visad.util;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;
import java.util.Enumeration;

import javax.media.j3d.Bounds;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.Geometry;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.Group;
import javax.media.j3d.Node;
import javax.media.j3d.OrderedGroup;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Switch;
import javax.media.j3d.Text3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.View;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import visad.java3d.DisplayRendererJ3D;

public class SceneGraphInspector extends JPanel implements TreeSelectionListener {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public static void show(DisplayRendererJ3D renderer) {
    
    JFrame frame = new JFrame("VisAD SceneGraph Inspector");
    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    frame.add(new SceneGraphInspector(renderer));
    frame.setSize(800, 480);
    frame.setVisible(true);
    
  }
  
  private static String makeName(Object obj) {
    String name = obj.getClass().getSimpleName();

    if (obj instanceof Node) {
      Node node = (Node) obj;
      String nodeName = Util.getName(node);
      if (nodeName != null && nodeName.length() > 0) {
        name = nodeName;
      }
    }
    else if (obj instanceof String) {
      return (String) obj;
    }
      
    return name + "@" + obj.hashCode();
  }

  class MyNode<T> extends DefaultMutableTreeNode {
    
    private static final long serialVersionUID = 1L;
    private T node;
    
    public MyNode(T node) {
      this.node = node;
    }
    
    public T getNode() {
      return node;
    }
    
    public String getName() {
      return makeName(node);
    }
    
    public String toString() {
      return getName();
    }
    
  }
  
  private JTree tree;
  private JPanel cards;
  
  public SceneGraphInspector(DisplayRendererJ3D renderer) {
    
    cards = new JPanel();
    cards.setLayout(new CardLayout());
    
    MyNode<String> top = new MyNode<String>("ROOT");

    View view = renderer.getView();
    MyNode<View> viewNode = new MyNode<View>(view);
    createViewNodes(viewNode);
    top.add(viewNode);
    
    MyNode<Node> scene = new MyNode<Node>(renderer.getRoot());
    createSceneNodes(scene);
    top.add(scene);
  
    tree = new JTree(top);
    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    tree.addTreeSelectionListener(this);
    
    Dimension minimumSize = new Dimension(300, 200);
    JScrollPane treeView = new JScrollPane(tree);
    treeView.setMinimumSize(minimumSize);
    
    
    JSplitPane split = new JSplitPane(
        JSplitPane.HORIZONTAL_SPLIT,
        treeView, 
        cards
    );
    setLayout(new BorderLayout());
    add(split, BorderLayout.CENTER);
    
    for (int i = 0; i < tree.getRowCount(); i++) {
      tree.expandRow(i);
    }
  }
  
  private void createViewNodes(MyNode<View> viewNode) {
    
    cards.add(makeViewComponent(viewNode.getNode()), viewNode.getName());
    Enumeration<Canvas3D> canvases = viewNode.getNode().getAllCanvas3Ds();
    while (canvases.hasMoreElements()) {
      Canvas3D canvas = canvases.nextElement();
      MyNode<Canvas3D> canvasNode = new MyNode<Canvas3D>(canvas);
      viewNode.add(canvasNode);
      cards.add(makeCanvasComponent(canvas), canvasNode.getName());
    }
  }
  
  private void createSceneNodes(MyNode<Node> scene) {
    
    Node node = scene.getNode();
    cards.add(makeNodeComponent(node), scene.getName());
    if (node instanceof Group) {
      Group group = (Group) node;
      Enumeration<Node> children = group.getAllChildren();
      while (children.hasMoreElements()) {
        MyNode<Node> tnode = new MyNode<Node>(children.nextElement());
        scene.add(tnode);
        createSceneNodes(tnode);
      }
    }
    
  }
  
  private JPanel makeViewComponent(final View view) {
    
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.add(new JLabel("Class: " + view.getClass().getName()));
    panel.add(new JLabel("AntiAliasing: " + view.getSceneAntialiasingEnable()));
    int x = view.getViewPolicy();
    panel.add(new JLabel("ViewPolicy: " + (x == View.HMD_VIEW ? "HMD_VIEW" : "SCREEN_VIEW")));
    String visPolicy = "";
    switch (view.getVisibilityPolicy()) {
    case View.VISIBILITY_DRAW_ALL: 
      visPolicy = "VISIBILITY_DRAW_ALL";
      break;
    case View.VISIBILITY_DRAW_INVISIBLE:
      visPolicy = "VISIBILITY_DRAW_INVISIBLE";
      break;
    case View.VISIBILITY_DRAW_VISIBLE:
      visPolicy = "VISIBILITY_DRAW_VISIBLE";
      break;
    }
    panel.add(new JLabel("VisibilityPolicy: " + visPolicy));
    panel.add(new JLabel("FrameNumber: " + view.getFrameNumber()));
    panel.add(new JLabel("ViewRunning: " + view.isViewRunning()));
    panel.add(new JLabel("BehaviorSchedulerRunning: " + view.isBehaviorSchedulerRunning()));
    
    final JButton startView = new JButton("Start View");
    startView.setEnabled(!view.isViewRunning());
    final JButton stopView = new JButton("Stop View");
    startView.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        view.startView();
        startView.setEnabled(!view.isViewRunning());
        stopView.setEnabled(view.isViewRunning());
      }
    });
    stopView.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        view.stopView();
        startView.setEnabled(!view.isViewRunning());
        stopView.setEnabled(view.isViewRunning());
      }
    });
    JPanel subPanel = new JPanel();
    subPanel.add(startView);
    subPanel.add(stopView);
    panel.add(subPanel);
    
    final JButton startBehav = new JButton("Start BehaviorScheduler");
    startView.setEnabled(!view.isViewRunning());
    final JButton stopBehav = new JButton("Stop BehaviorScheduler");
    startView.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        view.startBehaviorScheduler();
        stopBehav.setEnabled(!view.isViewRunning());
        startBehav.setEnabled(view.isViewRunning());
      }
    });
    stopView.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        view.stopBehaviorScheduler();
        stopBehav.setEnabled(!view.isViewRunning());
        startBehav.setEnabled(view.isViewRunning());
      }
    });
    subPanel = new JPanel();
    subPanel.add(startView);
    subPanel.add(stopView);
    panel.add(subPanel);
    
    return panel;
  }
  
  private JPanel makeCanvasComponent(final Canvas3D canvas) {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.add(new JLabel("Class: " + canvas.getClass().getName()));
    panel.add(new JLabel("Name: " + canvas.getName()));
   
    panel.add(new JLabel("DoubleBufferEnabled: " + canvas.getDoubleBufferEnable()));
    panel.add(new JLabel("Height: " + canvas.getHeight()));
    panel.add(new JLabel("Width: " + canvas.getWidth()));
    panel.add(new JLabel("IgnoreRepaint: " + canvas.getIgnoreRepaint()));
    panel.add(new JLabel("MousePosition: " + canvas.getMousePosition()));
   
    final JButton startRend = new JButton("Start Renderer");
    startRend.setEnabled(!canvas.isRendererRunning());
    final JButton stopRend = new JButton("Stop Renderer");
    startRend.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        canvas.startRenderer();
        startRend.setEnabled(!canvas.isRendererRunning());
        stopRend.setEnabled(canvas.isRendererRunning());
      }
    });
    stopRend.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        canvas.stopRenderer();
        startRend.setEnabled(!canvas.isRendererRunning());
        stopRend.setEnabled(canvas.isRendererRunning());
      }
    });
    JPanel subPanel = new JPanel();
    subPanel.add(startRend);
    subPanel.add(stopRend);
    panel.add(subPanel);
    
    return panel;
  }
  
  private JPanel makeNodeComponent(final Node node) {
    
    final Group parent = (Group) node.getParent();
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    
    panel.add(new JLabel("Name: " + makeName(node)));
    panel.add(new JLabel("Class: " + node.getClass().getName()));
    panel.add(new JLabel("Live: " + node.isLive()));
    panel.add(new JLabel("Compiled: " + node.isCompiled()));
    panel.add(new JLabel("UserData: " + node.getUserData()));
    panel.add(new JLabel("Pickable: " + node.getPickable()));
    panel.add(new JLabel("BoundsAutoCompute: " + node.getBoundsAutoCompute()));
    Bounds bounds = node.getBounds();
    if (bounds != null) {
      panel.add(new JLabel("Bounds.isEmpty: " + bounds.isEmpty()));
    }
    
    if (node instanceof Group) {
      Group group = (Group) node;
      panel.add(new JLabel("NumChildren: " + group.numChildren()));
      
      if ((node instanceof BranchGroup) && parent != null &&
          group.getCapability(BranchGroup.ALLOW_DETACH)) {
        final JToggleButton button = new JToggleButton("Detach");
        button.setSelected(true);
        button.addItemListener(new ItemListener(){
          public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
              parent.addChild(node);
              button.setText("Detach");
            } else if (e.getStateChange() == ItemEvent.DESELECTED) {
              ((BranchGroup) node).detach(); 
              button.setText("Attach");
            }
          }
        });
        panel.add(button);
      } else if (node instanceof OrderedGroup) {
        OrderedGroup ogroup = (OrderedGroup) node;
        panel.add(new JLabel("Order: " + Arrays.toString(ogroup.getChildIndexOrder())));
        
      } else if (node instanceof TransformGroup) {
        TransformGroup tgroup = (TransformGroup) node;
        Transform3D trans = new Transform3D();
        tgroup.getTransform(trans);
        panel.add(new JLabel("TransformMatrix: " + trans.toString()));
      }
    
      if (node instanceof Switch) {
        final Switch swich = (Switch) node;
        String[] indexes = new String[swich.numChildren() + 1];
        indexes[0] = "None";
        for (int i = 1; i < indexes.length; i++) {
          indexes[i] = "" + (i - 1);
        }
        JComboBox comboBox = new JComboBox(indexes);
        int selected = swich.getWhichChild();
        if (selected == Switch.CHILD_NONE) {
          selected = 0;
        }
        comboBox.setSelectedIndex(swich.getWhichChild() + 1);
        comboBox.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
             JComboBox cBox = (JComboBox) e.getSource();
             int selected = cBox.getSelectedIndex();
             if (selected == 0) {
               selected = Switch.CHILD_NONE;
             } else {
               selected -= 1;
             }
             swich.setWhichChild(selected);
          }
        });
        panel.add(comboBox);
      }
      
    } else {
      
      if (node instanceof Shape3D) {
        Shape3D shape = (Shape3D) node;
        Geometry geo = shape.getGeometry();
        if (geo != null) {
          panel.add(new JLabel("Geometry: " + geo.getClass().getSimpleName()));
          if (geo instanceof GeometryArray) {
            GeometryArray arr = (GeometryArray) geo;
            panel.add(new JLabel("VertexCount: " + arr.getVertexCount()));
          } else if (geo instanceof Text3D) {
            Text3D txt = (Text3D) geo;
            panel.add(new JLabel("String: \"" + txt.getString()+ "\""));
          }
        }
      }
    }
    
    return panel;
  }

  @Override
  public void valueChanged(TreeSelectionEvent evt) {
    
    MyNode tnode = (MyNode) evt.getPath().getLastPathComponent();
    Object node = tnode.getNode();
    CardLayout cl = (CardLayout) cards.getLayout();
    cl.show(cards, tnode.getName());
  }
  
}
