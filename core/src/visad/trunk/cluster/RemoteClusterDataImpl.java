//
// RemoteClusterDataImpl.java
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

change to test CVS

Everything is via RMI - no 'local' Impl

a Data object is partitioned if any Field in it has a
partitioned domain

interfaces:
  Thing
    Data
      RemoteData (extends Remote, Data, RemoteThing)
        RemoteClusterData
          RemoteClientData
            RemoteClientTuple (extends RemoteTupleIface)
            RemoteClientFunction (extends RemoteFunction)
              RemoteClientField (extends RemoteField)
            RemoteClientPartitionedField (extends RemoteField)
          RemoteNodeData
            RemoteNodeTuple (extends RemoteTupleIface)
            RemoteNodeField (extends RemoteField)
            RemoteNodePartitionedField (extends RemoteField)

classes:
  UnicastRemoteObject
    RemoteThingImpl
      RemoteDataImpl
        RemoteClusterDataImpl
          RemoteClientDataImpl
            RemoteClientTupleImpl
            RemoteClientFieldImpl
            RemoteClientPartitionedFunctionImpl
              RemoteClientPartitionedFieldImpl
          RemoteNodeDataImpl
            RemoteNodeTupleImpl
            RemoteNodeFieldImpl
            RemoteNodePartitionedFieldImpl


RemoteClientPartitionedFieldImpl.getDomainSet() return UnionSet
of getDomainSet() returns from each node

add TupleIface extends Data (Tuple implements TupleIface)
and RemoteTupleIface extends TupleIface

a non-partitioned Data object is local on the client
  that is, a DataImpl

a partitioned Data object is a RemoteClientDataImpl on the
cient connected to RemodeNodeDataImpl's on the nodes

ClusterAgent, Serializable class sent from client to each node
gets a Thread on arrival at node, return value from send of
ClusterAgent is RemoteAgentContact (and Impl)
values from ClusterAgent back declared Serializable

see page 60 of Java Enterprise in a Nutshell
no easy way to load RMI classes - security issues


partitioned data on client has similar data trees on
client and nodes
  leaf node on client is:
    Real, Text, or RealTuple
    Field with split domain
    Set with split domain (?)
    Set with non-split domain
  non-leaf node on client is:
    Tuple
    Field with non-split domain
  leaf tree-nodes on cluster-nodes are usual data leaves

every object in data tree on client connects to objects
in data trees on nodes




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
   RemoteClusterDataImpl is the super class for cluster
   client and node Data.<P>
*/
public class RemoteClusterDataImpl extends RemoteDataImpl
       implements RemoteClusterData {

  /** Set that defines partition of Data across cluster;
      values in partitionSet's domain RealTupleType are
      partitioned according to:
         jvmTable[partitionSet.valueToIndex()] */
  private Set partitionSet = null;

  /** domain dimension of partitionSet */
  private int dimension = -1;

  /** lookup table for RemoteClusterData objects on nodes, last
      entry is on client (for non-distributed data) */
  private RemoteClusterData[] jvmTable = null;

  /** used for testing equality */
  private RemoteClusterData me = null;

  public RemoteClusterDataImpl()
          throws RemoteException {
    super(null); // RemoteDataImpl.AdaptedData =
                 // RemoteThingImpl.AdaptedThing = null
    // but adapt a ThingImpl and a RemoteThingImpl
    // used for over-riding RemoteThingImpl methods
    adaptedThingImpl = new ThingImpl();
    adaptedRemoteThingImpl = new RemoteThingImpl(adaptedThingImpl);
    me = this;
  }

  /** return RemoteClusterData for JVM where data resides;
      may be RemoteClusterData for cleint for non-partitioned data;
      may be null for partitioned data outside partitoning */
  public RemoteClusterData getClusterData(RealTuple domain)
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

    RealTupleType out = ((SetType) partitionSet.getType()).getDomain();
    CoordinateSystem coord_out = partitionSet.getCoordinateSystem();
    RealTupleType in = (RealTupleType) domain.getType();
    CoordinateSystem coord_in = domain.getCoordinateSystem();
    if (!CoordinateSystem.canConvert(out, coord_out, in, coord_in)) {
      // return client (last entry) for non-partitoned data
      return jvmTable[jvmTable.length - 1];
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

  public void setupClusterData(Set ps, RemoteClusterData[] table)
         throws RemoteException, VisADException {
    if (ps == null || table == null) {
      throw new ClusterException("ps and table must be non-null");
    }
    if ((ps.getLength() + 1) > table.length) {
      throw new ClusterException("table.length (" + table.length +") must " +
                                 " >= ps.length + 1 (" + (ps.getLength() + 1) +
                                 ")");
    }
    partitionSet = ps;
    dimension = ps.getDimension();
    jvmTable = table;
  }

  public boolean clusterDataEquals(RemoteClusterData cd)
         throws RemoteException, VisADException {
    return (cd == me); // seems to work - but does it really?
  }



  /** parent logic, looosely copied from DataImpl */
  private RemoteClusterDataImpl parent = null;
  void setParent(RemoteClusterDataImpl p) {
    parent = p;
  }
  public void notifyReferences()
         throws VisADException, RemoteException {
    adaptedThingImpl.notifyReferences();
    // recursively propogate data change to parent
    if (parent != null) parent.notifyReferences();
  }

  /** over-ride methods of RemoteThingImpl, but note these
      are for notifyReferences(), which currently does nothing
      for RemoteThingImpl */
  private ThingImpl adaptedThingImpl = null;
  private RemoteThingImpl adaptedRemoteThingImpl = null;
  public void addReference(ThingReference r) throws VisADException {
    adaptedRemoteThingImpl.addReference(r);
  }
  public void removeReference(ThingReference r) throws VisADException {
    adaptedRemoteThingImpl.removeReference(r);
  }

/* MUST OVER-RIDE
methods that acccess AdaptedThing and AdaptedData

from RemoteThingImpl:
  public void addReference(ThingReference r) throws VisADException;
  public void removeReference(ThingReference r) throws VisADException;

from RemoteDataImpl:
  public DataImpl local() throws VisADException, RemoteException;
  public MathType getType() throws VisADException, RemoteException;
  public boolean isMissing() throws VisADException, RemoteException;
  public Data binary(Data data, int op, int sampling_mode, int error_mode)
              throws VisADException, RemoteException;
  public Data binary(Data data, int op, MathType new_type,
                     int sampling_mode, int error_mode )
              throws VisADException, RemoteException;
  public Data unary(int op, int sampling_mode, int error_mode)
              throws VisADException, RemoteException;
  public Data unary(int op, MathType new_type,
                    int sampling_mode, int error_mode)
              throws VisADException, RemoteException;
  public double[][] computeRanges(RealType[] reals)
         throws VisADException, RemoteException;
  public DataShadow computeRanges(ShadowType type, int n)
         throws VisADException, RemoteException;
  public DataShadow computeRanges(ShadowType type, DataShadow shadow)
         throws VisADException, RemoteException;
  public Data adjustSamplingError(Data error, int error_mode)
         throws VisADException, RemoteException;
  public String longString() throws VisADException, RemoteException;
  public String longString(String pre)
         throws VisADException, RemoteException;

END MUST OVER-RIDE */

  public static void main(String[] args)
         throws RemoteException, VisADException {
    RemoteClusterData cd = new RemoteClusterDataImpl();
    RemoteClusterData cd2 = new RemoteClusterDataImpl();
    System.out.println(cd.equals(cd));
    System.out.println(cd.equals(cd2));
    System.exit(0);
  }

}

