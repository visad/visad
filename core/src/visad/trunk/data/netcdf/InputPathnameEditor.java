/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: InputPathnameEditor.java,v 1.1 1998-06-24 19:58:20 visad Exp $
 */

package visad.data.netcdf;

import java.awt.Panel;
import java.awt.Canvas;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditorSupport;


/**
 * A JavaBean property editor for input pathnames.
 */
public abstract class
InputPathnameEditor
    extends	PropertyEditorSupport
{
    /**
     * The parent Frame of the FileDialog.
     */
    private Frame		frame;

    /**
     * The file dialog widget.
     */
    private NonWindowFileDialog	fileDialog;


    /**
     * Supports reporting of changes to the pathname by the custom editor.
     */
    protected class
    PathnameChangeListener
	implements	PropertyChangeListener
    {
	public void
	propertyChange(PropertyChangeEvent e)
	{
	    firePropertyChange();
	}
    }


    /**
     * Construct.
     */
    public
    InputPathnameEditor(String title, String initialPathname)
    {
	frame = new Frame(title);
	fileDialog = new NonWindowFileDialog(frame, title);

	/*
	 * The setDirectory() is not needed, except for a bug under Solaris...
	 */
	fileDialog.setFile(initialPathname);
	fileDialog.addPropertyChangeListener("file", 
	    new PathnameChangeListener());
    }


    /**
     * Indicate support for a custom editor.
     */
    public boolean
    supportsCustomEditor()
    {
	return true;
    }


    /**
     * Indicate support for painting the property value.
     */
    public boolean
    isPaintable()
    {
	return true;
    }


    /**
     * Paint a representation of the pathname in the given box.
     */
    public void
    paintValue(Graphics graphics, Rectangle box)
    {
	FontMetrics	fm = graphics.getFontMetrics();

	/*
	 * Make the position of the reference point in the box congruent
	 * to the position of the reference point in the font (i.e. same
	 * proportional position).
	 */
	graphics.drawString(getAsText(), box.x, 
	    box.y + 
	    Math.round(box.height*fm.getAscent()/(float)fm.getHeight()));
    }


    /**
     * Get the property as a text string.
     */
    public String
    getAsText()
    {
	return fileDialog.getFile();
    }


    /**
     * Set the property given a text string.
     */
    public void
    setAsText(String pathname)
    {
	fileDialog.setFile(pathname);
    }


    /**
     * Return the custom editor.
     */
    public Component
    getCustomEditor()
    {
	return fileDialog;
    }


    /**
     * Set the object to be edited.
     */
    public void
    setValue(Object value)
    {
	if (value instanceof String)
	    setAsText((String)value);
    }


    /**
     * Adapter for converting a FileDialog into a non-Window Component.
     * The custom editor returned by getCustomEditor() will be added to
     * a Container and, consequently, may not be a Window (which FileDialog
     * is).
     */
    protected class
    NonWindowFileDialog
	extends Canvas
    {
	/**
	 * The FileDialog.
	 */
	private final FileDialog	fileDialog;


	/**
	 * Construct.
	 */
	protected
	NonWindowFileDialog(Frame parent, String title)
	{
	    fileDialog = new FileDialog(frame, title);

	    /*
	     * The setDirectory() is not needed, except for a bug under Solaris.
	     */
	    fileDialog.setDirectory(System.getProperty("user.dir"));
	}


	/**
	 * Set the pathname.
	 */
	public void
	setFile(String pathname)
	{
	    fileDialog.setFile(pathname);
	}


	/**
	 * Get the pathname.
	 */
	public String
	getFile()
	{
	    return fileDialog.getFile();
	}


	/**
	 * Add a PropertyChangeListener.
	 */
	public void
	addPropertyChangeListener(String name, PropertyChangeListener listener)
	{
	    fileDialog.addPropertyChangeListener(name, listener);
	}


	/**
	 * Remove a PropertyChangeListener.
	 */
	public void
	removePropertyChangeListener(PropertyChangeListener listener)
	{
	    fileDialog.removePropertyChangeListener(listener);
	}


	/**
	 * Paint.
	 */
	public void
	paint(Graphics graphics)
	{
	    fileDialog.paint(graphics);
	}


	/**
	 * Get the size.
	 */
	public Dimension
	getSize()
	{
	    return fileDialog.getSize();
	}


	/**
	 * Get the preferred size.
	 */
	public Dimension
	getPreferredSize()
	{
	    return fileDialog.getPreferredSize();
	}


	/**
	 * Set the bounds.
	 */
	public void
	setBounds(int x, int y, int width, int height)
	{
	    fileDialog.setBounds(x, y, width, height);
	}
    }
}
