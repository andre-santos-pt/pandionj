package pt.iscte.pandionj;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.draw2d.Figure;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import pt.iscte.pandionj.extensibility.FontStyle;
import pt.iscte.pandionj.extensibility.PandionJConstants;

public class FontManager {
	private static Map<String, Font> instances = new HashMap<>();
	
	public static Font getFont(int size, FontStyle ... styles) {
		int sizeZoom = (int) Math.round(size);
		String key = sizeZoom + Arrays.toString(styles);
		Font f = instances.get(key);
		if(f == null || f.isDisposed()) {
			f = new Font(Display.getDefault(), PandionJConstants.FONT_FACE, sizeZoom, FontStyle.toSwtStyle(styles));
			instances.put(key, f);
		}
		return f;
	}
	
	public static void setFont(Control control, int size, FontStyle ... styles) {
		control.setFont(getFont(size, styles));
	}
	
	public static void setFont(Figure figure, int size, FontStyle ... styles) {
		figure.setFont(getFont(size, styles));
	}

	public static void dispose() {
		for(Font f : instances.values())
			f.dispose();
	}
}
