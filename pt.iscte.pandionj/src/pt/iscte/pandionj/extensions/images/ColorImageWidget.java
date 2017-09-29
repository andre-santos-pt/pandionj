package pt.iscte.pandionj.extensions.images;

import org.eclipse.draw2d.IFigure;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;

import pt.iscte.pandionj.extensibility.IArrayModel;
import pt.iscte.pandionj.extensibility.IArrayWidgetExtension;

public class ColorImageWidget implements IArrayWidgetExtension {

	private static PaletteData palette = new PaletteData(0xFF , 0xFF00 , 0xFF0000);

	@Override
	public boolean accept(IArrayModel<?> model) {
		return 
				model.getComponentType().equals(int.class.getName()) &&
				model.getDimensions() == 3 && 
				model.getLength() > 0 && 
				model.isMatrix() &&
				checkPixels(model);
	}


	private boolean checkPixels(IArrayModel<?> model) {
		Object[][][] values = (Object[][][]) model.getValues();

		for(int y = 0; y < values.length; y++)
			for(int x = 0; x < values[y].length; x++) {
				if(values[y][x].length != 3)
					return false;
				for(int i = 0; i < 3; i++)
					if((int) values[y][x][i] < 0 || (int) values[y][x][i] > 255)
						return false;
			}

		return true;
	}


	@Override
	public IFigure createFigure(IArrayModel<?> model) {
		return new ColorImageFigure(model, palette);
	}


	static class ColorImageFigure extends ImageFigure {
		public ColorImageFigure(IArrayModel<?> model, PaletteData palette) {
			super(model, palette);
		}

		protected ImageData getImageData() {
			ImageData data = new ImageData(width, height, 24, palette);
			for(int y = 0; y < height; y++) {
				Object[][] line = (Object[][]) array[y];
				for(int x = 0; x < line.length && x < width; x++)
					data.setPixel(x, y, (int) line[x][0]);
			}
			return data;
		}
	}
}