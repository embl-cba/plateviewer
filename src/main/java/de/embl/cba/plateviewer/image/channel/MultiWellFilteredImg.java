package de.embl.cba.plateviewer.image.channel;

import bdv.util.BdvOverlaySource;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;

public class MultiWellFilteredImg< T extends RealType< T > & NativeType< T > > extends MultiWellSource< T >
{
	public MultiWellFilteredImg(
			CachedCellImg< T, ? > cachedCellImg,
			String channelName,
			ARGBType color,
			double[] contrastLimits,
			BdvOverlaySource bdvOverlaySource )
	{
		super( null, null, 0, channelName );
		this.cachedCellImg = cachedCellImg;
		this.channelName = channelName;
		this.argbType = color;
		this.contrastLimits = contrastLimits;
		this.bdvOverlaySource = bdvOverlaySource;
	}
}
