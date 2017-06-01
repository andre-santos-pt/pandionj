package pt.iscte.pandionj.figures;

import java.net.URL;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.osgi.framework.Bundle;

import pt.iscte.pandionj.Constants;
import pt.iscte.pandionj.FontManager;
import pt.iscte.pandionj.model.ValueModel;

class ValueLabel extends Label {
	private static final Image trueImage = image("true.png");
	private static final Image falseImage = image("false.png");

	ValueModel model;
	
	ValueLabel(ValueModel model) {
		this.model = model;
		setOpaque(true);
		FontManager.setFont(this, Constants.VALUE_FONT_SIZE);
		setSize(model.isDecimal() ? Constants.POSITION_WIDTH*2 : Constants.POSITION_WIDTH, Constants.POSITION_WIDTH);
		setBackgroundColor(ColorConstants.white);
		
		//		int lineWidth = Role.FIXED_VALUE.equals(role) ? Constants.ARRAY_LINE_WIDTH * 2: Constants.ARRAY_LINE_WIDTH;
		setBorder(new LineBorder(ColorConstants.black, Constants.ARRAY_LINE_WIDTH, SWT.LINE_SOLID));
		updateValue();
		model.registerDisplayObserver((o,a) -> updateValue());
		
		// TODO: nao funciona, porque o evento de step nao eh propagado
		model.getStackFrame().registerDisplayObserver(new Observer() {
			
			@Override
			public void update(Observable o, Object arg) {
				String textValue = model.getCurrentValue();
				setBackgroundColor(getText().equals(textValue) ? ColorConstants.white: Constants.HIGHLIGHT_COLOR);
			}
		});
	}
	
	// TODO image manager
	private static Image image(String name) {
		Bundle bundle = Platform.getBundle(Constants.PLUGIN_ID);
		URL imagePath = FileLocator.find(bundle, new Path(Constants.IMAGE_FOLDER + "/" + name), null);
		ImageDescriptor imageDesc = ImageDescriptor.createFromURL(imagePath);
		return imageDesc.createImage();
	}
	

	private void updateValue() {
		// TODO color update
		String textValue = model.getCurrentValue();
//		setBackgroundColor(getText().equals(textValue) ? null : Constants.HIGHLIGHT_COLOR);

		if(model.getType().equals(boolean.class.getName())) {
			setIcon(textValue.equals(Boolean.TRUE.toString()) ? trueImage : falseImage);
			setIconAlignment(PositionConstants.CENTER);
			setText("");
		}
		else {
			setText(textValue);
		}
		setToolTip(new Label(textValue));
		if(textValue.length() > (model.isDecimal() ? 5 : 2))
			FontManager.setFont(this, (int) (Constants.VALUE_FONT_SIZE*.66));
		else
			FontManager.setFont(this, Constants.VALUE_FONT_SIZE);

	}
}
