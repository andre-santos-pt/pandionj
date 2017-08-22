package pt.iscte.pandionj.figures;

import org.eclipse.draw2d.geometry.Rectangle;

import pt.iscte.pandionj.extensibility.IArrayModel;

public abstract class AbstractArrayFigure<E> extends PandionJFigure<IArrayModel<E>> {

	public AbstractArrayFigure(IArrayModel<E> model) {
		super(model);
	}

	public int getNumberOfPositions() {
		return getModel().getLength();
	}

	public abstract Rectangle getPositionBounds(int i);

	
}
