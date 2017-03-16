package pt.iscte.pandionj.model;

import java.util.Observer;

import org.eclipse.draw2d.IFigure;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.zest.core.widgets.Graph;

import pt.iscte.pandionj.figures.NullFigure;

public class NullModel implements ModelElement {

	private final IJavaObject nullObject;

	NullModel(IJavaObject nullObject) {
		this.nullObject = nullObject;
	}

	@Override
	public void update() {

	}

	@Override
	public IJavaValue getContent() {
		return nullObject;
	}

	@Override
	public IFigure createFigure(Graph graph) {
		return new NullFigure();
	}

	@Override
	public String toString() {
		return "NULL";
	}
	
	@Override
	public void registerObserver(Observer o) {

	}

}
