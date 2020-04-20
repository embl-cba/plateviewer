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
		MultiWellChannelFilesProvider multiWellChannelFilesProvider = null;

		if ( namingScheme.equals( NamingSchemes.PATTERN_MD_A01_SITE_WAVELENGTH ) )
		{
			multiWellChannelFilesProvider = new MultiWellChannelFilesProviderMolDevMultiSite(
					files, imageDimensions, NamingSchemes.PATTERN_MD_A01_SITE_WAVELENGTH  );
		}
		else if ( namingScheme.equals( NamingSchemes.PATTERN_MD_A01_SITE ) )
		{
			multiWellChannelFilesProvider = new MultiWellChannelFilesProviderMolDevMultiSite(
					files, imageDimensions, NamingSchemes.PATTERN_MD_A01_SITE );
		}
		else if ( namingScheme.equals( NamingSchemes.PATTERN_SCANR_WELLNUM_SITENUM_CHANNEL ) )
		{
			multiWellChannelFilesProvider = new MultiWellChannelFilesProviderScanR( files, imageDimensions );
		}
		else if ( namingScheme.equals( NamingSchemes.PATTERN_ALMF_SCREENING_WELL_SITE_CHANNEL ) )
		{
			multiWellChannelFilesProvider = new MultiWellChannelFilesProviderALMFScreening( files, imageDimensions );
		}
		else if ( namingScheme.equals( NamingSchemes.PATTERN_MD_A01_WAVELENGTH ) )
		{
			multiWellChannelFilesProvider = new MultiWellChannelFilesProviderMolDevSingleSite( files, imageDimensions );
		}

		return multiWellChannelFilesProvider;
	}
}
