package de.embl.cba.plateviewer.image.source;

import bdv.viewer.Interpolation;
import bdv.viewer.Source;
import mpicbg.spim.data.sequence.VoxelDimensions;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealRandomAccessible;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.interpolation.InterpolatorFactory;
import net.imglib2.interpolation.randomaccess.ClampingNLinearInterpolatorFactory;
import net.imglib2.interpolation.randomaccess.NearestNeighborInterpolatorFactory;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.volatiles.VolatileARGBType;
import net.imglib2.view.ExtendedRandomAccessibleInterval;
import net.imglib2.view.Views;

// TODO:
// Constructor could look like this: ConvertedSource< A, B > implements Source< B >

public class ARGBConvertedRealAccessibleSource< R extends RealType< R > > implements Source< VolatileARGBType >
{
    private final Source source;
    private Converter< RealType, VolatileARGBType > converter;

    private VolatileARGBType outOfBoundsValue;

    public ARGBConvertedRealAccessibleSource( Source< RealType > source,
											  Converter< RealType, VolatileARGBType > converter,
											  VolatileARGBType outOfBoundsValue )
    {
        this.source = source;
        this.converter = converter;
        this.outOfBoundsValue = outOfBoundsValue;
    }

    public ARGBConvertedRealAccessibleSource( Source< RealType > source, Converter< RealType, VolatileARGBType > converter )
    {
        this( source, converter, new VolatileARGBType( 0 ) );
    }

    @Override
    public boolean isPresent( final int t )
    {
       return this.source.isPresent( t );
    }

    @Override
    public RandomAccessibleInterval< VolatileARGBType > getSource( final int t, final int mipMapLevel )
    {
        return Converters.convert(
                        source.getSource( t, mipMapLevel ),
                        converter,
                        new VolatileARGBType() );
    }

    @Override
    public RealRandomAccessible< VolatileARGBType > getInterpolatedSource(final int t, final int level, final Interpolation method)
    {
        return Converters.convert(
                source.getInterpolatedSource( t, level, method ),
                converter,
                new VolatileARGBType() );
    }

    @Override
    public void getSourceTransform( int t, int level, AffineTransform3D transform )
    {
        source.getSourceTransform( t, level, transform );
    }

    @Override
    public VolatileARGBType getType() {
        return new VolatileARGBType();
    }

    @Override
    public String getName() {
        return source.getName();
    }

    @Override
    public VoxelDimensions getVoxelDimensions()
    {
        return source.getVoxelDimensions();
    }

    @Override
    public int getNumMipmapLevels()
    {
        return source.getNumMipmapLevels();
    }

}
