//
// hdfeosDefault.java
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
import java.rmi.*;
import java.net.URL;

public class hdfeosDefault extends hdfeos {


  public hdfeosDefault() {

     super("Default");
  }
     
  public DataImpl open( String file_path ) throws VisADException, RemoteException {

    DataImpl data = null;

    hdfeosFile file = new hdfeosFile( file_path );

    data = getDataObject( file );

    return data;

  }

  public DataImpl open( URL url ) {

    return null;
  }

  public void add( String id, Data data, boolean replace ) throws
     BadFormException {

  }

  public void save( String id, Data data, boolean replace ) throws
     BadFormException, RemoteException {

  }

  public FormNode getForms( Data data ) {

    return this;
  }


  MathType getMathType( hdfeosFile file ) throws VisADException
  {

    MathType M_type = null;

    int n_grids = file.getNumberOfGrids();
    int n_swaths = file.getNumberOfSwaths();

    MathType[] types = new MathType[ n_grids + n_swaths ];
 
    for ( int ii = 0; ii < n_grids; ii++ ) {

      fileDataSet f_data = getGridData( null );

        try {

          M_type = f_data.getVisADMathType();
        }
        catch ( VisADException e ) {

          System.out.println( e.getMessage() );
        }
        finally {

          return M_type;
        }
    }

    for ( int ii = 0; ii < n_swaths; ii++ ) {

      fileDataSet f_data = getSwathData( null );

        try {

          M_type = f_data.getVisADMathType();
        }
        catch ( VisADException e ) {

          System.out.println( e.getMessage() );
        }
        finally {

          return M_type;
        }
    }


     TupleType t_type = new TupleType( types );
     return (MathType) t_type;
  }

  DataImpl getDataObject( hdfeosFile file ) throws VisADException, RemoteException 
  {

    DataImpl data = null;

    int n_grids = file.getNumberOfGrids();
    int n_swaths = file.getNumberOfSwaths();

    DataImpl[] datas = new DataImpl[ n_grids + n_swaths ];
    MathType[] types = new MathType[ n_grids + n_swaths ];
 
    if ( n_grids > 0 ) 
    {
      for ( int ii = 0; ii < n_grids; ii++ ) 
      {
        eosGrid grid = file.getGrid(ii);

        fileDataSet f_data = getGridData( grid );

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
    }

    if ( n_swaths > 0 ) 
    {
      for ( int ii = 0; ii < n_swaths; ii++ )
      {
        eosSwath swath = file.getSwath(ii);

        fileDataSet f_data = getSwathData( swath );

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
          types[ n_grids + ii] = data.getType();
          datas[ n_grids + ii] = data;
        }
      }
    }

     
    TupleType t_type = new TupleType( types );
    Tuple tuple = new Tuple( t_type, datas, false );

    return (DataImpl) tuple;
  }

   fileDataSet getGridData( eosGrid Grid ) {

    shape S_obj;
    dimensionSet D_set;
    dimensionSet F_dims;
    dimensionSet G_dims;
    namedDimension dim;
    metaDomainGen m_domain;
    metaDomainSimple m_domainS = null;
    metaDomainMap FF_domain = null;
    metaField S_link = null;
    Vector allm_domain = new Vector();
    int D_size;
    int idx;

    int grid_id = Grid.getGridId();

    fileDataSet file_data = new fileDataSet();
    shapeSet DV_shapeSet = Grid.getShapeSet();
    gctpMap gridMap = Grid.getMap();

    for ( Enumeration e_out = DV_shapeSet.getEnum(); e_out.hasMoreElements(); ) {


      S_obj = (shape)e_out.nextElement();     // this particular data variable group

      D_set = S_obj.getShape();     // dimension set of this variable group

      D_size = D_set.getSize();     // # of dimensions in the set


      F_dims = new dimensionSet();
      G_dims = new dimensionSet();

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

        FF_domain = new metaDomainMap( grid_id, gridMap );
      }

      System.out.println( "< < < < < < < < < < < < < < < < < < < ");
      System.out.println( F_dims.toString() );
      System.out.println( G_dims.toString() );

      variableSet range_var = S_obj.getVariables();
      System.out.println( range_var.toString() );
      metaFlatField m_FField = new metaFlatField( grid_id, FF_domain, range_var );

      if ( F_dims.getSize() == 0 ) {

         file_data.add( (fileData) m_FField );
      }
      else {

      for (  idx = 0; idx < F_dims.getSize(); idx++ ) {  // examine non-geo dimensions

        dim = F_dims.getElement( idx );

        shape c_var = DV_shapeSet.getCoordVar( dim );

        if ( c_var != null ) {

           int n_vars = c_var.getNumberOfVars();

           if ( n_vars == 1 ) {   // coordinate variable, factorable dimension

             variableSet Vset = c_var.getVariables();
             m_domain = new metaDomainGen( grid_id );
             m_domain.addDim( dim );
             m_domain.addVar( Vset.getElement( 0 ) );
             allm_domain.addElement( m_domain );
           }
           else {

              // not yet implemented
           }
        }
        else {   // no coordinate variable present

          m_domainS = new metaDomainSimple( grid_id );
          m_domainS.addDim( dim );
         // allm_domain.addElement( m_domain );
        }

      }


//- make metaFunction objects - - - - - - - - - -

      Vector t_vector = new Vector();
      t_vector.addElement( m_domainS );

      Enumeration enum = t_vector.elements();

      S_link = metaField.getLink( enum, m_FField );

      file_data.add( (fileData) S_link );

           /**
               topNode = FunctionLink.mergeLink( topNode, S_link );

            **/
      }


    } // end outer for loop

    return file_data;
  }

  fileDataSet getSwathData( eosSwath Swath ) throws hdfeosException {
 
    shape S_obj;
    dimensionSet D_set;
    dimensionSet F_dims;
    dimensionSet G_dims;
    variable Latitude = null;
    variable Longitude = null;
    namedDimension dim;
    metaDomainGen m_domain;
    metaDomainSimple m_domainS = null;
    metaDomainGen FF_domain = null;
    metaField S_link = null;
    Vector allm_domain = new Vector();
    int D_size;
    int idx;
    Enumeration e;

    int swath_id = Swath.getSwathId();
    shapeSet DV_shapeSet = Swath.getDV_shapeSet();
    shapeSet GV_shapeSet = Swath.getGV_shapeSet();

    fileDataSet file_data = new fileDataSet();

//*- look for standard required geo-spatial variables - - - - - - - - - - - - -

    for ( e = GV_shapeSet.getEnum(); e.hasMoreElements(); )
    {
      S_obj = (shape)e.nextElement();
      variableSet v_set = S_obj.getVariables();

      Latitude = v_set.getByName( "Latitude" );
      Longitude = v_set.getByName( "Longitude" );
    }

    if (( Latitude == null ) || ( Longitude == null ))
    {
       throw new hdfeosException(
       " expecting Latitude and Longitude geo variables ");
    }

    for ( Enumeration e_out = DV_shapeSet.getEnum(); e_out.hasMoreElements(); ) 
    {
      S_obj = (shape)e_out.nextElement();     // this particular data variable group

      D_set = S_obj.getShape();     // dimension set of this variable group

      D_size = D_set.getSize();     // # of dimensions in the set

      F_dims = new dimensionSet();
      G_dims = new dimensionSet();

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

//- examine geo-dimension sets for this variable group - - - - - - - - - - -
      int g_size = G_dims.getSize();

      if ( g_size == 0 ) 
      {
        throw new hdfeosException(" no geo dimensions found in file " );
      }
      else if ( g_size == 1 )
      {
        throw new hdfeosException(
           " presently, default form cannot intrepret a single geodimension" );
      }
      else if ( g_size == 2 )
      {
        FF_domain = new metaDomainGen( swath_id );
        FF_domain.addDim( G_dims.getElement(0) );
        FF_domain.addDim( G_dims.getElement(1) );
        FF_domain.addVar( Latitude );
        FF_domain.addVar( Longitude );
      }
      else if ( g_size == 3 ) 
      {
        throw new hdfeosException(
           " three geo-dims not yet implemented" );
      }
      else if ( g_size > 3 )
      {
        throw new hdfeosException(
           " > three geo-dims not yet implemented" );
      }


      variableSet range_var = S_obj.getVariables();

      metaFlatField m_FField = new metaFlatField( swath_id, FF_domain, range_var );


      if ( F_dims.getSize() == 0 ) 
      {
         file_data.add( (fileData) m_FField );
      }
      else         //- create functions of flatfield objects defined above
      {
        for (  idx = 0; idx < F_dims.getSize(); idx++ ) {    // loop through non-geo dimensions

          dim = F_dims.getElement( idx );

          shape c_var = DV_shapeSet.getCoordVar( dim );

          if ( c_var != null ) {

             int n_vars = c_var.getNumberOfVars();

             if ( n_vars == 1 ) {   // coordinate variable, factorable dimension

               variableSet Vset = c_var.getVariables();
               m_domain = new metaDomainGen( swath_id );
               m_domain.addDim( dim );
               m_domain.addVar( Vset.getElement( 0 ) );
               allm_domain.addElement( m_domain );
             }
             else {

                // not yet implemented
             }
          }
          else {   // no coordinate variable present

            m_domainS = new metaDomainSimple( swath_id );
            m_domainS.addDim( dim );
           // allm_domain.addElement( m_domain );
          }
         }

//- make metaFunction objects - - - - - - - - - -

        Vector t_vector = new Vector();
        t_vector.addElement( m_domainS );

        Enumeration enum = t_vector.elements();

        S_link = metaField.getLink( enum, m_FField );

        file_data.add( (fileData) S_link );

      }

    } // end outer for loop


    return file_data;
  }

}  // end class
