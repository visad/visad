/*
 * VisAD system for interactive analysis and visualization of numerical
 * data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden, Tom
 * Rink, Dave Glowacki, and Steve Emmerson.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 1, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License in file NOTICE for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 * $Id: FileAccessor.java,v 1.3 1998-03-04 14:26:20 rink Exp $
 */

package visad.data;

import visad.Data;
import visad.FlatField;
import visad.FunctionType;
import visad.VisADException;
import java.rmi.RemoteException;


/**
 * Exchange data with a "file".
 */
public abstract class FileAccessor
{
    public abstract void	writeFile(
				    int[]	fileLocations,
				    Data	range);


    public abstract double[][]	readFlatField(
				    FlatField	template,
				    int[]	fileLocation);


    public abstract void	writeFlatField(
				    double[][]	values,
				    FlatField	template,
				    int[]	fileLocation);


    public abstract FlatField getFlatField() throws VisADException, RemoteException;

    public abstract FunctionType getFunctionType() throws VisADException;
}
