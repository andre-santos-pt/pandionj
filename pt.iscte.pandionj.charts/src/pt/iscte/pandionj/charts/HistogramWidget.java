package pt.iscte.pandionj.charts;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Font;

import pt.iscte.pandionj.extensibility.IArrayModel;
import pt.iscte.pandionj.extensibility.IArrayWidgetExtension;
import pt.iscte.pandionj.extensibility.IPropertyProvider;
import pt.iscte.pandionj.extensibility.PandionJUI;

public class HistogramWidget implements IArrayWidgetExtension {
	private static final int HEIGHT = 100;
	private static final int MARGIN = 15;

	@Override
	public boolean accept(IArrayModel<?> e) {
		return e.getDimensions() == 1 && e.getComponentType().equals("int") && e.getLength() > 0 && e.getLength() <= 256;
	}

	@Override
	public IFigure createFigure(IArrayModel<?> e, IPropertyProvider args) {
		Integer height = args.getIntProperty("height", HEIGHT);
		return new HistFigure((IArrayModel<Integer>) e, height);
	}

	private static class HistFigure extends Figure {
		final IArrayModel<Integer> array;
		final int height;
		private int[] list;

		HistFigure(IArrayModel<Integer> array, Integer height) {
			this.array = array;
			this.height = height;
			
			setOpaque(true);	
			//			setBackgroundColor(ColorConstants.lightGray);
			//			setBorder(new LineBorder(ColorConstants.lightGray, 1));
			setLayoutManager(new FreeformLayout());
			array.registerDisplayObserver((a) -> {
				Object values = array.getValues();
				list = (int[]) values; 
				//				if(array.getRuntimeModel().isTerminated())
				//					array.unregisterObserver(this);
				repaint();
			});

		}

		@Override
		public Dimension getPreferredSize(int wHint, int hHint) {
			return new Dimension(array.getLength() + MARGIN, height + 3);
		}

		@Override
		protected void paintFigure(Graphics g) {
			super.paintFigure(g);
			Rectangle r = getClientArea();

			int maxX = list[0];
			int minX = list[0];
			for(int x = 1; x < list.length; x++) {
				if(list[x] > maxX)
					maxX = list[x];
				if(list[x] < minX)
					minX = list[x];
			}

			for(int x = 0; x < list.length; x++) {
				if(list[x] != 0) {
					int h = maxX == 0 ? 0 : (int) Math.round((list[x] * height) / (double)maxX);
					g.drawLine(r.x + x + MARGIN+1, r.y+r.height-1, r.x + x + MARGIN+1, r.y+r.height - h);
				}
			}
			g.setForegroundColor(ColorConstants.black);
			Font f = PandionJUI.getFont(8);
			g.setFont(f);
			int maxXw = FigureUtilities.getTextWidth(Integer.toString(maxX), f);
			int minXw = FigureUtilities.getTextWidth(Integer.toString(minX), f);

			g.drawText(Integer.toString(maxX), r.x + MARGIN - maxXw, r.y);
			g.drawText(Integer.toString(minX), r.x + MARGIN - minXw, r.y + r.height - MARGIN);

			g.setForegroundColor(ColorConstants.lightGray);
			g.drawRectangle(r.x+MARGIN,r.y,r.width-MARGIN+1,r.height);
		}
	}
}
