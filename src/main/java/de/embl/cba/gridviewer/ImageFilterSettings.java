package de.embl.cba.gridviewer;

import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public class ImageFilterSettings < T extends NativeType< T > & RealType< T > >
{
	public ImagesSource imagesSource;
	public CachedCellImg< T, ? > inputCachedCellImg;
	public String inputName;
	public String filterType;
	public int radius;
	public double offset;
	public double threshold;
	public long minObjectSize;
	public MultiPositionViewer multiPositionViewer;
}
