package pt.iscte.pandionj.figures;


import org.eclipse.draw2d.Label;

import pt.iscte.pandionj.Constants;

public class NullFigure extends Label {

	public NullFigure() {
		setOpaque(true);
		setPreferredSize(Constants.MARGIN*2, Constants.MARGIN*2);
		setSize(-1,-1);
	}
	
}
