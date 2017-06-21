package pt.iscte.pandionj.figures;

import java.util.Observable;
import java.util.Observer;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.swt.SWT;

import pt.iscte.pandionj.Constants;
import pt.iscte.pandionj.FontManager;
import pt.iscte.pandionj.model.ValueModel;

class ValueLabel extends Label {
	ValueModel model;
	boolean dirty;

	ValueLabel(ValueModel model) {
		this.model = model;
		setOpaque(true);
		FontManager.setFont(this, Constants.VALUE_FONT_SIZE);
		setSize(model.isDecimal() || model.isBoolean() ? Constants.POSITION_WIDTH*2 : Constants.POSITION_WIDTH, Constants.POSITION_WIDTH);
		setBackgroundColor(ColorConstants.white);
		setBorder(new LineBorder(ColorConstants.black, Constants.ARRAY_LINE_WIDTH, SWT.LINE_SOLID));
		updateValue();
		model.registerDisplayObserver((o,a) -> updateValue());

		// TODO repor com RuntimeModel
//		model.getStackFrame().registerDisplayObserver(new Observer() {
//			public void update(Observable o, Object arg) {
//				setBackgroundColor(dirty ? Constants.HIGHLIGHT_COLOR : ColorConstants.white);
//				dirty = false;
//			}
//		});
		
		dirty = false;
	}

	private void updateValue() {
		String textValue = model.getCurrentValue();
		dirty = !getText().equals(textValue);
		setText(textValue);
		if(model.isBoolean())
			setForegroundColor(textValue.equals(Boolean.TRUE.toString()) ? ColorConstants.lightGreen : ColorConstants.red);

		setToolTip(new Label(textValue));
		if(!model.isBoolean() && textValue.length() > (model.isDecimal() ? 5 : 2))
			FontManager.setFont(this, (int) (Constants.VALUE_FONT_SIZE*.66));
		else
			FontManager.setFont(this, Constants.VALUE_FONT_SIZE);
	}
}
