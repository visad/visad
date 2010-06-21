function [name] = fixName(string, nameMap)

%
% Converts a string into a value suitable to use as a field in a
% structure.  Also performs an requested name substitution.
%

if isjava(string) == 1
  name = string.toCharArray;
else
  name = string;
end

if size(name,1) > 1
  name = reshape(name, 1, size(name, 1));
end

if isjava(nameMap) == 1
  hashedName = nameMap.get(string);
  
  if length(hashedName) ~= 0
    name = hashedName;
  else
    % only keep a-z, A-Z, 0-9, and _
    newName = name(find( isletter(name) | name == '_' ... 
		      | (double(name) >= 48 & double(name) <= 57 )));
    
    % make sure the first char is a letter
    while ~isletter(newName(1));
      newName = newName(2:end);
    end
    if length(newName) > 31
      newName = newName(1:31);
    end
    
    nameMap.put(name, newName);
    name = newName;
  end

else

  name = name(find( isletter(name) | name == '_' ... 
	    | (double(name) >= 48 & double(name) <= 57 )));
  
  while ~isletter(name(1))
    name = name(2:end);
  end
  
  if length(name) > 31
    name = name(1:31);
  end
end

