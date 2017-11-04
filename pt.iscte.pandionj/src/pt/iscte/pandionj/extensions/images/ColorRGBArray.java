package pt.iscte.pandionj.extensions.images;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;

import pt.iscte.pandionj.Constants;
import pt.iscte.pandionj.extensibility.IArrayModel;
import pt.iscte.pandionj.extensibility.IArrayWidgetExtension;
import pt.iscte.pandionj.extensibility.PandionJUI;

public class ColorRGBArray implements IArrayWidgetExtension {

	@Override
	public boolean accept(IArrayModel<?> e) {
		return e.getComponentType().equals(int.class.getName()) && e.getDimensions() == 1 && e.getLength() == 3;
	}

	@Override
	public IFigure createFigure(IArrayModel<?> e) {
		ColorFigure fig = new ColorFigure();
		updateLabel(e, fig);
		e.registerDisplayObserver((a) -> updateLabel(e, fig));
		return fig;
	}
	
	

	private void updateLabel(IArrayModel<?> e, ColorFigure fig) {
		Object[] values = e.getValues();
		int r = Integer.parseInt(values[0].toString());
		int g = Integer.parseInt(values[1].toString());
		int b = Integer.parseInt(values[2].toString());
		fig.update(r, g, b);
	}

	private static class ColorFigure extends Figure {
		Figure colorFig;
		Label text;
		
		ColorFigure() {
			setLayoutManager(new GridLayout(2, false));
			colorFig = new Figure();
			colorFig.setSize(Constants.POSITION_WIDTH, Constants.POSITION_WIDTH);
			colorFig.setOpaque(true);
			colorFig.setBorder(new LineBorder(1));
			add(colorFig);
			text = new Label();
			add(text);
		}
		
		void update(int r, int g, int b) {
			colorFig.setBackgroundColor(PandionJUI.getColor(r, g, b));
			text.setText(r + ", " + g + ", " + b);
		}
	}
}
