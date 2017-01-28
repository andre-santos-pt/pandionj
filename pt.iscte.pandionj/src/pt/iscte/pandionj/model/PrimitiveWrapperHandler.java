package pt.iscte.pandionj.model;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.draw2d.Figure;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;

public class PrimitiveWrapperHandler implements TypeHandler {

	private static final String[] WRAPPER_TYPES = {
			Boolean.class.getName(),
			Byte.class.getName(),
			Short.class.getName(),
			Integer.class.getName(),
			Long.class.getName(),
			Float.class.getName(),
			Double.class.getName(),
			Character.class.getName()
	};
	
	private static final String REGEX = String.join("|", WRAPPER_TYPES);

	@Override
	public boolean qualifies(IJavaValue v) {
		try {
			return !v.isNull() && v.getJavaType().getName().matches(REGEX);
		}
		catch(DebugException ex) {
			ex.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean isValueType() {
		return true;
	}


	@Override
	public boolean expandLinks(ModelElement e) {
		return false;
	}

	@Override
	public Figure createFigure(ModelElement e) {
		return null;
	}

	@Override
	public String getTextualValue(IJavaValue v) {
		try {
			IVariable[] variables = v.getVariables();
			for(IVariable var : variables)
				if(var.getName().equals("value"))
					return var.getValue().getValueString();

			assert false;
			return null;
		} catch (DebugException e) {
			e.printStackTrace();
		}
		return null;
	}

}
