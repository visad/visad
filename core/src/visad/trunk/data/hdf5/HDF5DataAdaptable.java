/****************************************************************************
 * NCSA HDF                                                                 *
 * National Comptational Science Alliance                                   *
 * University of Illinois at Urbana-Champaign                               *
 * 605 E. Springfield, Champaign IL 61820                                   *
 *                                                                          *
 * For conditions of distribution and use, see the accompanying             *
 * hdf/COPYING file.                                                        *
 *                                                                          *
 ****************************************************************************/


package visad.data.hdf5;

import java.rmi.RemoteException;
import visad.VisADException;
import visad.MathType;
import visad.DataImpl;

/**
 * The interface for HDF5 data objects which can be adapted by the
 * VisAD data objects
 */
public interface HDF5DataAdaptable
{
	MathType getMathType() throws VisADException;

	DataImpl getAdaptedData() throws VisADException, RemoteException;

	DataImpl getAdaptedData(int[] indexes) throws VisADException, RemoteException;
}
