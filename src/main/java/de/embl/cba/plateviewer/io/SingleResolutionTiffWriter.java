package de.embl.cba.plateviewer.io;

import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.common.services.ServiceFactory;
import loci.formats.ImageWriter;
import loci.formats.meta.IMetadata;
import loci.formats.out.TiffWriter;
import loci.formats.services.OMEXMLService;
import loci.formats.tiff.IFD;
import ome.xml.model.enums.DimensionOrder;
import ome.xml.model.enums.PixelType;
import ome.xml.model.primitives.PositiveInteger;

public class SingleResolutionTiffWriter
{
	public SingleResolutionTiffWriter() throws DependencyException, ServiceException
	{

//		ServiceFactory factory = new ServiceFactory();
//		OMEXMLService service = factory.getInstance(OMEXMLService.class);
//		IMetadata omexml = service.createOMEXMLMetadata();
//		omexml.setImageID("Image:0", 0);
//		omexml.setPixelsID("Pixels:0", 0);
//		omexml.setPixelsBinDataBigEndian(Boolean.TRUE, 0, 0);
//		omexml.setPixelsDimensionOrder( DimensionOrder.XYZCT, 0);
//		if (imp.getBytesPerPixel() == 2) {
//			omexml.setPixelsType( PixelType.UINT16, 0);
//		} else if (imp.getBytesPerPixel() == 1) {
//			omexml.setPixelsType(PixelType.UINT8, 0);
//		}
//		omexml.setPixelsSizeX(new PositiveInteger(imp.getWidth()), 0);
//		omexml.setPixelsSizeY(new PositiveInteger(imp.getHeight()), 0);
//		omexml.setPixelsSizeZ(new PositiveInteger(imp.getNSlices()), 0);
//		omexml.setPixelsSizeC(new PositiveInteger(1), 0);
//		omexml.setPixelsSizeT(new PositiveInteger(1), 0);
//
//		int channel = 0;
//		omexml.setChannelID("Channel:0:" + channel, 0, channel);
//		omexml.setChannelSamplesPerPixel(new PositiveInteger(1), 0, channel);
//
//		ImageWriter writer = new ImageWriter();
//		writer.setCompression( TiffWriter.COMPRESSION_LZW);
//		writer.setValidBitsPerPixel(imp.getBytesPerPixel() * 8);
//		writer.setMetadataRetrieve(omexml);
//		writer.setId(pathCT);
//		writer.setWriteSequentially(true); // ? is this necessary
//		TiffWriter tiffWriter = (TiffWriter) writer.getWriter();
//		long[] rowsPerStripArray = new long[1];
//		rowsPerStripArray[0] = rowsPerStrip;
//
//		for (int z = 0; z < imp.getNSlices(); z++) {
//			if (stop.get()) {
//				logger.progress("Stopped saving thread: ", "" + t);
//				savingSettings.saveProjection = false;
//				return;
//			}
//
//			IFD ifd = new IFD();
//			ifd.put(IFD.ROWS_PER_STRIP, rowsPerStripArray);
//			//tiffWriter.saveBytes(z, Bytes.fromShorts((short[])image.getStack().getProcessor(z+1).getPixels(), false), ifd);
//			if (imp.getBytesPerPixel() == 2) {
//				tiffWriter.saveBytes(z, ShortToByteBigEndian((short[]) imp.getStack().getProcessor(z + 1).getPixels()), ifd);
//			} else if (imp.getBytesPerPixel() == 1) {
//				tiffWriter.saveBytes(z, (byte[]) (imp.getStack().getProcessor(z + 1).getPixels()), ifd);
//
//			}
//		}
//		writer.close();


	}
}
