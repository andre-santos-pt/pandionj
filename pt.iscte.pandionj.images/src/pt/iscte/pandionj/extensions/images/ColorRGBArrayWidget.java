package pt.iscte.pandionj.extensions.images;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;

import pt.iscte.pandionj.extensibility.PandionJConstants;
import pt.iscte.pandionj.extensibility.IArrayModel;
import pt.iscte.pandionj.extensibility.IArrayWidgetExtension;
import pt.iscte.pandionj.extensibility.PandionJUI;

// TODO alpha
public class ColorRGBArrayWidget implements IArrayWidgetExtension {

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
		int[] values = (int[]) e.getValues();
		fig.update(values[0], values[1], values[2]);
	}

	private static class ColorFigure extends Figure {
		Figure colorFig;
		Label text;

		ColorFigure() {
			setLayoutManager(new GridLayout(2, false));
			colorFig = new Figure();
			colorFig.setSize(PandionJConstants.POSITION_WIDTH, PandionJConstants.POSITION_WIDTH);
			add(colorFig);
			text = new Label();
			text.setForegroundColor(ColorConstants.black);
			add(text);
		}

		void update(int r, int g, int b) {
			boolean valid = 
					r >= 0 && r <= 255 && 
					g >= 0 && g <= 255 && 
					b >= 0 && b <= 255;
			colorFig.setOpaque(valid);
			colorFig.setBackgroundColor(valid ? PandionJUI.getColor(r, g, b) : null);
			colorFig.setBorder(new LineBorder(valid ? ColorConstants.black : PandionJConstants.Colors.ERROR));
			if(valid)
				colorFig.setToolTip(null);
			else {
				Label label = new Label("Invalid RGB values");
				label.setForegroundColor(PandionJConstants.Colors.ERROR);
				colorFig.setToolTip(label);
			}
			text.setText(r + ", " + g + ", " + b);
		}
	}


}
