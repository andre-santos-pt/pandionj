package pt.iscte.pandionj.model;

import java.util.Observable;
import java.util.Observer;

import org.eclipse.draw2d.IFigure;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.swt.widgets.Display;
import org.eclipse.zest.core.widgets.Graph;

import pt.iscte.pandionj.extensibility.WidgetExtension;

public abstract class ModelElement extends Observable {
	
	protected WidgetExtension extension;
	
	public void setWidgetExtension(WidgetExtension extension) {
		this.extension = extension;
	}
	
	public boolean hasWidgetExtension() {
		return extension != null;
	}
	
	public abstract IJavaValue getContent();
	public abstract void update();
	public abstract IFigure createFigure(Graph graph);
	
	public void registerObserver(Observer o) {
		addObserver(o);
	}
	
	public void registerDisplayObserver(Observer obs) {
		addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				Display.getDefault().asyncExec(() -> {
					obs.update(o, arg);
				});
			}
		});
	}
}