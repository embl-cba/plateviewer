package de.embl.cba.plateviewer.io;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import org.apache.commons.codec.binary.Base64OutputStream;
import weka.Run;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.util.Base64;
import java.util.Iterator;

public class JpegOutputStreamWriter
{
	/**
	 *
	 *
	 * @param imp
	 * @param quality value between 0 and 1.0
	 * @return
	 */
	public static String createBase64String( ImagePlus imp, float quality) {
		int width = imp.getWidth();
		int height = imp.getHeight();
		int biType = BufferedImage.TYPE_INT_RGB;
		boolean overlay = imp.getOverlay()!=null && !imp.getHideOverlay();
		ImageProcessor ip = imp.getProcessor();
		if (ip.isDefaultLut() && !imp.isComposite() && !overlay && ip.getMinThreshold()==ImageProcessor.NO_THRESHOLD)
			biType = BufferedImage.TYPE_BYTE_GRAY;
		BufferedImage bi = new BufferedImage(width, height, biType);
		String error = null;
		try {
			Graphics g = bi.createGraphics();
			Image img = imp.getImage();
			if (overlay)
				img = imp.flatten().getImage();
			g.drawImage(img, 0, 0, null);
			g.dispose();
			Iterator iter = ImageIO.getImageWritersByFormatName("jpeg");
			ImageWriter writer = (ImageWriter)iter.next();
			// File f = new File(path);
			final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			ImageOutputStream ios = ImageIO.createImageOutputStream( outputStream );
			writer.setOutput(ios);
			ImageWriteParam param = writer.getDefaultWriteParam();
			param.setCompressionMode(param.MODE_EXPLICIT);
			param.setCompressionQuality(quality);
			if (quality == 100)
				param.setSourceSubsampling(1, 1, 0, 0);
			IIOImage iioImage = new IIOImage(bi, null, null);
			writer.write(null, iioImage, param);
			ios.close();
			writer.dispose();
			return Base64.getEncoder().encodeToString( outputStream.toByteArray() );
		} catch (Exception e) {
			error = ""+e;
			IJ.error("Jpeg Writer", ""+error);
			throw new RuntimeException( "Could not produce Jpeg output stream." );
		}
	}
}
