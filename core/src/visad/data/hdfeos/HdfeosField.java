//
// HdfeosField.java
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

import java.rmi.*;
import visad.*;

public class HdfeosField extends HdfeosData
{
  HdfeosDomain domain;
  HdfeosData range;
  FunctionType mathtype;
  EosStruct struct;

  HdfeosField( HdfeosDomain domain, HdfeosData range )
    throws VisADException, RemoteException
  {
    this.domain = domain;
    this.range = range;
    this.struct = domain.getStruct();

    mathtype = makeType();
  }

  public MathType getType()
         throws VisADException
  {
    return mathtype;
  }

  public DataImpl getData()
         throws VisADException, RemoteException
  {
    Set domain = (Set) (this.domain).getData();
    int length = domain.getLength();
    int[] indexes;

    FieldImpl new_field = new FieldImpl(mathtype, domain);
    for ( int ii = 0; ii < length; ii++ ) {
      indexes = new int[1];
      indexes[0] = ii;
      new_field.setSample(ii, range.getData(indexes));
    }
    return new_field;
  }

  public DataImpl getData( int[] indexes )
         throws VisADException, RemoteException
  {
    Set domain = (Set) this.domain.getData(indexes);
    int length = domain.getLength();
    int n_indexes = indexes.length;
    int[] new_indexes = new int[n_indexes + 1];
    System.arraycopy(indexes, 0, new_indexes, 0, n_indexes);

    FieldImpl new_field = new FieldImpl(mathtype, domain);
    for ( int ii = 0; ii < length; ii++ ) {
      new_indexes[n_indexes] = ii;
      new_field.setSample(ii, range.getData(new_indexes));
    }
    return new_field;
  }

  public DataImpl getAdaptedData()
         throws VisADException, RemoteException
  {
    Set domain = (Set) this.domain.getData();
    int length = domain.getLength();
    int[] indexes;

    FieldImpl new_field = new FieldImpl(mathtype, domain);
    for ( int ii = 0; ii < length; ii++ ) {
      indexes = new int[1];
      indexes[0] = ii;
      new_field.setSample(ii, range.getAdaptedData(indexes), false);
    }
    return new_field;
  }

  public DataImpl getAdaptedData( int[] indexes )
         throws VisADException, RemoteException
  {
    Set domain = (Set) this.domain.getData(indexes);
    int length = domain.getLength();
    int n_indexes = indexes.length;
    int[] new_indexes = new int[n_indexes + 1];
    System.arraycopy(indexes, 0, new_indexes, 0, n_indexes);

    FieldImpl new_field = new FieldImpl(mathtype, domain);
    for ( int ii = 0; ii < length; ii++ ) {
      new_indexes[n_indexes] = ii;
      new_field.setSample(ii, range.getAdaptedData(new_indexes), false);
    }
    return new_field;
  }

  private FunctionType makeType()
          throws VisADException, RemoteException
  {
    MathType domain_type = this.domain.getType();
    MathType range_type = this.range.getType();
    FunctionType f_type = new FunctionType(domain_type, range_type);
    return f_type;
  }
}
