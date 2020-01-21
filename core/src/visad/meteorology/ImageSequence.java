//
//  ImageSequence.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2020 Bill Hibbard, Curtis Rueden, Tom
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

package visad.meteorology;

import visad.*;
import java.rmi.RemoteException;

/**
 * Interface for representing a time sequence of single-banded images.
 */
public interface ImageSequence extends Field
{
    /**
     * Return the list of times associated with this sequence.
     * @return  array of image start times.
     */
    DateTime[] getImageTimes()
        throws VisADException;

    /**
     * Return the number of images in the sequence.
     * @return number of images
     */
    int getImageCount()
        throws VisADException;

    /**
     * Get the image at the specified time
     * @param dt  image time
     * @return single banded image at that time.  
     * @throws VisADException  no image at that time in the set.
     * @throws RemoteException can't get remote image
     */
    SingleBandedImage getImage(DateTime dt)
        throws VisADException, RemoteException;

    /**
     * Return the image at the index'th position in the sequence.
     * @param  index  index in the sequence
     * @return single banded image at that index
     * @throws VisADException  no image at that index in the set.
     * @throws RemoteException can't get remote image
     */
    SingleBandedImage getImage(int index)
        throws VisADException, RemoteException;
}
