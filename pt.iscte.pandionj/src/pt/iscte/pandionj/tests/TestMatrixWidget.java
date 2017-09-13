package pt.iscte.pandionj.tests;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import pt.iscte.pandionj.extensions.MatrixWidget;
import pt.iscte.pandionj.tests.mock.MockArray;

public class TestMatrixWidget {
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

