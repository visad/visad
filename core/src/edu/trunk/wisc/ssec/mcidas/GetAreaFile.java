package edu.wisc.ssec.mcidas;

import java.io.*;
import java.util.*;
import java.lang.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
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
  final String[] paramNames = {"host", "group", "descr", "user", "proj", 
  "trace", "band","mag","linele","place","pos",
   "size","unit","spac","doc","latlon",
   "aux","time","day","cal","id" };

  String[] flags = {"h","g","d", "k","j","t",
   "b","m","l","n","p", 
   "s","u","z","o","r",
   "a","i","y","c","e"};

  String[] paramValues;
  String[] serverNames;
  String paramFile, outputFile;
  Properties pars;
  boolean verbose;
  boolean doGUI=false;
  UseGUI ug = null;

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
    verbose = false;

    // if no arguments, emit a "help" message
    if (args == null || args.length < 1) {
      System.out.println("Usage:  java edu.wisc.ssec.mcidas.GetAreaFile [options] output_file");
      System.out.println("   Command line [options] are:");
      for (int i=0; i<paramNames.length; i++) {
        System.out.println("    -"+flags[i]+" = "+paramNames[i]);
      }
      System.out.println("    -f = parameter save filename (def=params.properties)");
      System.out.println("    -v  (verbose text output)");
      System.out.println(" Note: for multi-argument options (like -s), you need to enclose the values in quotes. e.g., -s \"200 200\"");
      System.exit(0);
    }

    paramValues = new String[paramNames.length];

    // first try to get all the command line parameters
    outputFile = doArguments(args);
    if (outputFile == null) System.exit(1);

    // now go for the properties file
    pars = fetchParams(paramFile);

    if (doGUI) {
      ug = new UseGUI(this, pars);
    } else {
      doRequest(pars);
    }


  }

  public void doRequest(Properties params) {

    String request = makeADDEString(params);

    if (ug != null) outputFile = params.getProperty("outfile");

    System.out.println("Request sent: "+request);
    if (ug != null) ug.status("Request sent: "+request);

    AreaFile af;
    
    try {
      af = new AreaFile(request);
    } catch (AreaFileException e) {
      System.out.println("While getting AreaFile:"+e);
      if (ug != null) ug.status("Error while getting AREA file:"+e);
      return;
    }
    int[] dir;
    try { dir=af.getDir();
    } catch (AreaFileException e){
      if (verbose) System.out.println("Getting dir:"+e);
      return;
    }
    if (verbose) System.out.println("Length of directory = "+dir.length);

    for (int i=0; i<dir.length; i++) {
     if (verbose) System.out.println(" index "+i+" = "+dir[i]);
    }

    int[] nav=null;
    try { nav=af.getNav();
          if (verbose) System.out.println("Length of nav block = "+nav.length);
    } catch (AreaFileException e){
      if (verbose) System.out.println("Getting nav:"+e);
      return;
    }

    int[] cal=null;
    try { cal=af.getCal();
          if (verbose) System.out.println("Length of cal block = "+cal.length);
    } catch (AreaFileException e){
      if (verbose) System.out.println("Getting cal:"+e);
    }

    int[] aux=null;
    try { aux=af.getAux();
          if (verbose) System.out.println("Length of aux block = "+aux.length);
    } catch (AreaFileException e){
      if (verbose) System.out.println("Getting aux:"+e);
    }

    int NL=dir[8];
    int NE=dir[9];

    if (verbose) System.out.println("Start reading data, num points="+(NL*NE));
    if (ug != null) ug.status("Start reading data, num points="+(NL*NE));


    int[][]data;

    try { data = af.getData(0,0,NL,NE); }
    catch (AreaFileException e) {System.out.println(e);return;}

    if (verbose) System.out.println("Finished reading data");
    if (ug != null) ug.status("Finished reading data");


    try {
      RandomAccessFile raf = new RandomAccessFile(outputFile,"rw");

    if (verbose) System.out.println("Dir to word 0");
      raf.seek(0);
      dir[0] = 0; // make sure this is zero!!
      for (int i=0; i<dir.length; i++) raf.writeInt(dir[i]);

    if (verbose) System.out.println("Nav to word "+dir[AD_NAVOFFSET]);
      if (nav != null && dir[AD_NAVOFFSET] > 0) {
        raf.seek(dir[AD_NAVOFFSET]);
        for (int i=0; i<nav.length; i++) raf.writeInt(nav[i]);
      }

    if (verbose) System.out.println("Cal to word "+dir[AD_CALOFFSET]);
      if (cal != null && dir[AD_NAVOFFSET] > 0) {
        raf.seek(dir[AD_CALOFFSET]);
        for (int i=0; i<cal.length; i++) raf.writeInt(cal[i]);
      }

    if (verbose) System.out.println("Aux to word "+dir[AD_AUXOFFSET]);
      if (aux != null && dir[AD_NAVOFFSET] > 0) {
        raf.seek(dir[AD_AUXOFFSET]);
        for (int i=0; i<aux.length; i++) raf.writeInt(aux[i]);
      }

    if (verbose) System.out.println("Data to word "+dir[AD_DATAOFFSET]);
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

    System.out.println("Completed. Data saved to: "+outputFile+"   Saving parameters to: "+paramFile);
    if (ug != null) ug.status("Completed. Data saved to: "+outputFile+"   Saving parameters to: "+paramFile);

    writeParams(paramFile,params);
  }


  /** make the ADDE request string out of the various parameters
  * the host, group and descr are required.  Everything else is
  * optional.  version=1 is appended...
  */
  public String makeADDEString(Properties params) {
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

    // if there is only one parameter, see if it's the '-gui' switch
    // instead of the 'outfile' name
    if (outfile.equalsIgnoreCase("-gui")) {
      doGUI = true;
      outfile = " ";
      return outfile;
    }

    for (int k=0; k<arg.length-1; k++) {
      String s = arg[k];
      if ((s.length()) > 1 && s.startsWith("-")) {
        if (s.equalsIgnoreCase("-gui")) {
          doGUI = true;
          continue;
        }

        String r = s.substring(1,2);
        if (r.equals("f")) {
          if (s.length() == 2) {
            paramFile = arg[++k];
          } else {
            paramFile = s.substring(2);
          }

        } else if (r.equals("v")) {
          verbose = true;

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
    //if (outputFile != null && outputFile.length>1) p.put("outfile",outputFile); 
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

class UseGUI extends Frame implements ActionListener, FocusListener {
  TextField thost, tgroup, tdescr;
  TextField tpos, tday, ttime;
  TextField tplace, tlatlon, tlinele;
  TextField tsize, tmag, tspac;
  TextField tband, tunit;
  TextField taux, tcal, tid, tdoc;
  TextField ttrace, tuser, tproj;
  TextField toutfile;
  Properties p;
  GetAreaFile parent;
  
  Button getData, dismiss;
  int numb;
  boolean changed = false;
  Label help;

  public UseGUI(GetAreaFile mom, Properties pp) {

    super("GetAreaFile set/change parameters");
    setLayout(new BorderLayout());
    Panel p0 = new Panel();
    p = pp;
    parent = mom;
    p0.setLayout(new GridLayout(0,1,1,5));

    Panel p1 = new Panel();
    p1.setLayout(new FlowLayout());
    thost = new TextField(p.getProperty("host"));
    thost.addActionListener(this);
    thost.addFocusListener(this);
    p1.add(makePanel("Hostname",thost));
    tgroup = new TextField(p.getProperty("group"));
    tgroup.addActionListener(this);
    tgroup.addFocusListener(this);
    p1.add(makePanel("Groupname",tgroup));
    tdescr = new TextField(p.getProperty("descr"));
    tdescr.addActionListener(this);
    tdescr.addFocusListener(this);
    p1.add(makePanel("Descriptor",tdescr));
    p0.add(p1);

    Panel p2 = new Panel();
    p2.setLayout(new FlowLayout());
    tpos = new TextField(p.getProperty("pos"));
    tpos.addActionListener(this);
    tpos.addFocusListener(this);
    p2.add(makePanel("Position",tpos));
    tday = new TextField(p.getProperty("day"));
    tday.addActionListener(this);
    tday.addFocusListener(this);
    p2.add(new Label(" or ",Label.CENTER));
    p2.add(makePanel("Day",tday));
    ttime = new TextField(p.getProperty("time"));
    ttime.addActionListener(this);
    ttime.addFocusListener(this);
    p2.add(makePanel("Time",ttime));
    p0.add(p2);

    Panel p3 = new Panel();
    p3.setLayout(new FlowLayout());
    tplace = new TextField(p.getProperty("place"));
    tplace.addActionListener(this);
    tplace.addFocusListener(this);
    p3.add(makePanel("Place",tplace));
    tlatlon = new TextField(p.getProperty("latlon"));
    tlatlon.addActionListener(this);
    tlatlon.addFocusListener(this);
    p3.add(makePanel("Lat/Lon",tlatlon));
    p3.add(new Label(" or ",Label.CENTER));
    tlinele = new TextField(p.getProperty("linele"));
    tlinele.addActionListener(this);
    tlinele.addFocusListener(this);
    p3.add(makePanel("Line/Element",tlinele));
    p0.add(p3);

    Panel p4 = new Panel();
    p4.setLayout(new FlowLayout());
    tband = new TextField(p.getProperty("band"));
    tband.addActionListener(this);
    tband.addFocusListener(this);
    p4.add(makePanel("Band#",tband));
    tmag = new TextField(p.getProperty("mag"));
    tmag.addActionListener(this);
    tmag.addFocusListener(this);
    p4.add(makePanel("Magnify",tmag));
    tunit = new TextField(p.getProperty("unit"));
    tunit.addActionListener(this);
    tunit.addFocusListener(this);
    p4.add(makePanel("Unit",tunit));
    tspac = new TextField(p.getProperty("spac"));
    tspac.addActionListener(this);
    tspac.addFocusListener(this);
    p4.add(makePanel("Bytes",tspac));
    tsize = new TextField(p.getProperty("size"));
    tsize.addActionListener(this);
    tsize.addFocusListener(this);
    p4.add(makePanel("  Size  ",tsize));
    p0.add(p4);

    Panel p5 = new Panel();
    p5.setLayout(new FlowLayout());
    tcal = new TextField(p.getProperty("cal"));
    tcal.addActionListener(this);
    tcal.addFocusListener(this);
    p5.add(makePanel("Cal type",tcal));
    taux = new TextField(p.getProperty("aux"));
    taux.addActionListener(this);
    taux.addFocusListener(this);
    p5.add(makePanel("Aux block",taux));
    tdoc = new TextField(p.getProperty("doc"));
    tdoc.addActionListener(this);
    tdoc.addFocusListener(this);
    p5.add(makePanel("Doc block",tdoc));
    tid = new TextField(p.getProperty("id"));
    tid.addActionListener(this);
    tid.addFocusListener(this);
    p5.add(makePanel("Station ID",tid));
    p0.add(p5);

    Panel p6 = new Panel();
    p6.setLayout(new FlowLayout());
    tuser = new TextField(p.getProperty("user"));
    tuser.addActionListener(this);
    tuser.addFocusListener(this);
    p6.add(makePanel("User",tuser));
    tproj = new TextField(p.getProperty("proj"));
    tproj.addActionListener(this);
    tproj.addFocusListener(this);
    p6.add(makePanel("Project",tproj));
    ttrace = new TextField(p.getProperty("trace"));
    ttrace.addActionListener(this);
    ttrace.addFocusListener(this);
    p6.add(makePanel("Trace",ttrace));
    p0.add(p6);

    Panel p7 = new Panel();
    p7.setLayout(new FlowLayout());
    toutfile = new TextField(p.getProperty("outfile"));
    toutfile.addActionListener(this);
    toutfile.addFocusListener(this);
    p7.add(makePanel("Output file name",toutfile));
    p0.add(p7);

    add("North",p0);
    getData = new Button("Get Data");
    getData.addActionListener(this);
    dismiss = new Button("Dismiss");
    dismiss.addActionListener(this);

    Panel bpanel = new Panel();
    bpanel.setLayout(new FlowLayout(FlowLayout.CENTER,30,5));
    bpanel.add(getData);
    bpanel.add(dismiss);

    Panel pb = new Panel();
    pb.setLayout(new GridLayout(2,1,1,2));
    help = new Label("Help messages will appear here along with default values, etc.");
    help.setBackground(Color.black);
    help.setForeground(Color.white);
    pb.add(bpanel);
    pb.add(help);

    add("South",pb);

    resize(400,600);
    show();
  }

  Panel makePanel(String label, TextField tf) {
    Panel p = new Panel();
    p.setLayout(new GridLayout(2,1,1,1));
    Label lab = new Label(label,Label.CENTER);
    p.add(lab);
    p.add(tf);
    return p;
  }

  void putProp(Properties p, String name, String value) {
    if (value == null) return;
    if (value.length() < 1) return;
    p.put(name,value);
    return;
  }

  public void actionPerformed(ActionEvent e) {
    System.out.println("event:"+e);
    Object source = e.getSource();
    if (source.equals(dismiss)) {
      dispose();
      System.exit(0);
    }

    if (source.equals(getData)) {
      p = new Properties();
      putProp(p,"host",thost.getText().trim());
      putProp(p,"group",tgroup.getText().trim());
      putProp(p,"descr",tdescr.getText().trim());
      putProp(p,"user",tuser.getText().trim());
      putProp(p,"proj",tproj.getText().trim());
      putProp(p,"trace",ttrace.getText().trim());
      putProp(p,"band",tband.getText().trim());
      putProp(p,"mag",tmag.getText().trim());
      putProp(p,"linele",tlinele.getText().trim());
      putProp(p,"place",tplace.getText().trim());
      putProp(p,"pos",tpos.getText().trim());
      putProp(p,"size",tsize.getText().trim());
      putProp(p,"unit",tunit.getText().trim());
      putProp(p,"spac",tspac.getText().trim());
      putProp(p,"doc",tdoc.getText().trim());
      putProp(p,"latlon",tlatlon.getText().trim());
      putProp(p,"aux",taux.getText().trim());
      putProp(p,"time",ttime.getText().trim());
      putProp(p,"day",tday.getText().trim());
      putProp(p,"cal",tcal.getText().trim());
      putProp(p,"id",tid.getText().trim());
      putProp(p,"outfile",toutfile.getText().trim());
      parent.doRequest(p);
    }
  }

  public void focusGained(FocusEvent e) {
    System.out.println("focus event:"+e);
    Object s = e.getSource();
    if (s.equals(thost)) {
      help.setText("host name (eg, adde.unidata.ucar.edu)");
    } else if (s.equals(tgroup)) {
      help.setText("group name (eg, EASTL)");
    } else if (s.equals(tdescr)) {
      help.setText("group descriptor (eg, CONUS)");
    } else if (s.equals(tpos)) {
      help.setText("position in dataset (0 = now, <0 = relative, >0 = absolute)");
    } else if (s.equals(tuser)) {
      help.setText("user name (required by some systems)");
    } else if (s.equals(tproj)) {
      help.setText("project number (required by some systems)");
    } else if (s.equals(ttrace)) {
      help.setText("server trace flag; 1=full trace ON");
    } else if (s.equals(tband)) {
      help.setText("image/spectral band number to fetch");
    } else if (s.equals(tmag)) {
      help.setText("maginification factor (lin ele); <0 = reduce resolution");
    } else if (s.equals(tlinele)) {
      help.setText("line and element of original image at point named in 'place' (lin ele)");
    } else if (s.equals(tplace)) {
      help.setText("placement of anchor point (C = center)");
    } else if (s.equals(tsize)) {
      help.setText("number of lines and elements in returned AREA (lin ele)");
    } else if (s.equals(tunit)) {
      help.setText("units of returned values (BRIT = brightness temperature");
    } else if (s.equals(tspac)) {
      help.setText("number of bytes per point (1, 2, or 4)");
    } else if (s.equals(tdoc)) {
      help.setText("if = yes then return documentation block");
    } else if (s.equals(tlatlon)) {
      help.setText("latitude and longitude at point named in 'place' (lat lon)");
    } else if (s.equals(taux)) {
      help.setText("to retrieve the aux block, set value to yes");
    } else if (s.equals(ttime)) {
      help.setText("time of image (hhmmss)");
    } else if (s.equals(tday)) {
      help.setText("day of image (yyyddd)");
    } else if (s.equals(tcal)) {
      help.setText("calibration type to use (eg., VISSR)");
    } else if (s.equals(tid)) {
      help.setText("radar station ID (if image is radar data)");
    } else if (s.equals(toutfile)) {
      help.setText("name of disk file (incld. path) to write AREA into");
    } else {
      help.setText("Set values you want, then click 'Get Data' button");
    }
  }

  public void focusLost(FocusEvent e) {
  }

  public void status(String s) {
    help.setText(s);
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
 *-l   linele=<lin> <ele> <type> line/element to center image on 
 *-n   place=<placement>         placement of lat/lon or linele points (center
 *                               or upperleft (def=center)) 
 *-p   pos=<position>            request an absolute or relative ADDE position
 *                               number
 *-s   size=<lines> <elements>   size of image to be returned (imagedata only)
 *-u   unit=<unit>               to specify calibration units other than the
 *                               default
 *-z   spac=<bytes>              number of bytes per data point, 1, 2, or 4
 *                               (imagedata only)
 *-o   doc=<yes/no>              specify yes to include line documentation
 *                               with image (def=no)
 *-r   latlon=<lat> <lon>        lat/lon point to center image on 
 *-a   aux=<yes/no>              specify yes to include auxilliary information
 *                               with image
 *-i   time=<time1> <time2>      specify the time range of images to select
 *                               (def=latest image if pos not specified)
 *-y   day=<day>                 specify the day of the images to select
 *                               (def=latest image if pos not specified)
 *-c   cal=<cal type>            request a specific calibration on the image
 *                               (imagedata only)
 *-i   id=<stn id>               radar station id
 *-h   host=                     ADDE server hostname or IP address
 *
*/

