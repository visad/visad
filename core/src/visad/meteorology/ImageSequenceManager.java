//
//  ImageSequenceManager.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2018 Bill Hibbard, Curtis Rueden, Tom
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
import java.util.TreeMap;
import java.util.List;
import java.util.Collection;
import java.rmi.RemoteException;

/**
 * A class to create and manage image sequences.
 *
 * @author  Don Murray, Unidata
 */
public class ImageSequenceManager extends Object
{
    private ImageSequence sequence = null;
    private TreeMap imageMap;

    /**
     * Create an empty image sequence.
     *
     * @throws VisADException   Couldn't create an empty sequence.
     * @throws RemoteException  Couldn't create remote object.
     */
    public ImageSequenceManager()
        throws VisADException, RemoteException
    {
        this(new SingleBandedImage[0]);
    }

    /**
     * Create an image sequence from the array of images.
     *
     * @param  images  array of images for the sequence.
     * @throws VisADException   Couldn't create the necessary VisAD object
     * @throws RemoteException  Couldn't create remote object.
     */
    public ImageSequenceManager(SingleBandedImage[] images)
        throws VisADException, RemoteException
    {
        imageMap = new TreeMap();
        for (int i = 0; i < images.length; i++)
            imageMap.put(images[i].getStartTime(), images[i]);
        makeNewSequence();
    }

    /**
     * Create an image sequence from an array of images.
     *
     * @param  images  array of images for the sequence.
     * @return  an ImageSequence
     * @throws VisADException   Couldn't create the sequence.
     * @throws RemoteException  Couldn't create remote object.
     */
    public static ImageSequence createImageSequence(SingleBandedImage[] images)
        throws VisADException, RemoteException
    {
        return new ImageSequenceImpl(images);
    }

    /**
     * Add an image to the the sequence this object is managing.
     *
     * @param  image  image to add
     * @return  sequence containing the new image.
     * @throws VisADException   Couldn't create the sequence.
     * @throws RemoteException  Couldn't create remote object.
     */
    public ImageSequence addImageToSequence(SingleBandedImage image)
        throws VisADException, RemoteException
    {
        return addImagesToSequence(new SingleBandedImage[] {image});
    }

    /**
     * Add an array of images to the the sequence this object is managing.
     *
     * @param  images  images to add
     * @return  sequence containing the new images.
     * @throws VisADException   Couldn't create the sequence.
     * @throws RemoteException  Couldn't create remote object.
     */
    public ImageSequence addImagesToSequence(SingleBandedImage[] images)
        throws VisADException, RemoteException
    {
        for (int i = 0; i < images.length; i++)
            imageMap.put(images[i].getStartTime(), images[i]);
        makeNewSequence();
        return sequence;
    }


    public ImageSequence addImagesToSequence(List<SingleBandedImage> images)
        throws VisADException, RemoteException
    {
        for (int i = 0; i < images.size(); i++)
            imageMap.put(images.get(i).getStartTime(), images.get(i));
        makeNewSequence();
        return sequence;
    }

    /**
     * Remove an image from the sequence.
     *
     * @param  time   time of image to remove
     * @throws VisADException   Couldn't create the sequence.
     */
    public ImageSequence removeImageAtTime(DateTime time)
        throws VisADException
    {
        if (time == null)
            throw new VisADException("Time can't be null");
        try
        {
            imageMap.remove(time);
            makeNewSequence();
        }
        catch (Exception excp)
        {
            throw new VisADException(
                "Unable to remove image at " + time + " from sequence");
        }
        return sequence;
    }

    /**
     * Remove all images from the sequence.
     *
     */ 
    public void clearSequence()
    {
        imageMap.clear();
        sequence = null;
    }

    /**
     * Set the sequence that this object is to manage.
     *
     * @param  newSequence  sequence to use (can't be null)
     * @throws VisADException   Couldn't create the sequence.
     * @throws RemoteException  Couldn't create remote object.
     */
    public void setImageSequence(ImageSequence newSequence)
        throws VisADException, RemoteException
    {
        if (newSequence == null) 
            throw new VisADException("New sequence can't be null");
        clearSequence();
        int numImages = newSequence.getDomainSet().getLength();
        for (int i = 0; i < numImages; i++)
        {
            SingleBandedImage image = 
                (SingleBandedImage) newSequence.getSample(i);
            imageMap.put(image.getStartTime(), image);
        }
        sequence = newSequence;
    }

    /**
     * Get the sequence that this object is to manage.
     *
     * @return  sequence that is being managed.
     */
    public ImageSequence getImageSequence()
    {
        return sequence;
    }

    private void makeNewSequence()
        throws VisADException, RemoteException
    {
        if (imageMap.isEmpty()) 
            sequence = null;
        else
        {
            Collection imageSet = imageMap.values();
            SingleBandedImage[] images =
                (SingleBandedImage[]) imageSet.toArray(
                    new SingleBandedImage[imageSet.size()]);
            FunctionType imageFunction = (FunctionType) images[0].getType();
            FunctionType ftype = new FunctionType(RealType.Time, imageFunction);
            sequence = new ImageSequenceImpl(ftype, images);
        }
    }
}
