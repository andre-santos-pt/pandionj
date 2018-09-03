package pt.iscte.pandionj.extensions;

import java.lang.reflect.Array;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

import pt.iscte.pandionj.extensibility.IArrayModel;
import pt.iscte.pandionj.extensibility.IArrayWidgetExtension;
import pt.iscte.pandionj.extensibility.IPropertyProvider;
import pt.iscte.pandionj.extensibility.PandionJConstants;

// TODO limitar dimensao
public class MatrixWidget implements IArrayWidgetExtension{

	@Override
	public boolean accept(IArrayModel<?> model) {
		return model.isMatrix();
	}

	@Override
	public IFigure createFigure(IArrayModel<?> model, IPropertyProvider args) {
		return new MatrixFigure(model);
	}

	public static class MatrixFigure extends Figure {
		boolean valid;
		IArrayModel<?> model;
		
		public MatrixFigure(IArrayModel<?> model) {
			this.model = model;
			org.eclipse.draw2d.GridLayout layout;
			Object array = model.getValues();
			int nLines = Array.getLength(array);
			if(nLines == 0) {
				layout = new org.eclipse.draw2d.GridLayout(1,true);
			}
			else {
				Object firstLine = Array.get(array, 0);
				layout = new org.eclipse.draw2d.GridLayout(Array.getLength(firstLine), true);
			}
			layout.horizontalSpacing = 20;
			layout.marginWidth = 10;
			setLayoutManager(layout);

			model.registerDisplayObserver((a) -> update());
			update();
		}

		private void update() {
			removeAll();
			valid = model.isMatrix();
			if(valid) {
				Object[] array = (Object[]) model.getValues();
				for(Object line : array) {
					int len = Array.getLength(line);
					for(int i = 0; i < len; i++) {
						Object e = Array.get(line, i);
						Label label = new Label(e.toString()); // TODO array deepToString
						label.setForegroundColor(ColorConstants.black);
						add(label);
					}
				}
			}
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
			g.setForegroundColor(valid ? ColorConstants.black : PandionJConstants.Colors.ERROR);
			
			g.drawLine(r.x, r.y, r.x, r.y+r.height-1);
			g.drawLine(r.x, r.y, r.x+LEG, r.y);
			g.drawLine(r.x, r.y+r.height-1, r.x+LEG, r.y+r.height-1);

			g.drawLine(r.x+r.width-1, r.y, r.x+r.width-1, r.y+r.height);
			g.drawLine(r.x+r.width-1, r.y, r.x+r.width-1-LEG, r.y);
			g.drawLine(r.x+r.width-1, r.y+r.height-1, r.x+r.width-1-LEG, r.y+r.height-1);

			if(!valid) {
				g.setForegroundColor(PandionJConstants.Colors.ERROR);
				String text = "Invalid matrix";
				int textWidth = FigureUtilities.getTextWidth(text, g.getFont());
				Point p = r.getLocation().translate(r.width/2 - textWidth/2, 5);
				g.drawText(text, p);
			}
		}
	}
}
