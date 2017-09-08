package pt.iscte.pandionj.extensions;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import pt.iscte.pandionj.Constants;
import pt.iscte.pandionj.extensibility.IArrayModel;
import pt.iscte.pandionj.extensibility.IArrayWidgetExtension;
import pt.iscte.pandionj.model.ModelObserver;
import pt.iscte.pandionj.tests.mock.MockArray;

public class MatrixWidget implements IArrayWidgetExtension{

	@Override
	public boolean accept(IArrayModel model) {
		if(model.getDimensions() != 2 || model.getLength() < 1)
			return false;
		return internalAccept(model.getValues());
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
		return new MatrixFigure(model);
	}

	public static class MatrixFigure extends Figure implements ModelObserver {
		IArrayModel model;
		Object[] array;
		boolean valid;
		int width;
		int height;

		public MatrixFigure(IArrayModel model) {
			init(model);
		}

		private void init(IArrayModel model) {
			Object[][] m = (Object[][]) model.getValues();
			setLayoutManager(new org.eclipse.draw2d.GridLayout(m.length == 0 ? 1 : (m[0].length), true));
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


		@Override
		protected void paintFigure(Graphics g) {
			Rectangle r = getBounds();
			g.setLineWidth(1);

			if(valid) {
				// TODO
//				ImageData data = new ImageData(width, height, 8, grayscalePalette);
//				for(int y = 0; y < height; y++) {
//					Object[] line = (Object[]) array[y];
//					for(int x = 0; x < line.length && x < width; x++)
//						data.setPixel(x, y, (int) line[x]);
//				}
//
//				Image img = new Image(Display.getDefault(), data);
//				g.drawImage(img, r.x, r.y);
//				img.dispose();
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
	
	
	/// XXX para testar
	public static void main(String[] args) {
		Shell shell = new Shell(new Display());
		shell.setSize(1200, 500);
		shell.setLayout(new GridLayout());
		shell.setLocation(100, 150);

		Figure root = new Figure();
		root.setFont(shell.getFont());
		//		XYLayout layout = new XYLayout();
		//		root.setLayoutManager(layout);

		org.eclipse.draw2d.GridLayout layout = new org.eclipse.draw2d.GridLayout(2,false);
		layout.horizontalSpacing = 100;
		root.setLayoutManager(layout);

		Canvas canvas = new Canvas(shell, SWT.DOUBLE_BUFFERED);
		canvas.setBackground(ColorConstants.white);
		canvas.setLayoutData(new GridData(GridData.FILL_BOTH));

		MatrixWidget widget = new MatrixWidget();
		MockArray array = new MockArray("int[]", new int[]{1,2,3}, new int[]{4,5,6}, new int[]{7,8,9}, new int[]{10,11,12});
		root.add(widget.createFigure(array));
		
		LightweightSystem lws = new LightweightSystem(canvas);
		lws.setContents(root);

		Display display = shell.getDisplay();
		shell.open();
		while (!shell.isDisposed()) {
			while (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}
}
