package pt.iscte.pandionj.figures;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;

import pt.iscte.pandionj.Constants;
import pt.iscte.pandionj.FontManager;
import pt.iscte.pandionj.FontManager.Style;
import pt.iscte.pandionj.extensibility.IRuntimeModel;
import pt.iscte.pandionj.extensibility.IValueModel;

class ValueLabel extends Label {
	IValueModel model;
	boolean dirty;

	ValueLabel(IValueModel model) {
		this.model = model;
		setOpaque(true);
		FontManager.setFont(this, Constants.VALUE_FONT_SIZE);
		setSize(model.isDecimal() || model.isBoolean() ? Constants.POSITION_WIDTH*2 : Constants.POSITION_WIDTH, Constants.POSITION_WIDTH);
		setBackgroundColor(Constants.Colors.VARIABLE_BOX);
		setBorder(new LineBorder(ColorConstants.black, Constants.ARRAY_LINE_WIDTH, SWT.LINE_SOLID));
		updateValue();
		model.registerDisplayObserver((a) -> updateValue());
		model.getRuntimeModel().registerDisplayObserver((e) -> {
			if(e.type == IRuntimeModel.Event.Type.STEP) {
				updateBackground();
			}
		});
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
			setForegroundColor(textValue.equals(Boolean.TRUE.toString()) ? Constants.Colors.TRUE : Constants.Colors.FALSE);

		setToolTip(new Label(textValue));
		setAutoFont(Constants.VALUE_FONT_SIZE, textValue);
//		if(!model.isBoolean() && textValue.length() > (model.isDecimal() ? 5 : 2))
//			FontManager.setFont(this, (int) (Constants.VALUE_FONT_SIZE*.66));
//		else
//			FontManager.setFont(this, Constants.VALUE_FONT_SIZE);
	}
	
	private void setAutoFont(int size, String text, Style ... styles) {
		Font f = FontManager.getFont(size, styles);
		while(FigureUtilities.getTextWidth(text, f) > Constants.POSITION_WIDTH-4 && size > 8) {
			size--;
			f = FontManager.getFont(size, styles);
		}
		setFont(f);
	}
	
	void updateBackground() {
		setBackgroundColor(dirty ? Constants.Colors.HIGHLIGHT : ColorConstants.white);
		dirty = false;
	}
}
