package pt.iscte.pandionj.extensibility;


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

import pt.iscte.pandionj.model.ArrayModel;

public class ImageWidget2 implements WidgetExtension {

	@Override
	public boolean accept(ArrayModel model) {
		if(!model.getComponentType().equals("int[][]") || model.getDimensions() != 2 || model.getLength() < 1)
			return false;

		Object[] values = model.getValues();
		int width = ((Object[]) values[0]).length;

		for(int y = 1; y < values.length; y++)
			if(values[y] == null || ((Object[]) values[y]).length != width)
				return false;

		return true;
	}


	@Override
	public IFigure createFigure(ArrayModel model) {
		return new ImageFig(model);
	}


	private class ImageFig extends Figure {
		Object[][] array;
		int width;
		int height;
		
		ImageFig(ArrayModel model) {
			this.array = (Object[][]) model.getValues();
			setLayoutManager(new FlowLayout());
			width = array[0].length;
			height = array.length;
			setSize(width, height);
			setBackgroundColor(ColorConstants.white);
			setBorder(new LineBorder(ColorConstants.gray));
			setOpaque(true);

			model.registerDisplayObserver(new Observer() {

				@Override
				public void update(Observable o, Object arg) {
					array = (Object[][]) model.getValues();
					//					for(int y = 0; y < array.length; y++)
					//						for(int x = 0; x < array[y].length; x++)
					//							data.setPixel(x, y, (int) array[y][x] == 0 ? 5 : 200);

					repaint();
					System.out.println("repaint");
				}
			});

		}

		@Override
		protected void paintFigure(Graphics g) {
			Rectangle r = getBounds();
			g.setLineWidth(1);
			g.setForegroundColor(ColorConstants.red);

			RGB[] rgb = new RGB[256];

			// build grey scale palette: 256 different grey values are generated. 
			for (int i = 0; i < 256; i++) {
			    rgb[i] = new RGB(i, i, i);
			}

			// Construct a new indexed palette given an array of RGB values.
			PaletteData paletteData = new PaletteData(rgb);

			// create an image with given dimensions, depth and color palette
			ImageData data = new ImageData(width, height, 8, paletteData);
			
//			PaletteData palette = new PaletteData(new RGB[] { white.getRGB(), black.getRGB()});
//			ImageData data = new ImageData(array[0].length, array.length, 1, palette);
			
			for(int y = 0; y < height; y++)
				for(int x = 0; x < array[y].length && x < width; x++)
					data.setPixel(x, y, (int) array[y][x]);

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
