package pt.iscte.pandionj;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

public interface Constants {
	String PLUGIN_ID = Constants.class.getPackage().getName();

	int MESSAGE_FONT_SIZE = 16;

	int ARROW_EDGE = 4;
	int ARROW_LINE_WIDTH = 1;
	int POSITION_WIDTH = 32;

	int MARGIN = 20;
	int NODE_SPACING = 100;

	Dimension OBJECT_CORNER = new Dimension(10, 10);
	int OBJECT_PADDING = 5;

	int BUTTON_FONT_SIZE = 12;

	int OBJECT_HEADER_FONT_SIZE = 14;


	int VALUE_FONT_SIZE = 20;
	String FONT_FACE = "Monospace";

	int POSITION_LINE_WIDTH = 1;
	int INDEX_FONT_SIZE = 10;

	int ARRAY_POSITION_SPACING = 1;
	int ARRAY_LINE_WIDTH = 1;

	int ARRAY_LENGTH_LIMIT = 20;


	int VAR_FONT_SIZE = 18;

	interface Colors {
		Color OBJECT = new Color(Display.getDefault(), 240, 240, 240);
		Color OBJECT_HEADER_FONT = new Color(Display.getDefault(), 128, 128, 128);
		
		Color ARRAY_POSITION = ColorConstants.white;

		Color HIGHLIGHT = new Color(Display.getDefault(), 223, 234, 255);

		Color INST_POINTER = new Color(Display.getDefault(), 198, 219, 174);

		Color SELECT = new Color(Display.getDefault(), 0, 0, 200);

		Color VIEW_BACKGROUND = ColorConstants.white;

		Color ERROR = ColorConstants.red;
		Color OBSOLETE = new Color(Display.getDefault(), 150, 150, 150);
		
		Color[] ROLE_VARS = { ColorConstants.darkBlue, ColorConstants.darkGreen, ColorConstants.orange};

		static Color getVarColor(int i) {
			assert i >= 0;
			return i >= ROLE_VARS.length ? ColorConstants.black : ROLE_VARS[i];
		}
	}
	
	
	String START_MESSAGE = "This view will be populated once the execution of the Java debugger hits a breakpoint.";

	String IMAGE_FOLDER = "images";

	int STACK_LIMIT = 10; // TODO stack limit

	int STATIC_AREA_HEIGHT = 100;

	int COMBO_STRING_WIDTH = 200;

	int COMBO_WIDTH = 75;

	String CONTEXT_ID = "pt.iscte.pandionj.context";

	

	// TODO FONT FLYWEIGHT
	//	static void SET_FONT(Figure fig, int size) {
	//		fig.setFont(new Font(Display.getDefault(), FONT_FACE, size, SWT.NONE));
	//	}

	static GridLayout getOneColGridLayout() {
		GridLayout layout = new GridLayout(1, false);
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		return layout;
	}


	static final String TRASH_ICON = "trash.gif";
	static final String TRASH_MESSAGE = "Simulates the behavior of Java's garbage collector, removing all the unferenced objects.";


}
