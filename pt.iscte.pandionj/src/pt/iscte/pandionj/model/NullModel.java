package pt.iscte.pandionj.model;

import org.eclipse.draw2d.IFigure;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.zest.core.widgets.Graph;

import pt.iscte.pandionj.figures.NullFigure;

public class NullModel extends EntityModel<IJavaObject> {

	private final IJavaObject nullObject;

	NullModel(IJavaObject nullObject, StackFrameModel model) {
		super(model);
		this.nullObject = nullObject;
	}

	@Override
	public void update() {

	}

	@Override
	public IJavaObject getContent() {
		return nullObject;
	}

	@Override
	public IFigure createInnerFigure(Graph graph) {
		return new NullFigure();
	}

	@Override
	public String toString() {
		return "null";
	}
}
