package de.embl.cba.plateviewer.util;

import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.util.Intervals;

import java.awt.*;

public class Graphics
{
	/**
	 *
	 * @param g
	 * @param interval the interval into which to draw the cross
	 * @param shrink value between 0 and 1, 0 means to keep the original size
	 * @param width
	 */
	public static void drawCross( Graphics2D g, Interval interval, double shrink, int width )
	{
		final int[] dimensions = Intervals.dimensionsAsIntArray( interval );

		final int shrinkX = ( int ) ( dimensions[ 0 ] * shrink );
		final int shrinkY = ( int ) ( dimensions[ 1 ] * shrink );

		final FinalInterval expand = Intervals.expand( interval, new long[]{-shrinkX, -shrinkY} );

		Graphics2D g2 = (Graphics2D) g;
		g2.setStroke(new BasicStroke( width ));

		g.drawLine(
				(int) expand.min( 0 ),
				(int) expand.min( 1 ),
				(int) expand.max( 0 ),
				(int) expand.max( 1 ) );

		g.drawLine(
				(int) expand.min( 0 ),
				(int) expand.max( 1 ),
				(int) expand.max( 0 ),
				(int) expand.min( 1 ) );
	}
}
