
//
// QuickSort.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden and Tom
Rink.
 
This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 1, or (at your option)
any later version.
 
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License in file NOTICE for more details.
 
You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/

package visad;

import java.util.*;

/**
   QuickSort sorts a set of samples in R using a quicksort
   algorithm combined with an insertion sort algorithm to
   avoid an excess number of recursive calls.<P>

   All of QuickSort's variables and methods are static.<P>
*/
public class QuickSort {

  // number of elements to sort with insertion sort
  private static final int CHEAT_NUM = 15;

  // default number of elements to test in main method
  private static final int ELEMENTS = 100000;

  // Insertion Sort
  private static void insertion(float a[], int[] p, int lo, int hi)
                                      throws VisADException {
    for (int i=lo+1; i<=hi; i++) {
      int j = i;
      float B = a[i];
      int P = p[i];
      while ((j > 0) && (a[j-1] > B)) {
        a[j] = a[j-1];
        p[j] = p[j-1];
        j--;
      }
      a[j] = B;
      p[j] = P;
    }
  }

  // Quick Sort
  private static void sort(float a[], int[] p, int lo0, int hi0)
                                      throws VisADException {
    // call the insertion sort if few enough elements
    if (hi0-lo0 < CHEAT_NUM) {
      insertion(a, p, lo0, hi0);
    }
    else {
      int lo = lo0;
      int hi = hi0;

      // start in the middle
      float mid = a[(lo0+hi0)/2];

      // loop through the array until indices cross
      while (lo <= hi) {
        // find lo-most element >= partition element
        while ( (lo < hi0) && (a[lo] < mid) ) ++lo;

        // find hi-most element <= partition element
        while ( (hi > lo0) && (a[hi] > mid) ) --hi;

        // swap indices if they have not crossed
        if (lo <= hi) {
          int P = p[lo];
          p[lo] = p[hi];
          p[hi] = P;
          float T = a[lo];
          a[lo++] = a[hi];
          a[hi--] = T;
        }
      }
      // sort the left partition if necessary
      if (lo0 < hi) sort(a, p, lo0, hi);

      // sort the right partition if necessary
      if (lo < hi0) sort(a, p, lo, hi0);
    }
  }

  public static int[] sort(float a[]) throws VisADException {
    int[] p = new int[a.length];
    for (int i=0; i<a.length; i++) p[i] = i;
    sort(a, p, 0, a.length-1);
    return p;
  }

  /* run 'java visad.QuickSort [elements]' to test the QuickSort class.
     [elements] defaults to 100000, or you can specify your own value. */
  public static void main(String[] argv) throws VisADException {
    int elements = ELEMENTS;
    if (argv.length > 0) {
      try {
        elements = Integer.parseInt(argv[0]);
      }
      catch (Exception e) {
        System.out.println("Usage: java visad.QuickSort "
                          +"[number of elements to sort]");
        System.exit(1);
      }
    }
    System.out.print("Creating array of "+elements+" random elements...");
    long start1 = System.currentTimeMillis();
    float[] test = new float[elements];
    // make up an array with random elements
    for (int i=0; i<elements; i++) {
      test[i] = (float) (1000*Math.random());
    }
    long end1 = System.currentTimeMillis();
    float time1 = (float) (end1-start1) /1000;
    System.out.println("\nCreation of random elements took "
                                          +time1+" seconds.");
    System.out.print("Sorting...");
    long start2 = System.currentTimeMillis();
    int[] p = sort(test);
    long end2 = System.currentTimeMillis();
    System.out.println("done.");
    for (int i=1; i<elements; i++) {
      if (test[i-1] > test[i]) {
        System.out.println("Error in sort, values not in order!");
        System.exit(1);
      }
    }
    float time2 = (float) (end2-start2) /1000;
    System.out.println("Sort of elements took "+time2+" seconds.");
    System.exit(0);
  }

/* Here's the output:

iris 99% java visad.QuickSort 500000
Creating array of 500000 random elements...
Creation of random elements took 11.691 seconds.
Sorting...done.
Sort of elements took 4.953 seconds.
iris 100% 

*/

}

