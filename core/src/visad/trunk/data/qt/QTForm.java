//
// QTForm.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2002 Bill Hibbard, Curtis Rueden, Tom
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
import java.net.*;
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
public class QTForm extends Form
  implements FormFileInformer, FormBlockReader, FormProgressInformer
{

  // -- Constants --

  private static final String[] SUFFIXES = { "mov" };

  private static final String NO_QT_MSG = "You need to install " +
    "QuickTime for Java from http://www.apple.com/quicktime/";

  private static final boolean MAC_OS_X =
    System.getProperty("os.name").equals("Mac OS X");


  // -- Static fields --

  private static int num = 0;

  /** Flag indicating QuickTime for Java is not installed. */
  private static boolean noQT = false;

  /** Additional paths to search for QTJava.zip. */
  private static final URL[] PATHS = constructPaths();

  private static URL[] constructPaths() {
    // set up additional QuickTime for Java paths
    URL[] paths = null;
    try {
      paths = new URL[] {
        // Windows
        new URL("file:/WinNT/System32/QTJava.zip"),
        new URL("file:/Windows/System32/QTJava.zip"),
        new URL("file:/Windows/System/QTJava.zip"),
        // Mac OS X
        new URL("file:/System/Library/Java/Extensions/QTJava.zip")
      };
    }
    catch (MalformedURLException exc) { }
    return paths;
  }

  /** Reflection tool for QuickTime for Java calls. */
  private static final ReflectedUniverse R = constructUniverse();

  private static ReflectedUniverse constructUniverse() {
    boolean needClose = false;

    // create reflected universe
    ReflectedUniverse r = new ReflectedUniverse(PATHS);
    try {
      r.exec("import quicktime.QTSession");
      r.exec("QTSession.open()");
      needClose = true;
      if (MAC_OS_X) {
        r.exec("import quicktime.app.view.QTImageProducer");
        r.exec("import quicktime.app.view.MoviePlayer");
        r.exec("import quicktime.std.movies.TimeInfo");
      }
      else {
        r.exec("import quicktime.app.display.QTCanvas");
        r.exec("import quicktime.app.image.ImageUtil");
        r.exec("import quicktime.app.image.JImagePainter");
        r.exec("import quicktime.app.image.QTImageDrawer");
        r.exec("import quicktime.app.image.QTImageProducer");
        r.exec("import quicktime.app.image.Redrawable");
        r.exec("import quicktime.app.players.MoviePlayer");
      }
      r.exec("import quicktime.io.OpenMovieFile");
      r.exec("import quicktime.io.QTFile");
      r.exec("import quicktime.qd.Pict");
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
    catch (Throwable t) {
      noQT = true;
    }
    finally {
      if (needClose) {
        try { r.exec("QTSession.close()"); }
        catch (Throwable t) { }
      }
    }
    return r;
  }


  // -- Fields --

  /** Filename of current QuickTime movie. */
  private String currentId;

  /** Number of images in current QuickTime movie. */
  private int numImages;

  /** Time increment between frames. */
  private int timeStep;

  /** Image containing current frame. */
  private Image image;

  /** Flag indicating QuickTime frame needs to be redrawn. */
  private boolean needsRedrawing;

  /** Time between each frame, in 600ths of a second. */
  private int frameRate;

  /** Percent complete with current operation. */
  private double percent;


  // -- Constructor --

  /** Constructs a new QuickTime movie file form. */
  public QTForm() {
    super("QTForm" + num++);
    setFrameRate(10); // default to 10 fps
  }


  // -- FormFileInformer methods --

  /** Checks if the given string is a valid filename for a QuickTime movie. */
  public boolean isThisType(String name) {
    if (noQT) return false;
    for (int i=0; i<SUFFIXES.length; i++) {
      if (name.toLowerCase().endsWith(SUFFIXES[i])) return true;
    }
    return false;
  }

  /** Checks if the given block is a valid header for a QuickTime movie. */
  public boolean isThisType(byte[] block) {
    return false;
  }

  /** Returns the default file suffixes for the QuickTime movie formats. */
  public String[] getDefaultSuffixes() {
    String[] s = new String[SUFFIXES.length];
    System.arraycopy(SUFFIXES, 0, s, 0, SUFFIXES.length);
    return s;
  }


  // -- QTForm API methods --

  /** Whether QuickTime is available to this JVM. */
  public boolean canDoQT() { return !noQT; }

  /** Sets the frame rate of output movies in frames per second. */
  public void setFrameRate(int fps) { frameRate = 600 / fps; }

  /** Gets teh frame rate of output movies in frames per second. */
  public int getFrameRate() { return 600 / frameRate; }


  // -- Form API methods --

  /** Saves a VisAD Data object to a QuickTime movie at the given location. */
  public void save(String id, Data data, boolean replace)
    throws BadFormException, IOException, RemoteException, VisADException
  {
    if (MAC_OS_X) {
      throw new BadFormException("QuickTime movie " +
        "saving on Mac OS X is not supported.");
    }
    else if (noQT) throw new BadFormException(NO_QT_MSG);

    try {
      // extract image frames from data
      FlatField[] fields = DataUtility.getImageFields(data);
      int numFrames = fields.length;
      Gridded2DSet set = (Gridded2DSet) fields[0].getDomainSet();
      int[] lengths = set.getLengths();
      int kWidth = lengths[0];
      int kHeight = lengths[1];
      R.setVar("numFrames", numFrames);
      R.setVar("kWidth", kWidth);
      R.setVar("kHeight", kHeight);
      R.exec("QTSession.open()");

      // set up QuickTime drawing objects
      R.setVar("oneHalf", 0.5f);
      Component canv = (Component) R.exec(
        "canv = new QTCanvas(QTCanvas.kInitialSize, oneHalf, oneHalf)");
      JFrame frame = new JFrame();
      JPanel pane = new JPanel();
      frame.setContentPane(pane);
      pane.add("Center", canv);
      BufferedImage buffer =
        new BufferedImage(kWidth, kHeight, BufferedImage.TYPE_INT_RGB);
      R.setVar("buffer", buffer);
      R.exec("ip = new JImagePainter(buffer)");
      Dimension dim = new Dimension(kWidth, kHeight);
      R.setVar("dim", dim);
      R.exec("qid = new QTImageDrawer(ip, dim, Redrawable.kMultiFrame)");
      R.setVar("true", true);
      R.exec("qid.setRedrawing(true)");
      R.exec("canv.setClient(qid, true)");
      frame.pack();

      // create movie file & empty movie
      File file = new File(id);
      R.setVar("path", file.getAbsolutePath());
      R.exec("f = new QTFile(path)");
      Integer i1 = (Integer) R.getVar(
        "StdQTConstants.createMovieFileDeleteCurFile");
      Integer i2 = (Integer) R.getVar(
        "StdQTConstants.createMovieFileDontCreateResFile");
      R.setVar("flags", i1.intValue() | i2.intValue());
      R.exec("theMovie = " +
        "Movie.createMovieFile(f, StdQTConstants.kMoviePlayer, flags)");

      // add content
      int kNoVolume = 0;
      R.setVar("kNoVolume", kNoVolume);
      R.setVar("kVidTimeScale", 600);

      R.setVar("fkWidth", (float) kWidth);
      R.setVar("fkHeight", (float) kHeight);
      R.setVar("fkNoVolume", (float) kNoVolume);
      R.exec("vidTrack = theMovie.addTrack(fkWidth, fkHeight, fkNoVolume)");
      R.exec("vidMedia = new VideoMedia(vidTrack, kVidTimeScale)");

      R.exec("vidMedia.beginEdits()");
      R.exec("rect = new QDRect(kWidth, kHeight)");
      R.exec("gw = new QDGraphics(rect)");
      R.exec("pixmap = gw.getPixMap()");
      R.exec("pixsize = pixmap.getPixelSize()");
      R.exec("size = QTImage.getMaxCompressionSize(gw, rect, pixsize, " +
        "StdQTConstants.codecNormalQuality, " +
        "StdQTConstants.kAnimationCodecType, CodecComponent.anyCodec)");
      R.exec("imageHandle = new QTHandle(size, true)");
      R.exec("imageHandle.lock()");
      R.exec("compressedImage = RawEncodedImage.fromQTHandle(imageHandle)");
      R.setVar("zero", 0);
      R.exec("seq = new CSequence(gw, rect, pixsize, " +
        "StdQTConstants.kAnimationCodecType, " +
        "CodecComponent.bestFidelityCodec, " +
        "StdQTConstants.codecNormalQuality, " +
        "StdQTConstants.codecNormalQuality, numFrames, null, zero)");
      R.exec("desc = seq.getDescription()");

      // redraw first...
      setCurrentFrame(0, buffer, fields);
      R.exec("qid.redraw(null)");

      R.exec("qid.setGWorld(gw)");
      R.exec("qid.setDisplayBounds(rect)");

      for (int curSample=0; curSample<numFrames; curSample++) {
        setCurrentFrame(curSample, buffer, fields);
        R.exec("qid.redraw(null)");
        R.exec("info = seq.compressFrame(gw, rect, " +
          "StdQTConstants.codecFlagUpdatePrevious, compressedImage)");
        Integer sim = (Integer) R.exec("info.getSimilarity()");
        if (sim.intValue() == 0) R.setVar("keyFrame", 0);
        else {
          R.setVar("keyFrame", R.getVar("StdQTConstants.mediaSampleNotSync"));
        }
        R.exec("dataSize = info.getDataSize()");
        R.setVar("one", 1);
        R.setVar("frameRate", frameRate);
        R.exec("vidMedia.addSample(imageHandle, zero, dataSize, " +
          "frameRate, desc, one, keyFrame)");
      }

      // redraw after finishing...
      R.exec("port = canv.getPort()");
      R.exec("qid.setGWorld(port)");
      R.exec("qid.redraw(null)");
      R.exec("vidMedia.endEdits()");

      R.setVar("kTrackStart", 0);
      R.setVar("kMediaTime", 0);
      R.setVar("kMediaRate", 1.0f);
      R.exec("duration = vidMedia.getDuration()");
      R.exec(
        "vidTrack.insertMedia(kTrackStart, kMediaTime, duration, kMediaRate)");

      // save movie to file
      R.exec("outStream = OpenMovieFile.asWrite(f)");
      R.exec("name = f.getName()");
      R.exec("theMovie.addResource(outStream, " +
        "StdQTConstants.movieInDataForkResID, name)");
      R.exec("outStream.close()");

      R.exec("QTSession.close()");
    }
    catch (Exception exc) {
      R.exec("QTSession.close()");
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
    if (noQT) throw new BadFormException(NO_QT_MSG);

    percent = 0;
    int nImages = getBlockCount(id);
    FieldImpl[] fields = new FieldImpl[nImages];
    for (int i=0; i<nImages; i++) {
      fields[i] = (FieldImpl) open(id, i);
      percent = (double) (i + 1) / nImages;
    }

    DataImpl data;
    if (nImages == 1) data = fields[0];
    else {
      // combine data stack into time function
      RealType time = RealType.getRealType("time");
      FunctionType timeFunction = new FunctionType(time, fields[0].getType());
      Integer1DSet timeSet = new Integer1DSet(nImages);
      FieldImpl timeField = new FieldImpl(timeFunction, timeSet);
      timeField.setSamples(fields, false);
      data = timeField;
    }
    close();
    percent = -1;
    return data;
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


  // -- FormBlockReader methods --

  public DataImpl open(String id, int block_number)
    throws BadFormException, IOException, VisADException
  {
    if (!id.equals(currentId)) initFile(id);

    if (block_number < 0 || block_number >= numImages) {
      throw new BadFormException("Invalid image number: " + block_number);
    }

    if (noQT) throw new BadFormException(NO_QT_MSG);

    // paint frame into image
    R.setVar("time", timeStep * block_number);
    R.exec("moviePlayer.setTime(time)");
    if (needsRedrawing) R.exec("qtip.redraw(null)");
    R.exec("qtip.updateConsumers(null)");

    return DataUtility.makeField(image);
  }

  public int getBlockCount(String id)
    throws BadFormException, IOException, VisADException
  {
    if (!id.equals(currentId)) initFile(id);
    return numImages;
  }

  public void close() throws BadFormException, IOException, VisADException {
    if (currentId == null) return;

    try {
      R.exec("openMovieFile.close()");
      R.exec("QTSession.close()");
    }
    catch (Exception e) {
      R.exec("QTSession.close()");
      throw new BadFormException("Close movie failed: " + e.getMessage());
    }
    currentId = null;
  }


  // -- FormProgressInformer methods --

  public double getPercentComplete() { return percent; }


  // -- Helper methods --

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

  private void initFile(String id)
    throws BadFormException, IOException, VisADException
  {
    if (noQT) throw new BadFormException(NO_QT_MSG);

    // close any currently open files
    close();

    try {
      R.exec("QTSession.open()");

      // open movie file
      File file = new File(id);
      R.setVar("path", file.getAbsolutePath());
      R.exec("qtf = new QTFile(path)");
      R.exec("openMovieFile = OpenMovieFile.asRead(qtf)");
      R.exec("m = Movie.fromFile(openMovieFile)");

      // find first track with width != soundtrack
      int numTracks = ((Integer) R.exec("m.getTrackCount()")).intValue();
      int trackMostLikely = 0;
      int trackNum = 0;
      while (++trackNum <= numTracks && trackMostLikely == 0) {
        R.setVar("trackNum", trackNum);
        R.exec("imageTrack = m.getTrack(trackNum)");
        R.exec("d = imageTrack.getSize()");
        Integer w = (Integer) R.exec("d.getWidth()");
        if (w.intValue() > 0) trackMostLikely = trackNum;
      }
      R.setVar("trackMostLikely", trackMostLikely);
      R.exec("imageTrack = m.getTrack(trackMostLikely)");
      R.exec("d = imageTrack.getSize()");
      Integer w = (Integer) R.exec("d.getWidth()");
      Integer h = (Integer) R.exec("d.getHeight()");
      // now use controller to step movie
      R.exec("moviePlayer = new MoviePlayer(m)");
      R.setVar("dim", new Dimension(w.intValue(), h.intValue()));
      ImageProducer qtip = (ImageProducer)
        R.exec("qtip = new QTImageProducer(moviePlayer, dim)");
      image = Toolkit.getDefaultToolkit().createImage(qtip);
      needsRedrawing = ((Boolean) R.exec("qtip.isRedrawing()")).booleanValue();
      int maxTime = ((Integer) R.exec("m.getDuration()")).intValue();

      if (MAC_OS_X) {
        R.setVar("zero", 0);
        R.setVar("one", 1f);
        R.exec("timeInfo = new TimeInfo(zero, zero)");
        R.exec("moviePlayer.setTime(zero)");
        numImages = 0;
        int time = 0;
        do {
            numImages++;
            R.exec("timeInfo = imageTrack.getNextInterestingTime(" +
              "StdQTConstants.nextTimeMediaSample, timeInfo.time, one)");
            time = ((Integer) R.getVar("timeInfo.time")).intValue();
        }
        while (time >= 0);
      }
      else {
        R.exec("seq = ImageUtil.createSequence(imageTrack)");
        numImages = ((Integer) R.exec("seq.size()")).intValue();
      }
      timeStep = maxTime / numImages;
    }
    catch (Exception e) {
      R.exec("QTSession.close()");
      throw new BadFormException("Open movie failed: " + e.getMessage());
    }

    currentId = id;
  }


  // -- Utility methods --

  /** Gets QuickTime for Java reflected universe. */
  public static ReflectedUniverse getUniverse() { return R; }

  /** Converts the given byte array in PICT format to a VisAD field. */
  public static synchronized FlatField pictToField(byte[] bytes)
    throws VisADException
  {
    try {
      return DataUtility.makeField(pictToImage(bytes));
    }
    catch (IOException exc) { return null; }
  }

  /** Gets width and height for the given PICT bytes. */
  public static Dimension getPictDimensions(byte[] bytes)
    throws VisADException
  {
    if (noQT) throw new BadFormException(NO_QT_MSG);

    try {
      R.exec("QTSession.open()");
      R.setVar("bytes", bytes);
      R.exec("pict = new Pict(bytes)");
      R.exec("box = pict.getPictFrame()");
      int width = ((Integer) R.exec("box.getWidth()")).intValue();
      int height = ((Integer) R.exec("box.getHeight()")).intValue();
      R.exec("QTSession.close()");
      return new Dimension(width, height);
    }
    catch (Exception e) {
      R.exec("QTSession.close()");
      throw new BadFormException("PICT height determination failed: " +
        e.getMessage());
    }
  }

  /** Converts the given byte array in PICT format to a Java image. */
  public static Image pictToImage(byte[] bytes) throws VisADException {
    if (noQT) throw new BadFormException(NO_QT_MSG);

    try {
      R.exec("QTSession.open()");

      // Code adapted from:
      //   http://www.onjava.com/pub/a/onjava/2002/12/23/jmf.html?page=2
      R.setVar("bytes", bytes);
      R.exec("pict = new Pict(bytes)");
      R.exec("box = pict.getPictFrame()");
      int width = ((Integer) R.exec("box.getWidth()")).intValue();
      int height = ((Integer) R.exec("box.getHeight()")).intValue();
      // note: could get a RawEncodedImage from the Pict, but
      // apparently no way to get a PixMap from the REI
      R.exec("g = new QDGraphics(box)");
      Object blah = R.getVar("g");
      R.exec("pict.draw(g, box)");
      // get data from the QDGraphics
      R.exec("pixMap = g.getPixMap()");
      R.exec("rei = pixMap.getPixelData()");

      // copy bytes to an array
      int rowBytes = ((Integer) R.exec("pixMap.getRowBytes()")).intValue();
      int intsPerRow = rowBytes / 4;
      int pixLen = intsPerRow * height;
      R.setVar("pixLen", pixLen);
      int[] pixels = new int[pixLen];
      R.setVar("pixels", pixels);
      R.setVar("zero", new Integer(0));
      R.exec("rei.copyToArray(zero, pixels, zero, pixLen)");

      // now coax into image, ignoring alpha for speed
      int bitsPerSample = 32;
      int redMask = 0x00ff0000;
      int greenMask = 0x0000ff00;
      int blueMask = 0x000000ff;
      int alphaMask = 0x00000000;
      DirectColorModel colorModel = new DirectColorModel(
        bitsPerSample, redMask, greenMask, blueMask, alphaMask);

      R.exec("QTSession.close()");
      return Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(
        width, height, colorModel, pixels, 0, intsPerRow));
    }
    catch (Exception e) {
      R.exec("QTSession.close()");
      throw new BadFormException("PICT extraction failed: " + e.getMessage());
    }
  }


  // -- Main method --

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
