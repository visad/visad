/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: FileDialogPanel.java,v 1.2 2001-11-27 22:29:31 dglo Exp $
 */

package visad.data.netcdf;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Label;
import java.awt.List;
import java.awt.Panel;
import java.awt.TextField;


/**
 * A non-Window equivalent of FileDialog.
 */
public class
FileDialogPanel
    extends	Panel
{
    /**
     * The pathname.
     */
    private String	pathname;


    /**
     * Construct.
     */
    public
    FileDialogPanel(String filter, String initialPathname)
    {
	super(new GridBagLayout());

	pathname = initialPathname;

	Component	folderComponent = newFolderComponent();
	Component	filterComponent = newFilterComponent(filter);
	Component	filesListComponent = newFilesListComponent();
	Component	foldersListComponent = newFoldersListComponent();
	Component	fileComponent = newFileComponent(initialPathname);
	Component	buttonsComponent = newButtonsComponent();

	addComponent(folderComponent,      0, 0, GridBagConstraints.HORIZONTAL);
	addComponent(filterComponent,      0, 1, GridBagConstraints.HORIZONTAL);
	addComponent(filesListComponent,   1, 1, GridBagConstraints.BOTH);
	addComponent(foldersListComponent, 0, 2, GridBagConstraints.BOTH);
	addComponent(fileComponent,        0, 3, GridBagConstraints.HORIZONTAL);
	addComponent(buttonsComponent,     0, 4, GridBagConstraints.HORIZONTAL);

	validate();
	setSize(getPreferredSize());
    }


    /**
     * Add a component.
     */
    protected void
    addComponent(Component component, int gridx, int gridy, int fill)
    {
	GridBagConstraints	gbc = new GridBagConstraints();

	gbc.anchor= GridBagConstraints.NORTHWEST;
	gbc.gridx = gridx;
	gbc.gridy = gridy;
	gbc.fill= fill;

	((GridBagLayout)getLayout()).setConstraints(component, gbc);
	add(component);
    }


    /**
     * Return a folder component.
     */
    protected Component
    newFolderComponent()
    {
	return newTextComponent("Enter path or folder name:",
	    System.getProperty("user.dir"));
    }


    /**
     * Return a filter component.
     */
    protected Component
    newFilterComponent(String pattern)
    {
	return newTextComponent("Filter", pattern);
    }


    /**
     * Return a files list component.
     */
    protected Component
    newFilesListComponent()
    {
	return newListComponent("Files", new String[] {"file1", "file2"});
    }


    /**
     * Return a folders list component.
     */
    protected Component
    newFoldersListComponent()
    {
	return newListComponent("Folders", new String[] {"folder1", "folder2"});
    }


    /**
     * Return a file component.
     */
    protected Component
    newFileComponent(String initialPathname)
    {
	return newTextComponent("Enter file name:", initialPathname);
    }


    /**
     * Return a buttons component.
     */
    protected Component
    newButtonsComponent()
    {
	Panel	panel = new Panel(new GridBagLayout());

	addButton(panel, new Button("Update"));
	addButton(panel, new Button("Reset"));

	return panel;
    }


    /**
     * Add a button to a panel.
     */
    protected void
    addButton(Panel panel, Button button)
    {
	GridBagConstraints	gbc = new GridBagConstraints();

	((GridBagLayout)panel.getLayout()).setConstraints(button, gbc);
	panel.add(button);
    }


    /**
     * Return a list component.
     */
    protected Component
    newListComponent(String title, String[] items)
    {
	List	list = new List(items.length);

	for (int i = 0; i < items.length; ++i)
	    list.add(items[i]);

	return newLabeledComponent(title, list);
    }


    /**
     * Return a text component (label and text field).
     */
    protected Component
    newTextComponent(String title, String initialText)
    {
	return newLabeledComponent(title, new TextField(initialText, 20));
    }


    /**
     * Return a labeled component.
     */
    protected Component
    newLabeledComponent(String title, Component component)
    {
	BorderLayout	lm = new BorderLayout();
	Panel		panel = new Panel(lm);
	Label		label = new Label(title, Label.LEFT);

	lm.addLayoutComponent(label, BorderLayout.NORTH);
	panel.add(label);

	lm.addLayoutComponent(component, BorderLayout.SOUTH);
	panel.add(component);

	return panel;
    }


    /**
     * Gets the pathname.
     */
    public String
    getFile()
    {
	return pathname;
    }


    /**
     * Sets the pathname.
     */
    public void
    setFile(String pathname)
    {
	this.pathname = pathname;
    }


    /**
     * Test this class.
     */
    public static void main(String[] args)
    {
	Frame		frame = new Frame("FileDialogPanel Test");
	FileDialogPanel	fileDialog = new FileDialogPanel("*.*", "dummy.ext");

	frame.add(fileDialog);

	frame.show();
    }
}
