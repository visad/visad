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
 * $Id: FileField.java,v 1.3 1998-02-23 14:33:10 steve Exp $
 */

package visad.data;


import java.rmi.RemoteException;

import visad.Data;
import visad.FieldImpl;
import visad.FlatField;
import visad.FieldException;
import visad.VisADException;
import visad.FunctionType;


public class FileField extends FieldImpl {
  // note FileField extends FieldImpl but may not inherit
  // any of its methods - it must re-implement all of them
  // through the adapted FieldImpl
 
  FieldImpl adaptedField;
 
  // this is the FileAccessor for reading and writing range
  // samples to the adapted file
  FileAccessor fileAccessor;
  // these are the locations in the file for the range samples
  // of this FileField;
  // note fileLocations[index] has type int[] that defines
  // the location for the index-th range sample of this
  // FileField
  int[][] fileLocations;
 
  public FileField(FieldImpl field, FileAccessor accessor,
                   int[][] locations)
    throws FieldException, VisADException
  {
    super((FunctionType)null, field.getDomainSet());

    if (field instanceof FlatField) {
      throw new FieldException("FileField: cannot adapt FlatField");
    }
    adaptedField = field;
    fileAccessor = accessor;
    fileLocations = locations;
  }
 
  // must implement all the methods of Data, Function and Field
  //
  // most are simple adapters, like this:
  public Data getSample(int index)
         throws VisADException, RemoteException {
    return adaptedField.getSample(index);
  }
 
  // setSample changes the contents of this Field,
  // in both the adaptedField and in the file
  public void setSample(int index, Data range)
         throws VisADException, RemoteException {
    // set the index-th range sample in adaptedField
    adaptedField.setSample(index, range);
    // write range sample through to fileAccessor
    fileAccessor.writeFile(fileLocations[index], range);
  }
 
  // setSamples also changes the file contents;
  // it could be implemented as a series of calls to setSample
 
}
