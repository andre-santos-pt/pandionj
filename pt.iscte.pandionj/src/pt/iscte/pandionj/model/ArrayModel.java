package pt.iscte.pandionj.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.debug.core.DebugException;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaArrayType;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.zest.core.widgets.Graph;

import pt.iscte.pandionj.Constants;
import pt.iscte.pandionj.ExtensionManager;
import pt.iscte.pandionj.extensibility.IArrayModel;
import pt.iscte.pandionj.extensibility.IArrayWidgetExtension;
import pt.iscte.pandionj.model.ValueModel.Role;

public abstract class ArrayModel extends EntityModel<IJavaArray> implements IArrayModel {

	private IJavaValue[] elements;

	private int dimensions;
	private String componentType;
	private Map<String, ValueModel> vars;

	private String varError;

	private IArrayWidgetExtension extension;


	ArrayModel(IJavaArray array, StackFrameModel model) {
		super(array, model);
		vars = new HashMap<>();
	}

	protected IFigure createExtensionFigure() {
		if(extension == null)
			extension = ExtensionManager.getArrayExtension(this);
		return extension.createFigure(this);
	}

	@Override
	public boolean hasWidgetExtension() {
		return extension != IArrayWidgetExtension.NULL_EXTENSION;
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
		prev = getValues();
		initArray(array);
	}

	//	private static IJavaValue[] deepCopy(IJavaValue[] array, int dim) {
	//		Object values = Array.newInstance(IJavaValue.class, dim);
	////		IJavaValue[] values = new IJavaValue[array.length];
	//		for(int i = 0; i < array.length; i++) {
	//			if(dim == 1)
	//				Array.set(values, i, array[i]);
	////				values[i] = array[i];
	//			else
	//				Array.set(values, i, deepCopy(((IJavaArray) array[i]).getValues(), dim-1));
	////				values[i] = deepCopy(((IJavaArray) array[i]).getValues(), dim-1);
	//		}
	//		
	//		return values;
	//	}

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
				for(int i = 0; i < array.length; i++) {
					IJavaValue val = values[i];
					if(!val.isNull())
						array[i] = getValues((IJavaArray) val);
				}
			}	
			else {
				PrimitiveType primitive = PrimitiveType.match(compType.getName());
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

	private Object[] prev;

	private static boolean diff(Object[] a, Object[] b, int dim) {
		if(a.length == b.length) {
			for(int i = 0; i < a.length; i++) {
				if(a[i] != null && b[i] != null && a[i].equals(b[i])) {
					if(dim > 1 && diff((Object[]) a, (Object[]) b, dim - 1))
						return true;
				}
				else if(a[i] != b[i])
					return true;
			}
			return false;
		}
		return true;
	}

	private static boolean diff2(IJavaValue[] a, IJavaValue[] b, int dim) {
		try {
			if(a.length == b.length) {
				for(int i = 0; i < a.length; i++) {
					if(!a[i].isNull() && !b[i].isNull() && a[i].equals(b[i])) {
						if(dim > 1 && diff2(((IJavaArray) a[i]).getValues(), ((IJavaArray) b[i]).getValues(), dim - 1))
							return true;
					}
					else if(!a[i].equals(b[i]))
						return true;
				}
				return false;
			}
		}
		catch(DebugException e) {
			e.printStackTrace();
		}
		return true;
	}

	public final boolean update(int step) {
		for(int i = 0; i < getLength(); i++)
			if(updateInternal(i, step))
				setChanged();

		if(!hasChanged() && getDimensions() > 1) {
			Object[] newValues = getValues(entity);
			if(diff(prev, newValues, getDimensions()))
				setChanged();
			prev = newValues;
		}
		else
			prev = getValues(entity);

		boolean hasChanged = hasChanged();
		notifyObservers(prev);
		return hasChanged;

		//		try {
			//			if(!hasChanged() && getDimensions() > 1) {
		//				IJavaValue[] newValues = entity.getValues();
		//				if(diff2(elements, newValues, getDimensions()))
		//					setChanged();
		//				elements = newValues;
		//			}
		//			else
		//				elements = entity.getValues();
		//		}
		//		catch (DebugException e) {
		//			e.printStackTrace();
		//		}


		//		try {
		//			IJavaValue[] values = entity.getValues();
		//			List<Integer> changes = new ArrayList<Integer>();
		//			for(int i = 0; i < elements.length; i++) {
		//				boolean equals = values[i].equals(elements[i]);
		//				if(!equals) {
		//					elements[i] = values[i];
		//					changes.add(i);
		//					setChanged();
		//				}
		//				if(updateInternal(i, elements[i], step))
		//					setChanged();
		//			}
		//			
		//			boolean hasChanged = hasChanged();
		//			notifyObservers(changes);
		//			return hasChanged;
		//		}
		//		catch(DebugException e) {
		//			e.printStackTrace();
		//		}

	}

	abstract boolean updateInternal(int i, int step);

	public int getLength() {
		return elements.length;
	}

	public int getDimensions() {
		return dimensions;
	}

	public String getElementString(int i) {
		return elements[i].toString();
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

	@Override // TODO elements / prev
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

		if(els.length() == 1)
			els += " ";

		els += "}";
		return els;
	}

	public Set<String> getTags() {
		Set<String> tags = new HashSet<String>();
		Collection<ReferenceModel> references = getStackFrame().getReferencesTo(this);
		for(ReferenceModel r : references)
			tags.addAll(r.getTags());
		return tags;
	}
}
