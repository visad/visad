
//
// VisADNode.java
//

package visad.ss;

// AWT classes
import java.awt.swing.tree.DefaultMutableTreeNode;

// VisAD classes
import visad.ScalarType;

/** The VisADNode class is a TreeNode that stores extra node information. */
class VisADNode extends DefaultMutableTreeNode {
  ScalarType mathType;

  VisADNode(String label, ScalarType type) {
    super(label);
    mathType = type;
  }
}

