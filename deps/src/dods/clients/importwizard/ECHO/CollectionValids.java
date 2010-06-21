package dods.clients.importwizard.ECHO;

import java.lang.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;
import org.jdom.*;

/** 
 * Manages the valids for discovery search 
 * 
 * @author Sheila Jiang <jiangz@po.gso.uri.edu>
 */
 
 public class CollectionValids extends JList 
     implements ListSelectionListener 
 {
     private boolean selected;
     private String name;
     private String[] valids;
     private boolean[] selection;
    
     /** 
      * Constructs <code>CollectionValids</code>
      * 
      * @param theName the category name of the valids
      * @param theValids the vulues of the valids
      */
     public CollectionValids(String theName, String[] theValids){
	 super(theValids);
	 setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	 addListSelectionListener(this);
	 
	 selected = false;
	 name = theName;
	 valids = theValids;
	 selection = new boolean[valids.length];
	 for (int i=0; i<valids.length; i++){
	     selection[i] = false;
	 }
     }
          
     public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting())
            return;
	
	if (!isSelectionEmpty()) {
	    for(int i=0; i<valids.length;i++){
		if (isSelectedIndex(i)) setSelected(i);
		else deSelect(i);
	    }
	}
     }

     /** 
      * Returns the name of this category
      * 
      * @return the name of this category
      */
     public String getName(){
	 return name;
     }

     /** 
      * Returns <code>true</code> if this category is selected; 
      * <code>false</code> otherwise
      * 
      * @return <code>true</code> if this category is selected;
      *         <code>false</code> otherwise
      */
     public boolean isSelected(){
	 return selected;
     }

     /** 
      * Sets this category to be selected 
      */
     public void setSelected(){
	 selected = true;
     }

     /** 
      * Sets this category to be not selected 
      */
     public void deSelect(){
	 selected = false;
     }

     /** 
      * Returns the valids of this category 
      * 
      * @return the valids of this category
      */
     public String[] getValids(){
       return valids;
     }

     /** 
      * Sets the valids of this category 
      * 
      * @param theValids the valids of this category
      */
     public void setValids(String[] theValids){ 
	 valids = theValids;
	 setListData(valids);
     }
    
     /** 
      * Returns <code>true</code> if the valid with an index of 
      * <code>index</code> is selected; <code>false</code> otherwise
      * 
      * @return <code>true</code> if the valid with an index of
      *         <code>index</code> is selected; 
      *         <code>false</code> otherwise
      */
     public boolean getSelection(int index){
	 return selection[index];
     }
     
     /** 
      * Sets the valid with an index of <code>index</code> to be selected
      * 
      * @param index the index of the valid to be set selected
      */
     public void setSelected(int index){
	 selection[index] = true;
     }

     /** 
      * Sets the valid with an index of <code>index</code> to be not selected
      * 
      * @param index the index of the valid to be set not selected
      */
     public void deSelect(int index){
	 selection[index] = false;
     }
 }

