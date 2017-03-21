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
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.zest.core.widgets.Graph;

import pt.iscte.pandionj.figures.ArrayReferenceFigure;

public class ArrayReferenceModel extends ArrayModel {

	private List<ReferenceModel> elements;
	private Map<String, ValueModel> vars;

	private String varError;


	public ArrayReferenceModel(IJavaArray array, StackFrameModel model) {
		super(array, model);
		assert array != null;
		assert model != null;

		try {
			elements = new ArrayList<>();
			IVariable[] variables = array.getVariables();
			for(int i = 0; i < variables.length; i++) {
				ReferenceModel referenceModel = new ReferenceModel((IJavaVariable) variables[i], true, model);
				elements.add(referenceModel);
				int ii = i;
				referenceModel.registerObserver(new Observer() {
					public void update(Observable o, Object arg) {
						setChanged();
						notifyObservers(ii);
					}
				});
				//				ModelElement target = referenceModel.getTarget();
				//				if(target instanceof ArrayModel)
				//					target.registerObserver(new Observer() {
				//						int x = 0;
				//						public void update(Observable o, Object arg) {
				////							System.out.println(x++);
				//							setChanged();
				////							notifyObservers(ii);
				//						}
				//					});
			}
		}
		catch(DebugException e) {
			e.printStackTrace();
		}
		vars = new HashMap<>();
	}

	public void update() {
		for(ReferenceModel r : elements) {
			r.update();
		}
//		Object[][] values = (Object[][]) getValues();
		setChanged();
		notifyObservers();
	}

	public int getLength() {
		return elements.size();
	}



	@Override
	public Object[] getValues() {
		try {
			IJavaValue[] values = array.getValues();
			Object[][] v = new Object[values.length][];
			for(int i = 0; i < v.length; i++) {
				IJavaValue[] line = ((IJavaArray) values[i]).getValues();
				v[i] = new Object[line.length];
				for(int j = 0; j < line.length; j++)
					v[i][j] = ((IJavaPrimitiveValue) line[j]).getIntValue(); // TODO other types
//				ModelElement target = elements.get(i).getTarget();
//				if(target instanceof ArrayPrimitiveModel)
//					v[i] = ((ArrayPrimitiveModel) target).getValues();
			}
			return v;
		} catch (DebugException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public boolean isPrimitiveType() {
		return false;
	}


	public String get(int i) {
		assert i >= 0 && i < elements.size();
		return elements.get(i).toString();
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
	public IFigure createInnerFigure(Graph graph) {
		if(hasWidgetExtension())
			return extension.createFigure(this);
		else
			return new ArrayReferenceFigure(this);
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
