package visad.data;

import visad.Data;
import visad.FlatField;


/**
 * Exchange data with a "file".
 */
public abstract class FileAccessor
{
    public abstract void	writeFile(
				    int[]	fileLocations,
				    Data	range);


    public abstract double[][]	readFlatField(
				    FlatField	template,
				    int[]	fileLocation);


    public abstract void	writeFlatField(
				    double[][]	values,
				    FlatField	template,
				    int[]	fileLocation);
}
