package de.embl.cba.plateviewer.source.cachedcellimg;

import bdv.util.BdvOverlaySource;
import bdv.util.BdvSource;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public class MultiWellFilteredCachedCellImage< T extends RealType< T > & NativeType< T > > extends MultiWellCachedCellImage< T >
{
	public MultiWellFilteredCachedCellImage(
			CachedCellImg< T , ? > cachedCellImg,
			String channelName,
			BdvSource bdvSource,
			BdvOverlaySource bdvOverlaySource )
	{
		super( null, null, 1, 0 );
		this.cachedCellImg = cachedCellImg;
		this.channelName = channelName;
		this.bdvSource = bdvSource;
		this.bdvOverlaySource = bdvOverlaySource;
	}
}
