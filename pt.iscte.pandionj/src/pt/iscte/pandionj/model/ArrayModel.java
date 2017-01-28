package pt.iscte.pandionj.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Observable;

import org.eclipse.debug.core.DebugException;
import org.eclipse.draw2d.IFigure;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaValue;

import pt.iscte.pandionj.figures.ArrayValueFigure;

public class ArrayModel extends Observable implements ModelElement {

	private IJavaArray array;
	private IJavaValue[] elements;

	private List<PrimitiveVariableModel> vars;
	
	public ArrayModel(IJavaArray array) {
		assert array != null;
		try{
			this.array = array;
			elements = new IJavaValue[array.getLength()];
			for(int i = 0; i < elements.length; i++)
				elements[i] = array.getValue(i);
		}
		catch(DebugException e) {
			e.printStackTrace();
		}
		vars = new ArrayList<PrimitiveVariableModel>();
	}

	public void update() {
		try {
			for(int i = 0; i < elements.length; i++) {
				IJavaValue val = array.getValue(i);
				boolean equals = val.equals(elements[i]);
				elements[i] = val;
				if(!equals) {
					setChanged();
					notifyObservers(i);
				}
			}
		}
		catch(DebugException e) {
			e.printStackTrace();
		}
	}

	public int getLength() {
		return elements.length;
	}

	public String get(int i) {
		return elements[i].toString();
	}

	@Override
	public IJavaValue getContent() {
		return array;
	}
	
	public void addVar(PrimitiveVariableModel v) {
		vars.add(v);
		setChanged();
		notifyObservers(v);
	}

	public Collection<PrimitiveVariableModel> getVars() {
		return Collections.unmodifiableCollection(vars);
	}
	
	@Override
	public IFigure createFigure() {
		ArrayValueFigure fig = new ArrayValueFigure(this);
//		List<PrimitiveVariableModel> arrayVars = model.getArrayVars();
//		for(PrimitiveVariableModel v : arrayVars)
//			fig.addVariable(v);
		return fig;
	}

}
