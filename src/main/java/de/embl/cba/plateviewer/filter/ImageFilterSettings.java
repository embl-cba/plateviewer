package de.embl.cba.plateviewer.filter;

import de.embl.cba.plateviewer.view.ImagePlateViewer;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public class ImageFilterSettings < T extends NativeType< T > & RealType< T > >
{
	public RandomAccessibleInterval rai;
	public String inputName;
	public String filterType;
	public int radius = 7;
	public double offset = 0;
	public double factor = 1;
	public double threshold = 1;
	public long minObjectSize = 100;
	public boolean normalize = false;
	public ImagePlateViewer imagePlateViewer;

	public ImageFilterSettings( )
	{
	}

	public ImageFilterSettings( ImageFilterSettings settings )
	{
//		this.inputCachedCellImg = settings.inputCachedCellImg;
		this.inputName = settings.inputName;
		this.filterType = settings.filterType;
		this.radius = settings.radius;
		this.offset = settings.offset;
		this.threshold = settings.threshold;
		this.normalize = settings.normalize;
		this.factor = settings.factor;
		this.minObjectSize = settings.minObjectSize;
//		this.multiPositionViewer = settings.multiPositionViewer;
	}
}
