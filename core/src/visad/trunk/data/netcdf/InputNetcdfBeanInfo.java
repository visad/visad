/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: InputNetcdfBeanInfo.java,v 1.2 1998-06-26 20:34:56 visad Exp $
 */

package visad.data.netcdf;

import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;


/**
 * Provides information on the InputNetcdf Java bean.
 */
public class
InputNetcdfBeanInfo
    extends	SimpleBeanInfo
{
    public BeanDescriptor
    getBeanDescriptor()
    {
	BeanDescriptor	bd = 
	    new BeanDescriptor(InputNetcdf.class, InputNetcdfCustomizer.class);

	bd.setName("Input NetCDF File");
	return bd;
    }
}
