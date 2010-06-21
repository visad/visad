#!/bin/csh

#/////////////////////////////////////////////////////////////////////////////
#// Copyright (c) 1999, COAS, Oregon State University  
#// ALL RIGHTS RESERVED.   U.S. Government Sponsorship acknowledged. 
#//
#// Please read the full copyright notice in the file COPYRIGHT
#// in this directory.
#//
#// Author: Nathan Potter (ndp@oce.orst.edu)
#//
#//                        College of Oceanic and Atmospheric Scieneces
#//                        Oregon State University
#//                        104 Ocean. Admin. Bldg.
#//                        Corvallis, OR 97331-5503
#//         
#/////////////////////////////////////////////////////////////////////////////

BEGIN {

printf("package dods.servers.www;\n");

printf("public class jscriptCore {\n");

printf("    private static boolean _Debug = false;\n"); 

printf("    public static String jScriptCode = ");

foundEnd = 0;

}
{

   if(!foundEnd){


      if( index($0, "Log: jscriptCore.tmpl") ){
         foundEnd = 1;
      }
      else {

         if(NR>1){
            printf("        + ");
         }
         printf("\"%s\\n\"\n",$0);

      }
   }

}
END {

   printf(";\n}\n");
}
