/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2006 Bill Hibbard, Curtis Rueden, Tom
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
import visad.util.DataUtility;

/**
 * Implementation of an ImageSequence.  The images in this Field are
 * sorted by time.
 */
public class ImageSequenceImpl extends FieldImpl
    implements ImageSequence
{
    //SingleBandedImage[] images;

    /**
     * Create an image sequence with the specified FunctionType.
     *
     * @param  type   new type for the sequence.  The FunctionType of all
     *                the images must be the same as the range of type
     *                and the domain must be RealType.Time.
     * @param  images array of images to be in the sequence
     *
     * @throws VisADException  not the correct MathType or images have different
     *                         MathTypes.
     * @throws RemoteException couldn't create the remote object
     */
    public ImageSequenceImpl(FunctionType type, SingleBandedImage[] images)
        throws VisADException, RemoteException
    {
        super(ensureFunctionType(type), makeDomainSet(images));
        //this.images = images;
        FunctionType rangeType = (FunctionType) type.getRange();
        for (int i = 0; i < images.length; i++)
        {
            RealTuple timeTuple = 
                new RealTuple(new Real[] {images[i].getStartTime()});
            FunctionType imageRange = (FunctionType) images[i].getType();
            SingleBandedImage image = 
                (imageRange.equals(rangeType))
                    ? images[i]
                    : (SingleBandedImage) images[i].changeMathType(rangeType);
            setSample(timeTuple, image, false);
        }
    }

    /**
     * Create an image sequence from an array of images
     *
     * @param  images   array of images to be in the sequence. The FunctionType 
     *                  of all the images must be the same.
     *
     * @throws VisADException  images have different FunctionTypes.
     * @throws RemoteException couldn't create the remote object
     */
    public ImageSequenceImpl(SingleBandedImage[] images)
        throws VisADException, RemoteException
    {
        this( new FunctionType(RealType.Time, 
                              (FunctionType) images[0].getType()), images);
    }

    private static SampledSet makeDomainSet(SingleBandedImage[] images)
        throws VisADException, RemoteException
    {
        if (images == null) 
            throw new VisADException("images can't be null");
        DateTime[] startTimes = new DateTime[images.length];
        for (int i = 0; i < images.length; i++)
        {
            startTimes[i] = images[i].getStartTime();
        }
        
        return 
            (startTimes.length > 1)
               ? (SampledSet) DateTime.makeTimeSet(startTimes)
               : (SampledSet) 
                   new SingletonSet(new RealTuple(new Real[] {startTimes[0]}));
    }

    /**
     * Return the list of times associated with this sequence.
     * @return  array of image start times.
     */
    public DateTime[] getImageTimes()
        throws VisADException
    {
        DateTime[] times = null;
        if (getDomainSet().getLength() > 1)
           times = DateTime.timeSetToArray( 
               (Gridded1DDoubleSet) getDomainSet());
        else
           times = new DateTime[] {new DateTime( 
               ((SingletonSet) getDomainSet()).getDoubles()[0][0])};
        return times == null ? new DateTime[0] : times;
    }

    /**
     * Return the number of images in the sequence.
     * @return number of images
     */
    public int getImageCount()
        throws VisADException
    {
        return ((SampledSet) getDomainSet()).getLength();
    }

    /**
     * Get the image at the specified time
     * @param dt  image time
     * @return single banded image at that time.  
     * @throws  VisADException  no image in the sequence at the requested time
     */
    public SingleBandedImage getImage(DateTime dt)
        throws VisADException, RemoteException
    {
        return (SingleBandedImage) evaluate(dt);
    }

    /**
     * Return the image at the index'th position in the sequence.
     * @param  index in the sequence
     * @return single banded image at that index
     * @throws  VisADException  no image in the sequence at the requested index
     */
    public SingleBandedImage getImage(int index)
        throws VisADException, RemoteException
    {
        return (SingleBandedImage) getSample(index);
    }

    private static FunctionType ensureFunctionType(FunctionType type)
        throws VisADException
    {
        if (type.getDomain().equals(
             DataUtility.ensureRealTupleType(RealType.Time)) &&
             type.getRange() instanceof FunctionType &&
             ((RealTupleType) ((FunctionType) 
                 type.getRange()).getFlatRange()).getDimension() == 1)
            return type;
        else
           throw new VisADException(
               "Not a valid ImageSequence type: " + type);
    }

}
