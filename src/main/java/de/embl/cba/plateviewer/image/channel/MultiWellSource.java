package de.embl.cba.plateviewer.image.channel;

import bdv.util.BdvOverlay;
import bdv.util.BdvOverlaySource;
import bdv.util.BdvSource;
import bdv.viewer.Source;
import de.embl.cba.bdv.utils.sources.Metadata;
import de.embl.cba.plateviewer.image.cellloader.MultiSiteLoader;
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

public abstract class MultiWellSource< T extends RealType< T > & NativeType< T > > extends AbstractBdvViewable
{
	protected long[] plateDimensions;
	protected int[] imageDimensions;
	protected double[] contrastLimits = new double[]{0, 255};
	protected ARGBType argbType;
	protected double[] voxelSizes = new double[]{1,1,1};
	protected String voxelUnit = "pixel";

	protected ArrayList< SingleSiteChannelFile > singleSiteChannelFiles;

	protected ArrayList< String > wellNames;
	protected CachedCellImg< T, ? > cachedCellImg;
	protected MultiSiteLoader loader;
	protected final List< File > channelFiles;
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

	public MultiWellSource( List< File > files, String namingScheme, int resolutionLevel, String channelName )
	{
		this.channelFiles = files;
		this.namingScheme = namingScheme;
		this.resolutionLevel = resolutionLevel;
		this.channelName = channelName;
	}

	public void close()
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
		return contrastLimits;
	}

	public MultiSiteLoader getLoader()
	{
		return loader;
	}

	@Deprecated // use getSource() instead to have calibrated voxel sizes
	public RandomAccessibleInterval< T > getRAI( )
	{
		return cachedCellImg;
	}

	protected void createCachedCellImg()
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

	public void setInitiallyVisible( boolean initiallyVisible )
	{
		isInitiallyVisible = initiallyVisible;
	}

	public Metadata.Type getType()
	{
		return this.type;
	}

	public List< File > getChannelFiles()
	{
		return channelFiles;
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
