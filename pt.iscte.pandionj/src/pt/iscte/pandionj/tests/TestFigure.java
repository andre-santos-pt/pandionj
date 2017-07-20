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

import pt.iscte.pandionj.extensibility.IArrayIndexModel;
import pt.iscte.pandionj.figures.ArrayPrimitiveFigure;
import pt.iscte.pandionj.tests.mock.MockArray;
import pt.iscte.pandionj.tests.mock.MockArrayIndex;
import pt.iscte.pandionj.tests.mock.MockVariable;

public class TestFigure {
	public static void main(String[] args) {
		Shell shell = new Shell(new Display());
		shell.setSize(1200, 500);
		shell.setLayout(new GridLayout());
		shell.setLocation(100, 150);

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
		MockArray array = new MockArray("int", 1,2,3,4,5);
		MockVariable var = new MockVariable("int[]", "v", null, array);
		MockVariable i = new MockVariable("int", "i", null, 0);
		
		MockArrayIndex i2 = new MockArrayIndex(i, var, 5, IArrayIndexModel.Direction.FORWARD);
		array.addIndexVariable(i2);
		
//		MockArrayIndex i1 = new MockArrayIndex("i1", null, 5, IArrayIndexModel.Direction.FORWARD,
//		MockArrayIndex i2 = new MockArrayIndex("i2", null, 0, IArrayIndexModel.Direction.FORWARD, i1);
//		MockArrayIndex i3 = new MockArrayIndex("i3", null, 0, IArrayIndexModel.Direction.FORWARD, 3); ;
//		array.addIndexVariable(i1);
//		array.addIndexVariable(i2);
//		array.addIndexVariable(i3);
		
		ArrayPrimitiveFigure fig = new ArrayPrimitiveFigure(array);
		fig.setSize(fig.getPreferredSize());
		fig.setLocation(new Point(100, 100));
		root.add(fig);
		
		
		MockArray array2 = new MockArray("array2", "int", 1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25);
		ArrayPrimitiveFigure fig2 = new ArrayPrimitiveFigure(array2);
		fig2.setSize(fig2.getPreferredSize());
		fig2.setLocation(new Point(250, 300));
		root.add(fig2);
		
		
		MockArray array3 = new MockArray("array3", "int");
		ArrayPrimitiveFigure fig3 = new ArrayPrimitiveFigure(array3);
		fig3.setSize(fig3.getPreferredSize());
		fig3.setLocation(new Point(400, 200));
		root.add(fig3);
		
		
		Button but = new Button("test");
		but.setLocation(new Point(5, 5));
		but.setSize(but.getPreferredSize());
		but.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				try {
					array.set(i2.getCurrentIndex(), 9);
					if(i2.getBound().getValue() != i2.getCurrentIndex()) {
						i.set(i2.getCurrentIndex() + 1);
					}
					
					if(i2.getBound().getValue() != i2.getCurrentIndex()) {
						i.set(i2.getCurrentIndex() - 1);
					}
				}
				catch(IndexOutOfBoundsException e) {
					e.printStackTrace();
				}
			}
		});
		root.add(but);
	}

}

