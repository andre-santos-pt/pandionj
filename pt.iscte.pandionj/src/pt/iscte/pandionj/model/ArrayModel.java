package pt.iscte.pandionj.model;

import org.eclipse.debug.core.DebugException;
import org.eclipse.draw2d.IFigure;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaArrayType;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.zest.core.widgets.Graph;

import pt.iscte.pandionj.extensibility.IArrayModel;

public abstract class ArrayModel extends EntityModel<IJavaArray> implements IArrayModel {

	protected IJavaValue[] elements;
	private int length;
	private int dimensions;
	private String componentType;
	
	ArrayModel(IJavaArray array, StackFrameModel model) {
		super(array, model);
	}
	
	@Override
	protected void init(IJavaArray array) {
		this.entity = array;
		try {
			length = array.getLength();
		} catch (DebugException e) {
			e.printStackTrace();
		}
		dimensions = getDimensions(array);
		componentType = getComponentType(array);
		initArray(array);
	}
	
	protected abstract void initArray(IJavaArray array);
	
	public abstract boolean isPrimitiveType();
	public abstract Object[] getValues();
	
	public int getLength() {
		return length;
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
	
	private static String getComponentType(IJavaArray array) {
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
		assert !type.isEmpty();
		return type;
	}
	
	protected IFigure createInnerFigure(Graph graph) {
		IFigure fig = createExtensionFigure();
		if(fig == null)
			fig = createArrayFigure();
		return fig;
	}

	protected abstract IFigure createArrayFigure();

}
