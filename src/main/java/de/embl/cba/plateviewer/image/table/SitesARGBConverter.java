package de.embl.cba.plateviewer.image.table;

import de.embl.cba.plateviewer.table.DefaultSiteNameTableRow;
import de.embl.cba.tables.color.ColoringModel;
import de.embl.cba.tables.color.LabelsARGBConverter;
import de.embl.cba.tables.color.LazyCategoryColoringModel;
import de.embl.cba.tables.imagesegment.ImageSegment;
import de.embl.cba.tables.imagesegment.LabelFrameAndImage;
import net.imglib2.Volatile;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.volatiles.VolatileARGBType;

import java.util.List;
import java.util.Map;

public class SitesARGBConverter< DefaultSiteNameTableRow > implements LabelsARGBConverter
{
	private final ColoringModel< DefaultSiteNameTableRow > coloringModel;
	private final List< DefaultSiteNameTableRow > tableRows;
	private ARGBType singleColor;

	private int frame;

	public SitesARGBConverter(
			List< DefaultSiteNameTableRow > tableRows,
			ColoringModel< DefaultSiteNameTableRow > coloringModel )
	{
		this.tableRows = tableRows;
		this.coloringModel = coloringModel;
	}


	@Override
	public void convert( RealType label, VolatileARGBType color )
	{
		if ( label instanceof Volatile )
		{
			if ( ! ( ( Volatile ) label ).isValid() )
			{
				color.set( 0 );
				color.setValid( false );
				return;
			}
		}

		if ( label.getRealDouble() == 0 )
		{
			color.setValid( true );
			color.set( 0 );
			return;
		}

		if ( singleColor != null )
		{
			color.setValid( true );
			color.set( singleColor.get() );
			return;
		}

		final int rowIndex = ( int ) label.getRealDouble();
		final DefaultSiteNameTableRow tableRow = tableRows.get( rowIndex );

		if ( tableRow == null )
		{
			color.set( 0 );
			color.setValid( true );
		}
		else
		{
			coloringModel.convert( tableRow, color.get() );

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
}
