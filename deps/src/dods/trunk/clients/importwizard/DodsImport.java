package dods.clients.importwizard;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.*;
import java.util.*;
import java.io.*;

/**
 * This is an import wizard for dods URLs.  It can either be called from
 * the command line, or from data analysis packages which support java.
 * 
 * @author rhonhart
 */

public class DodsImport extends JFrame 
    implements ActionListener
{
    private Vector actionListeners;
    private String actionCommand;

    private Vector mainPanels;
    private JPanel currentMainPanel;

    private JButton nextButton;
    private JButton previousButton;
    private JButton finishButton;
    private JButton cancelButton;
    private JPanel buttonPanel;

    private Container contentPane;

    private DataFormatSelector formatSelector;
    private javax.swing.Timer timer; 
    
    /**
     * Create a dods import wizard
     */
    public DodsImport() {
	super("Dods Import Wizard");

	nextButton = new JButton("Next >");
	previousButton = new JButton("< Previous");
	finishButton = new JButton("Finish");
	cancelButton = new JButton("Cancel");
	buttonPanel = new JPanel();
	mainPanels = new Vector();
	actionListeners = new Vector();
	currentMainPanel = new URLBuilder();
	contentPane = getContentPane();
    
	//
	// Setup the button bar at the bottom
	//
	buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
	buttonPanel.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));

	buttonPanel.add(Box.createHorizontalGlue());
	buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));

	cancelButton.setActionCommand("cancel");
	cancelButton.addActionListener(this);
	buttonPanel.add(cancelButton);

	buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));

	previousButton.setActionCommand("previous");
	previousButton.addActionListener(this);
	previousButton.setEnabled(false);
	buttonPanel.add(previousButton);

	nextButton.setActionCommand("next");
	nextButton.addActionListener(this);
	buttonPanel.add(nextButton);

	buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));

	finishButton.setActionCommand("finish");
	finishButton.addActionListener(this);
	finishButton.setEnabled(false);
	buttonPanel.add(finishButton);

	buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
	buttonPanel.add(Box.createHorizontalGlue());

	mainPanels.addElement(currentMainPanel);
	contentPane.add(currentMainPanel, BorderLayout.CENTER);
	contentPane.add(buttonPanel, BorderLayout.SOUTH);

	pack();
    }

    /** 
     * This function gets called on action events.
     * @param e The event.
     */
    public void actionPerformed(ActionEvent e) {
	String command = e.getActionCommand();

	if(command.equals("next")) {
	    if(currentMainPanel instanceof URLBuilder) {

		DodsURL[] urls = ((URLBuilder)currentMainPanel).getURLs();
		boolean processed = false;

		if(urls.length == 0) {
		    JOptionPane.showMessageDialog(this, "No data URLs have been added.", "Error", JOptionPane.ERROR_MESSAGE);
		}

		else {
		    ((URLBuilder)currentMainPanel).applyToSelectedURLs();

		    for(int i=0;i<urls.length;i++) {
			if(urls[i].hasBeenProcessed())
			    processed = true;
		    }
		
		    if(processed == false) {
			JOptionPane.showMessageDialog(this, "<html><p>Warning, you have not applied a constraint expression to any of the URLs.<p>You will get all the data from these URLs.", "Warning", JOptionPane.WARNING_MESSAGE);
		    }
		    
		    currentMainPanel.setVisible(false);
		    contentPane.remove(currentMainPanel);
		    
		    if(formatSelector == null)
			formatSelector = new FileOutputSelector();

		    formatSelector.setURLs(urls);
		    formatSelector.setPreferredSize(currentMainPanel.getSize());
		    formatSelector.setVisible(true);
		    contentPane.add(formatSelector);
		    
		    mainPanels.addElement(formatSelector);
		    currentMainPanel = formatSelector;
		    pack();
		    finishButton.setEnabled(true);
		    previousButton.setEnabled(true);
		    nextButton.setEnabled(false);
		}
	    }

	}

	else if(command.equals("previous")) {
	    if(mainPanels.size() > 1) {
		JPanel temp = (JPanel)mainPanels.elementAt(mainPanels.size() - 2);
		temp.setPreferredSize(currentMainPanel.getSize());
		temp.setVisible(true);

		currentMainPanel.setVisible(false);
		contentPane.remove(currentMainPanel);
		mainPanels.removeElement(currentMainPanel);

		contentPane.add(temp);
		currentMainPanel = temp;
		pack();
	
		nextButton.setEnabled(true);
	    }

	    if(mainPanels.size() == 1) {
		previousButton.setEnabled(false);
		finishButton.setEnabled(false);
	    }
	}
	else if(command.equals("finish")) {
	    if(formatSelector != null)
		formatSelector.outputURLs();

	    ActionEvent evt = new ActionEvent(this,0,actionCommand);
	    for(int i=0;i<actionListeners.size();i++) {
		((ActionListener)actionListeners.elementAt(i)).actionPerformed(evt);
	    }
	    
	    //display exiting message
	    //JOptionPane.showMessageDialog(this, "<html><p>Writing to screen and exiting...", "Working Status", JOptionPane.INFORMATION_MESSAGE);
	    currentMainPanel.setVisible(false);
	    //contentPane.remove(currentMainPanel);
	    contentPane.removeAll();

	    JPanel temp = new JPanel();
	    temp.setLayout(new BoxLayout(temp, BoxLayout.Y_AXIS));
	    temp.setBorder(BorderFactory.createEmptyBorder(100,100,100,100));
	    temp.add(Box.createVerticalGlue());
	    JLabel text = new JLabel("<html><font color=\"black\"><b><center><p>Writing to screen and exiting...</center></font></b>");
	    text.setAlignmentX(Component.CENTER_ALIGNMENT);
	    temp.add(text);
	    temp.add(Box.createVerticalGlue());
	    
	    //temp.setPreferredSize(currentMainPanel.getSize());
	    //temp.setMinimumSize(currentMainPanel.getMinimumSize());
	    contentPane.add(temp, BorderLayout.CENTER);
	    //validate();
	    //repaint();
	    pack();

	    timer = new javax.swing.Timer(1000, new ActionListener() {
		  public void actionPerformed(ActionEvent evt) {
		      System.exit(0);
		  }
		});
	    timer.start();
	}
	else if(command.equals("cancel")) {
	    setVisible(false);
	}
    }

    /**
     * Return an array of the fully-constrained urls that the wizard
     * has generated.
     * @return the URLs.
     */
    public String[] getURLs() {
	if(currentMainPanel instanceof DataFormatSelector) {
	    DodsURL[] dodsURLs = ((DataFormatSelector)currentMainPanel).getURLs();
	    String[] urls = new String[dodsURLs.length];
	    
	    for(int i=0;i<urls.length;i++) {
		urls[i] = dodsURLs[i].getFullURL();
	    }
	    
	    return urls;
	}
	else 
	    return null;
    }

    /** 
     * Return an array of the variable names that the data should assigned to.
     * @return the name of the variables that the data should assigned to.
     */
    public String[] getNames() {
	if(currentMainPanel instanceof DataFormatSelector)
	    return ((DataFormatSelector)currentMainPanel).getNames();
	else
	    return null;
    }

    /** 
     * Return a string holding any options which should be passed to the
     * loaddods client.
     * @return an options which should be passed to the loaddods client.
     */
    public String[] getOptions() {
	String[] options = { "" };
	if(currentMainPanel instanceof DataFormatSelector)
	    options = ((DataFormatSelector)currentMainPanel).getOptions();

	return options;
    }

    /**
     * Return a reference to the finish button (Matlab needs this to 
     * setup a callback function).
     * @return a reference to the finish button
     */
    public JButton getFinishButton() {
	return finishButton;
    }

    public void setDataFormatSelector(DataFormatSelector f) {
	formatSelector = f;
    }

    public static void main(String args[]) {
	
        DodsImport importWizard = new DodsImport();
	importWizard.setLocation(50,50);
        //not available in jdk1.2
	//importWizard.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	importWizard.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {System.exit(0);}
        });
	importWizard.setVisible(true);
    }

    public void addActionListener(ActionListener a) {
	actionListeners.addElement(a);
    }
    public void setActionCommand(String command) {
	actionCommand = command;
    }
    
}
