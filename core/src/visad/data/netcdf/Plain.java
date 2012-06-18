/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: Plain.java,v 1.28 2002-10-21 20:07:44 donm Exp $
 */

package visad.data.netcdf;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import ucar.netcdf.Netcdf;
import ucar.netcdf.NetcdfFile;
import ucar.netcdf.Schema;
import ucar.netcdf.Variable;
import ucar.netcdf.VariableIterator;
import visad.data.BadFormException;
import visad.data.FormNode;
import visad.data.netcdf.in.*;
import visad.data.netcdf.out.VisADAdapter;
import visad.Data;
import visad.DataImpl;
import visad.UnimplementedException;
import visad.VisADException;


/**
 * A moderately stupid implementation of a netCDF data form for the
 * storage of persistent data objects on local disk.
 */
public class
Plain
    extends NetCDF
{
    /**
     * The quantity database to use for mapping netCDF variables to
     * VisAD Quantity-s.
     */
    private final QuantityDB    quantityDB;

    /**
     * The flag for transforming netCDF char variables to Text
     */
    private final boolean    charToText;


    /**
     * Constructs a default, netCDF data form.
     *
     * @throws VisADException   Couldn't create necessary VisAD object
     */
    public
    Plain()
        throws VisADException
    {
        this(QuantityDBManager.instance(), false);
    }

    /**
     * Constructs a netCDF data form that converts
     *
     * @param charToText                The char to Text flag
     */
    public
    Plain(boolean charToText)
    {
        this(QuantityDBManager.instance(), charToText);
    }

    /**
     * Constructs a netCDF data form that uses the given quantity database.
     *
     * @param db                        The quantity database.
     */
    public
    Plain(QuantityDB db)
    {
        this(db, false);
    }

    /**
     * Constructs a netCDF data form that uses the given quantity database,
     * and the flag for converting char to Text.
     *
     * @param db                        The quantity database.
     * @param charToText                The char to Text flag
     */
    public
    Plain(QuantityDB db, boolean charToText)
    {
        super("Plain");
        quantityDB = db;
        this.charToText = charToText;
    }


    /**
     * Save a VisAD data object in this form.
     *
     * @param path                      The pathname of the netCDF file to
     *                                  be created.
     * @param data                      The data to be saved.
     * @param replace                   Whether to replace an existing file.
     * @exception BadFormException      netCDF can't handle data object
     * @exception VisADException        Couldn't create necessary VisAD object
     * @exception IOException           I/O error.  File might already exist.
     * @exception RemoteException       Remote execution error
     * @exception UnimplementedException
     *                                  Not yet!
     */
    public synchronized void
    save(String path, Data data, boolean replace)
        throws BadFormException, IOException, RemoteException, VisADException,
            UnimplementedException
    {
        VisADAdapter    adapter = new VisADAdapter(data);
        Schema          schema = new Schema(adapter);
        NetcdfFile      file = new NetcdfFile(path, replace, /*fill=*/false,
                                        schema);

        try
        {
            VariableIterator    iter = file.iterator();

            while (iter.hasNext())
            {
                Variable        outVar = iter.next();
                Variable        inVar = adapter.get(outVar.getName());
                int             rank = outVar.getRank();
                int[]           origin = new int[rank];

                for (int i = 0; i < rank; ++i)
                    origin[i] = 0;

                outVar.copyin(origin, inVar);
            }
        }
        finally
        {
            file.close();
        }
    }


    /**
     * Add data to an existing data object.
     *
     * @param id        Pathname of the existing netCDF file.
     * @param data      Data to be saved.
     * @param replace   Whether or not to replace duplicate, existing data.
     * @exception BadFormException
     *                  netCDF can't handle data object.
     */
    public synchronized void
    add(String id, Data data, boolean replace)
        throws BadFormException
    {
    }


    /**
     * Returns a VisAD data object corresponding to a netCDF dataset.
     *
     * @param spec              Specification of the existing netCDF dataset.
     * @return                  A VisAD data object corresponding to the netCDF 
     *                          dataset.
     * @throws BadFormException if the netCDF dataset cannot be adapted to a
     *                          VisAD data object.
     * @throws VisADException   if a problem occurs in core VisAD.  Probably a
     *                          VisAD object couldn't be created.
     * @throws IOException      if an I/O failure occurs.
     * @see NetcdfAdapter#getData()
     */
    public synchronized DataImpl
    open(String spec)
        throws BadFormException, IOException, VisADException
    {
        return
            new NetcdfAdapter(
                new NetcdfFile(spec, /*readonly=*/true),
                quantityDB,
                charToText
            ).getData();
    }


    /**
     * Returns a VisAD data object corresponding to a netCDF dataset
     * and imported according to a given strategy.  Among the
     * pre-defined import strategies are {@link Strategy#DEFAULT},
     * {@link Strategy#MERGED_FILE_FLAT_FIELDS}, {@link
     * Strategy#UNMERGED_FILE_FLAT_FIELDS}, and {@link Strategy#IN_MEMORY}.
     *
     * @param spec              Specification of the existing netCDF dataset.
     * @param strategy          The data-import strategy.
     * @return                  A VisAD data object corresponding to the netCDF 
     *                          dataset.
     * @throws BadFormException if the netCDF dataset cannot be adapted to a
     *                          VisAD data object.
     * @throws VisADException   if a problem occurs in core VisAD.  Probably a
     *                          VisAD object couldn't be created.
     * @throws IOException      if an I/O failure occurs.
     * @see NetcdfAdapter#getData(Strategy)
     */
    public synchronized DataImpl
    open(String spec, Strategy strategy)
        throws BadFormException, IOException, VisADException
    {
        return
            new NetcdfAdapter(
                new NetcdfFile(spec, /*readonly=*/true),
                quantityDB,
                charToText
            ).getData(strategy);
    }


    /**
     * Open an existing netCDF file and return a proxy for a VisAD data object.
     *
     * @param path      Pathname of the existing netCDF file.
     * @return          A VisAD object corresponding to the netCDF dataset.
     * @exception BadFormException
     *                  The netCDF variable cannot be adapted to a VisAD API.
     * @exception VisADException
     *                  Problem in core VisAD.  Probably some VisAD object
     *                  couldn't be created.
     * @exception IOException
     *                  Data access I/O failure.
     */
    public synchronized DataImpl
    openProxy(String path)
        throws BadFormException, IOException, VisADException
    {
        NetcdfFile      file = new NetcdfFile(path, /*readonly=*/true);

        return new NetcdfAdapter(file, quantityDB, charToText).getProxy();
    }


    /**
     * Returns a VisAD data object corresponding to a URL.  If the query
     * component of the URL is <code>null</code>, then the returned object will
     * contain all the variables in the netCDF dataset.  If the query component
     * is non-<code>null</code>, then it must comprise a comma-separated list of
     * netCDF variable names; the returned VisAD data object will contain only
     * those variables in the netCDF dataset that are also named in the list
     * (i.e. the intersection is returned).  Consequently, if the list is empty,
     * then <code>null</code> is returned.  For example, this form:
     * <code> open(new URL("file://myfile.nc?var_one,var_two")); </code>
     * will return a VisAD data object consisting only of the
     * netCDF variables <code>var_one</code> and <code>var_two</code>
     * assuming they are in the file.
     *
     * @param url             The URL of the netCDF dataset.
     * @return                A VisAD object corresponding to the netCDF datset
     *                        or <code>null</code>.
     * @throws FileNotFoundException
     *                        if the URL specifies a file that doesn't exist.
     * @throws IOException    if an I/O failure occurs.
     * @throws VisADException if a necessary VisAD object couldn't be created.
     */
    public synchronized DataImpl
    open (URL url)
        throws FileNotFoundException, IOException, VisADException
    {
        /*
         * URL.getQuery() isn't used to accomodate JDK 1.2.
         */
        String query;
        {
            String file = url.getFile();
            int    i = file.indexOf('?');
            if (i == -1)
            {
                query = null;
            }
            else
            {
                query =
                    i == file.length() - 1
                        ? ""
                        : file.substring(i+1);
            }
        }
        Set    names = new TreeSet();
        if (query != null)
        {
            for (StringTokenizer st = new StringTokenizer(query, ",");
                st.hasMoreTokens(); )
            {
                names.add(st.nextToken());
            }
        }
        Netcdf netcdf = new NetcdfFile(url);
        return
            new NetcdfAdapter(
                query == null
                    ? netcdf
                    : new VariableFilter(netcdf, names),
                quantityDB, charToText).getData();
    }


    /**
     * Return the data forms that are compatible with a data object.
     *
     * @param data      The VisAD data object to be examined.
     * @return          <code>this</code> if <code>data</code> is compatible;
     *                  otherwise, <code>null</code>.
     * @exception VisADException        Problem with core VisAD.
     * @exception IOException           Problem with local data access.
     * @exception RemoteException       Problem with remote data access.
     */
    public synchronized FormNode
    getForms(Data data)
        throws VisADException, RemoteException, IOException
    {
        FormNode        form;

        try
        {
            VisADAdapter        adapter = new VisADAdapter(data);
            form = this;
        }
        catch (BadFormException e)
        {
            form = null;
        }

        return form;
    }


    /**
     * Test this class.
     *
     * @param args              Runtime arguments.  Ignored.
     * @exception Exception     Something went wrong.
     */
    public static void main(String[] args)
        throws Exception
    {
        String  inPath;
        String  outPath = "plain.nc";

        if (args.length == 0)
            inPath = "test.nc";
        else
            inPath = args[0];

        Plain   plain = new Plain(args.length > 1);

        System.out.println("Opening netCDF dataset \"" + inPath + "\"");

        Data    data;
        try
        {
            URL url = new URL(inPath);
            data = plain.open(url);
        }
        catch (MalformedURLException e)
        {
            data = plain.open(inPath);
        }

        if (data == null)
        {
            System.out.println("No data");
        }
        else
        {
            // System.out.println("Data:\n" + data);
            System.out.println("data.getType().toString():\n" +
                data.getType());
            System.out.println("Writing netCDF dataset \"" + outPath + "\"");
            plain.save(outPath, data, /*replace=*/true);
        }
    }
}
