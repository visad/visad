package edu.wisc.ssec.mcidas;

import java.io.*;
import java.util.*;
import java.lang.*;
import java.awt.*;
import java.awt.image.*;
import edu.wisc.ssec.mcidas.*;
import edu.wisc.ssec.mcidas.adde.*;
import edu.wisc.ssec.mcidas.AreaFile;
import edu.wisc.ssec.mcidas.AreaFileException;

/** Program to fetch an AREA file and write it as a local file
*
* @author: Tom Whittaker, SSEC/CIMSS
*
*/

class GetAreaFile {
  final String[] paramNames = {"group", "user", "proj", "trace",
   "descr","band","mag","linele","place","pos",
   "size","unit","spac","doc","latlon",
   "aux","time","day","cal","id","host" };

  String[] flags = {"g","k","j","t",
   "d","b","m","l","n","p", 
   "s","u","z","o","r",
   "a","i","y","c","e","h"};

  String[] paramValues;

  String paramFile, outputFile;

  Properties params;

  /** AD_DATAOFFSET - byte offset to start of data block */
  public static final int AD_DATAOFFSET = 33;
  /** AD_NAVOFFSET - byte offset to start of navigation block */
  public static final int AD_NAVOFFSET  = 34;
  /** AD_AUXOFFSET - byte offset to start of auxilliary data section */
  public static final int AD_AUXOFFSET  = 59;
  /** AD_CALOFFSET - byte offset to start of calibration section */
  public static final int AD_CALOFFSET  = 62;
  /** AD_DATAWIDTH - number of bytes per data point */
  public static final int AD_DATAWIDTH  = 10;

  public static void main(String args[] ) {
    GetAreaFile gaf = new GetAreaFile(args);
  }

  public GetAreaFile(String args[]) {

    paramFile = "params.properties";

    // if no arguments, emit a "help" message
    if (args == null || args.length < 1) {
      System.out.println("Usage:  java edu.wisc.ssec.mcidas.GetAreaFile [options] output_file");
      System.out.println("   Command line [options] are:");
      for (int i=0; i<paramNames.length; i++) {
        System.out.println("    -"+flags[i]+" = "+paramNames[i]);
      }
      System.out.println("    -f = parameter save filename (def=params.properties)");
      System.exit(0);
    }

    AreaFile af;
    
    paramValues = new String[paramNames.length];

    // first try to get all the command line parameters
    outputFile = doArguments(args);
    if (outputFile == null) System.exit(1);

    // now go for the properties file
    params = fetchParams(paramFile);

    String request = makeADDEString();

    System.out.println("Request: "+request);

    try {
      af = new AreaFile(request);
    } catch (AreaFileException e) {
      System.out.println("Getting af:"+e);
      return;
    }
    int[] dir;
    try { dir=af.getDir();
    } catch (AreaFileException e){
      System.out.println("Getting dir:"+e);
      return;
    }
    System.out.println("Length of directory = "+dir.length);

    for (int i=0; i<dir.length; i++) {
     System.out.println(" index "+i+" = "+dir[i]);
    }

    int[] nav=null;
    try { nav=af.getNav();
          System.out.println("Length of nav block = "+nav.length);
    } catch (AreaFileException e){
      System.out.println("Getting nav:"+e);
      return;
    }

    int[] cal=null;
    try { cal=af.getCal();
          System.out.println("Length of cal block = "+cal.length);
    } catch (AreaFileException e){
      System.out.println("Getting cal:"+e);
    }

    int[] aux=null;
    try { aux=af.getAux();
          System.out.println("Length of aux block = "+aux.length);
    } catch (AreaFileException e){
      System.out.println("Getting aux:"+e);
    }

    int NL=dir[8];
    int NE=dir[9];

    System.out.println("Start reading data, num points="+(NL*NE));

    int[][]data;

    try { data = af.getData(0,0,NL,NE); }
    catch (AreaFileException e) {System.out.println(e);return;}
    System.out.println("Finished reading data");


    try {
      RandomAccessFile raf = new RandomAccessFile(outputFile,"rw");

    System.out.println("Dir to word 0");
      raf.seek(0);
      dir[0] = 0; // make sure this is zero!!
      for (int i=0; i<dir.length; i++) raf.writeInt(dir[i]);

    System.out.println("Nav to word "+dir[AD_NAVOFFSET]);
      if (nav != null && dir[AD_NAVOFFSET] > 0) {
        raf.seek(dir[AD_NAVOFFSET]);
        for (int i=0; i<nav.length; i++) raf.writeInt(nav[i]);
      }

    System.out.println("Cal to word "+dir[AD_CALOFFSET]);
      if (cal != null && dir[AD_NAVOFFSET] > 0) {
        raf.seek(dir[AD_CALOFFSET]);
        for (int i=0; i<cal.length; i++) raf.writeInt(cal[i]);
      }

    System.out.println("Aux to word "+dir[AD_AUXOFFSET]);
      if (aux != null && dir[AD_NAVOFFSET] > 0) {
        raf.seek(dir[AD_AUXOFFSET]);
        for (int i=0; i<aux.length; i++) raf.writeInt(aux[i]);
      }

    System.out.println("Data to word "+dir[AD_DATAOFFSET]);
      if (dir[AD_NAVOFFSET] > 0) {
        raf.seek(dir[AD_DATAOFFSET]);
        for (int i=0; i<data.length; i++) {
          for (int j=0; j<data[i].length; j++) {
            if (dir[AD_DATAWIDTH] == 1) {
              raf.writeByte(data[i][j]);
            } else if (dir[AD_DATAWIDTH] == 2) {
              raf.writeShort(data[i][j]);
            } else if (dir[AD_DATAWIDTH] == 4) {
              raf.writeInt(data[i][j]);
            }
          }
        }
      }

      raf.close();
    } catch (Exception we) {System.out.println(we);}

    System.out.println("Conversion done; saving parameters to: "+paramFile);
    writeParams(paramFile,params);
  }


  /** make the ADDE request string out of the various parameters
  * the host, group and descr are required.  Everything else is
  * optional.  version=1 is appended...
  */
  public String makeADDEString() {
    String host = params.getProperty("host");
    if (host == null) return null;

    String group = params.getProperty("group");
    if (group == null) return null;

    String descr = params.getProperty("descr");
    if (descr == null) return null;

    StringBuffer sb = new StringBuffer("adde://"+host+"/image?");
    for (int i=0; i<paramNames.length; i++) {
      if (paramNames[i] != "host" && params.getProperty(paramNames[i]) != null) {
        sb.append(paramNames[i]+"="+params.getProperty(paramNames[i])+"&");
      }
    }
    sb.append("version=1");
    return sb.toString();
  }

  /** scans the input argument list and if it finds any legitimate
  * flags, it replaces the value of the parameter
  */
  String doArguments(String[] arg) {
    String outfile = arg[arg.length - 1];
    for (int k=0; k<arg.length-1; k++) {
      String s = arg[k];
      if ((s.length()) > 1 && s.startsWith("-")) {
        String r = s.substring(1,2);
          if (r.equals("f")) {
            if (s.length() == 2) {
              paramFile = arg[++k];
            } else {
              paramFile = s.substring(2);
            }
        } else {
          for (int i=0; i<paramNames.length; i++) {
            if (r.equals(flags[i])) {
              if (s.length() == 2) {
                paramValues[i] = arg[++k];
              } else {
                paramValues[i] = s.substring(2);
              }
            }
          }
        }
      } else {
        System.out.println("Problem with parameter: "+s);
        return null;
      }

    }
    return outfile;
  }


  /** fetch the parameters from the property file
  */
  Properties fetchParams(String filename) {
    
    Properties p = new Properties();
    try {

      InputStream is = new FileInputStream(filename);
      p.load(is);
      is.close();
    } catch (Exception e) {System.out.println(e);}

    for (int i=0; i<paramNames.length; i++) {
      if (paramValues[i] != null) p.put(paramNames[i], paramValues[i]);
    }
    return p;

  }


  /** write the property file back out...
  */
  void writeParams(String filename, Properties p) {
    try {
      OutputStream os = new FileOutputStream(filename);
      p.save(os,"GetAreaFile properties");
      os.flush();
      os.close();
    } catch (Exception e) {System.out.println(e);}
  }

}
/* 
 *-g   group=<groupname>         ADDE group name
 *-k   user=<user_id>            ADDE user identification
 *-j   proj=<proj #>             a valid ADDE project number
 *-t   trace=<0/1>               setting to 1 tells server to write debug
 *                               trace file (imagedata, imagedirectory)
 *     version=1                 ADDE version number, currently 1
 *
 *-d   descr=<descriptor>        ADDE descriptor name
 *-b   band=<band>               spectral band or channel number
 *-m   mag=<lmag> <emag>         image magnification, postitive for blowup,
 *                               negative for blowdown (default = 1, emag=lmag)
 *                               (imagedata only)
 *-l   latlon=<lat> <lon>        lat/lon point to center image on 
 *-n   linele=<lin> <ele> <type> line/element to center image on 
 *-c   place=<placement>         placement of lat/lon or linele points (center
 *                               or upperleft (def=center)) 
 *-p   pos=<position>            request an absolute or relative ADDE position
 *                               number
 *-s   size=<lines> <elements>   size of image to be returned (imagedata only)
 *-u   unit=<unit>               to specify calibration units other than the
 *                               default
 *-z   spac=<bytes>              number of bytes per data point, 1, 2, or 4
 *                               (imagedata only)
 *-c   doc=<yes/no>              specify yes to include line documentation
 *                               with image (def=no)
 *-a   aux=<yes/no>              specify yes to include auxilliary information
 *                               with image
 *-i   time=<time1> <time2>      specify the time range of images to select
 *                               (def=latest image if pos not specified)
 *-y   day=<day>                 specify the day of the images to select
 *                               (def=latest image if pos not specified)
 *-c   cal=<cal type>            request a specific calibration on the image
 *                               (imagedata only)
 *-f   id=<stn id>               radar station id
 *
*/

