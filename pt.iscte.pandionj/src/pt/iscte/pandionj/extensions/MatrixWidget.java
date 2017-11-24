package pt.iscte.pandionj.extensions;

import java.util.Arrays;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

import pt.iscte.pandionj.Constants;
import pt.iscte.pandionj.extensibility.IArrayModel;
import pt.iscte.pandionj.extensibility.IArrayWidgetExtension;
import pt.iscte.pandionj.model.ModelObserver;

public class MatrixWidget implements IArrayWidgetExtension{

	@Override
	public boolean accept(IArrayModel<?> model) {
		return model.isMatrix();
	}

	@Override
	public IFigure createFigure(IArrayModel<?> model) {
		return new MatrixFigure(model);
	}

	public static class MatrixFigure extends Figure implements ModelObserver {
		boolean valid;
		IArrayModel model;
		
		public MatrixFigure(IArrayModel model) {
			this.model = model;
			org.eclipse.draw2d.GridLayout layout;

			Object[] m = (Object[]) model.getValues();
			if(m.length == 0) {
				layout = new org.eclipse.draw2d.GridLayout(1,true);
			}
			else {
				Object[] firstLine = (Object[]) m[0];
				layout = new org.eclipse.draw2d.GridLayout(firstLine.length, true);
			}
			layout.horizontalSpacing = 20;
			layout.marginWidth = 10;
			setLayoutManager(layout);

			model.registerDisplayObserver(this);

			update(m);
		}

		public void update(Object arg) {
			removeAll();
			Object[] array = (Object[]) arg;
//			valid = internalAccept(array);
			valid = model.isMatrix();
			if(valid) {
				for(Object o : array) {
					Object[] line = (Object[]) o;
					for(Object e : line) {
						String text = e == null ? "null" : (e instanceof Object[] ? Arrays.deepToString((Object[]) e) : e.toString());
						Label label = new Label(text);
						label.setForegroundColor(ColorConstants.black);
						add(label);
					}
				}
			}
			String text = Arrays.deepToString(array);
			text = text.replaceAll("\\],", "],\n");
			setToolTip(new Label(text));
			getLayoutManager().layout(this);
			repaint();
		}

		@Override
		public Dimension getPreferredSize(int wHint, int hHint) {
			return valid ? super.getPreferredSize(wHint, hHint) : new Dimension(150,50);
		}

		@Override
		protected void paintFigure(Graphics g) {
			super.paintFigure(g);
			final int LEG = 5;
			Rectangle r = getBounds();
			g.setLineWidth(1);
			g.setForegroundColor(valid ? ColorConstants.black : Constants.Colors.ERROR);
			
			g.drawLine(r.x, r.y, r.x, r.y+r.height-1);
			g.drawLine(r.x, r.y, r.x+LEG, r.y);
			g.drawLine(r.x, r.y+r.height-1, r.x+LEG, r.y+r.height-1);

			g.drawLine(r.x+r.width-1, r.y, r.x+r.width-1, r.y+r.height);
			g.drawLine(r.x+r.width-1, r.y, r.x+r.width-1-LEG, r.y);
			g.drawLine(r.x+r.width-1, r.y+r.height-1, r.x+r.width-1-LEG, r.y+r.height-1);

			if(!valid) {
				g.setForegroundColor(Constants.Colors.ERROR);
				String text = "Invalid matrix";
				int textWidth = FigureUtilities.getTextWidth(text, g.getFont());
				Point p = r.getLocation().translate(r.width/2 - textWidth/2, 5);
				g.drawText(text, p);
			}
		}
	}
}
