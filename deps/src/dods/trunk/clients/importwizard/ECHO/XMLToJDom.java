package dods.clients.importwizard.ECHO;

import java.lang.*;
import javax.swing.*;
import java.awt.event.*;
//import java.awt.*;
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
 
 public class XMLToJDom
 {
     private Document outXMLDoc;

     public Document convert(File xmlFile){
	 //String XMLMessage = myXMLOutputter.outputString(xmlDoc); 
	 
	 try {
	     DOMBuilder domBuilder = new DOMBuilder(false);
	     outXMLDoc = domBuilder.build(xmlFile);
	 } catch(JDOMException ex){
	     System.err.println("\nXML file convertion to Document failed.");
	     System.err.println(ex.getMessage());
         } 
         return outXMLDoc;
     }

     public Document getDoc(){
	 return outXMLDoc;
     }

     public static void main(String[] argv) {
	 File xml;
	 XMLToJDom test = new XMLToJDom();
	 Vector queryValids = new Vector();
	 //convert xmlFile to a JDOM document
	 try {
	     xml = new File("/home/DODS/Java-DODS/ECHO_static_valids.xml");
	     test.convert(xml);
	 } catch(NullPointerException ex){
	     System.err.println("\n File doesn't exist.");
	     System.err.println(ex.getMessage());
         } 
	 
	 //convert the JDOM document back to a string
	 XMLOutputter myXMLOutputter = new XMLOutputter();
	 //String xmlMessage = myXMLOutputter.outputString(test.getDoc());
	 Element root = (test.getDoc()).getRootElement();
	 List rootChildren = root.getChildren();
	 //Object it = theList.get(4);
	 //System.out.println(it.toString());
	 String[] categoryName = {"archiveCenter", "campaign", "sensorName"};
	 for (int i=0; i<categoryName.length; i++) {
	     for (int j=0; j<rootChildren.size(); j++) {
		 Element category = (Element)rootChildren.get(j);
		 //Vector valids = new Vector();
		 String[] valids;
		 System.out.println(j+"  " + category.getChildText("CategoryName"));
		 System.out.println(j+"  " + categoryName[i]);
		 if ((category.getChildText("CategoryName")).equals(categoryName[i])) {
		     Element criteriaValues = category.getChild("CriteriaList").getChild("Criteria").getChild("CriteriaValues");
		     java.util.List values = criteriaValues.getChildren(); //<CriteriaValue>
		     System.out.println("Debug");
		     valids = new String[values.size()];
		     for (int k=0; k<values.size(); k++) {
			 String theValue = ((Element)values.get(k)).getText();
			 System.out.println(theValue);
			 //valids.addElement(theValue);
			 valids[k] = theValue;
		     }
		     CollectionValids theCategory = new CollectionValids(categoryName[i], valids);
		     queryValids.addElement(theCategory);
		     System.out.println(theCategory.getName());
		     System.out.println(theCategory.getValids().length);
		     System.out.println(theCategory.getValids()[2]);
		 }
	     }
	 }  
     }
     
 }
