package pt.iscte.pandionj.figures;

import org.eclipse.draw2d.Label;

public final class NullFigure extends Label {
	public NullFigure() {
		setOpaque(false);
		setSize(-1,-1);
		setText("  ");
		setToolTip(new Label("null"));
	}
}
