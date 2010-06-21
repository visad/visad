// $Id: ProtoVariableIterator.java,v 1.4 2002-05-29 18:31:36 steve Exp $
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

/**
 * Type specific Iterator.
 * Use the Iterator methods to fetch elements sequentially.
 * @see java.util.Iterator
 * @author $Author: steve $
 * @version $Revision: 1.4 $ $Date: 2002-05-29 18:31:36 $
 */
public interface ProtoVariableIterator {
    /**
     * Returns <code>true</code> if there are more elements.
     */
    boolean hasNext();
    /**
     * Returns the next element. Calls to this
     * method will step through successive elements.
     * @exception java.util.NoSuchElementException If no more elements exist.
     */
    ProtoVariable next();
}
