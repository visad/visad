//
// HdfeosDefault.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden, Tom
Rink and Dave Glowacki.

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 1, or (at your option)
any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License in file NOTICE for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/

package visad.data.hdfeos;

import java.util.*; 
import visad.*;
import visad.data.*;
import visad.UnimplementedException;
import java.rmi.*;
import java.net.URL;

public class HdfeosDefault extends Hdfeos {


  public HdfeosDefault() {

     super("Default");
  }
     
  public DataImpl open( String file_path ) 
    throws VisADException, RemoteException 
  {

    DataImpl data = null;

    HdfeosFile file = new HdfeosFile( file_path );

    data = getDataObject( file );

    return data;

  }

  public DataImpl open( URL url ) 
    throws VisADException 
  {
    throw new UnimplementedException( "HdfeosDefault.open( URL url )" );
  }

  public void add( String id, Data data, boolean replace ) throws
     BadFormException {
 
    throw new BadFormException( "HdfeosDefault.add" );
  }

  public void save( String id, Data data, boolean replace )
    throws BadFormException, RemoteException, VisADException 
  {
    throw new UnimplementedException( "HdfeosDefault.save" );
  }

  public FormNode getForms( Data data ) 
  {
    return this;
  }


  MathType getMathType( HdfeosFile file ) throws VisADException
  {

    MathType M_type = null;
    FileDataSet f_data = null;

    int n_structs = file.getNumberOfStructs();
    if ( n_structs == 0 )
    {
       throw new HdfeosException("no HDF-EOS data structures in file: "+file.getFileName());
    }

    MathType[] types = new MathType[ n_structs ];
 
    for ( int ii = 0; ii < n_structs; ii++ ) 
    {
      EosStruct obj = file.getStruct(ii);

      if ( obj instanceof EosGrid )
      {
        f_data = getGridData( (EosGrid)obj );
      }
      else if ( obj instanceof EosSwath )
      {
        f_data = getSwathData( (EosSwath)obj );
      }

      try
      {
        M_type = f_data.getVisADMathType();
      }
      catch ( VisADException e ) 
      {
        System.out.println( e.getMessage() );
      }
      finally 
      {
        types[ii] = M_type; 
      }
    }

     TupleType t_type = new TupleType( types );
     return (MathType) t_type;
  }

  DataImpl getDataObject( HdfeosFile file ) 
    throws VisADException, RemoteException 
  {

    DataImpl data = null;
    FileDataSet f_data = null;

    int n_structs = file.getNumberOfStructs();
    if ( n_structs == 0 ) 
    {
      throw new HdfeosException("no EOS data structures in file: "+file.getFileName());
    }

    DataImpl[] datas = new DataImpl[ n_structs ];
    MathType[] types = new MathType[ n_structs ];
 
    for ( int ii = 0; ii < n_structs; ii++ ) 
    {
      EosStruct obj = file.getStruct(ii);

      if ( obj instanceof EosGrid )
      {
        f_data = getGridData( (EosGrid)obj );
      }
      else if ( obj instanceof EosSwath )
      {
        f_data = getSwathData( (EosSwath)obj );
      }

      try 
      {
        data = f_data.getVisADDataObject();
      }
      catch ( VisADException e ) 
      {
        System.out.println( e.getMessage() );
      }
      catch ( RemoteException e ) 
      {
        System.out.println( e.getMessage() );
      }
      finally 
      {
        types[ii] = data.getType();
        datas[ii] = data;
      }
    }

    TupleType t_type = new TupleType( types );
    Tuple tuple = new Tuple( t_type, datas, false );

    return (DataImpl) tuple;
  }

   FileDataSet getGridData( EosGrid Grid ) 
   {

    Shape S_obj;
    DimensionSet D_set;
    DimensionSet F_dims;
    DimensionSet G_dims;
    NamedDimension dim;
    MetaDomainGen m_domain;
    MetaDomainSimple m_domainS = null;
    MetaDomainMap FF_domain = null;
    MetaField S_link = null;
    Vector allm_domain = new Vector();
    int D_size;
    int idx;

    int grid_id = Grid.getGridId();

    FileDataSet file_data = new FileDataSet();
    ShapeSet DV_shapeSet = Grid.getShapeSet();
    GctpMap gridMap = Grid.getMap();

    for ( Enumeration e_out = DV_shapeSet.getEnum(); e_out.hasMoreElements(); ) {


      S_obj = (Shape)e_out.nextElement();     // this particular data Variable group

      D_set = S_obj.getShape();     // dimension set of this Variable group

      D_size = D_set.getSize();     // # of dimensions in the set


      F_dims = new DimensionSet();
      G_dims = new DimensionSet();

      for ( idx = 0; idx < D_size; idx++ ) {   // separate dimensions first

        dim = D_set.getElement( idx );

        if( ((dim.getName()).equals("XDim")) || ((dim.getName()).equals("YDim")) ) {

          G_dims.add( dim );
        }
        else {

          F_dims.add( dim );
        }
      }

      if ( G_dims.getSize() != 2 ) {
         /* throw exception */
      }
      else {

        FF_domain = new MetaDomainMap( grid_id, gridMap );
      }

      System.out.println( "< < < < < < < < < < < < < < < < < < < ");
      System.out.println( F_dims.toString() );
      System.out.println( G_dims.toString() );

      VariableSet range_var = S_obj.getVariables();
      System.out.println( range_var.toString() );
      MetaFlatField m_FField = new MetaFlatField( grid_id, FF_domain, range_var );

      if ( F_dims.getSize() == 0 ) {

         file_data.add( (FileData) m_FField );
      }
      else {

      for (  idx = 0; idx < F_dims.getSize(); idx++ ) {  // examine non-geo dimensions

        dim = F_dims.getElement( idx );

        Shape c_var = DV_shapeSet.getCoordVar( dim );

        if ( c_var != null ) {

           int n_vars = c_var.getNumberOfVars();

           if ( n_vars == 1 ) {   // coordinate Variable, factorable dimension

             VariableSet Vset = c_var.getVariables();
             m_domain = new MetaDomainGen( grid_id );
             m_domain.addDim( dim );
             m_domain.addVar( Vset.getElement( 0 ) );
             allm_domain.addElement( m_domain );
           }
           else {

              // not yet implemented
           }
        }
        else {   // no coordinate Variable present

          m_domainS = new MetaDomainSimple( grid_id );
          m_domainS.addDim( dim );
         // allm_domain.addElement( m_domain );
        }

      }


//- make metaFunction objects - - - - - - - - - -

      Vector t_vector = new Vector();
      t_vector.addElement( m_domainS );

      Enumeration enum = t_vector.elements();

      S_link = MetaField.getLink( enum, m_FField );

      file_data.add( (FileData) S_link );

           /**
               topNode = FunctionLink.mergeLink( topNode, S_link );

            **/
      }


    } // end outer for loop

    return file_data;
  }

  FileDataSet getSwathData( EosSwath Swath ) throws HdfeosException {
 
    Shape S_obj;
    DimensionSet D_set;
    DimensionSet F_dims;
    DimensionSet G_dims;
    Variable Latitude = null;
    Variable Longitude = null;
    NamedDimension dim;
    MetaDomainGen m_domain;
    MetaDomainSimple m_domainS = null;
    MetaDomainGen FF_domain = null;
    MetaField S_link = null;
    Vector allm_domain = new Vector();
    int D_size;
    int idx;
    Enumeration e;

    int swath_id = Swath.getSwathId();
    ShapeSet DV_shapeSet = Swath.getDV_shapeSet();
    ShapeSet GV_shapeSet = Swath.getGV_shapeSet();

    FileDataSet file_data = new FileDataSet();

//*- look for standard required geo-spatial Variables - - - - - - - - - - - - -

    for ( e = GV_shapeSet.getEnum(); e.hasMoreElements(); )
    {
      S_obj = (Shape)e.nextElement();
      VariableSet v_set = S_obj.getVariables();

      Latitude = v_set.getByName( "Latitude" );
      Longitude = v_set.getByName( "Longitude" );
    }

    if (( Latitude == null ) || ( Longitude == null ))
    {
       throw new HdfeosException(
       " expecting Latitude and Longitude geo Variables ");
    }

    for ( Enumeration e_out = DV_shapeSet.getEnum(); e_out.hasMoreElements(); ) 
    {
      S_obj = (Shape)e_out.nextElement();     // this particular data Variable group

      D_set = S_obj.getShape();     // dimension set of this Variable group

      D_size = D_set.getSize();     // # of dimensions in the set

      F_dims = new DimensionSet();
      G_dims = new DimensionSet();

      for ( int ii = 0; ii < D_size; ii++ )        // separate dimensions ( geo, non-geo )
      {
        dim = D_set.getElement( ii );

        if ( dim.isGeoMapDefined() ) 
        {
          G_dims.add( dim );
        }
        else if ( GV_shapeSet.isMemberOf( dim ) ) 
        {
          G_dims.add( dim );
        }
        else 
        {
          F_dims.add( dim );
        }
      }

//- examine geo-dimension sets for this Variable group - - - - - - - - - - -
      int g_size = G_dims.getSize();

      if ( g_size == 0 ) 
      {
        throw new HdfeosException(" no geo dimensions found in file " );
      }
      else if ( g_size == 1 )
      {
        throw new HdfeosException(
           " presently, default form cannot intrepret a single geodimension" );
      }
      else if ( g_size == 2 )
      {
        FF_domain = new MetaDomainGen( swath_id );
        FF_domain.addDim( G_dims.getElement(0) );
        FF_domain.addDim( G_dims.getElement(1) );
        FF_domain.addVar( Latitude );
        FF_domain.addVar( Longitude );
      }
      else if ( g_size == 3 ) 
      {
        throw new HdfeosException(
           " three geo-dims not yet implemented" );
      }
      else if ( g_size > 3 )
      {
        throw new HdfeosException(
           " > three geo-dims not yet implemented" );
      }


      VariableSet range_var = S_obj.getVariables();

      MetaFlatField m_FField = new MetaFlatField( swath_id, FF_domain, range_var );


      if ( F_dims.getSize() == 0 ) 
      {
         file_data.add( (FileData) m_FField );
      }
      else         //- create functions of flatfield objects defined above
      {
        for (  idx = 0; idx < F_dims.getSize(); idx++ ) {    // loop through non-geo dimensions

          dim = F_dims.getElement( idx );

          Shape c_var = DV_shapeSet.getCoordVar( dim );

          if ( c_var != null ) {

             int n_vars = c_var.getNumberOfVars();

             if ( n_vars == 1 ) {   // coordinate Variable, factorable dimension

               VariableSet Vset = c_var.getVariables();
               m_domain = new MetaDomainGen( swath_id );
               m_domain.addDim( dim );
               m_domain.addVar( Vset.getElement( 0 ) );
               allm_domain.addElement( m_domain );
             }
             else {

                // not yet implemented
             }
          }
          else {   // no coordinate Variable present

            m_domainS = new MetaDomainSimple( swath_id );
            m_domainS.addDim( dim );
           // allm_domain.addElement( m_domain );
          }
         }

//- make metaFunction objects - - - - - - - - - -

        Vector t_vector = new Vector();
        t_vector.addElement( m_domainS );

        Enumeration enum = t_vector.elements();

        S_link = MetaField.getLink( enum, m_FField );

        file_data.add( (FileData) S_link );

      }

    } // end outer for loop


    return file_data;
  }

}  // end class
