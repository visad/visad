import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URL;

import visad.FlatField;
import visad.VisADException;

import visad.data.gif.GIFAdapter;;

public class TestGIF
{
  public static void main(String args[])
  {
    if (args.length == 0) {
      args = new String[3];

      args[0] = "sseclogo.gif";
      args[1] = "http://www.ssec.wisc.edu/sseclogo.gif";
      args[2] = "http://www.ssec.wisc.edu/";
    }

    for (int i = 0; i < args.length; i++) {
      System.out.println("Testing \"" + args[i] + "\"");

      GIFAdapter gif = null;
      try {
	try {
	  gif = new GIFAdapter(new URL(args[i]));
	} catch (MalformedURLException e) {
	  gif = new GIFAdapter(args[i]);
	}
      } catch (IOException e) {
	System.err.println("Caught IOException for \"" + args[i] + "\": " +
			   e.getMessage());
	continue;
      } catch (VisADException e) {
	System.err.println("Caught VisADException for \"" + args[i] + "\": " +
			   e.getMessage());
	continue;
      }

      FlatField ff = gif.getData();
      if (ff == null) {
	System.out.println("\tNULL FlatField!");
      } else {
	System.out.println("\t" + ff.getType());
      }
    }
    System.out.println("Done");
    System.exit(0);
  }
}
