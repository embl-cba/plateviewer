package de.embl.cba.plateviewer.image.channel;

import bdv.util.BdvOverlay;
import bdv.util.BdvOverlaySource;
import bdv.util.BdvSource;
import bdv.viewer.Source;
import de.embl.cba.bdv.utils.sources.Metadata;
import de.embl.cba.plateviewer.cellloader.MultiSiteLoader;
import de.embl.cba.plateviewer.image.MultiWellChannelFilesProvider;
import de.embl.cba.plateviewer.image.SingleSiteChannelFile;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.cache.img.ReadOnlyCachedCellImgFactory;
import net.imglib2.cache.img.ReadOnlyCachedCellImgOptions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;

import java.io.File;
import java.util.*;
import java.util.List;

public abstract class MultiWellImg < T extends RealType< T > & NativeType< T > > implements BdvViewable
{
	protected long[] plateDimensions;
	protected int[] imageDimensions;
	protected double[] lutMinMax = new double[]{0, 255};
	protected ARGBType argbType;

	protected ArrayList< SingleSiteChannelFile > singleSiteChannelFiles;

	protected ArrayList< String > wellNames;
	protected CachedCellImg< T, ? > cachedCellImg;
	protected MultiSiteLoader loader;
	protected final List< File > files;
	protected final int numIoThreads;
	protected final String namingScheme;
	protected String channelName;
	protected final int resolutionLevel;

	protected Source< ? > source;
	protected BdvSource bdvSource;
	protected BdvOverlaySource bdvOverlaySource;
	protected NativeType nativeType;
	protected Metadata.Type type = Metadata.Type.Image;
	protected boolean isInitiallyVisible;
	protected MultiWellChannelFilesProvider multiWellChannelFilesProvider;

	public MultiWellImg( List< File > files, String namingScheme, int numIoThreads, int resolutionLevel )
	{
		this.files = files;
		this.namingScheme = namingScheme;
		this.resolutionLevel = resolutionLevel;
		this.numIoThreads = numIoThreads;
	}

	public void dispose()
	{
		if( bdvSource != null ) bdvSource.removeFromBdv();
		if ( bdvOverlaySource != null ) bdvOverlaySource.removeFromBdv();
		cachedCellImg = null;
	}

	public ArrayList< String > getWellNames()
	{
		return wellNames;
	}

	public ARGBType getColor()
	{
		return argbType;
	}

//	public BdvSource getBdvSource()
//	{
//		return bdvSource;
//	}
//
//	public void setBdvSource( BdvSource bdvSource )
//	{
//		this.bdvSource = bdvSource;
//	}

	public String getName()
	{
		return channelName;
	}

	public void setCachedCellImgDimensions( ArrayList< SingleSiteChannelFile > singleSiteChannelFiles )
	{
		FinalInterval union = new FinalInterval( singleSiteChannelFiles.get( 0 ).getInterval() );

		for ( SingleSiteChannelFile singleSiteChannelFile : singleSiteChannelFiles )
			union = Intervals.union( singleSiteChannelFile.getInterval(), union );

		plateDimensions = new long[ 2 ];

		for ( int d = 0; d < 2; ++d )
			plateDimensions[ d ] = union.max( d ) + 1;
	}

	public double[] getContrastLimits()
	{
		return lutMinMax;
	}

	public MultiSiteLoader getLoader()
	{
		return loader;
	}

	public RandomAccessibleInterval< T > getRAI( )
	{
		return cachedCellImg;
	}

	protected void setCachedCellImg()
	{
		setCachedCellImgDimensions( singleSiteChannelFiles );

		cachedCellImg = new ReadOnlyCachedCellImgFactory().create(
				plateDimensions,
				nativeType,
				loader,
				ReadOnlyCachedCellImgOptions.options().cellDimensions( imageDimensions ) );
	}

	public boolean isInitiallyVisible()
	{
		return isInitiallyVisible;
	}

	public Metadata.Type getType()
	{
		return this.type;
	}

	@Override
	public Source< ? > getSource()
	{
		return source;
	}

	public void setSource( Source< ? > source )
	{
		this.source = source;
	}

	@Override
	public BdvOverlay getOverlay()
	{
		return null;
	}

}
