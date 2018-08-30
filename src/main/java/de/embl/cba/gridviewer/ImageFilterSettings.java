package de.embl.cba.gridviewer;

import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public class ImageFilterSettings < T extends NativeType< T > & RealType< T > >
{
	public CachedCellImg< T, ? > inputCachedCellImg;
	public String inputName;
	public String filterType;
	public int radius;
	public double offset;
	public double threshold;
	public long minObjectSize;
	public MultiPositionViewer multiPositionViewer;

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
		this.minObjectSize = settings.minObjectSize;
//		this.multiPositionViewer = settings.multiPositionViewer;
	}
}
