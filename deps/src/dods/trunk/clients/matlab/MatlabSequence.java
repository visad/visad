package dods.clients.matlab;

import dods.dap.*;
import java.lang.*;
import java.util.*;
import java.io.*;

/** 
 * A Sequence class which will return columns of variables as well as 
 * individual variables.
 */
public class MatlabSequence extends DSequence {
    
    public MatlabSequence() {
	super();
    }

    public MatlabSequence(String name) {
	super(name);
    }

    /** 
     * Returns a column in a sequence corresponding to <code>name</code>
     * 
     * @param name The name of the variable to be retrieved.  It can be of the 
     *             form <varname>.<fieldname> or simply the name of the 
     *             variable.
     * @return The column in the sequence as an array of BaseTypes.
     */
    public BaseType[] getColumn(String name) 
	throws NoSuchVariableException
    {
	int rowCount = getRowCount();
	int rowWidth = elementCount();
	int dotIndex = name.indexOf('.');
	int rowIndex = 0;
	int colIndex = -1;

	BaseType[] column = new BaseType[rowCount];
	Enumeration rows = allValues.elements();

	if (dotIndex != -1) {
	    // If name is of the form <aggregate>.<field>, recurse into
	    // the variable <aggregate> and get sub-variable <field>.

	    String aggregate = name.substring(0, dotIndex);
            String field = name.substring(dotIndex+1);
	    BaseType[] subVars = null;

	    for(int i=0; i<varTemplate.size(); i++) {
		BaseType var = (BaseType)varTemplate.elementAt(i);
		if(var.getName().equals(aggregate)) {
		    colIndex = i;
		    break;
		}		    
	    }
	    if(colIndex == -1)
		throw new NoSuchVariableException("MatlabSequence::getColumn()");

	    while(rows.hasMoreElements()) {
		Vector row = (Vector)rows.nextElement();
		BaseType var = (BaseType)row.elementAt(colIndex);
		int numVars;
		int i=0;
		
		if(var instanceof MatlabSequence) {
		    numVars = ((MatlabSequence)var).getRowCount();
		    try {
			subVars = ((MatlabSequence)var).getColumn(field);
		    }
		    catch (NoSuchVariableException e) {
			throw(e);
		    }
		}
		else if(var instanceof DConstructor) {
		    subVars = new BaseType[1];
		    numVars = 1;
		    try {
			subVars[0] = ((DConstructor)var).getVariable(field);
		    }
		    catch(NoSuchVariableException e) {
			throw(e);
		    }
		}
		else 
		    throw new NoSuchVariableException("MatlabSequence: getColumn()");
		
		for(int j=0; j<numVars; j++)
		    column[rowIndex++] = subVars[j];
	    }
	}
	
	else {
	    for(int i=0; i<varTemplate.size(); i++) {
		BaseType var = (BaseType)varTemplate.elementAt(i);
		if(var.getName().equals(name)) {
		    colIndex = i;
		    break;
		}	
	    }
	    
	    if(colIndex == -1)
		throw new NoSuchVariableException("MatlabSequence::getColumn()");

	    while(rows.hasMoreElements()) 
		column[rowIndex++] = (BaseType)((Vector)rows.nextElement()).elementAt(colIndex);
	    
	}

	return column;
    }
    
    // The deserialize stuff had to be thrown in to get around a bug in 
    // the DAP library.  

    public synchronized void deserialize(DataInputStream source,
					 ServerVersion sv,
					 StatusUI statusUI)
	throws IOException, EOFException, DataReadException {
	
	// check for old servers

	// Sometimes the DAP library will report the version of a server
	// as 0.0, and in that case it's usually better to assume that 
	// it's a new server as opposed to an old one.  rph 07/11/01.

	if (sv.getMajor() != 0 && (sv.getMajor() < 2 || (sv.getMajor() == 2 && sv.getMinor() < 15))) {
	    oldDeserialize(source, sv, statusUI);
	} else {
// ************* Pulled out the getLevel() check in order to support the "new" 
// and "improved" serialization of dods sequences. 8/31/01 ndp
	    // top level of sequence handles start and end markers
	    //if (getLevel() == 0) {
		// loop until end of sequence
		for(;;) {
		    byte marker = readMarker(source);
		    if(statusUI != null)
			statusUI.incrementByteCount(4);
		    if (marker == START_OF_INSTANCE)
			deserializeSingle(source, sv, statusUI);
		    else if (marker == END_OF_SEQUENCE)
			break;
		    else
			throw new DataReadException("Sequence start marker not found");
		}
// ************* Pulled out the getLevel() check in order to support the "new" 
// and "improved" serialization of dods sequences. 8/31/01 ndp
	   // } else {
		// lower levels only deserialize a single instance at a time
	//	deserializeSingle(source, sv, statusUI);
	    //}
	}
    }

    private byte readMarker(DataInputStream source) throws IOException {
	byte marker = source.readByte();
	// pad out to a multiple of four bytes
	byte unused;
	for (int i=0; i<3; i++)
	    unused = source.readByte();
	
	return marker;
    }

    private void oldDeserialize(DataInputStream source, ServerVersion sv,
				StatusUI statusUI)
	throws IOException, DataReadException {
	try {
	    for(;;) {
		deserializeSingle(source, sv, statusUI);
	    }
	}
	catch (EOFException e) {}
    }

    private void deserializeSingle(DataInputStream source, ServerVersion sv,
				   StatusUI statusUI)
	throws IOException, EOFException, DataReadException {
	// create a new instance from the variable template Vector
	Vector newInstance = new Vector();
	for(int i=0; i<varTemplate.size(); i++) {
	    BaseType bt = (BaseType)varTemplate.elementAt(i);
	    newInstance.addElement(bt.clone());
	}
	// deserialize the new instance
	for (Enumeration e = newInstance.elements(); e.hasMoreElements(); ) {
	    if(statusUI != null && statusUI.userCancelled())
		throw new DataReadException("User cancelled");
	    ClientIO bt = (ClientIO)e.nextElement();
	    bt.deserialize(source, sv, statusUI);
	}
	// add the new instance to the allValues vector
	allValues.addElement(newInstance);
    }
};
