package dods.clients.importwizard;

import javax.swing.*;
import java.lang.*;
import java.awt.*;

/** 
 * A custom cell renderer for dods URLs which displays an icon representing
 * the status of the URL, along with the URL itself.  
 *   Red means that the URL is an inventory and no URLs have been selected.
 *   Yellow means that the URL is a dataset, and hasn't been constrained yet.
 *   Green means that the URL is either a catalog from which URLs have been
 *     selected or a dataset URL that's been constrained.
 */
class DodsURLCellRenderer extends JLabel implements ListCellRenderer {
    
    // These really should be static, but I couldn't figure out how to load
    // the images from the jar file if they were setup that way.
    final ImageIcon processedIcon;
    final ImageIcon catalogIcon;
    final ImageIcon unconstrainedIcon;

    public DodsURLCellRenderer() {
        setOpaque(true);
	processedIcon = new ImageIcon(
           getClass().getResource("../icons/greenButton.gif")
	   );
	catalogIcon = new ImageIcon(
	   getClass().getResource("../icons/redButton.gif")
	   );	
	unconstrainedIcon = new ImageIcon(
           getClass().getResource("../icons/yellowButton.gif")
	   );	
    }

    // This is the only method defined by ListCellRenderer.
    // We just reconfigure the JLabel each time we're called.
    public Component getListCellRendererComponent(
        JList list,
	Object value,            // value to display
	int index,               // cell index
	boolean isSelected,      // is the cell selected
	boolean cellHasFocus)    // the list and the cell have the focus
    {
	String s = value.toString();
	setText(s);

	if(value instanceof DodsURL) {
	    if(((DodsURL)value).hasBeenProcessed())
		setIcon(processedIcon);
	    else if(((DodsURL)value).getType() == DodsURL.CATALOG_URL 
		    || ((DodsURL)value).getType() == DodsURL.DIRECTORY_URL)
		setIcon(catalogIcon);
	    else 
		setIcon(unconstrainedIcon);
	}

	if (isSelected) {
	    setBackground(list.getSelectionBackground());
	    setForeground(list.getSelectionForeground());
	}
	else {
	    setBackground(list.getBackground());
	    setForeground(list.getForeground());
	}
	setEnabled(list.isEnabled());
	setFont(list.getFont());
	return this;
    }
}

