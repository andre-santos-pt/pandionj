package pt.iscte.pandionj.extensions;


import java.util.Observable;
import java.util.Observer;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

import pt.iscte.pandionj.extensibility.IArrayModel;
import pt.iscte.pandionj.extensibility.IArrayWidgetExtension;
import pt.iscte.pandionj.model.ArrayModel;

public class ImageWidget implements IArrayWidgetExtension {

	@Override
	public boolean accept(IArrayModel model) {
		if(!model.getComponentType().equals("int") || model.getDimensions() != 2 || model.getLength() < 1)
			return false;

		Object[] values = model.getValues();
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


	public static class ImageFig extends Figure implements Observer {
		IArrayModel model;
		Object[] array;
		int width;
		int height;
		
		public ImageFig(IArrayModel model) {
			init(model);
		}

		private void init(IArrayModel model) {
			if(this.model != null)
				this.model.unregisterObserver(this);
			
			this.model = model;
			this.array = model.getValues();
			setLayoutManager(new FlowLayout());
			width = ((Object[]) array[0]).length;
			height = array.length;
			setSize(width, height);
			setBackgroundColor(ColorConstants.white);
			setBorder(new LineBorder(ColorConstants.gray));
			setOpaque(true);

			this.model.registerDisplayObserver(this);
		}
		
		public void update(Observable o, Object arg) {
			array = model.getValues();
			repaint();
		}
		
		public void updateModel(IArrayModel model) {
			init(model);
		}

		@Override
		protected void paintFigure(Graphics g) {
			Rectangle r = getBounds();
			g.setLineWidth(1);
			RGB[] rgb = new RGB[256];

			// build grey scale palette: 256 different grey values are generated. 
			for (int i = 0; i < rgb.length; i++) {
			    rgb[i] = new RGB(i, i, i);
			}

			// Construct a new indexed palette given an array of RGB values.
			PaletteData paletteData = new PaletteData(rgb);

			// create an image with given dimensions, depth and color palette
			ImageData data = new ImageData(width, height, 8, paletteData);
			
//			PaletteData palette = new PaletteData(new RGB[] { white.getRGB(), black.getRGB()});
//			ImageData data = new ImageData(array[0].length, array.length, 1, palette);
			
			for(int y = 0; y < height; y++) {
				Object[] line = (Object[]) array[y];
				for(int x = 0; x < line.length && x < width; x++)
					data.setPixel(x, y, (int) line[x]);
			}

			Image img = new Image(Display.getDefault(), data);
			g.drawImage(img, r.x, r.y);
			img.dispose();
		}

		@Override
		public Dimension getPreferredSize(int wHint, int hHint) {
			return new Dimension(width, height);
		}
	}

}
