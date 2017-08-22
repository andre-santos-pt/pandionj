package pt.iscte.pandionj.tests;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.ActionEvent;
import org.eclipse.draw2d.ActionListener;
import org.eclipse.draw2d.Button;
import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import pt.iscte.pandionj.extensibility.Direction;
import pt.iscte.pandionj.extensibility.IArrayIndexModel;
import pt.iscte.pandionj.extensibility.IArrayIndexModel.BoundType;
import pt.iscte.pandionj.extensibility.IArrayIndexModel.IBound;
import pt.iscte.pandionj.figures.ArrayPrimitiveFigure;
import pt.iscte.pandionj.figures.ArrayPrimitiveFigure;
import pt.iscte.pandionj.figures.ArrayReferenceFigure;
import pt.iscte.pandionj.tests.mock.MockArray;
import pt.iscte.pandionj.tests.mock.MockArrayIndex;
import pt.iscte.pandionj.tests.mock.MockReference;
import pt.iscte.pandionj.tests.mock.MockValue;

public class TestFigure {
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
		// Array com iteradores
		MockArray array = new MockArray("Integer", 1,2,3,4,5);
		//		MockVariable var = new MockVariable("int[]", "v", null, array);
		MockReference ref = new MockReference("int[]", "v", array, false);
		
		MockValue i1 = new MockValue("int", "i1", null, 0, false);
		MockArrayIndex ii1 = new MockArrayIndex(i1, ref, Direction.NONE);
		MockValue i2 = new MockValue("int", "i2", null, 1, false);
		MockArrayIndex ii2 = new MockArrayIndex(i2, ref, Direction.FORWARD);
		MockValue i3 = new MockValue("int", "i3", null, 5, false);
		MockArrayIndex ii3 = new MockArrayIndex(i3, ref, Direction.FORWARD, new MyBound(-1, BoundType.OPEN, "-1"));

		//		array.addIndexVariable(ii1);
		//		array.addIndexVariable(ii2);
		//		array.addIndexVariable(ii3);

		List<IArrayIndexModel> vars = new ArrayList<>();
		vars.add(ii1);
		vars.add(ii2);

		ArrayReferenceFigure fig = new ArrayReferenceFigure(array);
		fig.setLocation(new Point(100, 100));

		root.add(fig);



		//		IllustrationBorder b = new IllustrationBorder(vars, fig);
		//		MarginBorder b = new MarginBorder(15);
		//		fig.setBorder(b);



		// Array com lenght maior que o tamanho maximo da figura
		MockArray array2 = new MockArray("int", 1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25);
		ArrayPrimitiveFigure fig2 = new ArrayPrimitiveFigure(array2);
		fig2.setLocation(new Point(250, 300));
		root.add(fig2);

		// Array vazia
		MockArray array3 = new MockArray("int");
		ArrayPrimitiveFigure fig3 = new ArrayPrimitiveFigure(array3);
		fig3.setLocation(new Point(400, 200));
		root.add(fig3);

		Connection c = new PolylineConnection();
		ChopboxAnchor a = new ChopboxAnchor(fig);
		ChopboxAnchor b = new ChopboxAnchor(fig2);
		c.setSourceAnchor(a);
		c.setTargetAnchor(b);

		root.add(c);

		Button but = new Button("test");
		but.setLocation(new Point(5, 5));
		but.setSize(but.getPreferredSize());
		but.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				i1.set(ii1.getCurrentIndex()+1);
				try {
					if(ii3.getBound().getValue() != ii3.getCurrentIndex()) {
						i3.set(ii3.getCurrentIndex() - 1);
					}
					array.set(ii3.getCurrentIndex(), 9);
				}
				catch(IndexOutOfBoundsException e) {
					e.printStackTrace();
				}
				fig.repaint();
			}
		});
		root.add(but);
	}

	private static class MyBound implements IBound {
		int value;
		BoundType type; 
		String expression;

		public MyBound(int value, BoundType type, String expression) {
			this.value = value;
			this.type = type;
			this.expression = expression;
		}

		@Override
		public Integer getValue() {
			return value;
		}

		@Override
		public BoundType getType() {
			return type;
		}

		@Override
		public String getExpression() {
			return expression;
		}
	}
}
