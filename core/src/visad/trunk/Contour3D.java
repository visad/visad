
//
// Contour3D.java
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

The Contour3D class is derived from functions written in the C
language by Andre Battaiola.
*/

package visad;


/**
   Contour3D is the VisAD class for extracting iso-surfaces from
   3-D grids.  It is temporary and will be incorporated into the
   Gridded3DSet class.<P>
*/
public class Contour3D {

//<           instance variables            >

    static int  BIG_NEG = (int) -2e+9;
    static double  EPS_0 = (double) 1.0e-5;
    static double  EPS_1 = (double) (1.0 - EPS_0);

    double[] ptGRID;      //  Range values at domain samples
    double[] VX;
    double[] VY;
    double[] VZ;
    int  xdim;
    int  ydim;           //  Rectangular grid topology
    int  zdim;
    double  ARX;          //  x_dim  Aspect Ratio
    double  ARY;          //  y_dim  Aspect Ratio
    double  ARZ;          //  z_dim  Aspect Ratio
    double  isovalue;     //  constant range value surface
    int  LowLev;
    int  num_verts;
    double[] NX;
    double[] NY;
    double[] NZ;
    int NPTS;
    int[] VPTS;
/* WLH 25 Oct 97: Java3D, not PEX
    boolean PEX;
*/

//<          class variables          >

/* WLH 24 Oct 97
        static double INVALID_VALUE = (double) 1.0e30;
        static double INV_VAL = INVALID_VALUE;
*/
        static boolean  TRUE = true;
        static boolean  FALSE = false;
        static int  MASK = 0x0F;
        static int MAX_FLAG_NUM = 317;
        static int SF_6B = 0;
        static int SF_6D = 6;
        static int SF_79 = 12;
        static int SF_97 = 18;
        static int SF_9E = 24;
        static int SF_B6 = 30;
        static int SF_D6 = 36;
        static int SF_E9 = 42;
        static int Zp = 0;
        static int Zn = 1;
        static int Yp = 2;
        static int Yn = 3;
        static int Xp = 4;
        static int Xn = 5;
        static int incZn = 0;
        static int incYn = 8;
        static int incXn = 16;

static int pol_edges[][] =
{
  {  0x0,    0,   0x0,   0x0,     0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0  },
  {  0x1,    1,   0x3,   0xe,     1, 3, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0  },
  {  0x1,    1,   0x3,   0x32,    4, 5, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0  },
  {  0x3,    1,   0x4,   0x3c,    2, 4, 5, 3, 0, 0, 0, 0, 0, 0, 0, 0  },
  {  0x1,    1,   0x3,   0xc4,    2, 7, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0  },
  {  0x3,    1,   0x4,   0xca,    6, 1, 3, 7, 0, 0, 0, 0, 0, 0, 0, 0  },
  {  0x6,    2,   0x33,  0xf6,    1, 4, 5, 2, 7, 6, 0, 0, 0, 0, 0, 0  },
  {  0x7,    1,   0x5,   0xf8,    4, 5, 3, 7, 6, 0, 0, 0, 0, 0, 0, 0  },
  {  0x1,    1,   0x3,   0x150,   6, 8, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0  },
  {  0x6,    2,   0x33,  0x15e,   4, 6, 8, 1, 3, 2, 0, 0, 0, 0, 0, 0  },
  {  0x3,    1,   0x4,   0x162,   1, 6, 8, 5, 0, 0, 0, 0, 0, 0, 0, 0  },
  {  0x7,    1,   0x5,   0x16c,   6, 8, 5, 3, 2, 0, 0, 0, 0, 0, 0, 0  },
  {  0x3,    1,   0x4,   0x194,   4, 2, 7, 8, 0, 0, 0, 0, 0, 0, 0, 0  },
  {  0x7,    1,   0x5,   0x19a,   1, 3, 7, 8, 4, 0, 0, 0, 0, 0, 0, 0  },
  {  0x7,    1,   0x5,   0x1a6,   2, 7, 8, 5, 1, 0, 0, 0, 0, 0, 0, 0  },
  {  0xf,    1,   0x4,   0x1a8,   5, 3, 7, 8, 0, 0, 0, 0, 0, 0, 0, 0  },
  {  0x1,    1,   0x3,   0x608,   3, 9,10, 0, 0, 0, 0, 0, 0, 0, 0, 0  },
  {  0x3,    1,   0x4,   0x606,  10, 2, 1, 9, 0, 0, 0, 0, 0, 0, 0, 0  },
  {  0x6,    2,   0x33,  0x63a,   3, 9,10, 1, 4, 5, 0, 0, 0, 0, 0, 0  },
  {  0x7,    1,   0x5,   0x634,   9,10, 2, 4, 5, 0, 0, 0, 0, 0, 0, 0  },
  {  0x6,    2,   0x33,  0x6cc,   2, 7, 6, 3, 9,10, 0, 0, 0, 0, 0, 0  },
  {  0x7,    1,   0x5,   0x6c2,   7, 6, 1, 9,10, 0, 0, 0, 0, 0, 0, 0  },
  {  0x16,   3,   0x333, 0x6fe,   1, 4, 5, 2, 7, 6, 3, 9,10, 0, 0, 0  },
  {  0x17,   1,   0x6,   0x6f0,   5, 9,10, 7, 6, 4, 0, 0, 0, 0, 0, 0  },
  {  0x18,   2,   0x33,  0x758,   3, 9,10, 4, 6, 8, 0, 0, 0, 0, 0, 0  },
  {  0x19,   2,   0x34,  0x756,   1, 9,10, 2, 4, 6, 8, 0, 0, 0, 0, 0  },
  {  0x19,   2,   0x34,  0x76a,   1, 6, 8, 5, 3, 9,10, 0, 0, 0, 0, 0  },
  {  0x1b,   1,   0x6,   0x764,   2, 6, 8, 5, 9,10, 0, 0, 0, 0, 0, 0  },
  {  0x19,   2,   0x34,  0x79c,   7, 8, 4, 2,10, 3, 9, 0, 0, 0, 0, 0  },
  {  0x1d,   1,   0x6,   0x792,   1, 9,10, 7, 8, 4, 0, 0, 0, 0, 0, 0  },
  {  0x1e,   2,   0x53,  0x7ae,   3, 9,10, 2, 7, 8, 5, 1, 0, 0, 0, 0  },
  {  0xf8,   1,   0x5,   0x7a0,  10, 7, 8, 5, 9, 0, 0, 0, 0, 0, 0, 0  },
  {  0x1,    1,   0x3,   0xa20,   9, 5,11, 0, 0, 0, 0, 0, 0, 0, 0, 0  },
  {  0x6,    2,   0x33,  0xa2e,   1, 3, 2, 5,11, 9, 0, 0, 0, 0, 0, 0  },
  {  0x3,    1,   0x4,   0xa12,   4,11, 9, 1, 0, 0, 0, 0, 0, 0, 0, 0  },
  {  0x7,    1,   0x5,   0xa1c,   3, 2, 4,11, 9, 0, 0, 0, 0, 0, 0, 0  },
  {  0x18,   2,   0x33,  0xae4,   5,11, 9, 6, 2, 7, 0, 0, 0, 0, 0, 0  },
  {  0x19,   2,   0x34,  0xaea,   3, 7, 6, 1, 9, 5,11, 0, 0, 0, 0, 0  },
  {  0x19,   2,   0x34,  0xad6,   4,11, 9, 1, 6, 2, 7, 0, 0, 0, 0, 0  },
  {  0x1d,   1,   0x6,   0xad8,   3, 7, 6, 4,11, 9, 0, 0, 0, 0, 0, 0  },
  {  0x6,    2,   0x33,  0xb70,   5,11, 9, 4, 6, 8, 0, 0, 0, 0, 0, 0  },
  {  0x16,   3,   0x333, 0xb7e,   4, 6, 8, 1, 3, 2, 5,11, 9, 0, 0, 0  },
  {  0x7,    1,   0x5,   0xb42,  11, 9, 1, 6, 8, 0, 0, 0, 0, 0, 0, 0  },
  {  0x17,   1,   0x6,   0xb4c,   8,11, 9, 3, 2, 6, 0, 0, 0, 0, 0, 0  },
  {  0x19,   2,   0x34,  0xbb4,   4, 2, 7, 8, 5,11, 9, 0, 0, 0, 0, 0  },
  {  0x1e,   2,   0x53,  0xbba,   5,11, 9, 1, 3, 7, 8, 4, 0, 0, 0, 0  },
  {  0x1b,   1,   0x6,   0xb86,   1, 2, 7, 8,11, 9, 0, 0, 0, 0, 0, 0  },
  {  0xf8,   1,   0x5,   0xb88,   9, 3, 7, 8,11, 0, 0, 0, 0, 0, 0, 0  },
  {  0x3,    1,   0x4,   0xc28,  11,10, 3, 5, 0, 0, 0, 0, 0, 0, 0, 0  },
  {  0x7,    1,   0x5,   0xc26,   5,11,10, 2, 1, 0, 0, 0, 0, 0, 0, 0  },
  {  0x7,    1,   0x5,   0xc1a,   1, 4,11,10, 3, 0, 0, 0, 0, 0, 0, 0  },
  {  0xf,    1,   0x4,   0xc14,   2, 4,11,10, 0, 0, 0, 0, 0, 0, 0, 0  },
  {  0x19,   2,   0x34,  0xcec,   3, 5,11,10, 2, 7, 6, 0, 0, 0, 0, 0  },
  {  0x1b,   1,   0x6,   0xce2,  10, 7, 6, 1, 5,11, 0, 0, 0, 0, 0, 0  },
  {  0x1e,   2,   0x53,  0xcde,   2, 7, 6, 1, 4,11,10, 3, 0, 0, 0, 0  },
  {  0xf8,   1,   0x5,   0xcd0,   6, 4,11,10, 7, 0, 0, 0, 0, 0, 0, 0  },
  {  0x19,   2,   0x34,  0xd78,  11,10, 3, 5, 8, 4, 6, 0, 0, 0, 0, 0  },
  {  0x1e,   2,   0x53,  0xd76,   4, 6, 8, 5,11,10, 2, 1, 0, 0, 0, 0  },
  {  0x1d,   1,   0x6,   0xd4a,   1, 6, 8,11,10, 3, 0, 0, 0, 0, 0, 0  },
  {  0xf8,   1,   0x5,   0xd44,   8,11,10, 2, 6, 0, 0, 0, 0, 0, 0, 0  },
  {  0x3c,   2,   0x44,  0xdbc,   4, 2, 7, 8, 5,11,10, 3, 0, 0, 0, 0  },
  {  0xe6,   2,   0x34,  0xdb2,   8,11,10, 7, 4, 1, 5, 0, 0, 0, 0, 0  },
  {  0xe6,   2,   0x34,  0xd8e,  10, 7, 8,11, 3, 1, 2, 0, 0, 0, 0, 0  },
  {  0xfc,   1,   0x4,   0xd80,  10, 7, 8,11, 0, 0, 0, 0, 0, 0, 0, 0  },
  {  0x1,    1,   0x3,   0x1480, 10,12, 7, 0, 0, 0, 0, 0, 0, 0, 0, 0  },
  {  0x6,    2,   0x33,  0x148e,  3, 2, 1,10,12, 7, 0, 0, 0, 0, 0, 0  },
  {  0x18,   2,   0x33,  0x14b2,  7,10,12, 1, 4, 5, 0, 0, 0, 0, 0, 0  },
  {  0x19,   2,   0x34,  0x14bc,  2, 4, 5, 3, 7,10,12, 0, 0, 0, 0, 0  },
  {  0x3,    1,   0x4,   0x1444,  2,10,12, 6, 0, 0, 0, 0, 0, 0, 0, 0  },
  {  0x7,    1,   0x5,   0x144a, 10,12, 6, 1, 3, 0, 0, 0, 0, 0, 0, 0  },
  {  0x19,   2,   0x34,  0x1476,  2,10,12, 6, 1, 4, 5, 0, 0, 0, 0, 0  },
  {  0x1b,   1,   0x6,   0x1478,  6, 4, 5, 3,10,12, 0, 0, 0, 0, 0, 0  },
  {  0x6,    2,   0x33,  0x15d0,  6, 8, 4, 7,10,12, 0, 0, 0, 0, 0, 0  },
  {  0x16,   3,   0x333, 0x15de,  2, 1, 3, 6, 8, 4, 7,10,12, 0, 0, 0  },
  {  0x19,   2,   0x34,  0x15e2,  8, 5, 1, 6,12, 7,10, 0, 0, 0, 0, 0  },
  {  0x1e,   2,   0x53,  0x15ec,  7,10,12, 6, 8, 5, 3, 2, 0, 0, 0, 0  },
  {  0x7,    1,   0x5,   0x1514,  8, 4, 2,10,12, 0, 0, 0, 0, 0, 0, 0  },
  {  0x17,   1,   0x6,   0x151a,  3,10,12, 8, 4, 1, 0, 0, 0, 0, 0, 0  },
  {  0x1d,   1,   0x6,   0x1526,  2,10,12, 8, 5, 1, 0, 0, 0, 0, 0, 0  },
  {  0xf8,   1,   0x5,   0x1528, 12, 8, 5, 3,10, 0, 0, 0, 0, 0, 0, 0  },
  {  0x3,    1,   0x4,   0x1288,  7, 3, 9,12, 0, 0, 0, 0, 0, 0, 0, 0  },
  {  0x7,    1,   0x5,   0x1286,  2, 1, 9,12, 7, 0, 0, 0, 0, 0, 0, 0  },
  {  0x19,   2,   0x34,  0x12ba,  9,12, 7, 3, 5, 1, 4, 0, 0, 0, 0, 0  },
  {  0x1d,   1,   0x6,   0x12b4,  9,12, 7, 2, 4, 5, 0, 0, 0, 0, 0, 0  },
  {  0x7,    1,   0x5,   0x124c,  3, 9,12, 6, 2, 0, 0, 0, 0, 0, 0, 0  },
  {  0xf,    1,   0x4,   0x1242,  1, 9,12, 6, 0, 0, 0, 0, 0, 0, 0, 0  },
  {  0x1e,   2,   0x53,  0x127e,  1, 4, 5, 3, 9,12, 6, 2, 0, 0, 0, 0  },
  {  0xf8,   1,   0x5,   0x1270,  5, 9,12, 6, 4, 0, 0, 0, 0, 0, 0, 0  },
  {  0x19,   2,   0x34,  0x13d8,  7, 3, 9,12, 6, 8, 4, 0, 0, 0, 0, 0  },
  {  0x1e,   2,   0x53,  0x13d6,  6, 8, 4, 2, 1, 9,12, 7, 0, 0, 0, 0  },
  {  0x3c,   2,   0x44,  0x13ea,  1, 6, 8, 5, 3, 9,12, 7, 0, 0, 0, 0  },
  {  0xe6,   2,   0x34,  0x13e4, 12, 8, 5, 9, 7, 2, 6, 0, 0, 0, 0, 0  },
  {  0x1b,   1,   0x6,   0x131c,  2, 3, 9,12, 8, 4, 0, 0, 0, 0, 0, 0  },
  {  0xf8,   1,   0x5,   0x1312,  4, 1, 9,12, 8, 0, 0, 0, 0, 0, 0, 0  },
  {  0xe6,   2,   0x34,  0x132e,  5, 9,12, 8, 1, 2, 3, 0, 0, 0, 0, 0  },
  {  0xfc,   1,   0x4,   0x1320,  5, 9,12, 8, 0, 0, 0, 0, 0, 0, 0, 0  },
  {  0x6,    2,   0x33,  0x1ea0, 10,12, 7, 9, 5,11, 0, 0, 0, 0, 0, 0  },
  {  0x16,   3,   0x333, 0x1eae,  3, 2, 1,10,12, 7, 9, 5,11, 0, 0, 0  },
  {  0x19,   2,   0x34,  0x1e92,  9, 1, 4,11,10,12, 7, 0, 0, 0, 0, 0  },
  {  0x1e,   2,   0x53,  0x1e9c, 10,12, 7, 3, 2, 4,11, 9, 0, 0, 0, 0  },
  {  0x19,   2,   0x34,  0x1e64, 12, 6, 2,10,11, 9, 5, 0, 0, 0, 0, 0  },
  {  0x1e,   2,   0x53,  0x1e6a,  9, 5,11,10,12, 6, 1, 3, 0, 0, 0, 0  },
  {  0x3c,   2,   0x44,  0x1e56,  2,10,12, 6, 1, 4,11, 9, 0, 0, 0, 0  },
  {  0xe6,   2,   0x34,  0x1e58, 11,12, 6, 4, 9, 3,10, 0, 0, 0, 0, 0  },
  {  0x16,   3,   0x333, 0x1ff0, 11, 9, 5,12, 7,10, 8, 4, 6, 0, 0, 0  },
  {  0x69,   4,   0x3333,0x1ffe,  1, 3, 2, 6, 8, 4, 9, 5,11,10,12, 7  },
  {  0x1e,   2,   0x53,  0x1fc2, 12, 7,10,11, 9, 1, 6, 8, 0, 0, 0, 0  },
  {  0xe9,   3,   0x333, 0x1fcc, 12, 8,11,10, 9, 3, 7, 2, 6, 0, 0, 0  },
  {  0x1e,   2,   0x53,  0x1f34, 11, 9, 5, 8, 4, 2,10,12, 0, 0, 0, 0  },
  {  0xe9,   3,   0x333, 0x1f3a,  5, 4, 1, 9, 3,10,11,12, 8, 0, 0, 0  },
  {  0xe6,   2,   0x34,  0x1f06, 10, 9, 1, 2,12, 8,11, 0, 0, 0, 0, 0  },
  {  0xf9,   2,   0x33,  0x1f08,  9, 3,10,11,12, 8, 0, 0, 0, 0, 0, 0  },
  {  0x7,    1,   0x5,   0x18a8, 12, 7, 3, 5,11, 0, 0, 0, 0, 0, 0, 0  },
  {  0x17,   1,   0x6,   0x18a6,  1, 5,11,12, 7, 2, 0, 0, 0, 0, 0, 0  },
  {  0x1b,   1,   0x6,   0x189a, 11,12, 7, 3, 1, 4, 0, 0, 0, 0, 0, 0  },
  {  0xf8,   1,   0x5,   0x1894,  7, 2, 4,11,12, 0, 0, 0, 0, 0, 0, 0  },
  {  0x1d,   1,   0x6,   0x186c, 12, 6, 2, 3, 5,11, 0, 0, 0, 0, 0, 0  },
  {  0xf8,   1,   0x5,   0x1862, 11,12, 6, 1, 5, 0, 0, 0, 0, 0, 0, 0  },
  {  0xe6,   2,   0x34,  0x185e,  6, 4,11,12, 2, 3, 1, 0, 0, 0, 0, 0  },
  {  0xfc,   1,   0x4,   0x1850, 11,12, 6, 4, 0, 0, 0, 0, 0, 0, 0, 0  },
  {  0x1e,   2,   0x53,  0x19f8,  8, 4, 6,12, 7, 3, 5,11, 0, 0, 0, 0  },
  {  0xe9,   3,   0x333, 0x19f6,  6, 7, 2, 4, 1, 5, 8,11,12, 0, 0, 0  },
  {  0xe6,   2,   0x34,  0x19ca,  6, 7, 3, 1, 8,11,12, 0, 0, 0, 0, 0  },
  {  0xf9,   2,   0x33,  0x19c4,  8,11,12, 6, 7, 2, 0, 0, 0, 0, 0, 0  },
  {  0xe6,   2,   0x34,  0x193c,  5, 4, 2, 3,11,12, 8, 0, 0, 0, 0, 0  },
  {  0xf9,   2,   0x33,  0x1932, 11,12, 8, 5, 4, 1, 0, 0, 0, 0, 0, 0  },
  {  0xe7,   2,   0x33,  0x190e,  3, 1, 2,12, 8,11, 0, 0, 0, 0, 0, 0  },
  {  0xfe,   1,   0x3,   0x1900, 11,12, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0  },
  {  0x1,    1,   0x3,   0x1900, 11, 8,12, 0, 0, 0, 0, 0, 0, 0, 0, 0  },
  {  0x18,   2,   0x33,  0x190e,  8,12,11, 2, 1, 3, 0, 0, 0, 0, 0, 0  },
  {  0x6,    2,   0x33,  0x1932, 11, 8,12, 5, 1, 4, 0, 0, 0, 0, 0, 0  },
  {  0x19,   2,   0x34,  0x193c,  5, 3, 2, 4,11, 8,12, 0, 0, 0, 0, 0  },
  {  0x6,    2,   0x33,  0x19c4,  8,12,11, 6, 2, 7, 0, 0, 0, 0, 0, 0  },
  {  0x19,   2,   0x34,  0x19ca,  6, 1, 3, 7, 8,12,11, 0, 0, 0, 0, 0  },
  {  0x16,   3,   0x333, 0x19f6,  6, 2, 7, 4, 5, 1, 8,12,11, 0, 0, 0  },
  {  0x1e,   2,   0x53,  0x19f8,  8,12,11, 4, 5, 3, 7, 6, 0, 0, 0, 0  },
  {  0x3,    1,   0x4,   0x1850, 11, 4, 6,12, 0, 0, 0, 0, 0, 0, 0, 0  },
  {  0x19,   2,   0x34,  0x185e,  6,12,11, 4, 2, 1, 3, 0, 0, 0, 0, 0  },
  {  0x7,    1,   0x5,   0x1862,  5, 1, 6,12,11, 0, 0, 0, 0, 0, 0, 0  },
  {  0x1d,   1,   0x6,   0x186c,  6,12,11, 5, 3, 2, 0, 0, 0, 0, 0, 0  },
  {  0x7,    1,   0x5,   0x1894, 12,11, 4, 2, 7, 0, 0, 0, 0, 0, 0, 0  },
  {  0x1b,   1,   0x6,   0x189a,  4, 1, 3, 7,12,11, 0, 0, 0, 0, 0, 0  },
  {  0x17,   1,   0x6,   0x18a6,  7,12,11, 5, 1, 2, 0, 0, 0, 0, 0, 0  },
  {  0xf8,   1,   0x5,   0x18a8, 11, 5, 3, 7,12, 0, 0, 0, 0, 0, 0, 0  },
  {  0x6,    2,   0x33,  0x1f08,  9,10, 3,11, 8,12, 0, 0, 0, 0, 0, 0  },
  {  0x19,   2,   0x34,  0x1f06, 10, 2, 1, 9,12,11, 8, 0, 0, 0, 0, 0  },
  {  0x16,   3,   0x333, 0x1f3a,  9,10, 3,11, 8,12, 5, 1, 4, 0, 0, 0  },
  {  0x1e,   2,   0x53,  0x1f34, 11, 8,12, 9,10, 2, 4, 5, 0, 0, 0, 0  },
  {  0x16,   3,   0x333, 0x1fcc, 10, 3, 9, 7, 6, 2,12,11, 8, 0, 0, 0  },
  {  0x1e,   2,   0x53,  0x1fc2, 12,11, 8, 7, 6, 1, 9,10, 0, 0, 0, 0  },
  {  0x69,   4,   0x3333,0x1ffe,  4, 5, 1, 2, 7, 6,11, 8,12, 9,10, 3  },
  {  0xe9,   3,   0x333, 0x1ff0, 11, 5, 9,12,10, 7, 8, 6, 4, 0, 0, 0  },
  {  0x19,   2,   0x34,  0x1e58, 11, 4, 6,12, 9,10, 3, 0, 0, 0, 0, 0  },
  {  0x3c,   2,   0x44,  0x1e56, 10, 2, 1, 9,12,11, 4, 6, 0, 0, 0, 0  },
  {  0x1e,   2,   0x53,  0x1e6a,  9,10, 3, 5, 1, 6,12,11, 0, 0, 0, 0  },
  {  0xe6,   2,   0x34,  0x1e64, 12,10, 2, 6,11, 5, 9, 0, 0, 0, 0, 0  },
  {  0x1e,   2,   0x53,  0x1e9c, 10, 3, 9,12,11, 4, 2, 7, 0, 0, 0, 0  },
  {  0xe6,   2,   0x34,  0x1e92,  9,11, 4, 1,10, 7,12, 0, 0, 0, 0, 0  },
  {  0xe9,   3,   0x333, 0x1eae, 10, 7,12, 9,11, 5, 3, 1, 2, 0, 0, 0  },
  {  0xf9,   2,   0x33,  0x1ea0, 11, 5, 9,12,10, 7, 0, 0, 0, 0, 0, 0  },
  {  0x3,    1,   0x4,   0x1320, 12, 9, 5, 8, 0, 0, 0, 0, 0, 0, 0, 0  },
  {  0x19,   2,   0x34,  0x132e,  5, 8,12, 9, 1, 3, 2, 0, 0, 0, 0, 0  },
  {  0x7,    1,   0x5,   0x1312,  8,12, 9, 1, 4, 0, 0, 0, 0, 0, 0, 0  },
  {  0x1b,   1,   0x6,   0x131c,  4, 8,12, 9, 3, 2, 0, 0, 0, 0, 0, 0  },
  {  0x19,   2,   0x34,  0x13e4, 12, 9, 5, 8, 7, 6, 2, 0, 0, 0, 0, 0  },
  {  0x3c,   2,   0x44,  0x13ea,  6, 1, 3, 7, 8,12, 9, 5, 0, 0, 0, 0  },
  {  0x1e,   2,   0x53,  0x13d6,  6, 2, 7, 8,12, 9, 1, 4, 0, 0, 0, 0  },
  {  0xe6,   2,   0x34,  0x13d8,  7,12, 9, 3, 6, 4, 8, 0, 0, 0, 0, 0  },
  {  0x7,    1,   0x5,   0x1270,  4, 6,12, 9, 5, 0, 0, 0, 0, 0, 0, 0  },
  {  0x1e,   2,   0x53,  0x127e,  1, 3, 2, 4, 6,12, 9, 5, 0, 0, 0, 0  },
  {  0xf,    1,   0x4,   0x1242,  9, 1, 6,12, 0, 0, 0, 0, 0, 0, 0, 0  },
  {  0xf8,   1,   0x5,   0x124c,  2, 6,12, 9, 3, 0, 0, 0, 0, 0, 0, 0  },
  {  0x1d,   1,   0x6,   0x12b4, 12, 9, 5, 4, 2, 7, 0, 0, 0, 0, 0, 0  },
  {  0xe6,   2,   0x34,  0x12ba,  9, 3, 7,12, 5, 4, 1, 0, 0, 0, 0, 0  },
  {  0xf8,   1,   0x5,   0x1286,  7,12, 9, 1, 2, 0, 0, 0, 0, 0, 0, 0  },
  {  0xfc,   1,   0x4,   0x1288,  7,12, 9, 3, 0, 0, 0, 0, 0, 0, 0, 0  },
  {  0x7,    1,   0x5,   0x1528, 10, 3, 5, 8,12, 0, 0, 0, 0, 0, 0, 0  },
  {  0x1d,   1,   0x6,   0x1526,  5, 8,12,10, 2, 1, 0, 0, 0, 0, 0, 0  },
  {  0x17,   1,   0x6,   0x151a,  3, 1, 4, 8,12,10, 0, 0, 0, 0, 0, 0  },
  {  0xf8,   1,   0x5,   0x1514, 12,10, 2, 4, 8, 0, 0, 0, 0, 0, 0, 0  },
  {  0x1e,   2,   0x53,  0x15ec,  7, 6, 2,10, 3, 5, 8,12, 0, 0, 0, 0  },
  {  0xe6,   2,   0x34,  0x15e2,  8, 6, 1, 5,12,10, 7, 0, 0, 0, 0, 0  },
  {  0xe9,   3,   0x333, 0x15de,  2, 3, 1, 6, 4, 8, 7,12,10, 0, 0, 0  },
  {  0xf9,   2,   0x33,  0x15d0,  6, 4, 8, 7,12,10, 0, 0, 0, 0, 0, 0  },
  {  0x1b,   1,   0x6,   0x1478, 12,10, 3, 5, 4, 6, 0, 0, 0, 0, 0, 0  },
  {  0xe6,   2,   0x34,  0x1476,  2, 6,12,10, 1, 5, 4, 0, 0, 0, 0, 0  },
  {  0xf8,   1,   0x5,   0x144a,  3, 1, 6,12,10, 0, 0, 0, 0, 0, 0, 0  },
  {  0xfc,   1,   0x4,   0x1444,  2, 6,12,10, 0, 0, 0, 0, 0, 0, 0, 0  },
  {  0xe6,   2,   0x34,  0x14bc,  2, 3, 5, 4, 7,12,10, 0, 0, 0, 0, 0  },
  {  0xe7,   2,   0x33,  0x14b2,  7,12,10, 1, 5, 4, 0, 0, 0, 0, 0, 0  },
  {  0xf9,   2,   0x33,  0x148e,  3, 1, 2,10, 7,12, 0, 0, 0, 0, 0, 0  },
  {  0xfe,   1,   0x3,   0x1480, 10, 7,12, 0, 0, 0, 0, 0, 0, 0, 0, 0  },
  {  0x3,    1,   0x4,   0xd80,  10,11, 8, 7, 0, 0, 0, 0, 0, 0, 0, 0  },
  {  0x19,   2,   0x34,  0xd8e,  10,11, 8, 7, 3, 2, 1, 0, 0, 0, 0, 0  },
  {  0x19,   2,   0x34,  0xdb2,   8, 7,10,11, 4, 5, 1, 0, 0, 0, 0, 0  },
  {  0x3c,   2,   0x44,  0xdbc,   2, 4, 5, 3, 7,10,11, 8, 0, 0, 0, 0  },
  {  0x7,    1,   0x5,   0xd44,   6, 2,10,11, 8, 0, 0, 0, 0, 0, 0, 0  },
  {  0x1d,   1,   0x6,   0xd4a,  10,11, 8, 6, 1, 3, 0, 0, 0, 0, 0, 0  },
  {  0x1e,   2,   0x53,  0xd76,   4, 5, 1, 6, 2,10,11, 8, 0, 0, 0, 0  },
  {  0xe6,   2,   0x34,  0xd78,  11, 5, 3,10, 8, 6, 4, 0, 0, 0, 0, 0  },
  {  0x7,    1,   0x5,   0xcd0,   7,10,11, 4, 6, 0, 0, 0, 0, 0, 0, 0  },
  {  0x1e,   2,   0x53,  0xcde,   2, 1, 3, 7,10,11, 4, 6, 0, 0, 0, 0  },
  {  0x1b,   1,   0x6,   0xce2,  11, 5, 1, 6, 7,10, 0, 0, 0, 0, 0, 0  },
  {  0xe6,   2,   0x34,  0xcec,   3,10,11, 5, 2, 6, 7, 0, 0, 0, 0, 0  },
  {  0xf,    1,   0x4,   0xc14,   4, 2,10,11, 0, 0, 0, 0, 0, 0, 0, 0  },
  {  0xf8,   1,   0x5,   0xc1a,   3,10,11, 4, 1, 0, 0, 0, 0, 0, 0, 0  },
  {  0xf8,   1,   0x5,   0xc26,   1, 2,10,11, 5, 0, 0, 0, 0, 0, 0, 0  },
  {  0xfc,   1,   0x4,   0xc28,   3,10,11, 5, 0, 0, 0, 0, 0, 0, 0, 0  },
  {  0x7,    1,   0x5,   0xb88,  11, 8, 7, 3, 9, 0, 0, 0, 0, 0, 0, 0  },
  {  0x1b,   1,   0x6,   0xb86,   7, 2, 1, 9,11, 8, 0, 0, 0, 0, 0, 0  },
  {  0x1e,   2,   0x53,  0xbba,   5, 1, 4,11, 8, 7, 3, 9, 0, 0, 0, 0  },
  {  0xe6,   2,   0x34,  0xbb4,   4, 8, 7, 2, 5, 9,11, 0, 0, 0, 0, 0  },
  {  0x17,   1,   0x6,   0xb4c,   9,11, 8, 6, 2, 3, 0, 0, 0, 0, 0, 0  },
  {  0xf8,   1,   0x5,   0xb42,   8, 6, 1, 9,11, 0, 0, 0, 0, 0, 0, 0  },
  {  0xe9,   3,   0x333, 0xb7e,   4, 8, 6, 1, 2, 3, 5, 9,11, 0, 0, 0  },
  {  0xf9,   2,   0x33,  0xb70,   5, 9,11, 4, 8, 6, 0, 0, 0, 0, 0, 0  },
  {  0x1d,   1,   0x6,   0xad8,   7, 3, 9,11, 4, 6, 0, 0, 0, 0, 0, 0  },
  {  0xe6,   2,   0x34,  0xad6,   4, 1, 9,11, 6, 7, 2, 0, 0, 0, 0, 0  },
  {  0xe6,   2,   0x34,  0xaea,   3, 1, 6, 7, 9,11, 5, 0, 0, 0, 0, 0  },
  {  0xe7,   2,   0x33,  0xae4,   5, 9,11, 6, 7, 2, 0, 0, 0, 0, 0, 0  },
  {  0xf8,   1,   0x5,   0xa1c,   9,11, 4, 2, 3, 0, 0, 0, 0, 0, 0, 0  },
  {  0xfc,   1,   0x4,   0xa12,   4, 1, 9,11, 0, 0, 0, 0, 0, 0, 0, 0  },
  {  0xf9,   2,   0x33,  0xa2e,   9,11, 5, 3, 1, 2, 0, 0, 0, 0, 0, 0  },
  {  0xfe,   1,   0x3,   0xa20,   9,11, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0  },
  {  0x7,    1,   0x5,   0x7a0,   9, 5, 8, 7,10, 0, 0, 0, 0, 0, 0, 0  },
  {  0x1e,   2,   0x53,  0x7ae,   3, 2, 1, 9, 5, 8, 7,10, 0, 0, 0, 0  },
  {  0x1d,   1,   0x6,   0x792,   9, 1, 4, 8, 7,10, 0, 0, 0, 0, 0, 0  },
  {  0xe6,   2,   0x34,  0x79c,   7, 2, 4, 8,10, 9, 3, 0, 0, 0, 0, 0  },
  {  0x1b,   1,   0x6,   0x764,   8, 6, 2,10, 9, 5, 0, 0, 0, 0, 0, 0  },
  {  0xe6,   2,   0x34,  0x76a,   1, 5, 8, 6, 3,10, 9, 0, 0, 0, 0, 0  },
  {  0xe6,   2,   0x34,  0x756,   1, 2,10, 9, 4, 8, 6, 0, 0, 0, 0, 0  },
  {  0xe7,   2,   0x33,  0x758,   3,10, 9, 4, 8, 6, 0, 0, 0, 0, 0, 0  },
  {  0x17,   1,   0x6,   0x6f0,  10, 9, 5, 4, 6, 7, 0, 0, 0, 0, 0, 0  },
  {  0xe9,   3,   0x333, 0x6fe,   1, 5, 4, 2, 6, 7, 3,10, 9, 0, 0, 0  },
  {  0xf8,   1,   0x5,   0x6c2,  10, 9, 1, 6, 7, 0, 0, 0, 0, 0, 0, 0  },
  {  0xf9,   2,   0x33,  0x6cc,   2, 6, 7, 3,10, 9, 0, 0, 0, 0, 0, 0  },
  {  0xf8,   1,   0x5,   0x634,   5, 4, 2,10, 9, 0, 0, 0, 0, 0, 0, 0  },
  {  0xf9,   2,   0x33,  0x63a,   5, 4, 1, 9, 3,10, 0, 0, 0, 0, 0, 0  },
  {  0xfc,   1,   0x4,   0x606,   1, 2,10, 9, 0, 0, 0, 0, 0, 0, 0, 0  },
  {  0xfe,   1,   0x3,   0x608,   9, 3,10, 0, 0, 0, 0, 0, 0, 0, 0, 0  },
  {  0xf,    1,   0x4,   0x1a8,   8, 7, 3, 5, 0, 0, 0, 0, 0, 0, 0, 0  },
  {  0xf8,   1,   0x5,   0x1a6,   1, 5, 8, 7, 2, 0, 0, 0, 0, 0, 0, 0  },
  {  0xf8,   1,   0x5,   0x19a,   4, 8, 7, 3, 1, 0, 0, 0, 0, 0, 0, 0  },
  {  0xfc,   1,   0x4,   0x194,   4, 8, 7, 2, 0, 0, 0, 0, 0, 0, 0, 0  },
  {  0xf8,   1,   0x5,   0x16c,   2, 3, 5, 8, 6, 0, 0, 0, 0, 0, 0, 0  },
  {  0xfc,   1,   0x4,   0x162,   1, 5, 8, 6, 0, 0, 0, 0, 0, 0, 0, 0  },
  {  0xf9,   2,   0x33,  0x15e,   4, 8, 6, 1, 2, 3, 0, 0, 0, 0, 0, 0  },
  {  0xfe,   1,   0x3,   0x150,   6, 4, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0  },
  {  0xf8,   1,   0x5,   0xf8,    6, 7, 3, 5, 4, 0, 0, 0, 0, 0, 0, 0  },
  {  0xf9,   2,   0x33,  0xf6,    1, 5, 4, 2, 6, 7, 0, 0, 0, 0, 0, 0  },
  {  0xfc,   1,   0x4,   0xca,    6, 7, 3, 1, 0, 0, 0, 0, 0, 0, 0, 0  },
  {  0xfe,   1,   0x3,   0xc4,    2, 6, 7, 0, 0, 0, 0, 0, 0, 0, 0, 0  },
  {  0xfc,   1,   0x4,   0x3c,    2, 3, 5, 4, 0, 0, 0, 0, 0, 0, 0, 0  },
  {  0xfe,   1,   0x3,   0x32,    4, 1, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0  },
  {  0xfe,   1,   0x3,   0xe,     1, 2, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0  },
  {  0x0,    0,   0x0,   0x0,     0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0  },
  {  0xe6,   2,   0x54,  0xdb2,   8,11,10, 7, 4, 1, 5,11, 8, 0, 0, 0  },
  {  0xe6,   2,   0x54,  0xd8e,  10, 7, 8,11, 3, 1, 2, 7,10, 0, 0, 0  },
  {  0xe6,   2,   0x54,  0x13e4, 12, 8, 5, 9, 7, 2, 6, 8,12, 0, 0, 0  },
  {  0xe6,   2,   0x54,  0x132e,  5, 9,12, 8, 1, 2, 3, 9, 5, 0, 0, 0  },
  {  0xe6,   2,   0x54,  0x1e58, 11,12, 6, 4, 9, 3,10,12,11, 0, 0, 0  },
  {  0xe6,   2,   0x54,  0x1f06, 10, 9, 1, 2,12, 8,11, 9,10, 0, 0, 0  },
  {  0xe6,   2,   0x54,  0x185e,  6, 4,11,12, 2, 3, 1, 4, 6, 0, 0, 0  },
  {  0xe6,   2,   0x54,  0x19ca,  6, 7, 3, 1, 8,11,12, 7, 6, 0, 0, 0  },
  {  0xe6,   2,   0x54,  0x193c,  5, 4, 2, 3,11,12, 8, 4, 5, 0, 0, 0  },
  {  0xe6,   2,   0x54,  0x1e64, 12,10, 2, 6,11, 5, 9,10,12, 0, 0, 0  },
  {  0xe6,   2,   0x54,  0x1e92,  9,11, 4, 1,10, 7,12,11, 9, 0, 0, 0  },
  {  0xe6,   2,   0x54,  0x13d8,  7,12, 9, 3, 6, 4, 8,12, 7, 0, 0, 0  },
  {  0xe6,   2,   0x54,  0x12ba,  9, 3, 7,12, 5, 4, 1, 3, 9, 0, 0, 0  },
  {  0xe6,   2,   0x54,  0x15e2,  8, 6, 1, 5,12,10, 7, 6, 8, 0, 0, 0  },
  {  0xe6,   2,   0x54,  0x1476,  2, 6,12,10, 1, 5, 4, 6, 2, 0, 0, 0  },
  {  0xe6,   2,   0x54,  0x14bc,  2, 3, 5, 4, 7,12,10, 3, 2, 0, 0, 0  },
  {  0xe6,   2,   0x54,  0xd78,  11, 5, 3,10, 8, 6, 4, 5,11, 0, 0, 0  },
  {  0xe6,   2,   0x54,  0xcec,   3,10,11, 5, 2, 6, 7,10, 3, 0, 0, 0  },
  {  0xe6,   2,   0x54,  0xbb4,   4, 8, 7, 2, 5, 9,11, 8, 4, 0, 0, 0  },
  {  0xe6,   2,   0x54,  0xad6,   4, 1, 9,11, 6, 7, 2, 1, 4, 0, 0, 0  },
  {  0xe6,   2,   0x54,  0xaea,   3, 1, 6, 7, 9,11, 5, 1, 3, 0, 0, 0  },
  {  0xe6,   2,   0x54,  0x79c,   7, 2, 4, 8,10, 9, 3, 2, 7, 0, 0, 0  },
  {  0xe6,   2,   0x54,  0x76a,   1, 5, 8, 6, 3,10, 9, 5, 1, 0, 0, 0  },
  {  0xe6,   2,   0x54,  0x756,   1, 2,10, 9, 4, 8, 6, 2, 1, 0, 0, 0  },
  {  0xf9,   1,   0x6,   0x1f08,  9, 3,10,12, 8,11, 0, 0, 0, 0, 0, 0  },
  {  0xf9,   1,   0x6,   0x19c4,  8,11,12, 7, 2, 6, 0, 0, 0, 0, 0, 0  },
  {  0xf9,   1,   0x6,   0x1932, 11,12, 8, 4, 1, 5, 0, 0, 0, 0, 0, 0  },
  {  0xf9,   1,   0x6,   0x1ea0, 11, 5, 9,10, 7,12, 0, 0, 0, 0, 0, 0  },
  {  0xf9,   1,   0x6,   0x15d0,  6, 4, 8,12,10, 7, 0, 0, 0, 0, 0, 0  },
  {  0xf9,   1,   0x6,   0x148e,  3, 1, 2, 7,12,10, 0, 0, 0, 0, 0, 0  },
  {  0xf9,   1,   0x6,   0xb70,   5, 9,11, 8, 6, 4, 0, 0, 0, 0, 0, 0  },
  {  0xf9,   1,   0x6,   0xa2e,   9,11, 5, 1, 2, 3, 0, 0, 0, 0, 0, 0  },
  {  0xf9,   1,   0x6,   0x6cc,   2, 6, 7,10, 9, 3, 0, 0, 0, 0, 0, 0  },
  {  0xf9,   1,   0x6,   0x63a,   5, 4, 1, 3,10, 9, 0, 0, 0, 0, 0, 0  },
  {  0xf9,   1,   0x6,   0x15e,   4, 8, 6, 2, 3, 1, 0, 0, 0, 0, 0, 0  },
  {  0xf9,   1,   0x6,   0xf6,    1, 5, 4, 6, 7, 2, 0, 0, 0, 0, 0, 0  },
  {  0xe9,   2,   0x36,  0x1fcc, 12, 8,11, 9, 3,10, 7, 2, 6, 0, 0, 0  },
  {  0xe9,   2,   0x36,  0x1f3a,  5, 4, 1, 3,10, 9,11,12, 8, 0, 0, 0  },
  {  0xe9,   2,   0x36,  0x19f6,  6, 7, 2, 1, 5, 4, 8,11,12, 0, 0, 0  },
  {  0xe9,   2,   0x36,  0x1ff0, 11, 5, 9,10, 7,12, 8, 6, 4, 0, 0, 0  },
  {  0xe9,   2,   0x36,  0x1eae, 10, 7,12,11, 5, 9, 3, 1, 2, 0, 0, 0  },
  {  0xe9,   2,   0x36,  0x15de,  2, 3, 1, 4, 8, 6, 7,12,10, 0, 0, 0  },
  {  0xe9,   2,   0x36,  0xb7e,   4, 8, 6, 2, 3, 1, 5, 9,11, 0, 0, 0  },
  {  0xe9,   2,   0x36,  0x6fe,   1, 5, 4, 6, 7, 2, 3,10, 9, 0, 0, 0  },
  {  0xe9,   2,   0x36,  0x1fcc,  8,11,12, 7, 2, 6, 9, 3,10, 0, 0, 0  },
  {  0xe9,   2,   0x36,  0x1f3a,  4, 1, 5,11,12, 8, 3,10, 9, 0, 0, 0  },
  {  0xe9,   2,   0x36,  0x19f6,  7, 2, 6, 8,11,12, 1, 5, 4, 0, 0, 0  },
  {  0xe9,   2,   0x36,  0x1ff0,  5, 9,11, 8, 6, 4,10, 7,12, 0, 0, 0  },
  {  0xe9,   2,   0x36,  0x1eae,  7,12,10, 3, 1, 2,11, 5, 9, 0, 0, 0  },
  {  0xe9,   2,   0x36,  0x15de,  3, 1, 2, 7,12,10, 4, 8, 6, 0, 0, 0  },
  {  0xe9,   2,   0x36,  0xb7e,   8, 6, 4, 5, 9,11, 2, 3, 1, 0, 0, 0  },
  {  0xe9,   2,   0x36,  0x6fe,   5, 4, 1, 3,10, 9, 6, 7, 2, 0, 0, 0  },
  {  0xe9,   2,   0x36,  0x1fcc,  2, 6, 7,10, 9, 3,12, 8,11, 0, 0, 0  },
  {  0xe9,   2,   0x36,  0x1f3a, 12, 8,11, 9, 3,10, 5, 4, 1, 0, 0, 0  },
  {  0xe9,   2,   0x36,  0x19f6, 11,12, 8, 4, 1, 5, 6, 7, 2, 0, 0, 0  },
  {  0xe9,   2,   0x36,  0x1ff0,  6, 4, 8,12,10, 7,11, 5, 9, 0, 0, 0  },
  {  0xe9,   2,   0x36,  0x1eae,  1, 2, 3, 9,11, 5,10, 7,12, 0, 0, 0  },
  {  0xe9,   2,   0x36,  0x15de, 12,10, 7, 6, 4, 8, 2, 3, 1, 0, 0, 0  },
  {  0xe9,   2,   0x36,  0xb7e,   9,11, 5, 1, 2, 3, 4, 8, 6, 0, 0, 0  },
  {  0xe9,   2,   0x36,  0x6fe,  10, 9, 3, 2, 6, 7, 1, 5, 4, 0, 0, 0  }
};


static int sp_cases[] =
{
  000, 000, 000, 000, 000, 000, 000, 000, 000, 000, 
  000, 000, 000, 000, 000, 000, 000, 000, 000, 000, 
  000, 000, 000, 000, 000, 000, 000, 000, 000, 000, 
  000, 000, 000, 000, 000, 000, 000, 000, 000, 000, 
  000, 000, 000, 000, 000, 000, 000, 000, 000, 000, 
  000, 000, 000, 000, 000, 000, 000, 000, 000, 000, 
  000, 256, 257, 000, 000, 000, 000, 000, 000, 000, 
  000, 000, 000, 000, 000, 000, 000, 000, 000, 000, 
  000, 000, 000, 000, 000, 000, 000, 000, 000, 000, 
  000, 258, 000, 000, 259, 000, 000, 000, 000, 000, 
  000, 000, 000, 260, 000, 000, 000, 292, 000, 293, 
  261, 280, 000, 000, 000, 000, 000, 000, 262, 000, 
  000, 294, 263, 281, 264, 282, 000, 000, 000, 000, 
  000, 000, 000, 000, 000, 000, 000, 000, 000, 000, 
  000, 000, 000, 000, 000, 000, 000, 000, 000, 000, 
  000, 295, 000, 000, 000, 265, 000, 266, 296, 283, 
  000, 000, 000, 000, 000, 000, 000, 267, 000, 000, 
  000, 000, 000, 268, 000, 000, 000, 000, 000, 000, 
  000, 269, 297, 284, 000, 270, 000, 000, 271, 000, 
  285, 000, 000, 000, 000, 000, 000, 000, 000, 272, 
  000, 000, 000, 273, 000, 000, 000, 000, 000, 000, 
  000, 274, 000, 000, 298, 286, 000, 275, 276, 000, 
  000, 000, 287, 000, 000, 000, 000, 277, 000, 278, 
  279, 000, 000, 299, 000, 288, 000, 289, 000, 000, 
  000, 000, 000, 000, 000, 000, 290, 000, 000, 291, 
  000, 000, 000, 000, 000, 000
};

static int case_E9[] =
{
  Xn, Yp, Zp, incXn, incYn, incZn,
  Xp, Yn, Zp, incYn, incZn, incXn,
  Xp, Yp, Zn, incXn, incYn, incZn,
  Xp, Yp, Zp, incYn, incXn, incZn,
  Xn, Yn, Zp, incYn, incXn, incZn,
  Xn, Yp, Zp, incYn, incXn, incZn,
  Xp, Yn, Zn, incYn, incXn, incZn,
  Xn, Yn, Zn, incXn, incYn, incZn
};


static int NTAB[] = 
{   0,1,2,       1,2,0,       2,0,1,
    0,1,3,2,     1,2,0,3,     2,3,1,0,     3,0,2,1,
    0,1,4,2,3,   1,2,0,3,4,   2,3,1,4,0,   3,4,2,0,1,   4,0,3,1,2,
    0,1,5,2,4,3, 1,2,0,3,5,4, 2,3,1,4,0,5, 3,4,2,5,1,0, 4,5,3,0,2,1,
    5,0,4,1,3,2
};

static int ITAB[] =
{   0,2,1,       1,0,2,       2,1,0,
    0,3,1,2,     1,0,2,3,     2,1,3,0,     3,2,0,1,
    0,4,1,3,2,   1,0,2,4,3,   2,1,3,0,4,   3,2,4,1,0,   4,3,0,2,1,
    0,5,1,4,2,3, 1,0,2,5,3,4, 2,1,3,0,4,5, 3,2,4,1,5,0, 4,3,5,2,0,1,
    5,4,0,3,1,2
};

static int STAB[] =  { 0, 9, 25, 50 };


//<           constructors           >

  public  Contour3D( double[] ptGRID, int xdim, int ydim, int zdim, int LowLev,
                     double isovalue, double ARX, double ARY, double ARZ, int num_verts,
                     double[] VX, double[] VY, double[] VZ, 
                     double[] NX, double[] NY, double[] NZ, int NPTS, int[] VPTS ) 
          throws VisADException {

       this.ptGRID    = ptGRID;
       this.VX        = VX;
       this.VY        = VY;
       this.VZ        = VZ;
       this.xdim      = xdim;
       this.ydim      = ydim;
       this.zdim      = zdim;
       this.isovalue  = isovalue;
       this.LowLev    = LowLev;
       this.ARX       = ARX;
       this.ARY       = ARY;
       this.ARZ       = ARZ;
       this.num_verts = num_verts;
       this.NX        = NX;
       this.NY        = NY;
       this.NZ        = NZ;
       this.NPTS      = NPTS;
       this.VPTS      = VPTS;
/* WLH 25 Oct 97
       this.PEX       = false;
*/

       //  check (array size) vs. ( dimensions )

           if ( ptGRID.length != xdim*ydim*zdim ) { 
               throw new DisplayException("Contour3D: dimensions don't match ");
           }

       //  check domain dimensin, aspect ratios

           if ( xdim < 2 || ydim < 2 || zdim < 2 ||
                Math.abs(ARX) < EPS_0 || Math.abs(ARY) < EPS_0 ||
                Math.abs(ARZ) < EPS_0 ) {
                throw new DisplayException("Contour3D: domain size or " +
                                           "aspect ratio problem ");
           }

  }


//<            instance methods              >

  
  public void main_isosurf() throws VisADException {

      int      i, NVT, cnt;
      double   arX, arY, arZ;
      int      size_stripe;
      int      xdim_x_ydim, xdim_x_ydim_x_zdim;
      int      num_cubes, nvertex, npolygons;
      int      NVERTICE;
      int      ix, iy, ii;


        xdim_x_ydim = xdim * ydim;
        xdim_x_ydim_x_zdim = xdim_x_ydim * zdim;
        num_cubes = (xdim-1) * (ydim-1) * (zdim-1);



        int[]  ptFLAG = new int[ num_cubes ];
        int[]  ptAUX  = new int[ xdim_x_ydim_x_zdim ];
        int[]  pcube  = new int[ num_cubes+1 ];
 
      
        npolygons = flags( isovalue, ptFLAG, ptAUX, pcube,
                           ptGRID, xdim, ydim, zdim );

        System.out.println("npolygons= "+npolygons);

        NVERTICE = num_verts;

        ix = 9 * (npolygons*2 + 50);
        iy = 7 * npolygons;
        ii = ix + iy;

/* WLH 25 Oct 97 - BUG
        int[] Pol_f_Vert = new int[ii];
*/
        int[] Pol_f_Vert = new int[ix];
        int[] Vert_f_Pol = new int[iy];

        nvertex = isosurf( isovalue, ptFLAG, NVERTICE, npolygons, ptGRID,
                           xdim, ydim, zdim, VX, VY, VZ, Pol_f_Vert, Vert_f_Pol );

        System.out.println("nvertex= "+nvertex);

/* WLH 25 Oct 97 - BUG
        double[] NxA = new double[6*npolygons];
        double[] NxB = new double[5*npolygons];
        double[] NyA = new double[4*npolygons];
        double[] NyB = new double[3*npolygons];
        double[] NzA = new double[2*npolygons];
        double[] NzB = new double[1*npolygons];

        double[] Pnx = new double[3*npolygons];
        double[] Pny = new double[2*npolygons];
        double[] Pnz = new double[1*npolygons];
*/
        double[] NxA = new double[npolygons];
        double[] NxB = new double[npolygons];
        double[] NyA = new double[npolygons];
        double[] NyB = new double[npolygons];
        double[] NzA = new double[npolygons];
        double[] NzB = new double[npolygons];

        double[] Pnx = new double[npolygons];
        double[] Pny = new double[npolygons];
        double[] Pnz = new double[npolygons];


/* WLH 25 Oct 97 - move to normals
        for ( cnt = 0; cnt < nvertex; cnt++ ) {
            NX[cnt] = 0;
            NY[cnt] = 0;
            NZ[cnt] = 0;
        }
*/

        normals( VX, VY, VZ, NX, NY, NZ, nvertex, npolygons, Pnx, Pny, Pnz,
                 NxA, NxB, NyA, NyB, NzA, NzB, ARX, ARY, ARZ,
                 Pol_f_Vert, Vert_f_Pol);


                  /* ----- Map Vertex in Output Vectors */
        /*       Considere the Aspect Ratio in the Vertices Values */
        /*       Observe that AR is considered in Normals Calculat.*/
        /*
        if ( (((1.0-arX) >= 0.) ? (1.0-arX):-(1.0-arX)) > EPS_0)
            for (i=0; i<nvertex; i++) VX[i] *= arX;
        if ( (((1.0-arY) >= 0.) ? (1.0-arY):-(1.0-arY)) > EPS_0)
            for (i=0; i<nvertex; i++) VY[i] *= arY;
        if ( (((1.0-arZ) >= 0.) ? (1.0-arZ):-(1.0-arZ)) > EPS_0)
            for (i=0; i<nvertex; i++) VZ[i] *= arZ;
        */

        if (ARX != 1.0)
            for (i=0; i<nvertex; i++) VX[i] *= ARX;  /* Vectorized */
        if (ARY != 1.0)
            for (i=0; i<nvertex; i++) VY[i] *= ARY;  /* Vectorized */
        if (LowLev != 0.0)
            for (i=0; i<nvertex; i++) VZ[i] += LowLev;  /* Vectorized */
        if (ARZ != 1.0)
            for (i=0; i<nvertex; i++) VZ[i] *= ARZ;  /* Vectorized */


        /* ----- Find PolyTriangle Stripe */

        int[] Tri_Stripe = new int[6*npolygons];
        int[] vet_pol = new int[npolygons];

        size_stripe = poly_triangle_stripe( vet_pol, Tri_Stripe, nvertex,
                                            npolygons, Pol_f_Vert, Vert_f_Pol );

        System.out.println("size_stripe= "+size_stripe);

        NVT = ((NPTS < size_stripe) ? NPTS:size_stripe);
//      memcpy( VPTS, Tri_Stripe, NVT*sizeof(int) );
        System.arraycopy(Tri_Stripe, 0, VPTS, 0, NVT);

        for(ii=0;ii<NVT;ii++)
          System.out.println(+VPTS[ii]);

        /* ----- Update Output values */

//      *IVERT = nvertex;
//      *IPTS  = size_stripe;
//      *IPOLY = npolygons;
//      *ITRI  = 0;


  }


  public static int flags( double isovalue, int[] ptFLAG, int[] ptAUX, int[] pcube,
                           double[] ptGRID, int xdim, int ydim, int zdim ) {
      int ii, jj, ix, iy, iz, cb, SF, bcase;
      int num_cubes, num_cubes_xy, num_cubes_y;
      int xdim_x_ydim = xdim*ydim;
      int xdim_x_ydim_x_zdim = xdim_x_ydim*zdim;
      int npolygons;

      num_cubes_y  = ydim-1;
      num_cubes_xy = (xdim-1) * num_cubes_y;
      num_cubes = (zdim-1) * num_cubes_xy;


    /*
    *************
    Big Attention
    *************
    pcube must have the dimension "num_cubes+1" because in terms of
    eficiency the best loop to calculate "pcube" will use more one
    component to pcube.
    */

    /* Calculate the Flag Value of each Cube */
    /* In order to simplify the Flags calculations, "pcube" will
       be used to store the number of the first vertex of each cube */
    ii = 0;     pcube[0] = 0;  cb = 0;
    for (iz=0; iz<(zdim-1); iz++)
    {   for (ix=0; ix<(xdim-1); ix++)
        {   cb = pcube[ii];
            for (iy=1; iy<(ydim-1); iy++) /* Vectorized */
                pcube[ii+iy] = cb+iy;
            ii += ydim-1;
            pcube[ii] = pcube[ii-1]+2;
        }
        pcube[ii] += ydim;
    }

   /* Vectorized */
    for (ii = 0; ii < xdim_x_ydim_x_zdim; ii++) {
/* WLH 24 Oct 97
        if      (ptGRID[ii] >= INVALID_VALUE) ptAUX[ii] = 0x1001;
        if      (Double.isNaN(ptGRID[ii]) ptAUX[ii] = 0x1001;
*/
        // test for missing
        if      (ptGRID[ii] != ptGRID[ii]) ptAUX[ii] = 0x1001;
        else if (ptGRID[ii] >= isovalue)      ptAUX[ii] = 1;
        else                                  ptAUX[ii] = 0;
    }

   /* Vectorized */
    for (ii = 0; ii < num_cubes; ii++) {
        ptFLAG[ii] = ((ptAUX[ pcube[ii] ]      ) |
                      (ptAUX[ pcube[ii] + ydim ]  << 1) |
                      (ptAUX[ pcube[ii] + 1 ]  << 2) |
                      (ptAUX[ pcube[ii] + ydim + 1 ]  << 3) |
                      (ptAUX[ pcube[ii] + xdim_x_ydim ]  << 4) |
                      (ptAUX[ pcube[ii] + ydim + xdim_x_ydim ]  << 5) |
                      (ptAUX[ pcube[ii] + 1 + xdim_x_ydim ]  << 6) |
                      (ptAUX[ pcube[ii] + 1 + ydim + xdim_x_ydim ]  << 7));
     }
    /* After this Point it is not more used pcube */

    /* Analyse Special Cases in FLAG */
    ii = npolygons = 0;
    while ( TRUE )
    {  
        for (; ii < num_cubes; ii++) {
            if ( ((ptFLAG[ii] != 0) && (ptFLAG[ii] != 0xFF)) &&
                 ptFLAG[ii] < MAX_FLAG_NUM) break;
        }

        if ( ii == num_cubes ) break;

        bcase = pol_edges[ptFLAG[ii]][0];
        if (bcase == 0xE6 || bcase == 0xF9) {
            iz = ii/num_cubes_xy;
            ix = (int)((ii - (iz*num_cubes_xy))/num_cubes_y);
            iy = ii - (iz*num_cubes_xy) - (ix*num_cubes_y);

        /* == Z+ == */
            if      ((ptFLAG[ii] & 0xF0) == 0x90 ||
                     (ptFLAG[ii] & 0xF0) == 0x60) {
                   cb = (iz < (zdim - 1)) ? ii + num_cubes_xy : -1 ;
              }
        /* == Z- == */
            else if ((ptFLAG[ii] & 0x0F) == 0x09 ||
                     (ptFLAG[ii] & 0x0F) == 0x06) {
                   cb = (iz > 0) ? ii - num_cubes_xy : -1 ;
              }
        /* == Y+ == */
            else if ((ptFLAG[ii] & 0xCC) == 0x84 ||
                     (ptFLAG[ii] & 0xCC) == 0x48) {
                   cb = (iy < (ydim - 1)) ? ii + 1 : -1 ;
              }
        /* == Y- == */
            else if ((ptFLAG[ii] & 0x33) == 0x21 ||
                     (ptFLAG[ii] & 0x33) == 0x12) {
                   cb = (iy > 0) ? ii - 1 : -1 ;
              }
        /* == X+ == */
            else if ((ptFLAG[ii] & 0xAA) == 0x82 ||
                     (ptFLAG[ii] & 0xAA) == 0x28) {
                   cb = (ix < (xdim - 1)) ? ii + num_cubes_y : -1 ;
              }
        /* == X- == */
            else if ((ptFLAG[ii] & 0x55) == 0x41 ||
                     (ptFLAG[ii] & 0x55) == 0x14) {
                   cb = (ix > 0) ? ii - num_cubes_y : -1 ;
              }
        /* == Map Special Case == */
            if  ((cb > -1 && cb < num_cubes) && ptFLAG[cb]<316)  /*changed by BEP on 7-20-92*/
            {   bcase = pol_edges[ptFLAG[cb]][0];
                if (bcase == 0x06 || bcase == 0x16 ||
                    bcase == 0x19 || bcase == 0x1E ||
                    bcase == 0x3C || bcase == 0x69)
                    ptFLAG[ii] = sp_cases[ptFLAG[ii]];
            }
        }
        else if (bcase == 0xE9) {
            iz = ii/num_cubes_xy;
            ix = (int)((ii - (iz*num_cubes_xy))/num_cubes_y);
            iy = ii - (iz*num_cubes_xy) - (ix*num_cubes_y);

               SF = 0;
            if      (ptFLAG[ii] == 0x6B) SF = SF_6B;
            else if (ptFLAG[ii] == 0x6D) SF = SF_6D;
            else if (ptFLAG[ii] == 0x79) SF = SF_79;
            else if (ptFLAG[ii] == 0x97) SF = SF_97;
            else if (ptFLAG[ii] == 0x9E) SF = SF_9E;
            else if (ptFLAG[ii] == 0xB6) SF = SF_B6;
            else if (ptFLAG[ii] == 0xD6) SF = SF_D6;
            else if (ptFLAG[ii] == 0xE9) SF = SF_E9;
            for (jj=0; jj<3; jj++) {
                if      (case_E9[jj+SF] == Zp) {
                     cb = (iz < (zdim - 1)) ? ii + num_cubes_xy : -1 ;
                  }
                else if (case_E9[jj+SF] == Zn) {
                     cb = (iz > 0) ? ii - num_cubes_xy : -1 ;
                  }
                else if (case_E9[jj+SF] == Yp) {
                     cb = (iy < (ydim - 1)) ? ii + 1 : -1 ;
                  }
                else if (case_E9[jj+SF] == Yn) {
                     cb = (iy > 0) ? ii - 1 : -1 ;
                  }
                else if (case_E9[jj+SF] == Xp) {
                     cb = (ix < (xdim - 1)) ? ii + num_cubes_y : -1 ;
                  }
                else if (case_E9[jj+SF] == Xn) {
                     cb = (ix > 0) ? ii - num_cubes_y : -1 ;
                  }
       /* changed:
                if  ((cb > -1 && cb < num_cubes))
          to: */
                if  ((cb > -1 && cb < num_cubes) && ptFLAG[cb]<316)
       /* changed by BEP on 7-20-92*/
                {   bcase = pol_edges[ptFLAG[cb]][0];
                    if (bcase == 0x06 || bcase == 0x16 ||
                        bcase == 0x19 || bcase == 0x1E ||
                        bcase == 0x3C || bcase == 0x69)
                    {   ptFLAG[ii] = sp_cases[ptFLAG[ii]] +
                                     case_E9[jj+SF+3];
                        break;
                    }
                }
            }
        }

        /* Calculate the Number of Generated Triangles and Polygons */
        npolygons  += pol_edges[ptFLAG[ii]][1];
        ii++;
    }

     /*  npolygons2 = 2*npolygons; */

    return npolygons;
  }


  public static int isosurf( double isovalue, int[] ptFLAG, int NVERTICE,
                             int npolygons, double[] ptGRID, int xdim, int ydim,
                             int zdim, double[] VX, double[] VY, double[] VZ,
                             int[] Pol_f_Vert, int[] Vert_f_Pol ) 
         throws VisADException {

   int  ix, iy, iz, caseA, above, bellow, front, rear, mm, nn;
   int  ii, jj, kk, ncube, cpl, pvp, pa, ve;
   int[] calc_edge = new int[13];
   int  xx, yy, zz;
   double    cp;
   double  vnode0 = 0;
   double  vnode1 = 0;
   double  vnode2 = 0;
   double  vnode3 = 0;
   double  vnode4 = 0;
   double  vnode5 = 0;
   double  vnode6 = 0;
   double  vnode7 = 0;
   int  pt = 0;
   int  n_pol;
   int  aa;
   int  bb;
   int  temp;
   double  nodeDiff;
   int xdim_x_ydim = xdim*ydim;
   int nvet;

    bellow = rear = 0;  above = front = 1;

    /* Initialize the Auxiliar Arrays of Pointers */
/* WLH 25 Oct 97
    ix = 9 * (npolygons*2 + 50);
    iy = 7 * npolygons;
    ii = ix + iy;
*/
    for (jj=0; jj<Pol_f_Vert.length; jj++) {
      Pol_f_Vert[jj] = BIG_NEG;
    }
    for (jj=8; jj<Pol_f_Vert.length; jj+=9) {
      Pol_f_Vert[jj] = 0;
    }
    for (jj=0; jj<Vert_f_Pol.length; jj++) {
      Vert_f_Pol[jj] = BIG_NEG;
    }
    for (jj=6; jj<Vert_f_Pol.length; jj+=7) {
      Vert_f_Pol[jj] = 0;
    }

    /* Allocate the auxiliar edge vectors
    size ixPlane = (xdim - 1) * ydim = xdim_x_ydim - ydim
    size iyPlane = (ydim - 1) * xdim = xdim_x_ydim - xdim
    size izPlane = xdim
    */

    xx = xdim_x_ydim - ydim;
    yy = xdim_x_ydim - xdim;
    zz = ydim;
    ii = 2 * (xx + yy + zz);

    int[] P_array = new int[ii];

    /* Calculate the Vertex of the Polygons which edges were
       calculated above */
    nvet = ncube = cpl = pvp = 0;


        for ( iz = 0; iz < zdim - 1; iz++ ) {
            for ( ix = 0; ix < xdim - 1; ix++ ) {

                for ( iy = 0; iy < ydim - 1; iy++ ) {
                    if ( (ptFLAG[ncube] != 0 & ptFLAG[ncube] != 0xFF) ) {
                        if (nvet + 12 > NVERTICE) {
                            throw new DisplayException(
                                           "isosurf: nvet + 12 > NVERTICE");
                        }
                        if ( (ptFLAG[ncube] < MAX_FLAG_NUM) ) {
                        /*  fill_Vert_f_Pol(ncube); */

                                  kk  = pol_edges[ptFLAG[ncube]][2];
                                  aa = ptFLAG[ncube];
                                  bb = 4;
                                  pa  = pvp;
                                  n_pol = pol_edges[ptFLAG[ncube]][1];
                                  for (ii=0; ii < n_pol; ii++) {
                                      Vert_f_Pol[pa+6] = ve = kk&MASK;
                                      ve+=pa;
                                      for (jj=pa; jj<ve && jj<pa+6; jj++) {

                                            Vert_f_Pol[jj] = pol_edges[aa][bb];
                                            bb++;
                                            if (bb >= 16) {
                                                aa++;
                                                bb -= 16;
                                            }
                                      }
                                           kk >>= 4;    pa += 7;
                                  }
                        /* end  fill_Vert_f_Pol(ncube); */
                        /* */

         /* find_vertex(); */

           vnode0 = ptGRID[pt];
           vnode1 = ptGRID[pt + ydim];
           vnode2 = ptGRID[pt + 1];
           vnode3 = ptGRID[pt + ydim + 1];
           vnode4 = ptGRID[pt + xdim_x_ydim];
           vnode5 = ptGRID[pt + ydim + xdim_x_ydim];
           vnode6 = ptGRID[pt + 1 + xdim_x_ydim];
           vnode7 = ptGRID[pt + 1 + ydim + xdim_x_ydim];



   if ( ((pol_edges[ptFLAG[ncube]][3] & 0x0002) != 0) )    /* cube vertex 0-1 */
   {   if ( (iz != 0) || (iy != 0) )  calc_edge[1] = P_array[ bellow*xx + ix*ydim + iy ];
         else {
             nodeDiff = vnode1 - vnode0;
             cp = ( ( isovalue - vnode0 ) / nodeDiff ) + ix;

             calc_edge[1] = nvet;
             VX[nvet] = cp;
             VY[nvet] = iy;
             VZ[nvet] = iz;
             nvet++;
         }
     }
     if ( ((pol_edges[ptFLAG[ncube]][3] & 0x0004) != 0) )         /* cube vertex 0-2 */
     {   if ( (iz != 0) || (ix != 0) )  calc_edge[2] = P_array[ 2*xx + bellow*yy + iy*xdim + ix ];
         else {

             nodeDiff = vnode2 - vnode0;
             cp = ( ( isovalue - vnode0 ) / nodeDiff ) + iy;

             calc_edge[2] = nvet;
             VX[nvet] = ix;
             VY[nvet] = cp;
             VZ[nvet] = iz;
             nvet++;
         }
     }
     if ( ((pol_edges[ptFLAG[ncube]][3] & 0x0008) != 0) )                /* cube vertex 0-4 */
     {   if ( (ix != 0) || (iy != 0) )  calc_edge[3] = P_array[ 2*xx + 2*yy + rear*zz + iy ];
         else {
             nodeDiff = vnode4 - vnode0;
             cp = ( ( isovalue - vnode0 ) / nodeDiff ) + iz;

             calc_edge[3] = nvet;
             VX[nvet] = ix;
             VY[nvet] = iy;
             VZ[nvet] = cp;
             nvet++;
         }
     }
     if ( ((pol_edges[ptFLAG[ncube]][3] & 0x0010) != 0) )                /* cube vertex 1-3 */
     {   if ( (iz != 0) )     calc_edge[4] =  P_array[ 2*xx + bellow*yy + iy*xdim + (ix+1) ];
         else {
             nodeDiff = vnode3 - vnode1;
             cp = ( ( isovalue - vnode1 ) / nodeDiff ) + iy;

             calc_edge[4] = nvet;
             VX[nvet] = ix+1;
             VY[nvet] = cp;
             VZ[nvet] = iz;
             P_array[ 2*xx + bellow*yy + iy*xdim + (ix+1) ] = nvet;
             nvet++;
         }
     }
     if ( ((pol_edges[ptFLAG[ncube]][3] & 0x0020) != 0) )                /* cube vertex 1-5 */
     {   if ( (iy != 0) )        calc_edge[5] = P_array[ 2*xx + 2*yy + front*zz + iy ];
         else {
             nodeDiff = vnode5 - vnode1;
             cp = ( ( isovalue - vnode1 ) / nodeDiff ) + iz;

             calc_edge[5] = nvet;
             VX[nvet] = ix+1;
             VY[nvet] = iy;
             VZ[nvet] = cp;
             P_array[ 2*xx + 2*yy + front*zz + iy ] = nvet;
             nvet++;
         }
     }
     if ( ((pol_edges[ptFLAG[ncube]][3] & 0x0040) != 0) )         /* cube vertex 2-3 */
     {   if ( (iz != 0) )   calc_edge[6] = P_array[ bellow*xx + ix*ydim + (iy+1) ];
         else {
             nodeDiff = vnode3 - vnode2;
             cp = ( ( isovalue - vnode2 ) / nodeDiff ) + ix;

             calc_edge[6] = nvet;
             VX[nvet] = cp;
             VY[nvet] = iy+1;
             VZ[nvet] = iz;
             P_array[ bellow*xx + ix*ydim + (iy+1) ] = nvet;
             nvet++;
         }
     }
     if ( ((pol_edges[ptFLAG[ncube]][3] & 0x0080) != 0) )                /* cube vertex 2-6 */
     {   if ( (ix != 0) )        calc_edge[7] = P_array[ 2*xx + 2*yy + rear*zz + (iy+1) ];
         else {
             nodeDiff = vnode6 - vnode2;
             cp = ( ( isovalue - vnode2 ) / nodeDiff ) + iz;

             calc_edge[7] = nvet;
             VX[nvet] = ix;
             VY[nvet] = iy+1;
             VZ[nvet] = cp;
             P_array[ 2*xx + 2*yy + rear*zz + (iy+1) ] = nvet;
             nvet++;
         }
     }
     if ( ((pol_edges[ptFLAG[ncube]][3] & 0x0100) != 0) )         /* cube vertex 3-7 */
     {
         nodeDiff = vnode7 - vnode3;
         cp = ( ( isovalue - vnode3 ) / nodeDiff ) + iz;

         calc_edge[8] = nvet;
         VX[nvet] = ix+1;
         VY[nvet] = iy+1;
         VZ[nvet] = cp;
         P_array[ 2*xx + 2*yy + front*zz + (iy+1) ] = nvet;
         nvet++;
     }
     if ( ((pol_edges[ptFLAG[ncube]][3] & 0x0200) != 0) )         /* cube vertex 4-5 */
     {   if ( (iy != 0) )        calc_edge[9] = P_array[ above*xx + ix*ydim + iy ];
         else {
             nodeDiff = vnode5 - vnode4;
             cp = ( ( isovalue - vnode4 ) / nodeDiff ) + ix;

             calc_edge[9] = nvet;
             VX[nvet] = cp;
             VY[nvet] = iy;
             VZ[nvet] = iz+1;
             P_array[ above*xx + ix*ydim + iy ] = nvet;
             nvet++;
         }
     }
     if ( ((pol_edges[ptFLAG[ncube]][3] & 0x0400) != 0) )         /* cube vertex 4-6 */
     {   if ( (ix != 0) )       calc_edge[10] = P_array[ 2*xx + above*yy + iy*xdim + ix ];
         else {
             nodeDiff = vnode6 - vnode4;
             cp = ( ( isovalue - vnode4 ) / nodeDiff ) + iy;

             calc_edge[10] = nvet;
             VX[nvet] = ix;
             VY[nvet] = cp;
             VZ[nvet] = iz+1;
             P_array[ 2*xx + above*yy + iy*xdim + ix ] = nvet;
             nvet++;
         }
     }
    if ( ((pol_edges[ptFLAG[ncube]][3] & 0x0800) != 0) )          /* cube vertex 5-7 */
     {
         nodeDiff = vnode7 - vnode5;
         cp = ( ( isovalue - vnode5 ) / nodeDiff ) + iy;

         calc_edge[11] = nvet;
         VX[nvet] = ix+1;
         VY[nvet] = cp;
         VZ[nvet] = iz+1;
         P_array[ 2*xx + above*yy + iy*xdim + (ix+1) ] = nvet;
         nvet++;
     }
     if ( ((pol_edges[ptFLAG[ncube]][3] & 0x1000) != 0) )         /* cube vertex 6-7 */
     {
         nodeDiff = vnode7 - vnode6;
         cp = ( ( isovalue - vnode6 ) / nodeDiff ) + ix;

         calc_edge[12] = nvet;
         VX[nvet] = cp;
         VY[nvet] = iy+1;
         VZ[nvet] = iz+1;
         P_array[ above*xx + ix*ydim + (iy+1) ] = nvet;
         nvet++;
     }

         /* end  find_vertex(); */
                         /* update_data_structure(ncube); */
                             kk = pol_edges[ptFLAG[ncube]][2];
                             nn = pol_edges[ptFLAG[ncube]][1];
                             for (ii=0; ii<nn; ii++) {
                                  mm = pvp+(kk&MASK);
                                  for (jj=pvp; jj<mm; jj++) {
                                      Vert_f_Pol [jj] = ve = calc_edge[Vert_f_Pol [jj]];
                            //        Pol_f_Vert[ve*9 + (Pol_f_Vert[ve*9 + 8])++]  = cpl;
                                      temp = Pol_f_Vert[ve*9 + 8];
                                      Pol_f_Vert[ve*9 + temp] = cpl;
                                      Pol_f_Vert[ve*9 + 8] = temp + 1;
                                  }
                                  kk >>= 4;    pvp += 7;    cpl++;
                             }
                         /* end  update_data_structure(ncube); */
                        }
                        else { // !(ptFLAG[ncube] < MAX_FLAG_NUM)
       /* find_vertex_invalid_cube(ncube); */

    ptFLAG[ncube] &= 0x1FF;
    if ( (ptFLAG[ncube] != 0 & ptFLAG[ncube] != 0xFF) )
    { if ( ((pol_edges[ptFLAG[ncube]][3] & 0x0010) != 0) )         /* cube vertex 1-3 */
/* WLH 24 Oct 97
      {   if (!(iz != 0 ) && vnode3 < INV_VAL && vnode1 < INV_VAL)
      {   if (!(iz != 0 ) && !Double.isNaN(vnode3) && !Double.isNaN(vnode1))
*/
      // test for not missing
      {   if (!(iz != 0 ) && vnode3 == vnode3 && vnode1 == vnode1)
        {
              nodeDiff = vnode3 - vnode1;
              cp = ( ( isovalue - vnode1 ) / nodeDiff ) + iy;

              VX[nvet] = ix+1;
              VY[nvet] = cp;
              VZ[nvet] = iz;
              P_array[ 2*xx + bellow*yy + iy*xdim + (ix+1) ] = nvet;
              nvet++;
        }
      }
      if ( ((pol_edges[ptFLAG[ncube]][3] & 0x0020) != 0) )                /* cube vertex 1-5 */
/* WLH 24 Oct 97
      {   if (!(iy != 0) && vnode5 < INV_VAL && vnode1 < INV_VAL)
      {   if (!(iy != 0) && !Double.isNaN(vnode5) && !Double.isNaN(vnode1))
*/
      // test for not missing
      {   if (!(iy != 0) && vnode5 == vnode5 && vnode1 == vnode1)
        {
              nodeDiff = vnode5 - vnode1;
              cp = ( ( isovalue - vnode1 ) / nodeDiff ) + iz;

              VX[nvet] = ix+1;
              VY[nvet] = iy;
              VZ[nvet] = cp;
              P_array[ 2*xx + 2*yy + front*zz + iy ] = nvet;
              nvet++;
        }
      }
      if ( ((pol_edges[ptFLAG[ncube]][3] & 0x0040) != 0) )                /* cube vertex 2-3 */
/* WLH 24 Oct 97
      {   if (!(iz != 0) && vnode3 < INV_VAL && vnode2 < INV_VAL)
      {   if (!(iz != 0) && !Double.isNaN(vnode3) && !Double.isNaN(vnode2))
*/
      // test for not missing
      {   if (!(iz != 0) && vnode3 == vnode3 && vnode2 == vnode2)
        {
              nodeDiff = vnode3 - vnode2;
              cp = ( ( isovalue - vnode2 ) / nodeDiff ) + ix;

              VX[nvet] = cp;
              VY[nvet] = iy+1;
              VZ[nvet] = iz;
              P_array[ bellow*xx + ix*ydim + (iy+1) ] = nvet;
              nvet++;
        }
      }
      if ( ((pol_edges[ptFLAG[ncube]][3] & 0x0080) != 0) )                /* cube vertex 2-6 */
/* WLH 24 Oct 97
      {   if (!(ix != 0) && vnode6 < INV_VAL && vnode2 < INV_VAL)
      {   if (!(ix != 0) && !Double.isNaN(vnode6) && !Double.isNaN(vnode2))
*/
      // test for not missing
      {   if (!(ix != 0) && vnode6 == vnode6 && vnode2 == vnode2)
        {
              nodeDiff = vnode6 - vnode2;
              cp = ( ( isovalue - vnode2 ) / nodeDiff ) + iz;

              VX[nvet] = ix;
              VY[nvet] = iy+1;
              VZ[nvet] = cp;
              P_array[ 2*xx + 2*yy + rear*zz + (iy+1) ] = nvet;
              nvet++;
        }
      }
      if ( ((pol_edges[ptFLAG[ncube]][3] & 0x0100) != 0) )                /* cube vertex 3-7 */
/* WLH 24 Oct 97
      {   if (vnode7 < INV_VAL && vnode3 < INV_VAL)
      {   if (!Double.isNaN(vnode7) && !Double.isNaN(vnode3))
*/
      // test for not missing
      {   if (vnode7 == vnode7 && vnode3 == vnode3)
          {
              nodeDiff = vnode7 - vnode3;
              cp = ( ( isovalue - vnode3 ) / nodeDiff ) + iz;

              VX[nvet] = ix+1;
              VY[nvet] = iy+1;
              VZ[nvet] = cp;
              P_array[ 2*xx + 2*yy + front*zz + (iy+1) ] = nvet;
              nvet++;
        }
      }
      if ( ((pol_edges[ptFLAG[ncube]][3] & 0x0200) != 0) )         /* cube vertex 4-5 */
/* WLH 24 Oct 97
      {   if (!(iy != 0) && vnode5 < INV_VAL && vnode4 < INV_VAL)
      {   if (!(iy != 0) && !Double.isNaN(vnode5) && !Double.isNaN(vnode4))
*/
      // test for not missing
      {   if (!(iy != 0) && vnode5 == vnode5 && vnode4 == vnode4)
        {
              nodeDiff = vnode5 - vnode4;
              cp = ( ( isovalue - vnode4 ) / nodeDiff ) + ix;

              VX[nvet] = cp;
              VY[nvet] = iy;
              VZ[nvet] = iz+1;
              P_array[ above*xx + ix*ydim + iy ] = nvet;
              nvet++;
          }
      }
      if ( ((pol_edges[ptFLAG[ncube]][3] & 0x0400) != 0) )                /* cube vertex 4-6 */
/* WLH 24 Oct 97
      {   if (!(ix != 0) && vnode6 < INV_VAL && vnode4 < INV_VAL)
      {   if (!(ix != 0) && !Double.isNaN(vnode6) && !Double.isNaN(vnode4))
*/
      // test for not missing
      {   if (!(ix != 0) && vnode6 == vnode6 && vnode4 == vnode4)
          {
              nodeDiff = vnode6 - vnode4;
              cp = ( ( isovalue - vnode4 ) / nodeDiff ) + iy;

              VX[nvet] = ix;
              VY[nvet] = cp;
              VZ[nvet] = iz+1;
              P_array[ 2*xx + above*yy + iy*xdim + ix ] = nvet;
              nvet++;
          }
      }
      if ( ((pol_edges[ptFLAG[ncube]][3] & 0x0800) != 0) )                /* cube vertex 5-7 */
/* WLH 24 Oct 97
      {   if (vnode7 < INV_VAL && vnode5 < INV_VAL)
      {   if (!Double.isNaN(vnode7) && !Double.isNaN(vnode5))
*/
      // test for not missing
      {   if (vnode7 == vnode7 && vnode5 == vnode5)
        {
              nodeDiff = vnode7 - vnode5;
              cp = ( ( isovalue - vnode5 ) / nodeDiff ) + iy;

              VX[nvet] = ix+1;
              VY[nvet] = cp;
              VZ[nvet] = iz+1;
              P_array[ 2*xx + above*yy + iy*xdim + (ix+1) ] = nvet;
              nvet++;
        }
      }
      if ( ((pol_edges[ptFLAG[ncube]][3] & 0x1000) != 0) )                /* cube vertex 6-7 */
/* WLH 24 Oct 97
      {   if (vnode7 < INV_VAL && vnode6 < INV_VAL)
      {   if (!Double.isNaN(vnode7) && !Double.isNaN(vnode6))
*/
      // test for not missing
      {   if (vnode7 == vnode7 && vnode6 == vnode6)
        {
              nodeDiff = vnode7 - vnode6;
              cp = ( ( isovalue - vnode6 ) / nodeDiff ) + ix;

              VX[nvet] = cp;
              VY[nvet] = iy+1;
              VZ[nvet] = iz+1;
              P_array[ above*xx + ix*ydim + (iy+1) ] = nvet;
              nvet++;
        }
      }
     }
        /* end  find_vertex_invalid_cube(ncube); */
 
                        }
                    }
                    ncube++; pt++;
                }
             /* swap_planes(Z,rear,front); */
                caseA = rear;
                rear = front;
                front = caseA;
                pt++;
             /* end  swap_planes(Z,rear,front); */
            }
           /*  swap_planes(XY,bellow,above); */
               caseA = bellow;
               bellow = above;
               above = caseA;
            pt += ydim;
           /* end  swap_planes(XY,bellow,above); */
        }

    return nvet;
  }

  public static void normals( double[] VX, double[] VY, double[] VZ,
                     double[] NX, double[] NY, double[] NZ, int nvertex,
                     int npolygons, double[] Pnx, double[] Pny, double[] Pnz,
                     double[] NxA, double[] NxB, double[] NyA, double[] NyB,
                     double[] NzA, double[] NzB, double arX, double arY, double arZ, 
                     int[] Pol_f_Vert, int[] Vert_f_Pol)
         throws VisADException {

   int   i, k,  n;
   int   i1, i2, ix, iy, iz, ixb, iyb, izb;
   int   max_vert_per_pol, swap_flag;
   double x, y, z, a, minimum_area, len;
/* WLH 25 Oct 97
   int   xdim_x_ydim = xdim * ydim;
*/

   int iv[] = new int[3];

/* WLH 25 Oct 97
   ixb = xdim - 1;
   iyb = ydim - 1;
   izb = zdim - 1;
*/

/* WLH 25 Oct 97 */
   for ( i = 0; i < nvertex; i++ ) {
      NX[i] = 0;
      NY[i] = 0;
      NZ[i] = 0;
   }

   minimum_area = (((double) 1.e-4 > EPS_0) ? (double) 1.e-4:EPS_0);

   /* Calculate maximum number of vertices per polygon */
   k = 6;    n = 7*npolygons;
   while ( TRUE )
   {   for (i=k+7; i<n; i+=7)
           if (Vert_f_Pol[i] > Vert_f_Pol[k]) break;
       if (i >= n) break;    k = i;
   }
   max_vert_per_pol = Vert_f_Pol[k];

   /* Calculate the Normals vector components for each Polygon */
   /*$dir vector */
   for ( i=0; i<npolygons; i++) {  /* Vectorized */
      if (Vert_f_Pol[6+i*7]>0) {  /* check for valid polygon added by BEP 2-13-92 */
         NxA[i] = VX[Vert_f_Pol[1+i*7]] - VX[Vert_f_Pol[0+i*7]];
         NyA[i] = VY[Vert_f_Pol[1+i*7]] - VY[Vert_f_Pol[0+i*7]];
         NzA[i] = VZ[Vert_f_Pol[1+i*7]] - VZ[Vert_f_Pol[0+i*7]];
      }
   }

   swap_flag = 0;
   for ( k = 2; k < max_vert_per_pol; k++ )
   {

      if (swap_flag==0) {
         /*$dir no_recurrence */        /* Vectorized */
         for ( i=0; i<npolygons; i++ ) {
            if ( Vert_f_Pol[k+i*7] >= 0 ) {
               NxB[i]  = VX[Vert_f_Pol[k+i*7]] - VX[Vert_f_Pol[0+i*7]];
               NyB[i]  = VY[Vert_f_Pol[k+i*7]] - VY[Vert_f_Pol[0+i*7]];
               NzB[i]  = VZ[Vert_f_Pol[k+i*7]] - VZ[Vert_f_Pol[0+i*7]];
               Pnx[i] = NyA[i]*NzB[i] - NzA[i]*NyB[i];
               Pny[i] = NzA[i]*NxB[i] - NxA[i]*NzB[i];
               Pnz[i] = NxA[i]*NyB[i] - NyA[i]*NxB[i];
               NxA[i] = Pnx[i]*Pnx[i] + Pny[i]*Pny[i] + Pnz[i]*Pnz[i];
               if (NxA[i] > minimum_area) {
                  Pnx[i] /= NxA[i];
                  Pny[i] /= NxA[i];
                  Pnz[i] /= NxA[i];
               }
            }
         }
      }
      else {  /* swap_flag!=0 */
         /*$dir no_recurrence */        /* Vectorized */
         for ( i=0; i<npolygons; i++ ) {
            if ( Vert_f_Pol[k+i*7] >= 0 ) {
               NxA[i]  = VX[Vert_f_Pol[k+i*7]] - VX[Vert_f_Pol[0+i*7]];
               NyA[i]  = VY[Vert_f_Pol[k+i*7]] - VY[Vert_f_Pol[0+i*7]];
               NzA[i]  = VZ[Vert_f_Pol[k+i*7]] - VZ[Vert_f_Pol[0+i*7]];
               Pnx[i] = NyB[i]*NzA[i] - NzB[i]*NyA[i];
               Pny[i] = NzB[i]*NxA[i] - NxB[i]*NzA[i];
               Pnz[i] = NxB[i]*NyA[i] - NyB[i]*NxA[i];
               NxB[i] = Pnx[i]*Pnx[i] + Pny[i]*Pny[i] + Pnz[i]*Pnz[i];
               if (NxB[i] > minimum_area) {
                  Pnx[i] /= NxB[i];
                  Pny[i] /= NxB[i];
                  Pnz[i] /= NxB[i];
               }
            }
         }
      }

       /* This Loop <CAN'T> be Vectorized */
       for ( i=0; i<npolygons; i++ )
       {   if (Vert_f_Pol[k+i*7] >= 0)
           {   iv[0] = Vert_f_Pol[0+i*7];
               iv[1] = Vert_f_Pol[(k-1)+i*7];
               iv[2] = Vert_f_Pol[k+i*7];
/* WLH 25 Oct 97
               if (NxA[i] > minimum_area) {
*/
                 x = Pnx[i];   y = Pny[i];   z = Pnz[i];
/* WLH 25 Oct 97
               }
               else {
               //  adjust_normal_by_gradiente(iv,x,y,z);
                    if (VX[iv[0]]==VX[iv[1]] || VX[iv[0]]==VX[iv[2]])
                       ix = (int)VX[iv[0]];
                    else
                       ix = (int)VX[iv[1]];

                    if (VY[iv[0]]==VY[iv[1]] || VY[iv[0]]==VY[iv[2]])
                       iy = (int)VY[iv[0]];
                    else
                       iy = (int)VY[iv[1]];

                    if (VZ[iv[0]]==VZ[iv[1]] || VZ[iv[0]]==VZ[iv[2]])
                       iz = (int)VZ[iv[0]];
                    else
                       iz = (int)VZ[iv[1]];

                    i1 = ix;

                    if (i1 != ixb)
                       i2 = i1 + 1;
                    else {
                       i2 = i1;
                       i1--;
                    }

// WLH 24 Oct 97
//                    if (ptGRID[(int)iy + (int)i2*ydim + (int)iz*xdim_x_ydim]
//                        >= 1.0e30 ) {
//
                    int ii2 = (int)iy + (int)i2*ydim + (int)iz*xdim_x_ydim;
                    int ii1 = (int)iy + (int)i1*ydim + (int)iz*xdim_x_ydim;
                    // test for missing
                    if (ptGRID[ii2] != ptGRID[ii2] || ptGRID[ii1] != ptGRID[ii1]) {
                       i2--;
                       i1--;
                    }

// WLH 24 Oct 97
//                    x = ptGRID[(int)iy + (int)i2*ydim + (int)iz*xdim_x_ydim]
//                      - ptGRID[(int)iy + (int)i1*ydim + (int)iz*xdim_x_ydim];
//
                    x = ptGRID[ii2] - ptGRID[ii1];
                    i1 = iy;

                    if (i1 != iyb)
                       i2 = i1 + 1;
                    else {
                       i2 = i1;
                       i1--;
                    }

// WLH 24 Oct 97
//                    if (ptGRID[(int)i2 + (int)ix*ydim + (int)iz*xdim_x_ydim]
//                        >= 1.0e30 ) {
//
                    ii2 = (int)i2 + (int)ix*ydim + (int)iz*xdim_x_ydim;
                    ii1 = (int)i1 + (int)ix*ydim + (int)iz*xdim_x_ydim;
                    // test for missing
                    if (ptGRID[ii2] != ptGRID[ii2] || ptGRID[ii1] != ptGRID[ii1]) {
                       i2--;
                       i1--;
                    }
// WLH 24 Oct 97
//                    y = ptGRID[(int)i2 + (int)ix*ydim + (int)iz*xdim_x_ydim]
//                      - ptGRID[(int)i1 + (int)ix*ydim + (int)iz*xdim_x_ydim];
//
                    y = ptGRID[ii2] - ptGRID[ii1];
                    i1 = iz;

                    if (i1 != izb)
                       i2 = i1 + 1;
                    else {
                       i2 = i1;
                       i1--;
                    }
// WLH 24 Oct 97
//                    if (ptGRID[(int)iy + (int)ix*ydim + (int)i2*xdim_x_ydim]
//                        >= 1.0e30 ) {
//
                    ii2 = (int)iy + (int)ix*ydim + (int)i2*xdim_x_ydim;
                    ii1 = (int)iy + (int)ix*ydim + (int)i1*xdim_x_ydim;
                    // test for missing
                    if (ptGRID[ii2] != ptGRID[ii2] || ptGRID[ii1] != ptGRID[ii1]) {
                       i2--;
                       i1--;
                    }

// WLH 24 Oct 97
//                    z = ptGRID[(int)iy + (int)ix*ydim + (int)i2*xdim_x_ydim]
//                      - ptGRID[(int)iy + (int)ix*ydim + (int)i1*xdim_x_ydim];
//
                    z = ptGRID[ii2] - ptGRID[ii1];
                    a = (x*x + y*y + z*z);

                    if (a > 0.) {
                       x /= a;
                       y /= a;
                       z /= a;
                    }
               }
end of WLH 25 Oct 97 */


               // Update the origin vertex
                  NX[iv[0]] += x;   NY[iv[0]] += y;   NZ[iv[0]] += z;

               // Update the vertex that defines the first vector
                  NX[iv[1]] += x;   NY[iv[1]] += y;   NZ[iv[1]] += z;

               // Update the vertex that defines the second vector
                  NX[iv[2]] += x;   NY[iv[2]] += y;   NZ[iv[2]] += z;
           }
       }

       swap_flag = ( (swap_flag != 0) ? 0 : 1 );
    }
 
    /* Apply Aspect Ratio in the Normals */
    if (arX != 1.0) for (i=0; i<nvertex; i++) NX[i] /= arX;  /* Vectorized */
    if (arY != 1.0) for (i=0; i<nvertex; i++) NY[i] /= arY;  /* Vectorized */
    if (arZ != 1.0) for (i=0; i<nvertex; i++) NZ[i] /= arZ;  /* Vectorized */
 
    /* Normalize the Normals */
    for ( i=0; i<nvertex; i++ )  /* Vectorized */
    {   len = (double) Math.sqrt(NX[i]*NX[i] + NY[i]*NY[i] + NZ[i]*NZ[i]);
        if (len > EPS_0) {
            NX[i] /= len;
            NY[i] /= len;
            NZ[i] /= len;
        }
    }

  }

  public static int poly_triangle_stripe( int[] vet_pol, int[] Tri_Stripe,
                            int nvertex, int npolygons, int[] Pol_f_Vert,
                            int[] Vert_f_Pol ) throws VisADException {
   int  i, j, k, m, ii, npol, cpol, idx, off, Nvt,
        vA, vB, ivA, ivB, iST, last_pol;
   boolean f_line_conection = false;

    last_pol = 0;
    npol = 0;
    iST = 0;
    ivB = 0;

    for (i=0; i<npolygons; i++) vet_pol[i] = 1;  /* Vectorized */

    while (TRUE)
    {
        /* find_unselected_pol(cpol); */
        for (cpol=last_pol; cpol<npolygons; cpol++) {
           if ( (vet_pol[cpol] != 0) ) break;
        }
        if (cpol == npolygons) {
            cpol = -1;
        }
        else {
            last_pol = cpol;
        }
        /* end  find_unselected_pol(cpol); */

        if (cpol < 0) break;
/*      update_polygon            */
        vet_pol[cpol] = 0;
/* end     update_polygon            */

/*      get_vertices_of_pol(cpol,Vt,Nvt); {    */
            Nvt = Vert_f_Pol[(j=cpol*7)+6];
            off = j;
/*      }                                      */
/* end      get_vertices_of_pol(cpol,Vt,Nvt); {    */


        for (ivA=0; ivA<Nvt; ivA++) {
            ivB = (((ivA+1)==Nvt) ? 0:(ivA+1));
/*          get_pol_vert(Vt[ivA],Vt[ivB],npol) { */
               npol = -1;
               if (Vert_f_Pol[ivA+off]>=0 && Vert_f_Pol[ivB+off]>=0) {
                  i=Vert_f_Pol[ivA+off]*9;
                  k=i+Pol_f_Vert [i+8];
                  j=Vert_f_Pol[ivB+off]*9;
                  m=j+Pol_f_Vert [j+8];
                  while (i>0 && j>0 && i<k && j <m ) {
                     if (Pol_f_Vert [i] == Pol_f_Vert [j] &&
                         (vet_pol[Pol_f_Vert[i]] != 0) ) {
                        npol=Pol_f_Vert [i];
                        break;
                     }
                     else if (Pol_f_Vert [i] < Pol_f_Vert [j])
                          i++;
                     else
                          j++;
                  }
               }
/*          }                                   */
/* end          get_pol_vert(Vt[ivA],Vt[ivB],npol) { */
            if (npol >= 0) break;
        }
        /* insert polygon alone */
        if (npol < 0)
        { /*ptT = NTAB + STAB[Nvt-3];*/
            idx = STAB[Nvt-3];
            if (iST > 0)
            {   Tri_Stripe[iST]   = Tri_Stripe[iST-1];    iST++;
                Tri_Stripe[iST++] = Vert_f_Pol[NTAB[idx]+off];
            }
            else f_line_conection = true; /* WLH 3-9-95 added */
            for (ii=0; ii< ((Nvt < 6) ? Nvt:6); ii++) {
                Tri_Stripe[iST++] = Vert_f_Pol[NTAB[idx++]+off];
              }
            continue;
        }

        if (( (ivB != 0) && ivA==(ivB-1)) || ( !(ivB != 0) && ivA==Nvt-1)) {
         /* ptT = ITAB + STAB[Nvt-3] + (ivB+1)*Nvt; */
            idx = STAB[Nvt-3] + (ivB+1)*Nvt;

            if (f_line_conection)
            {   Tri_Stripe[iST]   = Tri_Stripe[iST-1];    iST++;
                Tri_Stripe[iST++] = Vert_f_Pol[ITAB[idx-1]+off];
                f_line_conection = false;
            }
            for (ii=0; ii<((Nvt < 6) ? Nvt:6); ii++) {
                Tri_Stripe[iST++] = Vert_f_Pol[ITAB[--idx]+off];
            }

        }
        else {
         /* ptT = NTAB + STAB[Nvt-3] + (ivB+1)*Nvt; */
            idx = STAB[Nvt-3] + (ivB+1)*Nvt;

            if (f_line_conection)
            {   Tri_Stripe[iST]   = Tri_Stripe[iST-1];    iST++;
                Tri_Stripe[iST++] = Vert_f_Pol[NTAB[idx-1]+off];
                f_line_conection = false;
            }
            for (ii=0; ii<((Nvt < 6) ? Nvt:6); ii++) {
                Tri_Stripe[iST++] = Vert_f_Pol[NTAB[--idx]+off];
            }

        }

        vB = Tri_Stripe[iST-1];
        vA = Tri_Stripe[iST-2];
        cpol = npol;

        while (TRUE)
        {
/*          get_vertices_of_pol(cpol,Vt,Nvt)  {   */
                Nvt = Vert_f_Pol [(j=cpol*7)+6];
                off = j;
/*          }                                     */


/*          update_polygon(cpol)                  */
            vet_pol[cpol] = 0;
            for (ivA=0; ivA<Nvt && Vert_f_Pol[ivA+off]!=vA; ivA++);
            for (ivB=0; ivB<Nvt && Vert_f_Pol[ivB+off]!=vB; ivB++);
                 if (( (ivB != 0) && ivA==(ivB-1)) || (!(ivB != 0) && ivA==Nvt-1)) {
                /* ptT = NTAB + STAB[Nvt-3] + ivA*Nvt + 2; */
                    idx = STAB[Nvt-3] + ivA*Nvt + 2;

                    for (ii=2; ii<((Nvt < 6) ? Nvt:6); ii++)
                        Tri_Stripe[iST++] = Vert_f_Pol[NTAB[idx++]+off];
                 }
                 else {
                /*  ptT = ITAB + STAB[Nvt-3] + ivA*Nvt + 2; */
                    idx = STAB[Nvt-3] + ivA*Nvt + 2;

                    for (ii=2; ii<((Nvt < 6) ? Nvt:6); ii++)
                        Tri_Stripe[iST++] = Vert_f_Pol[ITAB[idx++]+off];
                 }

            vB = Tri_Stripe[iST-1];
            vA = Tri_Stripe[iST-2];

/*          get_pol_vert(vA,vB,cpol) {     */
               cpol = -1;
               if (vA>=0 && vB>=0) {
                 i=vA*9;
                 k=i+Pol_f_Vert [i+8];
                 j=vB*9;
                 m=j+Pol_f_Vert [j+8];
                 while (i>0 && j>0 && i<k && j<m) {
                    if (Pol_f_Vert [i] == Pol_f_Vert [j] && (vet_pol[Pol_f_Vert[i]] != 0) ) {
                      cpol=Pol_f_Vert[i];
                      break;
                    }
                    else if (Pol_f_Vert [i] < Pol_f_Vert [j])
                      i++;
                    else
                      j++;
                 }
               }
/*         }                               */

            if (cpol < 0)
/* WLH 25 Oct 97
          if (PEX) {
            {   f_line_conection  = true;
                break;
            }
          }
          else {
*/

            {   vA = Tri_Stripe[iST-3];
/*          get_pol_vert(vA,vB,cpol) {   */
               cpol = -1;
               if (vA>=0 && vB>=0) {
                 i=vA*9;
                 k=i+Pol_f_Vert [i+8];
                 j=vB*9;
                 m=j+Pol_f_Vert [j+8];
                 while (i>0 && j>0 && i<k && j<m) {
                    if (Pol_f_Vert [i] == Pol_f_Vert [j] &&
                        (vet_pol[Pol_f_Vert[i]] != 0) ) {
                      cpol=Pol_f_Vert[i];
                      break;
                    }
                    else if (Pol_f_Vert [i] < Pol_f_Vert [j])
                      i++;
                    else
                      j++;
                 }
               }

/*          }                            */
                if (cpol < 0)
                {   f_line_conection  = true;
                    break;
                }
                else
                {   Tri_Stripe[iST++] = vA;
                    i = vA;
                    vA = vB;
                    vB = i;
                }
            }
/* WLH 25 Oct 97: no PEX
          }
*/
        }
    }

    return iST;
  }

    public static void main(String args[]) {

    int cnt = 0;
    int xdim = 21;
    int ydim = 21;
    int zdim = 21;
    double[] ptGRID = new double[xdim*ydim*zdim];
    int i;
    int j;
    int k;
    int LowLev = 0;
    double isovalue = (double)63.0;
    double ARX = (double)1.0;
    double ARY = (double)1.0;
    double ARZ = (double)1.0;
    int MAX_ISO_VERTS = 65000;
    double[] vc = new double[MAX_ISO_VERTS];
    double[] vr = new double[MAX_ISO_VERTS];
    double[] vl = new double[MAX_ISO_VERTS];
    double[] nx = new double[MAX_ISO_VERTS];
    double[] ny = new double[MAX_ISO_VERTS];
    double[] nz = new double[MAX_ISO_VERTS];
    int NPTS = 2*MAX_ISO_VERTS;
    int[] vpts = new int[NPTS];
    Contour3D  cube1;
    Contour3D  Test;


       for(k=0; k<zdim; k++) {
         for(i=0; i<xdim; i++) {
            for(j=0;j<ydim; j++) {

               ptGRID[cnt] = (double)(50 + Math.sqrt((j-10)*(j-10) + (i-10)*(i-10) + (k-10)*(k-10)));
               cnt++;
            }
         }
       }


       try {

          Test = new Contour3D(ptGRID, xdim, ydim, zdim, LowLev, isovalue,
                               ARX, ARY, ARZ, MAX_ISO_VERTS, vc, vr, vl, 
                               nx, ny, nz, NPTS, vpts );
       }
       catch ( VisADException e ) { 
          System.out.println( e.getMessage() );
          return;
       }

       try {
           Test.main_isosurf();
       }
       catch ( VisADException e ) {
          System.out.println( e.getMessage() );
       }

    }
  }

