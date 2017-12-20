package pt.iscte.pandionj.figures;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;

import pt.iscte.pandionj.FontManager;
import pt.iscte.pandionj.extensibility.FontStyle;
import pt.iscte.pandionj.extensibility.IRuntimeModel;
import pt.iscte.pandionj.extensibility.IValueModel;
import pt.iscte.pandionj.extensibility.PandionJConstants;

class ValueLabel extends Label {
	IValueModel model;
	boolean dirty;

	ValueLabel(IValueModel model) {
		this.model = model;
		setOpaque(true);
		FontManager.setFont(this, PandionJConstants.VALUE_FONT_SIZE);
		setSize(model.isDecimal() || model.isBoolean() ? PandionJConstants.POSITION_WIDTH*2 : PandionJConstants.POSITION_WIDTH, PandionJConstants.POSITION_WIDTH);
		setForegroundColor(ColorConstants.black);
		setBackgroundColor(PandionJConstants.Colors.VARIABLE_BOX);
		setBorder(new LineBorder(ColorConstants.black, PandionJConstants.ARRAY_LINE_WIDTH, SWT.LINE_SOLID));
		updateValue();
		model.registerDisplayObserver((a) -> updateValue());
		model.getRuntimeModel().registerDisplayObserver((e) -> {
			if(e.type == IRuntimeModel.Event.Type.STEP || e.type == IRuntimeModel.Event.Type.EVALUATION) {
				updateBackground();
			}
		});
		dirty = false;
	}
	
	ValueLabel(String fixedValue, boolean isIndexLabel){
		setText(fixedValue);
		setOpaque(!isIndexLabel);
		setPreferredSize(PandionJConstants.POSITION_WIDTH, PandionJConstants.POSITION_WIDTH);
		if(isIndexLabel){
			FontManager.setFont(this, PandionJConstants.INDEX_FONT_SIZE);
			setLabelAlignment(SWT.CENTER);
			setForegroundColor(ColorConstants.gray);
			setToolTip(new Label(fixedValue));
		}else{
			FontManager.setFont(this, PandionJConstants.VALUE_FONT_SIZE);
			setBackgroundColor(ColorConstants.white);
			setBorder(new LineBorder(ColorConstants.black, PandionJConstants.ARRAY_LINE_WIDTH, SWT.LINE_SOLID));
		}
	}

	private void updateValue() {
		String textValue = model.getCurrentValue();
		dirty = !getText().equals(textValue);
		setText(textValue);
		if(model.isBoolean())
			setForegroundColor(textValue.equals(Boolean.TRUE.toString()) ? PandionJConstants.Colors.TRUE : PandionJConstants.Colors.FALSE);

		setToolTip(new Label(textValue));
		setAutoFont(PandionJConstants.VALUE_FONT_SIZE, textValue);
//		if(!model.isBoolean() && textValue.length() > (model.isDecimal() ? 5 : 2))
//			FontManager.setFont(this, (int) (Constants.VALUE_FONT_SIZE*.66));
//		else
//			FontManager.setFont(this, Constants.VALUE_FONT_SIZE);
	}
	
	private void setAutoFont(int size, String text, FontStyle ... styles) {
		Font f = FontManager.getFont(size, styles);
		while(FigureUtilities.getTextWidth(text, f) > PandionJConstants.POSITION_WIDTH-4 && size > 8) {
			size--;
			f = FontManager.getFont(size, styles);
		}
		setFont(f);
	}
	
	void updateBackground() {
		setBackgroundColor(dirty ? PandionJConstants.Colors.HIGHLIGHT : ColorConstants.white);
		dirty = false;
	}
}
