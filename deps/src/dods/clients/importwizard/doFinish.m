function doFinish(obj, evt) 

% This is the callback function called by the finish button in the
% dods import wizard.

import javax.swing.*;
import java.awt.*;
import dods.clients.importwizard.*;

% Get the importWizard which has been declared globally by
% dodsimport.m and retrieve the important information from it.
global importWizard;
urls = importWizard.getURLs;
names = importWizard.getNames;
options = importWizard.getOptions;

% Create a progress window
global progress;
progress = ImportProgressWindow(0,size(names,1));
importWizard.setVisible(0);
progress.setLocation(200,200);
progress.setVisible(1);

% Go through the list of urls, and download each as long as the
% progress window remains visible (hitting 'cancel' will cause the
% window to hide).
for i=1:size(names,1)
    url = char(urls(i));
    if length(char(options)) > 0
        disp([char(names(i)) ' = loaddods(' char(options) ', ' url ')']);
        temp = loaddods(char(options), url);
    else
        disp([char(names(i)) ' = loaddods(' url ')']);
	temp = loaddods(url);
    end
    assignin('base', char(names(i)), temp);
    progress.finishedVar(names(i));
    if progress.isVisible == 0
        break;
    end
end

