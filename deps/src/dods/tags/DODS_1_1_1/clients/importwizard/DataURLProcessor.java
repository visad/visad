package dods.clients.importwizard;

import javax.swing.*;

public abstract class DataURLProcessor extends JPanel {
    public abstract DodsURL getURL();
    public abstract void updateCE();
}
