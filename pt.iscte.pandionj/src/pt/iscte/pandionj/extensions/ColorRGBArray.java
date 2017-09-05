package pt.iscte.pandionj.extensions;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;

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
		Label label = new Label("    ");
		label.setBorder(new LineBorder(1));
		label.setOpaque(true);
		updateLabel(e, label);
		e.registerDisplayObserver((a) -> updateLabel(e, label));
		return label;
	}

	private void updateLabel(IArrayModel<?> e, Figure label) {
		Object[] values = e.getValues();
		int r = Integer.parseInt(values[0].toString());
		int g = Integer.parseInt(values[1].toString());
		int b = Integer.parseInt(values[2].toString());
		Label tooltip = new Label(r + ", " + g + ", " + b);
		label.setToolTip(tooltip);
		label.setBackgroundColor(PandionJUI.getColor(r, g, b));
	}

}
