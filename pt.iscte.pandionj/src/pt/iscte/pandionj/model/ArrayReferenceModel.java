package pt.iscte.pandionj.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.draw2d.IFigure;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaArrayType;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.zest.core.widgets.Graph;

import pt.iscte.pandionj.figures.ArrayPrimitiveFigure;
import pt.iscte.pandionj.figures.ArrayReferenceFigure;

public class ArrayReferenceModel extends ArrayModel {

	private StackFrameModel model;
	
	private IJavaArray array;
	private List<ReferenceModel> elements;

	private Map<String, ValueModel> vars;
	private ArrayPrimitiveFigure fig;
	
	private String varError;
	
	private Class<?> type;
	
	public ArrayReferenceModel(IJavaArray array, StackFrameModel model) {
		assert array != null;
		assert model != null;
		this.model = model;
		
		try {
			this.array = array;
			elements = new ArrayList<>();
//			IJavaValue[] values = array.getValues();
			IVariable[] variables = array.getVariables(); // TODO
			for(int i = 0; i < variables.length; i++) {
				ReferenceModel referenceModel = new ReferenceModel((IJavaVariable) variables[i], true, model);
				elements.add(referenceModel);
				int ii = i;
				referenceModel.registerObserver(new Observer() {
					@Override
					public void update(Observable o, Object arg) {
						setChanged();
						notifyObservers(ii);
					}
				});
				ModelElement target = referenceModel.getTarget();
				if(target instanceof ArrayModel)
					target.registerObserver(new Observer() {
						
						@Override
						public void update(Observable o, Object arg) {
							setChanged();
							notifyObservers(ii);
						}
					});
			}
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
	
	public String getComponentType() { // TODO to upper	
		String type = "";
		try {
			IJavaType t = array.getJavaType();
			while(t instanceof IJavaArrayType) {
				type += "[]";
				t = ((IJavaArrayType) t).getComponentType();
			}
			type = t.getName() + type;
		} catch (DebugException e) {
			e.printStackTrace();
		}
		return type;
	}

	@Override
	public Object[] getValues() {
		Object[][] v = new Object[getLength()][];
		for(int i = 0; i < v.length; i++) {
			ModelElement target = elements.get(i).getTarget();
			if(target instanceof ArrayPrimitiveModel)
				v[i] = ((ArrayPrimitiveModel) target).getValues();
		}
		return v;
	}

	
	@Override
	public int getDimensions() { // TODO to upper
		int d = 0;
		try {
			IJavaType t = array.getJavaType();
			while(t instanceof IJavaArrayType) {
				d++;
				t = ((IJavaArrayType) t).getComponentType();
			}
		} catch (DebugException e) {
			e.printStackTrace();
		}
		return d;
	}
	
	@Override
	public boolean isPrimitiveType() {
		return false;
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
	public IFigure createFigure(Graph graph) {
		if(hasWidgetExtension())
			return extension.createFigure(this);
		else
			return new ArrayReferenceFigure(this);
	}
	
	public ArrayPrimitiveFigure getFigure() {
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
