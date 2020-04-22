package de.embl.cba.plateviewer.image.cellloader;

import de.embl.cba.plateviewer.Utils;
import de.embl.cba.plateviewer.image.SingleSiteChannelFile;
import ij.IJ;
import ij.ImagePlus;
import ij.process.ColorProcessor;
import net.imglib2.cache.img.SingleCellArrayImg;

import java.io.File;
import java.util.ArrayList;

public class MultiSiteImagePlusLoader extends MultiSiteLoader
{
	public MultiSiteImagePlusLoader( ArrayList< SingleSiteChannelFile > singleSiteChannelFiles, int numIoThreads )
	{
		super( singleSiteChannelFiles, numIoThreads );
	}

	@Override
	public synchronized void load( final SingleCellArrayImg cell )
	{
		SingleSiteChannelFile singleSiteChannelFile = getChannelSource( cell );

		if ( singleSiteChannelFile != null )
		{
			loadImagePlusIntoCell( cell, singleSiteChannelFile.getFile() );
		}
	}

	private void loadImagePlusIntoCell( SingleCellArrayImg< ? , ? > cell, File file )
	{
		Utils.debug( "Loading: " + file.getName() );

		// TODO: check for the data type of the cell (cell.getFirstElement())

		final ImagePlus imp = IJ.openImage( file.getAbsolutePath() );

		if ( imp.getBitDepth() == 8 )
		{
			final byte[] impdata = ( byte[] ) imp.getProcessor().getPixels();
			final byte[] celldata = ( byte[] ) cell.getStorageArray();
			System.arraycopy( impdata, 0, celldata, 0, celldata.length );
		}
		else if ( imp.getBitDepth() == 16 )
		{
			final short[] impdata = ( short[] ) imp.getProcessor().getPixels();
			final short[] celldata = ( short[] ) cell.getStorageArray();
			System.arraycopy( impdata, 0, celldata, 0, celldata.length );
		}
		else if ( imp.getBitDepth() == 24 ) // RGB
		{
			// Compute sum of RGB values and return as short array

			final byte[][] imgDataRGB = new byte[3][];
			for ( int c = 0; c < 3; c++ )
			{
				imgDataRGB[ c ] = ( byte[] ) ((ColorProcessor )imp.getProcessor()).getChannel( c + 1 );
			}

			final short[] celldata = ( short[] ) cell.getStorageArray();

			for ( int i = 0; i < imgDataRGB[ 0 ].length; i++ )
			{
				for ( int c = 0; c < 3; c++ )
				{
					celldata[ i ] += imgDataRGB[ c ][ i ] & 0xFF;
				}
			}
		}
		else if ( imp.getBitDepth() == 32 )
		{
			final float[] impdata = ( float[] ) imp.getProcessor().getPixels();
			final float[] celldata = ( float[] ) cell.getStorageArray();
			System.arraycopy( impdata, 0, celldata, 0, celldata.length );
		}
	}
}
