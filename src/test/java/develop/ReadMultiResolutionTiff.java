package develop;

import loci.formats.FormatException;
import loci.formats.IFormatReader;
import loci.formats.ImageReader;

import java.io.IOException;

public class ReadMultiResolutionTiff
{
	public static void main(String[] args) throws FormatException, IOException
	{

		String id = ReadMultiResolutionTiff.class.getResource( "../PyramidalTiff/image-scale2res4-lzw.ome.tif" ).getFile();

		// configure reader
		IFormatReader reader = new ImageReader();
		reader.setFlattenedResolutions( false );
		System.out.println("Initializing file: " + id);
		reader.setId(id); // parse metadata

		int seriesCount = reader.getSeriesCount();

		System.out.println("  Series count = " + seriesCount);

		for (int series=0; series<seriesCount; series++)
		{
			reader.setSeries(series);
			int resolutionCount = reader.getResolutionCount();

			System.out.println("    Resolution count for series #" + series +
					" = " + resolutionCount);

			for (int r=0; r<resolutionCount; r++) {
				reader.setResolution(r);
				System.out.println("      Resolution #" + r + " dimensions = " +
						reader.getSizeX() + " x " + reader.getSizeY());
				final int bitsPerPixel = reader.getBitsPerPixel();
				final int sizeX = reader.getSizeX();
				final int sizeY = reader.getSizeY();

				final byte[] bytes = reader.openBytes( 0 );
			}
		}

		reader.close();
	}
}
