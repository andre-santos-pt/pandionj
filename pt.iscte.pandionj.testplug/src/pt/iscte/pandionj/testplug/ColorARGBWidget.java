package pt.iscte.pandionj.testplug;


import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;

import pt.iscte.pandionj.extensibility.IPropertyProvider;
import pt.iscte.pandionj.extensibility.IValueModel;
import pt.iscte.pandionj.extensibility.IValueWidgetExtension;
import pt.iscte.pandionj.extensibility.PandionJConstants;
import pt.iscte.pandionj.extensibility.PandionJUI;

public class ColorARGBWidget implements IValueWidgetExtension {
	private Label label;
	
	@Override
	public boolean accept(IValueModel v) {
		return v.getTypeName().equals(int.class.getName());
	}

	@Override
	public IFigure createFigure(IValueModel v, IPropertyProvider args) {
		label = new Label("      "); 
		label.setOpaque(true);
		label.setPreferredSize(PandionJConstants.POSITION_WIDTH, PandionJConstants.POSITION_WIDTH);
		update(Integer.parseInt(v.getCurrentValue()));
		v.registerDisplayObserver((value) -> {
			update(Integer.parseInt(v.getCurrentValue()));
		});
		return label;
	}

	private void update(int value) {
		int r = (value >> 16) & 0xFF;
		int g = (value >> 8) & 0xFF;
		int b = (value >> 0) & 0xFF;
		label.setToolTip(new Label(r + ", " + g + ", " + b));
		label.setBackgroundColor(PandionJUI.getColor(r, g, b));
	}
}
