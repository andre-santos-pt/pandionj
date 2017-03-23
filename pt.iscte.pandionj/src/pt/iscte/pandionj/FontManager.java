package pt.iscte.pandionj;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.draw2d.Figure;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

public class FontManager {
	public enum Style {
		BOLD, ITALIC;
	}
	
	private static Map<String, Font> instances = new HashMap<>();
	
	// TODO: zoom sync
	private static double zoom = 1.0;
	
	public static Font getFont(int size, Style ... styles) {
		int sizeZoom = (int) Math.round(size*zoom);
		String key = sizeZoom + Arrays.toString(styles);
		Font f = instances.get(key);
		if(f == null || f.isDisposed()) {
			f = new Font(Display.getDefault(), Constants.FONT_FACE, sizeZoom, SWT.NONE);
			instances.put(key, f);
		}
		return f;
	}
	
	public static void setFont(Control control, int size, Style ... styles) {
		control.setFont(getFont(size));
	}
	
	public static void setFont(Figure figure, int size, Style ... styles) {
		figure.setFont(getFont(size));
	}

	public static void dispose() {
		for(Font f : instances.values())
			f.dispose();
	}
}
