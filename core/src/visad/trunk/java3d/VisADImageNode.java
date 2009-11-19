package visad.java3d;
import visad.*;
import visad.data.CachedBufferedByteImage;

import javax.media.j3d.ImageComponent2D;
import javax.media.j3d.ImageComponent2D.Updater;
import javax.media.j3d.Behavior;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Switch;
import javax.media.j3d.BoundingSphere;

import javax.vecmath.Point3d;
import java.awt.image.*;
import java.awt.color.*;
import java.util.Enumeration;
import javax.media.j3d.WakeupCriterion;
import javax.media.j3d.WakeupOnElapsedFrames;
import javax.media.j3d.WakeupOnElapsedTime;
import javax.media.j3d.WakeupOnBehaviorPost;


public class VisADImageNode implements ImageComponent2D.Updater {

   BufferedImage[] images;
   int numImages;
   public BranchGroup branch;
   Switch swit;
   public ImageComponent2D imageComp;
   BufferedImage buf_image = null;
   public int current_index = 0;


   AnimateBehavior animate = null; 


   public VisADImageNode() {
   }

   public void setImages(BufferedImage[] images) {
     this.images = images;
     this.numImages = images.length;
   }

   public BufferedImage[] getImages() {
     return this.images;
   }

   public void setImageComponent(ImageComponent2D imageComp) {
     this.imageComp = imageComp;
   }

   public void updateData(ImageComponent2D imageC2d, int x, int y, int lenx, int leny) {
     if (images != null) {
       //-imageComp.set(images[current_index]); // This should probably not be done in updateData
     }
   }


   private int lookAheadIndexBaseIndex = 0;

   public void setCurrent(int idx) {
     current_index = idx;

     //Have a local array here in case the images array changes in another thread
     BufferedImage[] theImages = images;

     if (imageComp != null && theImages != null && idx>=0 && idx< theImages.length) {
      //-imageComp.updateData(this, 0, 0, 0, 0); // See note above

       BufferedImage image = theImages[idx];
       if(image == null) {
	   //	   System.err.println ("Animate image is null for index:" + idx);
       } else {
	   imageComp.set(image);
	   //Do the lookahead
	   if(image instanceof CachedBufferedByteImage) {
	       //Find the next image
	       CachedBufferedByteImage nextImage = null;

	       //If we are at the end of the loop then go to the beginning
	       int nextIdx = idx+1;
	       if(nextIdx>=theImages.length)
		   nextIdx = 0;
	       nextImage = (CachedBufferedByteImage)theImages[nextIdx];
	       if(nextImage!=null && !nextImage.inMemory()) {
		   final CachedBufferedByteImage imageToLoad = nextImage;
		   Runnable r = new Runnable() {
			   public  void run() {
			       imageToLoad.getBytesFromCache();
			   }
		       };
		   Thread t = new Thread(r);
		   t.start();
	       }
	   }
       }
     }

     /** use if stepping via a Behavior
     if (animate != null) {
       animate.setCurrent(idx);
       animate.postId(777);
     }
     */

   }


/** use these with custom Behavior below **/
   public void initialize() {
     animate = new AnimateBehavior(this);
     animate.setEnable(true);
   }

   public void update(int index) {
     if (images != null && imageComp != null) {
       imageComp.set(images[index]);
     }
   }

   public void setBranch(BranchGroup branch) {
     this.branch = branch;
   }

   public void setSwitch(Switch swit) {
     this.swit = swit;
   }

   public Switch getSwitch() {
     return swit;
   }

   public BranchGroup getBranch() {
     return branch;
   }
}


class AnimateBehavior extends Behavior {
  private WakeupCriterion wakeupC;
  int current = 0;
  VisADImageNode imageNode;

  AnimateBehavior(VisADImageNode imgNode) {
    this.imageNode = imgNode;
    BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 100.0);
    this.setSchedulingBounds(bounds);
    imageNode.branch.addChild(this);
    wakeupC = new WakeupOnBehaviorPost(null, 777);
  }

  public void initialize() {
    wakeupOn(wakeupC);
  }

  // procesStimulus changes the ImageComponent of the texture
  public void processStimulus(Enumeration criteria) {
    imageNode.update(current);
    wakeupOn(wakeupC);
  }

  public void setCurrent(int idx) {
    current = idx;
  }
}
