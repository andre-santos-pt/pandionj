package pt.iscte.pandionj.extensions.images;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.widgets.Display;

import pt.iscte.pandionj.Constants;
import pt.iscte.pandionj.extensibility.IArrayModel;

abstract class ImageFigure extends Figure {
	IArrayModel<?> model;
	PaletteData palette;
	Object[] array;
	boolean valid;
	int width;
	int height;

	public ImageFigure(IArrayModel<?> model, PaletteData palette) {
		init(model);
		this.palette = palette;
	}

	private void init(IArrayModel<?> model) {
		setLayoutManager(new FlowLayout());
		setBackgroundColor(ColorConstants.white);
		setBorder(new LineBorder(ColorConstants.gray));
		setOpaque(true);

		this.model = model;
		this.model.registerDisplayObserver((a) -> update(a));

		update(model.getValues());
	}

	public void update(Object arg) {
		array = (Object[]) arg;
		valid = model.isMatrix();
		if(valid) {
			width = ((Object[]) array[0]).length;
			height = array.length;
		}
		else {
			width = 200;
			height = 50;
		}
		setSize(width+2, height+2);
		repaint();
	}

	public void updateModel(IArrayModel<?> model) {
		init(model);
	}

	@Override
	public Dimension getPreferredSize(int wHint, int hHint) {
		return new Dimension(width+2, height+2);
	}

	@Override
	protected void paintFigure(Graphics g) {
		Rectangle r = getBounds();
		g.setLineWidth(1);
		g.setForegroundColor(valid ? ColorConstants.lightGray : Constants.Colors.ERROR);
		g.setForegroundColor(Constants.Colors.ROLE_ANNOTATIONS);
		g.drawRectangle(r.x, r.y, r.width-1, r.height-1);
		if(valid) {
			ImageData data = getImageData();
			Image img = new Image(Display.getDefault(), data);
			g.drawImage(img, 0, 0, width, height, r.x+1, r.y+1, width, height);
			img.dispose();
		}
		else {
			g.drawText("Invalid matrix", r.getLocation().translate(5, 15));
		}
	}

	protected abstract ImageData getImageData();
}
