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
 
 public class TemporalQuery 
 {
     private String[] temporal;
     private String[] temporalKeywords;

     public TemporalQuery(){
	 temporal = new String[1];
	 temporalKeywords = new String[1];
     }
     
     public String[] getTemporal(){
         return temporal;
     }
     public void setTemporal(String[] theTemporal){ 
	 temporal = theTemporal;
     }
        
 }
