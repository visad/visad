function var = processSeq(dodsVar, nameMap, proc, path, ...
			  stringToFloat, flatten) 

%
% This function converts a dods sequence into a matlab structure of arrays
% Nested sequences are flattened so that all the variables are at
% the top level.
%
% dodsVar - the sequence variable to operate on
% nameMap - a java.lang.Hashtable containing any name substitutions 
%           (can be null)
% proc    - an already initialized dodsSequenceProcessor object
% path    - a string used to access variables in nested sequences.
%           For example, a path of 'Level_1' would tell the function to
%           retrieve a variable inside the Level_1 subsequence.
% stringToFloat - Whether strings should be converted to floats
% flatten       - Whether structures inside the sequence should be flattened
%

if isempty(path)
  seqVars = dodsVar.getVariables;
else
  try,
    subVar = dodsVar.getVariable(path);,
  catch,
    error(lasterr),
  end
  
  if isa(subVar, 'dods.dap.DConstructor')
    seqVars = subVar.getVariables;
  else
    error(strcat(path, ' is not of type DConstructor')),
  end
end

firstRun = 1;

while seqVars.hasMoreElements
  seqVar = seqVars.nextElement;
  
  dontPackage = 0;
  
  typeName = char(seqVar.getTypeName);
  varName = char(seqVar.getName);
  
  if ~isempty(path) 
    varName = strcat(path, '.', varName);
  end
  
  switch typeName
    
   case 'Byte'
    try,
      data = proc.getByte(varName);,
    catch,
      error(lasterr),
    end
   case 'Int16'
    try,
      data = proc.getInt16(varName);,
    catch,
      error(lasterr),
    end
   case 'UInt16'
    try,
      data = proc.getUInt16(varName);,
    catch,
      error(lasterr),
    end
   case 'Int32'
    try,
      data = proc.getInt32(varName);
    catch,
      error(lasterr),
    end
   case 'UInt32'
    try,
      data = proc.getUInt32(varName);
    catch,
      error(lasterr),
    end
   case 'Float32'
    try,
      data = proc.getFloat32(varName);
    catch,
      error(lasterr),
    end
   case 'Float64'
    try,
      data = proc.getFloat64(varName);
    catch,
      error(lasterr),
    end
   
   case 'String'
    try,
      tmpData = proc.getString(varName);
    catch,
      error(lasterr),
    end

    % If the strings aren't the same length, matlab will return
    % them as a cell array of strings.  If it does, we need to
    % convert the array to a regular array.
    
    if(iscell(tmpData)) 
      data = cellArrayOfStringsToCharArray(tmpData);
    else
      data = tmpData;
    end

    if stringToFloat == 1
      % data = str2double(data);
      
      num = 0;
      for i=1:size(data,1)
	num(i) = str2double(data(i,1:end));
      end
      data = num;
    end
    
   case 'Grid'    
    
    if ~flatten
      grids = dodsVar.getColumn(varName);
      
      if length(grids) > 0
	data = processVar(grids(1), nameMap, stringToFloat);
      end
      for i=2:length(grids)
	data(i) = processVar(grids(i), nameMap, stringToFloat);
      end
    else
      disp([ varName ' skipped, Sequences of Grids are not supported' ...
	     ' in flatten mode']);
      data = '';
    end
    
   case 'Array'
    disp([ varName 'skipped, Sequences of arrays not supported.']);
    data = '';
    
   case 'Structure'
    data = processSeq(dodsVar, nameMap, proc, varName, ...
		      stringToFloat, flatten);
    if flatten
      dontPackage = 1;
      names = fieldnames(data);
      
      for i=1:size(names, 1)
	if firstRun == 1
	  var = struct(names{i}, getfield(data, names{i}));
	  firstRun = 0;
	else
	  var = setfield(var, names{i}, getfield(data, names{i}));
	end
      end
    end
    
   case 'Sequence'
    dontPackage = 1;
    data = processSeq(dodsVar, nameMap, proc, varName, ... 
		      stringToFloat, flatten); 
    names = fieldnames(data);
    
    for i=1:size(names, 1)
      if firstRun == 1
	var = struct(names{i}, getfield(data, names{i}));
	firstRun = 0;
      else
	var = setfield(var, names{i}, getfield(data, names{i}));
      end
    end
  
  end
  
  if dontPackage == 0
    if firstRun == 1
      var = struct(fixName(seqVar.getName, nameMap), data);
      firstRun = 0;
    else
      var = setfield(var, fixName(seqVar.getName, nameMap), data);
    end
  end
  
end

