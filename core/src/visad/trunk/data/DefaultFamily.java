package visad.data;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.net.URL;

import java.rmi.RemoteException;

import java.util.Enumeration;

import visad.Data;
import visad.DataImpl;
import visad.VisADException;

import visad.data.fits.FitsForm;

import visad.data.gif.GIFForm;

import visad.data.hdfeos.HdfeosDefault;

import visad.data.netcdf.Plain;

import visad.data.vis5d.Vis5DForm;

import visad.data.visad.VisADForm;

/**
  * A container for all the officially supported VisAD datatypes.
  */
public class DefaultFamily
	extends FormFamily
{
  /**
    * List of all supported VisAD datatype Forms.
    */
  /*
   *  note that I hardcoded the number of FormNodes (100)
   *  increase this if you add a new FormNode
   */
  private static FormNode[] list = new FormNode[100];
  private static boolean listInitialized = false;

  /**
    * Base class which tries to perform an operation on an object
    * using the first valid VisAD datatype Form.
    */
  abstract class FormFunction
  {
    /**
      * Return 'true' if this object's name applies to the given node.
      */
    abstract boolean check(FormFileInformer node);

    /**
      * Return an InputStream for the object.
      *
      * Used to read in the first block of the object.
      */
    abstract InputStream getStream() throws IOException;

    /**
      * The operation to be performed on the object.
      */
    abstract boolean function(FormNode node);

    /**
      * Perform an operation on an object
      * using the first valid VisAD datatype Form.
      *
      * If a Form successfully performs the operation, return 'true'.
      */
    public boolean run()
	throws IOException
    {
      // see if we can guess the file type based on the name
      for (Enumeration enum = forms.elements(); enum.hasMoreElements(); ) {
	FormNode node = (FormNode)enum.nextElement();

	if (node instanceof FormFileInformer) {
	  if (check((FormFileInformer )node)) {
	    try {
	      if (function(node)) {
		return true;
	      }
	    } catch (Exception e) {
	    }
	  }
	}
      }

      // get the first block of data from the file
      byte[] block = new byte[2048];
      InputStream is = getStream();
      is.read(block);
      is.close();

      // see if we can guess the file type based on first block of data
      for (Enumeration enum = forms.elements(); enum.hasMoreElements(); ) {
	FormNode node = (FormNode)enum.nextElement();

	if (node instanceof FormFileInformer) {
	  if (((FormFileInformer )node).isThisType(block)) {
	    try {
	      if (function(node)) {
		return true;
	      }
	    } catch (Exception e) {
	    }
	  }
	} else {
	}
      }

      // use the brute-force method of checking all the forms
      for (Enumeration enum = forms.elements(); enum.hasMoreElements(); ) {
	FormNode node = (FormNode)enum.nextElement();

	try {
	  if (function(node)) {
	    return true;
	  }
	} catch (Exception e) {
	} catch (UnsatisfiedLinkError ule) {
	}
      }

      return false;
    }
  }

  /**
    * Perform an operation on a local file object
    * using the first valid VisAD datatype Form.
    */
  abstract class FileFunction
	extends FormFunction
  {
    String name;

    public FileFunction()
    {
      name = null;
    }

    boolean check(FormFileInformer node)
    {
      return node.isThisType(name);
    }

    InputStream getStream()
	throws IOException
    {
      return new FileInputStream(name);
    }
  }

  /**
    * Save a VisAD Data object to a local file
    * using the first valid VisAD datatype Form.
    */
  class SaveForm
	extends FileFunction
  {
    private Data data;
    private boolean replace;

    public SaveForm(String name, Data data, boolean replace)
    {
      this.name = name;
      this.data = data;
      this.replace = replace;
    }

    boolean function(FormNode node)
    {
      try {
	node.save(name, data, replace);
      } catch (Exception e) {
	return false;
      }

      return true;
    }
  }

  /**
    * Add a VisAD Data object to an existing local file
    * using the first valid VisAD datatype Form.
    */
  class AddForm
	extends FileFunction
  {
    private Data data;
    private boolean replace;

    public AddForm(String name, Data data, boolean replace)
    {
      this.name = name;
      this.data = data;
      this.replace = replace;
    }

    boolean function(FormNode node)
    {
      try {
	node.add(name, data, replace);
      } catch (Exception e) {
	return false;
      }

      return true;
    }
  }

  /**
    * Read a VisAD Data object from a local file
    * using the first valid VisAD datatype Form.
    */
  class OpenStringForm
	extends FileFunction
  {
    private DataImpl data;

    public OpenStringForm(String name)
    {
      this.name = name;
      data = null;
    }

    boolean function(FormNode node)
    {
      try {
	data = node.open(name);
      } catch (Throwable t) {
	return false;
      }

      return true;
    }

    public DataImpl getData()
    {
      return data;
    }
  }

  /**
    * Perform an operation on a remote file object
    * using the first valid VisAD datatype Form.
    */
  abstract class URLFunction
	extends FormFunction
  {
    URL url;

    public URLFunction()
    {
      url = null;
    }

    boolean check(FormFileInformer node)
    {
      return node.isThisType(url.getFile());
    }

    InputStream getStream()
	throws IOException
    {
      return url.openStream();
    }
  }

  /**
    * Read a VisAD Data object from a remote file
    * using the first valid VisAD datatype Form.
    */
  class OpenURLForm
	extends URLFunction
  {
    private URL url;
    private DataImpl data;

    public OpenURLForm(URL url)
    {
      this.url = url;
      data = null;
    }

    boolean function(FormNode node)
    {
      try {
	data = node.open(url);
      } catch (Throwable t) {
	return false;
      }

      return true;
    }

    public DataImpl getData()
    {
      return data;
    }
  }

  private void buildList()
  {
    int i = 0;

    try {
      list[i] = new FitsForm();
      i++;
    } catch (Throwable t) {
    }
    try {
      list[i] = new GIFForm();
      i++;
    } catch (Throwable t) {
    }
    try {
      list[i] = new HdfeosDefault();
      i++;
    } catch (Throwable t) {
    }
    try {
      list[i] = new Plain();
      i++;
    } catch (Throwable t) {
    }
    try {
      list[i] = new Vis5DForm();
      i++;
    } catch (Throwable t) {
    }
    try {
      list[i] = new VisADForm();
      i++;
    } catch (Throwable t) {
    }

    while (i < list.length) {
      list[i++] = null;
    }
  }

  /**
    * Construct a family of the supported VisAD datatype Forms
    */
  public DefaultFamily(String name)
  {
    super(name);

    synchronized (list) {
      if (!listInitialized) {
	buildList();
      }
    }

    for (int i = 0; i < list.length && list[i] != null; i++) {
      forms.addElement(list[i]);
    }
  }

  /**
    * Save a VisAD Data object using the first appropriate Form.
    */
  public synchronized void save(String id, Data data, boolean replace)
	throws BadFormException, RemoteException, IOException, VisADException
  {
    SaveForm s = new SaveForm(id, data, replace);
    if (!s.run()) {
      throw new BadFormException("Data object not compatible with \"" +
				 getName() + "\" data family");
    }
  }

  /**
    * Add data to an existing data object using the first appropriate Form.
    */
  public synchronized void add(String id, Data data, boolean replace)
	throws BadFormException
  {
    AddForm a = new AddForm(id, data, replace);
    try {
      if (a.run()) {
	return;
      }
    } catch (IOException e) {
    }

    throw new BadFormException("Data object not compatible with \"" +
			       getName() + "\" data family");
  }

  /**
    * Open a local data object using the first appropriate Form.
    */
  public synchronized DataImpl open(String id)
	throws BadFormException, IOException, VisADException
  {
    OpenStringForm o = new OpenStringForm(id);
    if (!o.run()) {
      throw new BadFormException("Data object \"" + id +
				 "\" not compatible with \"" + getName() +
				 "\" data family");
    }

    return o.getData();
  }

  /**
    * Open a remote data object using the first appropriate Form.
    */
  public synchronized DataImpl open(URL url)
	throws BadFormException, IOException, VisADException
  {
    OpenURLForm o = new OpenURLForm(url);
    if (!o.run()) {
      throw new BadFormException("Data object \"" + url +
				 "\" not compatible with \"" + getName() +
				 "\" data family");
    }

    return o.getData();
  }

  /**
    * Test the DefaultFamily class
    */
  public static void main(String[] args)
	throws BadFormException, IOException, RemoteException, VisADException
  {
    if (args.length < 1) {
      System.err.println("Usage: DefaultFamily infile [infile ...]");
      System.exit(1);
      return;
    }

    DefaultFamily fr = new DefaultFamily("sample");

    for (int i = 0; i < args.length; i++) {
      System.out.println(args[i] + ": " + fr.open(args[i]).getType());
    }
  }
}
