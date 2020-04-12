package de.embl.cba.plateviewer.cellloader;


import bdv.util.*;
import de.embl.cba.plateviewer.Utils;
import de.embl.cba.plateviewer.bdv.BdvVolatileTextOverlay;
import de.embl.cba.plateviewer.bdv.TextOverlay;
import de.embl.cba.plateviewer.source.ChannelSources;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.CellLoader;
import net.imglib2.cache.img.SingleCellArrayImg;
import net.imglib2.roi.labeling.LabelRegion;
import net.imglib2.roi.labeling.LabelRegions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.view.Views;

import java.awt.*;

public class SimpleSegmentationLoader< T extends NativeType< T > & RealType< T > > implements CellLoader< UnsignedByteType >
{
	final ChannelSources channelSources;
	final RandomAccessibleInterval< T > input;
	final double realThreshold;
	final Bdv bdv;
	final BdvVolatileTextOverlay volatileTextBdvOverlay;
	final BdvOverlaySource< BdvOverlay > objectNumberBdvOverlay;
	private final long minSize;
	public static final UnsignedByteType ONE = new UnsignedByteType( 255 );
	public static final UnsignedByteType ZERO = new UnsignedByteType( 0 );

	public SimpleSegmentationLoader(
			ChannelSources channelSources,
			RandomAccessibleInterval< T > input,
			final double realThreshold,
			long minSize,
			final Bdv bdv )
	{
		this.channelSources = channelSources;
		this.input = input;
		this.realThreshold = realThreshold;
		this.bdv = bdv;
		this.minSize = minSize;
		this.volatileTextBdvOverlay = new BdvVolatileTextOverlay();
		this.objectNumberBdvOverlay = BdvFunctions.showOverlay( volatileTextBdvOverlay, "overlay", BdvOptions.options().addTo( bdv ) );

	}

	public void dispose()
	{
		objectNumberBdvOverlay.removeFromBdv();
	}

	@Override
	public void load( final SingleCellArrayImg< UnsignedByteType, ? > cell ) throws Exception
	{
		if ( channelSources.getLoader().getChannelSource( cell ) == null ) return;

		thresholdImageSourceAndPutResultIntoCell( cell );

		final LabelRegions< Integer > labelRegions = Utils.createLabelRegions( cell );

		clearCell( cell );

		int numValidObjects = paintValidObjectsIntoCell( cell, labelRegions, minSize );

		volatileTextBdvOverlay.addTextOverlay(
				new TextOverlay(
						"" + numValidObjects,
						Utils.getCenter( cell ),
						200,
						Color.GREEN )
		);

	}

	public void thresholdImageSourceAndPutResultIntoCell( SingleCellArrayImg< UnsignedByteType, ? > cell )
	{
		final Cursor< T > inputImageCursor = Views.flatIterable( Views.interval( input, cell ) ).cursor();

		final Cursor< UnsignedByteType > cellCursor = Views.flatIterable( cell ).cursor();

		T threshold = ( T ) input.randomAccess().get().copy();

		threshold.setReal( realThreshold );

		while ( cellCursor.hasNext() )
		{
			cellCursor.next().set( inputImageCursor.next().compareTo( threshold ) > 0 ? ONE : ZERO );
		}
	}

	public int paintValidObjectsIntoCell( SingleCellArrayImg< UnsignedByteType, ? > cell, LabelRegions< Integer > labelRegions, long minSize )
	{
		int numValidObjects = 0;
		for ( LabelRegion labelRegion : labelRegions )
		{
			if ( labelRegion.size() > minSize )
			{
				numValidObjects++;
				paintRegionIntoCell( cell, labelRegion );
			}
		}
		return numValidObjects;
	}

	public void clearCell( SingleCellArrayImg< UnsignedByteType, ? > cell )
	{
		final Cursor< UnsignedByteType > cellCursor2 = cell.cursor();
		while ( cellCursor2.hasNext() )
		{
			cellCursor2.next().set( 0 );
		}
	}

	public static void paintRegionIntoCell( SingleCellArrayImg< UnsignedByteType, ? > cell, LabelRegion labelRegion )
	{
		final Cursor< Void > regionCursor = labelRegion.cursor();
		final RandomAccess< UnsignedByteType > access = cell.randomAccess();
		while ( regionCursor.hasNext() )
		{
			regionCursor.fwd();
			access.setPosition( regionCursor );
			access.get().set( ONE );
		}
	}
}

