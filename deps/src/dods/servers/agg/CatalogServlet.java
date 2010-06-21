// $Id: CatalogServlet.java,v 1.3 2004-02-06 15:23:49 donm Exp $
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

package dods.servers.agg;

import dods.dap.*;
import dods.dap.Server.ServerDDS;
import dods.dap.parser.ParseException;
import dods.util.iniFile;
import dods.util.Debug;

import dods.servers.netcdf.NcDataset;
import dods.servlet.DODSServlet;
import dods.servlet.GuardedDataset;
import dods.servlet.requestState;

import thredds.catalog.AggServerConfig;
import thredds.catalog.InvCatalog;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

//import javax.xml.transform.*;
//import javax.xml.transform.stream.StreamSource;
//import javax.xml.transform.stream.StreamResult;

/***************************************************************************
* This servlet uses an InventoryCatalog XML files to list what datasets it can serve.
* It can currently 1) serve netcdf files and 2) aggregate files into logical datasets using
* files from other DODS servers or from its own set of netcdf files. (It will be possible
* to serve other kinds of files later).
*
* The configuration file is kept in "$user_home/DODSagg.ini"; it should have one or both of
* the following sections:
*
* <pre>
* [Catalog]
* catalogURL        =  the xml catalog URL
* maxDatasetsCached =  number of datasets to cache; old ones are closed using an LRU algorithm.
* dataRoot          =  data root directory for netcdf files
* </pre>
*
* @author John Caron
* @version $Id: CatalogServlet.java,v 1.3 2004-02-06 15:23:49 donm Exp $
*/

public class CatalogServlet extends DODSServlet {

  private static final boolean debugInit = false;
  private static final boolean debugLocks = false;
  private static final boolean debugDIR = false;
  private static final boolean debugOpen = false;
  private static final boolean showCatalog = false;
  private static final boolean showServerInfo = false;

  private static final boolean runWithJBuilder = false; // debugging

  private String displayName = "DODS Aggregation/Catalog Server";
  private String serverURL = "http://localhost:8080/servlet/dods.servers.agg.CatalogServlet/"; // debugging
  private String configURL = null;
  private int maxCatalogDatasetsCached = 0;
  private int maxAggDatasetsCached = 10;
  private int maxDODSDatasetsCached = 100;
  private int maxNetcdfFilesCached = 100;

  private String catalogPage;
  private AggServerConfig catalog;

  public void init() throws javax.servlet.ServletException {
    //System.setProperty("javax.xml.parsers.SAXParserFactory", "org.apache.xerces.jaxp.SAXParserFactoryImpl");

    super.init();

    if (showServerInfo) {
      System.out.println("--------------------");
      System.out.println("getServletName() = "+getServletName());
      System.out.println("getServletInfo() = "+getServletInfo());

      System.out.println("InitParameters = ");
      java.util.Enumeration params = getInitParameterNames();
      while (params.hasMoreElements()) {
        String p = (String) params.nextElement();
        System.out.println(" "+p+" == "+getInitParameter(p));
      }

      ServletContext sc = getServletContext();
      System.out.println("ServletContext "+sc);
      System.out.println(" getServerInfo() = "+sc.getServerInfo());
      System.out.println(" getMajorVersion() = "+sc.getMajorVersion());
      System.out.println(" getMinorVersion() = "+sc.getMinorVersion());
      System.out.println(" getRealPath() = "+sc.getRealPath("/"));

      System.out.println(" ServletContext Attributes = ");
      java.util.Enumeration attNames = sc.getAttributeNames();
      while (attNames.hasMoreElements()) {
        String att = (String) attNames.nextElement();
        System.out.println("  "+att+" == "+sc.getAttribute(att));
      }

      System.out.println(" ServletContext InitParameters = ");
      params = sc.getInitParameterNames();
      while (params.hasMoreElements()) {
        String p = (String) params.nextElement();
        System.out.println("  "+p+" == "+sc.getInitParameter(p));
      }

      ServletConfig sg = getServletConfig();
      System.out.println("ServletConfig "+sg);
      System.out.println(" Context "+sg.getServletContext());
      System.out.println(" Config InitParameters = ");
      params = sg.getInitParameterNames();
      while (params.hasMoreElements()) {
        String p = (String) params.nextElement();
        System.out.println("  "+p+" == "+sg.getInitParameter(p));
      }
      System.out.println("--------------------");
    }

    String docRoot = getServletContext().getRealPath("/");
    if (debugInit) System.out.println("Root = "+ docRoot);

    try {
      File root =  new File(".");
      if (debugInit) System.out.println("Test PWD File = "+ root.getAbsolutePath());
      if (debugInit) System.out.println("PWD File = "+ root.getCanonicalPath());
  } catch (Exception e ) {
    e.printStackTrace();
  }

    // set up the AggDataset cache
    String p = getInitParameter("maxAggDatasetsCached");
    if (p != null) {
      try {
        maxAggDatasetsCached = Integer.parseInt(p);
        if (debugInit) System.out.println(" maxAggDatasetsCached = "+maxAggDatasetsCached);
      } catch (NumberFormatException e) {
        System.out.println(" maxAggDatasetsCached bad number format in web.xml; use value "+maxDODSDatasetsCached);
      }
    }
    AggDataset.setCacheMax(maxAggDatasetsCached);

    // set up the dodsDataset cache
    p = getInitParameter("maxDODSDatasetsCached");
    if (p != null) {
      try {
        maxDODSDatasetsCached = Integer.parseInt(p);
        if (debugInit) System.out.println(" maxDODSDatasetsCached = "+maxDODSDatasetsCached);
      } catch (NumberFormatException e) {
        System.out.println(" maxDODSDatasetsCached bad number format in web.xml; use value "+maxDODSDatasetsCached);
      }
    }
    DODSDataset.setCacheMax(maxDODSDatasetsCached);

    // set up the NetcdfFile cache
    p = getInitParameter("maxNetcdfFilesCached");
    if (p != null) {
      try {
        maxNetcdfFilesCached = Integer.parseInt(p);
        if (debugInit) System.out.println(" maxNetcdfFilesCached = "+maxNetcdfFilesCached);
      } catch (NumberFormatException e) {
        System.out.println(" maxNetcdfFilesCached bad number format in web.xml; use value "+maxNetcdfFilesCached);
      }
    }
    NcDataset.setCacheMax(maxNetcdfFilesCached);

    // read the config file
    String configURL = getInitParameter("serverConfig");
    if (configURL != null) {
      try {
        catalog = new AggServerConfig( configURL);
      } catch (java.net.MalformedURLException e) {
        System.out.println("CatalogServlet reading xml config file: MalformedURLException: "+ configURL);
        throw new IllegalStateException(e.getMessage());
      } catch (FileNotFoundException e) {
        System.out.println("CatalogServlet reading xml config file: URL not found: "+ configURL);
        throw new IllegalStateException(e.getMessage());
      } catch (IOException e) {
        System.out.println("CatalogServlet: error reading xml config file: "+ configURL+"/n"+e);
        e.printStackTrace();
        throw new IllegalStateException(e.getMessage());
      }
      if (showCatalog) System.out.println("Parsed Catalog =\n"+catalog.dump());
    }

    // other parameters
    String dn = getInitParameter("displayName");
    if (dn != null)
      displayName = dn;


    if (debugInit) System.out.println("CatalogServlet init done");

    // Create the HTML page once
    //catalogPage = doTransform( configURL, "http://www.unidata.ucar.edu/projects/THREDDS/xml/AggServerConfig.0.4.xsl");
  }

  public String getServerVersion() { return "CatalogAggServer/0.2"; }

  protected void printCatalog(PrintWriter pw) throws IOException {
    String xml = catalog.getSerializedForm(false);
    // System.out.println( xml);
    pw.write( xml);
  }

      // to be overridden by servers that implement status report
    protected void printStatus(PrintWriter os) throws IOException {
      super.printStatus( os);

      os.println("<li># Agg files in cache= "+AggDataset.getCacheSize());
      Iterator iter = AggDataset.getCache();
      while (iter.hasNext()) {
        Dataset ds = (Dataset) iter.next();
        os.println("<p>-  "+ds.getInternalPath()+" "+ds.whoHasLock());
      }

      os.println("<li># DODS files in cache= "+DODSDataset.getCacheSize());
      iter = DODSDataset.getCache();
      while (iter.hasNext()) {
        Dataset ds = (Dataset) iter.next();
        os.println("<p>-  "+ds.getInternalPath()+" "+ds.whoHasLock());
      }

      os.println("<li># Netcdf files in cache= "+NcDataset.getCacheSize());
      iter = NcDataset.getCache();
      while (iter.hasNext()) {
        NcDataset ds = (NcDataset) iter.next();
        ucar.nc2.NetcdfFile ncfile = ds.getNetcdfFile();
        os.println("<p>-  "+ncfile.getPathName()+" "+ds.whoHasLock());
      }

    }

   /* private String doTransform( String xmlURL, String styleSheet) {
    //throws TransformerException, TransformerConfigurationException,
    //       FileNotFoundException, IOException {


    System.out.println(" XSLT transform = "+xmlURL+" "+styleSheet);

    // debug
    javax.xml.parsers.SAXParserFactory factory = null;
    try {
      factory = javax.xml.parsers.SAXParserFactory.newInstance();
      System.out.println(" SAXParserFactory class = "+factory.getClass().getName());
    } catch( Error e) {
      System.out.println(" SAXParserFactory Exception "+e);
      e.printStackTrace();
    }

    try {
      javax.xml.parsers.SAXParser test = factory.newSAXParser();
      System.out.println(" SAXParser class = "+test.getClass().getName()
                           +" "+test.getClass().getClassLoader());

      System.out.println("  Namespaces = "+test.isNamespaceAware()+
                            " Validate = "+test.isValidating());
    } catch( Exception e) {
      System.out.println(" SAXParser Exception "+e);
      e.printStackTrace();
    }

    ByteArrayOutputStream bos = new ByteArrayOutputStream(10000);
    Transformer transformer = null;
    try {
      TransformerFactory tFactory = TransformerFactory.newInstance();
      System.out.println(" TransformerFactory class = "+tFactory.getClass().getName());
      transformer = tFactory.newTransformer(new StreamSource(styleSheet));

      //transformer = new org.apache.xalan.transformer.TransformerImpl( new StreamSource(styleSheet));
      System.out.println(" Transformer class = "+transformer.getClass().getName());

      transformer.transform(new StreamSource(xmlURL), new StreamResult(bos));
    } catch (Exception e) {
      System.out.println(" Exception in XSLT = "+styleSheet+" on file "+xmlURL);
      System.out.println(" "+ e.getMessage());
      e.printStackTrace();
      return "Error";
    }
    return bos.toString();
  } */


  /*********************** dataset directory ***************************************************/
  public void doGetDIR(HttpServletRequest request, HttpServletResponse response, requestState rs)
                         throws IOException, ServletException {

    response.setHeader("XDODS-Server", getServerVersion());
    response.setContentType("text/html");
    response.setHeader("Content-Description", "dods_directory");

    if (debugDIR) {
      System.out.println("doGetDIR request = "+ request);
      System.out.println("  URI = "+ request.getRequestURI());
      System.out.println("  ServletPath = "+ request.getServletPath());
      //System.out.println("  serverPath() = "+ getServerPath());
      System.out.println("  URL = "+ HttpUtils.getRequestURL(request));
    }

    //PrintWriter pw = new PrintWriter(response.getOutputStream());
    PrintWriter pw = response.getWriter();
    /*pw.print(catalogPage);
    pw.flush();
  } */

    //String thisServer = HttpUtils.getRequestURL(request).toString();

    pw.println("<html>");
    pw.println("<head>");
    pw.println("<title>DODS Directory</title>");
    pw.println("<meta http-equiv=\"Content-Type\" content=\"text/html\">");
    pw.println("</head>");
    pw.println("<body bgcolor=\"#FFFFFF\">");

    pw.println("<h1>DODS Directory for:</h1>");
    pw.println("<h2>" + displayName+"</h2>");
    pw.println("<hr>");

    //pw.println("<table border=\"0\">");

    InvCatalog.Collection root = catalog.getRootCollection();
    showCollection( pw, root);

    //pw.println("</table>");
    pw.println("<hr>");
    pw.println("</html>");
    pw.flush();
  }

  private void showCollection(PrintWriter pw, InvCatalog.Collection c) {

    ArrayList datasets = c.getDatasets();
    if (datasets.size() > 0) {
      pw.println("<ul> ");
      for (int i=0; i<datasets.size(); i++) {
        InvCatalog.Dataset ds = (InvCatalog.Dataset) datasets.get(i);
        pw.println("<li> ");
        showDataset(pw, ds);
      }
      pw.println("</ul> ");
    }

      ArrayList collections = c.getCollections();
      if (collections.size() > 0) {
        pw.println("<ul> ");
        for (int i=0; i<collections.size(); i++) {
          InvCatalog.Collection cc = (InvCatalog.Collection) collections.get(i);
          pw.println("<li> " +cc.getName());
          showCollection( pw, cc);
        }
        pw.println("</ul> ");
      }

  }

  private void showDataset(PrintWriter pw, InvCatalog.Dataset dset) {
    String urlName;
    if (runWithJBuilder)
      urlName = serverURL + dset.getURLpath();
    else
      urlName = dset.getURL();

    pw.print("<b>" +dset.getName() + ":</b> ");
    pw.print(" <a href='" + urlName + ".dds'>DDS</a> ");
    pw.print(" <a href='" +urlName + ".das'>DAS</a> ");
    pw.print(" <a href='" +urlName+ ".info'>Information</a> ");
    pw.print(" <a href='" +urlName +".html'>Data Request Form</a> ");
  }

  /************************** dataset caching ************************************************/
  protected GuardedDataset getDataset(requestState preq) throws DODSException, IOException, ParseException {
    Dataset ds = null;
    String urlPath = preq.getDataSet();

    // open it
    InvCatalog.Dataset invDS = catalog.findDatasetByURLpath( urlPath);
    if (invDS == null) throw new IOException( "Dataset not found in catalog; dataURL= <"+urlPath+">");

    if (invDS.getUserObj() != null) { // its an agg dataset
       try {
        if (debugOpen) System.out.println("CatalogServlet try to acquire Agg = "+urlPath);
        ds = AggDataset.acquire(urlPath, invDS.getLocalPath(), invDS);
        if (debugOpen) System.out.println("   acquire is ok = "+urlPath);
      } catch (IOException e) {
        System.out.println("CatalogServlet ERROR opening AggDataset "+urlPath);
        e.printStackTrace();
        throw new DODSException("CatalogServlet ERROR opening AggDataset "+urlPath);
      }
    } else  {   // otherwise its a netcdf dataset
       try {
        if (debugOpen) System.out.println("CatalogServlet try to acquire Nc = "+urlPath);
        ds = NcDataset.acquire(urlPath, invDS.getLocalPath(), invDS, true);
      } catch (IOException e) {
        System.out.println("CatalogServlet ERROR opening NcDataset "+urlPath);
        throw new DODSException("CatalogServlet ERROR opening NcDataset "+urlPath);
      }
    }

    if (debugLocks && !ds.isLockedByMe()) {
      System.out.println("CatalogServlet Dataset NOT LOCKED "+urlPath);
      throw new RuntimeException("CatalogServlet Dataset NOT LOCKED "+urlPath);
    }

    return ds;
  }

}

/* Change History:
   $Log: not supported by cvs2svn $
   Revision 1.10  2002/02/25 16:35:35  caron
   ServletException int

   Revision 1.9  2002/02/25 15:49:51  caron
   file path debug

   Revision 1.8  2001/11/16 01:04:46  caron
   doGetDir() params

   Revision 1.7  2001/11/05 17:53:47  caron
   requestState

   Revision 1.6  2001/10/26 19:07:10  caron
   getClientDDS()

   Revision 1.5  2001/10/24 23:00:47  ndp
   added Makefile

   Revision 1.4  2001/10/24 22:49:57  ndp
   *** empty log message ***

   Revision 1.3  2001/10/12 21:21:12  caron
   JC: use ParsedRequest

   Revision 1.2  2001/10/03 22:47:33  caron
   serverVersion

   Revision 1.1.1.1  2001/09/26 15:36:47  caron
   checkin beta1


 */
