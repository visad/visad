package dods.clients.importwizard.ECHO;

import java.lang.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;
import org.jdom.Document;

/** 
 * This is the interface to be implemented by classes DiscoveryQuery and
 * InventoryQuery 
 *
 * @author Sheila Jiang
 */
 
 public interface QueryRequest
 {
     public  Document getIIMSAQL();
     public  Document getQueryRequest();
 }
