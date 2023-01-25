//
// Vis5DFamily.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2023 Bill Hibbard, Curtis Rueden, Tom
Rink, Dave Glowacki, Steve Emmerson, Tom Whittaker, Don Murray, and
Tommy Jasmin.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Library General Public
License as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Library General Public License for more details.

You should have received a copy of the GNU Library General Public
License along with this library; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
MA 02111-1307, USA
*/

package visad.data.vis5d;


import java.io.IOException;

import java.rmi.RemoteException;

import visad.Data;

import visad.DataImpl;

import visad.VisADException;

import visad.data.*;

import java.net.URL;


/**
 * A container for Vis5D file types - regular using
 * Vis5DAdaptedForm and Vis5D TOPO files.
 * To read a <tt>Data</tt> object from a file or URL:<br>
 * <pre>
 *    Data data = new Vis5DFamily("vis5d").open(string);
 * </pre>
 * @see  visad.data.vis5d.Vis5DAdaptedForm
 * @see  visad.data.vis5d.Vis5DTopoForm
 * @author Don Murray
 */
public class Vis5DFamily extends FunctionFormFamily {

    /**
      * List of all supported VisAD datatype Forms.
      * @serial
      */
    private static FormNode[] list            = new FormNode[5];
    private static boolean    listInitialized = false;

    /**
     * Build a list of all known file adapter Forms
     */
    private static void buildList() {

        int i = 0;

        try {
            list[i] = new Vis5DAdaptedForm();

            i++;
        } catch (Throwable t) {}

        try {
            list[i] = new Vis5DTopoForm();

            i++;
        } catch (Throwable t) {}

        // throw an Exception if too many Forms for list
        FormNode junk = list[i];

        while (i < list.length) {
            list[i++] = null;
        }

        listInitialized = true;
    }

    /**
     * Add to the family of the supported map datatype Forms
     * @param  form   FormNode to add to the list
     *
     * @exception ArrayIndexOutOfBoundsException
     *                   If there is no more room in the list.
     */
    public static void addFormToList(FormNode form)
            throws ArrayIndexOutOfBoundsException {

        synchronized (list) {
            if (!listInitialized) {
                buildList();
            }

            int i = 0;

            while (i < list.length) {
                if (list[i] == null) {
                    list[i] = form;

                    return;
                }

                i++;
            }
        }

        throw new ArrayIndexOutOfBoundsException("Only " + list.length
                                                 + " entries allowed");
    }

    /**
     * Construct a family of the supported map datatype Forms
     * @param  name   name of the family
     */
    public Vis5DFamily(String name) {

        super(name);

        synchronized (list) {
            if (!listInitialized) {
                buildList();
            }
        }

        for (int i = 0; (i < list.length) && (list[i] != null); i++) {
            forms.addElement(list[i]);
        }
    }

    /**
     * Open a local data object using the first appropriate map form.
     * @param  id   String representing the path of the map file
     * @throws  BadFormException  - no form is appropriate
     * @throws  VisADException  - VisAD error
     */
    public DataImpl open(String id) throws BadFormException, VisADException {
        return super.open(id);
    }

    /**
     * Open a remote data object using the first appropriate map form.
     * @param  url   URL representing the location of the map file
     * @throws  BadFormException  - no form is appropriate
     * @throws  VisADException  - VisAD error
     * @throws  IOException  - file not found
     */
    public DataImpl open(URL url)
            throws BadFormException, VisADException, IOException {
        return super.open(url);
    }

    /**
     * Test the Vis5DFamily class
     * run java visad.data.vis5d.Vis5DFamily  v5dfile1 v5dfile2 ... v5dfilen
     */
    public static void main(String[] args)
            throws BadFormException, IOException, RemoteException,
                   VisADException {

        if (args.length < 1) {
            System.err.println("Usage: Vis5DFamily infile [infile ...]");
            System.exit(1);

            return;
        }

        Vis5DFamily fr = new Vis5DFamily("sample");

        for (int i = 0; i < args.length; i++) {
            Data data;

            System.out.println("Trying file " + args[i]);

            data = fr.open(args[i]);

            System.out.println(args[i] + ": "
                               + data.getType().prettyString());
        }
    }
}


/*--- Formatted 2002-02-04 16:10:31 MST in Sun Java Convention Style ---*/


/*------ Formatted by Jindent 3.24 Basic 1.0 --- http://www.jindent.de ------*/
