function orderVector = getOrderVector(dimOrder, numDims)

orderVector = [];

switch dimOrder
 case 'dods'
  for i=1:numDims
    orderVector(i) = numDims + 1 - i;
  end
  
 case 'matlab'
  orderVector = [2 1];
  for i=3:numDims
    orderVector(i) = i;
  end
  
 case 'reverse'
  for i=1:numDims
    orderVector(i) = i;
  end
  
 otherwise
  error([ 'unrecognized ordering scheme ' dimOrder ]);
  
end
