package pt.iscte.pandionj.figures;


import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;

import pt.iscte.pandionj.Constants;
import pt.iscte.pandionj.FontManager;

public class StringFigure extends Label {

	public StringFigure(String content) {
		setText("\"" + content + "\"");
		FontManager.setFont(this, Constants.VALUE_FONT_SIZE);
		setBorder(new MarginBorder(Constants.OBJECT_PADDING));
	}
}
