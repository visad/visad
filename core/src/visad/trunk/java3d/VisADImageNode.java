package visad.java3d;
import visad.*;

import javax.media.j3d.ImageComponent2D;
import javax.media.j3d.ImageComponent2D.Updater;
import javax.media.j3d.Behavior;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.BoundingSphere;
import javax.vecmath.Point3d;
import java.awt.image.*;
import java.awt.color.*;
import java.util.Enumeration;
import javax.media.j3d.WakeupCriterion;
import javax.media.j3d.WakeupOnElapsedFrames;


public class VisADImageNode implements ImageComponent2D.Updater {

   BufferedImage[] images;
   int numImages;
   public BranchGroup branch;
   public ImageComponent2D imageComp;
   BufferedImage buf_image = null;
   public int current_index = 0;

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
       imageComp.set(images[current_index]);
     }
   }

   public void setCurrent(int idx) {
     current_index = idx;
     if (imageComp != null) {
       imageComp.updateData(this, 0, 0, 0, 0);
     }
   }

   /** These would be used with the Behavior thread logic below
   public void initialize() {
     AnimateBehavior animate = new AnimateBehavior(this);
     animate.setEnable(true);
   }

   public void update(int index) {
     imageComp.set(images[index]);
   }

   public void setBranch(BranchGroup branch) {
     this.branch = branch;
   }
   **/
}

/*** another way to update an ImageComponent  ------------------------------
class AnimateBehavior extends Behavior {
  private WakeupCriterion wakeupC;
  int current = 0;
  int max = 32;
  VisADImageNode imageNode;

   AnimateBehavior(VisADImageNode imgNode) {
     this.imageNode = imgNode;
     BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 100.0);
     this.setSchedulingBounds(bounds);
     imageNode.branch.addChild(this);
     wakeupC = new WakeupOnElapsedFrames(10);
     max = imgNode.numImages;
   }

  public void initialize() {
    if (current < max - 1)
      current++;
    else
      current = 0;
    wakeupOn(wakeupC);
  }

  // procesStimulus changes the ImageComponent of the texture
  public void processStimulus(Enumeration criteria) {
    imageNode.update(current);
    if (current < max - 1)
      current++;
    else
      current = 0;
    wakeupOn(wakeupC);
  }
}
**/
