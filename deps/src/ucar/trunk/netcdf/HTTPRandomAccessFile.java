// $Id: HTTPRandomAccessFile.java,v 1.4 2002-05-29 18:31:34 steve Exp $
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

/*
 * HTTPRandomAccessFile.java.
 * @author John Caron, based on work by Donald Denbo
 */

package ucar.netcdf;

import HTTPClient.*;

import java.io.FileNotFoundException;
import java.io.FileDescriptor;
import java.io.IOException;
import java.net.URL;

/**
 * HTTPRandomAccessFile.java.
 * @author John Caron, based on work by Donald Denbo
 */

public class HTTPRandomAccessFile extends RandomAccessFile {

  private long total_length = 0;
  private HTTPConnection conn;
  private String path;
  private NVPair[] form = null;
  private NVPair[] header = new NVPair[2];

  public HTTPRandomAccessFile(URL url) throws IOException {
    this( url, defaultBufferSize);
  }

  public HTTPRandomAccessFile(URL url, int bufferSize) throws IOException {
    super( bufferSize);
    file = null;

    path = url.getFile();	// not "getPath()" to accomodate JDK 1.2
    conn = new HTTPConnection(url);
    try {
      HTTPResponse test = conn.Head(path);
      if(test.getStatusCode() == 404)
        throw new FileNotFoundException(test.getReasonLine());
      if(test.getStatusCode() >= 300) {
        throw new IOException(test.getReasonLine());
      }
      total_length = test.getHeaderAsInt("Content-Length");
    } catch (ModuleException me) {
      me.printStackTrace();
      throw new IOException();
    }
    header[0] = new NVPair("User-Agent", "HTTPnetCDF;");
  }

  protected int read_(long pos, byte[] buff, int off, int len) throws IOException {
    long end = pos + len - 1;
    if (end >= total_length)
      end = total_length - 1;

    byte[] data = null;
    header[1] = new NVPair("Range", "bytes="+pos+"-"+end);
    //System.out.print(" want = "+pos+"-"+end+": ");

    try {
      HTTPResponse res = conn.Get(path, form, header);
      if(res.getStatusCode() >= 300) {
        System.out.println(new String(res.getData()));
        throw new IOException(res.getReasonLine());
      }
      data = res.getData();
      //System.out.println(res.getHeader("Content-Range"));
    } catch (ModuleException me) {
      me.printStackTrace();
      throw new IOException(me.getMessage());
    }

    // copy to output buffer
    int reslen = Math.min( len, data.length);
    System.arraycopy( data, 0, buff, off, reslen );

    return reslen;
  }

  public long length( ) throws IOException {
    long fileLength = total_length;
    if( fileLength < dataEnd )
      return dataEnd;
    else
      return fileLength;
  }


  /**
   * override the rest of the RandomAccessFile public methods
   */
  public void close() {
  }

  public FileDescriptor getFD() {
    return null;
  }

  /**
   * implement HTTP access.
   */

}

