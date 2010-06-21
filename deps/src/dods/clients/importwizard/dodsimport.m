% DESCRIPTION: 
% 
%  An interface which allows the user to search for dods urls,
%  select urls for certain dates from an inventory, and select
%  certain variables from a dataset.  From this, it creates
%  constrained urls which it can download into the matlab workspace.
%
% USAGE:
%  dodsimport (there are no parameters or return variables at this time)
%

import dods.clients.importwizard.*;

% Check that we have matlab 6 and that java is enabled.
ver = version;
verMajor = str2num(ver(1));

if verMajor <= 5
  disp('This program needs matlab version 6 or higher to run.');
else
  
  version -java;
  
  if strcmp(ans, 'Java is not enabled') == 0

    % Create the import wizard globally so the callback functions
    % can see it.
    global importWizard;
    importWizard = DodsImport;

    % Setup a callback function to import the data into matlab when
    % the user hits the finish button.
    button = importWizard.getFinishButton;
    set(button, 'ActionPerformedCallback', {@doFinish});

    % Set the window size and display it.
    importWizard.setBounds(50,50,700,400);
    importWizard.setVisible(1);
  else
    disp([ 'You must have java enabled to use this program. ' ...
	   'If you want to avoid the overhead of the matlab desktop' ...
	   ' interface, try matlab -nodesktop as opposed to matlab' ...
	   ' -nojvm ' ]);
  end
end

