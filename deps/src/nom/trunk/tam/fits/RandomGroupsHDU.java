package nom.tam.fits;

/*
 * Copyright: Thomas McGlynn 1997-1998.
 * This code may be used for any purpose, non-commercial
 * or commercial so long as this copyright notice is retained
 * in the source code or included in or referred to in any
 * derived software.
 */


/** Random groups HDUs.  These are not currently supported.
  */
public class RandomGroupsHDU extends PrimaryHDU {

    public RandomGroupsHDU() throws FitsException {}

    public RandomGroupsHDU(Header myHeader)  throws FitsException {
	super(myHeader);
	if (!isHeader()) {
	  throw new FitsException("Not a valid random groups header");
	}
    }


    public static boolean isHeader(Header myHeader) {

        return (myHeader.getBooleanValue("SIMPLE") &
                myHeader.getBooleanValue("GROUPS")   ) ;

    }

    /** Check that this HDU has a valid header.
      * @return <CODE>true</CODE> if this HDU has a valid header.
      */
    public boolean isHeader() { return isHeader(myHeader); }

    public Data manufactureData() throws FitsException {
        throw new FitsException("Random groups structure not currently supported");
    }

    public void info() {

        System.out.println("Random Groups data not currently supported");

    }
}
