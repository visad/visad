function [urls] = splitUrls(urlString)

% Splits up a string of urls seperated by whitespace into an array
% of individual urls.  Per-url options are left at the end of the
% url to be parsed later.
%
% USAGE:    [urls] = splitUrls(urlString)
%

inds = findstr(urlString, 'http://');
lastInd = 1;
urls = '';

for i=2:length(inds)
  urls = strvcat(urls, urlString(lastInd:inds(i)-1));
  lastInd = inds(i);
end

urls = strvcat(urls, urlString(lastInd:end));
