package visad.data.hdfeos;

import java.util.*;
import java.lang.*;
import experiment.*;
import visad.*;

  abstract class metaDomain {

    public abstract Set getVisADSet( indexSet i_set ) throws VisADException;

    public abstract MathType getVisADMathType() throws VisADException;
      

  }
