package edu.wisc.ssec.mcidas;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Created by yuanho on 5/14/15.
 */
public class CalibratorGvarG14 extends CalibratorGvar {
    // the following static init block sets the class temp/rad constants
    protected static float [] imager14FK1  = {0.f, 0.20432E6f, 0.42187E5f, 0.97211E4f, 0.50701E4f};

    protected static float [] sounder14FK1 =
            {0.37489E4f, 0.40083E4f, 0.42696E4f,
                    0.46952E4f, 0.49780E4f, 0.58074E4f,
                    0.68398E4f, 0.89725E4f, 0.13085E5f,
                    0.28920E5f, 0.34362E5f, 0.42901E5f,
                    0.12504E6f, 0.12834E6f, 0.13521E6f,
                    0.16890E6f, 0.18900E6f, 0.22720E6f};


    protected static float [] imager14FK2  = {0.f, 0.37107E+04f, 0.21929E+04f, 0.13446E+04f, 0.10823E+04f};

    protected static float [] sounder14FK2 =
            {0.97871E+03f, 0.10008E+04f, 0.10221E+04f,
                        0.10550E+04f, 0.10757E+04f, 0.11324E+04f,
                        0.11959E+04f, 0.13092E+04f, 0.14846E+04f,
                        0.19338E+04f, 0.20482E+04f, 0.22055E+04f,
                        0.31504E+04f, 0.31779E+04f, 0.32336E+04f,
                        0.34825E+04f, 0.36155E+04f, 0.38443E+04f};

    protected static float [] imager14TC1  = {0.f, 1.56149f, 3.75637f, 0.37970f, 0.08482f};

    protected static float [] sounder14TC1 =
            {0.00949f, 0.01039f, 0.01018f,  0.01299f,
             0.01335f,  0.04179f, 0.12271f,  0.11528f,
             0.03870f,  0.14343f, 0.27532f,  0.18229f,
             0.01783f,  0.01744f, 0.01906f,  0.05207f,
             0.05324f,  0.29680f};;

    protected static float [] imager14TC2  = {0.f, 0.99783f, 0.99154f, 0.99869f, 0.99964f};

    protected static float [] sounder14TC2 =
            {  0.99996f,   0.99995f, 0.99995f,   0.99994f,
               0.99994f,   0.99983f, 0.99953f,   0.99960f,
               0.99988f,   0.99964f, 0.99933f,   0.99958f,
               0.99997f,   0.99997f, 0.99997f,   0.99992f,
               0.99992f,   0.99960f};
    /**
     *
     * constructor
     *
     * @param dis         data input stream
     * @param ad          AncillaryData object
     * @param cb		    calibration parameters array
     *
     */

    public CalibratorGvarG14(DataInputStream dis, AncillaryData ad, int [] cb)
            throws IOException
    {
        super(dis, ad, cb);
    }


    public CalibratorGvarG14(int sensorId, int[] cb) {
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
            expn = (imager14FK1[band - 1] / inVal) + 1.0;
            temp = imager14FK2[band - 1] / Math.log(expn);
            outVal = (float) ((temp - imager14TC1[band - 1]) / imager14TC2[band - 1]);
        } else {
            expn = (sounder14FK1[band - 1] / inVal) + 1.0;
            temp = sounder14FK2[band - 1] / Math.log(expn);
            outVal = (float)
                    ((temp - sounder14TC1[band - 1]) / sounder14TC2[band - 1]);
        }

        return (outVal);
    }
}
