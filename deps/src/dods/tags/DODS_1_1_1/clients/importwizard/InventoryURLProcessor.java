package dods.clients.importwizard;

import javax.swing.*;
import java.awt.event.*;

public abstract class InventoryURLProcessor extends JPanel {
    public abstract DodsURL[] getURLs();
    public abstract void addActionListener(ActionListener a);
    public abstract void setActionCommand(String actionCommand);
}
