//
// RemoteSwitchImpl.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2000 Bill Hibbard, Curtis Rueden, Tom
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

/* Cluster Design Ideas

Everything is via RMI - no 'local' Impl

a Data object is partitioned if any Field in it has a
partitioned domain

a non-partitioned Data object is local on the client

a partitioned Data object is a RemoteClientDataImpl on the
cient connected to RemodeNodeDataImpl's on the nodes

these may be either RemoteClientTupleImpl or
RemoteClientFieldImpl (and Node classes)

Client and nodes each have one RemoteSwitchImpl instance,
identical on each except for

how to deal with Field domains that overlap but are not
identical to clsuter partition domain (e.g., cluster
domain is (lat, lon) and Field domain is (lat, lon, alt),
(lat), or (row, col, lev) with reference (lat, lon, alt))




use java.net.URLClassLoader (?) to load classes onto nodes
from client for data search criteria, see java.lang.ClassLoader

cluster design should include a native VisAD Data Model on
binary files, via serialization, for an implementation of
FileFlatField on nodes
*/

/* VisAD Data Model on various file formats

Data instance method for write
Data static method for read
a parameter to these methods is a file-format-specific
implementation of a FileIO interface, that is used for
low level I/O (should deal with missing data in
file-format-specific way)

other interfaces for constructing appropriate file-format-
specific structures for Tuple, Field, FlatField, Set, Real,
Text, RealTuple, CoordinateSystem, Unit, ErrorEstimate

get review from Steve on this

*/

package visad.cluster;

import visad.*;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;

/**
   RemoteSwitchImpl is the interface for cluster client and node
   swicthes for communicating across the cluster.<P>
*/
public class RemoteSwitchImpl extends UnicastRemoteObject
       implements RemoteSwitch {

  /** Set that defines partition of Data across cluster;
      values in partitionSet's domain RealTupleType are
      partitioned according to:
         jvmTable[partitionSet.valueToIndex()] */
  private Set partitionSet = null;

  /** domain dimension of partitionSet */
  private int dimension = -1;

  /** lookup table for RemoteSwitche objects on nodes, last
      entry is on client (for non-distributed data) */
  private RemoteSwitch[] jvmTable = null;

  private RemoteSwitch me = null;

  private RemoteSwitchImpl()
          throws RemoteException {
    me = this;
  }

  /** return RemoteSwitch for JVM where data resides;
      may be RemoteSwitch for cleint for non-partitioned data;
      may be null for partitioned data outside partitoning */
  public RemoteSwitch getSwitch(RealTuple domain)
         throws RemoteException, VisADException {
    if (domain == null || partitionSet == null || jvmTable == null) {
      throw new ClusterException("null domain or setup not done");
    }
    if (dimension != domain.getDimension()) {
      // return client (last entry) for non-partitoned data
      return jvmTable[jvmTable.length - 1];
    }
    double[][] vals = new double[dimension][1];
    for (int i=0; i<dimension; i++) {
      vals[i][0] = ((Real) domain.getComponent(i)).getValue();
    }

// replace with 'WLH 4 Julyy 2000' throw Exception in CoordinateSystem
    RealTupleType out = ((SetType) partitionSet.getType()).getDomain();
    CoordinateSystem coord_out = partitionSet.getCoordinateSystem();
    RealTupleType in = (RealTupleType) domain.getType();
    CoordinateSystem coord_in = domain.getCoordinateSystem();
    if (!out.equals(in)) {
      RealTupleType ref_out = out;
      if (coord_out != null) ref_out = coord_out.getReference();
      RealTupleType ref_in = in;
      if (coord_in != null) ref_in = coord_in.getReference();
      if (!ref_out.equals(ref_in)) {
        // return client (last entry) for non-partitoned data
        return jvmTable[jvmTable.length - 1];
      }
    }

    vals = CoordinateSystem.transformCoordinates(
                     ((SetType) partitionSet.getType()).getDomain(),
                     partitionSet.getCoordinateSystem(),
                     partitionSet.getSetUnits(), null,
                     (RealTupleType) domain.getType(),
                     domain.getCoordinateSystem(),
                     domain.getTupleUnits(), null, vals);
    int[] indices = partitionSet.doubleToIndex(vals);
    return (indices[0] < 0) ? null : jvmTable[indices[0]];
  }

  public void setupSwitch(Set ps, RemoteSwitch[] table)
         throws RemoteException, VisADException {
    if (ps == null || table == null) {
      throw new ClusterException("ps and table must be non-null");
    }
    if ((ps.getLength() + 1) != table.length) {
      throw new ClusterException("table.length (" + table.length +") must " +
                                 " = ps.length + 1 (" + (ps.getLength() + 1) +
                                 ")");
    }
    partitionSet = ps;
    dimension = ps.getDimension();
    jvmTable = table;
  }

  public boolean switchEquals(RemoteSwitch sw)
         throws RemoteException, VisADException {
    return (sw == me); // seems to work - but does it really?
  }

  public static void main(String[] args)
         throws RemoteException, VisADException {
    RemoteSwitch sw = new RemoteSwitchImpl();
    RemoteSwitch sw2 = new RemoteSwitchImpl();
    System.out.println(sw.equals(sw));
    System.out.println(sw.equals(sw2));
    System.exit(0);
  }

}

