package pt.iscte.pandionj.model;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.draw2d.IFigure;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaValue;

import pt.iscte.pandionj.figures.NullFigure;

public class NullModel implements ModelElement {
	
	private static Map<ModelElement, NullModel> map = new HashMap<>();
	
	public static NullModel getInstance(ModelElement pointer, IJavaObject nullObject) {
		NullModel n = map.get(pointer);
		if(n == null) {
			n = new NullModel(nullObject);
			map.put(pointer, n);
		}
		return n;
	}
	
	private IJavaObject nullObject;
	
	private NullModel(IJavaObject nullObject) {
		this.nullObject = nullObject;
	}

	@Override
	public void update() {
		
	}

	@Override
	public IJavaValue getContent() {
		return nullObject;
	}

	@Override
	public IFigure createFigure() {
		return new NullFigure();
	}

	
}
