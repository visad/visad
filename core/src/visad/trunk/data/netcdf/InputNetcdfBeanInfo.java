/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: InputNetcdfBeanInfo.java,v 1.1 1998-06-24 20:56:09 visad Exp $
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
	BeanDescriptor	bd = new BeanDescriptor(InputNetcdf.class);

	bd.setName("Input NetCDF File");
	return bd;
    }

    public PropertyDescriptor[]
    getPropertyDescriptors()
    {
	Class			beanClass = InputNetcdf.class;
	PropertyDescriptor[]	pds;

	try
	{
	    PropertyDescriptor	pathnameDescriptor =
		new PropertyDescriptor("pathname", beanClass);

	    // pathnameDescriptor.setBound(true);
	    pathnameDescriptor.setPropertyEditorClass(
		InputNetcdfPathnameEditor.class);

	    pds = new PropertyDescriptor[]
	    {
		pathnameDescriptor,
		new PropertyDescriptor("data", beanClass, "getData", null),
	    };
	}
	catch (Exception e)
	{
	    System.out.println(e.getMessage());
	    pds = null;
	}

	return pds;
    }
}
