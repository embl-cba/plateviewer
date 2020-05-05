package de.embl.cba.plateviewer.table;

import net.imglib2.Interval;

public interface AnnotatedInterval extends Outlier
{
	Interval getInterval();
	String getName();
}
