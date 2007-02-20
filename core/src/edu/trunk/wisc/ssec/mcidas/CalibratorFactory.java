package edu.wisc.ssec.mcidas;

/**
 * Utility class for creating <code>Calibrator</code> instances.
 * 
 * @author Bruce Flynn, SSEC
 * @version $Id: CalibratorFactory.java,v 1.1 2007-02-20 18:46:07 brucef Exp $
 */
public final class CalibratorFactory {

	/** Disallow instatiantion. */
	private CalibratorFactory() {}
	
    /**
     * Get an appropriate <code>Calibrator</code> for the sensor id provided.
     * See the McIDAS Users Guide 
     * <a href="http://www.ssec.wisc.edu/mcidas/doc/users_guide/current/app_c-1.html">Appendix C</a>
     * for a table of sensor ids.
     * @param id Sensor id from the directory block
     * @param cal Calibration block used to initialize the <code>Calibrator
     * </code>
     * @return initialized <code>Calibrator</code>.
     * @throws CalibratorException on an error initializing the object.
     */
	public final static Calibrator getCalibrator(final int id, final int[] cal) 
		throws CalibratorException {
		
		Calibrator calibrator = null;
	    switch (id) {
	      
	      case Calibrator.SENSOR_MSG:
	    	  calibrator = new CalibratorMsg(cal);
	    	  break;
	        
	      case Calibrator.SENSOR_GOES8_IMGR:
	      case Calibrator.SENSOR_GOES8_SNDR:
	    	  calibrator = new CalibratorGvarG8(id, cal);
	    	  break;
	    	  
	      case Calibrator.SENSOR_GOES9_IMGR:
	      case Calibrator.SENSOR_GOES9_SNDR:
	    	  calibrator = new CalibratorGvarG9(id, cal);
	    	  break;
	    	  
	      case Calibrator.SENSOR_GOES10_IMGR:
	      case Calibrator.SENSOR_GOES10_SNDR:
	    	  calibrator = new CalibratorGvarG9(id, cal);
	    	  break;
	    	  
	      default:
	        throw new CalibratorException(
	            "Unknown or unimplemented sensor id: " + id
	        );
	    }
	    return calibrator;
	}
	
}
