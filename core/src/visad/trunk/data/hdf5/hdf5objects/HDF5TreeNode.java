
/****************************************************************************
 * NCSA HDF                                                                 *
 * National Comptational Science Alliance                                   *
 * University of Illinois at Urbana-Champaign                               *
 * 605 E. Springfield, Champaign IL 61820                                   *
 *                                                                          *
 * For conditions of distribution and use, see the accompanying             *
 * hdf/COPYING file.                                                        *
 *                                                                          *
 ****************************************************************************/
package visad.data.hdf5.hdf5objects;

import javax.swing.tree.TreeNode;
import javax.swing.tree.DefaultMutableTreeNode;

public class HDF5TreeNode extends DefaultMutableTreeNode
{
	/** the indent for printing the tree */
	private final String INDENT = "  ";

	/** Creates a tree node that has no parent and no children,
		but which allows children.
	 */
	public HDF5TreeNode()
	{
		super();
	}

	/** Creates a tree node with no parent, no children, but which allows
		children, and initializes it with the specified user object.
	 */
	public HDF5TreeNode(HDF5Object userObject)
	{
		super (userObject);
	}

	/** Creates a tree node with no parent, no children, initialized with the
		specified user object, and that allows children only if specified.
	 */
	public HDF5TreeNode(Object userObject, boolean allowsChildren)
	{
		super(userObject, allowsChildren);
	}

	/** Print the tree information starting this node */
	public void printTree()
	{
		String indent = INDENT;
		printNodeDown(indent, this);
	}


	/** Print the tree information from starting node
	 *  @param indent the indent of tree level
	 *  @param sNode the starting node
	 */
	private void printNodeDown(String indent, TreeNode sNode)
	{
		System.out.print(indent);
		indent = indent + INDENT;

		if (sNode == null) {
			System.out.println("null");
			return;
		}

		System.out.println(sNode);

		if (sNode.isLeaf() || sNode.getChildCount() == 0)
			return;
		else {
			int nChildren = sNode.getChildCount();
			for (int i=0; i< nChildren; i++)
				printNodeDown(indent, sNode.getChildAt(i));
		}
	}

	/**
	 * Returns the result of sending <code>toString()</code> to this node's
	 * user object, or null if this node has no user object.
	 *
	 * @see	#getUserObject
	 */
	public String toString() {
		if (userObject == null) {
			return "null";
		} else if (userObject instanceof HDF5Object) {
			return ((HDF5Object)userObject).getShortName();
		} else {
			return userObject.toString();
		}
	}


}
