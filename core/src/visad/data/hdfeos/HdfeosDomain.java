//
// HdfeosDomain.java
//

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
*/

package visad.data.hdfeos;

import visad.Set;
import visad.MathType;
import visad.RealType;
import visad.GriddedSet;
import visad.Gridded1DSet;
import visad.IntegerNDSet;
import visad.LinearNDSet;
import visad.SampledSet;
import visad.ProductSet;
import visad.SetException;
import visad.VisADException;
import visad.RealTupleType;
import visad.CoordinateSystem;
import visad.GridCoordinateSystem;
import visad.Unit;

class HdfeosDomain
{
  private EosStruct struct;

  final static int INTEGER = 0;
  final static int HYBRID = 1;         // product of next two
  final static int FACTORED = 2;       // aligned with R^N
  final static int FACTORED_ARITH = 3; //
  final static int UNFACTORED = 4;     // non-aligned with R^N
  final static int LINEAR = 5;
  final static int SINGLE = 6;

  private int op;
  private NamedDimension[] domDims;
  private DimensionSet domDimSet = null;

  private Variable[] domVars;

  private int domainDim;
  private int manifoldDim;
  private int[] lengths = null;
  private int[] inv_lengths = null;
  private int[] num_type = null;
  private Calibration[] cal = null;
  private int n_samples;
  private float[][] samples;
  private String[] name_s = null;
  MathType mathtype = null;
  private CoordinateSystem coord_sys = null;
  private Unit[] units = null;
  private Set domainSet = null;
  private HdfeosDomain gridCoordSys = null;
  private boolean subRank;

  private int[] start = null;
  private int[] edge = null;
  private int[] stride = null;

  public HdfeosDomain( EosStruct struct,
                       DimensionSet dimSet)
         throws VisADException
  {
    this(struct, dimSet.getElements(), null, null);
  }

  public HdfeosDomain( EosStruct struct,
                       DimensionSet dimSet,
                       CoordinateSystem coord_sys )
         throws VisADException
  {
    this(struct, dimSet.getElements(), coord_sys, null);
  }

  public HdfeosDomain( EosStruct struct,
                       DimensionSet dimSet,
                       CoordinateSystem coord_sys,
                       Unit[] units )
         throws VisADException
  {
    this(struct, dimSet.getElements(), coord_sys, units);
  }

  public HdfeosDomain( EosStruct struct,
                       NamedDimension dim )
         throws VisADException
  {
    NamedDimension[] dims = {dim};
    initializeNoVars( struct, dims );
  }

  public HdfeosDomain( EosStruct struct,
                       NamedDimension[] dims,
                       CoordinateSystem coord_sys,
                       Unit[] units)
         throws VisADException
  {
    this.coord_sys = coord_sys;
    this.units = units;
    initializeNoVars( struct, dims );
  }

  public HdfeosDomain( EosStruct struct,
                       DimensionSet dimSet,
                       HdfeosDomain gridCoordSys )
         throws VisADException
  {
    this.coord_sys = getNullGridCoordinateSystem(gridCoordSys);
    this.gridCoordSys = gridCoordSys;
    initializeNoVars( struct, dimSet.getElements() );
  }

  public static GridCoordinateSystem
         getNullGridCoordinateSystem( HdfeosDomain gridCoordSys )
         throws VisADException
  {
    RealTupleType reference = (RealTupleType)gridCoordSys.getType();
    int dim = reference.getDimension();
    int[] lens = new int[dim];
    for ( int ii = 0; ii < dim; ii++ ) {
      lens[ii] = 2;
    }
    IntegerNDSet set = new IntegerNDSet(reference, lens);
    GridCoordinateSystem c_sys = new GridCoordinateSystem(set);
    return c_sys;
  }

  private void initializeNoVars( EosStruct struct,
                                 NamedDimension[] dims)
          throws VisADException
  {
    this.struct = struct;
    domDims = dims;
    domainDim = dims.length;
    lengths = new int[ domainDim ];
    inv_lengths = new int[ domainDim ];
    manifoldDim = domainDim;
    name_s = new String[ domainDim ];
    this.domDimSet = new DimensionSet(dims);
    if ( units == null ) {
      units = new Unit[domainDim];
    }

    start = new int[ manifoldDim ];
    stride = new int[ manifoldDim ];
    edge = new int[ manifoldDim ];
    n_samples = 1;
    for ( int ii = 0; ii < domainDim; ii++ )
    {
      name_s[ii] = domDims[ii].getName();
      lengths[ii] = domDims[ii].getLength();
      n_samples *= lengths[ii];
      start[ii] = 0;
      edge[ii] = lengths[ii];
      stride[ii] = 1;
    }
    for ( int kk = 0; kk < domainDim; kk++ )
    {
      inv_lengths[kk] = lengths[(domainDim-1)-kk];
    }
    op = LINEAR;
    subRank = false;

    mathtype = makeType(null);
  }

  public HdfeosDomain( EosStruct struct,
                       Variable[] vars,
                       NamedDimension[] dims )
         throws VisADException
  {
    initialize( struct, vars, dims );
  }

  public HdfeosDomain( EosStruct struct,
                       VariableSet v_set,
                       DimensionSet d_set )
         throws VisADException
  {
    domDimSet = d_set;
    initialize(struct, v_set.getElements(), d_set.getElements());
  }

  public HdfeosDomain( EosStruct struct,
                       Variable var )
         throws VisADException
  {
    Variable[] vars = new Variable[1];
    NamedDimension[] dims = new NamedDimension[1];
    vars[0] = var;
    dims[0] = var.getDim(0);

    initialize(struct, vars, dims);
  }

  private void initialize( EosStruct struct,
                           Variable[] vars,
                           NamedDimension[] dims )
          throws VisADException
  {
    this.struct = struct;
    domVars = vars;
    int n_vars = vars.length;
    this.num_type = new int[n_vars];
    this.cal = new Calibration[n_vars];
    domDims = dims;
    int n_dims = dims.length;
    boolean all_1D = false;
    boolean one_1D = false;
    boolean all_eq = true;

    domainDim = vars.length;
    manifoldDim = dims.length;

    if ( domDimSet == null ) {
      domDimSet = new DimensionSet( dims );
    }

    name_s = new String[ domainDim ];
    if ( units == null ) {
      units = new Unit[domainDim];
    }

    if (domainDim == 1)
    {
      op = SINGLE;
      subRank = false;
      start = new int[1];
      edge = new int[1];
      stride = new int[1];
      name_s[0] = domVars[0].getName();
      num_type[0] = domVars[0].getNumberType();
      cal[0] = domVars[0].getCalibration();
    }
    else
    {
      int v_rank = 1;
      int v_rank0 = domVars[0].getRank();
      for ( int ii = 0; ii < n_vars; ii++ )
      {
        v_rank = domVars[ii].getRank();
        if ( n_dims != 1 )
        {
          all_1D = false;
          if ( v_rank != v_rank0 )
          {
            all_eq = false;
          }
        }
        else
        {
          one_1D = true;
        }
        name_s[ii] = domVars[ii].getName();
        num_type[ii] = domVars[ii].getNumberType();
        cal[ii] = domVars[ii].getCalibration();
      }

      if ( all_1D )
      {
        op = FACTORED;
        subRank = false;
        start = new int[1];
        edge = new int[1];
        stride = new int[1];
      }
      else if ( all_eq )
      {
        if ( v_rank > manifoldDim ) {
          subRank = true;
        }
        else if ( v_rank == manifoldDim ) {
          subRank = false;
        }
        else {
          throw new HdfeosException("variables rank cannot be greater"+
                                    " than manifoldDim" );
        }
        op = UNFACTORED;
        start = new int[v_rank];
        stride = new int[v_rank];
        edge = new int[v_rank];
      }
      else
      {
        throw new HdfeosException("undefined domain case");
      }
    }

    lengths = new int[ manifoldDim ];
    inv_lengths = new int[ manifoldDim];
    n_samples = 1;
    for ( int ii = 0; ii < manifoldDim; ii++ )
    {
      lengths[ii] = domDims[ii].getLength();
      n_samples *= lengths[ii];
    }
    for ( int kk = 0; kk < manifoldDim; kk++ )
    {
      inv_lengths[kk] = lengths[(manifoldDim-1)-kk];
    }
    samples = new float[domainDim][n_samples];
    mathtype = makeType(null);
  }

  public DimensionSet getDimSet()
  {
    return domDimSet;
  }

  MathType makeType(CoordinateSystem coord_sys)
           throws VisADException
  {
    int inv_ii;
    RealType[] r_types = new RealType[domainDim];
    for ( int ii = 0; ii < domainDim; ii++ ) {
      inv_ii = (domainDim-1) - ii;
      r_types[ii] = RealType.getRealType(name_s[inv_ii], units[inv_ii], null);
    }
    if ( r_types.length == 1 ) {
      return r_types[0];
    }
    else {
      if ( coord_sys == null ) {
        return new RealTupleType(r_types, this.coord_sys, null);
      }
      else {
        return new RealTupleType(r_types, coord_sys, null);
      }
    }
  }

  public MathType getType()
         throws VisADException
  {
    return mathtype;
  }

  public Set getData()
         throws VisADException
  {
    if ( !subRank ) {
      return getData(null);
    }
    else {
      throw new HdfeosException("getData(int[] indexes) must be used");
    }
  }

  public Set getData( int[] indexes )
         throws VisADException
  {
    int cnt = 0;
    if ( indexes == null )
    {
      if ( subRank ) {
        throw new HdfeosException("indexes cannot be null");
      }
      else if ( domainSet != null ) {
        return domainSet;
      }
    }
    else
    {
      if ( !subRank ) {
        throw new HdfeosException("getData() must be used");
      }
      for ( int ii = 0; ii < indexes.length; ii++ ) {
        start[cnt] = indexes[ii];
        edge[cnt] = 1;
        cnt++;
      }
    }
    Set set = null;
    switch (op)
    {
      case UNFACTORED:
        for ( int kk = 0; kk < domainDim; kk++ )
        {
          for ( int ii = 0; ii < manifoldDim; ii++ ) {
            start[cnt+ii] = 0;
            edge[cnt+ii] = lengths[ii];
            stride[cnt+ii] = 1;
          }
          struct.readData(name_s[kk], start, stride, edge,
                          num_type[kk], cal[kk], samples[kk] );
        }
        set = GriddedSet.create(mathtype, samples, inv_lengths);
     //-set = new GriddedSet(mathtype, samples, inv_lengths);
        break;

      case SINGLE:
        struct.readData(name_s[0], start, stride, edge,
                        num_type[0], cal[0], samples[0]);
        set = new Gridded1DSet(mathtype, samples, lengths[0]);
        break;

      case LINEAR:
        if ( gridCoordSys == null ) {
          set = new IntegerNDSet(mathtype, lengths, null, null, null);
        }
        else {
          GriddedSet geo_domain;
          try {
            geo_domain = (GriddedSet) gridCoordSys.getData(indexes);
          }
          catch ( SetException e ) {
            System.out.println( (gridCoordSys.getType()).toString()+" "
                               +e.getMessage());
            set = new IntegerNDSet(mathtype, lengths, null, null, null);
            break;
          }
          int[] lens = geo_domain.getLengths();
          double[] firsts = new double[domainDim];
          double[] lasts = new double[domainDim];
          for ( int ii = 0; ii < domainDim; ii++ ) {
            firsts[ii] = 0;
             lasts[ii] = lens[ii];
          }
          GridCoordinateSystem c_sys = new GridCoordinateSystem(geo_domain);
          set = new LinearNDSet(mathtype, firsts, lasts, lengths, c_sys, null, null);
        }
        break;

      case FACTORED:
        SampledSet[] sets = new SampledSet[domainDim];
        for ( int kk = 0; kk < domainDim; kk++ ) {
          start[0] = 0;
          edge[0] = lengths[kk];
          stride[0] = 1;
          struct.readData(name_s[kk], start, stride, edge,
                          num_type[kk], cal[kk], samples[kk]);
          sets[kk] = new Gridded1DSet(mathtype, samples, lengths[kk]);
        }
        set = new ProductSet(mathtype, sets);
        break;
    }
    if ( !subRank ) {
      domainSet = set;
    }
    return set;
  }

  public EosStruct getStruct()
  {
     return struct;
  }
}
