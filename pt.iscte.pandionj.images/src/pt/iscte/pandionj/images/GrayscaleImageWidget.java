 package pt.iscte.pandionj.images;


import org.eclipse.draw2d.IFigure;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;

import pt.iscte.pandionj.extensibility.IArrayModel;
import pt.iscte.pandionj.extensibility.IArrayWidgetExtension;
import pt.iscte.pandionj.extensibility.IPropertyProvider;


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
	public IFigure createFigure(IArrayModel<?> model, IPropertyProvider args) {
		return new GrayscaleImageFigure(model);
	}

	static class GrayscaleImageFigure extends ImageFigure {
		public GrayscaleImageFigure(IArrayModel<?> model) {
			super(model);
		}

		protected ImageData getImageData() {
			int[][] a = (int[][]) array;
			ImageData data = new ImageData(width, height, 8, grayscalePalette);
			for(int y = 0; y < height; y++) {
				for(int x = 0; x < width && x < width; x++)
					data.setPixel(x, y, valid(a[y][x]) ? a[y][x] : 0);
			}
			return data;
		}
		
		private static boolean valid(int v) {
			return v >= 0 && v <= 255;
		}
	}
}
