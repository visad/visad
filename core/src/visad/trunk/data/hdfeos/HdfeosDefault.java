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

public class HdfeosDefault extends Hdfeos 
{

  public HdfeosDefault() 
  {
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

  public void add( String id, Data data, boolean replace ) 
              throws BadFormException
  {
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


  MathType getMathType( HdfeosFile file ) 
           throws VisADException
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
    if ( types.length > 1 ) {
      M_type = new TupleType( types );
    }
    else {
      M_type = types[0];
    }

    return M_type;
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

    if ( types.length > 1 ) {
      TupleType t_type = new TupleType( types );
      Tuple tuple = new Tuple( t_type, datas, false );
      return (DataImpl) tuple;
    }
    else {
      return datas[0];
    }
  }

   FileDataSet getGridData( EosGrid Grid ) 
               throws HdfeosException
   {

    Shape S_obj;
    DimensionSet D_set;
    DimensionSet G_dims;
    DimensionSet D_dims;
    NamedDimension dim;
    MetaDomainGen m_domain;
    MetaDomainSimple m_domainS = null;
    MetaDomainMap FF_domain = null;
    MetaField S_link = null;
    Vector allm_domain = new Vector();
    int D_size;
    int idx;


    FileDataSet file_data = new FileDataSet();
    ShapeSet DV_shapeSet = Grid.getShapeSet();
    GctpMap gridMap = Grid.getMap();

    for ( Enumeration e_out = DV_shapeSet.getEnum(); e_out.hasMoreElements(); ) 
    {

      S_obj = (Shape)e_out.nextElement();     // this particular data Variable group

      D_set = S_obj.getShape();     // dimension set of this Variable group

      D_size = D_set.getSize();     // # of dimensions in the set

      if ( D_size == 0 ) 
      {
         throw new HdfeosException(" number of dimension equals zero ");
      }
      if ( D_size == 1 )  // coordinate variable
      {
         continue;
      }

      G_dims = new DimensionSet();
      D_dims = new DimensionSet();

      for ( idx = 0; idx < D_size; idx++ )    // separate dimensions first 
      {
        dim = D_set.getElement( idx );

        if( ((dim.getName()).equals("XDim")) || ((dim.getName()).equals("YDim")) ) 
        {
          G_dims.add( dim );
        }
        else 
        {
          D_dims.add( dim );
        }
      }

      if ( G_dims.getSize() > 2 ) 
      {
        throw new HdfeosException(" number of geo-dimensions > 2 ");
      }
      else 
      {
        FF_domain = new MetaDomainMap( Grid, gridMap );
      }

      VariableSet range_var = S_obj.getVariables();
      MetaFlatFieldTuple m_FField = new MetaFlatFieldTuple( Grid, FF_domain, range_var );

      if ( D_dims.getSize() == 0 ) 
      {
         file_data.add( (FileData) m_FField );
      }
      else 
      {
         S_link = makeMetaField( Grid, DV_shapeSet, D_dims, (FileData)m_FField );
         file_data.add( (FileData) S_link );
      }

    } // end outer for loop

    return file_data;
  }

  FileDataSet getSwathData( EosSwath Swath ) 
              throws HdfeosException 
  {
 
    Shape S_obj;
    DimensionSet D_set;
    DimensionSet F_dims = null;
    DimensionSet G_dims;
    DimensionSet Geo_set;
    DimensionSet D_dims;
    Variable Latitude = null;
    Variable Longitude = null;
    Variable Time = null;
    Variable var;
    VariableSet range_var;
    VariableSet v_set;
    NamedDimension dim;
    MetaDomain m_domain;
    MetaDomainSimple m_domainS = null;
    MetaDomain FF_domain = null;
    MetaDomain Geo_domain = null;
    MetaFlatField m_FField;
    MetaField S_link = null;
    Vector allm_domain = new Vector();
    int D_size;
    int op;
    int g_rank;
    Enumeration e;

    ShapeSet DV_shapeSet = Swath.getDV_shapeSet();
    ShapeSet GV_shapeSet = Swath.getGV_shapeSet();

    FileDataSet file_data = new FileDataSet();

//*- look for standard required geo-spatial Variables - - - - - - - - - - - - -

    for ( e = GV_shapeSet.getEnum(); e.hasMoreElements(); )
    {
      S_obj = (Shape)e.nextElement();
      v_set = S_obj.getVariables();

      var = v_set.getByName( "Latitude" );
      if ( var != null ) {
        Latitude = var;
      }
      var = v_set.getByName( "Longitude" );
      if ( var != null ) {
        Longitude = var;
      }
      var = v_set.getByName( "Time" );
      if ( Time != null ) {
        Time = var;
      }
    }

    if (( Latitude == null ) || ( Longitude == null ))
    {
       throw new HdfeosException(
       " expecting Latitude and Longitude geolocation Variables ");
    }
    Geo_set = Longitude.getDimSet();

    if ( (Latitude.getDimSet()).sameSetSameOrder( Geo_set ) )
    {
       g_rank = Latitude.getDimSet().getSize();

       if ( g_rank == 1 )
       {
          v_set = new VariableSet();

          if ( Time != null ) {
            if ( Time.getDimSet().getSize() != 1 )
            {
              throw new HdfeosException(" Time should have rank one if Lat/Lon do also ");
            }
            v_set.add( Time );
          }

          v_set.add( Latitude );
          v_set.add( Longitude ); 
          MetaDomainSimple FF_domain2 = new MetaDomainSimple( Swath );
          FF_domain2.addDim( Geo_set.getElement(0) );
          MetaFlatFieldTuple obj = new MetaFlatFieldTuple( Swath, FF_domain2 , v_set );

          file_data.add( (FileData) obj );
       }
       else if ( g_rank == 2 )
       {  
          Geo_domain = new MetaDomainGen( Swath );
          ((MetaDomainGen)Geo_domain).addDim( Geo_set.getElement(0) );
          ((MetaDomainGen)Geo_domain).addDim( Geo_set.getElement(1) );
          ((MetaDomainGen)Geo_domain).addVar( Latitude );
          ((MetaDomainGen)Geo_domain).addVar( Longitude );
       }
       else if ( g_rank == 3 )
       {
          Geo_domain = new MetaDomainGen( Swath );
          ((MetaDomainGen)Geo_domain).addDim( Geo_set.getElement(1) );
          ((MetaDomainGen)Geo_domain).addDim( Geo_set.getElement(2) );
          ((MetaDomainGen)Geo_domain).addVar( Latitude );
          ((MetaDomainGen)Geo_domain).addVar( Longitude );
          F_dims = new DimensionSet();
          F_dims.add( Geo_set.getElement(0) );
       }
       else if ( g_rank > 3 )
       {
          throw new HdfeosException(
                   " Lat/Lon variables have rank > 3 " );
       }
    }
    else
    {
      throw new HdfeosException(
          " expecting Latitude and Longitude to have the same dimensionSet ");
    }

    for ( Enumeration e_out = DV_shapeSet.getEnum(); e_out.hasMoreElements(); ) 
    {
      S_obj = (Shape)e_out.nextElement();     // this particular data Variable group
      System.out.println( S_obj.toString());

      D_set = S_obj.getShape();     // dimension set of this Variable group

      D_size = D_set.getSize();     // # of dimensions in the set

      if ( D_size == 1 )    // Treat this as a coordinate variable - metadata
      {
         continue;
      }

      G_dims = new DimensionSet();
      D_dims = new DimensionSet();
      range_var = S_obj.getVariables();

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
          D_dims.add( dim );
        }
      }

//- examine geo-dimension sets for this Variable group - - - - - - - - - - -
      int g_size = G_dims.getSize();

      /*
          FF_domain = makeDomain( Swath, DV_shapeSet, D_set );
          m_FField = new MetaFlatField( Swath, FF_domain, range_var );
          file_data.add( (FileData) m_FField );
       */

      if ( g_size == 0 ) 
      {
        FF_domain = makeDomain( Swath, DV_shapeSet, D_dims );
        m_FField = new MetaFlatField( Swath, FF_domain, range_var );
        file_data.add( (FileData) m_FField );
      }
      else if ( g_size == 1 )
      {
        if ( Geo_set.sameSetSameOrder(G_dims) )
        {
          m_domain = makeDomain( Swath, DV_shapeSet, D_dims );
          m_FField = new MetaFlatField( Swath, m_domain, range_var);
          S_link = makeMetaField( Swath, DV_shapeSet, G_dims, m_FField );
          file_data.add( (FileData) S_link );
        }
        else
        {
          m_domain = makeDomain( Swath, DV_shapeSet, D_set );
          m_FField = new MetaFlatField( Swath, m_domain, range_var );
          file_data.add( (FileData) m_FField );
        }
      }
      else if ( g_size == 2 )
      {
        if ( Geo_set.sameSetSameOrder(G_dims) )
        {
          F_dims = D_dims;
          m_FField = new MetaFlatField( Swath, Geo_domain, range_var );
          if ( F_dims.getSize() == 0 )
          {
            file_data.add( (FileData) m_FField );
          }
          else         //- create functions of flatfield objects defined above
          {
            S_link = makeMetaField( Swath, DV_shapeSet, F_dims, (FileData)m_FField );
            file_data.add( (FileData) S_link );
          }
        }
        else
        {
          FF_domain = makeDomain( Swath, DV_shapeSet, D_set );
          m_FField = new MetaFlatField( Swath, FF_domain, range_var);
          file_data.add( (FileData) m_FField );
        }
      }
      else if ( g_size == 3 ) 
      {
        if ( Geo_set.sameSetSameOrder(G_dims) )
        {
          for ( int ii = 0; ii < D_dims.getSize(); ii++ ) {
            F_dims.add( D_dims.getElement(ii) );
          }
          m_FField = new MetaFlatField( Swath, Geo_domain, range_var );
          S_link = makeMetaField( Swath, DV_shapeSet, F_dims, (FileData)m_FField );
          file_data.add( (FileData) S_link );
        }
        else
        {
          FF_domain = makeDomain( Swath, DV_shapeSet, D_set );
          m_FField = new MetaFlatField( Swath, FF_domain, range_var);
          file_data.add( (FileData) m_FField );
        }
      }
      else if ( g_size > 3 )
      {
        throw new HdfeosException(
           "more than three geo-dims not yet implemented" );
      }

    } // end outer for loop

    return file_data;
  }

  MetaField makeMetaField( EosStruct struct, ShapeSet DV_shapeSet, 
                           DimensionSet F_dims, FileData F_field  )
            throws HdfeosException
  {

     MetaDomain m_domain = null;
     Vector all = new Vector();
     MetaField m_field = null;

     for ( int ii = 0; ii < F_dims.getSize(); ii++ ) 
     {
        NamedDimension dim = F_dims.getElement( ii );
        Shape c_var = DV_shapeSet.getCoordVar( dim );

        if ( c_var != null ) 
        {
           int n_vars = c_var.getNumberOfVars();
           if ( n_vars == 1 )
           {
              VariableSet Vset = c_var.getVariables();
              m_domain = new MetaDomainGen( struct );
              ((MetaDomainGen)m_domain).addDim( dim );
              ((MetaDomainGen)m_domain).addVar( Vset.getElement( 0 ) );
              all.addElement( m_domain );
           }
           else
           {
              throw new HdfeosException(
                        "more than one coord var for this dimension");
           }
        }
        else  // no coordinate Variable present
        {
           m_domain = new MetaDomainSimple( struct );
           ((MetaDomainSimple)m_domain).addDim( dim );
           all.addElement( m_domain );
        }
     }

     Enumeration enum = all.elements();

     m_field = MetaField.getLink( enum, F_field );

     all = null;
     return m_field;
  }

  MetaDomain makeDomain( EosStruct struct, ShapeSet DV_shapeSet, DimensionSet D_set )
             throws HdfeosException
  {

    MetaDomain m_domain = new MetaDomainGen( struct );

    for ( int ii = 0; ii < D_set.getSize(); ii++ )
    {
      NamedDimension dim = D_set.getElement(ii);

        Shape c_var = DV_shapeSet.getCoordVar( dim );

        if ( c_var != null )
        {
           int n_vars = c_var.getNumberOfVars();
           if ( n_vars == 1 )
           {
              VariableSet Vset = c_var.getVariables();
              ((MetaDomainGen)m_domain).addDim( dim );
              ((MetaDomainGen)m_domain).addVar( Vset.getElement( 0 ) );
           }
           else
           {
              throw new HdfeosException(
                        "more than one coord var for this dimension:"+dim.toString() );
           }
        }
        else  // no coordinate Variable present
        {
           ((MetaDomainGen)m_domain).addDim( dim );
           ((MetaDomainGen)m_domain).addVar( null );
        }
    }

    return m_domain;
  }

}  // end class
