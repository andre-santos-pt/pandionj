package pt.iscte.pandionj;

import java.util.WeakHashMap;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

public class ColorManager {
	private static WeakHashMap<Integer, Color> cache = new WeakHashMap<Integer, Color>();

	// TODO Color dispose
	public static Color getColor(int r, int g, int b) {
		
		assert r >= 0 && r < 256;
		assert g >= 0 && g < 256;
		assert b >= 0 && b < 256;

		int val = (b << 16) + (g << 8) + r;
		Color c = cache.get(val);
		if(c == null) {
			c = new Color(Display.getDefault(), r, g, b);
			cache.put(val, c);
		}
		return c;
	}

}
