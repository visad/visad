function varargout = loaddods(varargin)

% DESCRIPTION:
%
%  Provides and interface to the Distributed Ocenographic Data
%  System (DODS) for matlab 6.  The syntax mirrors that of the
%  matlab 5 client.
%
% USAGE:
%  [var] = loaddods([switches,] URL [options] [, URL [options]]);
%
%  When loaddods is called with a return argument, it will return
%  the first variable in the dataset in flattened form.  When it is called
%  without a return argument it will return all the variables in
%  the dataset to the workspace in flattened form.
%
% SWITCHES:
%  -A                     -- return the DAS
%  -s                     -- return a list of the variables
%  -S                     -- preserve the structure of the dataset
%  -F                     -- convert strings to floats.
%  -V                     -- print version information
%  -e                     -- use the new error reporting scheme
%
%    note: -A, -s, and -S require return arguments
%
% PER URL OPTIONS:
%  -c <ce>                -- use constraint expression <ce>
%  -r <oldname>:<newname> -- map <oldname> to <newname> wherever it appears 
%                            in the dataset
%  -d [matlab|dods|reverse] -- reorder the dimensions 


% Check for Java
ver = version;
verMajor = str2num(ver(1));

if verMajor <= 5
  disp('This program needs matlab version 6 or higher to run.');
  return;
else
  version -java;
  if strcmp(ans, 'Java is not enabled') ~= 0
    disp([ 'You must have java enabled to use this program. ' ...
	   'If you want to avoid the overhead of the matlab desktop' ...
	   ' interface, try matlab -nodesktop as opposed to matlab' ...
	   ' -nojvm ' ]);
    return;
  end
end

% Initialize the output arguments so matlab won't complain if we
% exit unexpectedly.
for i=1:nargout
  varargout{i} = '';
end


if length(varargin) == 0
  error('Error, you must provide an input argument');
elseif length(varargin) == 1
  % If only one argument is provided, it can only be a url or '-V'
  
  if varargin{1}(1) ~= '-'
    urls = splitUrls(varargin{1});
    getData = 1;
    getDAS = 0;
    getNames = 0;
    stringToFloat = 0;
    newErrorScheme = 0;
    flatten = 1;
    
  elseif ~isempty(findstr('-V', varargin{1}))
    disp('loaddods for java: version 0.0.1');
    break;
  else
    error('Error: you must provide a url');
  end
    
else
  urls = '';
  
  if varargin{1}(1) == '-'
    switches = varargin{1};
    for i=2:length(varargin)
      urls = strvcat(urls, splitUrls(varargin{i}));
    end
  else
    switches = '';
    for i=1:length(varargin)
      urls = strvcat(urls, splitUrls(varargin{i}));
    end
  end
  
  % Check for all the possible switches.  Unidentified switches are
  % ignored.
  
  if ~isempty(findstr('-A', switches))
    getData = 0;
    getDAS = 1;
  else
    getData = 1;
    getDAS = 0;
  end

  if ~isempty(findstr('-s', switches))
    getNames = 1;
    getData = 0;
    getDAS = 0;
  else
    getNames = 0;
  end

  if ~isempty(findstr('-S', switches))
    flatten = 0;
  else
    flatten = 1;
  end
  
  if ~isempty(findstr('-F', switches))
    stringToFloat = 1;
  else
    stringToFloat = 0;
  end
  
  if ~isempty(findstr('-e', switches))
    newErrorScheme = 1;
  else
    newErrorScheme = 0;
  end
  
  % This doesn't do anything yet
  if ~isempty(findstr('-k', switches))
    if flatten == 1
      error('-k cannot be used with -S');
    end
    concatenate = 1;
  else
    concatenate = 0;
  end
  
end  
   
% Make sure there's an output argument if we're in one of the modes
% that needs it.

if nargout == 0
  if getDAS | getNames | ~flatten
    dodsError('You must provide an output argument for this option', ...
	      newErrorScheme);
    return;
  end
else
  if size(urls, 1) ~= nargout
    dodsError('Incorrect number of output arguments', newErrorScheme);
    return;
  end
end


for i=1:size(urls,1)
  [url, ce, nameMap, dimOrder] = parseUrlOptions(urls(i,:));

  dodsObject = dods.clients.matlab.MatlabDods(url);
  
  if getData == 1
    % Get  the data
    try,
      dodsObject.downloadData(ce),
    catch,
      dodsError(lasterr, newErrorScheme),
      return,
    end
    
    disp('Finished Downloading the data');
    
    dodsVars = dodsObject.getVariables;
    data = struct([]);
    
    % Convert each top-level dods variable into a matlab variable
    if dodsVars.hasMoreElements == 1
      dodsVar = dodsVars.nextElement;
      try,
	if flatten == 0,
	  data = struct(fixName(dodsVar.getName, nameMap), ...
			processVar(dodsVar, nameMap, stringToFloat, ...
				   dimOrder));,
	else,
	  [data] = processVarFlat(dodsVar, nameMap, data, stringToFloat, ...
				  dimOrder);,
	end,
      catch,
	dodsError(lasterr, newErrorScheme),
	return,
      end
    end
    
    while dodsVars.hasMoreElements == 1
      dodsVar = dodsVars.nextElement;
      try,
	if flatten == 0,
	  data = setfield(data, fixName(dodsVar.getName, nameMap), ...
				processVar(dodsVar, nameMap, ...
					   stringToFloat, dimOrder));,
	else,
	  [data] = processVarFlat(dodsVar, nameMap, data, stringToFloat, dimOrder);,
	end
      catch,
	dodsError(lasterr, newErrorScheme),
	return,
      end
    end
    

    if flatten == 0
      % If the data doesn't need to be flattened, just set
      % varargout equal to it
      
      varargout{i} = data;
    else
      % Otherwise, go through the fields of data and create a
      % variable in the workspace for each field.      
      
      varNames = fieldnames(data);
      if(nargout >= 1)
	varargout{i} = getfield(data, varNames{1});
      else
	for i=1:length(varNames)
	  
	  fieldData = getfield(data, varNames{i});
	  fieldSize = size(fieldData);
	  output = 'Creating ';
	  if length(fieldSize) == 2 & (fieldSize(1) == 1 | fieldSize(2) == 1)
	    output = [ output 'vector ' varNames{i} ' ('];
	  else
	    output = [ output 'matrix ' varNames{i} ' ('];
	  end
	  
	  for j=1:length(fieldSize)-1
	    output = sprintf('%s%dx',output,fieldSize(j));
	  end
	  output = sprintf('%s%d)',output,fieldSize(end));
	  
	  disp(cat(2, output, '.'));
	  assignin('base', varNames{i}, fieldData);
	end
      end
    end
    
  end
  
  if getDAS == 1
    % Get the attributes
    
    try,
      dodsObject.downloadDAS,
    catch,
      dodsError(lasterr, newErrorScheme),
      return,
    end
    
    attrNames = dodsObject.getAttrTableNames;
    
    if attrNames.hasMoreElements == 1
      attrName = attrNames.nextElement;
      dodsAttr = dodsObject.getAttrTable(attrName);
      data = struct(attrName, processAttrTable(dodsAttr, nameMap));
    end
    
    while attrNames.hasMoreElements == 1
      attrName = attrNames.nextElement;
      dodsAttr = dodsObject.getAttrTable(attrName);
      data = setfield(data, attrName, processAttrTable(dodsAttr, nameMap));
    end
    
    varargout{i} = data;
  end
  
  if getNames == 1
    % Get the variable names
    
    try,
      dodsObject.downloadDDS,
    catch,
      dodsError(lasterr, newErrorScheme),
      return,
    end
      
    dodsVars = dodsObject.getVariables;
    
    varargout{i} = '';
    while dodsVars.hasMoreElements == 1
      dodsVar = dodsVars.nextElement;
      varargout{i} = strvcat(varargout{i}, getVarNames(dodsVar, nameMap));
    end
  end
end 







