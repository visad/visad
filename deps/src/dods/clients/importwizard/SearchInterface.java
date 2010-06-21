/* 
 * Any Interface added by the SearchWindow class must extend this class
 * 
 * In addition to extending this class, it must
 *   - Have it's first constructor take either a String, or no value at all
 *
 * For further information, see the comments at the top of SearchWindow.java .
 *
 * @author rhonhart
 */
package dods.clients.importwizard;

public abstract class SearchInterface extends javax.swing.JPanel {
    protected SearchInterface() {

    }

    public abstract DodsURL[] getURLs();
}

