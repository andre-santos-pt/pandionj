package pt.iscte.pandionj.figures;


import org.eclipse.draw2d.Label;

import pt.iscte.pandionj.Constants;

public class NullFigure extends Label {

	public NullFigure() {
		setOpaque(false);
		setPreferredSize(Constants.MARGIN, Constants.MARGIN);
		setSize(-1,-1);
	}
	
}
