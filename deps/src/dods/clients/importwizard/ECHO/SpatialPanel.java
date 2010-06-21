package dods.clients.importwizard.ECHO;
import dods.clients.importwizard.TMAP.map.*;
import dods.clients.importwizard.TMAP.convert.*;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import gnu.regexp.*;
import org.jdom.*;
import org.jdom.output.XMLOutputter;
import org.jdom.input.DOMBuilder;
import java.io.*;

/** 
 * 
 * This class is the panel for spatial query
 *
 * @author Sheila Jiang
 */
 
 public class SpatialPanel extends JPanel
     implements MouseListener, MouseMotionListener, ActionListener, MapConstants
 {
     //private JLabel mapLabel;
     //private JLabel keywordLabel;
     //private JLabel map;

     final static int IMAGE_SIZE_X = 500;  
     final static int IMAGE_SIZE_Y = 240;  
     final static Color MAPTOOL_COLOR1 = Color.white;
     final static int TOOL_TYPE_XY = 3;
     private boolean spatialIsSet;
     private MediaTracker tracker;
     private MapCanvas map;
     private MapGrid grid;
     private MapTool [] toolArray = new MapTool[1];
     private MapRegion [] regionArray = new MapRegion[0];
     private Convert XConvert, YConvert; // XText, YText;
     private ImageIcon mapImage;
     //private Image mapImage;

     //private SelectionArea map;
     private JTextField longFrom;
     private JTextField longTo;
     private JTextField latFrom;
     private JTextField latTo;
     private JButton zoomIn;
     private JButton zoomOut;
     private JButton finish;
     private JButton reset;
     private JScrollPane mapPanel;
     private JPanel zoomPanel;
     private JPanel numericPanel;
     private JPanel graphicPanel;
     private JPanel finishPanel;
     private JList spatialKeywords;
     private JScrollPane keywordPane;

     private JPopupMenu popup;

     //private int startX;
     //private int startY;
     //private Rectangle currentRect;

     /**
      * Constructor  
      * 
      * Create a new <code>SpatialPanel</code>
     */
     public SpatialPanel(){
	 //Create a panel
	 super();
	 
	 //init
	 spatialIsSet = false;

	 XConvert = new ConvertLongitude(ConvertLongitude.SPACE_E_W);
	 YConvert = new ConvertLatitude(ConvertLatitude.SPACE_N_S);
	 XConvert.setRange(-180.0, 180.0);
	 YConvert.setRange(-90.0, 90.0);

	 toolArray[0] = new XYTool(50,50,100,50,MAPTOOL_COLOR1);
	 toolArray[0].setRange_X(-180.0, 180.0);
	 toolArray[0].setRange_Y(-90.0, 90.0);
	 toolArray[0].setSnapping(true, true);
	 //toolArray[0].setUserBounds(1.0, 2.0, 2.0, 1.0);

	 grid = new MapGrid(-180.0, 180.0, -90.0, 90.0);
	 grid.setDomain_X(-180.0, 180.0);
	 grid.setDomain_Y(-90.0, 90.0);

	 mapImage = new ImageIcon("/home/DODS/Java-DODS/images/java_0_world.gif");
	 //mapImage = image.getImage();
	 //
	 //??? what for
	 //
	 tracker = new MediaTracker(this);
	 tracker.addImage(mapImage.getImage(), 1);
	 // this.showStatus("Loading image");
	 try {
	     tracker.waitForID(1);
	 } catch (InterruptedException e) {
	     System.out.println("Caught InterruptedException while loading image.");
	     //EHC: throw exception ?
	     return;
	 }
	 if (tracker.isErrorID(1)) {
	     System.out.println("Error loading image...");
	     //  this.showStatus("Error loading image.");
	     //this.stop();
	     //EHC: throw exception ?
	     return;
	 }
	 
	 map = new MapCanvas(mapImage, IMAGE_SIZE_X, IMAGE_SIZE_Y, toolArray, grid);
	 map.setToolArray(toolArray);
	 map.setRegionArray(regionArray);
	 
	 //map = new JLabel(new ImageIcon("images/world_sm02.jpg"));
	 //map = new SelectionArea(new ImageIcon("images/world_sm02.jpg"));
	 map.addMouseListener(this);
	 map.addMouseMotionListener(this);
	 
	 longFrom = new JTextField(5);
	 longFrom.addMouseListener(this);
	 //longFrom.setPreferredSize(new Dimension(30, 10)); 
	 longTo = new JTextField(5);
	 longTo.addMouseListener(this);
	 //longTo.setPreferredSize(new Dimension(30, 10)); 
	 latFrom = new JTextField(5);
	 latFrom.addMouseListener(this);
	 //latFrom.setPreferredSize(new Dimension(30, 10)); 
	 latTo = new JTextField(5);
	 latTo.addMouseListener(this);
	 //latTo.setPreferredSize(new Dimension(30, 10));
	  
	 zoomIn = new JButton("Zoom In");
	 zoomIn.addMouseListener(this);
	
	 zoomOut = new JButton("Zoom out");
	 zoomOut.addMouseListener(this);
	
	 finish = new JButton("Finish");
	 finish.addMouseListener(this);
	 reset = new JButton("Reset");
	 reset.addMouseListener(this);

	 mapPanel  = new JScrollPane(map);
	 numericPanel = new JPanel();
	 graphicPanel = new JPanel();
	 zoomPanel = new JPanel();
	 finishPanel = new JPanel();

	 String[] keywords = {"Africa", "Bermuda", "Indian Ocean"};
	 spatialKeywords = new JList(keywords);
	 spatialKeywords.addMouseListener(this);
	 keywordPane = new JScrollPane(spatialKeywords);

	 popup = new JPopupMenu("Select a map");
	 JMenuItem menuItem = new JMenuItem("Map 1");
	 menuItem.addActionListener(this);
	 menuItem.setActionCommand("Map 1");
	 popup.add(menuItem);
	 menuItem = new JMenuItem("Map 2");
	 menuItem.addActionListener(this);
	 menuItem.setActionCommand("Map 2");
	 popup.add(menuItem);
	 
	 initGUI();
     }
     
     /**
      * Initialize the GUI components.
     */
     public void initGUI() {
	 //
	 //set up keywordPane 
	 //
	 keywordPane.setPreferredSize(new Dimension(150, 40));
         keywordPane.setMinimumSize(new Dimension(150, 40));
	 //keywordPane.setMinimumSize(new Dimension(150, 40)); 
	 //keywordPane.setAlignmentX(LEFT_ALIGNMENT);

	 //
	 //set up zoomPanel 
	 //
	 zoomPanel.setLayout(new BoxLayout(zoomPanel, BoxLayout.X_AXIS));
	 zoomPanel.add(Box.createHorizontalGlue());
	 zoomPanel.add(zoomIn);
	 zoomPanel.add(zoomOut);
	 zoomPanel.add(Box.createHorizontalGlue());
	 zoomPanel.setBorder(BorderFactory.createEtchedBorder());       
	 
	 //
	 //set up numeric panel
	 //
	 //numericPanel.setPreferredSize(new Dimension(250, 250));
	 //numericPanel.setMaximumSize(new Dimension(300, 300));
	 //set layout
	 GridBagLayout gridbag = new GridBagLayout();
	 GridBagConstraints c = new GridBagConstraints();
	 numericPanel.setLayout(gridbag);
	 c.fill = GridBagConstraints.HORIZONTAL; 
	 
	 //set labels
	 JLabel longitude = new JLabel("Long");
	 JLabel latitude = new JLabel("Lat");
	 JLabel from = new JLabel("From");
	 JLabel to = new JLabel("To");
	 
	 //add components
	 c.gridx = 0;
         c.gridy = 1;
	 c.gridwidth = 1;
	 gridbag.setConstraints(longitude, c);
	 numericPanel.add(longitude);

	 //c.anchor = GridBagConstraints.SOUTH; //bottom of space
	 c.insets = new Insets(20,0,0,0);  //top padding
	 c.gridx = 0;
         c.gridy = 3;
	 c.gridwidth = 1;
	 gridbag.setConstraints(latitude, c);
	 numericPanel.add(latitude);

	 
	 c.weightx = 1.0;
	 c.anchor = GridBagConstraints.EAST; //
	 c.insets = new Insets(0,10,0,0);  //left padding
         c.gridx = 1;
         c.gridy = 0;
	 c.gridwidth = 2;
	 gridbag.setConstraints(from, c);
	 numericPanel.add(from);

	 c.gridx = 3;
         c.gridy = 0;
	 c.gridwidth = 2;
	 gridbag.setConstraints(to, c);
	 numericPanel.add(to);

	 //	 c.weightx = 1.0;
	 c.anchor = GridBagConstraints.SOUTH; //bottom of space
	 //c.insets = new Insets(20,20,0,0);  //top padding
	 c.gridx = 1;
         c.gridy = 1;
	 c.gridwidth = 2;
	 gridbag.setConstraints(longFrom, c);
	 numericPanel.add(longFrom);

	 c.gridx = 3;
         c.gridy = 1;
	 c.gridwidth = 2;
	 gridbag.setConstraints(longTo, c);
	 numericPanel.add(longTo);

	 c.insets = new Insets(20,10,0,0);  //top padding
	 c.gridx = 1;
         c.gridy = 3;
	 c.gridwidth = 2;
	 gridbag.setConstraints(latFrom, c);
	 numericPanel.add(latFrom);

	 c.gridx = 3;
         c.gridy = 3;
	 c.gridwidth = 2;
	 gridbag.setConstraints(latTo, c);
	 numericPanel.add(latTo);

	 //add zoom panel
	 c.weightx = 0.0;
	 c.anchor = GridBagConstraints.EAST; //bottom of space
	 c.insets = new Insets(20,10,0,0);  //top padding
	 c.gridx = 2;
         c.gridy = 5;
	 c.gridwidth = 3;
	 gridbag.setConstraints(zoomPanel, c);
	 numericPanel.add(zoomPanel);

	 //add border
	 numericPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(0,10,0,10)));
	 
	 //
	 //set up map panel
	 //
	 //mapPanel.setBorder(BorderFactory.createEtchedBorder());
	 mapPanel.setPreferredSize(new Dimension(500,240));
	 //mapPanel.setMaximumSize(new Dimension(400, 300)); 

	 //
	 //add map and numeric panel to graphic panel
	 //
	 graphicPanel.setLayout(new BoxLayout(graphicPanel, BoxLayout.X_AXIS));
	 graphicPanel.add(mapPanel);
	 graphicPanel.add(Box.createHorizontalGlue());
         graphicPanel.add(Box.createRigidArea(new Dimension(10,0)));
	 graphicPanel.add(numericPanel);
	 
	 //
	 //add title info
	 //
	 graphicPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Select an Area"), BorderFactory.createEmptyBorder(0,10,0,0)));

	 keywordPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Select a Keyword"), BorderFactory.createEmptyBorder(0,10,0,0)));
	 
	 //
	 //set up finishPanel 
	 //
	 finishPanel.setLayout(new BoxLayout(finishPanel, BoxLayout.X_AXIS));
	 //finishPanel.add(Box.createHorizontalGlue());
	 finishPanel.add(finish);
	 finishPanel.add(reset);
	 //finishPanel.add(Box.createHorizontalGlue());
	 finishPanel.setBorder(BorderFactory.createEtchedBorder());
	 //finishPanel.setAlignmentX(LEFT_ALIGNMENT);
	 //
	 //add components onto the panel
	 //
	 setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
         add(Box.createVerticalGlue());
	 //add(mapLabel);
	 //add(Box.createVerticalGlue());
         add(Box.createRigidArea(new Dimension(0,5)));
         //add(map);
	 add(graphicPanel);
	 add(Box.createVerticalGlue());
	 add(Box.createRigidArea(new Dimension(0,20)));
	 add(finishPanel);
	 add(Box.createVerticalGlue());
	 add(Box.createRigidArea(new Dimension(0,5)));
	 add(keywordPane);
         setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
     } 
     
     public void mouseEntered(MouseEvent e) {//do nothing
     }
     
     public void mousePressed(MouseEvent e) {//keep in track if in map
	 /*
	 Object o = e.getSource();
	 if(o == map) {
	     map.repaint();
	     startX = e.getX();
	     startY = e.getY();
	     map.setRect(new Rectangle(startX, startY, 0, 0));
	     }*/
	 if (e.isPopupTrigger()) {
	     popup.show(e.getComponent(),
			e.getX(), e.getY());
	 }
     }

     public void mouseReleased(MouseEvent e) {//do nothing
	 /*
	 Object o = e.getSource();
	 Rectangle currentRect = map.getRect();
	 if(o == map && currentRect != null) {
	     //int x = currentRect.x;
	     //int y = currentRect.y; 
	     //int width = currentRect.width;
	     //int height = currentRect.height;
	     //if ((x+width) < map.getWidth() && x > 0 && 
	     // (y+height) < map.getHeight() && y > 0) {
	     // map.repaint();
	     //  }
	     map.setRect(null);
	     }	*/   
     }
     
     public void mouseExited(MouseEvent e) {//do nothing
     }
     
     public void mouseClicked(MouseEvent e) {//zoom
	 //System.out.println("mouse clicked");

	 Object o = e.getSource();
	 if(o == zoomIn) {
	     try {
		 map.zoom_in();
	     } catch (MaxZoomException mze) {
		 System.out.println(mze);
	     } catch (MinZoomException mze) {
		 System.out.println(mze);
	     }
	    
	 } else if (o == zoomOut) {
	     try {
		 map.zoom_out();
	     } catch (MaxZoomException mze) {
		 System.out.println(mze);
	     } catch (MinZoomException mze) {
		 System.out.println(mze);
	     }
	 } else if (o == finish) {
	     spatialIsSet = true;
	     ((JTabbedPane)getParent()).setSelectedIndex(0);
	 } else if (o == reset) {
	     spatialIsSet = false;
	     longFrom.setText("");
	     longTo.setText("");
	     latFrom.setText("");
	     latTo.setText("");
	     map.setImage(mapImage);
	     toolArray[0].setBounds(50,50,100,50);
	 }
	 
     }
     
     public void mouseDragged(MouseEvent e) {//do nothing
	 Object o = e.getSource();
	 if(o == map) { 
	     XConvert.setRange(grid.domain_X[LO],grid.domain_X[HI]);
	     YConvert.setRange(grid.domain_Y[LO],grid.domain_Y[HI]);
	 
	     try {
		 latFrom.setText("" + (int)map.getTool().user_Y[HI]);
		 latTo.setText("" + (int)map.getTool().user_Y[LO]);
		 longFrom.setText("" + (int)map.getTool().user_X[LO]);
		 longTo.setText("" + (int)map.getTool().user_X[HI]);
		 //latFrom.setText(YConvert.toString((int)map.getTool().user_Y[HI]));
		 //latTo.setText(YConvert.toString((int)map.getTool().user_Y[LO]));
		 //longFrom.setText(XConvert.toString((int)map.getTool().user_X[LO]));
		 //longTo.setText(XConvert.toString((int)map.getTool().user_X[HI]));
	     }
	     catch (IllegalArgumentException ex) {
		 System.out.println("During setting text fields: " + ex);
	     }
	 }
	  
	 /*
	 Object o = e.getSource();
	 Rectangle currentRect = map.getRect();
	 if(o == map && currentRect != null) {
	     int x = e.getX();
	     int y = e.getY();
	     int width = (x-startX)>0 ? (x-startX) : -(x-startX);
	     int height = (y-startY)>0 ? (y-startY) :-(y-startY);
	     int rectX = (x-startX)>0 ? startX : x; //the top-leftmost point
	     int rectY = (y-startY)>0 ? startY : y; //for currentRect
	     
	     if (x < map.getWidth() && x > 0 && y < map.getHeight() && y > 0) {
		 map.setRect(new Rectangle(rectX, rectY, width, height));
		 
		 longFrom.setText("" + rectX);
		 longTo.setText("" + (rectX+width));
		 latFrom.setText("" + rectY);
		 latTo.setText("" + (rectY+height));
 	     }
		 //map.repaint();
	     //else 
	     // map.setRect(null);
	     map.repaint();
	     repaint();
	     }	*/     
	 
     }
     
     public void mouseMoved(MouseEvent e) {//do nothing
     }
    
     public void actionPerformed(ActionEvent e) {
	 String command = e.getActionCommand();

	 if(command.equals("Map 1")) {
	     System.out.println("map 1 selected");
	     mapImage = new ImageIcon("/home/DODS/Java-DODS/images/Bird.gif");
	     map.setImage(mapImage);
	 }else if(command.equals("Map 2")) {
	     System.out.println("map 2 selected");
	     mapImage = new ImageIcon("/home/DODS/Java-DODS/images/Pig.gif");
	     map.setImage(mapImage);
	 }
     }

     /**
      * Returns the westernmost longitude of the rectangle  
      *
      * @return the westernmost longitude 
      */
     public String getWesternmost() {
	 return longFrom.getText();
     }

     /**
      * Returns the easternmost longitude of the rectangle  
      *
      * @return the easternmost longitude 
      */
     public String getEasternmost() {
	 return longTo.getText();
     }

     /**
      * Returns the northernmost latitude of the rectangle  
      *
      * @return the northernmost latitude 
      */
     public String getNorthernmost() {
	 return latFrom.getText();
     }

     /**
      * Returns the southernmost latitude of the rectangle  
      *
      * @return the southernmost latitude 
      */
     public String getSouthernmost() {
	 return latTo.getText();
     }

     /**
      * Returns <code>spatialKeywords</code>  
      *
      * @return <code>spatialKeywords</code>
      */
     public JList getKeywords() {
	 return spatialKeywords;
     } 

     /**
      * Returns if spatial has been set or not
      *        
      *
      * @return <code>true</code> if user has clicked "finish";
      *         <code>false</code> otherwise
      */
     public boolean spatialIsSet() {
	 return spatialIsSet;
     } 
 }

	 
