function [attr] = processAttr(dodsAttr, nameMap)

%
% For a dods attribute: returns a two-dimensional character array
%                       containing the values of the attribute
% For a dods attribute table: returns a structure containing a
%                             field for each attribute in the table
%                             (with the data for each attribute inside)
%

if dodsAttr.isContainer == 1
  attrTable = dodsAttr.getContainer;
  attr = processAttrTable(attrTable, nameMap);
  
else
  attrValues = dodsAttr.getValues;
  attr = '';
  
  while attrValues.hasMoreElements == 1
    attr = strvcat(attr, attrValues.nextElement);
  end
  
end
