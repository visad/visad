function dodsError(message, errorScheme)

%
% This function will report an error either using the traditional
% error() function, or by setting the variables 'dods_err' and
% 'dods_err_message' in the matlab workspace.
%
% INPUTS:
%  message         - The error message
%  newErrorScheme  - Which way the error should be reported (1 to
%                    set the variables, anything else to use error())
%

if errorScheme == 1
  assignin('base', 'dods_err', 1);
  assignin('base', 'dods_err_message', message);
else
  error(message);
end

