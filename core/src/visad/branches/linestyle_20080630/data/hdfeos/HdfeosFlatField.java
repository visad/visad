//
// HdfeosFlatField.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2008 Bill Hibbard, Curtis Rueden, Tom
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

import java.rmi.*;
import visad.*;
import visad.data.FileFlatField;

public class HdfeosFlatField extends HdfeosData
{
  HdfeosDomain domain;
  Variable[] range_s;
  int n_fields;
  int r_rank;
  int d_rank;
  String[] name_s;
  int num_type[];
  Calibration[] cal;
  FunctionType mathtype;
  EosStruct struct;
  boolean pointStruct = false;

  int[] start = null;
  int[] inv_start = null;
  int[] stride = null;
  int[] inv_stride = null;
  int[] edge = null;
  int[] inv_edge = null;

  DimensionSet rangeDimSet = null;
  DimensionSet domainDimSet = null;

  FlatField data = null;

  public HdfeosFlatField( HdfeosDomain domain, Variable[] range_s )
         throws VisADException,
                UnimplementedException,
                HdfeosException
  {
    initialize( domain, range_s );
  }

  public HdfeosFlatField( HdfeosDomain domain, Variable range )
         throws VisADException,
                UnimplementedException,
                HdfeosException
  {
    Variable[] range_s = { range };
    initialize( domain, range_s );
  }

  public HdfeosFlatField( HdfeosDomain domain, VariableSet range_s )
         throws VisADException,
                UnimplementedException,
                HdfeosException
  {
    initialize( domain, range_s.getElements() );
  }

  private void initialize( HdfeosDomain domain,
                           Variable[] range_s )
          throws VisADException,
                 UnimplementedException,
                 HdfeosException
  {
    this.domain = domain;
    this.struct = domain.getStruct();
    this.n_fields = range_s.length;
    this.name_s = new String[n_fields];
    this.num_type = new int[n_fields];
    this.cal = new Calibration[n_fields];

    for ( int ii = 0; ii < n_fields; ii++ ) {
      name_s[ii] = range_s[ii].getName();
      num_type[ii] = range_s[ii].getNumberType();
      cal[ii] = range_s[ii].getCalibration();
    }

    mathtype = makeType();

    if ( (struct instanceof EosSwath) ||
         (struct instanceof EosGrid) )
    {
      this.rangeDimSet = range_s[0].getDimSet();
      r_rank = rangeDimSet.getSize();
      domainDimSet = domain.getDimSet();
      d_rank = domainDimSet.getSize();

      if ( d_rank > r_rank )
      {
        throw new HdfeosException("d_rank > r_rank");
      }

      start = new int[r_rank];
      inv_start = new int[r_rank];
      edge = new int[r_rank];
      inv_edge = new int[r_rank];
      stride = new int[r_rank];
      inv_stride = new int[r_rank];

      for ( int ii = 0; ii < r_rank; ii++ ) {
        NamedDimension n_dim = rangeDimSet.getElement(ii);
        start[ii] = 0;
        edge[ii] = n_dim.getLength();
        stride[ii] = 1;
      }
    }
    else
    {
      pointStruct = true;
      throw new UnimplementedException("ECS structmetadata: POINT");
    }
  }

  public MathType getType()
         throws VisADException
  {
    return mathtype;
  }

  public DataImpl getData()
         throws VisADException, RemoteException
  {
    return getData(null);
  }

  public DataImpl getAdaptedData()
         throws VisADException, RemoteException
  {
    HdfeosAccessor accessor =
      new HdfeosAccessor( this, null );

    FileFlatField f_field = new FileFlatField( accessor, HdfeosForm.c_strategy );
    return f_field;
  }

  public DataImpl getData( int[] indexes )
         throws VisADException, RemoteException
  {
    Set set = null;
    if ( ! pointStruct )
    {
      if ( indexes == null )
      {
        if ( data != null )
        {
          return data;
        }
        else
        {
          set = domain.getData();
        }
      }
      else
      {
        if ( d_rank == r_rank ) {
          set = domain.getData(indexes);
        }
        else if ( d_rank < r_rank ) {
          set = domain.getData();
        }

        int cnt = 0;
        for ( int ii = 0; ii < indexes.length; ii++ )
        {
          start[cnt] = indexes[ii];
          edge[cnt] = 1;
          cnt++;
        }
      }

      if ( struct instanceof EosSwath ) {
        /**- invert dimension order  --*/
        for ( int ii = 0; ii < r_rank; ii++ )
        {
          inv_start[ii] = start[(r_rank-1) - ii];
          inv_edge[ii] = edge[(r_rank-1) - ii];
          inv_stride[ii] = stride[(r_rank-1) - ii];
        }
      }
      else {
        for ( int ii = 0; ii < r_rank; ii++ )
        {
          inv_start[ii] = start[ii];
          inv_edge[ii] = edge[ii];
          inv_stride[ii] = stride[ii];
        }
      }

      FlatField f_field = new FlatField( mathtype, set );
      int n_samples = set.getLength();
      float[][] f_array = new float[n_fields][n_samples];

      /**- File I/O   ---*/
      for ( int kk = 0; kk < n_fields; kk++ )
      {
        struct.readData(name_s[kk], inv_start, inv_stride, inv_edge,
                        num_type[kk], cal[kk], f_array[kk] );
      }

      f_field.setSamples( f_array );
      data = f_field;

      return data;
    }
    return null;
  }

  public DataImpl getAdaptedData(int[] indexes)
         throws VisADException
  {
    HdfeosAccessor accessor =
      new HdfeosAccessor( this, indexes );
    FileFlatField f_field = new FileFlatField( accessor, HdfeosForm.c_strategy );
    return f_field;
  }

  private FunctionType makeType()
          throws VisADException
  {
    MathType domain_type = this.domain.getType();
    RealType r_type;
    RealType[] r_types = new RealType[n_fields];

    for ( int ii = 0; ii < n_fields; ii++ )
    {
      r_types[ii] = RealType.getRealType( name_s[ii], null, null );
    }

    MathType range_type;
    if ( n_fields == 1 ) {
      range_type = r_types[0];
    }
    else {
      range_type = new RealTupleType(r_types);
    }
    FunctionType f_type = new FunctionType(domain_type, range_type);
    return f_type;
  }
}
