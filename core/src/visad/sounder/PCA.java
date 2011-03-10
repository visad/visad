//
// PCA.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2011 Bill Hibbard, Curtis Rueden, Tom
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

package visad.sounder;

import java.lang.reflect.InvocationTargetException;
import visad.*;
import visad.matrix.*;
import java.rmi.RemoteException;

public class PCA 
{
  JamaMatrix eigenvectors;
  JamaMatrix mean_vector;

  public PCA(JamaMatrix eigenvectors, JamaMatrix mean_vector)
  {
    this.eigenvectors = eigenvectors;
    this.mean_vector = mean_vector;
  }

  public Spectrum compressReconstruct( Spectrum spectrum )
         throws VisADException, RemoteException,IllegalAccessException, InstantiationException, InvocationTargetException
  {
    Spectrum new_spectrum = (Spectrum) spectrum.clone();
    JamaMatrix tmp = uncompress(compress(spectrum));
    new_spectrum.setSamples(tmp.getValues());
    return new_spectrum;
  } 

  public JamaMatrix compress( Spectrum spectrum ) 
         throws VisADException, RemoteException, IllegalAccessException, InstantiationException, InvocationTargetException
  {
    JamaMatrix data_vector = new JamaMatrix(spectrum.getValues());
    JamaMatrix tmp_vector = data_vector.minus(mean_vector);
    JamaMatrix trans_data_vector = eigenvectors.times(tmp_vector.transpose());
    return trans_data_vector;
  }

  public JamaMatrix uncompress( JamaMatrix trans_data_vector )
         throws VisADException, IllegalAccessException, InstantiationException, InvocationTargetException
  {
     JamaMatrix r_data_vector =
       (eigenvectors.transpose()).times(trans_data_vector);
     r_data_vector = r_data_vector.plusEquals(mean_vector.transpose());
     return r_data_vector;
  }

  public static JamaMatrix makeCovarianceMatrix( double[][] data_vectors )
         throws VisADException, IllegalAccessException, InstantiationException, InvocationTargetException
  {
    int dim = data_vectors[0].length;
    int n_vectors = data_vectors.length;
    double[] mean_vector = new double[dim];

    for ( int jj = 0; jj < dim; jj++ ) {
      double sum = 0;
      for ( int kk = 0; kk < n_vectors; kk++ ) {
         sum += data_vectors[kk][jj];
      }
      mean_vector[jj] = sum/n_vectors;
    }

    double[][] cv = new double[dim][dim];

    for ( int jj = 0; jj < dim; jj++ ) {
      for ( int ii = jj; ii < dim; ii++ ) {
        double sum = 0;
        for ( int kk = 0; kk < n_vectors; kk++ ) {
          sum += (data_vectors[kk][jj] - mean_vector[jj])*
                 (data_vectors[kk][ii] - mean_vector[ii]);
        }
        cv[jj][ii] = sum/n_vectors;
        cv[ii][jj] = cv[jj][ii];
      }
    }
    return new JamaMatrix(cv);
  }
}
