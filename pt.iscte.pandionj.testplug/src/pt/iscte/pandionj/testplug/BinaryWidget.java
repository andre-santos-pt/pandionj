package pt.iscte.pandionj.testplug;




import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;

import pt.iscte.pandionj.extensibility.IValueModel;
import pt.iscte.pandionj.extensibility.IValueWidgetExtension;
import pt.iscte.pandionj.extensibility.PandionJUI;

public class BinaryWidget implements IValueWidgetExtension {
	private Label label;
	
	@Override
	public boolean accept(IValueModel v) {
		return v.getTypeName().matches("byte|short|long|int");
	}

	@Override
	public IFigure createFigure(IValueModel v) {
		label = new Label(""); 
		label.setOpaque(true);
		label.setBackgroundColor(ColorConstants.white);
		label.setBorder(new LineBorder(1));
		label.setFont(PandionJUI.getFont(14));
		update(v, Integer.parseInt(v.getCurrentValue()));
		v.registerDisplayObserver((value) -> {
			update(v, Integer.parseInt(v.getCurrentValue()));
		});
		return label;
	}

	// TODO rever negativos
	private void update(IValueModel v, int value) {
		long n = Long.parseLong(v.getCurrentValue());
		int nBits = 0;
		String bits = Long.toBinaryString(n);
		switch(v.getTypeName()) {
		case "byte": nBits = 8; break;
		case "short": nBits = 16; break;
		case "int": nBits = 32; break;
		case "long": nBits = 64; break;
		}
		
		while(bits.length() < nBits)
			bits = "0" + bits;
		
		label.setText(bits);
		label.setToolTip(new Label(v.getCurrentValue()));
	}
}
