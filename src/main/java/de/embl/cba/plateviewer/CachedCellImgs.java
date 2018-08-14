package de.embl.cba.plateviewer;

import ij.IJ;
import ij.ImagePlus;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.cache.img.ReadOnlyCachedCellImgFactory;
import net.imglib2.cache.img.ReadOnlyCachedCellImgOptions;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;

import java.io.File;
import java.util.Map;

public class CachedCellImgs
{

	private final Map< String, File > cellFileMap;
	private final String plateType;
	private long[] dimensions;
	private int[] cellDimensions;
	private int bitDepth;


	public CachedCellImgs( Map< String, File > cellFileMap, String plateType )
	{
		this.cellFileMap = cellFileMap;
		this.plateType = plateType;

		setDimensionsAndBitDepth();
	}

	public int getBitDepth()
	{
		return bitDepth;
	}

	public int[] getCellDimensions()
	{
		return cellDimensions;
	}

	private void setDimensionsAndBitDepth()
	{
		final String next = cellFileMap.keySet().iterator().next();
		File file = cellFileMap.get( next );

		final ImagePlus imagePlus = IJ.openImage( file.getAbsolutePath() );

		bitDepth = imagePlus.getBitDepth();

		final int width = imagePlus.getWidth();
		final int height = imagePlus.getHeight();

		dimensions = new long[ 2 ];

		switch ( plateType )
		{
			case Utils.WELL_PLATE_96:
				dimensions[ 0 ] = width * 12;
				dimensions[ 1 ] = height * 8;
		};


		cellDimensions = new int[]{
				imagePlus.getWidth(),
				imagePlus.getHeight(),
		};

	}

	public CachedCellImg create( )
	{
		final PlateImgLoader loader = new PlateImgLoader( cellDimensions, bitDepth, cellFileMap );

		switch ( bitDepth )
		{
			case 8:

				final CachedCellImg< UnsignedByteType, ? > byteTypeImg = new ReadOnlyCachedCellImgFactory().create(
						dimensions,
						new UnsignedByteType(),
						loader,
						ReadOnlyCachedCellImgOptions.options().cellDimensions( cellDimensions ) );
				return byteTypeImg;

			case 16:

				final CachedCellImg< UnsignedShortType, ? > unsignedShortTypeImg = new ReadOnlyCachedCellImgFactory().create(
						dimensions,
						new UnsignedShortType(),
						loader,
						ReadOnlyCachedCellImgOptions.options().cellDimensions( cellDimensions ) );
				return unsignedShortTypeImg;

			case 32:

				final CachedCellImg< FloatType, ? > floatTypeImg = new ReadOnlyCachedCellImgFactory().create(
						dimensions,
						new UnsignedShortType(),
						loader,
						ReadOnlyCachedCellImgOptions.options().cellDimensions( cellDimensions ) );
				return floatTypeImg;

			default:

				return null;

		}

	}
}
