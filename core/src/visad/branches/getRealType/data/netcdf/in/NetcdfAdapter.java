/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: NetcdfAdapter.java,v 1.24.2.1 2001-09-17 19:24:46 steve Exp $
 */

package visad.data.netcdf.in;

import java.net.MalformedURLException;
import java.net.URL;
import java.io.IOException;
import java.rmi.RemoteException;
import ucar.netcdf.Netcdf;
import ucar.netcdf.NetcdfFile;
import visad.*;
import visad.data.BadFormException;
import visad.data.netcdf.*;


/**
 * The NetcdfAdapter class adapts a netCDF dataset to a VisAD API.  It is
 * useful for importing a netCDF dataset.
 *
 * @author Steven R. Emmerson
 */
public class
NetcdfAdapter
{
    /**
     * The name of the import-strategy property.
     */
    public static final String  IMPORT_STRATEGY_PROPERTY =
        "visad.data.netcdf.in.Strategy";

    /**
     * The view of the netCDF datset.
     */
    private View        view;

    /**
     * The top-level VisAD data object corresponding to the netCDF datset.
     */
    private DataImpl    data;


    /**
     * Constructs from a netCDF dataset.
     *
     * @param netcdf            The netCDF dataset to be adapted.
     * @param quantityDB        A quantity database to be used to map netCDF
     *                          variables to VisAD {@link Quantity}s.
     * @throws VisADException   Problem in core VisAD.  Probably some VisAD
     *                          object couldn't be created.
     * @throws RemoteException  Remote data access failure.
     * @throws IOException      Data access I/O failure.
     * @throws BadFormException Non-conforming netCDF dataset.
     */
    public
    NetcdfAdapter(Netcdf netcdf, QuantityDB quantityDB)
        throws VisADException, RemoteException, IOException, BadFormException
    {
        this(View.getInstance(netcdf, quantityDB));
    }


    /**
     * Constructs from a view of a netCDF dataset.
     *
     * @param view              The view of the netCDF dataset to be adapted.
     */
    public
    NetcdfAdapter(View view)
    {
        this.view = view;
    }


    /**
     * Gets the VisAD data object corresponding to the netCDF dataset.  This
     * is a potentially expensive method in either time or space.</p>
     *
     * <p>This method uses the Java property
     * <code>IMPORT_STRATEGY_PROPERTY</code> to determine the strategy with
     * which to import the netCDF dataset.  If the property is not set, then
     * the default is to use the <code>getData()</code> method of inner class
     * <code>Strategy</code>; otherwise, the value of the property is used as a
     * class name to instantiate the strategy for importing the netCDF dataset.
     * The strategy used to import a netCDF dataset can be set programatically 
     * by code like the following:
     * <blockquote><code><pre>
     * String strategyClassName = ...;
     * System.setProperty(
     *     NetcdfAdapter.IMPORT_STRATEGY_PROPERTY, strategyClassName);
     * visad.Data data = new NetcdfAdapter(...).getData();</pre>
     * </code></blockquote>
     * The import strategy can also be set by the user of an application by
     * means of the property "visad.data.netcdf.in.Strategy":
     * <blockquote><code><pre>
     * java -Dvisad.data.netcdf.in.Strategy=<em>SomeClassName</em> ...</pre>
     * </code></blockquote>
     *
     * @return                  The top-level, VisAD data object in the netCDF
     *                          dataset.
     * @throws VisADException   Problem in core VisAD.  Probably some VisAD
     *                          object couldn't be created.
     * @throws IOException      Data access I/O failure.
     * @throws BadFormException netCDF dataset doesn't conform to conventions
     *                          implicit in the View that was passed to the
     *                          constructor.
     * @throws OutOfMemoryError Couldn't read netCDF dataset into memory.
     * @see #IMPORT_STRATEGY_PROPERTY
     * @see Strategy#getData
     */
    public DataImpl
    getData()
        throws IOException, VisADException, RemoteException, BadFormException,
            OutOfMemoryError
    {
        if (data == null)
        {
            String      strategyName =
                System.getProperty(IMPORT_STRATEGY_PROPERTY);
            Strategy    strategy;

            try
            {
                strategy =
                    strategyName == null
                        ? Strategy.instance()
                        : (Strategy)Class.forName(strategyName).getMethod(
                            "instance", new Class[0])
                            .invoke(null, new Object[0]);
            }
            catch (NoSuchMethodException e)
            {
                throw new VisADException(
                    getClass().getName() + ".getData(): " +
                    "Import strategy \"" + strategyName + "\" doesn't have an "
                    + "\"instance()\" method");
            }
            catch (ClassNotFoundException e)
            {
                throw new VisADException(
                    getClass().getName() + ".getData(): " +
                    "Import strategy \"" + strategyName + "\" not found");
            }
            catch (IllegalAccessException e)
            {
                throw new VisADException(
                    getClass().getName() + ".getData(): " +
                    "Permission to access import strategy \"" + strategyName +
                    "\" denied");
            }
            catch (java.lang.reflect.InvocationTargetException e)
            {
                throw new VisADException(
                    getClass().getName() + ".getData(): Import strategy's \"" +
                    strategyName + "\" \"instance()\" method threw exception: "
                    + e.getMessage());
            }

            data = strategy.getData(this);
        }

        return data;
    }


    /**
     * Returns a proxy for the VisAD data object corresponding to the netCDF 
     * dataset.  Because of the way import strategies are used, this just
     * invokes the <em>getData()</em> method.
     *
     * @return                  A proxy for the top-level, VisAD data object in
     *                          the netCDF dataset.
     * @throws VisADException   Problem in core VisAD.  Probably some VisAD
     *                          object couldn't be created.
     * @throws IOException      Data access I/O failure.
     * @throws BadFormException netCDF dataset doesn't conform to conventions
     *                          implicit in constructing View.
     * @throws OutOfMemoryError Couldn't read netCDF dataset into memory.
     * @see Strategy#getData
     */
    public DataImpl
    getProxy()
        throws IOException, VisADException, RemoteException, BadFormException,
            OutOfMemoryError
    {
        return getData();
    }


    /**
     * Returns the VisAD data object corresponding to the netCDF dataset.  This
     * is a potentially expensive method in either time or space.  This method
     * is designed to be used by a <em>Strategy</em>.
     *
     * @param view              The view of the netCDF dataset.
     * @param merger            The object that merges the data objects in the
     *                          netCDF dataset.
     * @param dataFactory       The factory that creates VisAD data objects from
     *                          virtual data objects.
     * @return                  The VisAD data object corresponding to the
     *                          netCDF dataset.
     * @throws VisADException   Problem in core VisAD.  Probably some VisAD
     *                          object couldn't be created.
     * @throws IOException      Data access I/O failure.
     * @throws BadFormException netCDF dataset doesn't conform to conventions
     *                          implicit in constructing View.
     * @throws OutOfMemoryError Couldn't read netCDF dataset into memory.
     * @see Strategy
     */
    protected static DataImpl
    importData(View view, Merger merger, DataFactory dataFactory)
        throws IOException, VisADException, RemoteException, BadFormException,
            OutOfMemoryError
    {
        VirtualTuple    topTuple = new VirtualTuple();

        for (VirtualDataIterator iter = view.getVirtualDataIterator();
            iter.hasNext(); )
        {
            merger.merge(topTuple, iter.next());
        }

        topTuple.setDataFactory(dataFactory);

        return topTuple.getData();
    }


    /**
     * Gets the view of the netCDF dataset.
     *
     * @return                  The view of the netCDF dataset.
     */
    protected View
    getView()
    {
        return view;
    }


    /**
     * Tests this class.
     *
     * @param args              File pathnames.
     * @throws Exception        Something went wrong.
     */
    public static void
    main(String[] args)
        throws Exception
    {
        String[]        pathnames;

        if (args.length == 0)
            pathnames = new String[] {"test.nc"};
        else
            pathnames = args;

        for (int i = 0; i < pathnames.length; ++i)
        {
            NetcdfFile  file;
            try
            {
                URL     url = new URL(pathnames[i]);
                file = new NetcdfFile(url);
            }
            catch (MalformedURLException e)
            {
                file = new NetcdfFile(pathnames[i], /*readonly=*/true);
            }
            NetcdfAdapter       adapter =
                new NetcdfAdapter(file, QuantityDBManager.instance());
            DataImpl            data = adapter.getData();

            System.out.println("data.getClass().getName() = " +
                data.getClass().getName());

            System.out.println("data.getType().prettyString():\n" +
                data.getType().prettyString());
            // System.out.println("Domain set:\n" +
                // ((FieldImpl)data).getDomainSet());
            // System.out.println("Data:\n" + data);
        }
    }


    /**
     * Provides support for implementing strategies for importing netCDF
     * datasets.  The <code>getData()</code> method of this class implements the
     * default strategy.  This method may be overriden in order to implement a
     * different strategy.
     *
     * @author Steven R. Emmerson
     */
    public static class Strategy
    {
        /**
         * The singleton instance of this class.
         */
        private static Strategy instance;

        static
        {
            instance = new Strategy();
        }


        /**
         * Returns an instance of this class.
         *
         * @return                      An instance of this class.
         */
        public static Strategy instance()
        {
            return instance;
        }


        /**
         * Constructs from nothing.  Protected to ensure use of 
         * <code>instance()</code> method.
         *
         * @see #instance()
         */
        protected Strategy()
        {}


        /**
         * Returns a VisAD data object corresponding to the netCDF dataset.
         * </p>
         *
         * <p>The following tactics are used, in order, to import the netCDF
         * dataset.  The first one to succeed determines the details of the
         * returned VisAD data object.
         * <ol>
         * <li>Attempt to import the entire dataset into memory;</li>
         * <li>Attempt to import the entire dataset into memory -- using
         * visad.data.FileFlatField-s wherever possible;</li>
         * <li>Attempt to import the entire dataset into memory -- using
         * visad.data.FileFlatField-s wherever possible and maximizing
         * their number.</li>
         * </ol>
         * The first two tactics above will yield VisAD data objects with
         * identical MathType-s.  The last tactic above can yield a VisAD
         * data object with a different MathType than the first two because
         * multiple FlatField-s with the same domain will <em>not</em> be
         * consolidated.</p>
         *
         * <p>This method may be overridden in order to implement a different
         * netCDF-dataset import-strategy.</p>
         *
         * @param adapter               The netCDF-to-VisAD adapter.
         * @return                      The top-level, VisAD data object in the
         *                              netCDF dataset.
         * @throws VisADException       Problem in core VisAD.  Probably some
         *                              VisAD object couldn't be created.
         * @throws IOException          Data access I/O failure.
         * @throws BadFormException     netCDF dataset doesn't conform to
         *                              conventions implicit in constructing
         *                              View.
         * @throws OutOfMemoryError     Couldn't import netCDF dataset into 
         *                              memory.
         */
        public DataImpl
        getData(NetcdfAdapter adapter)
            throws IOException, VisADException, RemoteException,
                BadFormException, OutOfMemoryError
        {
            DataImpl    data;
            View        view = adapter.getView();
            Merger      merger = Merger.instance();             // default
            DataFactory dataFactory = DataFactory.instance();   // default

            try
            {
                data = adapter.importData(view, merger, dataFactory);
            }
            catch (OutOfMemoryError e1)
            {
                System.err.println(
                    getClass().getName() + ".getData(): " +
                    "Couldn't import netCDF dataset into memory; " + 
                    "Attempting to use FileFlatField-s");
                try
                {
                    dataFactory = FileDataFactory.instance();

                    data = adapter.importData(view, merger, dataFactory);
                }
                catch (OutOfMemoryError e2)
                {
                    System.err.println(
                        getClass().getName() + ".getData(): " +
                        "Couldn't import netCDF dataset into memory; " + 
                        "Attempting to maximize the number of FileFlatField-s");
                    try
                    {
                        merger = FlatMerger.instance();

                        data = adapter.importData(view, merger, dataFactory);
                    }
                    catch (OutOfMemoryError e3)
                    {
                        System.err.println(
                            getClass().getName() + ".getData(): " +
                            "Couldn't import netCDF dataset into memory; " +
                            "Giving up");
                        throw e3;
                    }
                }
            }

            return data;
        }
    }
}
