//
// HdfeosForm.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2014 Bill Hibbard, Curtis Rueden, Tom
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

package visad.data.hdfeos;

import java.util.Vector;
import java.util.Enumeration;
import visad.*;
import visad.data.*;
import visad.UnimplementedException;
import java.rmi.*;
import java.net.URL;

public class HdfeosForm extends Hdfeos
{
  static CacheStrategy c_strategy = new CacheStrategy();

  public HdfeosForm()
  {
    super("Default");
  }

  HdfeosForm( String formName )
  {
    super( formName );
  }

  public DataImpl open( String file_path )
         throws VisADException, RemoteException
  {
    DataImpl data = null;

    HdfeosFile file = new HdfeosFile( file_path );

    data = getFileData( file );

    return data;
  }

  public DataImpl open( URL url )
         throws VisADException
  {
    throw new UnimplementedException( "HdfeosForm.open( URL url )" );
  }

  public void add( String id, Data data, boolean replace )
              throws BadFormException
  {
    throw new BadFormException( "HdfeosForm.add" );
  }

  public void save( String id, Data data, boolean replace )
         throws BadFormException, RemoteException, VisADException
  {
    throw new UnimplementedException( "HdfeosForm.save" );
  }

  public FormNode getForms( Data data )
  {
    return this;
  }

  MathType getMathType( HdfeosFile file )
           throws VisADException, RemoteException
  {
    MathType M_type = null;
    HdfeosData data = null;

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
        data = getGridData( (EosGrid)obj );
      }
      else if ( obj instanceof EosSwath )
      {
        data = getSwathData( (EosSwath)obj );
      }

      try
      {
        M_type = data.getType();
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

  DataImpl getFileData( HdfeosFile file )
           throws VisADException, RemoteException
  {
    DataImpl data = null;
    HdfeosData f_data = null;

    int n_structs = file.getNumberOfStructs();
    if ( n_structs == 0 )
    {
      throw new HdfeosException("no EOS data structures in file: "+file.getFileName());
    }

    HdfeosData[] datas = new HdfeosData[ n_structs ];

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

      datas[ii] = f_data;
    }
    return assembleStructs( datas );
  }

  private DataImpl assembleStructs( HdfeosData[] h_datas )
          throws VisADException, RemoteException
  {
    DataImpl fileData = null;
    int n_structs = h_datas.length;

    if ( n_structs == 1 )
    {
      return getVisADDataObject( h_datas[0] );
    }
    else
    {
      boolean types_equal = true;
      MathType first_type;
      MathType[] types = new MathType[ n_structs ];
      DataImpl[] datas = new DataImpl[ n_structs ];

      datas[0] = getVisADDataObject( h_datas[0] );
      types[0] = datas[0].getType();
      first_type = types[0];
      for ( int ii = 1; ii < n_structs; ii++ ) {
        datas[ii] = getVisADDataObject( h_datas[ii] );
        types[ii] = datas[ii].getType();
        if ( !(types[ii].equals(first_type)) ) {
          types_equal = false;
        }
      }

      if ( types_equal )
      {
        RealType struct_id = RealType.getRealType("struct_id");
        Integer1DSet domain = new Integer1DSet(struct_id, n_structs);
        FieldImpl field = new FieldImpl(new FunctionType((MathType) struct_id,
                                                       first_type), domain);
        for ( int ii = 0; ii < n_structs; ii++ ) {
          field.setSample(ii, datas[ii]);
        }
        fileData = field;
      }
      else
      {
        TupleType t_type = new TupleType( types );
        fileData = new Tuple( t_type, datas, false );
      }

      return fileData;
    }
  }

  DataImpl getVisADDataObject( HdfeosData h_data )
           throws VisADException, RemoteException
  {
    return h_data.getData();
  }

  HdfeosData getGridData( EosGrid grid )
             throws HdfeosException,
                    VisADException,
                    RemoteException
  {
    Shape s_obj;
    DimensionSet D_set;
    DimensionSet G_dims;
    DimensionSet D_dims;
    NamedDimension dim;
    Variable var;
    HdfeosDomain geo_domain;
    HdfeosDomain domain;
    HdfeosFlatField f_field;
    HdfeosField field;
    Vector datas = new Vector();
    int d_size;

    ShapeSet DV_shapeSet = grid.getShapeSet();
    GctpMap gridMap = grid.getMap();

    VariableSet vars_1D = DV_shapeSet.get1DVariables();

    for ( Enumeration e_out = DV_shapeSet.getEnum(); e_out.hasMoreElements(); )
    {
      s_obj = (Shape)e_out.nextElement();     // this particular data Variable group

      D_set = s_obj.getShape();     // dimension set of this Variable group

      d_size = D_set.getSize();     // # of dimensions in the set

      VariableSet range_var = s_obj.getVariables();

      G_dims = new DimensionSet();
      D_dims = new DimensionSet();

      for ( int ii = 0; ii < d_size; ii++ ) //-- separate dimensions first
      {
        dim = D_set.getElement(ii);

        if( ((dim.getName()).equals("XDim")) ||
            ((dim.getName()).equals("YDim")) )
        {
          G_dims.add( dim );
        }
        else
        {
          D_dims.add( dim );
        }
      }

      if ( G_dims.getSize() != 2 )
      {
        domain = new HdfeosDomain( grid, D_set );
        f_field = new HdfeosFlatField( domain, range_var );
        datas.addElement(f_field);
        continue;
      }
      else
      {
        geo_domain = new HdfeosDomainMap(grid, G_dims, gridMap);
      }

      f_field = new HdfeosFlatField( geo_domain, range_var );

      if ( D_dims.getSize() == 0 )
      {
        datas.addElement(f_field);
      }
      else
      {
        field = makeField( grid, D_dims, f_field );
        datas.addElement(field);
      }
    }

    int n_datas = datas.size();
    if ( n_datas == 0 ) {
      return null;
    }
    if ( n_datas == 1 ) {
      return (HdfeosData) datas.elementAt(0);
    }
    else {
      HdfeosData[] array = new HdfeosData[n_datas];
      for ( int ii = 0; ii < n_datas; ii++ ) {
        array[ii] = (HdfeosData) datas.elementAt(ii);
      }
      return new HdfeosTuple(array);
    }
  }

  HdfeosData getSwathData( EosSwath swath )
             throws VisADException,
                    RemoteException,
                    HdfeosException
  {
    Shape s_obj;
    DimensionSet D_set;
    DimensionSet F_dims = null;
    DimensionSet G_dims;
    DimensionSet Geo_set = null;
    DimensionSet D_dims;
    Variable Latitude = null;
    Variable Longitude = null;
    Variable Time = null;
    Variable var;
    VariableSet range_var;
    VariableSet v_set;
    NamedDimension dim;
    HdfeosDomain domain = null;
    HdfeosDomain geo_domain = null;
    HdfeosFlatField f_field = null;
    HdfeosField field = null;
    int d_size;
    int op;
    int g_rank;

    ShapeSet DV_shapeSet = swath.getDV_shapeSet();
    ShapeSet GV_shapeSet = swath.getGV_shapeSet();

    Vector datas = new Vector();

//*- look for Swath geo-spatial-time Variables - - - - - - - - - - - - -

    for ( Enumeration e = GV_shapeSet.getEnum(); e.hasMoreElements(); )
    {
      s_obj = (Shape)e.nextElement();
      v_set = s_obj.getVariables();

      var = v_set.getByName("Latitude");
      if ( var != null ) {
        Latitude = var;
      }
      var = v_set.getByName("Longitude");
      if ( var != null ) {
        Longitude = var;
      }
      var = v_set.getByName("Time");
      if ( Time != null ) {
        Time = var;
      }
    }

    if (( Latitude == null ) || ( Longitude == null ))
    {
      geo_domain = null;
    }
    else
    {
      Variable[] g_vars = { Longitude, Latitude };
      Geo_set = Longitude.getDimSet();

      if ( (Latitude.getDimSet()).sameSetSameOrder( Geo_set ) )
      {
        g_rank = Latitude.getDimSet().getSize();
        if ( g_rank == 1 )
        {
          geo_domain = null;
          domain = new HdfeosDomain(swath, Geo_set);
          f_field = new HdfeosFlatField( domain, g_vars );
          datas.addElement(f_field);
        }
        else if ( g_rank == 2 )
        {
          geo_domain = new HdfeosDomain(swath, g_vars, Geo_set.getElements());
        }
        else if ( g_rank > 2 )
        {
          geo_domain = null;
          domain = new HdfeosDomain(swath, Geo_set);
          f_field = new HdfeosFlatField( domain, g_vars );
          datas.addElement(f_field);
        }
      }
      else  //- Latitude/Longitude variables have different shapes
      {
        geo_domain = null;
        domain = new HdfeosDomain(swath, Latitude.getDimSet());
        f_field = new HdfeosFlatField( domain, Latitude );
        datas.addElement(f_field);

        domain = new HdfeosDomain(swath, Longitude.getDimSet());
        f_field = new HdfeosFlatField( domain, Longitude );
        datas.addElement(f_field);
      }
    }
    if ( Time != null )
    {
      domain = new HdfeosDomain(swath, Time.getDimSet());
      f_field = new HdfeosFlatField( domain, Time );
      datas.addElement(f_field);
    }

    for ( Enumeration e_out = DV_shapeSet.getEnum(); e_out.hasMoreElements(); )
    {
      s_obj = (Shape)e_out.nextElement();     // this particular data Variable group
      D_set = s_obj.getShape();     // dimension set of this Variable group

      d_size = D_set.getSize();     // # of dimensions in the set

      range_var = s_obj.getVariables();

      if ( geo_domain == null )
      {
        domain = new HdfeosDomain( swath, D_set );
        f_field = new HdfeosFlatField( domain, range_var );
        datas.addElement(f_field);
        continue;
      }

      G_dims = new DimensionSet();
      D_dims = new DimensionSet();

      for ( int ii = 0; ii < d_size; ii++ ) //- separate dimensions ( geo, non-geo )
      {
        dim = D_set.getElement( ii );

        if ((dim.isGeoMapDefined())||(GV_shapeSet.isMemberOf(dim)))
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

      if ( g_size == 0 || g_size == 1 )
      {
        domain = new HdfeosDomain(swath, D_set);
        f_field = new HdfeosFlatField(domain, range_var);
        datas.addElement(f_field);
      }
      else if ( g_size == 2 )
      {
        F_dims = D_dims;
        HdfeosDomain img_domain = new HdfeosDomain( swath, G_dims, geo_domain );
        f_field = new HdfeosFlatField( img_domain, range_var );
        if ( F_dims.getSize() == 0 )
        {
          datas.add(f_field);
        }
        else
        {
          int len = F_dims.getSize();
          boolean any = false;
          for ( int ii = 0; ii < len; ii++ ) {
            if ( (D_set.getIndexOf(F_dims.getElement(ii))) >
                 (D_set.getIndexOf(G_dims.getElement(1))) )
            {
              any = true;
            }
          }
          if ( any ) {
            domain = new HdfeosDomain(swath, D_set);
            f_field = new HdfeosFlatField(domain, range_var);
            datas.addElement(f_field);
          }
          else {
            field = makeField( swath, F_dims, f_field );
            datas.add(field);
          }
        }
      }
      else if ( g_size >= 3 )
      {
        domain = new HdfeosDomain(swath, D_set);
        f_field = new HdfeosFlatField(domain, range_var);
        datas.addElement(f_field);
      }
    }
    int n_datas = datas.size();
    if ( n_datas == 0 ) {
      return null;
    }
    if ( n_datas == 1 ) {
      return (HdfeosData) datas.elementAt(0);
    }
    else {
      HdfeosData[] array = new HdfeosData[n_datas];
      for ( int ii = 0; ii < n_datas; ii++ ) {
        array[ii] = (HdfeosData) datas.elementAt(ii);
      }
      return new HdfeosTuple(array);
    }
  }

  HdfeosField makeField( EosStruct struct,
                         DimensionSet F_dims,
                         HdfeosData t_data )
              throws VisADException,
                     RemoteException,
                     HdfeosException
  {
    HdfeosData range = t_data;
    HdfeosDomain domain = null;
    HdfeosField field = null;
    for ( int ii = (F_dims.getSize() - 1); ii >= 0; ii-- )
    {
      domain = new HdfeosDomain( struct, F_dims.getElement(ii));
      field = new HdfeosField( domain, range );
      range = field;
    }
    return field;
  }
}
