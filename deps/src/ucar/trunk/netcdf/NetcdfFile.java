// $Id: NetcdfFile.java,v 1.9 2003-03-14 16:29:05 donm Exp $
/*
 * Copyright 1997-2000 Unidata Program Center/University Corporation for
 * Atmospheric Research, P.O. Box 3000, Boulder, CO 80307,
 * support@unidata.ucar.edu.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package ucar.netcdf;

import ucar.multiarray.Accessor;
import ucar.multiarray.AbstractAccessor;
import ucar.multiarray.MultiArray;
import ucar.multiarray.MultiArrayImpl;
import ucar.multiarray.IndexIterator;
import ucar.multiarray.OffsetIndexIterator;

import java.lang.reflect.Array;
import java.lang.Math;

import java.io.File;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

// use buffered ucar.netcdf.RandomAccessFile instead
// import java.io.RandomAccessFile;

import java.lang.reflect.InvocationTargetException;

/**
 * A concrete implementation of the Netcdf interface,
 * this class provides connection to NetCDF version 1 files.
 * <p>
 * Constructors for creating new files and opening existing
 * ones.
 *
 * @see Netcdf
 * @author $Author: donm $
 * @version $Revision: 1.9 $ $Date: 2003-03-14 16:29:05 $
 */
public class NetcdfFile	extends AbstractNetcdf {

 /* Begin constructors */

    /**
     * Create a new netcdf version 1 file from a Schema template.
     *
     * @param file       the file name as File object
     * @param clobber    if <code>true</code>, overwrite existing
     * @param fill       if <code>false</code>, suppress variable pre fill
     * @param template   the Schema used as construction template. May be empty,
     *                     shouldn't be null.
     *
     * @see #setFill
     * @see Netcdf
     */
    public
    NetcdfFile(File file, boolean clobber, boolean fill,
                        Schema template)
                throws IOException
    {
        super(new Schema(template), true);
        if(!clobber && file.exists())
        {
                // TODO: netcdf exception?
                throw new SecurityException(file.getName() + " exists");
        }
        // else
        this.file = file;
        this.raf = new RandomAccessFile(file, "rw");
        this.doFill = fill;

        compileBegins();
        initRecSize();
        writeV1();
        fillerup();

        url = null;
    }

    /**
     * Create a new netcdf version 1 file from a Schema template.
     *
     * @param path       the file name as a String
     * @param clobber    if <code>true</code>, overwrite existing
     * @param fill       if <code>false</code>, suppress variable pre fill
     * @param template   the Schema used as construction template. May be empty,
     *                     shouldn't be null.
     *
     * @see #setFill
     * @see Netcdf
     */
    public
    NetcdfFile(String path, boolean clobber, boolean fill,
                        Schema template)
                throws IOException
    {
        this(new File(path), clobber, fill, template);
    }

    /**
     * Open existing netcdf version 1 file.
     *
     * @param file       the file name as File object
     * @param readonly	 if <code>true</code>, open read only,
     *			else open for read and write.
     */
    public
    NetcdfFile(File file, boolean readonly) throws IOException {
        super();
        this.file = file;
        raf = new RandomAccessFile(file, readonly ? "r" : "rw");
        readV1(raf);
        initRecSize();
        this.doFill = true;

        url = null;
    }

    /**
     * Open existing netcdf version 1 file.
     *
     * @param path       the file name as a String
     * @param readonly	 if <code>true</code>, open read only,
     *			else open for read and write.
     */
    public
    NetcdfFile(String path, boolean ro)
                throws IOException
    {
        this(new File(path), ro);
    }

    /**
     * Open existing, read-only netcdf file through a URL. This may use either the
     * file: or http: protocol. If it uses the file protocol, it will be opened as a
     * read-only file using url.getFile(). If it uses the http protocol, it will be
     * read over http using HTTPRandomAccessFile. The query
     * component of the URL is ignored
     *
     * <p>Modified from ncBrowse (Donald Denbo).</p>
     *
     * @param url                    the URL of the netCDF dataset.
     * @throws FileNotFoundException if the URL specifies a file that doesn't
     *                               exist.
     * @throws IOException           if an I/O failure occurs.
     */
  public NetcdfFile(URL url) throws FileNotFoundException, IOException {
    super();
    String path = url.getFile();
    int    i = path.indexOf('?');
    if (i != -1)
	path = path.substring(0, i);
    if (url.getProtocol().equalsIgnoreCase("file"))
    {
	/*
	 * URL.getPath() isn't used in order to accomodate JDK 1.2.
	 */
        this.url = null;
        file = new File(path);
        raf = new RandomAccessFile (path, "r", 204800);
    }   else   {
	/* Defensive copy */
	this.url =
	    new URL(url.getProtocol(), url.getHost(), url.getPort(), path);
	file = null;
	//Use a pretty big buffer to reduce the number of seeks
	raf = new HTTPRandomAccessFile(this.url, 204800);
	//raf = new HTTPRandomAccessFile(this.url);
    }
    readV1(raf);
    initRecSize();
    this.doFill = true;
  }

 /* End constructors */
 /* Begin Public */

    /**
     * Close this netcdf file.
     * The inquiry interface calls will continue to be available,
     * but I/O accesses will fail after this call.
     * @see RandomAccessFile#close
     */
    public void
    close() throws IOException
    {
        raf.close();
    }

    /**
     * Flush anything written to disk.
     * @see RandomAccessFile#flush
     */
    public void
    flush() throws IOException
    {
        raf.flush();
    }

    /**
     * Useful for identifying this instance among others.
     * @return File object this was opened or created as.
     */
    public final File getFile() {
        return file;
    }

    /**
     * Useful for identifying this instance among others.
     * @return File object this was opened or created as.
     */
    public final String getName() {
        return (file != null) ? file.getPath() : url.toString();
    }

    /**
     * Sets the "fill mode" to the argument.
     * If true (the default), new storage is prefilled with the
     * appropriate fill value. Otherwise, this activity is suppressed
     * and the programmer should initialize all values.
     * @see #getFill
     * @param pleaseFill true to fill.
     */
    public synchronized void
    setFill(boolean pleaseFill)
    {
        doFill = pleaseFill;
    }

    /**
     * Get the current "fill mode".
     * @see #setFill
     * @return true iff we are prefilling new storage
     * with the appropriate fill value.
     */
    public final boolean
    getFill()
    {
        return doFill;
    }

    /**
     * If this has an unlimited Dimension,
     * return it, otherwise null.
     * Note that this specific to NetcdfFile, not
     * part of the Netcdf interface. Other implementations
     * may support multiple unlimited dimensions.
     * @deprecated
     * @return UnlimitedDimension the unlimited dimension
     */
    public final UnlimitedDimension
    unlimitedDimension()
    {
        return recDim;
    }

    /**
     * Format as CDL.
     * @param buf StringBuffer into which to write
     */
    public void
    toCdl(StringBuffer buf)
    {
        buf.append("netcdf ");
        if (file != null)
          buf.append(file.getName());
        else
          buf.append(url.toString());
        buf.append(" ");
        super.toCdl(buf);
    }

 /* End Public */
 /* Begin Constants */

    /*
     * netcdf file format version 1 "magic number"
     */
    final static int v1magic = 0x43444601;
    /*
     * tag for signed 1 byte integer
     */
    final static int NC_BYTE = 1;
    /*
     * tag for ISO/ASCII characters
     */
    final static int NC_CHAR = 2;
    /*
     * tag for signed 2 byte integer
     */
    final static int NC_SHORT = 3;
    /*
     * tag for signed 4 byte integer
     */
    final static int NC_INT = 4;
    /*
     * tag for single precision floating point number
     */
    final static int NC_FLOAT =	5;
    /*
     * tag for double precision floating point number
     */
    final static int NC_DOUBLE = 6;
    /*
     * tag for array of netcdf Dimensions
     */
    final static int NC_DIMENSION = 10;
    /*
     * tag for array of netcdf Variables
     */
    final static int NC_VARIABLE = 11;
    /*
     * tag for array of netcdf attributes
     */
    final static int NC_ATTRIBUTE = 12;

    /*
     * External representation aligns to 4 byte boundaries.
     * a.k.a. BYTES_PER_XDR_UNIT
     */
    final static int X_ALIGN = 4;

    /*
     * External size of char.
     * Netcdf v1 file format uses 8 bit ASCII
     */
    final static int X_SIZEOF_CHAR = 1;
    /*
     * External size of byte
     */
    final static int X_SIZEOF_BYTE = 1;
    /*
     * External size of short
     */
    final static int X_SIZEOF_SHORT = 2;
    /*
     * External size of int
     */
    final static int X_SIZEOF_INT = 4;
    /*
     * External size of float
     */
    final static int X_SIZEOF_FLOAT = 4;
    /*
     * External size of DOUBLE
     */
    final static int X_SIZEOF_DOUBLE = 8;

    /*
     * Reserved name of Fill attribute
     */
    static final String _FillValue = "_FillValue";

    static final byte NC_FILL_BYTE = -127;
    static final byte NC_FILL_CHAR = 0;
    static final short NC_FILL_SHORT = -32767;
    static final int NC_FILL_INT = -2147483647;
    static final float NC_FILL_FLOAT = 9.9692099683868690e+36F;
    static final double NC_FILL_DOUBLE = 9.9692099683868690e+36;

 /* End Constants */

    private int
    padsz(int xsz) {
        int rem = xsz % X_ALIGN;
        if(rem == 0)
              return 0;
        return (X_ALIGN - rem);
    }

    private int
    rndup(int xsz) {
        return (xsz + padsz(xsz));
    }

    private int
    xszofV1String(String str) {
        int xsz = X_SIZEOF_INT;
        xsz += rndup(str.length());
        return xsz;
    }

    private void
    writeV1String(String str)
                throws IOException {
        int rndup = str.length() % X_ALIGN;
        if(rndup != 0)
                rndup = X_ALIGN - rndup;
        raf.writeInt(str.length());
        raf.writeBytes(str);
        while(rndup != 0) {
                raf.writeByte(0);
                rndup--;
        }

    }

    private String
    readV1String(DataInput hgs, int size)
                throws IOException {
        int rndup = size % X_ALIGN;
        if(rndup != 0)
                rndup = X_ALIGN - rndup;
        byte [] bytes = new byte[size];
        hgs.readFully(bytes); // TODO: premature EOF detection provided?
        hgs.skipBytes(rndup);
        return new String(bytes).intern();
    }

    private String
    readV1String(DataInput hgs)
                throws IOException {
        return readV1String(hgs, hgs.readInt());
    }

    private void
    writeV1Bytes(byte [] bytes)
                throws IOException {
        int rndup = bytes.length % X_ALIGN;
        if(rndup != 0)
                rndup = X_ALIGN - rndup;
        raf.writeInt(bytes.length);
        raf.write(bytes);
        while(rndup != 0) {
                raf.writeByte(0);
                rndup--;
        }

    }

    private int
    v1TypeEncode(Class componentType) {
        if(componentType.isPrimitive()) {
                if(componentType.equals(Character.TYPE))
                        return NC_CHAR;
                if(componentType.equals(Byte.TYPE))
                        return NC_BYTE;
                if(componentType.equals(Short.TYPE))
                        return NC_SHORT;
                if(componentType.equals(Integer.TYPE))
                        return NC_INT;
                if(componentType.equals(Float.TYPE))
                        return NC_FLOAT;
                if(componentType.equals(Double.TYPE))
                        return NC_DOUBLE;
        }
        throw new IllegalArgumentException("Not a V1 type: " + componentType);
    }

    private Class
    v1TypeDecode(int v1type) {
        switch (v1type) {
        case NC_CHAR:
                return Character.TYPE;
        case NC_BYTE:
                return Byte.TYPE;
        case NC_SHORT:
                return Short.TYPE;
        case NC_INT:
                return Integer.TYPE;
        case NC_FLOAT:
                return Float.TYPE;
        case NC_DOUBLE:
                return Double.TYPE;
        }
        return null;
    }

    private int
    xszofElement(Class componentType) {
        if(componentType.equals(Short.TYPE))
                return 2;
        if(componentType.equals(Integer.TYPE))
                return 4;
        if(componentType.equals(Float.TYPE))
                return 4;
        if(componentType.equals(Double.TYPE))
                return 8;
        return 1;
    }

    /*
     * Used to calculate V1Io.vsize and this.recsize
     */
    private int
    initVsize(DimensionIterator ee, int xsz) {
        int size = 1;
        while ( ee.hasNext() ) {
                final Dimension dim = ee.next();
                if(dim instanceof UnlimitedDimension) {
                        continue;
                }
                // else
                size *= dim.getLength();
        }

        size *= xsz;
        return size; // N.B. not rounded up
   }

        private void
        writeV1(Dimension dim)
                        throws IOException {
                writeV1String(dim.getName());
                if(dim instanceof UnlimitedDimension)
                        raf.writeInt(0);
                else
                        raf.writeInt(dim.getLength());
        }

        private int
        xszof(Dimension dim) {
                int xsz = xszofV1String(dim.getName());
                xsz += X_SIZEOF_INT;
                return xsz;
        }

        private void
        writeV1(Attribute attr) throws IOException {
                writeV1String(attr.getName());
                if(attr.isString())
                {
                        raf.writeInt(NC_CHAR); // type
                        writeV1String((String)attr.getValue());
                        return;
                }
                // else
                final int v1type = v1TypeEncode(attr.getComponentType());
                raf.writeInt(v1type); // type
                if(v1type == NC_CHAR)
                {
                        writeV1String((String)attr.getValue());
                        return;
                }
                if(v1type == NC_BYTE)
                {
                        writeV1Bytes((byte [])attr.getValue());
                        return;
                }
                // else
                final int length = Array.getLength(attr.getValue());
                raf.writeInt(length); // nelems
                // TODO: invert so loop is inside switch?
                for(int ii = 0; ii < length; ii++) {
                        switch (v1type) {
                        case NC_SHORT:
                                raf.writeShort(((short[])attr.getValue())[ii]);
                                if(length % 2 != 0)
                                        raf.writeShort(0); // pad to X_ALIGN
                                break;
                        case NC_INT:
                                raf.writeInt(((int[])attr.getValue())[ii]);
                                break;
                        case NC_FLOAT:
                                raf.writeFloat(((float[])attr.getValue())[ii]);
                                break;
                        case NC_DOUBLE:
                                raf.writeDouble(((double[])attr.getValue())[ii]);
                                break;
                        }
                }
        }

        private int
        xszof(Attribute attr) {
                int xsz = xszofV1String(attr.getName());
                xsz += X_SIZEOF_INT; // tag
                if(attr.isString()) {
                        return xsz + xszofV1String((String)attr.getValue());
                }
                // else
                xsz += X_SIZEOF_INT; // nelems
                final int v1type = v1TypeEncode(attr.getComponentType());
                final int length = Array.getLength(attr.getValue());
                switch (v1type) {
                case NC_BYTE:
                case NC_CHAR:
                        xsz += rndup(length);
                        break;
                case NC_SHORT:
                        xsz += rndup(length * X_SIZEOF_SHORT);
                        break;
                case NC_INT:
                        xsz += length * X_SIZEOF_INT;
                        break;
                case NC_FLOAT:
                        xsz += length * X_SIZEOF_FLOAT;
                        break;
                case NC_DOUBLE:
                        xsz += length * X_SIZEOF_DOUBLE;
                        break;
                }
                return xsz;
        }


        private Dimension []
        readV1DimensionArray(DataInput hgs)
                        throws IOException {
                final int numrecs = hgs.readInt();
                final int tag = hgs.readInt();
                if(tag != NC_DIMENSION && tag != 0)
                        throw new IllegalArgumentException(
                                "Not a netcdf file (dimensions)");
                final int ndims = hgs.readInt();
                Dimension [] dimArray = new Dimension[ndims];
                for(int ii = 0; ii < ndims; ii++) {
                        final String name = readV1String(hgs);
                        final int length = hgs.readInt();
                        if(length == 0) {
                                if(this.recDim != null)
                                        throw new IllegalArgumentException(
                                                "Multiple UnlimitedDimensions");
                                this.recDim =
                                        new UnlimitedDimension(name, numrecs);
                                dimArray[ii] = this.recDim;
                        }
                        else
                                dimArray[ii] = new Dimension(name, length);
                }
                return dimArray;
        }

        private void
        writeV1(DimensionSet ds)
                throws IOException
        {
                writeV1numrecs();
                final int size = ds.size();
                if(size != 0)
                        raf.writeInt(NC_DIMENSION);
                else
                        raf.writeInt(0); // bit for bit backward compat.
                raf.writeInt(size);
                final DimensionIterator ee = ds.iterator();
                while(ee.hasNext()) {
                        writeV1(ee.next());
                }
        }

        private int
        xszof(DimensionSet ds) {
                int xsz = X_SIZEOF_INT; // numrecs
                xsz += X_SIZEOF_INT; // tag
                xsz += X_SIZEOF_INT; // nelems
                final DimensionIterator ee = ds.iterator();
                while(ee.hasNext()) {
                        xsz += xszof(ee.next());
                }
                return xsz;
        }

        private void
        writeV1numrecs()
                throws IOException
        {
                if(recDim == null)
                        raf.writeInt(0);
                else
                        raf.writeInt(recDim.getLength());
        }

        private void
        writeV1(AttributeSet as)
                throws IOException
        {
                final int size = as.size();
                if(size != 0)
                        raf.writeInt(NC_ATTRIBUTE);
                else
                        raf.writeInt(0); // bit for bit backward compat.
                raf.writeInt(size);
                final AttributeIterator ee = as.iterator();
                while (ee.hasNext()) {
                                writeV1(ee.next());
                }
        }

        private Object
        readV1AttrVal(DataInput hgs) throws IOException {
                final int v1type = hgs.readInt();
                final int nelems = hgs.readInt();

                switch (v1type) {
                case NC_CHAR:
                {
                        int rndup = nelems % X_ALIGN;
                        if(rndup != 0)
                                rndup = X_ALIGN - rndup;
                        char [] values = new char[nelems];
                        for(int ii = 0; ii < nelems; ii++)
                                values[ii] = (char) hgs.readUnsignedByte();
                        hgs.skipBytes(rndup);
                        return values;
                }
                case NC_BYTE:
                {
                        int rndup = nelems % X_ALIGN;
                        if(rndup != 0)
                                rndup = X_ALIGN - rndup;
                        byte [] values = new byte[nelems];
                        hgs.readFully(values); // TODO: premature EOF detection?
                        hgs.skipBytes(rndup);
                        return values;
                }
                case NC_SHORT:
                {
                        short [] values = new short[nelems];
                        for(int ii = 0; ii < nelems; ii++)
                                values[ii] = hgs.readShort();
                        if(nelems % 2 != 0)
                                hgs.skipBytes(2); // pad to X_ALIGN
                        return values;
                }
                case NC_INT:
                {
                        int [] values = new int[nelems];
                        for(int ii = 0; ii < nelems; ii++)
                                values[ii] = hgs.readInt();
                        return values;
                }
                case NC_FLOAT:
                {
                        float [] values = new float[nelems];
                        for(int ii = 0; ii < nelems; ii++)
                                values[ii] = hgs.readFloat();
                        return values;
                }
                case NC_DOUBLE:
                {
                        double [] values = new double[nelems];
                        for(int ii = 0; ii < nelems; ii++)
                                values[ii] = hgs.readDouble();
                        return values;
                }
                } // end switch
                /*NOTREACHED*/
                return null;
        }

        private Attribute []
        readV1AttributeArray(DataInput hgs)
                        throws IOException {
                final int tag = hgs.readInt();
                if(tag != NC_ATTRIBUTE && tag != 0)
                        throw new IllegalArgumentException(
                                "Not a netcdf file (attributes)");
                final int nelems = hgs.readInt();
                Attribute [] attrArray = new Attribute[nelems];
                for(int ii = 0; ii < nelems; ii++) {
                        final String name = readV1String(hgs);
                        final Object value = readV1AttrVal(hgs);
                        attrArray[ii] = new Attribute(name, value);
                }
                return attrArray;
        }

        private int
        xszof(AttributeSet as) {
                int xsz = X_SIZEOF_INT; // tag
                xsz += X_SIZEOF_INT; // nelems
                final AttributeIterator ee = as.iterator();
                while (ee.hasNext()) {
                        xsz += xszof(ee.next());
                }
                return xsz;
        }

    abstract class
    V1Io extends AbstractAccessor {

        /*
         * This form of constructor used when creating.
         */
        protected
        V1Io(ProtoVariable proto) {
                meta = proto;
                lengths = proto.getLengths();
                initFillValue(proto);
                // TODO
                this.vsize = rndup(initVsize(proto.getDimensionIterator(),
                                xszofElement(proto.getComponentType())));
                this.begin = 0;
                isUnlimited = proto.isUnlimited();
                this.dsizes = compileDsizes(proto.getLengths());
                this.xsz = xszofElement(proto.getComponentType());
        }

        abstract /* protected */ void
        readArray(long offset, Object dst, int dst_position, int nelems)
                        throws IOException;

        private final int
        iocount(int [] origin, int [] shape)
        {
                int product = 1;
                int minIndex = 0;
                if(isUnlimited)
                        minIndex = 1;
                for(int ii = shape.length -1; ii >= minIndex; ii--)
                {
                        final int si = shape[ii];
                        product *= si;
                        if(origin[ii] != 0 || si < lengths[ii] )
                                break;
                }
                return product;
        }

        public MultiArray
        copyout(int [] origin, int [] shape)
                        throws IOException {
                final int [] dimensions = (int []) shape.clone();
                final int [] products = new int[dimensions.length];
                final int product = MultiArrayImpl.numberOfElements(dimensions,           products);
                final Object storage = Array.newInstance (
                        meta.getComponentType(),
                        product);
                final int contig = iocount(origin, shape);

                // convert dimensions to limits
                final int [] limits = (int []) dimensions.clone();
                for(int ii = 0; ii < limits.length; ii++)
                        limits[ii] += origin[ii];

                final OffsetIndexIterator odo = new OffsetIndexIterator(origin, limits);


		int cnt = 0;


                for(int begin = 0; 
		    odo.notDone (); 
		    odo.advance (contig), begin += contig)
                {
                        final long offset = computeOffset (odo.value());
                        readArray(offset, storage, begin, contig);
			cnt++;
                }

		MultiArray result = new MultiArrayImpl(dimensions, products,  storage);


                return result;
        }

        /*
         * @param data Array of byte which can be
	 *     	 *     	 *     	 *     	 *     	 *     	 *     	 *	contiguously written.
         */
        abstract void
        writeArray(long offset, Object from, int begin, int nelems)
                throws IOException;

        public void
        copyin(int [] origin, MultiArray data)
                throws IOException
        {
                /*
                 * The switch on subclass here is justified
                 * to make the specialized optimization available here
                 * without adding specializations to Accessor,
                 * AbstractAccessor, and Variable.
                 */
                if(data instanceof MultiArrayImpl)
                {
                        this.copyin(origin, (MultiArrayImpl) data);
                }
                else
                {
                        // TODO checkfill
                        if(isUnlimited)
                                checkfill(origin[0] + (data.getLengths())[0]);
                        super.copyin(origin, data); // AbstractAccessor.
                }
        }

        public void
        copyin(int [] origin, MultiArrayImpl data)
                throws IOException
        {
                final int [] dimensions = data.getLengths();
                final int contig = iocount(origin, dimensions);
                // convert dimensions to limits
                for(int ii = 0; ii < dimensions.length; ii++)
                        dimensions[ii] += origin[ii];
                if(isUnlimited)
                        checkfill(dimensions[0]);
                final Object storage = data.storage;
                final OffsetIndexIterator odo = new OffsetIndexIterator(origin,
                        dimensions);
                for(int begin = 0; odo.notDone(); odo.advance(contig),
                         begin += contig)
                {
                        final long offset = computeOffset(odo.value());
                        writeArray(offset, storage, begin, contig);

                }
        }

        public Object
        toArray()
                throws IOException
        {
                return this.toArray(null, null, null);
        }

        public Object
        toArray(Object dst, int [] origin, int [] shape)
                throws IOException
        {
                final int rank = getRank();
                if(origin == null)
                        origin = new int[rank];
                else if(origin.length != rank)
                        throw new IllegalArgumentException("Rank Mismatch");

                int [] shp = null;
                if(shape == null)
                        shp = (int []) lengths.clone();
                else if(shape.length == rank)
                        shp = (int []) shape.clone();
                else
                        throw new IllegalArgumentException("Rank Mismatch");

                final int product = MultiArrayImpl.numberOfElements(shp);
                dst = MultiArrayImpl.fixDest(dst, product,
                        meta.getComponentType());
                final int contig = iocount(origin, shp);

                // convert dimensions to limits
                final int [] limits = (int []) shp.clone();
                for(int ii = 0; ii < limits.length; ii++)
                        limits[ii] += origin[ii];

                final OffsetIndexIterator odo = new OffsetIndexIterator(origin,
                        limits);
                for(int begin = 0; odo.notDone(); odo.advance(contig),
                         begin += contig)
                {
                        final long offset = computeOffset(odo.value());
                        readArray(offset, dst, begin, contig);

                }

                return dst;
        }

  /**/
        private final int []
        compileDsizes(int [] shape)
        {
                final int [] ds = new int [shape.length];
                int product = 1;
                for(int ii = shape.length - 1; ii >= 0; ii--)
                {
                        if(!(ii == 0 && isUnlimited))
                                product *= shape[ii];
                        ds[ii] = product;
                }
                return ds;
        }

        public final void
        checkfill(int newLength)
                        throws IOException {
                synchronized(recDim) {
                        int length = recDim.getLength();
                        if(newLength > length)
                        {
                                if(doFill)
                                {
                                        for(; length < newLength; length++)
                                        {
                                                fillRec(length);
                                        }
                                }
                                recDim.setLength(newLength);
                                // TODO? allow caching? (NC_NSYNC)
                                raf.seek(((long)4)); // NC_NUMRECS_OFFSET
                                // writeV1numrecs();
                                raf.writeInt(recDim.getLength());
                        }
                }

        }

        /* TODO */
        final int getRank() { return dsizes.length; }
        final boolean isScalar() { return 0 == getRank(); }

        final long
        computeOffset(int [] origin) {
                if(isScalar())
                        return begin;
                // else
                if(getRank() == 1) {
                        if(isUnlimited) {
                                return (begin + origin[0] * recsize);
                        }
                        // else
                        return (begin + origin[0] * this.xsz);
                }
                // else
                final int end = dsizes.length -1;
                int lcoord = origin[end];
                int index  = 0;
                if(isUnlimited) {
                        index++;
                }
                for(; index < end ; index++) {
                        lcoord += dsizes[index +1] * origin[index];
                }
                lcoord *= this.xsz;
                if(isUnlimited)
                        lcoord += origin[0] * recsize;
                lcoord += begin;
                return lcoord;
        }

        abstract void
        fill(DataOutput dos, int nbytes, Attribute fAttr)
                        throws IOException;

        private final void
        initFillValue(Attribute fAttr) {
                final int nbytes = 32; // tune here
                ByteArrayOutputStream bos = new ByteArrayOutputStream(nbytes);
                DataOutputStream dos = new DataOutputStream(bos);
                try {
                        this.fill(dos, nbytes, fAttr);
                        dos.flush();
                } catch (IOException ioe) {
                        // assert(cant happen);
                }
                fillbytes = bos.toByteArray();

        }

        private final void
        initFillValue(ProtoVariable proto)
        {
                initFillValue(proto.getAttribute(_FillValue));
        }

        private final void
        initFillValue(Attribute [] attrArray)
        {
                Attribute fAttr = null;
                for(int ii = 0; ii < attrArray.length; ii++)
                {
                        if(attrArray[ii].getName() == _FillValue)
                                fAttr = attrArray[ii];
                }
                initFillValue(fAttr);
        }

        void
        fillO(long offset)
                throws IOException {
                raf.seek(offset);
                int remainder = vsize;
                for(; remainder >= fillbytes.length;
                                remainder -= fillbytes.length)
                        raf.write(fillbytes);
                // handle any remainder;
                if(remainder > 0)
                        for(int ii = 0; ii < remainder; ii++)
                                raf.write(fillbytes[ii]);
        }

        void
        fill(int recno)
                throws IOException {
                long offset = begin;
                if(isUnlimited)
                        offset +=  (long)recno * recsize;
                this.fillO(offset);
        }

        private final ProtoVariable meta; // sibling within the Variable.
        private final int [] lengths; // cache of meta.getLengths()
        byte [] fillbytes;
        int vsize;
        int begin;
        final boolean isUnlimited; // TODO factor this!!
        final int [] dsizes;
        int xsz; // TODO: Is this member needed?
    }

 /* Begin IWISHWEHADTEMPLATES or a macro preprocessor */

    private final class
    V1ByteIo extends V1Io {

        V1ByteIo(ProtoVariable var) {
                super(var);
        }

        void
        readArray(long offset, Object into, int begin, int nelems)
                        throws IOException {
                final byte [] values = (byte []) into;
                raf.seek(offset);
                raf.read(values, begin, nelems);
        }

        public byte
        getByte(int [] index)
                throws IOException
        {
                raf.seek(computeOffset(index));
                return raf.readByte();
        }

        public Object
        get(int [] index)
                throws IOException
        {
                return new Byte(this.getByte(index));
        }

        void
        writeArray(long offset, Object from, int begin, int nelems)
                        throws IOException {
                byte [] values = (byte []) from;
                raf.seek(offset);
                raf.write(values, begin, nelems);
        }

        public void
        setByte(int [] index, byte value)
                throws IOException
        {
                if(isUnlimited)
                        checkfill(index[0] +1);
                raf.seek(computeOffset(index));
                raf.writeByte(value);
        }

        public void
        set(int [] index, Object value)
                        throws IOException
        {
                this.setByte(index, ((Number)value).byteValue());
        }

        final void
        fill(DataOutput dos, int nbytes, Attribute fAttr)
                        throws IOException {
                byte fv = NC_FILL_BYTE;
                if(fAttr != null)
                        fv = fAttr.getNumericValue().byteValue();
                for(int ii = 0; ii < nbytes; ii++)
                                dos.write(fv);
        }
    }

    private final class
    V1CharacterIo extends V1Io {

        V1CharacterIo(ProtoVariable var) {
                super(var);
        }

        void
        readArray(long offset, Object into, int begin, int nelems)
                        throws IOException {
                final char [] values = (char []) into;
                raf.seek(offset);
                final int end = begin + nelems;
                for(int ii = begin; ii < end; ii++) {
                        values[ii] = (char) raf.readUnsignedByte();
                }
        }

        public char
        getChar(int [] index)
                throws IOException
        {
                raf.seek(computeOffset(index));
                return (char) raf.readUnsignedByte();
        }

        public Object
        get(int [] index)
                throws IOException
        {
                return new Character(this.getChar(index));
        }

        void
        writeArray(long offset, Object from, int begin, int nelems)
                        throws IOException {
                final char [] values = (char []) from;
                raf.seek(offset);
                final int end = begin + nelems;
                for(int ii = begin; ii < end; ii++) {
                        raf.writeByte((byte) values[ii]);
                }
        }

        public void
        setChar(int [] index, char value)
                throws IOException
        {
                if(isUnlimited)
                        checkfill(index[0] +1);
                raf.seek(computeOffset(index));
                raf.writeByte((byte) value);
        }

        public void
        set(int [] index, Object value)
                        throws IOException
        {
                this.setChar(index, ((Character)value).charValue());
        }

        final void
        fill(DataOutput dos, int nbytes, Attribute fAttr)
                        throws IOException {
                byte fv = NC_FILL_CHAR;
                if(fAttr != null)
                        fv = fAttr.getLength() == 0
                            ? 0  // because Atribute strips trailing NUL
                            : fAttr.getNumericValue().byteValue();
                for(int ii = 0; ii < nbytes; ii++)
                                dos.write(fv);
        }
    }

    private final class
    V1ShortIo extends V1Io {

        V1ShortIo(ProtoVariable var) {
                super(var);
        }

        void
        readArray(long offset, Object into, int begin, int nelems)
                        throws IOException {
                final short [] values = (short []) into;
                raf.seek(offset);
                final int end = begin + nelems;
                for(int ii = begin; ii < end; ii++) {
                        values[ii] = raf.readShort();
                }
        }

        public short
        getShort(int [] index)
                throws IOException
        {
                raf.seek(computeOffset(index));
                return raf.readShort();
        }

        public Object
        get(int [] index)
                throws IOException
        {
                return new Short(this.getShort(index));
        }

        void
        writeArray(long offset, Object from, int begin, int nelems)
                        throws IOException {
                final short [] values = (short []) from;
                raf.seek(offset);
                final int end = begin + nelems;
                for(int ii = begin; ii < end; ii++) {
                        raf.writeShort(values[ii]);
                }
        }

        public void
        setShort(int [] index, short value)
                throws IOException
        {
                if(isUnlimited)
                        checkfill(index[0] +1);
                raf.seek(computeOffset(index));
                raf.writeShort(value);
        }

        public void
        set(int [] index, Object value)
                        throws IOException
        {
                this.setShort(index, ((Number)value).shortValue());
        }

        final void
        fill(DataOutput dos, int nbytes, Attribute fAttr)
                        throws IOException {
                short fv = NC_FILL_SHORT;
                if(fAttr != null)
                        fv = fAttr.getNumericValue().shortValue();
                for(int ii = 0; ii < nbytes; ii++)
                                dos.writeShort(fv);
        }
    }

    private final class
    V1IntegerIo extends V1Io {

        V1IntegerIo(ProtoVariable var) {
                super(var);
        }

        void
        readArray(long offset, Object into, int begin, int nelems)
                        throws IOException {
                final int [] values = (int []) into;
                raf.seek(offset);
                final int end = begin + nelems;
                for(int ii = begin; ii < end; ii++) {
                        values[ii] = raf.readInt();
                }
        }

        public int
        getInt(int [] index)
                throws IOException
        {
                raf.seek(computeOffset(index));
                return raf.readInt();
        }

        public Object
        get(int [] index)
                throws IOException
        {
                return new Integer(this.getInt(index));
        }

        void
        writeArray(long offset, Object from, int begin, int nelems)
                        throws IOException {
                final int [] values = (int []) from;
                raf.seek(offset);
                final int end = begin + nelems;
                for(int ii = begin; ii < end; ii++) {
                        raf.writeInt(values[ii]);
                }
        }

        public void
        setInt(int [] index, int value)
                throws IOException
        {
                if(isUnlimited)
                        checkfill(index[0] +1);
                raf.seek(computeOffset(index));
                raf.writeInt(value);
        }

        public void
        set(int [] index, Object value)
                        throws IOException
        {
                this.setInt(index, ((Number)value).intValue());
        }

        final void
        fill(DataOutput dos, int nbytes, Attribute fAttr)
                        throws IOException {
                int fv = NC_FILL_INT;
                if(fAttr != null)
                        fv = fAttr.getNumericValue().intValue();
                for(int ii = 0; ii < nbytes; ii++)
                                dos.writeInt(fv);
        }
    }

    private final class
    V1FloatIo extends V1Io {

        V1FloatIo(ProtoVariable var) {
                super(var);
        }

        void  readArray(long offset, Object into, int begin, int nelems)
	    throws IOException {
	    //	    if (begin+nelems>100)
	    float [] values = (float []) into;
	    raf.seek (offset);
	    final int end = begin + nelems;
	    for(int ii = begin; ii < end; ii++) {
		values[ii] = raf.readFloat();
	    }

        }

        public float
        getFloat(int [] index)
                throws IOException
        {
                raf.seek(computeOffset(index));
                return raf.readFloat();
        }

        public Object
        get(int [] index)
                throws IOException
        {
                return new Float(this.getFloat(index));
        }

        void
        writeArray(long offset, Object from, int begin, int nelems)
                        throws IOException {
                final float [] values = (float []) from;
                raf.seek(offset);
                final int end = begin + nelems;
                for(int ii = begin; ii < end; ii++) {
                        raf.writeFloat(values[ii]);
                }
        }

        public void
        setFloat(int [] index, float value)
                throws IOException
        {
                if(isUnlimited)
                        checkfill(index[0] +1);
                raf.seek(computeOffset(index));
                raf.writeFloat(value);
        }

        public void
        set(int [] index, Object value)
                        throws IOException
        {
                this.setFloat(index, ((Number)value).floatValue());
        }

        final void
        fill(DataOutput dos, int nbytes, Attribute fAttr)
                        throws IOException {
                float fv = NC_FILL_FLOAT;
                if(fAttr != null)
                        fv = fAttr.getNumericValue().floatValue();
                for(int ii = 0; ii < nbytes; ii++)
                                dos.writeFloat(fv);
        }
    }

    private final class
    V1DoubleIo extends V1Io {

        V1DoubleIo(ProtoVariable var) {
                super(var);
        }

        void
        readArray(long offset, Object into, int begin, int nelems)
                        throws IOException {
                final double [] values = (double []) into;
                raf.seek(offset);
                final int end = begin + nelems;
                for(int ii = begin; ii < end; ii++) {
                        values[ii] = raf.readDouble();
                }
        }

        public double
        getDouble(int [] index)
                throws IOException
        {
                raf.seek(computeOffset(index));
                return raf.readDouble();
        }

        public Object
        get(int [] index)
                throws IOException
        {
                return new Double(this.getDouble(index));
        }

        void
        writeArray(long offset, Object from, int begin, int nelems)
                        throws IOException {
                final double [] values = (double []) from;
                raf.seek(offset);
                final int end = begin + nelems;
                for(int ii = begin; ii < end; ii++) {
                        raf.writeDouble(values[ii]);
                }
        }

        public void
        setDouble(int [] index, double value)
                throws IOException
        {
                if(isUnlimited)
                        checkfill(index[0] +1);
                raf.seek(computeOffset(index));
                raf.writeDouble(value);
        }

        public void
        set(int [] index, Object value)
                        throws IOException
        {
                this.setDouble(index, ((Number)value).doubleValue());
        }

        final void
        fill(DataOutput dos, int nbytes, Attribute fAttr)
                        throws IOException {
                double fv = NC_FILL_DOUBLE;
                if(fAttr != null)
                        fv = fAttr.getNumericValue().doubleValue();
                for(int ii = 0; ii < nbytes; ii++)
                                dos.writeDouble(fv);
        }
    }

 /* End IWISHWEHADTEMPLATES or a macro preprocessor */


        private V1Io
        V1IoFactory(ProtoVariable proto)
        {
                final Class componentType = proto.getComponentType();
                V1Io io = null;

                if(componentType.equals(Character.TYPE)) {
                        io =  new V1CharacterIo(proto);
                }
                else if (componentType.equals(Byte.TYPE)) {
                        io =  new V1ByteIo(proto);
                }
                else if (componentType.equals(Short.TYPE)) {
                        io =  new V1ShortIo(proto);
                }
                else if (componentType.equals(Integer.TYPE)) {
                        io =  new V1IntegerIo(proto);
                }
                else if (componentType.equals(Float.TYPE)) {
                        io =  new V1FloatIo(proto);
                }
                else if (componentType.equals(Double.TYPE)) {
                        io =  new V1DoubleIo(proto);
                }

                return io;
        }

        protected Accessor
        ioFactory(ProtoVariable proto)
                { return V1IoFactory(proto); }

        private void
        writeV1(Variable var)
                throws IOException
        {
                writeV1String(var.getName());
                raf.writeInt(var.getRank());

                DimensionIterator ee = var.getDimensionIterator();
                while(ee.hasNext())
                {
                        raf.writeInt(indexOf(ee.next()));
                }

                writeV1(var.getAttributes());
                raf.writeInt(v1TypeEncode(var.getComponentType()));
                final V1Io io = (V1Io) var.io;
                raf.writeInt(io.vsize);
                raf.writeInt(io.begin);
        }

        private int
        xszof(Variable var) {
                int xsz = xszofV1String(var.getName());
                xsz += X_SIZEOF_INT; // dimArray.length
                xsz += var.getRank() * X_SIZEOF_INT; // dim indexes
                xsz += xszof(var.getAttributes());
                xsz += X_SIZEOF_INT; // tag
                xsz += X_SIZEOF_INT; // vsize
                xsz += X_SIZEOF_INT; // begin
                return xsz;
        }

        private void
        readV1VarArray(DataInput hgs, Dimension [] allDims)
                        throws IOException {
                int tag = hgs.readInt();
                if(tag != NC_VARIABLE && tag != 0)
                        throw new IllegalArgumentException(
                                "Not a netcdf file (variables)");
                int nelems = hgs.readInt();
                for(int ii = 0; ii < nelems; ii++) {
                        final String name = readV1String(hgs);
                        final int ndims = hgs.readInt();
                        final Dimension [] dimArray = new Dimension[ndims];
                        for(int jj = 0; jj < ndims; jj++)
                                dimArray[jj] = allDims[hgs.readInt()];
                        final Attribute [] attrArray =
                                readV1AttributeArray(hgs);
                        final Class type =  v1TypeDecode(hgs.readInt());
                        final ProtoVariable proto = new ProtoVariable(
                                name, type, dimArray, attrArray
                                );

                        final V1Io io = V1IoFactory(proto);
                        io.vsize =  hgs.readInt();
                        io.begin =  hgs.readInt();

                        try {
                                add(proto, io);
                        }
                        catch (InstantiationException ie)
                        {
                                // Can't happen: Variable is concrete
                                throw new Error();
                        }
                        catch (IllegalAccessException iae)
                        {
                                // Can't happen: Variable is accessable
                                throw new Error();
                        }
                        catch (InvocationTargetException ite)
                        {
                                // all the possible target exceptions are
                                // RuntimeException
                                throw (RuntimeException)
                                        ite.getTargetException();
                        }
                }
        }

        private void
        writeV1(int size, VariableIterator ee)
                throws IOException
        {
                if(size != 0)
                        raf.writeInt(NC_VARIABLE);
                else
                        raf.writeInt(0); // bit for bit backward compat.
                raf.writeInt(size);
                while( ee.hasNext()) {
                        writeV1(ee.next());
                }
        }

        private int
        xszof(VariableIterator ee) {
                int xsz = X_SIZEOF_INT; // tag
                xsz += X_SIZEOF_INT; // nelems
                while(ee.hasNext()) {
                        xsz += xszof(ee.next());
                }
                return xsz;
        }

    private void
    writeV1()
                throws IOException
    {
        raf.writeInt(v1magic);
        writeV1(getDimensions());
        writeV1(getAttributes());
        writeV1(size(), iterator());
    }

    private int
    xszof() {
        int xsz = X_SIZEOF_INT; // magic number
        xsz += xszof(getDimensions());
        xsz += xszof(getAttributes());
        xsz += xszof(iterator());
        return xsz;
    }

    private void
    readV1(DataInput hgs)
                throws IOException {

        final int magic = hgs.readInt();
        if(magic != v1magic)
                throw new IllegalArgumentException("Not a netcdf file");

        final Dimension [] dimArray = readV1DimensionArray(hgs);
        for(int ii = 0; ii < dimArray.length; ii++)
                putDimension(dimArray[ii]);

        {
                final Attribute [] gAttrArray =
                        readV1AttributeArray(hgs);
                for(int ii = 0; ii < gAttrArray.length; ii++)
                        putAttribute(gAttrArray[ii]);
        }

        readV1VarArray(hgs, dimArray);
    }

    /*
     * In the C interface this is called NC_begins();
     */
    private void
    compileBegins() {

        int index = xszof();
        /* loop thru vars, first pass is for the 'non-record' vars */
        {
        final VariableIterator ee = iterator();
        while(ee.hasNext())
        {
                final Variable var = ee.next();
                if(var.isUnlimited())
                        continue;
                // else
                final V1Io io = (V1Io) var.io;
                io.begin = index;
                index += io.vsize;
        }
        }

        {
        /* loop thru vars, 2nd pass is for the 'record' vars */
        final VariableIterator ee = iterator();
        while(ee.hasNext())
        {
                final Variable var = ee.next();
                if(!var.isUnlimited())
                        continue;
                if(recDim == null)
                {
                        final Dimension dim0 =
                                 var.getDimensionIterator().next();
                        if(!(dim0 instanceof UnlimitedDimension))
                                throw new IllegalArgumentException(
                                        "Unlimited Dim not leftmost"
                                );
                        recDim = (UnlimitedDimension)dim0;
                }
                final V1Io io = (V1Io) var.io;
                io.begin = index;
                index += io.vsize;
        }
        }
    }

    private void
    initRecSize() {
        recsize = 0;
        /* loop thru vars, 2nd pass is for the 'record' vars */
        final VariableIterator ee = iterator();
        while(ee.hasNext())
        {
                final Variable var = ee.next();
                if(!var.isUnlimited())
                        continue;
                final V1Io io = (V1Io) var.io;
                if(recsize == 0 && !ee.hasNext())
                {
                        // special case exactly one record variable
                        // pack value
                        recsize = initVsize(var.getDimensionIterator(),
                                io.xsz);
                        break;
                }
                // else
                recsize += io.vsize;
        }
    }

    // can't be private and still visible in inner class V1Var?
    void
    fillRec(int recno) throws IOException {
        // synchronized in caller
        // "only call when doFill set" checked in caller
        final VariableIterator ee = iterator();
        while(ee.hasNext())
        {
                final Variable var = ee.next();
                if(!var.isUnlimited())
                        continue;
                // else
                // var.fill(recno);
                final V1Io io = (V1Io) var.io;
                final long offset = (long)io.begin + (long)recno * recsize;
                io.fillO(offset);
        }
    }

    private void
    fillerup()
                 throws IOException {
        if(!this.doFill)
                return;
        final VariableIterator ee = iterator();
        while(ee.hasNext())
        {
                final Variable var = ee.next();
                if(var.isUnlimited())
                        continue;
                // else
                final V1Io io = (V1Io) var.io;
                io.fillO((long)io.begin);
        }
        if(this.recDim != null) {
            final int nrecs = recDim.getLength();
            for(int recno = 0; recno < nrecs; recno++)
                fillRec(recno);
        }
    }

    /**
     * Ensures that the close method of this file is called when
     * there are no more
     * references to it.
     * @exception Throwable The lack of covariance for exception specifications
     * dictates the specificed type;
     * it can actually only be <code>IOException</code> thrown
     * by <code>RandomAccessFile.close</code>.
     * @see NetcdfFile#close
     */
    protected void
    finalize() throws Throwable
    {
        super.finalize();
        close();
    }

    private URL url;	// not "final" to accomodate JDK 1.2
    private File file;	// not "final" to accomodate JDK 1.2
    private RandomAccessFile raf; // unidata.netcdf version, not java.io
    private UnlimitedDimension recDim;
    private int recsize;
    private boolean doFill; // set to false to suppress data prefill.

}

/* Change History:
   $Log: not supported by cvs2svn $
   Revision 1.15  2003/03/04 22:26:32  jeffmc
   Add a new ctor that takes buffer size and cleanup some old debug in netcdffile

   Revision 1.14  2003/03/04 22:23:22  jeffmc
   Create the httprandomaccess file with a largish buffer size

   Revision 1.13  2003/01/21 21:24:05  jeffmc
   Add a getStorage method that returns the raw multiarray object

   Revision 1.12  2002/07/15 21:39:17  steve
   Changed use of _FillValue attribute.  If zero-length, then use byte-value 0.

   Revision 1.11  2002/05/24 00:06:06  caron
   add flush()

   Revision 1.10  2001/09/14 21:29:28  caron
   minor doc improvements

   Revision 1.9  2001/09/10 20:37:12  steve
   Improved constructor NetcdfFile(URL):
       Replaced URL.getPath() with URL.getFile() to accomodate JDK 1.2.
       Made protocol comparison case-insensitive.
       Added defensive copying of modifiable URL argument.
       Added FileNotFoundException.
       Added ignoring of query component of URL.
       Enhanced JavaDoc.

   Revision 1.8  2001/08/28 16:59:59  steve
   Added support for "file" protocol to constructor NetcdfFile(URL).

   Revision 1.7  2001/05/17 15:15:09  steve
   Modified to accomodate JDK 1.2.

   Revision 1.6  2001/05/01 15:06:02  caron
   add netcdf HTTP access

 */
