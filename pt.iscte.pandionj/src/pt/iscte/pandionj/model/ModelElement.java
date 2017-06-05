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

	private int stepInit;
	private int scopeEnd;
	
	public ModelElement(StackFrameModel model) {
		this.model = model;
		stepInit = model.getRunningStep();
		scopeEnd = Integer.MAX_VALUE;
	}

	public abstract T getContent();

	public abstract boolean update(int step);


	public StackFrameModel getStackFrame() {
		return model;
	}

	public boolean isWithinScope() {
		return stepInit <= model.getStepPointer() && model.getStepPointer() <= scopeEnd;
	}
	
	public void setOutOfScope() {
		scopeEnd = model.getRunningStep();
		setChanged();
		notifyObservers();
	}
	
	
	public abstract void setStep(int stepPointer);
	
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

	public void unregisterObserver(Observer obs) {
		deleteObserver(obs);
	}
}