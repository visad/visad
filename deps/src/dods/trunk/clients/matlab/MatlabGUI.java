package dods.clients.matlab;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

public class MatlabGUI{
     String url="1";
    public MatlabGUI(String title){ 

        JFrame frame = new JFrame(title);
	Toolkit thekit=frame.getToolkit();
	Dimension wndsize=thekit.getScreenSize();
        frame.setBounds(wndsize.width/4,wndsize.height/4,
		  wndsize.width/2,wndsize.height/4);
	//set up the panel
	JPanel panel = new JPanel();
	panel.setLayout(new GridLayout(5,3));
	//set up Label
	JLabel urlLabel = new JLabel("URL Constrain: ", SwingConstants.CENTER);

              
        JTextField textField = new JTextField( "Initial Data", 24 );
        textField.setActionCommand( "TestText" );
	panel.add( urlLabel);
	panel.add(textField);
              
              //
              // Add the ActionListener.
              //
              textField.addActionListener( new ActionListener()
		  { 
                  public void actionPerformed( ActionEvent event ) 
		      { 
	                                
                      JTextField source = ( JTextField )event.getSource();
                      url = source.getText();
                      source.setText(" try it again? " );
                      System.out.println("url is"+url);
		      }
		  }
					   );
              
              textField.setBorder( BorderFactory.createLoweredBevelBorder() );
              frame.getContentPane().add( panel, BorderLayout.CENTER );
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);             
              frame.pack();
              frame.setVisible( true );

	      
    }
   
    public String getURL(){
	while(url.equals("1")){}
   System.out.println("here url is:"+url);
   return url;
    }
}	      











