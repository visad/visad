//
// QTForm.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2001 Bill Hibbard, Curtis Rueden, Tom
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
import java.awt.image.*;
import java.io.*;
import java.net.URL;
import java.rmi.RemoteException;
import javax.swing.*;
import visad.*;
import visad.data.*;
import visad.util.*;

/**
 * QTForm is the VisAD data form for QuickTime movie files.
 * To use it, QuickTime for Java must be installed.
 *
 * Much of this form's code was adapted from Wayne Rasband's
 * QuickTime Movie Opener and QuickTime Movie Writer plugins for
 * ImageJ (available at http://rsb.info.nih.gov/ij/).
 */
public class QTForm extends Form implements FormFileInformer {

  public static final int FRAME_RATE = 60; // 60/600 = 1/10 of a second

  private static int num = 0;

  private static final String[] suffixes = { "mov" };

  private static final String noQTmsg = "You need to install " +
    "QuickTime for Java from http://www.apple.com/quicktime/";

  private static boolean noQT = false;

  private static ReflectedUniverse r = createReflectedUniverse();

  private static ReflectedUniverse createReflectedUniverse() {
    ReflectedUniverse r = null;
    try {
      r = new ReflectedUniverse();
      r.exec("import quicktime.QTSession");
      r.exec("import quicktime.app.display.QTCanvas");
      r.exec("import quicktime.app.image.ImageDataSequence");
      r.exec("import quicktime.app.image.ImageUtil");
      r.exec("import quicktime.app.image.JImagePainter");
      r.exec("import quicktime.app.image.QTImageDrawer");
      r.exec("import quicktime.app.image.QTImageProducer");
      r.exec("import quicktime.app.image.Redrawable");
      r.exec("import quicktime.app.players.MoviePlayer");
      r.exec("import quicktime.io.OpenMovieFile");
      r.exec("import quicktime.io.QTFile");
      r.exec("import quicktime.qd.QDDimension");
      r.exec("import quicktime.qd.QDGraphics");
      r.exec("import quicktime.qd.QDRect");
      r.exec("import quicktime.std.StdQTConstants");
      r.exec("import quicktime.std.image.CodecComponent");
      r.exec("import quicktime.std.image.CompressedFrameInfo");
      r.exec("import quicktime.std.image.CSequence");
      r.exec("import quicktime.std.image.ImageDescription");
      r.exec("import quicktime.std.image.QTImage");
      r.exec("import quicktime.std.movies.Movie");
      r.exec("import quicktime.std.movies.Track");
      r.exec("import quicktime.std.movies.media.VideoMedia");
      r.exec("import quicktime.util.QTHandle");
      r.exec("import quicktime.util.RawEncodedImage");
    }
    catch (VisADException exc) { noQT = true; }
    return r;
  }

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

  /** Paints the given movie frame into the image buffer. */
  private void setCurrentFrame(int frame,
    BufferedImage buffer, FlatField[] fields)
  {
    Image img = DataUtility.extractImage(fields[frame], false);
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
      r.setVar("numFrames", new Integer(numFrames));
      r.setVar("kWidth", new Integer(kWidth));
      r.setVar("kHeight", new Integer(kHeight));
      r.exec("QTSession.open()");

      // set up QuickTime drawing objects
      r.setVar("oneHalf", new Float(0.5f));
      Component canv = (Component) r.exec(
        "canv = new QTCanvas(QTCanvas.kInitialSize, oneHalf, oneHalf)");
      JFrame frame = new JFrame();
      JPanel pane = new JPanel();
      frame.setContentPane(pane);
      pane.add("Center", canv);
      BufferedImage buffer =
        new BufferedImage(kWidth, kHeight, BufferedImage.TYPE_INT_RGB);
      r.setVar("buffer", buffer);
      r.exec("ip = new JImagePainter(buffer)");
      Dimension dim = new Dimension(kWidth, kHeight);
      r.setVar("dim", dim);
      r.exec("qid = new QTImageDrawer(ip, dim, Redrawable.kMultiFrame)");
      r.setVar("true", new Boolean(true));
      r.exec("qid.setRedrawing(true)");
      r.exec("canv.setClient(qid, true)");
      frame.pack();

      // create movie file & empty movie
      File file = new File(id);
      r.setVar("path", file.getAbsolutePath());
      r.exec("f = new QTFile(path)");
      Integer i1 = (Integer) r.getVar(
        "StdQTConstants.createMovieFileDeleteCurFile");
      Integer i2 = (Integer) r.getVar(
        "StdQTConstants.createMovieFileDontCreateResFile");
      r.setVar("flags", new Integer(i1.intValue() | i2.intValue()));
      r.exec("theMovie = " +
        "Movie.createMovieFile(f, StdQTConstants.kMoviePlayer, flags)");

      // add content
      int kNoVolume = 0;
      r.setVar("kNoVolume", new Integer(kNoVolume));
      r.setVar("kVidTimeScale", new Integer(600));

      r.setVar("fkWidth", new Float(kWidth));
      r.setVar("fkHeight", new Float(kHeight));
      r.setVar("fkNoVolume", new Float(kNoVolume));
      r.exec("vidTrack = theMovie.addTrack(fkWidth, fkHeight, fkNoVolume)");
      r.exec("vidMedia = new VideoMedia(vidTrack, kVidTimeScale)");

      r.exec("vidMedia.beginEdits()");
      r.exec("rect = new QDRect(kWidth, kHeight)");
      r.exec("gw = new QDGraphics(rect)");
      r.exec("pixmap = gw.getPixMap()");
      r.exec("pixsize = pixmap.getPixelSize()");
      r.exec("size = QTImage.getMaxCompressionSize(gw, rect, pixsize, " +
        "StdQTConstants.codecNormalQuality, " +
        "StdQTConstants.kAnimationCodecType, CodecComponent.anyCodec)");
      r.exec("imageHandle = new QTHandle(size, true)");
      r.exec("imageHandle.lock()");
      r.exec("compressedImage = RawEncodedImage.fromQTHandle(imageHandle)");
      r.setVar("zero", new Integer(0));
      r.exec("seq = new CSequence(gw, rect, pixsize, " +
        "StdQTConstants.kAnimationCodecType, " +
        "CodecComponent.bestFidelityCodec, " +
        "StdQTConstants.codecNormalQuality, " +
        "StdQTConstants.codecNormalQuality, numFrames, null, zero)");
      r.exec("desc = seq.getDescription()");

      // redraw first...
      setCurrentFrame(0, buffer, fields);
      r.exec("qid.redraw(null)");

      r.exec("qid.setGWorld(gw)");
      r.exec("qid.setDisplayBounds(rect)");

      for (int curSample=0; curSample<numFrames; curSample++) {
        setCurrentFrame(curSample, buffer, fields);
        r.exec("qid.redraw(null)");
        r.exec("info = seq.compressFrame(gw, rect, " +
          "StdQTConstants.codecFlagUpdatePrevious, compressedImage)");
        Integer sim = (Integer) r.exec("info.getSimilarity()");
        if (sim.intValue() == 0) r.setVar("keyFrame", new Integer(0));
        else {
          r.setVar("keyFrame", r.getVar("StdQTConstants.mediaSampleNotSync"));
        }
        r.exec("dataSize = info.getDataSize()");
        r.setVar("one", new Integer(1));
        r.setVar("frameRate", new Integer(FRAME_RATE));
        r.exec("vidMedia.addSample(imageHandle, zero, dataSize, " +
          "frameRate, desc, one, keyFrame)");
      }

      // redraw after finishing...
      r.exec("port = canv.getPort()");
      r.exec("qid.setGWorld(port)");
      r.exec("qid.redraw(null)");
      r.exec("vidMedia.endEdits()");

      r.setVar("kTrackStart", new Integer(0));
      r.setVar("kMediaTime", new Integer(0));
      r.setVar("kMediaRate", new Float(1.0f));
      r.exec("duration = vidMedia.getDuration()");
      r.exec(
        "vidTrack.insertMedia(kTrackStart, kMediaTime, duration, kMediaRate)");

      // save movie to file
      r.exec("outStream = OpenMovieFile.asWrite(f)");
      r.exec("name = f.getName()");
      r.exec("theMovie.addResource(outStream, " +
        "StdQTConstants.movieInDataForkResID, name)");
      r.exec("outStream.close()");

      r.exec("QTSession.close()");
    }
    catch (Exception exc) {
      r.exec("QTSession.close()");
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
      r.exec("QTSession.open()");

      // open movie file
      File file = new File(id);
      r.setVar("path", file.getAbsolutePath());
      r.exec("qtf = new QTFile(path)");
      r.exec("openMovieFile = OpenMovieFile.asRead(qtf)");
      r.exec("m = Movie.fromFile(openMovieFile)");

      // find first track with width != soundtrack
      int numTracks = ((Integer) r.exec("m.getTrackCount()")).intValue();
      int trackMostLikely = 0;
      int trackNum = 0;
      while (++trackNum <= numTracks && trackMostLikely == 0) {
        r.setVar("trackNum", new Integer(trackNum));
        r.exec("imageTrack = m.getTrack(trackNum)");
        r.exec("d = imageTrack.getSize()");
        Integer w = (Integer) r.exec("d.getWidth()");
        if (w.intValue() > 0) trackMostLikely = trackNum;
      }
      r.setVar("trackMostLikely", new Integer(trackMostLikely));
      r.exec("imageTrack = m.getTrack(trackMostLikely)");
      r.exec("d = imageTrack.getSize()");
      Integer w = (Integer) r.exec("d.getWidth()");
      Integer h = (Integer) r.exec("d.getHeight()");
      r.exec("seq = ImageUtil.createSequence(imageTrack)");
      totalFrames = ((Integer) r.exec("seq.size()")).intValue();

      // now use controller to step movie
      r.exec("moviePlayer = new MoviePlayer(m)");
      r.setVar("dim", new Dimension(w.intValue(), h.intValue()));
      ImageProducer qtip = (ImageProducer)
        r.exec("qtip = new QTImageProducer(moviePlayer, dim)");
      Image img = Toolkit.getDefaultToolkit().createImage(qtip);
      boolean needsRedrawing = ((Boolean)
        r.exec("qtip.isRedrawing()")).booleanValue();
      int maxTime = ((Integer) r.exec("m.getDuration()")).intValue();
      int timeStep = maxTime / totalFrames;
      fields = new FlatField[totalFrames];
      for (int i=0; i<totalFrames; i++) {
        // paint next frame into image
        r.setVar("time", new Integer(timeStep * i));
        r.exec("moviePlayer.setTime(time)");
        if (needsRedrawing) r.exec("qtip.redraw(null)");
        r.exec("qtip.updateConsumers(null)");

        // convert image to VisAD Data object
        fields[i] = DataUtility.makeField(img);
      }

      r.exec("openMovieFile.close()");
      r.exec("QTSession.close()");
    }
    catch (Exception e) {
      r.exec("QTSession.close()");
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
