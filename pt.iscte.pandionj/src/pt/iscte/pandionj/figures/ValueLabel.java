package pt.iscte.pandionj.figures;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.swt.SWT;

import pt.iscte.pandionj.Constants;
import pt.iscte.pandionj.FontManager;
import pt.iscte.pandionj.extensibility.IValueModel;

class ValueLabel extends Label {
	IValueModel model;
	boolean dirty;

	ValueLabel(IValueModel model) {
		this.model = model;
		setOpaque(true);
		FontManager.setFont(this, Constants.VALUE_FONT_SIZE);
		setSize(model.isDecimal() || model.isBoolean() ? Constants.POSITION_WIDTH*2 : Constants.POSITION_WIDTH, Constants.POSITION_WIDTH);
		setBackgroundColor(ColorConstants.white);
		setBorder(new LineBorder(ColorConstants.black, Constants.ARRAY_LINE_WIDTH, SWT.LINE_SOLID));
		updateValue();
		model.registerDisplayObserver((a) -> updateValue());
		dirty = false;
	}
	
	ValueLabel(String fixedValue, boolean isIndexLabel){
		setText(fixedValue);
		setOpaque(!isIndexLabel);
		setPreferredSize(Constants.POSITION_WIDTH, Constants.POSITION_WIDTH);
		if(isIndexLabel){
			FontManager.setFont(this, Constants.INDEX_FONT_SIZE);
			setLabelAlignment(SWT.CENTER);
			setForegroundColor(ColorConstants.gray);
			setToolTip(new Label(fixedValue));
		}else{
			FontManager.setFont(this, Constants.VALUE_FONT_SIZE);
			setBackgroundColor(ColorConstants.white);
			setBorder(new LineBorder(ColorConstants.black, Constants.ARRAY_LINE_WIDTH, SWT.LINE_SOLID));
		}
	}

	private void updateValue() {
		String textValue = model.getCurrentValue();
		dirty = !getText().equals(textValue);
		setText(textValue);
		if(model.isBoolean())
			setForegroundColor(textValue.equals(Boolean.TRUE.toString()) ? ColorConstants.green : ColorConstants.red);

		setToolTip(new Label(textValue));
		if(!model.isBoolean() && textValue.length() > (model.isDecimal() ? 5 : 2))
			FontManager.setFont(this, (int) (Constants.VALUE_FONT_SIZE*.66));
		else
			FontManager.setFont(this, Constants.VALUE_FONT_SIZE);
	}
	
	void updateBackground() {
		setBackgroundColor(dirty ? Constants.Colors.HIGHLIGHT : ColorConstants.white);
		dirty = false;
	}
}
