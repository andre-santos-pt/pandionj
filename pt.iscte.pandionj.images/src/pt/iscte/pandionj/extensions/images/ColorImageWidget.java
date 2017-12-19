package pt.iscte.pandionj.extensions.images;

import org.eclipse.draw2d.IFigure;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;

import pt.iscte.pandionj.extensibility.IArrayModel;
import pt.iscte.pandionj.extensibility.IArrayWidgetExtension;

public class ColorImageWidget implements IArrayWidgetExtension {

	private static final int redMask = 0xFF;
	private static final int greenMask = 0xFF00;
	private static final int blueMask = 0xFF0000;
	private static final PaletteData palette = new PaletteData(redMask, greenMask, blueMask);
	private static final int redShift = palette.redShift;
	private static final int greenShift = palette.greenShift;
	private static final int blueShift = palette.blueShift;

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
		return new ColorImageFigure(model);
	}


	static class ColorImageFigure extends ImageFigure {
		public ColorImageFigure(IArrayModel<?> model) {
			super(model);
		}

		protected ImageData getImageData() {
			int[][] a = (int[][]) array;
			ImageData data = new ImageData(width, height, 24, palette);
			for(int y = 0; y < height; y++) {
				for(int x = 0; x < width; x++) {
					int v = a[y][x];
					int r = (v >> 16) & 0xFF;
					int g = (v >> 8) & 0xFF;
					int b = v & 0xFF;

					int pixel = 0;
					if(valid(r, g, b)) {
						pixel |= (redShift < 0 ? r << -redShift : r >>> redShift) & redMask;
						pixel |= (greenShift < 0 ? g << -greenShift : g >>> greenShift) & greenMask;
						pixel |= (blueShift < 0 ? b << -blueShift : b >>> blueShift) & blueMask;
					}
					//					int pixel = valid(r,g,b) ? palette.getPixel(new RGB(r,g,b)) : palette.getPixel(new RGB(255, 0, 0));
					data.setPixel(x, y, pixel);
				}
			}
			return data;
		}

		private static boolean valid(int r, int g, int b) {
			return r >= 0 && r <= 255 && g >= 0 && g <= 255 && b >= 0 && b <= 255;
		}
	}
}
