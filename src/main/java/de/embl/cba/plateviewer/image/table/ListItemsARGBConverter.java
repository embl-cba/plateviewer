package de.embl.cba.plateviewer.image.table;

import de.embl.cba.tables.color.ColoringModel;
import de.embl.cba.tables.color.LabelsARGBConverter;
import net.imglib2.Volatile;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.volatiles.VolatileARGBType;

import java.util.HashMap;
import java.util.List;

// TODO: move to table-utils
public class ListItemsARGBConverter< T > implements LabelsARGBConverter
{
	public static final int OUT_OF_BOUNDS_ROW_INDEX = -1;
	private final ColoringModel< T > coloringModel;
	private final List< T > list;
	private ARGBType singleColor;
	private int frame;
	private int noColorArgbIndex; // default, background color
	private final HashMap< Integer, Integer > indexToColor;

	public ListItemsARGBConverter(
			List< T > list,
			ColoringModel< T > coloringModel )
	{
		this.list = list;
		this.coloringModel = coloringModel;
		noColorArgbIndex = 0;
		indexToColor = new HashMap<>();
	}

	@Override
	public void convert( RealType rowIndex, VolatileARGBType color )
	{
		if ( rowIndex instanceof Volatile )
		{
			if ( ! ( ( Volatile ) rowIndex ).isValid() )
			{
				color.set( noColorArgbIndex );
				color.setValid( false );
				return;
			}
		}

		final int index = ( int ) rowIndex.getRealDouble();

		if ( indexToColor.containsKey( index ))
		{
			color.set( indexToColor.get( index ) );
			return;
		}

		if ( index == OUT_OF_BOUNDS_ROW_INDEX )
		{
			color.set( noColorArgbIndex );
			color.setValid( true );
			return;
		}

		if ( singleColor != null )
		{
			color.setValid( true );
			color.set( singleColor.get() );
			return;
		}

		final T item = list.get( index );

		if ( item == null )
		{
			color.set( noColorArgbIndex );
			color.setValid( true );
		}
		else
		{
			coloringModel.convert( item, color.get() );

			final int alpha = ARGBType.alpha( color.get().get() );
			if( alpha < 255 )
				color.mul( alpha / 255.0 );

			color.setValid( true );
		}
	}

	@Override
	public void timePointChanged( int timePointIndex )
	{
		this.frame = timePointIndex;
	}

	@Override
	public void setSingleColor( ARGBType argbType )
	{
		singleColor = argbType;
	}

	public HashMap< Integer, Integer > getIndexToColor()
	{
		return indexToColor;
	}
}
