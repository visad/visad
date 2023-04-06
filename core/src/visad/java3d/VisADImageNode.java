package visad.java3d;
import visad.*;
import visad.data.CachedBufferedByteImage;

import org.jogamp.java3d.ImageComponent2D;
import org.jogamp.java3d.ImageComponent2D.Updater;
import org.jogamp.java3d.Behavior;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Switch;
import org.jogamp.java3d.BoundingSphere;

import org.jogamp.vecmath.Point3d;
import java.awt.image.*;
import java.awt.color.*;
import java.util.Enumeration;
import java.util.ArrayList;
import java.util.Iterator;
import org.jogamp.java3d.WakeupCriterion;
import org.jogamp.java3d.WakeupOnElapsedFrames;
import org.jogamp.java3d.WakeupOnElapsedTime;
import org.jogamp.java3d.WakeupOnBehaviorPost;


public class VisADImageNode {

   VisADImageTile[] images;
   public ArrayList<VisADImageTile> imageTiles = new ArrayList<VisADImageTile>(); 
   public int numChildren = 0;
   public BranchGroup branch;
   Switch swit;
   public int current_index = 0;

   public int numImages;
   public int data_width;
   public int data_height;

   AnimateBehavior animate = null; 

   public VisADImageNode() {
   }

   public VisADImageNode(BranchGroup branch, Switch swit) {
     this.branch = branch;
     this.swit = swit;
   }

   public void addTile(VisADImageTile tile) {
    imageTiles.add(tile);
    numChildren++;
   }

   public VisADImageTile getTile(int index) {
     return imageTiles.get(index);
   }

   public Iterator getTileIterator() {
     return imageTiles.iterator();
   }

   public int getNumTiles() {
     return numChildren;
   }

   /**
   //- for implementing Updater
   public void updateData(ImageComponent2D imageC2d, int x, int y, int lenx, int leny) {
     if (images != null) {
       //-imageComp.set(images[current_index]); // This should probably not be done in updateData
     }
   }
   **/


   public void setCurrent(int idx) {
     current_index = idx;

     //images[i].setCurrent(idx);
     for (int i=0; i<numChildren; i++) {
       imageTiles.get(i).setCurrent(idx);
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
     // need to iterate over children (tiles)
     /**
     if (images != null && imageComp != null) {
       imageComp.set(images[index]);
     }
     **/
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

    public void processStimulus(java.util.Iterator<WakeupCriterion> criteria) {
        //do something?
        imageNode.update(current);
        wakeupOn(wakeupC);
    }
}
