package pt.iscte.pandionj.tests;

import org.eclipse.draw2d.ActionEvent;
import org.eclipse.draw2d.ActionListener;
import org.eclipse.draw2d.Button;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import pt.iscte.pandionj.figures.ArrayPrimitiveFigure;
import pt.iscte.pandionj.tests.mock.MockArray;
import pt.iscte.pandionj.tests.mock.MockVariable;

public class TestFigure {
	public static void main(String[] args) {
		Shell shell = new Shell(new Display());
		shell.setSize(365, 280);
		shell.setLayout(new GridLayout());

		Figure root = new Figure();
		root.setFont(shell.getFont());
		XYLayout layout = new XYLayout();
		root.setLayoutManager(layout);

		Canvas canvas = new Canvas(shell, SWT.DOUBLE_BUFFERED);
		canvas.setBackground(ColorConstants.white);
		canvas.setLayoutData(new GridData(GridData.FILL_BOTH));

		createDiagram(root);
		
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
	

	private static void createDiagram(IFigure root) {
		MockArray array = new MockArray("int", 1,2,3,4);
		MockVariable var = new MockVariable("int", "i", null, 0);
		array.addVariableRole(var);
		ArrayPrimitiveFigure fig = new ArrayPrimitiveFigure(array);
		fig.setSize(fig.getPreferredSize());
		fig.setLocation(new Point(100, 100));
		root.add(fig);
		
		
		Button but = new Button("test");
		but.setLocation(new Point(5, 5));
		but.setSize(but.getPreferredSize());
		but.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				try {
					int i = Integer.parseInt(var.getCurrentValue());
					array.set(i, 9);
					var.set(i-1);
					System.out.println(i);
				}
				catch(IndexOutOfBoundsException e) {
					e.printStackTrace();
				}
			}
		});
		root.add(but);
	}

}

