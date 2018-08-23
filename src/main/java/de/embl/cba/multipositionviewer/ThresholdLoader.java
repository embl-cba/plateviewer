package de.embl.cba.multipositionviewer;


import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.CellLoader;
import net.imglib2.cache.img.SingleCellArrayImg;
import net.imglib2.type.NativeType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

public class ThresholdLoader< T extends RealType< T > & NativeType< T > > implements CellLoader< BitType >
{

	final RandomAccessibleInterval< T > inputImage;
	final double realThreshold;

	public ThresholdLoader(
			final RandomAccessibleInterval< T > inputImage,
			final double realThreshold )
	{
		this.inputImage = inputImage;
		this.realThreshold = realThreshold;
	}

	@Override
	public void load( final SingleCellArrayImg< BitType, ? > cell ) throws Exception
	{
		final Cursor< T > inputImageCursor = Views.flatIterable( Views.interval( inputImage, cell ) ).cursor();
		final Cursor< BitType > cellCursor = Views.flatIterable( cell ).cursor();

		final BitType one = new BitType( true );
		final BitType zero = new BitType( false );

		T threshold = inputImage.randomAccess().get().copy();
		threshold.setReal( realThreshold );

		while ( cellCursor.hasNext() )
		{
			cellCursor.next().set( inputImageCursor.next().compareTo( threshold ) > 0 ?  one : zero  );
		}

	}
}

