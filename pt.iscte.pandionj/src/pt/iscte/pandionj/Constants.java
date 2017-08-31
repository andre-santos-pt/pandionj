package pt.iscte.pandionj;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

public interface Constants {
	String PLUGIN_ID = Constants.class.getPackage().getName();
	String CONTEXT_ID = PLUGIN_ID + ".context";
	String VIEW_ID = PLUGIN_ID + ".view";
	
	String IMAGE_FOLDER = "images";
	
	int ARRAY_LENGTH_LIMIT = 10;
	int STACK_LIMIT = 10; // TODO stack limit
	
	
	
	int MESSAGE_FONT_SIZE = 14;

	int ARROW_EDGE = 4;
	int ARROW_LINE_WIDTH = 1;
	int POSITION_WIDTH = 32;

	int MARGIN = 20;
	int NODE_SPACING = 100;

	Dimension OBJECT_CORNER = new Dimension(15, 15);
	int OBJECT_PADDING = 2;

	int BUTTON_FONT_SIZE = 12;

	int OBJECT_HEADER_FONT_SIZE = 14;


	int VALUE_FONT_SIZE = 16;
	String FONT_FACE = "Monospace";

	int POSITION_LINE_WIDTH = 1;
	int INDEX_FONT_SIZE = 10;

	int ARRAY_POSITION_SPACING = 4;
	int ARRAY_LINE_WIDTH = 1;

	int STACKFRAME_LINE_WIDTH = 3;

	int STACKCOLUMN_MIN_WIDTH = 150;
	int STACK_TO_OBJECTS_GAP = 40;

	
	int ILLUSTRATION_LINE_WIDTH = 2;

	
	int VAR_FONT_SIZE = 18;

	interface Colors {
		Color OBJECT = new Color(Display.getDefault(), 225, 225, 225);
		Color OBJECT_HEADER_FONT = new Color(Display.getDefault(), 128, 128, 128);
		
		Color VARIABLE_BOX = new Color(Display.getDefault(), 255, 255, 255);

		Color ILLUSTRATION = ColorConstants.black;
		
		Color HIGHLIGHT = new Color(Display.getDefault(), 223, 234, 255);

		Color INST_POINTER = new Color(Display.getDefault(), 198, 219, 174);

		Color SELECT = new Color(Display.getDefault(), 0, 0, 200);

		Color VIEW_BACKGROUND = ColorConstants.white;
		Color ERROR = ColorConstants.red;
		
		Color FRAME_BORDER = new Color(Display.getDefault(), 200, 200, 200);
		
		Color OBSOLETE = new Color(Display.getDefault(), 240, 240, 240);
		
		Color[] ROLE_VARS = { ColorConstants.darkBlue, ColorConstants.darkGreen, ColorConstants.orange};
		Color CONSTANT = new Color(null, 160, 160, 160);

		static Color getVarColor(int i) {
			assert i >= 0;
			return i >= ROLE_VARS.length ? ColorConstants.black : ROLE_VARS[i];
		}
	}
	
	interface Messages {
		String START = "This view will be populated once the execution of the Java debugger hits a breakpoint.";
		String TRASH = "Simulates the behavior of Java's garbage collector, removing all the unferenced objects.";
		String RUN_DIALOG = "Do you want to open PandionJ view?";
	}
	

	



	int COMBO_STRING_WIDTH = 200;

	int COMBO_WIDTH = 75;
	
	GridData RIGHT_ALIGN = new GridData(SWT.RIGHT, SWT.DEFAULT, false, false);

	static GridLayout getOneColGridLayout() {
		GridLayout layout = new GridLayout(1, false);
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		return layout;
	}


	static final String TRASH_ICON = "trash.gif";
	

}
