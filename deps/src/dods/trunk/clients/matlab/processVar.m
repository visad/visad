function [var] = processVar(dodsVar, nameMap, stringToFloat, dimOrder)

%
% Returns a matlab representation of a dods variable.  
%    
%   - No conversion is required for atomic types.
%   - Strings are converted to horizontal character arrays.
%   - Arrays are returned with the dimensions reversed
%     (This is how matlab expects arrays to be)
%   - Grids are returned as a structure with both the main array 
%     and the map vectors as fields
%   - Structures are returned as structures with the subvars as fields
%   - Sequences are returned as structures with arrays of the
%     subvars as fields.  Nested Sequences are returned as nested structs.
%

import dods.clients.matlab.*;

name = char(dodsVar.getTypeName);

switch name
 case { 'Byte', 'Int16', 'UInt16', 'Int32', 'UInt32', 'Float32', 'Float64' }
  var = dodsVar.getValue;
  
 case { 'String', 'Url' }
  var = char(dodsVar.getValue);
  if stringToFloat == 1
    var = str2double(var);
  end
  
 case { 'Array', 'List' }

  if strcmp(name, 'List') == 1
    cmDims = dodsVar.getLength;
    rmDims = cmDims;
  else
    % Get the row-major dimensions dods uses
    dimensions = dodsVar.getDimensions;
    rmDims = [];
    cmDims = [];
    while dimensions.hasMoreElements == 1;
      dim = dimensions.nextElement;
      rmDims = [ rmDims dim.getSize ];
      cmDims = [ dim.getSize cmDims ];
    end
  end
  
  arrayTypeName = char(dodsVar.getArrayTypeName);
  
  switch(arrayTypeName) 
   case { 'String', 'Url' }
    % At this point, only single-dimension arrays of non-atomic
    % types are supported.  Multi-dimension support is possible,
    % but would require the reshaping code to be made into an m-file
    var = dodsVar.getData;
    if iscell(var)
      var = cellArrayOfStringsToCharArray(var);
    end
    
   case { 'Structure', 'Grid' }
    pv = dodsVar.getPrimitiveVector;
    length = pv.getLength;
    
    for i=1:length
      arrayVar = pv.getValue(i-1);
      var(i) = processVar(arrayVar, nameMap, stringToFloat, dimOrder);
    end
    
   case { 'Byte', 'Int16', 'UInt16', 'Int32', 'UInt32', 'Float32', 'Float64' }
    var = double(dodsVar.getData);
    
    % The data will always be returned as a signed type, so if it
    % should be unsigned it has to be converted here
    switch(arrayTypeName)
     case 'Byte'
       ind = find(var < 0);
       var(ind) = var(ind) + 256;
     case 'UInt16PrimitiveVector'
      ind = find(var < 0);
      var(ind) = var(ind) + 65536;
     case 'dods.dap.UInt32PrimitiveVector'
      ind = find(var < 0);
      var(ind) = var(ind) + 4294967296;
    end
    
    % Convert the array to column-major order and reshape it to
    % the correct dimensions.
    if size(rmDims, 2) > 1
      var = reshape(var, cmDims);
      var = permute(var, getOrderVector(dimOrder, size(cmDims, 2)));
    end
   otherwise
    var = '';
  end
  
 case { 'Structure', 'Grid' }
  % Getting the data from Grids and Structures is pretty much the
  % same, so these two can be generalized into one case.
  
  subVars = dodsVar.getVariables;
   
  subVar = subVars.nextElement;
  subVarName = fixName(subVar.getName, nameMap);
  var = struct(subVarName, processVar(subVar, nameMap, stringToFloat, ...
				      dimOrder));
  
  while subVars.hasMoreElements == 1
    subVar = subVars.nextElement;
    subVarName = fixName(subVar.getName, nameMap);
    try,
      var = setfield(var, subVarName, processVar(subVar, nameMap, ...
						 stringToFloat, dimOrder));,
    catch,
      error(lasterr),
    end
  end
  
 case 'Sequence'
  
  % Calling java functions from matlab isn't particularly fast, so
  % the DodsSequenceProcessor class is used to convert the columns
  % of the sequence into arrays
  proc = dods.clients.matlab.DodsSequenceProcessor(dodsVar);
  
  try,
    var = processSeq(dodsVar, nameMap, proc, '', stringToFloat, 0);,
  catch,
    error(lasterr),
  end
  
end
