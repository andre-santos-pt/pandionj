package pt.iscte.pandionj.figures;

import static pt.iscte.pandionj.Constants.OBJECT_PADDING;
import static pt.iscte.pandionj.Constants.getOneColGridLayout;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MarginBorder;

import pt.iscte.pandionj.model.ModelElement;

public final class BaseFigure extends Figure {
	public final ModelElement model;
	public final IFigure innerFig;
	
	public BaseFigure(ModelElement model, IFigure innerFig) {
		assert model != null;
		this.model = model;
		this.innerFig = innerFig;
		setBorder(new MarginBorder(OBJECT_PADDING));
		setLayoutManager(getOneColGridLayout());
		setOpaque(false);
		add(innerFig);
		setSize(-1,-1);
	}
}
