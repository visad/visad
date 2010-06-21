package nom.tam.test;

 /*
  * Copyright: Thomas McGlynn 1997-1998.
  * This code may be used for any purpose, non-commercial
  * or commercial so long as this copyright notice is retained
  * in the source code or included in or referred to in any
  * derived software.
  * Many thanks to David Glowacki (U. Wisconsin) for substantial
  * improvements, enhancements and bug fixes.
  */



import nom.tam.fits.*;
import nom.tam.util.*;
import java.util.Date;
import java.io.*;


/** This class comprises a set of static methods to test the Java FITS implementation.
   */
public class FitsTester {

    public static void main(String[] args) {
      testSimpleWrite();
      testSimpleRead();
      testNetRead();
      testSkipAndRead();
      testReadByRow();
      testWriteByRow();
      testBuildByColumn();
      testVarCols();
      testRandomGroups();
    }

    static void testRandomGroups() {

      start("Write and read a random groups data set");
      try {

         short[] pararr = new short[5];
         short[][] dataArr = new short[50][50];

         Object[][] test = new Object[10][2];
         // Only fill in the first row so we
         // can write the data group by group.

         test[0][0] = pararr;
         test[0][1] = dataArr;

         RandomGroupsHDU hdu = new RandomGroupsHDU(test);

         BufferedDataOutputStream os =
            new BufferedDataOutputStream(
               new FileOutputStream("test6.fits")
            );

         int padding = hdu.getData().getPadding();

         hdu.getHeader().write(os);

         for (int i=0; i<10; i += 1) {
             pararr[2] = (short) i;
             pararr[3] = (short) (i*i);
             dataArr[i][i] = (short)(i*i*i);
             os.writePrimitiveArray(pararr);
             os.writePrimitiveArray(dataArr);
         }
         byte[] pad = new byte[padding];
         os.write(pad);
         os.flush();
         os.close();
         os = null;

     } catch (Exception e) {
         System.out.println("Error writing random groups data");
         e.printStackTrace(System.out);
         return;
     }

     // Read the data back in.
     try {

         Fits rg = new Fits("Test6.fits");
         BasicHDU[] HDUs = rg.read();
         HDUs[0].info();
         Object[][] data = (Object[][]) HDUs[0].getData().getData();
         for (int i=0; i<10; i += 1) {

             short[] par = (short[]) data[i][0];
             System.out.println("    Group:"+(i+1)+" params2,3= "+par[2]+" "+par[3]);
             short[][] arr = (short[][])data[i][1];
             System.out.println("           Data[i][i] = "+arr[i][i]);
         }
     } catch (Exception e) {
         System.out.println("Error reading random groups data");
         e.printStackTrace(System.out);
     }
     end("Test Random Groups");
    }
    static void testVarCols() {

      start("Build and read variable length columns");
      try {
        Fits myFits = new Fits();

        myFits.addHDU(new PrimaryHDU());
        myFits.addHDU(HDU.create(genTable()));

        BasicHDU[] myHDUs = myFits.read();

        BinaryTableHDU table = (BinaryTableHDU )myHDUs[1];

        int[][] varData = new int[8][];
        for (int i=0; i<8; i += 1) {
            varData[i] = new int[i+1];
            for (int j=0; j <= i; j += 1) {
                varData[i][j] = 2*j;
            }
        }
        System.out.println("    Written to FITS:");
        for (int i=0; i<varData.length; i += 1) {
            System.out.print("       "+i+":");
            for (int j=0; j<varData[i].length; j += 1) {
               System.out.print(" "+j+":"+varData[i][j]);
            }
            System.out.println("");
        }

        Column newCol = table.makeVarColumn(varData, "J");
        newCol.addKey(Header.formatFields("TTYPE", "'TestVar '", "Name of variable column"));
        table.addColumn(newCol);

        BufferedDataOutputStream obs = new BufferedDataOutputStream(
                                        new FileOutputStream("test5.fits"));

        myFits.write(obs);
        obs.flush();
        obs.close();
      } catch (Exception e) {
        System.out.println("Exception writing test5.fits:"+e);
        e.printStackTrace(System.out);
      }

      try {

        Fits myFits = new Fits("test5.fits");
        BasicHDU[] myHDUs = myFits.read();

	if (myHDUs == null) {
	   System.out.println("Error: test5.fits doesn't seem to have any HDUs!");
	   return;
	}

        BinaryTableHDU table = (BinaryTableHDU )myHDUs[1];
        table.info();
        int[][] varcol = (int[][]) table.getVarData("TestVar");
	if (varcol == null) {
	   System.out.println("Error: test5.fits TestVar data not found");
	   return;
	}
        System.out.println("    Read from FITS:");
        for (int i=0; i<varcol.length; i += 1) {
            System.out.print("       "+i+":");
            for (int j=0; j<varcol[i].length; j += 1) {
               System.out.print(" "+j+":"+varcol[i][j]);
            }
            System.out.println("");
        }
     } catch (Exception e) {
        System.out.println("Caught exception reading test5.fits: " + e);
        e.printStackTrace(System.out);
     }

     end("Test Var columns");
    }

    static void testBuildByColumn () {

      start("Build by Column");

      try {
        Fits myFits = new Fits();
        myFits.addHDU(new PrimaryHDU());

        BinaryTableHDU myHDU = new BinaryTableHDU();

        int[][][] column = new int[8][][];

        for (int i=0; i<8; i += 1) {
            column[i] = new int[4][3];
            column[i][0][0] = i;
        }

        myHDU.addColumn(column);

        float[][] column2 = new float[8][10];
        for (int i=0; i<8; i += 1) {
            column2[i][0] = 100*i;
        }

        myHDU.addColumn(column2);

        double[][] column3 = new double[8][4];
        myHDU.addColumn(column3);

        myFits.addHDU(myHDU);

        BufferedDataOutputStream obs = new BufferedDataOutputStream(
                                           new FileOutputStream("test4.fits"));
        myFits.write(obs);

        obs.flush();
        obs.close();
        obs = null;

        myFits = new Fits("test4.fits");

        BasicHDU[] myHDUs = myFits.read();

	if (myHDUs == null) {
	   System.out.println("Error: test4.fits doesn't seem to have any HDUs!");
	   return;
	}

        for (int i=0; i<myHDUs.length; i += 1) {
             myHDUs[i].info();
        }
      } catch (Exception e) {
        System.out.println("Caught exception writing/reading test4.fits: "+e);
        e.printStackTrace(System.out);
      }

      end("Build by column: ");
    }

    static void testWriteByRow() {

      start("Write data row by row");

      Object[] aRow = new Object[3];
      aRow[0] = new int[30];
      aRow[1] = new float[10][10];
      aRow[2] = new double[2][3][4];
      String[] names = new String[3];
      names[0]= "RandomName1"; names[1]= "RandomName2"; names[2]= "RandomName3";

      int nrows = 20;

      try {
          Object[][] testTable = new Object[1][];
          testTable[0] = aRow;
          BinaryTableHDU dummy = new BinaryTableHDU(testTable);
          Header myHeader = dummy.getHeader();
          myHeader.addIntValue("NAXIS2", nrows, "Number of rows");
          for (int i=0; i<3; i += 1) {
              myHeader.findKey("TFORM"+(i+1));
              myHeader.addStringValue("TTYPE"+(i+1), names[i], "");
          }

          BufferedDataOutputStream obs = new BufferedDataOutputStream(
                                             new FileOutputStream("test2.fits"));

          PrimaryHDU rg = new PrimaryHDU();
          rg.write(obs);
          myHeader.write(obs);
          for (int i=0; i<nrows; i += 1) {
              ((int[])aRow[0])[0] = i;
              obs.writePrimitiveArray(aRow);
          }

          // Add in padding to make legal FITS.

          int paddingSize = myHeader.paddedDataSize() - myHeader.trueDataSize();
          byte[] pad = new byte[paddingSize];
          obs.write(pad);
          obs.flush();
          obs.close();

      } catch (Exception e) {
          System.out.println("Caught exception writing test2.fits: " +e);
          return;
      }

      try {
          Fits myFits = new Fits("test2.fits");
          BasicHDU[] myHDUs = myFits.read();
	  if (myHDUs == null) {
	     System.out.println("Error: test2.fits doesn't seem to have any HDUs!");
	     return;
	  }
          for (int i=0; i<myHDUs.length; i += 1) {
              myHDUs[i].info();
          }

           BinaryTable data = (BinaryTable) myHDUs[1].getData();
           for (int i=0; i < data.getNrow(); i += 1) {
               int[] col0 = (int[])data.getElement(i,0);
               System.out.println("    Row marker is:"+col0[0]);
           }
       } catch (Exception e) {
           System.out.println("Caught exception reading test2.fits:"+e);
	   e.printStackTrace(System.out);
       }
       end("Writing data row by row");
    }

    static void testReadByRow() {
      start("Read row by row");

      try {
      Fits myFits = new Fits("test1.fits");
      myFits.skipHDU(2);

      BufferedDataInputStream ibs = myFits.getStream();
      Header myHeader = Header.readHeader(ibs);
      if (myHeader == null) {
	  System.out.println("Third HDU from test1.fits is null!");
	  return;
      }

      Object[] aRow = new BinaryTableHeaderParser(myHeader).getModelRow();

      int nrows = myHeader.getIntValue("NAXIS2");

      byte[] col1 = (byte[]) aRow[0];
      for (int i=0; i<nrows; i += 1) {
          ibs.readPrimitiveArray(aRow);
          System.out.println("    Reading row:"+(i+1)+" with marker:"+col1[0]);
      }

      // We don't need to do this here, but it shows how you might do to get
      // to the start of the next HDU.
      ibs.skipBytes(myHeader.paddedDataSize() - myHeader.trueDataSize());
    } catch (Exception e) {
      System.out.println("Caught exception reading by rows:"+e);
      e.printStackTrace(System.out);
    }
    end("Reading row by row");

    }

    static void testSkipAndRead() {

      start("Skip to third extension");
      try {
        Fits myFits = new Fits("test1.fits");
        myFits.skipHDU(2);
        BasicHDU[] myHDUs = myFits.read();
	if (myHDUs == null) {
	   System.out.println("Error: test1.fits third extension doesn't exist!");
	   return;
	}
        for (int i=0; i<myHDUs.length; i += 1) {
            myHDUs[i].info();
        }
      } catch (Exception e) {
        System.out.println("Caught exception in skip and read:"+e);
      }
      end("Skip to third extension");
    }

    static void testSimpleWrite() {

      Fits myFits;
      start("Write a FITS file");
      try {
        // First create a null FITS object.
        myFits = new Fits();

        // Now create three extensions.

        int[] dims1 = {20,20,20};
        myFits.addHDU(HDU.create(ArrayFuncs.generateArray(Float.TYPE, dims1)));


        int[] dims2 = {2,2,2,8,16};
        myFits.addHDU(HDU.create(ArrayFuncs.generateArray(Integer.TYPE, dims2)));

        myFits.addHDU(HDU.create(genTable()));

        java.io.FileOutputStream fo = new java.io.FileOutputStream("test1.fits");
        BufferedDataOutputStream o = new BufferedDataOutputStream(fo);
        myFits.write(o);
     } catch (Exception e) {
        System.err.println("Exception thrown:"+e);
        e.printStackTrace(System.out);
        return;
     }

     end("Write a FITS file");
   }

   static void testSimpleRead() {

     Fits myFits;
     BasicHDU[] myHDUs;

     start("Read a FITS file");

     try {

       myFits = new Fits("test1.fits");
       myHDUs = myFits.read();

    } catch (Exception e) {

       System.out.println("Caught an exception reading test1.fits:"+e);
       e.printStackTrace(System.out);
       return;
    }

    if (myHDUs == null) {
       System.out.println("Error: test1.fits doesn't seem to have any HDUs!");
       return;
    }

    for (int i=0; i < myHDUs.length; i += 1) {
       try {
          if (myHDUs[i] == null) {
	    System.out.println("test1.fits HDU#" + i + " is null");
	  } else {
	    myHDUs[i].info();
	  }
       } catch (Exception e) {

          System.out.println("Caught an exception examining test1.fits HDU#"+i+":"+e);
          e.printStackTrace(System.out);
       }
    }

    end("Read a FITS file");

  }

static void testNetRead () {

    Fits myFits;
    BasicHDU[] myHDUs;

    start("Read compressed FITS file over the network");

    String testURL=
    "http://legacy.gsfc.nasa.gov/FTP/compton/data/egret/phase01/pnt_0010/counts_vp0010_g001.fits.gz";

    try {

      myFits = new Fits(testURL);
      myHDUs = myFits.read();
    } catch (Exception e) {
      System.out.println("Caught an exception reading over the net:"+e);
      e.printStackTrace(System.out);
      return;
    }

    if (myHDUs == null) {
       System.out.println("Error: net file doesn't seem to have any HDUs!");
       return;
    }

    for (int i=0; i<myHDUs.length; i += 1) {
        try {
	    myHDUs[i].info();
	} catch (Exception e) {
	  System.out.println("Caught an exception examining net HDU#"+i+":"+e);
	  e.printStackTrace(System.out);
	  return;
	}
    }
    end("Read over net");
}

static Object[][] genTable() {

         Object[] row1 = new Object[6];

         int[] dims0 = {10};
         row1[0] = ArrayFuncs.generateArray(Byte.TYPE, dims0);

         int[] dims1 = {5,5};
         row1[1] = ArrayFuncs.generateArray(Short.TYPE, dims1);

         int[] dims2 = {5,2};
         row1[2] = ArrayFuncs.generateArray(Integer.TYPE, dims2);

         int[] dims3 = {3,3,2};
         row1[3] = ArrayFuncs.generateArray(Integer.TYPE, dims3);

         int[] dims4 = {4,3,2};
         row1[4] = ArrayFuncs.generateArray(Float.TYPE, dims4);

         int[] dims5 = {6,7};
         row1[5] = ArrayFuncs.generateArray(Double.TYPE, dims5);

         Object[][] table = new Object[8][6];

         for (int i=0; i<8; i += 1) {

             table[i] = (Object[]) ArrayFuncs.deepClone(row1);

             // Mark each row.
             byte[] col0 = (byte[])  table[i][0];
             col0[0] = (byte)i;

         }
         return table;
    }

    static void start(String msg) {
        System.out.println("***************************************");
        System.out.println("Start:  "+ msg + " @ " + new Date());
    }

    static void end(String msg) {
        System.out.println("End:    "+ msg + " @ " + new Date());
        System.out.println("");
        System.out.println("");
        System.out.println("");
    }


}
