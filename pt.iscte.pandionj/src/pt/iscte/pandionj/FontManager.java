package pt.iscte.pandionj;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.draw2d.Figure;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import pt.iscte.pandionj.extensibility.PandionJConstants;

public class FontManager {
	public enum Style {
		BOLD(SWT.BOLD), ITALIC(SWT.ITALIC);
		
		private final int swtBit;
		
		private Style(int swtBit) {
			this.swtBit = swtBit;
		}
		static int toSwtStyle(Style[] styles) {
			int code = SWT.NONE;
			for(Style s : styles)
				code |= s.swtBit;
			return code;
		}
	}
	
	private static Map<String, Font> instances = new HashMap<>();
	
	public static Font getFont(int size, Style ... styles) {
		int sizeZoom = (int) Math.round(size);
		String key = sizeZoom + Arrays.toString(styles);
		Font f = instances.get(key);
		if(f == null || f.isDisposed()) {
			f = new Font(Display.getDefault(), PandionJConstants.FONT_FACE, sizeZoom, Style.toSwtStyle(styles));
			instances.put(key, f);
		}
		return f;
	}
	
	public static void setFont(Control control, int size, Style ... styles) {
		control.setFont(getFont(size, styles));
	}
	
	public static void setFont(Figure figure, int size, Style ... styles) {
		figure.setFont(getFont(size, styles));
	}

	public static void dispose() {
		for(Font f : instances.values())
			f.dispose();
	}
}
