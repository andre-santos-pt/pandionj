package pt.iscte.pandionj.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.debug.core.DebugException;
import org.eclipse.draw2d.IFigure;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.zest.core.widgets.Graph;

import pt.iscte.pandionj.figures.ArrayValueFigure;

public class ArrayModel extends Observable implements ModelElement {

	
	private IJavaArray array;
	private IJavaValue[] elements;

	private Map<String, ValueModel> vars;
	private String varError;
	
	public ArrayModel(IJavaArray array) {
		assert array != null;
		
		try {
			this.array = array;
			elements = new IJavaValue[array.getLength()];
			for(int i = 0; i < elements.length; i++)
				elements[i] = array.getValue(i);
		}
		catch(DebugException e) {
			e.printStackTrace();
		}
		vars = new HashMap<>();
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
	
	
	
	public void addVar(ValueModel v) {
		if(!vars.containsKey(v.getName())) {
			vars.put(v.getName(), v);
			setChanged();
			notifyObservers(v);
		}
	}
	
	public void setVarError(String var) {
		varError = var;
		setChanged();
		notifyObservers(new RuntimeException(var));
	}

	public Collection<ValueModel> getVars() {
		return Collections.unmodifiableCollection(vars.values());
	}

	
	@Override
	public IFigure createFigure(Graph graph) {
		return new ArrayValueFigure(this);
	}

	
	@Override
	public String toString() {
		String els = "{";
		for(int i = 0; i < elements.length; i++) {
			if(i != 0)
				els += ", ";
			els += get(i);
		}
		els += "}";
		return ArrayModel.class.getSimpleName() + " " + els;
	}

	@Override
	public void registerObserver(Observer o) {
		addObserver(o);
	}
}
