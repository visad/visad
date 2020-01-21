//
// ShadowImageByRefFunctionTypeJ3D.java
//

/*
 * VisAD system for interactive analysis and visualization of numerical
 * data.  Copyright (C) 1996 - 2020 Bill Hibbard, Curtis Rueden, Tom
 * Rink, Dave Glowacki, Steve Emmerson, Tom Whittaker, Don Murray, and
 * Tommy Jasmin.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the Free
 * Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
 * MA 02111-1307, USA
 */



package visad.data;


import visad.*;

import visad.data.ArrayWrapper;
import visad.data.gif.GIFForm;
import visad.data.mcidas.AreaAdapter;
import visad.data.mcidas.BaseMapAdapter;

import visad.java3d.*;

import visad.util.Util;

import java.awt.color.*;

import java.awt.event.*;
import java.awt.image.*;

import java.io.*;

import java.net.URL;

import java.rmi.*;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Vector;

import javax.media.j3d.*;

import javax.swing.*;


/**
 * Class CachedBufferedByteImage _more_
 *
 *
 * @author IDV Development Team
 */
public class CachedBufferedByteImage extends BufferedImage {

    /** _more_ */
    public static int cnt = 0;

    /** _more_          */
    private ArrayWrapper bytes;

    /** _more_ */
    private String cacheFile;

    /** _more_ */
    private int myWidth;

    /** _more_ */
    private int myHeight;

    /** _more_ */
    private int dbSize;

    /** _more_ */
    private int dbOffset;

    /** _more_ */
    private Object MUTEX = new Object();


    /**
     * _more_
     *
     * @param width _more_
     * @param height _more_
     * @param type _more_
     */
    public CachedBufferedByteImage(int width, int height, int type) {
        super(1, 1, type);
        BufferedImage  fullImage = new BufferedImage(width, height, type);

        WritableRaster raster    = fullImage.getRaster();
        DataBuffer     db        = raster.getDataBuffer();
        byte[]         byteData  = ((DataBufferByte) db).getData();
        dbSize   = db.getSize();
        dbOffset = db.getOffset();
        //      System.err.println("bytes:" + byteData.length);
        this.myWidth  = width;
        this.myHeight = height;
	bytes = new ArrayWrapper(byteData);
    }



    /**
     * _more_
     *
     * @param newByteData _more_
     */
    public void bytesChanged(byte[] newByteData) {
        bytes.updateData(newByteData);
    }

    public void finalize() throws Throwable {
        super.finalize();
	bytes = null;
	//        System.err.println("image finalized");
	//        DataCacheManager.getCacheManager().removeFromCache(cacheId);
    }



    public boolean inMemory() {
	return  bytes.inMemory();
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public byte[] getBytesFromCache() {
        return bytes.getByteArray1D();
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public int getHeight() {
        return myHeight;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public int getWidth() {
        return myWidth;
    }

    /**
     * _more_
     *
     * @param obs _more_
     *
     * @return _more_
     */
    public int getHeight(ImageObserver obs) {
        return myWidth;
    }

    /**
     * _more_
     *
     * @param obs _more_
     *
     * @return _more_
     */
    public int getWidth(ImageObserver obs) {
        return myHeight;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public WritableRaster getRaster() {
        DataBuffer db = new DataBufferByte(getBytesFromCache(), dbSize, dbOffset);
        WritableRaster newRaster =
            java.awt.image.Raster.createWritableRaster(getSampleModel(), db,
                null);
        return newRaster;
        //            return super.getRaster();
    }
}

