package visad.data.hdfeos;

import visad.*;
import visad.data.*;
import java.lang.*;
import java.rmi.*;

public class hdfeosAccessor extends FileAccessor
{

   fileData f_data;
   indexSet i_set;

   public hdfeosAccessor( fileData f_data, indexSet i_set )
   {
    
     this.f_data = f_data;
     this.i_set = i_set;
   }

   public FlatField getFlatField() throws VisADException, RemoteException
   {

     return (FlatField) f_data.getVisADDataObject( i_set );

   }

   public FunctionType getFunctionType() throws VisADException
   {

     return (FunctionType) f_data.getVisADMathType();

   }

   public void writeFile( int[] fileLocations, Data range ) 
   {

   }

   public double[][] readFlatField( FlatField template, int[] fileLocation )
   {

     return null;
   }

   public void writeFlatField( double[][] values, FlatField template, int[] fileLocation )
   {


   }
}
