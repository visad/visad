package dods.clients.importwizard;

import javax.swing.*;

public abstract class DataFormatSelector extends JPanel {
    public DataFormatSelector() {

    }

    /** 
     * Every data format selector must implement the getURLs function
     * It should return an array of fully constrained Dods URLs.
     */
    public abstract DodsURL[] getURLs();

    
    /** 
     * Set the URLs.
     */
    public abstract void setURLs(DodsURL[] urls);

    /**
     * If the urls need to be assigned to specific variables, return the 
     * names of those variables as an array of strings
     */
    public String[] getNames() {
	return null;
    }

    /**
     * If the Dods client needs certain options to load the URLs, return
     * them as an array of strings using this function.  If the same option
     * is used for every URL, it may be returned as the one and only element
     * in the array.
     */
    public String[] getOptions() {
	return null;
    }

    /** 
     * This function tells the <code>DataFormatSelector</code> to do 
     * whatever needs to be done to get the URLs to the user.
     */
    public void outputURLs() {

    }
}




