package de.embl.cba.plateviewer.source.cachedcellimg;

import bdv.util.BdvOverlaySource;
import bdv.util.BdvSource;
import ch.systemsx.cisd.hdf5.HDF5DataSetInformation;
import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5Reader;
import de.embl.cba.bdv.utils.sources.Metadata;
import de.embl.cba.plateviewer.cellloader.MultiSiteLoader;
import de.embl.cba.plateviewer.Utils;
import de.embl.cba.plateviewer.source.MultiWellChannelFilesProvider;
import de.embl.cba.plateviewer.source.SingleSiteChannelFile;
import ij.IJ;
import ij.ImagePlus;
import ij.process.LUT;
import net.imglib2.FinalInterval;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.cache.img.ReadOnlyCachedCellImgFactory;
import net.imglib2.cache.img.ReadOnlyCachedCellImgOptions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedIntType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;

import java.awt.*;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.util.*;
import java.util.List;

public abstract class MultiWellCachedCellImage< T extends RealType< T > & NativeType< T > >
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

	protected BdvSource bdvSource;
	protected BdvOverlaySource bdvOverlaySource;
	protected NativeType nativeType;
	protected Metadata.Type type;
	protected boolean isInitiallyVisible;
	protected MultiWellChannelFilesProvider multiWellChannelFilesProvider;

	public MultiWellCachedCellImage( List< File > files, String namingScheme, int numIoThreads, int resolutionLevel )
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

	public BdvSource getBdvSource()
	{
		return bdvSource;
	}

	public void setBdvSource( BdvSource bdvSource )
	{
		this.bdvSource = bdvSource;
	}

	public String getChannelName()
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

	public double[] getLutMinMax()
	{
		return lutMinMax;
	}

	public MultiSiteLoader getLoader()
	{
		return loader;
	}

	public CachedCellImg< T, ? > getCachedCellImg( )
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
}
