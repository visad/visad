
//
// RadarFile.java
//

/*
This sofware is part of the Australian Integrated Forecast System (AIFS)
Copyright (C) 2023 Bureau of Meteorology
*/

package visad.bom;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.Vector;

import visad.DateTime;
import visad.VisADException;

/**
 * RadarFile
 * 
 * @author - James Kelly : J.Kelly@bom.gov.au converted from Phil Purdam's
 *         radl_cnvt.c
 * 
 * 
 */

public class RadarFile {

  public DateTime dtTime;
  public double  dRadarTime;
  public float rngres = 250.0f;    // Resolution of range rings, in metres
  public float startrng = 4000.0f; // Start Range in metres
  public float azimuthres = 1.0f;  // Resolution of azmuth, in degrees
  public float elev = 0.0f;        // Elevation of radar beam
  // public float center_latitiude = -30.0f;
  // public float center_longitiude = 160.0f;
  private BufferedReader rf;
  private int az;
  // public int azimuth[];
  public byte radial[][];
  final static char decimal = '.';
  final static char percent ='%';
  final static char[] A2NXlat     = {'\u0000','\u0001','\u0002','\u0003','\u0004','\u0005','\u0006',
                                '\u0010','\u0011','\u0012','\u0013','\u0014','\u0015','\u0016',
                                '\u0020','\u0021','\u0022','\u0023','\u0024','\u0025','\u0026',
                                '\u0030','\u0031','\u0032','\u0033','\u0034','\u0035','\u0036',
                                '\u0040','\u0041','\u0042','\u0043','\u0044','\u0045','\u0046',
                                '\u0050','\u0051','\u0052','\u0053','\u0054','\u0055','\u0056',
                                '\u0060','\u0061','\u0062','\u0063','\u0064','\u0065','\u0066' };

  final static int maxSize = 250; // maximum number of radial values, usually 512
  public byte[] bdata;

  public class PolarByteData {
    public double azimuth;
    public byte[] bdata;
    public PolarByteData() { azimuth = 0.0; bdata = new byte[maxSize];}
    public PolarByteData(double az, byte[] bdata) {
      azimuth = az;
      this.bdata = new byte[bdata.length];
      System.arraycopy(bdata, 0, this.bdata, 0, bdata.length);
    }
  }
  public Vector pbvector = new Vector();
  public PolarByteData pbdata;
  public PolarByteData[] pbdataArray;


  public RadarFile(String radarSource) throws IOException {
    // try as a disk file first
    // try {
    rf = new BufferedReader( new FileReader (radarSource));
    az=0;
    while (rf != null) {
      readRadial();
      pbdata = new PolarByteData((double) az, bdata);
      System.arraycopy(bdata, 0, pbdata.bdata, 0, bdata.length);
      if (rf != null) pbvector.addElement(pbdata);
    }
    pbdataArray = new PolarByteData[pbvector.size()];
    pbvector.copyInto(pbdataArray);

  }

 /**
   * Retrieves the time of the radar image as a double.
   *
   * @return image time
   */
  public double getTime()
	{
		return dRadarTime;
	}


  public void setTime(String radarTime)
  {
		try {
		 	dRadarTime = Double.valueOf(radarTime).doubleValue();
		} catch (NumberFormatException e) {
			System.out.println("Exception converting Radar Time in module visad.bom.RadarFile.getTime() " + e);
			dRadarTime = 0.0;
		}
	}


 /**
   * Retrieves the time of the radar image as a VisAD DateTime.
   *
   * @return image time
   */
  public DateTime getRadarTime()
  {
    return (dtTime);
  }

  public void setRadarTime(String timeStamp)
      throws VisADException
  {
    // TIMESTAMP: 19990915024004
    int year;
    int month;
    int day;
    int hours;
    int mins;
    int secs;
		String[] ids = TimeZone.getAvailableIDs(0);
		TimeZone timeZone = new SimpleTimeZone(0, ids[0]);
		Calendar cal = new GregorianCalendar(timeZone);

		year  = Integer.valueOf(timeStamp.substring(0,4)).intValue();
		month = Integer.valueOf(timeStamp.substring(4,6)).intValue();
		day   = Integer.valueOf(timeStamp.substring(6,8)).intValue();
		hours  = Integer.valueOf(timeStamp.substring(8,10)).intValue();
		mins  = Integer.valueOf(timeStamp.substring(10,12)).intValue();
		secs  = Integer.valueOf(timeStamp.substring(12,14)).intValue();
    System.out.println("timeStamp: " + timeStamp);
    System.out.println("year,month,day,hour,mins,secs: " + year+ " " + month + " " +day+ " " + hours+ " " + mins+ " " + secs);
		// Subtract 1 from month since Jan = 0, Feb = 1 etc
		cal.clear();
		cal.set(year,month-1,day,hours,mins,secs);
    System.out.println("Initialized with date: " + (cal.getTime()).toString());

    dtTime = new DateTime(cal.getTime());
    System.out.println("Initialized with date: " + dtTime);
  }


  public void readHeader(char[] cbuff ) {
  	String radarTime;
		String thisLine = new String(cbuff);
		// System.out.println("line = " + thisLine);
		if (thisLine.startsWith("COUNTRY:")) {
			System.out.println("line = " + thisLine);
		} else
		if (thisLine.startsWith("NAME:")) {
			System.out.println("line = " + thisLine);
		} else
		if (thisLine.startsWith("STNID:")) {
			System.out.println("line = " + thisLine);
		} else
		if (thisLine.startsWith("DATE:")) {
			System.out.println("line = " + thisLine);
		} else
		if (thisLine.startsWith("TIME:")) {
			radarTime = new String (thisLine.substring(6));
			System.out.println("radarTime = " + radarTime);
		} else
		if (thisLine.startsWith("TIMESTAMP:")) {
			try {
      	setRadarTime(thisLine.substring(11)) ;
			} catch (VisADException e) {
      	System.out.println("error setting radar time " + e );
			}
		} else
		if (thisLine.startsWith("VERS:")) {
			System.out.println("line = " + thisLine);
		} else
		if (thisLine.startsWith("RNGRES:")) {
			rngres = Float.valueOf(thisLine.substring(8)).floatValue();
			System.out.println("rngres = " + rngres);
		} else
		if (thisLine.startsWith("ANGRES:")) {
			System.out.println("line = " + thisLine);
		} else
		if (thisLine.startsWith("VIDRES:")) {
			System.out.println("line = " + thisLine);
		} else
		if (thisLine.startsWith("STARTRNG:")) {
			startrng = Float.valueOf(thisLine.substring(10)).floatValue();
			System.out.println("startrng = " + startrng);
		} else
		if (thisLine.startsWith("ENDRNG:")) {
			System.out.println("line = " + thisLine);
		} else
		if (thisLine.startsWith("PRODUCT:")) {
			System.out.println("line = " + thisLine);
		} else
		if (thisLine.startsWith("IMGFMT:")) {
			System.out.println("line = " + thisLine);
		} else
		if (thisLine.startsWith("ELEV:")) {
			elev = Float.valueOf(thisLine.substring(6)).floatValue();
			System.out.println("elev = " + elev);
		} else
		if (thisLine.startsWith("DBZLVL:")) {
			System.out.println("line = " + thisLine);
		} else
		if (thisLine.startsWith("CLEARAIR:")) {
			System.out.println("line = " + thisLine);
		} else
		if (thisLine.startsWith("DBZCALDLVL:")) {
			System.out.println("line = " + thisLine);
		} else
		if (thisLine.startsWith("DIGCALDLVL:")) {
			System.out.println("line = " + thisLine);
		} else
		if (thisLine.startsWith("BEAMWIDTH:")) {
			System.out.println("line = " + thisLine);
		} else
		if (thisLine.startsWith("PULSELENGTH:")) {
			System.out.println("line = " + thisLine);
		} else
		if (thisLine.startsWith("STCRANGE:")) {
			System.out.println("line = " + thisLine);
		} else
		if (thisLine.startsWith("TXFREQUENCY:")) {
			System.out.println("line = " + thisLine);
		} else
		if (thisLine.startsWith("TXPEAKPWR:")) {
			System.out.println("line = " + thisLine);
		} else
		if (thisLine.startsWith("ANTGAIN:")) {
			System.out.println("line = " + thisLine);
		} else
		if (thisLine.startsWith("NOISETHRESH:")) {
			System.out.println("line = " + thisLine);
		}
  }
/* Header format:
COUNTRY: 036
NAME: Adel
STNID: 11
DATE: 25899
TIME: 02.40
TIMESTAMP: 19990915024004
VERS: 8.13
RNGRES: 2000
ANGRES: 1.0
VIDRES: 6
STARTRNG: 4000
ENDRNG: 512000
PRODUCT: NORMAL
IMGFMT: CompPPI
ELEV: 000.5
DBZLVL: 11.8 27.8 39.0 43.8 48.6 55.0
CLEARAIR: OFF
DBZCALDLVL: 11.8 23.0 28.0 31.0 34.0 37.0 40.0 43.0 46.0 49.0 52.0 55.0 58.0 61.0 64.0
DIGCALDLVL: 17 34 43 50 56 61 68 73 77 84 90 96 101 107 113
BEAMWIDTH: 3.00
PULSELENGTH: 1.7
STCRANGE: 111
TXFREQUENCY: 2880
TXPEAKPWR: 500
ANTGAIN: 33.0
NOISETHRESH: 14
*/


  public void readRadial() throws IOException {

    int pos = 0;
    boolean done = false;
    int  rptCount;
    int sizeBuff;

    char[] cbuff;
    char thisChar;
    StringBuffer sbuff;
    String ipString;

    sbuff = new StringBuffer();
    bdata = new byte[maxSize];

    //
    // %ddd or %ddd.d
    //
    // % = first character
    //
    ipString = rf.readLine();

// WLH
if (ipString == null) {
  rf = null;
  return;
}

    cbuff = ipString.toCharArray();
    sizeBuff = cbuff.length;

// WLH
if (sizeBuff == 0) {
//   rf = null;
  return;
}

    thisChar = cbuff[pos];
    if ( percent == thisChar) {
	// System.out.println("percent found");
      pos++;
    } else {
			readHeader(cbuff);
		}

    //
    // ddd = azimuthal direction (degrees)
    //
    thisChar = cbuff[pos++];
    while (Character.isDigit(thisChar) || (thisChar == decimal)) {
       sbuff.append(thisChar);
       // System.out.println("thisChar, pos, sbuff =" + thisChar + " " + pos + " " + sbuff);
       thisChar = cbuff[pos++];
    }
    try {
       // System.out.println("azimuth: " + az);
       // System.out.println("sbuff, length = " + sbuff + " " + sbuff.length());
	   if (sbuff.length() != 0)
         az = Math.round((Float.valueOf(sbuff.toString())).floatValue());

    }
    catch (NumberFormatException e) {
       System.out.println("error converting radial " + e );
    }

    pos--;
    // assert: next char is alpha
    while (!done) {
      // System.out.println("assert: next char is alpha");

      thisChar = cbuff[pos];
      // System.out.println("thisChar = " + thisChar);
      pos++;
      if (thisChar >= 'A' && thisChar <= 'Y') {
         thisChar -= 'A';
	  }
      else {
         if (thisChar >= 'a' && thisChar <= 'x')
            thisChar -= 'H';
         else
            thisChar = '\u00FF';
      } // endif
      if (thisChar != '\u00FF') {
         thisChar = A2NXlat[(int)thisChar];
         rptCount = 0;
		 if (pos < cbuff.length) {
         while (Character.isDigit( cbuff[pos]) ) {
             rptCount = (rptCount * 10) + (cbuff[pos] - '0');
			 if (++pos >= cbuff.length) {
			 	done = true;
				break;
			 }
         } // endwhile
		 }
		 else
		   done = true;
         rptCount++;
         if ((sizeBuff + (rptCount * 2)) > maxSize) {
           rptCount = (maxSize - sizeBuff)/2;
           done = true;
         } // endif
         while ((rptCount--) > 0) {
           bdata[sizeBuff] = ((byte)((thisChar)  & '\u000f'));
           bdata[sizeBuff+1] = ((byte)((thisChar) >> 4));
           sizeBuff +=2;
         } // endwhile
       }
       else {
         done = true;
         pos++;
       } //  endif
       // return 0;
	   // pos++ ;
     } // endwhile

	// System.out.print("bdata = ");
	// for (int i=0; i < maxSize; i++) {
	//      System.out.print(bdata[i] + " ");
    // }
	// System.out.println(" ");

    // System.out.println("end readRadial");
  } // readRadial


} // RadarFile

