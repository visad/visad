// $Id: NcSDArray.java,v 1.3 2004-02-06 15:23:49 donm Exp $
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

import ucar.ma2.*;
import ucar.nc2.*;

import dods.dap.Server.*;
import dods.dap.*;
import dods.servers.agg.HasProxyObject;

import java.io.IOException;
import java.io.EOFException;
import java.util.Iterator;

/**
 * Wraps a netcdf variable with rank > 0 as an SDArray.
 * For char arrays, use NcSDString (rank 0 or 1) or NcSDCharArray (rank > 1).
 *
 * @version $Revision: 1.3 $
 * @author jcaron
 * @see NcSDCharArray
 */
public class NcSDArray extends SDArray implements HasProxyObject  {

  private boolean debug = false, debugRead = false, showTiming = false;
  private Variable ncVar = null;

  /**
  * Constructor: Wraps a netcdf variable in a DODS SDArray.
  *
  * @param v: netcdf Variable
  * @param bt: DODS element type
  */
  NcSDArray(Variable v, BaseType bt) {
    super(v.getName());
    this.ncVar = v;

    // set dimensions
    Iterator iter = v.getDimensions().iterator();
    while (iter.hasNext()) {
      Dimension dim = (Dimension) iter.next();
      appendDim(dim.getLength(), dim.getName());
    }

    // this seems to be how you set the type
    // it creates the "primitive vector"
    addVariable(bt);
  }

      /** get/set the underlying proxy */
  public void setProxy(Object v) { this.ncVar = (Variable) v; }
  public Object getProxy() { return ncVar; }

  /** Read the data values (parameters are ignored).
   *  Use the start, stop and stride values, typically set by the constraint evaluator.
   * @param datasetName not used
   * @param specialO not used
   * @return false (no more data to be read)
   * @exception IOException
   * @exception EOFException
   */
    public boolean read(String datasetName, Object specialO) throws IOException, EOFException {
      long tstart = System.currentTimeMillis();
      boolean hasStride = false;

      ArrayAbstract a = null;
      try {

        if (debugRead) {
          System.out.println("NcSDArray read "+ncVar.getName());
          for (int i=0; i < numDimensions(); i++) {
            DArrayDimension d = getDimension(i);
            System.out.println(" "+d.getName()+" "+getStart(i)+" "+getStride(i)+" "+getStop(i));
          }
        }

        // set up the netcdf read
        int n = numDimensions();
        int[] origin = new int[n];
        int[] shape = new int[n];

        for (int i=0; i<n; i++) {
          origin[i] = getStart(i);
          shape[i] = getStop(i) - getStart(i) + 1;
          hasStride = hasStride || (getStride(i) > 1);
        }

        try {
          a = (ArrayAbstract) ncVar.read(origin, shape);
        } catch (java.lang.ArrayIndexOutOfBoundsException t) {
          t.printStackTrace(System.out);
          System.out.println("  ERROR NcSDArray Read "+ getName());
          for (int i=0; i<n; i++)
            System.out.println("   "+origin[i]+" "+shape[i]);
          throw new RuntimeException();
        }

        if (debug) System.out.println("  NcSDArray Read "+ getName()+" "+a.getSize()+" elems of type = "+a.getElementType());
        if (debugRead) System.out.println("  Read = "+ a.getSize()+" elems of type = "+a.getElementType());
        if (showTiming) {
          long tookTime = System.currentTimeMillis() - tstart;
          System.out.println("NcSDArray read array: " + tookTime*.001 + " seconds");
          tstart = System.currentTimeMillis();
        }

        // deal with strides using a section
        if (hasStride) {
          Range[] r = new Range[n];
          for (int i=0; i<n; i++) {
            int s = getStride(i);
            if (s > 1) {  // otherwise null, means "take all elements"
              r[i] = new Range(0, shape[i]-1, s);
              if (debugRead) System.out.println(" Section dim "+i+" stride = "+s);
            }
          }
          a = (ArrayAbstract) a.section(r);
          if (debugRead) System.out.println("   section size "+a.getSize());
          if (showTiming) {
            long tookTime = System.currentTimeMillis() - tstart;
            System.out.println("NcSDArray section array: " + tookTime*.001 + " seconds");
            tstart = System.currentTimeMillis();
          }
        }

      } catch (InvalidParameterException e) {
        System.out.println(e);
        e.printStackTrace();
        throw new IllegalStateException("NcSDArray InvalidParameterException");
      } catch (InvalidRangeException e) {
        System.out.println(e);
        e.printStackTrace();
        throw new IllegalStateException("NcSDArray InvalidRangeException");
      }

      PrimitiveVector pv = getPrimitiveVector();
      if (debugRead) System.out.println(" PrimitiveVector type = "+pv.getTemplate()+
        " pv type = "+pv.getClass().getName());

      // copy the data into the PrimitiveVector
      // this is optimized (when there are no strides) to eliminate the copy
      // note we have to expose the internal MultiArray storage :(
      if (hasStride) {
        Object o = a.copyTo1DJavaArray();
        pv.setInternalStorage( o);
      } else
        pv.setInternalStorage( a.getStorage());

      if (debugRead) System.out.println(" PrimitiveVector len = "+pv.getLength()+" type = "+pv.getTemplate());
      if (showTiming) {
        long tookTime = System.currentTimeMillis() - tstart;
        System.out.println("NcSDArray copy array: " + tookTime*.001 + " seconds");
      }

      setRead(true);
      return (false);
    }

}

/* Change History:
   $Log: not supported by cvs2svn $
   Revision 1.1.1.1  2001/09/26 15:34:30  caron
   checkin beta1


 */