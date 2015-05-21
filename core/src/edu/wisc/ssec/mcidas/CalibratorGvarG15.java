package edu.wisc.ssec.mcidas;

import java.io.DataInputStream;
import java.io.IOException;
import edu.wisc.ssec.mcidas.AncillaryData;
/**
 * Created by yuanho on 5/14/15.
 */
public class CalibratorGvarG15 extends CalibratorGvar {
    // the following static init block sets the class temp/rad constants
    protected static float [] imager15FK1  = {0.f, 0.20075E+06f, 0.42330E+05f, 0.97668E+04f,0.50882E+04f};

    protected static float [] sounder15FK1 =
            {0.37318E+04f, 0.40022E+04f, 0.42656E+04f,
                    0.46849E+04f, 0.49817E+04f, 0.58332E+04f,
                    0.68168E+04f, 0.89937E+04f, 0.13075E+05f,
                    0.28886E+05f, 0.34356E+05f, 0.42758E+05f,
                    0.12488E+06f, 0.12832E+06f, 0.13520E+06f,
                    0.16962E+06f, 0.18873E+06f, 0.22791E+06f};


    protected static float [] imager15FK2  = {0.f, 0.36890E+04f, 0.21957E+04f, 0.13466E+04f, 0.10836E+04f};

    protected static float [] sounder15FK2 =
            {0.97722E+03f, 0.10003E+04f, 0.10218E+04f,
                    0.10542E+04f, 0.10760E+04f, 0.11341E+04f,
                    0.11946E+04f, 0.13102E+04f, 0.14842E+04f,
                    0.19331E+04f, 0.20481E+04f, 0.22030E+04f,
                    0.31491E+04f, 0.31777E+04f, 0.32335E+04f,
                    0.34875E+04f, 0.36138E+04f, 0.38483E+04f};


    protected static float [] imager15TC1  = {0.f, 1.59091f, 3.75399f, 0.38176f, 0.08671f};

    protected static float [] sounder15TC1 =
            { 0.00909f,   0.00998f, 0.01021f,   0.01327f,
              0.01318f,   0.04219f, 0.11811f,   0.11003f,
              0.03890f,   0.13744f, 0.27731f,   0.17079f,
              0.01809f,   0.01782f, 0.01976f,   0.05217f,
              0.05275f,   0.29972f,  };


    protected static float [] imager15TC2  = {0.f, 0.99777f, 0.99156f, 0.99870f, 0.99963f};

    protected static float [] sounder15TC2 =
            { 0.99996f,   0.99995f, 0.99995f,   0.99994f,
              0.99994f,   0.99983f, 0.99955f,   0.99962f,
              0.99988f,   0.99965f, 0.99933f,   0.99961f,
              0.99997f,   0.99997f, 0.99997f,   0.99992f,
              0.99992f,   0.99959f  };



    /**
     *
     * constructor
     *
     * @param dis         data input stream
     * @param ad          AncillaryData object
     * @param cb		    calibration parameters array
     *
     */

    public CalibratorGvarG15(DataInputStream dis, AncillaryData ad, int [] cb)
            throws IOException
    {
        super(dis, ad, cb);
    }


    public CalibratorGvarG15(int sensorId, int[] cb) {
        super(sensorId, cb);
    }

    /**
     *
     * calibrate from radiance to temperature
     *
     * @param inVal       input data value
     * @param band        channel/band number
     * @param sId         sensor id number
     *
     */

    public float radToTemp(float inVal, int band, int sId) {

        double expn;
        double temp;
        float outVal;

        if ((sId % 2) == 0) {
            expn = (imager15FK1[band - 1] / inVal) + 1.0;
            temp = imager15FK2[band - 1] / Math.log(expn);
            outVal = (float) ((temp - imager15TC1[band - 1]) / imager15TC2[band - 1]);
        } else {
            expn = (sounder15FK1[band - 1] / inVal) + 1.0;
            temp = sounder15FK2[band - 1] / Math.log(expn);
            outVal = (float)
                    ((temp - sounder15TC1[band - 1]) / sounder15TC2[band - 1]);
        }

        return (outVal);
    }
}
