package dods.clients.importwizard;

import java.lang.*;

/**
 * A <code>DodsURL</code> stores information about a Dods URL.  More 
 * specifially, it stores the base url, the constraint expression, the
 * type of URL, and what class is need to get further information from
 * the URL.  It should be used over <code>String</code> to represent 
 * Dods URLs whenever possible.
 *
 * @author rhonhart
 */
public class DodsURL {
    public static int DATA_URL = 0;
    public static int DIRECTORY_URL = 1;
    public static int CATALOG_URL = 2;

    protected static String DEFAULT_DATA_PROCESSOR = "dods.clients.importwizard.CEGenerator";
    protected static String DEFAULT_DIRECTORY_PROCESSOR = "dods.clients.importwizard.DodsDirectory";
    protected static String DEFAULT_CATALOG_PROCESSOR = "dods.clients.importwizard.Inventory";

    private String url;
    private String ce;
    private int urlType;
    private boolean processed;
    private String urlProcessorName;
    private String title;

    /**
     * Create an empty <code>DodsURL</code>.
     */
    public DodsURL() {
	url = "";
	ce = "";
	urlType = 0;
	processed = false;
	urlProcessorName = null;
	title = null;
    }
    
    /**
     * Create a <code>DodsURL</code> by copying an existing <code>DodsURL</code>
     * @param dodsURL The url to copy.
     */
    public DodsURL(DodsURL dodsURL) {
	url = dodsURL.url;
	ce = dodsURL.ce;
	urlType = dodsURL.urlType;
	processed = dodsURL.processed;
	urlProcessorName = dodsURL.urlProcessorName;
	title = null;
    }

    /**
     * Create a <code>DodsURL</code> with a specific base URL and constraint
     * expression.  This url is assumed to be a DATA_URL, and it uses the
     * default DATA_URL processor.
     * @param dodsURL The base url.
     * @param dodsCE The constraint expression.
     */
    public DodsURL(String dodsURL, String dodsCE) {
	url = dodsURL;
	ce = dodsCE;

	// It only makes sense to supply a constraint expression for a 
	// data URL, so we can assume this is a data URL.
	urlType = DATA_URL;
	urlProcessorName = DEFAULT_DATA_PROCESSOR;
	processed = true;
	title = null;
    }

    /** 
     * Create a <code>DodsURL</code> with a specific base URL of type
     * <code>type</code>.  The constraint expression is set to an empty string
     * and the urlProcessor is set to the default for the give type.
     * @param dodsURL The base url.
     * @param type The type of URL.
     */
    public DodsURL(String dodsURL, int type) {
	url = dodsURL;
	ce = "";
	urlType = type;
	processed = false;
	title = null;

	switch(urlType) 
	    {
	    case 0:
		urlProcessorName = DEFAULT_DATA_PROCESSOR;
		break;
	    case 1:
		urlProcessorName = DEFAULT_DIRECTORY_PROCESSOR;
		break;
	    case 2:
		urlProcessorName = DEFAULT_CATALOG_PROCESSOR;
		break;
	    }    
    }
    /** 
     * Create a <code>DodsURL</code> with a specific base URL of type
     * <code>type</code>.  The constraint expression is set to an empty string
     * and the urlProcessor is set to <code>processorName</code>.
     * @param dodsURL The base url.
     * @param type The type of URL.
     * @param processorName The name of the class needed to further process
     *                      the URL.
     */
    public DodsURL(String dodsURL, int type, String processorName) {
	url = dodsURL;
	ce = "";
	urlType = type;
	processed = false;
	urlProcessorName = processorName;
	title = null;
    }

    /**
     * Returns the base URL of the DodsURL.
     * @return the base URL of the DodsURL.
     */
    public String getBaseURL() {
	return url;
    }

    /**
     * Returns the CE of the DodsURL.
     * @return the CE of the DodsURL.
     */
    public String getConstraintExpression() {
	return ce;
    }

    /** 
     * Concatenates the baseURL and the constraint expression to get
     * the full Dods URL.
     * @return a complete Dods URL.
     */
    public String getFullURL() {
	if(ce.length() > 0) 
	    return url + "?" + ce;
	else
	    return url;
    }

    /** 
     * Returns the name of the class needed to further process the URL.
     * @return the name of the class needed to further process the URL.
     */
    public String getProcessorName() {
	return urlProcessorName;
    }  

    /** 
     * Returns the title, if any, of the URL.
     * @return the title, if any, of the URL.
     */

    public String getTitle() {
	return title;
    }

    /** 
     * Returns the type of the URL.
     * @return the type of the URL.
     */
    public int getType() {
	return urlType;
    }

    /**
     * Returns true if the URL has been processed yet, false otherwise.
     * @return true if the URL has been processed yet, false otherwise.
     */
    public boolean hasBeenProcessed() {
	return processed;
    }

    /**
     * Create a string representation of the URL.
     * @return The base URL.
     */
    public String toString() {
	if(title != null) 
	    return title;
	else
	    return getBaseURL();
    }

    /**
     * Set the constraint expression for the URL.
     * @param dodsCE The constraint expression.
     */
    public void setConstraintExpression(String dodsCE) {
	ce = dodsCE;
	if(ce.startsWith("?"))
	    ce = ce.substring(1);
	processed = true;
    }
    
    /** 
     * Set the name of the class needed to process the URL.
     * @param className the name of the class needed to process the URL.
     */
    public void setProcessorName(String className) {
	urlProcessorName = className;
    }

    /**
     * Set whether or not the URL has been processed.  This is used by 
     * the <code>DodsURLList</code> class (as well as others) to let the 
     * user know which urls have had a constraint expression applied
     * @param isDoneProcessing Whether or not the URL has been processed.
     */
    public void setProcessed(boolean isDoneProcessing) {
	processed = isDoneProcessing;
    }

    /** 
     * Set the title of the URL.
     * @param urlTitle The title of the URL.
     */
    public void setTitle(String urlTitle) {
	title = urlTitle;
    }

    /**
     * Set the type of the URL.  Additionally, if no processor has been set,
     * this function will set it to the default processor for type 
     * <code>type</type>.
     * @param type The type of URL.
     */
    public void setType(int type) {
	urlType = type;

	if(urlProcessorName == null) {
	    switch(urlType) 
		{
		case 0:
		    urlProcessorName = DEFAULT_DATA_PROCESSOR;
		    break;
		case 1:
		    urlProcessorName = DEFAULT_DIRECTORY_PROCESSOR;
		    break;
		case 2:
		    urlProcessorName = DEFAULT_CATALOG_PROCESSOR;
		    break;
		}    
	}
    }

    /** 
     * Set the base URL
     * @param dodsURL the base URL.
     */
    public void setURL(String dodsURL) {
	url = dodsURL;
    }
}
