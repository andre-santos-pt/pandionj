package pt.iscte.pandionj.model;

import java.util.Observer;

import org.eclipse.draw2d.IFigure;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaValue;

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
	public IFigure createFigure() {
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
