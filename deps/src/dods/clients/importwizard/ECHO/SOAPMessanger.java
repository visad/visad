package dods.clients.importwizard.ECHO;

import java.lang.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;
import org.jdom.*;
import org.jdom.output.XMLOutputter;
import org.jdom.input.DOMBuilder;
import gov.nasa.echo.soap.*;
import java.io.*;

/** 
 * 
 * 
 *
 * @author Sheila Jiang
 */
 
 public class SOAPMessanger
 {
     private Document outXMLDoc;

     public Document exeQuery(Document xmlDoc){
	 
	 //convert xmlDoc to a string
	 XMLOutputter myXMLOutputter = new XMLOutputter();
	 String XMLMessage = myXMLOutputter.outputString(xmlDoc); 
	 
	 //wrap the xml up into SOAP and send it
	 try {
	     //create ECHO SOAP object
	     EchoSOAPProxy echoRef = new EchoSOAPProxy("http://fosters.gsfc.nasa.gov:4300/soap/servlet/rpcrouter");
	     
	     //perform XML transaction on ECHO
	     String response = echoRef.perform(XMLMessage);
	     
	     //convert the response to a Document object
	     ByteArrayInputStream inputStream = new ByteArrayInputStream(response.getBytes());
	     DOMBuilder domBuilder = new DOMBuilder(false);
	     outXMLDoc = domBuilder.build(inputStream);

	     //System.out.println("\nResult from your XML trasaction:");
	     //System.out.println(response);
	 } catch(java.net.MalformedURLException ex){
	     System.err.println("\nRoutering ECHO failed.");
	     System.err.println(ex.getMessage());
	 } catch(EchoSOAPException ex){
	     System.err.println("\nXML Transaction to ECHO failed.");
	     System.err.println(ex.getMessage());
	 } catch(JDOMException ex){
	     System.err.println("\nXML response convertion to Document failed.");
	     System.err.println(ex.getMessage());
         } 
         return outXMLDoc;
     }
	     
 }
