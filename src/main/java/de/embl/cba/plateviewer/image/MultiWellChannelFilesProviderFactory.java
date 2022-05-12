package de.embl.cba.plateviewer.image;

import java.io.File;
import java.util.List;

public class MultiWellChannelFilesProviderFactory
{
	public static MultiWellChannelFilesProvider getMultiWellChannelFilesProvider(
			List< File > files,
			String namingScheme,
			int[] imageDimensions )
	{
		if ( namingScheme.equals( NamingSchemes.PATTERN_MD_A01_SITE_WAVELENGTH ) || namingScheme.equals( NamingSchemes.PATTERN_MD_A01_SITE ) || namingScheme.equals(  NamingSchemes.PATTERN_MD_A01_WAVELENGTH) )
		{
			return new DefaultMultiWellMultiSiteChannelFilesProvider( files, imageDimensions, namingScheme  );
		}
		else if ( namingScheme.equals( NamingSchemes.PATTERN_SCANR_WELLNUM_SITENUM_CHANNEL ) )
		{
			return new MultiWellChannelFilesProviderScanR( files, imageDimensions );
		}
		else if ( namingScheme.equals( NamingSchemes.PATTERN_ALMF_TREAT1_TREAT2_WELLNUM_POSNUM_CHANNEL ) )
		{
			return new MultiWellChannelFilesProviderALMFScreening( files, imageDimensions );
		}
		else
		{
			return new DefaultMultiWellMultiSiteChannelFilesProvider( files, imageDimensions, namingScheme  );
		}
	}
}
