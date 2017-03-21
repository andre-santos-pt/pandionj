package pt.iscte.pandionj.figures;

import static pt.iscte.pandionj.Constants.OBJECT_PADDING;
import static pt.iscte.pandionj.Constants.getOneColGridLayout;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MarginBorder;

public final class BaseFigure extends Figure {
	public final IFigure innerFig;
	
	public BaseFigure(IFigure innerFig) {
		assert innerFig != null;
		this.innerFig = innerFig;
		setBorder(new MarginBorder(OBJECT_PADDING));
		setLayoutManager(getOneColGridLayout());
		setOpaque(false);
		add(innerFig);
		setSize(-1,-1);
	}
}
