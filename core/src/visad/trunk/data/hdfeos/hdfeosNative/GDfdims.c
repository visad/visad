/*
Copyright (C) 1996 Hughes and Applied Research Corporation

Permission to use, modify, and distribute this software and its documentation
for any purpose without fee is hereby granted, provided that the above
copyright notice appear in all copies and that both that copyright notice and
this permission notice appear in supporting documentation.
*/


#include "mfhdf.h"
#include "hcomp.h"
#include <math.h>
#include "cfortHdf.h"
#include "HdfEosDef.h"

#define GDIDOFFSET 4194304


int32 GDXSDcomb[512*5];
char  GDXSDname[HDFE_NAMBUFSIZE];
char  GDXSDdims[HDFE_DIMBUFSIZE];


#define NGRID 200
/* Grid Structure External Arrays */
struct gridStructure
{
    int32 active;
    int32 IDTable;
    int32 VIDTable[2];
    int32 fid;
    int32 nSDS;
    int32 *sdsID;
    int32 compcode;
    intn  compparm[5];
    int32 tilecode;
    int32 tilerank;
    int32 tiledims[8];
};
struct gridStructure GDXGrid[NGRID];



#define NGRIDREGN 256
struct gridRegion
{
    int32 fid;
    int32 gridID;
    int32 xStart;
    int32 xCount;
    int32 yStart;
    int32 yCount;
    float64 upleftpt[2];
    float64 lowrightpt[2];
    int32 StartVertical[8];
    int32 StopVertical[8];
    char *DimNamePtr[8];
};
struct gridRegion *GDXRegion[NGRIDREGN];



/* Grid Function Prototypes (internal routines) */
intn GDchkgdid(int32, char *, int32 *, int32 *, int32 *);
intn GDfldinfo(int32, char *, int32 *, int32 [], int32 *, char *);
intn GDdeffld(int32, char *, char *, int32, int32);
intn GDwrmeta(int32, char *, char *, int32);
intn GDSDfldsrch(int32, int32, char *, int32 *, int32 *,
                 int32 *, int32 *, int32 [], int32 *);
intn GDwrrdfield(int32, char *, char *,
            int32 [], int32 [], int32 [], VOIDP datbuf);
intn GDwrfld(int32, char *, int32 [], int32 [], int32 [], VOIDP);
intn GDrdfld(int32, char *, int32 [], int32 [], int32 [], VOIDP);
intn GDwrrdattr(int32, char *, int32, int32, char *, VOIDP);
intn GDll2ij(int32, int32, float64 [], int32, int32, int32, float64 [],
             float64 [], int32, float64 [], float64 [], int32 [], int32 [],
             float64 [], float64 []);
intn GDij2ll(int32, int32, float64 [], int32, int32, int32,
             float64 [], float64 [], int32, int32 [], int32 [],
             float64 [], float64 [], int32, int32);
intn GDreginfo(int32, int32, char *, int32 *, int32 *, int32 [], int32 *,
               float64 [], float64 []);
intn  GDgetdefaults(int32, int32, float64 [], int32, float64 [], float64 []);
int32 GDdefvrtreg(int32, int32, char *, float64 []);
intn GDgetpix(int32, int32, float64 [], float64 [], int32 [], int32 []);
int32 GDgetpixval(int32, int32, int32 [], int32 [], char *, VOIDP);
intn GDtangentpnts(int32, float64 [], float64 [], float64 [], float64 [],
                   float64 [], int32 *);
intn GDwrrdtile(int32, char *, char *, int32 [], VOIDP);
intn GDdeftle(int32, int32, int32, int32 []);
intn GDtleinfo(int32, char *, int32 *, int32 *, int32 []);
intn GDwrtle(int32, char *, int32 [], VOIDP);
intn GDrdtle(int32, char *, int32 [],  VOIDP);

/*----------------------------------------------------------------------------|
|  BEGIN_PROLOG                                                               |
|                                                                             |
|  FUNCTION: GDfdims                                                          |
|                                                                             |
|  DESCRIPTION: Retrieve information about a specific geolocation or data     |
|                field in the grid.                                           |
|                                                                             |
|                                                                             |
|  Return Value    Type     Units     Description                             |
|  ============   ======  =========   =====================================   |
|  status         intn                return status (0) SUCCEED, (-1) FAIL    |
|                                                                             |
|  INPUTS:                                                                    |
|  gridID         int32               grid structure id                       |
|  fieldname      char                name of field                           |
|                                                                             |
|                                                                             |
|  OUTPUTS:                                                                   |
|  rank           int32               rank of field (# of dims)               |
|  dims           int32               field dimensions                        |
|  numbertype     int32               field number type                       |
|  dimlist        char                field dimension list                    |
|                                                                             |
|                                                                             |
|  OUTPUTS:                                                                   |
|             None                                                            |
|                                                                             |
|  NOTES:                                                                     |
|                                                                             |
|                                                                             |
|   Date     Programmer   Description                                         |
|  ======   ============  =================================================   |
|  Jun 96   Joel Gales    Original Programmer                                 |
|  Aug 96   Joel Gales    Make metadata ODL compliant                         |
|  Jan 97   Joel Gales    Check for metadata error status from EHgetmetavalue |
|                                                                             |
|  END_PROLOG                                                                 |
-----------------------------------------------------------------------------*/
int32
GDfdims(int32 gridID, char *fieldname, int32 * strbufsize )

{
    intn            i;		/* Loop index */
    intn            status;	/* routine return status variable */
    intn            statmeta = 0;	/* EHgetmetavalue return status */

    int32           fid;	/* HDF-EOS file ID */
    int32           sdInterfaceID;	/* HDF SDS interface ID */
    int32           idOffset = GDIDOFFSET;	/* Grid ID offset */
    int32           ndims;	/* Number of dimensions */
    int32           slen[8];	/* Length of each entry in parsed string */
    int32           dum;	/* Dummy variable */
    int32           xdim;	/* X dim size */
    int32           ydim;	/* Y dim size */
    int32           sdid;	/* SDS id */

    char           *metabuf;	/* Pointer to structural metadata (SM) */
    char           *metaptrs[2];/* Pointers to begin and end of SM section */
    char            gridname[80];	/* Grid Name */
    char            utlstr[80];	/* Utility string */
    char           *ptr[8];	/* String pointers for parsed string */
    char            dimstr[64];	/* Individual dimension entry string */

    *strbufsize = -1;
          ndims = -1;

    status = GDchkgdid(gridID, "GDfieldinfo", &fid, &sdInterfaceID, &dum);

    Vgetname(GDXGrid[gridID % idOffset].IDTable, gridname);

	metabuf = (char *) EHmetagroup(sdInterfaceID, gridname, "g",
				       "DataField", metaptrs);


	/* Search for field */
	sprintf(utlstr, "%s%s%s", "\"", fieldname, "\"\n");
	metaptrs[0] = strstr(metaptrs[0], utlstr);

	/* If field found ... */
	if (metaptrs[0] < metaptrs[1] && metaptrs[0] != NULL)
	{

	    /*
	     * Get DimList string and trim off leading and trailing parens
	     * "()"
	     */
	    statmeta = EHgetmetavalue(metaptrs, "DimList", utlstr);

	    if (statmeta == 0)
	    {
		memcpy(utlstr, utlstr + 1, strlen(utlstr) - 2);
		utlstr[strlen(utlstr) - 2] = 0;

		/* Parse trimmed DimList string and get rank */
		ndims = EHparsestr(utlstr, ',', ptr, slen);

            /*
             * Copy each entry in DimList and remove leading and trailing quotes,
             * Get dimension sizes and concatanate dimension names to dimension
             * list
             */
            for (i = 0; i < ndims; i++)
            {
                memcpy(dimstr, ptr[i] + 1, slen[i] - 2);
                dimstr[slen[i] - 2] = 0;

                  if (i > 0)
                  {
                      *strbufsize += 1;
                  }

                *strbufsize += strlen( dimstr );
             }

	    }
	    else
	    {
		status = -1;
		HEpush(DFE_GENAPP, "GDfieldinfo", __FILE__, __LINE__);
		HEreport(
			 "\"DimList\" string not found in metadata.\n");
	    }

        }
	free(metabuf);

        if (*strbufsize == -1)
        {
	  ndims= -1;

        }

    return ( ndims );
}
