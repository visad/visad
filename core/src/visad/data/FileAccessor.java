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

$Id: FileAccessor.java,v 1.11 2009-03-02 23:35:46 curtis Exp $
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
