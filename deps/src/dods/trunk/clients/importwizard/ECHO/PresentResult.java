
package dods.clients.importwizard.ECHO;

import java.lang.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;
import org.jdom.*;
import org.jdom.output.XMLOutputter;
import org.jdom.input.DOMBuilder;
import java.io.*;

/** 
 *
 * @author Sheila Jiang
 */
 
public class PresentResult
{
    private Element[] resultDetail;
    private Vector valids;
     
    public PresentResult() { //(Vector resultValids){
	resultDetail = new Element[3];
	valids = new Vector(); //resultValids;
	//buildResultDetail(); 
    }
     
    public Element[] getResultDetail(){
	return resultDetail;
    }
     
    // this method builds the result detail as JDOM objects first, then
    // converts them to a string  
    public void buildResultDetail(Vector resultValids){
	valids = resultValids;

	// IteratorSize
	resultDetail[0] = new Element("IteratorSize");
	resultDetail[0].addContent("10");// ? changeable
 
	// Cursor
	resultDetail[1] = new Element("Cursor");
	resultDetail[1].addContent("1");// ?
	 
	// PresentationDescription
	resultDetail[2] = new Element("PresentationDescription");
	 
	// Tuple types
	for (int i=0; i<valids.size(); i++) {
	    Element tupleType = new Element("TupleType");
	    Element attribute = new Element("attributeName");
	    attribute.addContent((String)valids.elementAt(i));
	    Element typeName = new Element("PrimitiveTypeName");
	    typeName.addContent(new Element("String"));//? int sometimes
	    tupleType.addContent(attribute);
	    tupleType.addContent(typeName);
	    resultDetail[2].addContent(tupleType);
	}

	// PredefinedPresentationType
	Element predefined = new Element("PredefinedPresentationType");
	predefined.addContent(new Element("FULL"));

	// add to PresentationDescription
	resultDetail[2].addContent(predefined);
    }

    public void displayResult(Document resultDoc, boolean isDiscovery) {
	JScrollPane resultPane; 
	if (valids.isEmpty()) {
	    JLabel nothing = new JLabel("Nothing returned!");
	    resultPane = new JScrollPane(nothing);
	}
	else {
	    Vector data = new Vector();
	    
	    try {
		Element root = resultDoc.getRootElement();
		Element resultType = (Element)root.getChild("QueryResponse").getChild("BooleanResult").getChild("BooleanResultType");
		// if discovery query secceeded, continue; otherwise display error message  
		if (!resultType.getChildren("REQUEST_SUCCEEDED").isEmpty()) {
		    Element payload = (Element)root.getChild("QueryResponse").getChild("ReturnData").getChild("payload");
	     
		    //convert CDATA in payload to a file
		    String resultStr = payload.getText();
		    File temp = new File("temp");
		    StringReader reader = new StringReader(resultStr);
		    FileWriter writer = new FileWriter(temp);
		    int c;

		    while ((c = reader.read()) != -1)
			writer.write(c);

		    writer.close();

		    //convert file to document
		    DOMBuilder domBuilder = new DOMBuilder(false);
		    Document result =  domBuilder.build(temp);
	     
		    //temp.delete();

		    //System.out.println(" new root is: " + result.getRootElement().getName());
		    if (!result.getRootElement().getChildren().isEmpty()) {
			java.util.List collections = result.getRootElement().getChild("provider").getChildren(); //<collection> or <granule>
			System.out.println(" collection size is: " + collections.size());
			 
			for (int i=0; i<collections.size(); i++) {
			    Vector record = new Vector();//stores the values for one record
			    //System.out.println("size = " + collections.size());
			    Element thisOne = (Element)collections.get(i);
			    for (int j=0; j<valids.size(); j++) {
				if (!thisOne.getChildren((String)valids.elementAt(j)).isEmpty()) 			 
				    record.addElement(thisOne.getChild((String)valids.elementAt(j)).getText());
				else 
				    record.addElement("");
			    }
			    data.addElement(record.clone()); 
			}
		    }
		}
		else 
		    System.out.println("error message: " + root.getChild("QueryResponse").getChild("BooleanResult").getChild("Message").getText());
		 
	    } catch(Exception ex){
		System.err.println(ex.getMessage());
	    }
	    JTable resultTable = new JTable(data, valids);
	    resultPane = new JScrollPane(resultTable);
		 
	    JFrame display;
	    if (isDiscovery == true)
		display = new JFrame("Result of Discovery Search");
	    else 
		display = new JFrame("Result of Granule Search"); 
	    display.getContentPane().add(resultPane, BorderLayout.CENTER);
	    display.pack();
	    display.setLocation(550,550);
	    display.setVisible(true);
	}
    }
}

// $Log: not supported by cvs2svn $






