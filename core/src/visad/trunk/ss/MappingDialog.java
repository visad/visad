
//
// MappingDialog.java
//

package visad.ss;

// AWT packages
import java.awt.*;
import java.awt.event.*;
import com.sun.java.swing.*;
import com.sun.java.swing.event.*;
import com.sun.java.swing.tree.*;

// RMI classes
import java.rmi.RemoteException;

// VisAD packages
import visad.*;

/** MappingDialog is a dialog that lets the user create ScalarMaps. */
public class MappingDialog extends JDialog implements ActionListener,
                                               ListSelectionListener {
  // needed in TreeSelectionListener's valueChanged method
  JPanel CurMapsPanel = new JPanel();
  JScrollPane CurMapsView;
  JLabel[] MapLabels = new JLabel[FancySSCell.NumMaps];
  boolean[] MapOnList = new boolean[FancySSCell.NumMaps];
  VisADNode NoneNode = new VisADNode("None", null);
  JList VisadMapList = new JList(FancySSCell.MapList);

  // needed in ListSelectionListener's valueChanged method
  JTree MathTree;

  // needed in SpreadSheet's code after displaying MappingDialog
  public boolean Confirm = false;
  VisADNode[] DisplayMaps = new VisADNode[FancySSCell.NumMaps];

  /** This is the constructor for MappingDialog. */
  MappingDialog(Frame parent, Data data, String treeRootTitle) {
    super(parent, "Set up data mappings", true);
    setBackground(Color.white);
    Dimension zero = new Dimension(0, 0);
    for (int i=0; i<FancySSCell.NumMaps; i++) MapOnList[i] = false;

    // set up content pane
    JPanel dialogPane = new JPanel();
    setContentPane(dialogPane);
    dialogPane.setAlignmentY(JPanel.TOP_ALIGNMENT);
    dialogPane.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    dialogPane.setLayout(new BoxLayout(dialogPane, BoxLayout.Y_AXIS));

    // set up main panel
    JPanel mainPanel = new JPanel();
    mainPanel.setAlignmentY(JPanel.TOP_ALIGNMENT);
    mainPanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));
    dialogPane.add(Box.createVerticalStrut(5));
    dialogPane.add(mainPanel);

    // set up VisAD mappings list
    VisadMapList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    VisadMapList.clearSelection();
    VisadMapList.addListSelectionListener(this);
    for (int i=0; i<FancySSCell.NumMaps; i++) DisplayMaps[i] = NoneNode;
    JScrollPane visadMapListView = new JScrollPane(VisadMapList) {
      public Dimension getMaximumSize() {
        return new Dimension(150, super.getMaximumSize().height);
      }
    };
    visadMapListView.setAlignmentY(JScrollPane.TOP_ALIGNMENT);
    visadMapListView.setAlignmentX(JScrollPane.LEFT_ALIGNMENT);
    visadMapListView.setMinimumSize(zero);
    visadMapListView.setPreferredSize(new Dimension(150, 0));
    mainPanel.add(Box.createHorizontalStrut(5));
    mainPanel.add(visadMapListView);

    // set up right-hand panel
    JPanel rightPanel = new JPanel();
    rightPanel.setAlignmentY(JPanel.TOP_ALIGNMENT);
    rightPanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
    mainPanel.add(Box.createHorizontalStrut(5));
    mainPanel.add(rightPanel);
    mainPanel.add(Box.createHorizontalStrut(5));

    // set up MathType tree
    VisADNode dataNode = buildDataTree(data, treeRootTitle);
    VisADNode rootNode = new VisADNode("Map from:", null);
    rootNode.add(NoneNode);
    rootNode.add(dataNode);
    MathTree = new JTree(rootNode);
    MathTree.getSelectionModel().setSelectionMode(
                             TreeSelectionModel.SINGLE_TREE_SELECTION);
    MathTree.addTreeSelectionListener(new TreeSelectionListener() {
      public void valueChanged(TreeSelectionEvent e) {
        VisADNode node = (VisADNode) e.getPath().getLastPathComponent();
        int index = VisadMapList.getSelectedIndex();
        if (node.isLeaf()) {
          if (index >= 0 && DisplayMaps[index] != node) {
            DisplayMaps[index] = node;
            // update current map list box
            if (MapOnList[index]) CurMapsPanel.remove(MapLabels[index]);
            MapLabels[index] = new JLabel(new String((String)
                                          node.getUserObject()+" -> "
                                         +FancySSCell.MapList[index]));
            if (node == NoneNode) MapOnList[index] = false;
            else {
              CurMapsPanel.add(MapLabels[index]);
              MapOnList[index] = true;
            }
            CurMapsView.validate();
          }
        }
      }
    });
    JScrollPane treeView = new JScrollPane(MathTree);
    treeView.setAlignmentY(JScrollPane.TOP_ALIGNMENT);
    treeView.setAlignmentX(JScrollPane.LEFT_ALIGNMENT);
    treeView.setMinimumSize(zero);
    rightPanel.add(treeView);

    // expand entire tree
    for (DefaultMutableTreeNode n=rootNode.getFirstLeaf(); n!=null;
                                n=n.getNextLeaf()) {
      MathTree.expandPath(new TreePath(n.getPath()));
    }

    // set up current mappings panel
    CurMapsPanel.setAlignmentY(JPanel.TOP_ALIGNMENT);
    CurMapsPanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    CurMapsPanel.setBackground(Color.white);
    CurMapsPanel.setLayout(new BoxLayout(CurMapsPanel, BoxLayout.Y_AXIS));
    CurMapsView = new JScrollPane(CurMapsPanel) {
      public Dimension getMaximumSize() {
        return new Dimension(super.getMaximumSize().width, 100);
      }
    };
    CurMapsView.setAlignmentY(JScrollPane.TOP_ALIGNMENT);
    CurMapsView.setAlignmentX(JScrollPane.LEFT_ALIGNMENT);
    CurMapsView.setMinimumSize(zero);
    CurMapsView.setPreferredSize(new Dimension(0, 100));
    rightPanel.add(Box.createVerticalStrut(10));
    rightPanel.add(new JLabel("Current mappings"));
    rightPanel.add(CurMapsView);

    // set up JButtons
    JPanel buttons = new JPanel();
    buttons.setAlignmentY(JPanel.TOP_ALIGNMENT);
    buttons.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
    JButton done = new JButton("Done");
    done.setAlignmentY(JButton.TOP_ALIGNMENT);
    done.setAlignmentX(JButton.LEFT_ALIGNMENT);
    done.addActionListener(this);
    done.setActionCommand("done");
    JButton cancel = new JButton("Cancel");
    cancel.setAlignmentY(JButton.TOP_ALIGNMENT);
    cancel.setAlignmentX(JButton.LEFT_ALIGNMENT);
    cancel.addActionListener(this);
    cancel.setActionCommand("cancel");
    buttons.add(Box.createHorizontalGlue());
    buttons.add(done);
    buttons.add(Box.createHorizontalStrut(10));
    buttons.add(cancel);
    buttons.add(Box.createHorizontalGlue());
    dialogPane.add(Box.createVerticalStrut(10));
    dialogPane.add(buttons);
    dialogPane.add(Box.createVerticalStrut(10));
  }

  /** Handles button press events. */
  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
    if (cmd.equals("done")) {
      Confirm = true;
      setVisible(false);
    }
    else if (cmd.equals("cancel")) {
      setVisible(false);
    }
  }

  /** Handles list selection change events. */
  public void valueChanged(ListSelectionEvent e) {
    int index = e.getFirstIndex();
    if (index == -1) MathTree.clearSelection();
    else {
      VisADNode node = DisplayMaps[index];
      TreePath path = new TreePath(node.getPath());
      MathTree.setSelectionPath(path);
      MathTree.scrollPathToVisible(path);
    }
  }

  /** Recursively builds a JTree object from a Data object. */
  VisADNode buildDataTree(Data data, String rootName) {
    VisADNode rootNode = new VisADNode(rootName, null);
    MathType dataType;
    try {
      dataType = data.getType();
    }
    catch (RemoteException exc) {
      return null;
    }
    catch (VisADException exc) {
      return null;
    }

    if (dataType instanceof FunctionType) {
      addFunctionBranch((FunctionType) dataType, rootNode);
    }
    else if (dataType instanceof SetType) {
      addSetBranch((SetType) dataType, rootNode);
    }
    else if (dataType instanceof TupleType) {
      addTupleBranch((TupleType) dataType, rootNode);
    }
    else addScalarBranch((ScalarType) dataType, rootNode);

    return rootNode;
  }

  /** Used by buildDataTree to build a Function branch. */
  void addFunctionBranch(FunctionType mathType, VisADNode node) {
    VisADNode functionNode = new VisADNode("Function", null);
    node.add(functionNode);

    // extract domain
    VisADNode domainNode = new VisADNode("Domain", null);
    functionNode.add(domainNode);
    RealTupleType domain = mathType.getDomain();
    addTupleBranch((TupleType) domain, domainNode);

    // extract range
    VisADNode rangeNode = new VisADNode("Range", null);
    functionNode.add(rangeNode);
    MathType range = mathType.getRange();
    if (range instanceof FunctionType) {
      addFunctionBranch((FunctionType) range, rangeNode);
    }
    else if (range instanceof SetType) {
      addSetBranch((SetType) range, rangeNode);
    }
    else if (range instanceof TupleType) {
      addTupleBranch((TupleType) range, rangeNode);
    }
    else addScalarBranch((ScalarType) range, rangeNode);
  }

  /** Used by buildDataTree to build a Set branch. */
  void addSetBranch(SetType mathType, VisADNode node) {
    VisADNode setNode = new VisADNode("Set", null);
    node.add(setNode);

    // extract domain
    VisADNode domainNode = new VisADNode("Domain", null);
    setNode.add(domainNode);
    RealTupleType domain = mathType.getDomain();
    addTupleBranch((TupleType) domain, domainNode);
  }

  /** Used by buildDataTree to build a Tuple branch. */
  void addTupleBranch(TupleType mathType, VisADNode node) {
    int tupleLen = mathType.getDimension();
    VisADNode tupleNode;
    if (tupleLen > 1) {
      tupleNode = new VisADNode("Tuple", null);
      node.add(tupleNode);
    }
    else tupleNode = node;

    // extract components
    for (int i=0; i<tupleLen; i++) {
      MathType cType = null;
      try {
        cType = mathType.getComponent(i);
      }
      catch (VisADException exc) { }

      if (cType != null) {
        if (cType instanceof FunctionType) {
          addFunctionBranch((FunctionType) cType, tupleNode);
        }
        else if (cType instanceof SetType) {
          addSetBranch((SetType) cType, tupleNode);
        }
        else if (cType instanceof TupleType) {
          addTupleBranch((TupleType) cType, tupleNode);
        }
        else addScalarBranch((ScalarType) cType, tupleNode);
      }
    }
  }

  /** Used by buildDataTree to build a Scalar branch. */
  void addScalarBranch(ScalarType mathType, VisADNode node) {
    String name = mathType.getName();
    if (mathType instanceof TextType) name = "[TEXT] "+name;
    node.add(new VisADNode(name, mathType));
  }
}

