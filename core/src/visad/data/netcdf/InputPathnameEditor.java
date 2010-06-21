/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: InputPathnameEditor.java,v 1.5 2001-11-27 22:29:31 dglo Exp $
 */

package visad.data.netcdf;

import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.beans.PropertyEditorSupport;


/**
 * A JavaBean property editor for input pathnames.
 */
public abstract class
InputPathnameEditor
    extends	PropertyEditorSupport
{
    private FileDialogPanel	fileDialog;

    /**
     * Construct.
     */
    public
    InputPathnameEditor(String filterPattern, String initialPathname)
    {
	fileDialog = new FileDialogPanel(filterPattern, initialPathname);
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
}
