package pt.iscte.pandionj.extensions;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;

import pt.iscte.pandionj.extensibility.IArrayModel;
import pt.iscte.pandionj.extensibility.IArrayWidgetExtension;
import pt.iscte.pandionj.extensibility.PandionJUI;

public class StringCharArray implements IArrayWidgetExtension {

	@Override
	public boolean accept(IArrayModel<?> e) {
		return e.getComponentType().equals(char.class.getName()) && e.getDimensions() == 1;
	}

	@Override
	public IFigure createFigure(IArrayModel<?> e) {
		Label label = new Label();
		label.setBorder(new MarginBorder(5));
		PandionJUI.setFont(label, 18);
		updateLabel(e, label);
		e.registerDisplayObserver((a) -> updateLabel(e, label));
		return label;
	}

	private void updateLabel(IArrayModel<?> e, Label label) {
		String text = "\"";
		char[] chars = (char[]) e.getValues();
		for(char c : chars)
			text += c;
		text += "\"";
		label.setText(text);
	}

}
