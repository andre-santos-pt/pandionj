package pt.iscte.pandionj.extensions.images;


import java.io.File;
import java.lang.reflect.Array;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import pt.iscte.pandionj.extensibility.IArrayModel;

abstract class ImageFigure extends Figure {
	IArrayModel<?> model;
	Object array;
	boolean valid;
	int width;
	int height;

	public ImageFigure(IArrayModel<?> model) {
		init(model);
	}

	private void init(IArrayModel<?> model) {
		setLayoutManager(new FlowLayout());
		setBackgroundColor(ColorConstants.white);
		setOpaque(true);

		this.model = model;
		this.model.registerDisplayObserver((a) -> update(model.getValues()));

		update(model.getValues());
		addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent me) {
			}

			@Override
			public void mousePressed(MouseEvent me) {
			}

			@Override
			public void mouseDoubleClicked(MouseEvent me) {
				Shell shell = Display.getDefault().getActiveShell();
				if(MessageDialog.openQuestion(shell, "Save image", "Save image to file?")) {
					DirectoryDialog dialog = new DirectoryDialog(shell);
					//					   FileDialog dialog = new FileDialog(shell, SWT.OPEN);
					//					   dialog.setFilterExtensions(new String [] {"*.png"});
					//					   dialog.setFilterPath("c:\\temp");
					String result = dialog.open();
					if(result != null) {
						ImageData data = getImageData();
						Image img = new Image(Display.getDefault(), data);
						ImageLoader loader = new ImageLoader();
						loader.data = new ImageData[] {img.getImageData()};
						loader.save(result + File.separator + "image.png", SWT.IMAGE_PNG);
						img.dispose();
					}
				}
			}
		});

	}

	public void update(Object array) {
		this.array = array;
		valid = model.isMatrix();
		if(valid) {
			Dimension dim = model.getMatrixDimension();
			width = dim.width;
			height = dim.height;
//			width = Array.getLength(Array.get(array, 0));
//			height = Array.getLength(array);
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
