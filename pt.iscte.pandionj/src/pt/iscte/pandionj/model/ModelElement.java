package pt.iscte.pandionj.model;

import java.util.Observable;
import java.util.Observer;

import org.eclipse.draw2d.IFigure;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.swt.widgets.Display;
import org.eclipse.zest.core.widgets.Graph;

import pt.iscte.pandionj.extensibility.WidgetExtension;
import pt.iscte.pandionj.figures.BaseFigure;

public abstract class ModelElement<T extends IJavaValue> extends Observable {
	
	protected StackFrameModel model;
	protected WidgetExtension extension;
	private IFigure figure;

	public ModelElement(StackFrameModel model) {
		this.model = model;
	}
	
	public void setWidgetExtension(WidgetExtension extension) {
		this.extension = extension;
	}
	
	public boolean hasWidgetExtension() {
		return extension != null;
	}
	
	public abstract T getContent();
	
	public abstract void update();
	public IFigure createFigure(Graph graph) {
		if(figure == null)
			figure = new BaseFigure(createInnerFigure(graph));
		
		return figure;
	}
	
	protected abstract IFigure createInnerFigure(Graph graph);
	
	
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