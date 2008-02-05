 * /*
 * VisAD system for interactive analysis and visualization of numerical
 * data.  Copyright (C) 1996 - 2008 Bill Hibbard, Curtis Rueden, Tom
 * Rink, Dave Glowacki, Steve Emmerson, Tom Whittaker, Don Murray, and
 * Tommy Jasmin.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 * 
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the Free
 * Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
 * MA 02111-1307, USA
 */

#include "mfhdf.h"
#include "hcomp.h"
#include "cfortHdf.h"
#include "HdfEosDef.h"

#define SWIDOFFSET 1048576


int32 SWX1dcomb[512*3];
int32 SWXSDcomb[512*5];
char  SWXSDname[HDFE_NAMBUFSIZE];
char  SWXSDdims[HDFE_DIMBUFSIZE];


#define NSWATH 200
/* Swath Structure External Arrays */
struct swathStructure
{
    int32 active;
    int32 IDTable;
    int32 VIDTable[3];
    int32 fid;
    int32 nSDS;
    int32 *sdsID;
    int32 compcode;
    intn  compparm[5];
    int32 tilecode;
    int32 tilerank;
    int32 tiledims[8];
};
struct swathStructure SWXSwath[NSWATH];



#define NSWATHREGN 256
struct swathRegion
{
    int32 fid;
    int32 swathID;
    int32 nRegions;
    int32 StartRegion[32];
    int32 StopRegion[32];
    int32 StartVertical[8];
    int32 StopVertical[8];
    char *DimNamePtr[8];
};
struct swathRegion *SWXRegion[NSWATHREGN];

/* Swath Prototypes (internal routines) */
intn SWchkswid(int32, char *, int32 *, int32 *, int32 *);
int32 SWimapinfo(int32, char *, char *, int32 []);
int32 SWfinfo(int32, char *, char *, int32 *, int32 [], int32 *, char *);
intn SWfldinfo(int32, char *, int32 *, int32 [], int32 *, char *);
intn SWdefimap(int32, char *, char *, int32 []);
intn SWdefinefield(int32, char *, char *, char *, int32, int32);
intn SWdefgfld(int32, char *, char *, int32, int32);
intn SWdefdfld(int32, char *, char *, int32, int32);
intn SWwrgmeta(int32, char *, char *, int32);
intn SWwrdmeta(int32, char *, char *, int32);
intn SWwrrdattr(int32, char *, int32, int32, char *, VOIDP);
intn SW1dfldsrch(int32, int32, char *, char *, int32 *, int32 *, int32 *);
intn SWSDfldsrch(int32, int32, char *, int32 *, int32 *,
                 int32 *, int32 *, int32 [], int32 *);
intn SWwrrdfield(int32, char *, char *, int32 [], int32 [], int32 [], VOIDP);
intn SWwrfld(int32, char *, int32 [], int32 [], int32 [], VOIDP);
intn SWrdfld(int32, char *, int32 [], int32 [], int32 [], VOIDP);
intn SWreginfo(int32, int32, char *, int32 *, int32 *, int32 [], int32 *);
intn SWperinfo(int32, int32, char *, int32 *, int32 *, int32 [], int32 *);
int32 SWinqfields(int32, char *, char *, int32 [], int32 []);
int32 SWdefvrtreg(int32, int32, char *, float64 []);

/*----------------------------------------------------------------------------|
|  BEGIN_PROLOG                                                               |
|                                                                             |
|  FUNCTION: SWfdims                                                          |
|                                                                             |
|  DESCRIPTION: Returns field info                                            |
|                                                                             |
|                                                                             |
|  Return Value    Type     Units     Description                             |
|  ============   ======  =========   =====================================   |
|  ndims          int32               return status (0) SUCCEED, (-1) FAIL    |
|                                                                             |
|  INPUTS:                                                                    |
|  swathID        int32               swath structure id                      |
|  fieldtype      char                fieldtype (geo or data)                 |
|  fieldname      char                name of field                           |
|                                                                             |
|                                                                             |
|  OUTPUTS:                                                                   |
|  strbufsize     int32               size of dimlist                         |
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
SWfdims(int32 swathID, char *fieldtype, char *fieldname,
        int32 * strbufsize )

{
    intn            i;		/* Loop index */
    intn            j;		/* Loop index */
    intn            status;	/* routine return status variable */
    intn            statmeta = 0;	/* EHgetmetavalue return status */

    int32           fid;	/* HDF-EOS file ID */
    int32           sdInterfaceID;	/* HDF SDS interface ID */
    int32           idOffset = SWIDOFFSET;	/* Swath ID offset */
    int32           fsize;	/* field size in bytes */
    int32           ndims;	/* Number of dimensions */
    int32           slen[8];	/* Length of each entry in parsed string */
    int32           dum;	/* Dummy variable */
    int32           vdataID;	/* 1d field vdata ID */


    char           *metabuf;	/* Pointer to structural metadata (SM) */
    char           *metaptrs[2];/* Pointers to begin and end of SM section */
    char            swathname[80];	/* Swath Name */
    char            utlstr[80];	/* Utility string */
    char           *ptr[8];	/* String pointers for parsed string */
    char            dimstr[64];	/* Individual dimension entry string */


    *strbufsize = -1;
          ndims = -1;

    /* Get HDF-EOS file ID and SDS interface ID */
    status = SWchkswid(swathID, "SWfinfo", &fid, &sdInterfaceID, &dum);

    /* Get swath name */
    Vgetname(SWXSwath[swathID % idOffset].IDTable, swathname);

    /* Get pointers to appropriate "Field" section within SM */
    if (strcmp(fieldtype, "Geolocation Fields") == 0)
    {
	metabuf = (char *) EHmetagroup(sdInterfaceID, swathname, "s",
				       "GeoField", metaptrs);
    }
    else
    {
	metabuf = (char *) EHmetagroup(sdInterfaceID, swathname, "s",
				       "DataField", metaptrs);
    }


    /* Search for field */
    sprintf(utlstr, "%s%s%s", "\"", fieldname, "\"\n");
    metaptrs[0] = strstr(metaptrs[0], utlstr);

    /* If field found ... */
    if (metaptrs[0] < metaptrs[1] && metaptrs[0] != NULL)
    {

	/*
	 * Get DimList string and trim off leading and trailing parens "()"
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
	    HEpush(DFE_GENAPP, "SWfieldinfo", __FILE__, __LINE__);
	    HEreport(
		     "\"DimList\" string not found in metadata.\n");
	}

    }
    free(metabuf);

    if (*strbufsize == -1)
    {
	ndims = -1;
    }

    return ( ndims );
}
