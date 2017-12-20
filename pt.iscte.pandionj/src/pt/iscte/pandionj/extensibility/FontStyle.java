package pt.iscte.pandionj.extensibility;

import org.eclipse.swt.SWT;

public enum FontStyle {
	BOLD(SWT.BOLD), 
	ITALIC(SWT.ITALIC);

	private final int swtBit;

	private FontStyle(int swtBit) {
		this.swtBit = swtBit;
	}

	public static int toSwtStyle(FontStyle[] styles) {
		int code = SWT.NONE;
		for(FontStyle s : styles)
			code |= s.swtBit;
		return code;
	}
}