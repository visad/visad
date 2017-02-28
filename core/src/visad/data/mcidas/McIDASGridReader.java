//
// McIDASGridDirectory.java
//

/*
The software in this file is Copyright(C) 2017 by Tom Whittaker.
It is designed to be used with the VisAD system for interactive
analysis and visualization of numerical data.

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 1, or (at your option)
any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License in file NOTICE for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/
package visad.data.mcidas;

import java.io.*;
import java.util.*;
import visad.*;
import edu.wisc.ssec.mcidas.*;

/**  Read grid(s) from a McIDAS grid file
*/
public class McIDASGridReader {
  ArrayList gridH, gridD;
  int[] entry;
  RandomAccessFile fn;
  boolean needToSwap = false;


  public McIDASGridReader() {
    gridD = null;
    gridH = null;
  }

  /** read the first grid from the named file
  *
  * @return first grid
  */
  public ArrayList getGridData(String filename ) {

    // open file and get pointer block
    try {
      fn = new RandomAccessFile(filename,"r");
      int numEntries = Math.abs(readInt(10));
      if (numEntries > 10000000) {
          needToSwap = true;
          numEntries = Math.abs(McIDASUtil.swbyt4(numEntries));
      }
      fn.seek(0);
      // read the fileheader
      int[] fileHeader = new int[8];
      for (int i=0; i<8; i++) {
        fileHeader[i] = fn.readInt();
      }
      System.out.println("head="+McIDASUtil.intBitsToString(fileHeader));

      int project = readInt(8);
      //System.out.println("Project = " + project);

      int date = readInt(9);
      //System.out.println("date = " + date);

      entry = new int[numEntries];
      for (int i=0; i<numEntries; i++) {
        entry[i] = readInt(i + 11);
      }

      readEntry(0);

    } catch (Exception e) {System.out.println("exp="+e);}

    return gridD;
  }

  // internal method to fetch the 'ent'-th grid
  private void readEntry(int ent) {
    try {
      int te = entry[ent] * 4;
      System.out.println("Entry 0 = "+te);
      int[] gridHeader = new int[64];
      fn.seek(te);
      for (int i = 0; i < 64; i++) {
          gridHeader[i] = fn.readInt();
      }
      if (needToSwap) {
          swapGridHeader(gridHeader);
      }
      McIDASGridDirectory mgd = 
        new McIDASGridDirectory(new GridDirectory(gridHeader));
      System.out.println("grid header ="+mgd.toString());
      CoordinateSystem c = mgd.getCoordinateSystem();
      int rows = mgd.getRows();
      int cols = mgd.getColumns();
      System.out.println("# rows & cols = "+rows+" "+cols);

      double scale = mgd.getParamScale();
      //System.out.println("param scale = "+scale+" gridType="+mgd.getGridType());

      double[] data = new double[rows*cols];
      int n = 0;
            // store such that 0,0 is in lower left corner...
      for (int nc=0; nc<cols; nc++) {
        for (int nr=0; nr<rows; nr++) {
         int temp = fn.readInt();           // check for missing value
         if (needToSwap) temp = McIDASUtil.swbyt4(temp);
         data[(rows-nr-1)*cols + nc] = 
           (temp == McIDASUtil.MCMISSING)
             ? Double.NaN
             : ( (double) temp) / scale ;
        }
      }
      gridH = new ArrayList();
      gridD = new ArrayList();
      gridH.add(mgd);
      gridD.add(data);
    } catch (Exception esc) {System.out.println(esc);}
  }

  /**
   * Swap the grid header, avoiding strings
   *
   * @param gh   grid header to swap
   */
  private void swapGridHeader(int[] gh) {
    McIDASUtil.flip(gh, 0, 5);
    McIDASUtil.flip(gh, 7, 7);
    McIDASUtil.flip(gh, 9, 10);
    McIDASUtil.flip(gh, 12, 14);
    McIDASUtil.flip(gh, 32, 51);
  }

  /** to get some grid, by index value, other than the first one
  *
  * @return ArrayList of the single grid
  */
  public ArrayList getGrid(int index) {
    readEntry(index);
    return gridD;
  }

  /** to get the grid header corresponding to the last grid read
  *
  * @return McIDASGridDirectory of the last grid read
  */
  public ArrayList getGridHeaders() {
    return gridH;
  }

  /** for testing purposes
  */
  public  static void main(String[] args) {
    String file = "/src/visad/data/mcidas/GRID1715";
    if (args.length > 0) {
      file = args[0];
    }
    McIDASGridReader mg = new McIDASGridReader();
    mg.getGridData(file);
  }
    /**
     * Read an integer
     * @param word   word in file (0 based) to read
     *
     * @return  int read
     *
     * @throws IOException   problem reading file
     */
    private int readInt(int word) throws IOException {
        if (fn == null) {
            throw new IOException("no file to read from");
        }
        fn.seek(word * 4);
        int idata = fn.readInt();
        // set the order
        if (needToSwap) {
            idata = McIDASUtil.swbyt4(idata);
        }
        return idata;
    }

}
