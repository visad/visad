// $Id: NcDAS.java,v 1.3 2004-02-06 15:23:49 donm Exp $
/*
 * Copyright 1997-2000 Unidata Program Center/University Corporation for
 * Atmospheric Research, P.O. Box 3000, Boulder, CO 80307,
 * support@unidata.ucar.edu.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package dods.servers.netcdf;

import ucar.nc2.*;
import ucar.nc2.dods.*;

import java.io.IOException;
import java.util.Iterator;

/**
 * Netcdf DAS object
 *
 * @version $Revision: 1.3 $
 * @author jcaron
 */

public class NcDAS extends dods.dap.DAS {

  /** Constructor */
  NcDAS( NetcdfFile ncfile ) {

    // Variable attributes
    Iterator iter = ncfile.getVariableIterator();
    while (iter.hasNext()) {
      Variable v = (Variable) iter.next();
      addAttributes(v.getName(), v, v.getAttributeIterator());
    }

    // Global attributes
    addAttributes("NC_GLOBAL", null, ncfile.getGlobalAttributeIterator());

    // unlimited dimension
    iter = ncfile.getDimensionIterator();
    while (iter.hasNext()) {
      Dimension d = (Dimension) iter.next();
      if (d.isUnlimited()) {
        dods.dap.AttributeTable table = new dods.dap.AttributeTable();
        try {
          table.appendAttribute("Unlimited_Dimension", dods.dap.Attribute.STRING, d.getName());
          addAttributeTable("DODS_EXTRA", table);
        } catch (Exception e) {
          System.out.println("Error adding Unlimited_Dimension ="+e);
        }
        break;
      }
    }
  }

  private void addAttributes(String name, Variable v, Iterator iter) {
    int count = 0;

    // add attribute table for this variable
    dods.dap.AttributeTable table = new dods.dap.AttributeTable();

    while (iter.hasNext()) {
      Attribute att = (Attribute) iter.next();
      try {
        if (att.isString())
          table.appendAttribute(att.getName(), DODSAttribute.getDODSType(att.getValueType()),
             "\""+att.getStringValue()+"\"");
        else {
          for (int i=0; i< att.getLength(); i++)
            table.appendAttribute( att.getName(), DODSAttribute.getDODSType(att.getValueType()),
              att.getNumericValue(i).toString());
        }
        count++;

      } catch (Exception e) {
        System.out.println("Error appending attribute "+att.getName()+" = "+att.getValue()+
        "\n"+e);
      }
    } // loop over variable attributes

    // kludgy thing to map char arrays to DODS Strings
    if ((v != null) && (v.getElementType() == char.class)) {
      int rank = v.getRank();
      int strlen = (rank == 0) ? 1 : v.getShape()[rank-1];
      try {
        dods.dap.AttributeTable dodsTable = table.appendContainer("DODS");
        dodsTable.appendAttribute("strlen", dods.dap.Attribute.INT32, Integer.toString(strlen));
        count++;
      } catch (Exception e) {
        System.out.println("Error appending attribute strlen\n"+e);
      }
    }

    if (count > 0)
      addAttributeTable(name, table);
  }

}

/* Change History:
   $Log: not supported by cvs2svn $
   Revision 1.1.1.1  2001/09/26 15:34:30  caron
   checkin beta1


 */
