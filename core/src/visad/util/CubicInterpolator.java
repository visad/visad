package visad.util;

import Jama.Matrix;
import Jama.LUDecomposition;
import java.util.Arrays;

public class CubicInterpolator implements Interpolator {

      private LUDecomposition solver;

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
      
      public CubicInterpolator(boolean doIntrp, int numSpatialPts) {
         this.doIntrp = doIntrp;
         this.numSpatialPts = numSpatialPts;
         this.solution = new double[4][numSpatialPts];
         this.needed = new boolean[numSpatialPts];
         this.computed = new boolean[numSpatialPts];
         Arrays.fill(needed, false);
         Arrays.fill(computed, false);
      }

      private void buildSolver() {
         double x0_p3 = x0*x0*x0;
         double x1_p3 = x1*x1*x1;
         double x2_p3 = x2*x2*x2;

         double x0_p2 = x0*x0;
         double x1_p2 = x1*x1;
         double x2_p2 = x2*x2;

         Matrix coeffs = new Matrix(new double[][]
              { {x0_p3, x0_p2, x0, 1},
                {x1_p3, x1_p2, x1, 1},
                {x2_p3, x2_p2, x2, 1},
                {3*x0_p2, 2*x0, 1, 0}}, 4, 4);

         solver = new LUDecomposition(coeffs);
      }

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
            interpValues[k] = (float) cubic_poly(xt, solution[0][k], solution[1][k], solution[2][k], solution[3][k]);
         }
      }

      public void next(double x0, double x1, double x2, float[] values0, float[] values1, float[] values2) {
         this.x0 = x0;
         this.x1 = x1;
         this.x2 = x2;
         this.values0 = values0;
         this.values1 = values1;
         this.values2 = values2;
         Arrays.fill(computed, false);
         
         if (!doIntrp) {
           return;
         }
         
         buildSolver();
      }
      
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
            double y0 = values0[k];
            double y1 = values1[k];
            double y2 = values2[k];

            // TODO: Initialize first derivative at first point with estimate from the
            // first two data pts instead of using derivative from cubic polynomial fit
            // at the last point.  This works pretty well, but can be improved.
            double D1 = (y1 - y0)/(x1 - x0);
            //double D1 = cubic_poly_D1(x0, solution[0][k], solution[1][k], solution[2][k]);     
            
            double[] sol = getSolution(y0, y1, y2, D1);
            solution[0][k] = sol[0];
            solution[1][k] = sol[1];
            solution[2][k] = sol[2];
            solution[3][k] = sol[3];
            
            computed[k] = true;
         }
      }

      private double[] getSolution(double y0, double y1, double y2, double D1) {
        Matrix constants = new Matrix(new double[][]
             { {y0}, {y1}, {y2}, {D1} }, 4, 1);

        double[][] solution = (solver.solve(constants)).getArray();

        return new double[] {solution[0][0], solution[1][0], solution[2][0], solution[3][0]};
      }

      public static double cubic_poly_D1(double x, double a, double b, double c) {
         return 3*a*x*x + 2*b*x + c;
      }

      public static double cubic_poly(double x, double a, double b, double c, double d) {
         return a*x*x*x + b*x*x + c*x + d;
      }
  }
