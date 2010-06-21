function charArray = cellArrayOfStringsToCharArray(cellArray)

if iscell(cellArray)
  max = 0;
	
  for i=1:size(cellArray,1)
    if length(cellArray{i}) > max
      max = length(cellArray{i});
    end
  end
  
  charArray = repmat(blanks(max), size(cellArray, 1), 1);
  
  for i=1:size(cellArray,1)
    charArray(i,1:length(cellArray{i})) = cellArray{i};
  end
else
  charArray = cellArray;
end
