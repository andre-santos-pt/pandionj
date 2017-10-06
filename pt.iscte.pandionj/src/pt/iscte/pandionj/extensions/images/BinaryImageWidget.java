package pt.iscte.pandionj.extensions.images;


import org.eclipse.draw2d.IFigure;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;

import pt.iscte.pandionj.extensibility.IArrayModel;
import pt.iscte.pandionj.extensibility.IArrayWidgetExtension;


public class BinaryImageWidget implements IArrayWidgetExtension {

	private final static PaletteData binaryPalette = new PaletteData(new RGB[] { new RGB(255,255,255), new RGB(0,0,0) });
	
	@Override
	public boolean accept(IArrayModel<?> model) {
		return 
				model.getComponentType().equals(boolean.class.getName()) &&
				model.getDimensions() == 2 && 
				model.getLength() > 0 && 
				model.isMatrix();
	}


	@Override
	public IFigure createFigure(IArrayModel<?> model) {
		return new BinaryImageFigure(model);
	}


	static class BinaryImageFigure extends ImageFigure {
		public BinaryImageFigure(IArrayModel<?> model) {
			super(model);
		}

		protected ImageData getImageData() {
			ImageData data = new ImageData(width, height, 1, binaryPalette);
			for(int y = 0; y < height; y++) {
				Object[] line = (Object[]) array[y];
				for(int x = 0; x < line.length && x < width; x++)
					data.setPixel(x, y, (boolean) line[x] ? 1 : 0);
			}
			return data;
		}
	}
}
