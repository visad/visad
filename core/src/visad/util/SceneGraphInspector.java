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
import javax.swing.BoxLayout;
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

public class SceneGraphInspector extends JPanel implements TreeSelectionListener {

  public static void show(Group group) {
    
    JFrame frame = new JFrame("VisAD SceneGraph Inspector");
    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    frame.add(new SceneGraphInspector(group));
    frame.setSize(800, 480);
    frame.setVisible(true);
    
  }
  
  private static String makeName(Node node) {
    String name = node.getName();
    if (name == null || name.length() == 0) {
      name = node.getClass().getSimpleName() + "@" + node.hashCode();
    }
    return name;
  }

  class J3DTreeNode extends DefaultMutableTreeNode {
    
    private Node node;
    
    public J3DTreeNode(Node node) {
      this.node = node;
    }
    
    public Node getNode() {
      return node;
    }
    
    public String toString() {
      return makeName(node);
    }
    
  }
  
  private JTree tree;
  private JPanel cards;
  
  public SceneGraphInspector(Group group) {
    
    cards = new JPanel();
    cards.setLayout(new CardLayout());
    
    J3DTreeNode top = new J3DTreeNode(group);

    createNodes(top);
    
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
  
  private void createNodes(J3DTreeNode top) {
    
    Node node = top.getNode();

    cards.add(makeNodeComponent(node), makeName(node));
    if (node instanceof Group) {
      Group group = (Group) node;
      Enumeration<Node> children = group.getAllChildren();
      while (children.hasMoreElements()) {
        J3DTreeNode tnode = new J3DTreeNode(children.nextElement());
        top.add(tnode);
        createNodes(tnode);
      }
    }
    
  }
  
  private JPanel makeNodeComponent(final Node node) {
    
    final Group parent = (Group) node.getParent();
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    
    panel.add(new JLabel("Name: " + makeName(node)));
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
    
    J3DTreeNode tnode = (J3DTreeNode) evt.getPath().getLastPathComponent();
    Node node = tnode.getNode();
    CardLayout cl = (CardLayout)(cards.getLayout());
    cl.show(cards, makeName(node));
  }
  
}
