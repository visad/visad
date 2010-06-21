function [attr] = processAttrTable(attrTable, nameMap)

%
% Given a dods attribute table, this returns a structure with
% fields containing the values of the attributes in the table
%

attrNames = attrTable.getNames;

if attrNames.hasMoreElements == 1
  attrName = fixName(attrNames.nextElement, nameMap);
  subAttr = attrTable.getAttribute(attrName);

  % getAttribute won't return anything useful if the attribute has
  % non-string data, so check to make sure subAttr is valid before
  % proccesing it
  if isjava(subAttr) == 1;
    attr = struct(attrName, processAttr(subAttr, nameMap));
  else
    attr = struct(attrName, '');
  end
  
else
  attr = '';
end

while attrNames.hasMoreElements == 1
  attrName = fixName(attrNames.nextElement, nameMap);
  subAttr = attrTable.getAttribute(attrName);
  
  if isjava(subAttr) == 1
    attr = setfield(attr, attrName, processAttr(subAttr, nameMap));
  else 
    attr = setfield(attr, attrName, '');
  end
  
end

