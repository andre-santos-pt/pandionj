package pt.iscte.pandionj.model;

import java.util.Observable;
import java.util.Observer;

import org.eclipse.draw2d.IFigure;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.swt.widgets.Display;
import org.eclipse.zest.core.widgets.Graph;

import pt.iscte.pandionj.figures.BaseFigure;

public abstract class ModelElement<T extends IJavaValue> extends Observable {

	private StackFrameModel model;
	private IFigure figure;

	public ModelElement(StackFrameModel model) {
		this.model = model;
	}

	public abstract T getContent();

	public abstract void update(int step);


	public StackFrameModel getStackFrame() {
		return model;
	}

	public IFigure createFigure(Graph graph) {
		if(figure == null)
			figure = new BaseFigure(createInnerFigure(graph));

		return figure;
	}

	protected abstract IFigure createInnerFigure(Graph graph);

	protected void debugStepEvent() {
		
	}

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

	public void unregisterObserver(Observer obs) {
		deleteObserver(obs);
	}
}