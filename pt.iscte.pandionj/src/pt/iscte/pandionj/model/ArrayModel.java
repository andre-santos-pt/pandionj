package pt.iscte.pandionj.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.debug.core.DebugException;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaArrayType;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.zest.core.widgets.Graph;

import pt.iscte.pandionj.Constants;
import pt.iscte.pandionj.extensibility.IArrayModel;
import pt.iscte.pandionj.model.ValueModel.Role;

public abstract class ArrayModel extends EntityModel<IJavaArray> implements IArrayModel {

	// TODO Array Observer?
	interface ObserverTemp {
		void indexChanged(int index, Object oldValue, Object newValue);
	}
	
	protected IJavaValue[] elements;

	private int dimensions;
	private String componentType;
	private Map<String, ValueModel> vars;
	
	
	private String varError;
	
	ArrayModel(IJavaArray array, StackFrameModel model) {
		super(array, model);
		vars = new HashMap<>();
	}
	
	@Override
	protected void init(IJavaArray array) {
		entity = array;
		try {
			elements = array.getValues();
		} catch (DebugException e) {
			e.printStackTrace();
		}
		dimensions = getDimensions(array);
		componentType = getComponentType(array);
		initArray(array);
	}
	
	protected abstract void initArray(IJavaArray array);
	
	
	public Object[] getValues() {
		return getValues(entity);
	}

	private static Object[] getValues(IJavaArray javaArray) {
		try {
			IJavaType compType = ((IJavaArrayType) javaArray.getJavaType()).getComponentType();
			IJavaValue[] values = javaArray.getValues();
			Object[] array = new Object[javaArray.getLength()];

			if(compType instanceof IJavaArrayType) {
				for(int i = 0; i < array.length; i++)
					array[i] = getValues((IJavaArray) values[i]);
			}	
			else {
				PrimitiveType primitive = PrimitiveType.match(compType);
				if(primitive == null) {
					for(int j = 0; j < values.length; j++)
						array[j] = values[j].isNull() ? null : values[j].getValueString();
				}
				else {
					primitive.fillPrimitiveWrapperValues(javaArray.getValues(), array);
				}
			}
			
			return array;
		}
		catch (DebugException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public int getLength() {
		return elements.length;
	}
	
	public int getDimensions() {
		return dimensions;
	}
	
	private static int getDimensions(IJavaArray array) {
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
	
	public String getComponentType() {
		return componentType;
	}
	
//	private static String getComponentType(IJavaArray array) {
//		String type = "";
//		try {
//			IJavaType t = array.getJavaType();
//			while(t instanceof IJavaArrayType) {
//				type += "[]";
//				t = ((IJavaArrayType) t).getComponentType();
//			}
//			type = t.getName() + type;
//		} catch (DebugException e) {
//			e.printStackTrace();
//		}
//		assert !type.isEmpty();
//		return type;
//	}
	
	static String getComponentType(IJavaArray array) {
		try {
			IJavaType t = array.getJavaType();
			while(t instanceof IJavaArrayType) {
				t = ((IJavaArrayType) t).getComponentType();
			}
			return t.getName();
		} catch (DebugException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	protected IFigure createInnerFigure(Graph graph) {
		IFigure fig = createExtensionFigure();
		if(fig == null)
			fig = createArrayFigure();
		return fig;
	}

	protected abstract IFigure createArrayFigure();

	public abstract String getElementString(int index);
	
	
	
	public void addVar(ValueModel v) {
		assert v.getRole().equals(Role.ARRAY_ITERATOR);
		if(!vars.containsKey(v.getName())) {
			vars.put(v.getName(), v);
			v.addObserver((o,a) -> {
				if(v.isOutOfScope()) {
					vars.remove(v.getName()); 
					setChanged();
					notifyObservers(v);
				}
			});
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
	public boolean isMatrix() {
		if(getDimensions() != 2)
			return false;
		
		// TODO: with IJava elements
		Object[] values = getValues();
		for(Object o : values)
			if(o == null)
				return false;
		
		for(int i = 0; i < values.length-1; i++)
			if(((Object[]) values[i]).length != ((Object[]) values[i+1]).length)
				return false;
		
		return true;
	}
	
	public Dimension getMatrixDimension() {
		if(!isMatrix())
			throw new IllegalStateException("not a matrix");
		
		try {
			return new Dimension(getLength(), getLength() == 0 ? 0 : ((IJavaArray) elements[0]).getLength());
		} catch (DebugException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public String toString() {
		int lim = Math.min(Constants.ARRAY_LENGTH_LIMIT, elements.length);
		String els = "{";
		for(int i = 0; i < lim; i++) {
			if(i != 0)
				els += ", ";
			els += getElementString(i);
		}
		if(lim < elements.length)
			els += ", ...";
		
		els += "}";
		return getClass().getSimpleName() + " " + els;
	}
	
}
