package nom.tam.fits;

 /*
  * Copyright: Thomas McGlynn 1997-1998.
  * This code may be used for any purpose, non-commercial
  * or commercial so long as this copyright notice is retained
  * in the source code or included in or referred to in any
  * derived software.
  * Many thanks to David Glowacki (U. Wisconsin) for substantial
  * improvements, enhancements and bug fixes.
  */


/** Random groups HDUs.  Note that the internal storage of random
  * groups is a Object[ngroup][2] array.  The first element of
  * each group is the parameter data from that group.  The second element
  * is the data.  The parameters should be a one dimensional array
  * of the primitive types byte, short, int, long, float or double.
  * The second element is a n-dimensional array of the same type.
  * When analyzing group data structure only the first group is examined,
  * but for a valid FITS file all groups must have the same structure.
  */
public class RandomGroupsHDU extends BasicHDU {


    public RandomGroupsHDU(Header myHeader)  throws FitsException {
        super(myHeader);
        myData = manufactureData();
    }

    public RandomGroupsHDU(Object[][] data)
        throws FitsException {
        super(new Header());
        myData = new RandomGroupsData(data);
        pointToData(myHeader, data);
    }

    /** Make a header point to the given object.
      * @param h The header to be modified.
      * @param data The random groups data the header should describe.
      */
    static void pointToData(Header h, Object[][] data)
                            throws FitsException {

        if (data.length <= 0 || data[0].length != 2) {
            throw new FitsException("Data not conformable to Random Groups");
        }

        int gcount = data.length;
        Object paraSamp = data[0][0];
        Object dataSamp = data[0][1];

        Class pbase = nom.tam.util.ArrayFuncs.getBaseClass(paraSamp);
        Class dbase = nom.tam.util.ArrayFuncs.getBaseClass(dataSamp);

        if (pbase != dbase) {
            throw new FitsException("Data and parameters do not agree in type for random group");
        }

        int[] pdims = nom.tam.util.ArrayFuncs.getDimensions(paraSamp);
        int[] ddims = nom.tam.util.ArrayFuncs.getDimensions(dataSamp);

        if (pdims.length != 1) {
            throw new FitsException("Parameters are not 1 d array for random groups");
        }

        // We've now got the information we need to build the
        // header.

        h.setSimple(true);
        if (dbase == byte.class) {
            h.setBitpix(8);
        } else if (dbase == short.class) {
            h.setBitpix(16);
        } else if (dbase == int.class) {
            h.setBitpix(32);
        } else if (dbase == long.class) { // Non-standard
            h.setBitpix(64);
        } else if (dbase == float.class) {
            h.setBitpix(-32);
        } else if (dbase == double.class) {
            h.setBitpix(-64);
        } else {
            throw new FitsException("Data type:"+dbase+" not supported for random groups");
        }

        h.setNaxes(ddims.length+1);
        h.addIntValue("NAXIS1", 0, "");
        for (int i=2; i<=ddims.length+1; i += 1) {
            h.addIntValue("NAXIS"+i, ddims[i-2], "");
        }

        h.addBooleanValue("GROUPS", true, "");
        h.addIntValue("GCOUNT", data.length, "");
        h.addIntValue("PCOUNT", pdims[0], "");
    }

    /** Is the a random groups header?
      * @param myHeader The header to be tested.
      */
    public static boolean isHeader(Header myHeader) {

        return (myHeader.getBooleanValue("SIMPLE") &
                myHeader.getBooleanValue("GROUPS")   ) ;

    }

    /** Check that this HDU has a valid header.
      * @return <CODE>true</CODE> if this HDU has a valid header.
      */
    public boolean isHeader() { return isHeader(myHeader); }

    /** Create a FITS Data object corresponding to
      * this HDU header.
      */
    public Data manufactureData() throws FitsException {
        if (myHeader != null) {
            return new RandomGroupsData(myHeader);
        } else {
            return null;
        }

    }

    /** Display structural information about the current HDU.
      */
    public void info() {

        System.out.println("Random Groups HDU");
        if (myHeader != null) {
            System.out.println("   HeaderInformation:");
            System.out.println("     Ngroups:"+myHeader.getIntValue("GCOUNT"));
            System.out.println("     Npar:   "+myHeader.getIntValue("PCOUNT"));
            System.out.println("     BITPIX: "+myHeader.getIntValue("BITPIX"));
            System.out.println("     NAXIS:  "+myHeader.getIntValue("NAXIS"));
            for (int i=0; i<myHeader.getIntValue("NAXIS"); i += 1) {
                System.out.println("      NAXIS"+(i+1)+"= "+
                   myHeader.getIntValue("NAXIS"+(i+1)));
            }
        } else {
            System.out.println("    No Header Information");
        }


        Object[][] data = null;
        if (myData != null) {
            data = (Object[][]) myData.getData();
        }

        if (data == null || data.length < 1 || data[0].length != 2 ) {
            System.out.println("    Invalid/unreadable data");
        } else {
            System.out.println("    Number of groups:"+data.length);
            System.out.println("    Parameters: "+nom.tam.util.ArrayFuncs.arrayDescription(data[0][0]));
            System.out.println("    Data:"+nom.tam.util.ArrayFuncs.arrayDescription(data[0][1]));
        }
    }

}
