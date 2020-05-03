package de.embl.cba.plateviewer.table;

import net.imglib2.Interval;

public interface AnnotatedInterval
{
	Interval getInterval();
	String getName();
	boolean isOutlier();
	void setOutlier( boolean isOutlier );
	String getAnnotation();
	void setAnnotation( String annotation );
}
