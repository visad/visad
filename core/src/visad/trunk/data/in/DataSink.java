package visad.data.in;

import java.rmi.RemoteException;
import visad.*;

interface DataSink 
{
    void receive(Real real)
	throws VisADException, RemoteException;

    void receive(Text text)
	throws VisADException, RemoteException;

    void receive(Scalar scalar)
	throws VisADException, RemoteException;

    void receive(Set set)
	throws VisADException, RemoteException;

    void receive(Field field)
	throws VisADException, RemoteException;

    void receive(Tuple tuple)
	throws VisADException, RemoteException;

    void receive(DataImpl data)
	throws VisADException, RemoteException;

    void flush()
	throws VisADException, RemoteException;
}
