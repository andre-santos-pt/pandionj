package pt.iscte.pandionj.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.debug.core.DebugException;
import org.eclipse.draw2d.IFigure;
import org.eclipse.jdt.debug.core.IJavaArrayType;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaReferenceType;
import org.eclipse.jdt.debug.core.IJavaVariable;

import pt.iscte.pandionj.figures.ReferenceFigure;

public class ReferenceModel extends Observable implements ModelElement {

	private IJavaVariable var;
	private List<IJavaObject> history;
	private StackFrameModel model;

	private NullModel nullModel;
	
	ReferenceModel(IJavaVariable var, StackFrameModel model) {
		this.model = model;
		try {
			assert var.getJavaType() instanceof IJavaReferenceType;
			this.var = var;
			history = new ArrayList<>();
			history.add((IJavaObject) var.getValue());
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
				history.add((IJavaObject) var.getValue());
				setChanged();
				notifyObservers(getTarget());
			}
//			getTarget().update();
		}
		catch(DebugException e) {
			e.printStackTrace();
		}
	}

	@Override
	public IJavaObject getContent() {
		try {
			return (IJavaObject) var.getValue();
		} catch (DebugException e) {
			e.printStackTrace();
			return null;
		}
	}

	public ModelElement getTarget() {
		IJavaObject obj = getContent();
		return obj.isNull() ? getNullInstance(obj) : model.getObject(getContent(), true);
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

	public boolean isArray() {
		try {
			return var.getJavaType() instanceof IJavaArrayType;
		} catch (DebugException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public IFigure createFigure() {
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
