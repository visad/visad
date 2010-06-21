package dods.clients.importwizard;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.SAXParserFactory;  
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import java.util.Vector;

/**
 * This class is used by the SAX parser to turn an XML file containing
 * <Entry_ID> tags into a Vector of Strings representing the IDs.
 */
public class EntryIDHandler extends DefaultHandler {
    
    private boolean insideEntryID;
    private Vector ids;

    public EntryIDHandler() {
	ids = new Vector();
	insideEntryID = false;
    }

    //
    // Default Handler Functions
    //
    public void startElement(String namespaceURI,
			     String lName, // local name
			     String qName, // qualified name
			     Attributes attrs)
	throws SAXException
    {
	if(lName.equals("Entry_ID"))
	    insideEntryID = true;
	else
	    insideEntryID = false;
	
    }
    
    public void endElement(String namespaceURI,
			   String sName, // simple name
			   String qName  // qualified name
			   )
	throws SAXException
    {
	insideEntryID = false;
    }
    
    public void characters(char buf[], int offset, int len)
	throws SAXException
    {
	if(insideEntryID) {
	    String s = new String(buf, offset, len);
	    ids.addElement(s);
	}
    }

    //
    // Other Functions
    //

    /** 
     * Returns the Vector of IDs.
     * @return the Vector of IDs.
     */
    public Vector getIDs() {
	return ids;
    }

}

