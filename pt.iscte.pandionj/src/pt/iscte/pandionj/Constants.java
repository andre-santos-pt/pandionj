package pt.iscte.pandionj;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.LayoutManager;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;

public interface Constants {
	String PLUGIN_ID = Constants.class.getPackage().getName();

	int ARROW_EDGE = 3;
	int ARROW_LINE_WIDTH = 2;
	int POSITION_WIDTH = 64;
	
	int MARGIN = 5;
	
	int OBJECT_SPACING = MARGIN*4;
	Color OBJECT_COLOR = new Color(Display.getDefault(), 240, 240, 240);
	Dimension OBJECT_CORNER = new Dimension(10, 10);
	
	int VALUE_FONT_SIZE = 32;
	String FONT_FACE = "Arial";

	int POSITION_LINE_WIDTH = 1;
	int INDEX_FONT_SIZE = 14;

	int ARRAY_POSITION_SPACING = 1;
	int ARRAY_LINE_WIDTH = 1;
	
	int VAR_FONT_SIZE = 20;
	
	Color ARRAY_POSITION_COLOR = ColorConstants.white;

	Color HIGHLIGHT_COLOR = new Color(null, 153, 204, 255);

	
	Color[] VARCOLORS = {
			ColorConstants.blue, ColorConstants.green
	};

	Color ERROR_COLOR = ColorConstants.red;
	
	static Color getVarColor(int i) {
		assert i >= 0;
		return i >= VARCOLORS.length ? ColorConstants.black : VARCOLORS[i];
	}
	
	static void SET_FONT(Figure fig, int size) {
		fig.setFont(new Font(Display.getDefault(), FONT_FACE, size, SWT.NONE));
	}
	
	static GridLayout getOneColGridLayout() {
		GridLayout layout = new GridLayout(1, false);
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		return layout;
	}

}
