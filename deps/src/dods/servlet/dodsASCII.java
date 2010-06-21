/////////////////////////////////////////////////////////////////////////////
// Copyright (c) 1999, COAS, Oregon State University
// ALL RIGHTS RESERVED.   U.S. Government Sponsorship acknowledged.
//
// Please read the full copyright notice in the file COPYRIGHT
// in this directory.
//
// Author: Nathan Potter (ndp@oce.orst.edu)
//
//                        College of Oceanic and Atmospheric Scieneces
//                        Oregon State University
//                        104 Ocean. Admin. Bldg.
//                        Corvallis, OR 97331-5503
//
/////////////////////////////////////////////////////////////////////////////

/* $Id: dodsASCII.java,v 1.3 2004-02-06 15:23:50 donm Exp $
*
*/

package dods.servlet;

import java.io.*;
import java.text.*;
import java.net.MalformedURLException;
import java.util.*;
import java.util.zip.DeflaterOutputStream;
import javax.servlet.*;
import javax.servlet.http.*;

import dods.dap.*;
import dods.util.*;
import dods.servers.ascii.*;
import dods.dap.parser.ParseException;

/**
* Default handler for DODS ascii requests. This class is used
* by DODSServlet. This code exists as a seperate class in order to alleviate
* code bloat in the DODSServlet class. As such, it contains virtually no
* state, just behaviors.
*
* @author Nathan David Potter
*/

public class dodsASCII {

    private static final boolean _Debug = false;


    /***************************************************************************
    * Default handler for DODS ascii requests. Returns DODS data in
    * comma delimited ascii columns for ingestion into some not so
    * DODS enabled application such as MS-Excel. Accepts constraint expressions
    * in exactly the same way as the regular DODS dataserver.
    *
    *
    * @param dServ The DODSServlet object containing the correct getDDS(),
    * getDAS), and getServerName() methods for the dataSet.
    */
    public void sendASCII(HttpServletRequest request,
                          HttpServletResponse response,
			  String dataSet)
                          throws DODSException, ParseException {

        if (Debug.isSet("showResponse")) 
            System.out.println("Sending DODS ASCII Data For: " + dataSet +
			       "    CE: '" + request.getQueryString()+ "'");


        String requestURL, ce;
	DConnect url;
	DataDDS dds;

	if(request.getQueryString() == null){
	    ce = "";
        }
	else {
	    ce = "?" + request.getQueryString();
        }

	int suffixIndex = HttpUtils.getRequestURL(request).toString().lastIndexOf(".");

	requestURL = HttpUtils.getRequestURL(request).substring(0,suffixIndex);

        if (Debug.isSet("showResponse")) {
	    System.out.println("New Request URL Resource: '" + requestURL +"'");
	    System.out.println("New Request Constraint Expression: '" + ce +"'");
        }
	
	try {
	
	    if(_Debug) System.out.println("Making connection to .dods service...");
	    url = new DConnect(requestURL, true);
	    
	    if(_Debug) System.out.println("Requesting data...");
            dds = url.getData(ce,null,new asciiFactory());
	    
            if (_Debug) System.out.println(" ASC DDS: ");
            if (_Debug) dds.print(System.out);

            PrintWriter pw = new PrintWriter(response.getOutputStream());
            PrintWriter pwDebug = new PrintWriter(System.out);

	    //pw.println("<pre>");
	    dds.print(pw);
	    pw.println("---------------------------------------------");


            String s = "";
            Enumeration e = dds.getVariables();

            while(e.hasMoreElements()){
                BaseType bt = (BaseType)e.nextElement();
                if (_Debug) ((toASCII)bt).toASCII(pwDebug,true,null,true);
                //bt.toASCII(pw,addName,getNAme(),true);
                ((toASCII)bt).toASCII(pw,true,null,true);
            }

	    //pw.println("</pre>");
	    pw.flush();
            if (_Debug) pwDebug.flush();

        }
	catch (FileNotFoundException fnfe){
	    System.out.println("OUCH! FileNotFoundException: " + fnfe.getMessage());
	    fnfe.printStackTrace(System.out);
	}
	catch(MalformedURLException mue){
	    System.out.println("OUCH! MalformedURLException: " + mue.getMessage());
	    mue.printStackTrace(System.out);
	}
	catch (IOException ioe){
	    System.out.println("OUCH! IOException: " + ioe.getMessage());
	    ioe.printStackTrace(System.out);
	}

        if (_Debug) System.out.println(" dodsASCII done");
    }
    /***************************************************************************/





}



