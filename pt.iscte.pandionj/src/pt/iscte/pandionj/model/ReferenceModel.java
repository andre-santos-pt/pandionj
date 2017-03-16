package pt.iscte.pandionj.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.debug.core.DebugException;
import org.eclipse.draw2d.IFigure;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaArrayType;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaReferenceType;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.zest.core.widgets.Graph;

import pt.iscte.pandionj.figures.ReferenceFigure;

public class ReferenceModel extends Observable implements ModelElement {

	private IJavaVariable var;
	private IJavaObject target;
	private List<IJavaObject> history;
	private StackFrameModel model;
	private final boolean isInstance;
	
	private NullModel nullModel;
	
	ReferenceModel(IJavaVariable var, boolean isInstance, StackFrameModel model) {
		this.model = model;
		this.isInstance = isInstance;
		
		try {
			assert var.getValue() instanceof IJavaObject;
			this.var = var;
			history = new ArrayList<>();
			target = (IJavaObject) var.getValue();
			history.add(target);
		}
		catch(DebugException e) {
			e.printStackTrace();
		}
	}

	public String getName() {
		try {
			return var.getName();
		} catch (DebugException e) {
			e.printStackTrace();
			return null;
		}
	}


	@Override
	public void update() {
		try {
			boolean equals = ((IJavaObject) var.getValue()).getUniqueId() == history.get(history.size()-1).getUniqueId();
			if(!equals) {
				target = (IJavaObject) var.getValue();
				history.add(target);
				setChanged();
				notifyObservers(getTarget());
			}
		}
		catch(DebugException e) {
			e.printStackTrace();
		}
	}

	@Override
	public IJavaObject getContent() {
		return target;
	}

	public ModelElement getTarget() {
		return target.isNull() ? getNullInstance(target) : model.getObject(target, true);
	}

	
	
	private NullModel getNullInstance(IJavaObject nullObj) {
		assert nullObj.isNull();
		if(nullModel == null)
			nullModel = new NullModel(nullObj);
		return nullModel;
	}

	public boolean isNull() {
		try {
			return ((IJavaObject) var.getValue()).isNull();
		} catch (DebugException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean isArrayValue() {
		try {
			return var.getValue() instanceof IJavaArray &&
					!(((IJavaArrayType) var.getJavaType()).getComponentType() instanceof IJavaReferenceType); 
		} catch (DebugException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean isInstance() {
		return isInstance;
	}
	
	@Override
	public IFigure createFigure(Graph graph) {
		return new ReferenceFigure(this);
	}
	
	@Override
	public String toString() {
		try {
			return var.getName() + " -> " + getTarget();
		} catch (DebugException e) {
			e.printStackTrace();
		}
		return super.toString();
	}
	
	@Override
	public void registerObserver(Observer o) {
		addObserver(o);
	}
}
