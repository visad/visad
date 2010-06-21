function [vars] = processVarFlat(dodsVar, nameMap, vars, ...
				 stringToFloat, dimOrder)

%
% Returns a flattened matlab representation of a dods variable.  
% This function behaves the same as processVar.m, except any
% variable that would have been inside a structure is now at the top level.
%

import dods.clients.matlab.*;

name = dodsVar.getTypeName.toCharArray;
name = reshape(name, 1, size(name,1));

switch name
  case {'Byte', 'Int16', 'UInt16', 'Int32', 'UInt32', 'Float32', 'Float64' }
  varName = fixName(dodsVar.getName, nameMap);
  
  if ~isempty(vars)
    if isempty(strmatch(varName, fieldnames(vars)))
      vars = setfield(vars, varName, dodsVar.getValue);
    end
  else
    vars = struct(varName, dodsVar.getValue);
  end

  case { 'String', 'Url' }
   var = dodsVar.getValue.toCharArray;
   var = reshape(var, 1, size(var,1));
   varName = fixName(dodsVar.getName, nameMap);
   
   if ~isempty(vars)
     if isempty(strmatch(varName, fieldnames(vars), 'exact'))
       vars = setfield(vars, varName, var);
     end
   else
     vars = struct(varName, var);
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
  
  varName = fixName(dodsVar.getName, nameMap);
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
    disp([varName ' skipped, Sequences of Grids or Structures are' ...
	  ' not supported in flatten mode']);
    var = '';
    
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
      var = permute(var, getOrderVector(dimOrder, length(cmDims)));
    end
   otherwise
    disp([ varName ' skipped, arrays of this type are not supported']);
    var = '';
  end
  
  if ~isempty(vars)
    if isempty(strmatch(varName, fieldnames(vars), 'exact'))
      vars = setfield(vars, varName, var);
    end
  else
    vars = struct(varName, var);
  end

 case { 'Structure', 'Grid' }

  subVars = dodsVar.getVariables;

  while subVars.hasMoreElements == 1
    subVar = subVars.nextElement;
    try,
      [vars] = processVarFlat(subVar, nameMap, vars, stringToFloat, ...
			      dimOrder);
    catch,
      error(lasterr),
    end
  end
  
 case 'Sequence'
  
  proc = DodsSequenceProcessor(dodsVar);

  try,
    seq = processSeq(dodsVar, nameMap, proc, '', stringToFloat, 1);,
  catch,
    error(lasterr),
  end
  
  seqNames = fieldnames(seq);
  
  % Unpack the structure returned by processSeq and add the fields
  % to the main structure.
  if ~isempty(fieldnames(vars))
    if isempty(strmatch(seqNames(1), fieldnames(vars)))
      vars = setfield(vars, seqNames{1}, getfield(seq, seqNames{1}));
    end
  else
    vars = struct(seqNames{1}, getfield(seq, seqNames{1}));
  end
  
  for i=2:length(seqNames)
    if isempty(strmatch(seqNames(i), fieldnames(vars)))
      vars = setfield(vars, seqNames{i}, getfield(seq, seqNames{i}));
    end
  end
  
end

