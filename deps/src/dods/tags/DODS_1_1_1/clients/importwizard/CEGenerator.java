/*
 * CEGenerator.java
 *
 * Created on December 21, 2001, 11:03 AM
 */

package dods.clients.importwizard;

import java.lang.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import dods.dap.*;


/**
 *
 * @author Rich Honhart <rhonhart@po.gso.uri.edu>
 */

public class CEGenerator extends DataURLProcessor 
    implements ActionListener
{

    private String actionCommand;
    private Vector actionListeners;
    private DDSSelector ddss;
    private JTextField ceField;
    private DodsURL url;

    /** Creates a new instance of CEGenerator */
    public CEGenerator(DodsURL newURL) {
	actionCommand = "";
	actionListeners = new Vector();
	url = newURL;
        initGUI(url.getBaseURL());
	if(!newURL.getConstraintExpression().equals("")) {
	    ddss.deselectAll();
	    ddss.applyCE(newURL.getConstraintExpression());
	}
    }

    protected void initGUI(String url) {
        try {
	    DDS dds = getDDS(url);
	    
	    JPanel mainPanel = new JPanel();
	    JPanel topPanel = new JPanel();
	    JButton selectAll = new JButton("Select All");
	    JButton selectNone = new JButton("Deselect All");
	    JButton resetCE = new JButton("Reset CE");

	    ceField = new JTextField();
	    
	    ddss = new DDSSelector(dds);
	    JScrollPane scroller = new JScrollPane(ddss);
	    
	    setLayout(new BorderLayout());
	    setBorder(BorderFactory.createTitledBorder(
					 BorderFactory.createEtchedBorder(), 
					 "Choose which variables you want"
					 ));
	    topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
	    
	    selectAll.addActionListener(this);
	    selectAll.setActionCommand("selectall");
	    topPanel.add(selectAll);
	    
	    selectNone.addActionListener(this);
	    selectNone.setActionCommand("deselectall");
	    topPanel.add(selectNone);
	    
	    topPanel.add(Box.createHorizontalGlue());
	    
	    resetCE.addActionListener(this);
	    resetCE.setActionCommand("resetce");
	    topPanel.add(resetCE);
	    
	    add(topPanel, BorderLayout.SOUTH);
	    add(scroller, BorderLayout.CENTER);
		
	}

	catch(Exception e) {
	    //e.printStackTrace();
	    setLayout(new BorderLayout());
	    setBorder(BorderFactory.createEmptyBorder(15,15,15,15));
	    add(new JLabel("<html><center>Could not get the DDS object for: "
			   + "<p>" + url + ".<p>If you entered this URL by "
			   + "hand, check to see if it was typed correctly."));
	}
    }
    
    protected DDS getDDS(String url) 
	throws java.lang.Exception
    {
        try {
            DConnect conn = new DConnect(url);
            return conn.getDDS();
        }
        catch(Exception e) {
            throw(e);
        }
    }
    
    public void setActionCommand(String command) {
	actionCommand = command;
    }
    
    public void addActionListener(ActionListener a) {
	actionListeners.addElement(a);
    }

    public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
	String command = actionEvent.getActionCommand();

	if(command.equals("resetce")) {
	    ddss.reset();
	}

	else if(command.equals("selectall")) {
	    ddss.selectAll();
	}

	else if(command.equals("deselectall")) {
	    ddss.deselectAll();
	}
   }
    /**
    * @param args the command line arguments
    */
    public static void main (String args[]) {
        JFrame main = new JFrame("Constraint Expresssion Generator");
        if(args.length == 1) {
            main.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {System.exit(0);}
            });
            main.setContentPane(new CEGenerator(new DodsURL(args[0], "")));
            main.setSize(540,400);
            main.setVisible(true);
        }
        else {
            System.out.println("Usage: java CEGenerator <url>");
        }
    } 
    
    public DodsURL getURL() {
	if(ddss != null) {
	    url.setConstraintExpression(ddss.generateCE(""));
	}
	return url;
    }
    
    public void updateCE() {
	if(ddss != null)
	    ddss.applyCE(url.getConstraintExpression());
    }

}



