package visad.util;

import java.util.Arrays;

public class LinearInterpolator implements Interpolator {

      private double[][] solution = null;

      private double x0 = 0;
      private double x1 = 0;
      private double x2 = 0;
      private double x0_last = 0;
      private double x0_save;
      
      private float[] values0 = null;
      private float[] values1 = null;
      private float[] values2 = null;
      private float[] values0_last = null;
      private float[] values0_save = null;

      private int numSpatialPts = 1;

      private boolean doIntrp = true;
      
      private boolean[] needed = null;
      private boolean[] computed = null;
      
      public LinearInterpolator(boolean doIntrp, int numSpatialPts) {
         this.doIntrp = doIntrp;
         this.numSpatialPts = numSpatialPts;
         this.solution = new double[4][numSpatialPts];
         this.needed = new boolean[numSpatialPts];
         this.computed = new boolean[numSpatialPts];
         Arrays.fill(needed, false);
         Arrays.fill(computed, false);
      }


   @Override
      public void interpolate(double xt, float[] interpValues) {
         if (!doIntrp) {
            if (xt == x0) {
               System.arraycopy(values0, 0, interpValues, 0, numSpatialPts);
            }
            else if (xt == x1) {
               System.arraycopy(values1, 0, interpValues, 0, numSpatialPts);
            }
            else if (xt == x2) {
               System.arraycopy(values2, 0, interpValues, 0, numSpatialPts);               
            }
            return;
         }
         java.util.Arrays.fill(interpValues, Float.NaN);
         
         for (int k=0; k<numSpatialPts; k++) {
            if (!computed[k]) { // don't need to interp at these locations, at this time
                continue;
            }
            interpValues[k] = (float) (solution[0][k]*xt + solution[1][k]);
         }
      }

   @Override
      public void next(double x0, double x1, double x2, float[] values0, float[] values1, float[] values2) {
         this.x0 = x0;
         this.x1 = x1;
         this.x2 = x2;
         this.values0 = values0;
         this.values1 = values1;
         this.values2 = values2;
         
         this.x0_last = x0_save;
         this.x0_save = x0;
         this.values0_last = values0_save;
         this.values0_save = values0;
         Arrays.fill(computed, false);
         
         if (!doIntrp) {
           return;
         }
         
      }
 
   @Override
      public void update(boolean[] needed) {
          java.util.Arrays.fill(this.needed, false);
          for (int k=0; k<numSpatialPts; k++) {
              if (needed[k]) {
                  if (!computed[k]) {
                      this.needed[k] = true;
                  }
              }
          }
          if (doIntrp) {
                getSolution();
          }
      }
      
      private void getSolution() {
         for (int k=0; k<numSpatialPts; k++) {
            if (!this.needed[k]) {
                continue;
            }
            
            double D1_1 = Double.NaN;
            double D1_0 = Double.NaN;
            double y0 = values0[k];
            double y1 = values1[k];
            
            
            solution[0][k] = (y1 - y0)/(x1 - x0);
            solution[1][k] = y0 - solution[0][k]*x0;
            
            computed[k] = true;
         }
      }
      
  }
