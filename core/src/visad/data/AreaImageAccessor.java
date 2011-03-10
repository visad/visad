package visad.data;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import visad.VisADException;
import edu.wisc.ssec.mcidas.AreaFile;
import edu.wisc.ssec.mcidas.AreaFileException;
import edu.wisc.ssec.mcidas.AreaFileFactory;
import edu.wisc.ssec.mcidas.adde.AddeImageURL;
import edu.wisc.ssec.mcidas.adde.AddeURLException;

/**
 * Knows how to read local satellite data to be used by a cache adaptor.
 * 
 * TODO: Add capability to work in conjunciton with ADDE data sources 
 * including the IDV <code>ucar.visad.data.CachedFlatField</code>.
 */
public class AreaImageAccessor implements FlatFieldCacheAccessor, 
    Comparable<AreaImageAccessor> {

  private static Logger log = Logger.getLogger(AreaImageAccessor.class.getName());
  
  private final int band;
  private final String source;
  private int startLine;
  private int numLines;
  private int lineMag;
  private int startElem;
  private int numElems;
  private int elemMag;
  private Date nominalTime;
  private int[][][] readCache;
  
  private boolean isAddeSource;
  
  /**
   * Create an instance. No data is read at this time.
   * 
   * @param source As described in {@link AreaFileFactory#getAreaFileInstance(String)}.
   * @param band The band number of the image.
   * @param readCache Array to use as a cache for reading AREA data into. If null data will
   *  not be read by-reference. See {@link AreaFile#getData(int[][][])}.
   * @throws VisADException
   */
  public AreaImageAccessor(String source, int band, int[][][] readCache) throws VisADException {
    super();
    this.band = band;
    this.source = source;
    this.readCache = readCache;
    if (this.readCache == null) {
      log.fine("readCache is null, by-reference data reading is disabled");
    }
    try {
      new AddeImageURL(source, AddeImageURL.REQ_IMAGEDATA, "", "");
      isAddeSource = true;
      throw new IllegalArgumentException("adde sources are not currently supported");
    } catch (Exception e) {
      isAddeSource = false;
    }
  }

  /**
   * Set AREA file subsetting parameters.
   * 
   * @param startLine
   * @param numLines
   * @param lineMag
   * @param startElem
   * @param numElems
   * @param elemMag
   */
  public void setAreaParams(int startLine, int numLines, int lineMag, int startElem, int numElems, 
        int elemMag) {
    this.startLine = startLine;
    this.numLines = numLines;
    this.lineMag = lineMag;
    this.startElem = startElem;
    this.numElems = numElems;
    this.elemMag = elemMag;
  }
  
  public boolean isAddeSource() {
    return isAddeSource;
  }

  protected int[][][] getAreaData() throws AreaFileException, AddeURLException {
    
    AreaFile af = null;
    if (!isAddeSource) {
      af = AreaFileFactory.getAreaFileInstance(source.toString(), startLine, numLines, lineMag,
          startElem, numElems, elemMag, band);
    } else {
      // expect sub-setting to be done in URL
      af = AreaFileFactory.getAreaFileInstance(source.toString());
    }
    
    if (nominalTime == null) {
      nominalTime = af.getAreaDirectory().getNominalTime();
    } else if (nominalTime.equals(af.getAreaDirectory())) {
      throw new FlatFieldCacheError("nominal time mismatch on subsequent reads", null);
    }
    
    // use by-reference if possible
    int[][][] raw = null;
    if (readCache != null) {
      raw = af.getData(readCache);
    } else {
      raw = af.getData();
    }
    return raw; 
  }
  
  public String getSource() {
    return source;
  }
  
  public double[][] readFlatField() {
    double[][] range = null;
    try {
      
      int[][][] raw = getAreaData();
      
      range = new double[1][numLines * numElems];
      int idx = 0;
      for (int line = 0; line < raw[0].length; line++) {
        for (int elem = 0; elem < raw[0][0].length; elem++) {
          range[0][idx++] = (double) raw[0][line][elem];
        }
      }
    } catch (Exception e) {
      log.log(Level.SEVERE, "Could not read image data: " + source.toString(), e);
    }
    return range;
  }
  
  public float[][] readFlatFieldFloats() {
    float[][] range = null;
    try {
      
      int[][][] raw = getAreaData();
      
      range = new float[1][numLines * numElems];
      int idx = 0;
      for (int line = 0; line < raw[0].length; line++) {
        for (int elem = 0; elem < raw[0][0].length; elem++) {
          range[0][idx++] = (float) raw[0][line][elem];
        }
      }
    } catch (Exception e) {
      log.log(Level.SEVERE, "Could not read AREA file: " + source.toString(), e);
    }
    return range;
  }

  public Date getNominalTime() {
    if (nominalTime == null) {
      AreaFile af;
      try {
        af = AreaFileFactory.getAreaFileInstance(source.toString());
      } catch (Exception e) {
        throw new FlatFieldCacheError("Error getting nominal time from AREA file", e);
      }
      nominalTime = af.getAreaDirectory().getNominalTime();
    }
    return nominalTime;
  }
  
  public int compareTo(AreaImageAccessor that) {
    long myTime = getNominalTime().getTime();
    long yourTime = that.getNominalTime().getTime();
    if (myTime > yourTime) {
      return 1;
    } else if (myTime < yourTime) {
      return -1;
    }
    return 0;
  }
  
  
}
