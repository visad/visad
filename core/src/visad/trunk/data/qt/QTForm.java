//
// QTForm.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2000 Bill Hibbard, Curtis Rueden, Tom
Rink, Dave Glowacki, Steve Emmerson, Tom Whittaker, Don Murray, and
Tommy Jasmin.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Library General Public
License as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Library General Public License for more details.

You should have received a copy of the GNU Library General Public
License along with this library; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
MA 02111-1307, USA
*/

package visad.data.qt;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.rmi.RemoteException;
import javax.swing.*;
import visad.*;
import visad.data.*;
import visad.util.DataUtility;

// CTR: To be replaced with calls via reflection
import quicktime.QTSession;
import quicktime.app.display.QTCanvas;
import quicktime.app.image.ImageDataSequence;
import quicktime.app.image.ImageUtil;
import quicktime.app.image.JImagePainter;
import quicktime.app.image.QTImageDrawer;
import quicktime.app.image.QTImageProducer;
import quicktime.app.image.Redrawable;
import quicktime.app.players.MoviePlayer;
import quicktime.io.OpenMovieFile;
import quicktime.io.QTFile;
import quicktime.qd.QDDimension;
import quicktime.qd.QDGraphics;
import quicktime.qd.QDRect;
import quicktime.std.StdQTConstants;
import quicktime.std.image.CodecComponent;
import quicktime.std.image.CompressedFrameInfo;
import quicktime.std.image.CSequence;
import quicktime.std.image.ImageDescription;
import quicktime.std.image.QTImage;
import quicktime.std.movies.Movie;
import quicktime.std.movies.Track;
import quicktime.std.movies.media.VideoMedia;
import quicktime.util.QTHandle;
import quicktime.util.RawEncodedImage;

/**
 * QTForm is the VisAD data form for QuickTime movie files.
 * To use it, QuickTime for Java must be installed.
 *
 * Much of this form's code was adapted from Wayne Rasband's
 * QuickTime Movie Opener and QuickTime Movie Writer plugins for
 * ImageJ (available at http://rsb.info.nih.gov/ij/).
 */
public class QTForm extends Form implements FormFileInformer, StdQTConstants {

  private static int num = 0;

  private static final String[] suffixes = { "mov" };

  private static final String noQTmsg = "You need to install " +
    "QuickTime for Java from http://www.apple.com/quicktime/";

  private static boolean noQT = false;

  /** Constructs a new QuickTime movie file form. */
  public QTForm() {
    super("QTForm" + num++);
  }

  /** Checks if the given string is a valid filename for a QuickTime movie. */
  public boolean isThisType(String name) {
    if (noQT) return false;
    for (int i=0; i<suffixes.length; i++) {
      if (name.toLowerCase().endsWith(suffixes[i])) return true;
    }
    return false;
  }

  /** Checks if the given block is a valid header for a QuickTime movie. */
  public boolean isThisType(byte[] block) {
    return false;
  }

  /** Returns the default file suffixes for the QuickTime movie formats. */
  public String[] getDefaultSuffixes() {
    String[] s = new String[suffixes.length];
    System.arraycopy(suffixes, 0, s, 0, suffixes.length);
    return s;
  }

  private void setCurrentFrame(int frame, BufferedImage buffer, FlatField[] fields) {
    Image img = DataUtility.extractImage(fields[frame], true);
    if (img != null) {
      Graphics g = buffer.getGraphics();
      g.drawImage(img, 0, 0, null);
      g.dispose();
    }
  }

  /** Saves a VisAD Data object to a QuickTime movie at the given location. */
  public void save(String id, Data data, boolean replace)
    throws BadFormException, IOException, RemoteException, VisADException
  {
    if (noQT) throw new BadFormException(noQTmsg);
    try {
      // extract image frames from data
      FlatField[] fields = DataUtility.getImageFields(data);
      int numFrames = fields.length;
      Gridded2DSet set = (Gridded2DSet) fields[0].getDomainSet();
      int[] lengths = set.getLengths();
      int kWidth = lengths[0];
      int kHeight = lengths[1];
      QTSession.open();

//BEGIN
      QTCanvas canv = new QTCanvas(QTCanvas.kInitialSize, 0.5F, 0.5F);
      JFrame frame = new JFrame();
      JPanel pane = new JPanel();
      frame.setContentPane(pane);
      pane.add("Center", canv);
      BufferedImage buffer = new BufferedImage(kWidth, kHeight, BufferedImage.TYPE_INT_RGB);
      JImagePainter ip = new JImagePainter(buffer);
      QTImageDrawer qid = new QTImageDrawer(ip, new Dimension(kWidth, kHeight), Redrawable.kMultiFrame);
      qid.setRedrawing(true);
      canv.setClient(qid, true);
      frame.pack();

//--makeMovie
      // create movie file & empty movie
      File file = new File(id);
      QTFile f = new QTFile(file.getAbsolutePath());
      Movie theMovie = Movie.createMovieFile(f, kMoviePlayer, createMovieFileDeleteCurFile | createMovieFileDontCreateResFile);

      // add content
//--start addVideoTrack--
      int kNoVolume  = 0;
      int kVidTimeScale = 600;

      Track vidTrack = theMovie.addTrack(kWidth, kHeight, kNoVolume);
      VideoMedia vidMedia = new VideoMedia(vidTrack, kVidTimeScale);  

      vidMedia.beginEdits();
//--start addVideoSample--
      QDRect rect = new QDRect(kWidth, kHeight);
      QDGraphics gw = new QDGraphics(rect);
      int size = QTImage.getMaxCompressionSize(gw, rect, gw.getPixMap().getPixelSize(), codecNormalQuality, kAnimationCodecType, CodecComponent.anyCodec);
      QTHandle imageHandle = new QTHandle(size, true);
      imageHandle.lock();
      RawEncodedImage compressedImage = RawEncodedImage.fromQTHandle(imageHandle);
      CSequence seq = new CSequence(gw,
                      rect, 
                      gw.getPixMap().getPixelSize(),
                      kAnimationCodecType, 
                      CodecComponent.bestFidelityCodec,
                      codecNormalQuality, 
                      codecNormalQuality, 
                      numFrames,  //1 key frame
                      null, //cTab,
                      0);
      ImageDescription desc = seq.getDescription();

      // redraw first...
      setCurrentFrame(0, buffer, fields);
      qid.redraw(null);

      qid.setGWorld(gw);
      qid.setDisplayBounds(rect);

      for (int curSample=0; curSample<numFrames; curSample++) {
        setCurrentFrame(curSample, buffer, fields);
        qid.redraw(null);
        CompressedFrameInfo info = seq.compressFrame(gw, rect, codecFlagUpdatePrevious, compressedImage);
        boolean isKeyFrame = info.getSimilarity() == 0;
        vidMedia.addSample(imageHandle, 
          0, // dataOffset,
          info.getDataSize(),
          60, // frameDuration, 60/600 = 1/10 of a second, desired time per frame  
          desc,
          1, // one sample
          (isKeyFrame ? 0 : mediaSampleNotSync)); // no flags
       }

      // redraw after finishing...
      qid.setGWorld(canv.getPort());
      qid.redraw(null);
//--end addVideoSample--
      vidMedia.endEdits();

      int kTrackStart = 0;
      int kMediaTime = 0;
      int kMediaRate = 1;
      vidTrack.insertMedia(kTrackStart, kMediaTime, vidMedia.getDuration(), kMediaRate);
//--end addVideoTrack--

      // save movie to file
      OpenMovieFile outStream = OpenMovieFile.asWrite(f);
      theMovie.addResource( outStream, movieInDataForkResID, f.getName() );
      outStream.close();
//END

      QTSession.close();
    }
    catch (Exception exc) {
      QTSession.close();
      throw new BadFormException("Save movie failed: " + exc.getMessage());
    }
  }

  /**
   * Adds data to an existing QuickTime movie.
   *
   * @exception BadFormException Always thrown (method is not implemented).
   */
  public void add(String id, Data data, boolean replace)
    throws BadFormException
  {
    throw new BadFormException("QTForm.add");
  }

  /**
   * Opens an existing QuickTime movie from the given location.
   *
   * @return VisAD Data object containing QuickTime data.
   */
  public DataImpl open(String id)
    throws BadFormException, IOException, VisADException
  {
    if (noQT) throw new BadFormException(noQTmsg);
    int totalFrames;
    FlatField[] fields;
    try {
      QTSession.open();

      // open movie file
      File file = new File(id);
      QTFile qtf = new QTFile(file.getAbsolutePath());
      OpenMovieFile openMovieFile = OpenMovieFile.asRead(qtf);
      Movie m = Movie.fromFile(openMovieFile);

      // find first track with width != soundtrack
      int numTracks = m.getTrackCount();
      int trackMostLikely = 0;
      int trackNum = 0;
      while (++trackNum <= numTracks && trackMostLikely == 0) {
        Track imageTrack = m.getTrack(trackNum);
        QDDimension d = imageTrack.getSize();
        if (d.getWidth() > 0) trackMostLikely = trackNum;
      }
      Track imageTrack = m.getTrack(trackMostLikely);
      QDDimension d = imageTrack.getSize();
      int width = d.getWidth();
      int height = d.getHeight();
      ImageDataSequence seq = ImageUtil.createSequence(imageTrack);
      totalFrames = seq.size();

      // now use controller to step movie
      MoviePlayer moviePlayer = new MoviePlayer(m);
      QTImageProducer qtip = new QTImageProducer(moviePlayer, new Dimension(width, height));
      Image img = Toolkit.getDefaultToolkit().createImage(qtip);
      boolean needsRedrawing = qtip.isRedrawing();
      int maxTime = m.getDuration();
      int timeStep = maxTime / totalFrames;
      fields = new FlatField[totalFrames];
      for (int i=0; i<totalFrames; i++) {
        // paint next frame into image
        moviePlayer.setTime(timeStep * i);
        if (needsRedrawing) qtip.redraw(null);
        qtip.updateConsumers(null);

        // convert image to VisAD Data object
        fields[i] = DataUtility.makeField(img);
      }

      openMovieFile.close();
      QTSession.close();
    }
    catch (Exception e) {
      QTSession.close();
      throw new BadFormException("Open movie failed: " + e.getMessage());
    }

    if (totalFrames == 1) return fields[0];
    else {
      // combine data stack into time function
      RealType time = RealType.getRealType("time");
      FunctionType time_function = new FunctionType(time, fields[0].getType());
      Integer1DSet time_set = new Integer1DSet(totalFrames);
      FieldImpl time_field = new FieldImpl(time_function, time_set);
      time_field.setSamples(fields, false);
      return time_field;
    }
  }

  /**
   * Opens an existing QuickTime movie from the given URL.
   *
   * @exception BadFormException Always thrown (method is not implemented).
   */
  public DataImpl open(URL url)
    throws BadFormException, IOException, VisADException
  {
    throw new BadFormException("QTForm.open(URL)");
  }

  public FormNode getForms(Data data) {
    return null;
  }

  /**
   * Run 'java visad.data.qt.QTForm in_file out_file' to convert
   * in_file to out_file in QuickTime movie format.
   */
  public static void main(String[] args)
    throws VisADException, RemoteException, IOException
  {
    if (args == null || args.length < 1 || args.length > 2) {
      System.out.println("To convert a file to QuickTime, run:");
      System.out.println("  java visad.data.qt.QTForm in_file out_file");
      System.out.println("To test read a QuickTime file, run:");
      System.out.println("  java visad.data.qt.QTForm in_file");
      System.exit(2);
    }

    if (args.length == 1) {
      // Test read QuickTime movie
      QTForm form = new QTForm();
      System.out.print("Reading " + args[0] + " ");
      Data data = form.open(args[0]);
      System.out.println("[done]");
      System.out.println("MathType =\n" + data.getType().prettyString());
    }
    else if (args.length == 2) {
      // Convert file to QuickTime format
      System.out.print(args[0] + " -> " + args[1] + " ");
      DefaultFamily loader = new DefaultFamily("loader");
      DataImpl data = loader.open(args[0]);
      loader = null;
      QTForm form = new QTForm();
      form.save(args[1], data, true);
      System.out.println("[done]");
    }
  }

}
