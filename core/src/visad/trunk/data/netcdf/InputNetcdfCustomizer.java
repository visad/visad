/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: InputNetcdfCustomizer.java,v 1.1 1998-06-29 19:47:58 visad Exp $
 */

package visad.data.netcdf;

import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.Customizer;


/**
 * Customizes the Java bean for an input, netCDF file.
 */
public class
InputNetcdfCustomizer
    extends	Frame
    implements	Customizer
{
    /**
     * The bean to be customized.
     */
    private InputNetcdf	inputNetcdf;

    /**
     * The FileDialog.
     */
    private FileDialog	fileDialog;


    /**
     * Construct.
     */
    public
    InputNetcdfCustomizer()
    {
	super("Input netCDF File");

	fileDialog = new FileDialog(this, "Input netCDF File");
	fileDialog.setFile("");
	fileDialog.addWindowListener(new FileDialogAdapter());
    }


    /**
     * Set the object to be customized.
     */
    public void
    setObject(Object bean)
    {
	if (!(bean instanceof InputNetcdf))
	    throw new IllegalArgumentException("not an InputNetcdf bean");

	inputNetcdf = (InputNetcdf)bean;
    }


    /**
     * Supports listening to FileDialog events.
     */
    protected class
    FileDialogAdapter
	extends	WindowAdapter
    {
	/**
	 * Handle FileDialog close events.
	 */
	public void
	windowClosed(WindowEvent event)
	{
	    String	pathname = fileDialog.getFile();

	    try
	    {
		if (pathname != null)
		    inputNetcdf.setPathname(pathname);
	    }
	    catch (Exception excpt)
	    {}
	}
    }
}
