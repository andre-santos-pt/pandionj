package pt.iscte.pandionj.figures;


import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;

import pt.iscte.pandionj.Constants;

public class StringFigure extends Label {

	public StringFigure(String content) {
		setText("\"" + content + "\"");
		setFont(new Font(null,"Arial", Constants.VALUE_FONT_SIZE, SWT.NONE));
		setBorder(new MarginBorder(Constants.MARGIN));
	}
}
