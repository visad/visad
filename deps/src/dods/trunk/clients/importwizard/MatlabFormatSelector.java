package dods.clients.importwizard;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import gnu.regexp.*;

public class MatlabFormatSelector extends DataFormatSelector
    implements ActionListener
{

    private JPanel mainPanel;
    
    private DodsURL[] urls;
    private JList urlList;
    private JScrollPane urlScroller;
    private JPanel urlPanel;

    private ButtonGroup flattened;
    private JPanel flattenedPanel;
    private JRadioButton flattenedButton;
    private JRadioButton structureButton;

    private ButtonGroup namingOption;
    private JPanel namingPanel;
    private JRadioButton simpleButton;
    private JRadioButton regexpButton;
    private JTextField simpleField;
    private JTextField regexpField;
    private JTextField replaceField;

    public MatlabFormatSelector() {
	mainPanel = new JPanel();
	urlList = new JList();
	urlScroller = new JScrollPane(urlList);
	urlPanel = new JPanel();
	namingOption = new ButtonGroup();
	namingPanel = new JPanel();
	simpleButton = new JRadioButton("Prefix: ");
	regexpButton = new JRadioButton("Regexp: ");
	simpleField = new JTextField();
	regexpField = new JTextField();
	replaceField = new JTextField();

	flattened = new ButtonGroup();
	flattenedPanel = new JPanel();
	flattenedButton = new JRadioButton("Flattened");
	structureButton = new JRadioButton("Structure");

	//
	// Setup the Return Data Format Panel
	//
	flattened.add(flattenedButton);
	flattened.add(structureButton);
	flattenedPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Data Format"));
	flattenedPanel.setMaximumSize(new Dimension(300,70));
	flattenedPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
	flattenedPanel.add(flattenedButton);
	structureButton.setSelected(true);
	flattenedPanel.add(structureButton);
	
	//
	// Setup the Variable Names panel
	//
	namingOption.add(simpleButton);
	namingOption.add(regexpButton);
	GridBagLayout gridbag = new GridBagLayout();
	GridBagConstraints c = new GridBagConstraints();
	namingPanel.setLayout(gridbag);
	namingPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Variable Naming Scheme"));
	namingPanel.setMaximumSize(new Dimension(32768,70));
	c.fill = GridBagConstraints.HORIZONTAL; 
	c.gridx = 0;
	c.gridy = 0;
	c.weightx = 0;

	simpleButton.addActionListener(this);
	simpleButton.setActionCommand("simpleName");
	simpleButton.setSelected(true);
	gridbag.setConstraints(simpleButton,c);
	namingPanel.add(simpleButton);

	c.gridx = 1;
	c.gridy = 0;
	c.gridwidth = 3;
	c.weightx = 1;
	simpleField.setText("data");
	gridbag.setConstraints(simpleField,c);
	namingPanel.add(simpleField);

	c.gridx = 0;
	c.gridy = 1;
	c.gridwidth = 1;
	c.weightx = 0;

	regexpButton.addActionListener(this);
	regexpButton.setActionCommand("regexpName");
	gridbag.setConstraints(regexpButton,c);
	namingPanel.add(regexpButton);

	c.gridx = 1;
	c.gridy = 1;
	c.weightx = 0.5;
	regexpField.setEnabled(false);
	gridbag.setConstraints(regexpField,c);
	namingPanel.add(regexpField);

	c.gridx = 2;
	c.gridy = 1;
	c.weightx = 0;
	JLabel replaceLabel = new JLabel("Replace: ");
	gridbag.setConstraints(replaceLabel,c);
	namingPanel.add(replaceLabel);

	c.gridx = 3;
	c.gridy = 1;
	c.weightx = 0.5;
	replaceField.setEnabled(false);
	gridbag.setConstraints(replaceField,c);
	namingPanel.add(replaceField);

	setLayout(new BorderLayout());
	
	//urlList.setListData(urls);
	urlPanel.setLayout(new BoxLayout(urlPanel, BoxLayout.Y_AXIS));
	urlPanel.setPreferredSize(new Dimension(200,3));
	urlPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "DODS URLs"), BorderFactory.createEmptyBorder(1,1,1,1)));
	urlPanel.add(urlScroller);

	//
	// Setup the main (CENTER) panel
	//
	mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
	mainPanel.add(Box.createRigidArea(new Dimension(0,10)));
	mainPanel.add(flattenedPanel);
	mainPanel.add(Box.createRigidArea(new Dimension(0,20)));
	mainPanel.add(namingPanel);
	mainPanel.add(Box.createVerticalGlue());
	add(urlPanel, BorderLayout.EAST);
	add(mainPanel, BorderLayout.CENTER);
    }
    
    public void actionPerformed(ActionEvent e) {
	if(e.getActionCommand().equals("simpleName")) {
	    simpleField.setEnabled(true);
	    regexpField.setEnabled(false);
	}
	else if(e.getActionCommand().equals("regexpName")) {
	    simpleField.setEnabled(false);
	    regexpField.setEnabled(true);
	    replaceField.setEnabled(true);
	    String[] names = new String[urls.length];

	    try {
		RE regexp = new RE(regexpField.getText());
		RE number = new RE("%n");
		for(int i=0;i<urls.length;i++) {
		    if(!replaceField.getText().equals("")) 
			names[i] = regexp.substituteAll(urls[i].toString(), replaceField.getText());
		    else
			names[i] = regexp.getMatch(urls[i].toString()).toString();

		    names[i] = number.substituteAll(names[i],String.valueOf(i));
		    System.out.println(names[i]);
		}
	    }
	    catch(Exception excp) { excp.printStackTrace(); }
	}
    }

    public String[] getOptions() {
	String[] ret = new String[1];

	if(structureButton.isSelected())
	    ret[0] = "-S";
	else 
	    ret[0] = "";

	return ret;

    }

    public String[] getNames() {
	String names[] = new String[urls.length];
	if(simpleButton.isSelected()) {
	    String base = simpleField.getText();
	    for(int i=0;i<urls.length;i++) {
		if(urls.length > 9 && i <= 9)
		    names[i] = base + "0" + i;
		else 
		    names[i] = base + i;
	    }
	}
	else {
	    try {
		RE regexp = new RE(regexpField.getText());
		RE number = new RE("%n");
		for(int i=0;i<urls.length;i++) {
		    if(!replaceField.getText().equals("")) 
			names[i] = regexp.substituteAll(urls[i].toString(), 
							replaceField.getText());
		    else
			names[i] = regexp.getMatch(urls[i].toString()).toString();

		    names[i] = number.substituteAll(names[i],String.valueOf(i));
		}
	    }
	    catch(Exception excp) { excp.printStackTrace(); }
	}
	
	return names;
    }

    public DodsURL[] getURLs() {
	return urls;
    }

    public void setURLs(DodsURL[] newURLs) {
	urls = newURLs;
	urlList.setListData(urls);
    }
}
