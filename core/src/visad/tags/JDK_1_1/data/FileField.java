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
