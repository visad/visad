package dods.clients.importwizard.GCMD;

//import dods.clients.importwizard.SearchInterface;
import dods.clients.importwizard.*;
import dods.clients.importwizard.ECHO.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import gnu.regexp.*;
import org.jdom.*;
import org.jdom.output.XMLOutputter;

/** 
 * Displays a window for GCMD search
 *
 * @author Zhifang(Sheila Jiang)
 */
public class GCMDSearch extends SearchInterface 
    implements ActionListener 
{
    //private Vector actionListeners;
    //private String actionCommand;
    private JTabbedPane tabbedPane;
    private JPanel freeTextSearch;
    private JPanel keywordSearch;
    private JPanel spatialPanel;
    private DodsURL[] urls;
           
    /**
     * Create a new <code>GCMDSearch</code>
     */
    public GCMDSearch(String baseURL) {
	//super("ECHO Search Wizard");
	//actionListeners = new Vector();
	tabbedPane = new JTabbedPane();
        
	freeTextSearch = new FreeTextSearch(baseURL);
	tabbedPane.addTab("Free Text", freeTextSearch);
        tabbedPane.setSelectedIndex(0);
	
	keywordSearch = new KeywordSearch(baseURL);
	tabbedPane.addTab("Keyword", keywordSearch);
	
	spatialPanel = new SpatialPanel();
        tabbedPane.addTab("Spatial", spatialPanel);
	
	
	//add title info
	tabbedPane.setBorder(BorderFactory.createTitledBorder("Global Change Master Directory Search"));
	//tabbedPane.addChangeListener(this);
	//add tabbed panel and button panel
	setLayout(new BorderLayout());
	add(tabbedPane, BorderLayout.CENTER);
	
	//pack();
	
    }

    public void actionPerformed(ActionEvent e) {
    }

    //
    public DodsURL[] getURLs(){
	return ((SearchInterface)tabbedPane.getSelectedComponent()).getURLs();
    }
    
    public JPanel getSpatial(){
	return spatialPanel;
    }
       
    public static void main(String args[]) {
	
	JFrame frame = new JFrame("GCMD");
	frame.getContentPane().add(new GCMDSearch("http://128.183.164.60/servlets/md/"));
	frame.pack();
	frame.setVisible(true);
    }
}


