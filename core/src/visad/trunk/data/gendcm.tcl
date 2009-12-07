##
## This script generates all of the array specific APIs for the DataCacheManager
##
## Run it as:
## tclsh gendcm.tcl > dcmapi
##then include dcmapi into DataCacheManager
##



set template {

  /**
   * get the value from the cache
   *
   * @param cacheId  the cache id
   *
   * @return  the value
   */
    public %type%%brackets% get%Type%Array%dimension%D(Object cacheId) {
        return (%type%%brackets%)getData(cacheId);
    }

  /**
   * add the data to the cache
   *
   * @param values the values to add
   *
   * @return the cache id
   */
  public Object addToCache(%type%%brackets% values) {
    return addToCache(null, values, TYPE_%TYPE%%dimension%D, false);
  }

  /**
   * add the data to the cache
   *
   * @param what the name of the item. used for tracking cache behavior
   * @param values the values to add
   *
   * @return the cache id
   */
  public Object addToCache(String what, %type%%brackets% values) {
      return addToCache(what, values, TYPE_%TYPE%%dimension%D, false);
  }
  
  /**
   * add the data to the cache
   *
   * @param what the name of the item. used for tracking cache behavior
   * @param values the values to add
   * @param removeIfNeeded If true then this data will not be written to disk and will be removed from the cache
   * when the cache is exceeding memory limits
   *
   * @return the cache id
   */
  public Object addToCache(String what, %type%%brackets% values, boolean removeIfNeeded) {
    return addToCache(what, values, TYPE_%TYPE%%dimension%D, removeIfNeeded);
  }


}

set sizeTemplate {
   if (type == TYPE_%TYPE%%dimension%D) {
        %type%%brackets% data= (%type%%brackets%) values;
        %sizecheck%
        return %bytes%*%sizecode%;

   }
}


puts "/********\n  Begin generated access methods\n*****/"

set procs "";
set types ""
set sizeMethod "/** Get the size of the array **/\nprivate static int getArraySize(int type, Object values) {\n";
set nameMethod "/** Get the name of the type **/\nprivate static String getNameForType(int type) {\n";
set cnt 0
for {set dimension 1} {$dimension<4} {incr dimension} {
    set brackets "";
     for {set j 0} {$j<$dimension} {incr j} {
         append brackets {[]}
    }
    foreach {type bytes} {double  8 float 4 int 4 short 2 byte 1} {
        set Type "[string toupper [string range $type 0 0]][string range $type 1 end]"
        set TYPE [string toupper $type]
        set code $template
        regsub -all %type% $code $type code
        regsub -all %Type% $code $Type code
        regsub -all %TYPE% $code $TYPE code
        regsub -all %dimension% $code $dimension code
        regsub -all %brackets% $code $brackets code


        set sizeCode ""
        set sizeCheck ""
        if {$dimension == 1} {
            set sizeCode {data.length}
        } elseif {$dimension == 2} {
            set sizeCheck {if (data[0]==null) return 0;}
            set sizeCode {data.length * data[0].length}
        } elseif {$dimension == 3} {
            set sizeCheck {if (data[0]==null) return 0; if(data[0][0]==null) return 0;}
            set sizeCode {data.length * data[0].length*data[0][0].length}
        }
        set tmp $sizeTemplate
        regsub -all %sizecode% $tmp $sizeCode tmp
        regsub -all %sizecheck% $tmp $sizeCheck tmp
        regsub -all %bytes% $tmp $bytes tmp
        regsub -all %type% $tmp $type tmp
        regsub -all %TYPE% $tmp $TYPE tmp
        regsub -all %dimension% $tmp $dimension tmp
        regsub -all %brackets% $tmp $brackets tmp
        append sizeMethod $tmp


        set tmp {    if (type == TYPE_%TYPE%%dimension%D) {return "%type%%dimension%d";}}
        regsub -all %type% $tmp $type tmp
        regsub -all %TYPE% $tmp $TYPE tmp
        regsub -all %dimension% $tmp $dimension tmp
        append nameMethod  $tmp
        append nameMethod  "\n"




        append procs $code
        set enum {private static final int TYPE_%TYPE%%dimension%D = %cnt%;}
        regsub -all %TYPE% $enum $TYPE enum
        regsub -all %dimension% $enum $dimension enum
        regsub -all %cnt% $enum $cnt enum
        append types $enum
        append types "\n"
        incr cnt
    }
}

append sizeMethod "\n   throw new IllegalArgumentException(\"Unknown type:\" + type);\n}\n\n"
append nameMethod " return \"unknown type\";\n}\n"
puts $types
puts $procs
puts $sizeMethod
puts $nameMethod
