package pt.iscte.pandionj.model;

import java.util.Observer;

import org.eclipse.debug.core.DebugException;
import org.eclipse.draw2d.IFigure;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaArrayType;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.zest.core.widgets.Graph;

import pt.iscte.pandionj.extensibility.IArrayModel;
import pt.iscte.pandionj.extensibility.IArrayWidgetExtension;

public abstract class ArrayModel extends EntityModel<IJavaArray> implements IArrayModel {

	protected IJavaArray array;
	protected IJavaValue[] elements;
	private int length;
	private int dimensions;
	private String componentType;
	
	ArrayModel(IJavaArray array, StackFrameModel model) {
		super(model);
		assert array != null;
		assert model != null;
		this.array = array;
		try {
			length = array.getLength();
		} catch (DebugException e) {
			e.printStackTrace();
		}
		dimensions = getDimensions(array);
		componentType = getComponentType(array);
	}
	
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
		return type;
	}
	
	public IJavaArray getContent() {
		return array;
	}
	
	protected IFigure createInnerFigure(Graph graph) {
		if(hasWidgetExtension())
			return createExtensionFigure();
		else
			return createArrayFigure();
	}

	protected abstract IFigure createArrayFigure();

}
