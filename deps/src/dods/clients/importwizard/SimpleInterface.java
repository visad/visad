package dods.clients.importwizard;

import javax.swing.*;
import java.net.*;
import java.io.*;
import java.util.*;

public class SimpleInterface extends SearchInterface {
    private JList urlList;

    public SimpleInterface(String invURL) {
	try {
	    URL url = new URL(invURL);
	    BufferedReader in;
	    Vector listData = new Vector();
	    String line;
	    urlList = new JList();

	    in = new BufferedReader(new InputStreamReader(url.openStream()));

	    while( (line = in.readLine()) != null) 
		listData.add(line);

	    urlList.setListData(listData);
	    add(urlList);
	}
	catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public DodsURL[] getURLs() {
	Object[] values = urlList.getSelectedValues();
	DodsURL[] ret = new DodsURL[values.length];
	for(int i=0;i<values.length;i++) 
	    ret[i] = new DodsURL((String)values[i], DodsURL.DATA_URL);

	return ret;
    }
}



