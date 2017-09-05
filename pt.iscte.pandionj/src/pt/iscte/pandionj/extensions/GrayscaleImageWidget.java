package pt.iscte.pandionj.extensions;


import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

import pt.iscte.pandionj.Constants;
import pt.iscte.pandionj.extensibility.IArrayModel;
import pt.iscte.pandionj.extensibility.IArrayWidgetExtension;
import pt.iscte.pandionj.model.ModelObserver;


public class GrayscaleImageWidget implements IArrayWidgetExtension {

	private static PaletteData grayscalePalette; 
	
	static {
		RGB[] rgb = new RGB[256];
		for (int i = 0; i < rgb.length; i++)
			rgb[i] = new RGB(i, i, i);
		grayscalePalette = new PaletteData(rgb);
	}
	
	
	@Override
	public boolean accept(IArrayModel model) {
		return 
				model.getComponentType().equals(int.class.getName()) &&
				model.getDimensions() == 2 && 
				model.getLength() > 0 && 
				internalAccept(model.getValues());
	}


	private static boolean internalAccept(Object[] values) {
		if(values[0] == null)
			return false;

		int width = ((Object[]) values[0]).length;

		for(int y = 1; y < values.length; y++)
			if(values[y] == null || ((Object[]) values[y]).length != width)
				return false;

		return true;
	}


	@Override
	public IFigure createFigure(IArrayModel model) {
		return new ImageFig(model);
	}


	public static class ImageFig extends Figure implements ModelObserver {
		IArrayModel model;
		Object[] array;
		boolean valid;
		int width;
		int height;

		public ImageFig(IArrayModel model) {
			init(model);
		}

		private void init(IArrayModel model) {
			setLayoutManager(new FlowLayout());
			setBackgroundColor(ColorConstants.white);
			setBorder(new LineBorder(ColorConstants.gray));
			setOpaque(true);

			this.model = model;
			this.model.registerDisplayObserver(this);
			
			update(model.getValues());
		}

		public void update(Object arg) {
			array = (Object[]) arg;
			valid = internalAccept(array);
			if(valid) {
				width = ((Object[]) array[0]).length;
				height = array.length;
			}
			else {
				width = 100;
				height = 50;
			}
			setSize(width, height);
			repaint();
		}

		public void updateModel(IArrayModel model) {
			init(model);
		}

		@Override
		protected void paintFigure(Graphics g) {
			Rectangle r = getBounds();
			g.setLineWidth(1);

			if(valid) {
				ImageData data = new ImageData(width, height, 8, grayscalePalette);
				for(int y = 0; y < height; y++) {
					Object[] line = (Object[]) array[y];
					for(int x = 0; x < line.length && x < width; x++)
						data.setPixel(x, y, (int) line[x]);
				}

				Image img = new Image(Display.getDefault(), data);
				g.drawImage(img, r.x, r.y);
				img.dispose();
			}
			else {
				g.setForegroundColor(Constants.Colors.ERROR);
				g.drawText("Invalid matrix", r.getLocation().translate(5, 15));
			}
		}

		@Override
		public Dimension getPreferredSize(int wHint, int hHint) {
			return new Dimension(width, height);
		}
	}

}
