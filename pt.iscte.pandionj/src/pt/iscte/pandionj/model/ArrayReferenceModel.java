package pt.iscte.pandionj.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.debug.core.DebugException;
import org.eclipse.draw2d.IFigure;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;

import pt.iscte.pandionj.figures.ArrayReferenceFigure;
import pt.iscte.pandionj.figures.ArrayValueFigure;

public class ArrayReferenceModel extends Observable implements ModelElement {

	private StackFrameModel model;
	
	private IJavaArray array;
	private List<ReferenceModel> elements;

	private Map<String, ValueModel> vars;
	private ArrayValueFigure fig;
	
	private String varError;
	
	public ArrayReferenceModel(IJavaArray array, StackFrameModel model) {
		assert array != null;
		assert model != null;
		this.model = model;
		
		try {
			this.array = array;
			elements = new ArrayList<>();
			IJavaValue[] values = array.getValues();
			for(int i = 0; i < values.length; i++)
				elements.add(new ReferenceModel((IJavaVariable) array.getVariable(i), model));
		}
		catch(DebugException e) {
			e.printStackTrace();
		}
		vars = new HashMap<>();
	}

	public void update() {
		for(ReferenceModel r : elements)
			r.update();
	}

	public int getLength() {
		return elements.size();
	}

	public String get(int i) {
		assert i >= 0 && i < elements.size();
		return elements.get(i).toString();
	}

	@Override
	public IJavaValue getContent() {
		return array;
	}
	
	
	
	public List<ReferenceModel> getModelElements() {
		return Collections.unmodifiableList(elements);
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
	public IFigure createFigure() {
		return new ArrayReferenceFigure(this);
	}
	
	public ArrayValueFigure getFigure() {
		return fig;
	}
	
	@Override
	public String toString() {
		String els = "{";
		for(int i = 0; i < elements.size(); i++) {
			if(i != 0)
				els += ", ";
			els += get(i);
		}
		els += "}";
		return ArrayReferenceModel.class.getSimpleName() + " " + els;
	}

	@Override
	public void registerObserver(Observer o) {
		addObserver(o);
	}
}
