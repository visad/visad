package dods.clients.matlab;
import java.lang.*;
import java.util.*;
import java.io.*;
import dods.dap.*;

/** 
 * Provides an interface to the Java DAP library to be used by matlab
 * @version 1.0
 * @author rhonhart
 * Modified by Long Yan. add GUI function
 */

public class MatlabDods extends Object {
    static protected DAS das = null;
    static protected DDS dds = null;
    static protected DConnect connect = null;

    /**
     * Construct a new <code>MatlabDods</code> with dods url <code>url</code>.
     * @param url The dods url (without ce) to connect to
     */
    public MatlabDods(String url) throws java.io.FileNotFoundException {
	
	try {
	    connect = new DConnect(url);
	}
	catch(java.io.FileNotFoundException e) {
	    throw(e);
	}
    }
    
    /**
     * Download the dods data and store it locally.
     * @param ce The constraint expression to use when downloading the dataset.
     */
    public void downloadData(String ce) throws Exception {
	StatusUI ui = null;
	MatlabFactory fac = new MatlabFactory();

	try {
	    dds = connect.getData(ce, ui, fac);
	}
	catch(Exception e) {
	    throw(e);
	}
       
    }

    /** Download the dods DAS and store it locally. */
    public void downloadDDS() throws Exception {
	StatusUI ui = null;
	MatlabFactory fac = new MatlabFactory();

	try {
	    dds = connect.getDDS();
	}
	catch(Exception e) {
	    throw(e);
	}
       
    }
    /** Download the dods DAS and store it locally. */
    public void downloadDAS() throws Exception {
	try {
	    das = connect.getDAS();
	}
	catch(Exception e) {
	    throw(e);
	}
    }

    /** Print the DDS of the dataset */
    public void printDDS() throws Exception {
	if(dds != null)
	    dds.print(System.out);
	else 
	    System.err.println("The DDS must be retrieved before it can be printed");
    }

    public String printDDSToString() {
	ByteArrayOutputStream out = new ByteArrayOutputStream();
	PrintWriter stringOut = new PrintWriter(out);
	if(dds != null)
	    dds.print(stringOut);
	else 
	    System.err.println("The DDS must be retrieved before it can be printed");

	stringOut.close();
	return out.toString();
    }

    public String printDASToString() {
	ByteArrayOutputStream out = new ByteArrayOutputStream();
	PrintWriter stringOut = new PrintWriter(out);
	if(das != null)
	    das.print(stringOut);
	else 
	    System.err.println("The DDS must be retrieved before it can be printed");

	stringOut.close();
	return out.toString();
    }
    /** 
     * Return the number of variables in the dataset
     *   (0 if the data hasn't been downloaded yet).
     * @return the number of variables in the dataset.
     */
    public int numVariables() {
	if(dds != null) 
	    return dds.numVariables();
	else 
	    return 0;
    }

    /**
     * Return an Enumeration of the variables in the dataset.
     * @return an Enumeration of the variables in the dataset.
     */
    public Enumeration getVariables() {
	if(dds != null) 
	    return dds.getVariables();
	else
	    return null;

    }

    /** 
     * Return an Enumeration of the names of the Attr tables in the dataset.
     * @return Enumeration of the names of the Attribute tables in the dataset.
     */
    public Enumeration getAttrTableNames() {
	if(das != null) 
	    return das.getNames();
	else
	    return null;
    }

    /**
     * Return the <code>AttributeTable</code> with name <code>name</code>.
     * @param name The name of the attribute
     * @return the <code>AttributeTable</code> with name <code>name</code>.
     */
    public AttributeTable getAttrTable(String name) {
	if(das != null)
	    return das.getAttributeTable(name);
	else 
	    return null;
    }
};
