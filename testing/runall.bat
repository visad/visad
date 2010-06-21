@echo Run all VisAD Tests
@set JAVA=c:\jdk1.5.0\jre
@set path=%JAVA%\bin;%path%
@set CLASSPATH=..\visad_examples.jar;..\visad.jar;.\
@echo Starting rmiregistry
@start rmiregistry

@echo test of VisADCanvasJ3D
@java visad.java3d.VisADCanvasJ3D

@echo test of FlatField
@java visad.FlatField

@echo test of VerySimple
@java VerySimple

@echo Test the SpreadSheet (still in the examples directory):
@echo.
@echo in cell A1, import b2rlc.nc and verify the display
@echo is an animated spectrum
@echo.
@echo edit the mappings to clear the map Time -> Animation
@echo and add the map Time -> ZAxis, then verify the display
@echo is a sequence of spectra along the ZAxis
@echo.
@echo in cell B1, import images.nc and verify the display
@echo is an animated image with a missing line in the second
@echo image
@echo.
@echo edit the mappings to add the map val -> ZAxis, then
@echo verify the display is an animated terrain image
@echo. 
@echo delete the data from cells A1 and B1
@echo.
@echo in cell A1, import copy.gif from the ../ss directory
@echo verify the display of the copy icon
@echo.
@echo in cell B1, import cut.gif from the same directory
@echo verify the display of the cut icon
@echo.
@echo in cell A2, enter the formula 'A1 - B1' and verify the
@echo display their difference
@echo.
@echo delete the data from cell B1 and verify the error message
@echo cell A2
@echo.
@echo. in cell B1, import del.gif and verify the difference
@echo in cell A2

@java visad.ss.SpreadSheet

@echo Now running test cases

@echo java DisplayTest 0

@echo Two 3-D displays should come up; drag both points and the
@echo V-shaped line in each (note you will need to click the left
@echo button for direct manipluation in the right display), and
@echo verify the same changes in the other 3-D display; verify
@echo that coordinates are displayed in the left display but not
@echo the right
@java DisplayTest 0
@echo.
@echo.
@echo java DisplayTest 1
@echo.
@echo Drag the vis_radiance iso-level and verify a colored sphere;
@echo Play with the color widget and verify color changes in the
@echo iso-surface
@java DisplayTest 1
@echo.
@echo java DisplayTest 2
@echo.
@echo Drag the vis_radiance iso-level and verify an irregular
@echo colored sphere; play with the color widget and verify color
@echo changes in the iso-surface
@java DisplayTest 2
@echo.
@echo java DisplayTest 3
@echo.
@echo Rotate the 3-D display a bit, then animate and verify that
@echo the plane takes 6 steps and the cone takes only 4 steps
@java DisplayTest 3
@echo.
@echo java DisplayTest 4
@echo.
@echo Verify display of a bull's eye color image on the surface
@echo of a sphere
@java DisplayTest 4
@echo.
@echo java DisplayTest 5
@echo.
@echo Verify display of a contour bull's eye on a slanted plane;
@echo enable and verify labels
@java DisplayTest 5
@echo.
@echo java DisplayTest 5 1
@echo.
@echo Verify display of an unevenlyr-spaced contour bull's
@echo eye on a slanted plane, with labels
@java DisplayTest 5 1
@echo.
@echo java DisplayTest 6
@echo.
@echo Verify display of a contour bull's eye on a slanted plane
@java DisplayTest 6
@echo.
@echo java DisplayTest 6 1
@echo.
@echo Verify display of an unevenlyr-spaced contour bull's
@echo eye on a slanted plane
@java DisplayTest 6 1
@echo.
@echo java DisplayTest 7
@echo.
@echo Verify color bull's eye opaque at top gradually increasing
@echo transparency down
@java DisplayTest 7
@echo.
@echo java DisplayTest 8
@echo.
@echo Verify warped cone (cone + slanted plane)
@java DisplayTest 8
@echo.
@echo java DisplayTest 9 bill.gif
@echo.
@echo Verify GIF image display (may use any simple GIF file) 
@java DisplayTest 9 bill.gif
@echo.
@echo java DisplayTest 10 bill.nc
@echo.
@echo Verify netCDF image display as a terrain (may use any
@echo netCDF file generated from a GIF file - easiest way is
@echo oo read a GIF into the SpreadSheet and save as netCDF)
@java DisplayTest 10 bill.nc
@echo.
@echo java DisplayTest 11
@echo.
@echo Verify bull's eye image warped in partial arc of circle
@java DisplayTest 11
@echo.
@echo java DisplayTest 12
@echo.
@echo Edit red, green and blue curves in color widget, clicking
@echo apply after each; then Undo and Apply, Grey Scale and Apply,
@echo finally Reset and Apply; verify proper colr change in
@echo display after each Apply
@java DisplayTest 12
@echo.
@echo java DisplayTest 12 1
@echo.
@echo Click left mouse button in widget, then quickly drag
@echo cursor back and forth across color widget making sure to
@echo run well off left and right edges as color table size
@echo increases to 1024 and then decreases to 256
@java DisplayTest 12 1
@echo.
@echo java DisplayTest 13
@echo.
@echo Verify three Exception messages in display
@java DisplayTest 13
@echo.
@echo java DisplayTest 14
@echo.
@echo Wait for the display to pop up with two points and a V-shaped
@echo line, rotate the view to the left or right, then in another
@echo window run:
@start java DisplayTest 14
@echo Wait and then hit enter...
@pause
@echo.
@echo java DisplayTest 15
@echo.
@echo Now there should be displays for both the '14' and '15'
@echo commands; in each display drag both points and the V-shaped
@echo line and verify that they move in both displays; then shut
@echo kown both the '14' and '15' commands
@java DisplayTest 15
@echo.
@echo java DisplayTest 16
@echo.
@echo Verify an opaque color bull's eye image
@java DisplayTest 16
@echo.
@echo java DisplayTest 17
@echo.
@echo Verify a color bull's eye image with constant transparency
@java DisplayTest 17
@echo.
@echo java DisplayTest 18
@echo.
@echo Rotate display a bit left or right; verify slanted planes in
@echo all 6 animation steps, but cones in only the first four
@echo animation steps
@java DisplayTest 18
@echo.
@echo java DisplayTest 19
@echo.
@echo Rotate display a bit left or right; drag value slider and
@echo verify that slanted planes and cones change in display
@java DisplayTest 19
@echo.
@echo java DisplayTest 20
@echo.
@echo Edit red, green, blue and alpha curves in color-alpha
@echo widget and verify display (especially transparency);
@echo verify that Grey Scale and Reset work right
@java DisplayTest 20
@echo.
@echo java DisplayTest 21
@echo.
@echo Drag in both ends of select range widget, then drag whole range
@echo just click in middle of range); verify range selections in
@echo display
@java DisplayTest 21
@echo.
@echo java DisplayTest 22
@echo.
@echo Verify correct coloring of image (white along bottom edge, hue
@echo rainbow along top)
@java DisplayTest 22
@echo.
@echo java DisplayTest 23
@echo.
@echo Verify correct coloring of image (roughly green top left, blue
@echo top right, yellow bottom left, red bottom right)
@java DisplayTest 23
@echo.
@echo java DisplayTest 24
@echo.
@echo Verify correct coloring of image (color bull's eye from brown
@echo through red to white inside)
@java DisplayTest 24
@echo.
@echo java DisplayTest 25
@echo.
@echo Verify correct coloring of image (color bull's eye from cyan
@echo through purple to yellow inside)
@java DisplayTest 25
@echo.
@echo java DisplayTest 26
@echo.
@echo Rotate display up, verify flipping and shrinking cone and
@echo verify scale labels
@java DisplayTest 26
@echo.
@echo java DisplayTest 27
@echo.
@echo When yellow cross hair cursors appear in display, drag them
@echo with right mouse button and verify that cone rescales
@java DisplayTest 27
@echo.
@echo java DisplayTest 28
@echo.
@echo Verify arror vector field (point right along bottom, up in
@echo the middle, and up and right on top and along left and right
@echo edges)
@java DisplayTest 28
@echo.
@echo java DisplayTest 29
@echo.
@echo Verify irregular cone
@java DisplayTest 29
@echo.
@echo java DisplayTest 30
@echo.
@echo Verify stack of images and axis scale with time labels
@java DisplayTest 30
@echo.
@echo java DisplayTest 31
@echo.
@echo Verify scatter diagram, rotate up and verify points lie
@echo on two V-shaped planes
@java DisplayTest 31
@echo.
@echo java DisplayTest 32 ngc1316o.fits
@echo.
@echo Verify astronomy image display (data available from
@echo ftp://www.ssec.wisc.edu/pub/visad/ngc1316o.fits)
@java DisplayTest 32 ngc1316o.fits
@echo.
@echo java DisplayTest 33
@echo.
@echo Edit red, green and blue curves in color widget and
@echo verify color changes in display; then verify Grey Scale
@echo and verify that Reset restores original linear color
@echo curves
@java DisplayTest 33
@echo.
@echo java DisplayTest 34
@echo.
@echo In left display, drag both points and part of V-shaped line
@echo until they disappear outside box; then in right display drag
@echo them back into box and verify they reappear in left display
@java DisplayTest 34
@echo.
@echo java DisplayTest 35
@echo.
@echo In left, 3-D display, drag both points and part of V-shaped
@echo line until they disappear outside box; then in right, 2-D
@echo display drag them back into box and verify they reappear in
@echo left display
@java DisplayTest 35
@echo.
@echo java DisplayTest 36
@echo.
@echo Verify color bull's eye warped in arc of circle
@java DisplayTest 36
@echo.
@echo java DisplayTest 37
@echo.
@echo Verify contour warped bull's eye; verify fill; verify labels
@java DisplayTest 37
@echo.
@echo.
@echo java DisplayTest 38
@echo.
@echo Verify bull's eye contours
@java DisplayTest 38
@echo.
@echo.
@echo java DisplayTest 39
@echo.
@echo Edit red, green and blue curves in color widget and verify
@echo color changes in display
@java DisplayTest 39
@echo.
@echo.
@echo java DisplayTest 40
@echo.
@echo Redraw spiral V-shaped curve in left display and verify change
@echo in right display, then redraw in right display and verify
@echo change in left display
@java DisplayTest 40
@echo.
@echo.
@echo java DisplayTest 41
@echo.
@echo Verify alignment of contours and colors in left and right
@echo displays
@java DisplayTest 41
@echo.
@echo java DisplayTest 42
@echo.
@echo Verify alignment of contours and colors in left and right
@echo displays; pan and zoom left display and verify correct
@echo changes to axis scales
@java DisplayTest 42
@echo.
@echo java DisplayTest 43
@echo.
@echo Verify that right display is the x-derivative of the left
@echo display
@java DisplayTest 43
@echo.
@echo java DisplayTest 44
@echo.
@echo Verify text sloping slightly down to the right
@java DisplayTest 44
@echo.
@echo java DisplayTest 45
@echo.
@echo Verify text
@java DisplayTest 45
@echo.
@echo java DisplayTest 45 1
@echo.
@echo Verify text on sphere
@java DisplayTest 45 1
@echo.
@echo java DisplayTest 46
@echo.
@echo Verify shapes: 3 solid triangles in open squares, 2 squares
@echo with diagonal crosses, in various colors
@java DisplayTest 46
@echo.
@echo java DisplayTest 47
@echo.
@echo Verify shapes: point, cube, X, cube, 1.2 and cube, small to
@echo large, in various colors
@java DisplayTest 47
@echo.
@echo java DisplayTest 48
@echo.
@echo Verify green cone
@java DisplayTest 48
@echo.
@echo java DisplayTest 49
@echo.
@echo Verify green V-shaped line with breaks
@java DisplayTest 49
@echo.
@echo. java DisplayTest 50
@echo.
@echo Move top frame, then pan bottom display and verify that top
@echo frame tracks once per second
@java DisplayTest 50
@echo.
@echo java DisplayTest 51
@echo.
@echo Move top frame, then rotate bottom display and verify that
@echo top frame tracks once per second
@java DisplayTest 51
@echo.
@echo java DisplayTest 52
@echo.
@echo Verify color bull's eye
@java DisplayTest 52
@echo.
@echo java DisplayTest 53
@echo.
@echo Click center mouse button in display and verify changes
@echo to box, cursor and backgroup colors, and box off
@java DisplayTest 53
@echo.
@echo java DisplayTest 54
@echo.
@echo Click center mouse button in display and verify changes
@echo to box, cursor and backgroup colors
@java DisplayTest 54
@echo.
@echo java DisplayTest 55
@echo.
@echo Wait for the display to pop up with two points and a V-shaped
@echo line, then in another window run (substitute the IP name of
@echo your machine for "demedici"):
@start java DisplayTest 55
@pause
@echo.
@echo java DisplayTest 56 demedici
@echo.
@echo Now there should be displays for both the '55' and '56'
@echo commands; in each display drag both points and the V-shaped
@echo line and verify that they move in both displays; then shut
@echo down both the '55' and '56' commands
@java DisplayTest 56 volante
@echo.
@echo java DisplayTest 57
@echo.
@echo Verify 3-D box and cone with non-square aspect rotating
@java DisplayTest 57
@echo.
@echo java DisplayTest 58
@echo.
@echo Verify box and image with non-square aspect rotating
@java DisplayTest 58
@echo.
@echo java DisplayTest 59
@echo.
@echo Drag contour level and verify irregular spherical isosurface
@java DisplayTest 59
@echo.
@echo java DisplayTest 60
@echo.
@echo Verify irregular contour bull's eye
@java DisplayTest 60
@echo.
@echo java DisplayTest 61
@echo.
@echo Edit red, green, blue and alpha curves in color widget and
@echo verify volume color changes; drag xr, yr and zr ranges and
@echo verify clipping of volume
@java DisplayTest 61
@echo.
@echo java DisplayTest 61 1
@echo.
@echo Edit red, green, blue and alpha curves in color widget and
@echo verify volume color changes; drag xr, yr and zr ranges and
@echo verify clipping of volume
@java DisplayTest 61 1
@echo.
@echo Some of Don's recommendations
@echo.
@java DisplayTest 61 t
@echo.
@java DisplayTest 61 1 t
@echo.
@echo java DisplayTest 62
@echo.
@echo Drag contour level and verify wire frame surface
@java DisplayTest 62
@echo.
@echo java DisplayTest 63
@echo.
@echo Wait for the display to pop up with a cone, then in another
@echo window run (substitute the IP name of your machine for
@echo "demedici"):
@start java DisplayTest 63
@pause
@echo.
@echo java DisplayTest 64 host
@echo.
@echo click left mouse button and drag in new window, and verify
@echo cone rotates in both displays; click and drag center mouse
@echo button and verify cursor coordinates in both displays
@java DisplayTest 64 volante
@echo.
@echo java DisplayTest 63 -2d
@echo.
@echo Wait for the display to pop up with a V-shaped line, then
@echo in another window run (substitute the IP name of your
@echo machine for "demedici"):
@start java DisplayTest 63 -2d
@pause
@echo.
@echo java DisplayTest 64 host
@echo.
@echo Click left mouse button and drag in new window, and verify
@echo cone rotates in both displays; click and drag center mouse
@echo button and verify cursor coordinates in both displays
@java DisplayTest 64 volante
@echo.
@echo java DisplayTest 65
@echo.
@echo Verify widget panel; play with widgets and verify correct
@echo display
@java DisplayTest 65
@echo.
@echo java DisplayTest 66
@echo.
@echo Play with widgets and verify correct displays
@java DisplayTest 66
@echo.
@echo java DisplayTest 67 1
@echo.
@echo Verify blue and red sinusoidal curves in left and right
@echo displays
@java DisplayTest 67 1
@echo.
@echo java DisplayTest 67 2
@echo.
@echo Verify blue and red sinusoidal surfaces in left and right
@echo displays
@java DisplayTest 67 2
@echo.
@echo java DisplayTest 67 3
@echo.
@echo Verify identical point volume patterns in left and right
@echo displays
@java DisplayTest 67 3
@echo.
@echo java DisplayTest 68
@echo.
@echo Wait for the display to pop up with a conical contour
@echo bull's eye, then in another window run:
@echo.
@echo. cd ../browser
@echo. appletviewer viewer_applet.html
@start runapplet.bat
@echo.
@echo When the applet pops up, click "Connect"; click and drag the
@echo left mouse button in the browser display and verify that it
@echo and the original display rotate; modify the "interval" value
@echo in the browser widgets and verify the change in contour
@echo interval in both displays
@java DisplayTest 68
@echo.
@echo.
@echo java DisplayTest 69
@echo.
@echo Verify text using a filled font, and verify special characters
@echo on top row
@java DisplayTest 69
@echo.
@echo java DisplayTest 69 1
@echo.
@echo Verify text using a filled font on a sphere
@java DisplayTest 69 1
@echo.
@echo java DisplayTest 70
@echo.
@echo Verify 5 numbers along diagonal
@java DisplayTest 70
@echo.
@echo java DisplayTest 70 1
@echo.
@echo Verify the 5 numbers along a diagonal around a sphere
@java DisplayTest 70 1
@echo.
@echo java DisplayTest 71 bill.gif aj.gif
@echo.
@echo Use any two GIF images of roughly the same size; verify the
@echo display starts with the first, and switches to the second
@echo whenever it is moving
@java DisplayTest 71 cut.gif copy.gif
@echo.
@echo java DisplayTest 72
@echo.
@echo Verify streamlines on a cone
@java DisplayTest 72
@echo.
@echo.
@echo.
@echo You are done!!!!!
