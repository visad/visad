function [baseUrl, ce, nameMap, dimOrder] = parseUrlOptions(url)

url = deblank(url);

inds = findstr(url, ' ');

if mod(size(inds, 2), 2) ~= 0
  error('Error in per-url options');
end

if size(inds, 2) > 1
  baseUrl = url(1:inds(1)-1);
else
  baseUrl = url;
end

nameMap = 0;
ce = '';
dimOrder = 'matlab';

i = 1;
while i < size(inds, 2)

  option = url(inds(i)+1:inds(i+1)-1);
  
  if i+1 < size(inds, 2)
    value = url(inds(i+1)+1:inds(i+2)-1);
  else
    value = url(inds(i+1)+1:end);
  end
  
  switch option
   case '-c'
    ce = value;
    if ce(1) ~= '?'
      ce = [ '?' ce ];
    end
    
   case '-r'
    colon = findstr(value, ':');
    if size(colon,2) > 0
      if nameMap == 0
	nameMap = java.util.Hashtable(10);
      end
      nameMap.put(value(1:colon(1)-1),value(colon(1)+1:end));
    else
      error('value for -r must take form <old_name>:<new_name>');
      return;
      
    end
    
   case '-d'
    if ~isempty(strmatch(value,{'matlab', 'dods', 'reverse'}))
      dimOrder = value;
    else
      error([ 'unrecognized ordering scheme ' value ]);
    end
    
   otherwise
    error([ 'unrecognized per-url option: ' option ]);
    return
    
  end
  
  i = i+2;
end
