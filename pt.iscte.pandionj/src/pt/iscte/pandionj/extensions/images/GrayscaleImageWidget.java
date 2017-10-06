package pt.iscte.pandionj.extensions.images;


import org.eclipse.draw2d.IFigure;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;

import pt.iscte.pandionj.extensibility.IArrayModel;
import pt.iscte.pandionj.extensibility.IArrayWidgetExtension;


public class GrayscaleImageWidget implements IArrayWidgetExtension {

	private static PaletteData grayscalePalette; 
	
	static {
		RGB[] rgb = new RGB[256];
		for (int i = 0; i < rgb.length; i++)
			rgb[i] = new RGB(i, i, i);
		grayscalePalette = new PaletteData(rgb);
	}
	
	
	@Override
	public boolean accept(IArrayModel<?> model) {
		return 
				model.getComponentType().equals(int.class.getName()) &&
				model.getDimensions() == 2 && 
				model.getLength() > 0 && 
				model.isMatrix();
	}


	@Override
	public IFigure createFigure(IArrayModel<?> model) {
		return new GrayscaleImageFigure(model);
	}


	static class GrayscaleImageFigure extends ImageFigure {
		public GrayscaleImageFigure(IArrayModel<?> model) {
			super(model);
		}

		protected ImageData getImageData() {
			ImageData data = new ImageData(width, height, 8, grayscalePalette);
			for(int y = 0; y < height; y++) {
				Object[] line = (Object[]) array[y];
				for(int x = 0; x < line.length && x < width; x++)
					data.setPixel(x, y, (int) line[x]);
			}
			return data;
		}
	}
}
