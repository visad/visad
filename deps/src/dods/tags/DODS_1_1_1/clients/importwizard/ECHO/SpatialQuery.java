package dods.clients.importwizard.ECHO;

import java.lang.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;
import org.jdom.*;

/** 
 * 
 * 
 *
 * @author Sheila Jiang
 */
 
 public class SpatialQuery 
 {
     private Element[] spatialQuery;
     //private String[] spatialKeywords;

     /**
      * Constructs a <code>SpatialQuery</code>
      */
     public SpatialQuery(){
	 spatialQuery = new Element[2];
	 //spatial=new String[1];
	 //spatialKeywords=new String[1];
     }
     
     /**
      * Returns the elements of spatial query 
      *
      * @return the elements of spatial query
      */
     public Element[] getSpatialQuery(){
	 return spatialQuery;
     }
     
     /**
      * Builds the spatial query elements
      *
      * @param easternmost   the easternmost longitude of the rectangle
      * @param westernmost   the westernmost longitude of the rectangle
      * @param northernmost  the northernmost longitude of the rectangle
      * @param southernmost  the southernmost longitude of the rectangle
      * @param keywords      the spatial keyword list
      */
     public void buildSpatialQuery(String easternmost, String westernmost, 
		    String northernmost, String southernmost, JList keywords)
     {
	 Object[] selected = keywords.getSelectedValues();
	 //
	 // First element -- the rectangle
	 //
	 if (selected.length == 0) {
	     // remove letters in the strings 
	     easternmost = toSigned(easternmost);
	     westernmost = toSigned(westernmost);
	     northernmost = toSigned(northernmost);
	     southernmost = toSigned(southernmost);

	     //
	     spatialQuery[0] = new Element("spatial");
	     spatialQuery[0].setAttribute(new Attribute("operator", "RELATE"));
	 
	     Element iimsBox = new Element("IIMSBox");
	     
	     // First point -- lower left corner
	     Element iimsPoint = new Element("IIMSPoint");
	     Vector attributes = new Vector();
	     Attribute attr = new Attribute("long", easternmost);
	     attributes.add(attr);
	     attr = new Attribute("lat", southernmost);
	     attributes.add(attr);
	     
	     iimsPoint.setAttributes(attributes);
	     iimsBox.addContent(iimsPoint);
	     
	     // Second point -- upper right corner
	     iimsPoint = new Element("IIMSPoint");
	     attributes = new Vector();
	     attr = new Attribute("long", westernmost);
	     attributes.add(attr);
	     attr = new Attribute("lat", northernmost);
	     attributes.add(attr);
	     
	     iimsPoint.setAttributes(attributes);
	     iimsBox.addContent(iimsPoint);
	     
	     // Add iimsBox
	     spatialQuery[0].addContent(iimsBox);
	 }
	 //
	 // Second element -- keywords
	 //
	 else {
	     spatialQuery[1] = new Element("spatialKeywords");
	     Element list = new Element("list");
	     Element value;
	     
	     for (int i=0; i<selected.length; i++){
		 value = new Element("value");
		 value.addContent("'" + selected[i].toString() + "'");
		 list.addContent(value);
	     }
	     spatialQuery[1].addContent(list);
	 }
	 
     }
    
     /**
      * Removes the letter (e, w, n, s) from a long/lat string and adds 
      * corresponding sign  
      *
      * @param input the input long/lat string    
      */
     protected String toSigned(String input) {
	 char end = input.charAt(input.length()-1);
	 switch (end) {
	 case 'e' :
	 case 'E' :
	 case 'n' :
	 case 'N' :
	     input = input.substring(0, input.length()-1);
	     break;

	 case 'w' :
	 case 'W' :
	 case 's' :
	 case 'S' :
	     input = "-" + input.substring(0, input.length()-1);
	     break;
	 default:
	     break;
	 }
	 
	 return input;
     }
	 
     /*
     public String[] getSpatial(){
         return spatial;
     }
     public void setSpatial(String[] theSpatial){ 
	 spatial = theSpatial;
     }
     */
        
 }
