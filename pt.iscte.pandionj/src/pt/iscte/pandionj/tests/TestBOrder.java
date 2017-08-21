package pt.iscte.pandionj.tests;

import org.eclipse.draw2d.ActionEvent;
import org.eclipse.draw2d.ActionListener;
import org.eclipse.draw2d.Border;
import org.eclipse.draw2d.Button;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.TitleBarBorder;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class TestBOrder {
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

		Figure f = new Figure();
		f.setSize(300, 150);
		f.setOpaque(true);
		f.setBackgroundColor(ColorConstants.yellow);

		root.add(f);
		f.setLocation(new Point(50, 0));
		
		class MyBorder implements Border {

			int i = 0;
			
			@Override
			public void paint(IFigure figure, Graphics g, Insets insets) {
				Rectangle r = figure.getBounds().getShrinked(insets);
				g.drawLine(i, i, r.width, r.height);

			}

			@Override
			public boolean isOpaque() {
				return true;
			}

			@Override
			public Dimension getPreferredSize(IFigure figure) {
				return figure.getPreferredSize().expand(0, 0);
			}

			@Override
			public Insets getInsets(IFigure figure) {
				return new Insets(50);
			}
			
			public void set() {
				i+=50;
				
			}
		};
		TitleBarBorder t = new TitleBarBorder("hello");
		
		
//		MyBorder b = new MyBorder();
//		f.setBorder(b);
		
		Button but = new Button("ok");
		but.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent event) {
//				b.set();
				f.repaint();
			}
		});
		but.setLocation(new Point(0, 300));
		but.setSize(100, 20);
		root.add(but);
		
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

