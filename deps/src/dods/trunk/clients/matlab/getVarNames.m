function [varNames] = getVarNames(dodsVar, nameMap)

name = dodsVar.getTypeName.toCharArray;
name = reshape(name, 1, size(name,1));
varNames = '';

switch name
 case { 'Byte', 'Int16', 'UInt16', 'Int32', 'UInt32', 'Float32', ...
	'Float64', 'String', 'Array' }
  varNames = fixName(dodsVar.getName, nameMap);
  
 case { 'Structure', 'Grid', 'Sequence' }
  subVars = dodsVar.getVariables;
  
  while subVars.hasMoreElements == 1
    subVar = subVars.nextElement;
    varNames = strvcat(varNames, getVarNames(subVar, nameMap));
  end

end
