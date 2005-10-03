//
// OMETools.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2002 Bill Hibbard, Curtis Rueden, Tom
Rink, Dave Glowacki, Steve Emmerson, Tom Whittaker, Don Murray, and
Tommy Jasmin.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Library General Public
License as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Library General Public License for more details.

You should have received a copy of the GNU Library General Public
License along with this library; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
MA 02111-1307, USA
*/

package visad.data.bio;

import visad.util.ReflectedUniverse;
import visad.VisADException;

/**
 * A utility class for constructing and manipulating OME-XML DOMs.
 * It uses the loci.ome.xml package via reflection.
 *
 * @author Melissa Linkert linkert at cs.wisc.edu
 * @author Curtis Rueden ctrueden at wisc.edu
 */
public abstract class OMETools {

  // -- Constants --

  private static final String HEADER = "<?xml version = \"1.0\"?>\n" +
    "<OME xmlns = \"http://www.openmicroscopy.org/XMLschemas/OME/FC/" +
    "ome.xsd\"\n" +
    "xmlns:STD = \"http://www.openmicroscopy.org/XMLschemas/STD/RC2/" +
    "STD.xsd\"\n" +
    "xmlns:Bin = \"http://www.openmicroscopy.org/XMLschemas/BinaryFile/RC1/" +
    "BinaryFile.xsd\"\n" +
    "xmlns:xsi = \"http://www.w3.org/2001/XMLSchema-instance\"\n" +
    "xsi:schemaLocation = \"http://www.openmicroscopy.org/XMLschemas/OME/FC/" +
    "ome.xsd http://www.openmicroscopy.org/XMLschemas/OME/FC/ome.xsd " +
    "http://www.openmicroscopy.org/XMLschemas/STD/RC2/STD.xsd " +
    "http://www.openmicroscopy.org/XMLschemas/STD/RC2/STD.xsd\">\n" +
    "  <Experimenter/>\n" +
    "  <Group/>\n" +
    "  <Instrument/>\n" +
    "  <Image>\n" +
    "    <Pixels>\n" +
    "      <DisplayOptions/>\n" +
    "    </Pixels>\n" +
    "    <ChannelInfo/>\n" +
    "    <StageLabel/>\n" +
    "  </Image>\n" +
    "</OME>\n";

  private static final ReflectedUniverse R = createUniverse();

  private static ReflectedUniverse createUniverse() {
    ReflectedUniverse r = new ReflectedUniverse();
    try {
      r.exec("import loci.ome.xml.OMENode");
      r.exec("import loci.ome.xml.OMEXMLNode");
      r.exec("import loci.ome.xml.DOMUtil");
      r.setVar("FALSE", false);
    }
    catch (VisADException exc) { r = null; }
    return r;
  }


  // -- Static fields --

  private static int lsid = 1;


  // -- OMETools API methods --

  /** Constructs a new OME-XML root node. */
  public static Object createRoot() {
    return createRoot(HEADER);
  }

  /** Constructs a new OME-XML root node with the given XML block. */
  public static Object createRoot(String xml) {
    if (R == null || xml == null) return null;
    try {
      R.setVar("xml", xml);
      return R.exec("new OMENode(xml)");
    }
    catch (VisADException exc) { }
    return null;
  }

  /**
   * Sets the value of the specified attribute in the specified node.
   * @return True if the operation was successful.
   */
  public static boolean setAttribute(Object root,
    String nodeName, String name, String value)
  {
    if (R == null || root == null) return false;
    R.setVar("root", root);
    try {
      // get the node
      Object node = findNode(root, nodeName);
      if (node == null) return false;

      // set the LSID of the node
      R.setVar("node", node);
      R.setVar("lsid", "" + lsid++);
      R.exec("node.setLSID(lsid)");

      // set the attribute
      R.setVar("name", name);
      R.setVar("value", value);
      R.exec("node.setAttribute(name, value)");

      return true;
    }
    catch (VisADException exc) { }
    return false;
  }

  /** Dumps the given OME-XML DOM tree to a string. */
  public static String dumpXML(Object root) {
    if (root == null) return null;
    R.setVar("root", root);
    try {
      Object s = R.exec("root.writeOME(FALSE)");
      if (s instanceof String) return (String) s;
    }
    catch (VisADException exc) { }
    return null;
  }


  // -- Helper methods --

  /** Retrieves the first node associated with the given DOM element name. */
  private static Object findNode(Object root, String name)
    throws VisADException
  {
    if (R == null || root == null || name == null) return null;
    R.setVar("root", root);
    R.setVar("name", name);
    R.exec("rel = root.getDOMElement()");
    R.exec("doc = rel.getOwnerDocument()");
    R.exec("el = DOMUtil.findElement(name, doc)");
    if (R.getVar("el") == null) return null;
    return R.exec("OMEXMLNode.createNode(el)");
  }

}
