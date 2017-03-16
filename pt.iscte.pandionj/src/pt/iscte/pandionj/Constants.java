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

	int MESSAGE_FONT_SIZE = 12;
	
	int ARROW_EDGE = 4;
	int ARROW_LINE_WIDTH = 1;
	int POSITION_WIDTH = 32;
	
	int MARGIN = 20;
	int NODE_SPACING = 128;
	
	Color OBJECT_COLOR = new Color(Display.getDefault(), 240, 240, 240);
	Dimension OBJECT_CORNER = new Dimension(10, 10);
	int OBJECT_PADDING = 5;
	
	int BUTTON_FONT_SIZE = 12;

	
	int VALUE_FONT_SIZE = 20;
	String FONT_FACE = "Arial";

	int POSITION_LINE_WIDTH = 1;
	int INDEX_FONT_SIZE = 10;

	int ARRAY_POSITION_SPACING = 1;
	int ARRAY_LINE_WIDTH = 1;
	
	int VAR_FONT_SIZE = 18;
	
	Color ARRAY_POSITION_COLOR = ColorConstants.white;

	Color HIGHLIGHT_COLOR = new Color(Display.getDefault(), 223, 234, 255);

	Color SELECT_COLOR = new Color(Display.getDefault(), 0, 0, 200);
	
	Color[] VARCOLORS = {
			ColorConstants.darkBlue, ColorConstants.darkGreen, ColorConstants.orange
	};

	Color ERROR_COLOR = ColorConstants.red;

	String START_MESSAGE = "This view will be populated once the execution of the Java debugger hits a breakpoint.";

	
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
