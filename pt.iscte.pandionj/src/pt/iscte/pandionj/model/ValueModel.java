package pt.iscte.pandionj.model;

import org.eclipse.debug.core.DebugException;
import org.eclipse.draw2d.IFigure;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.zest.core.widgets.Graph;

import pt.iscte.pandionj.figures.ValueFigure;
import pt.iscte.pandionj.parser.variable.FixedValue;
import pt.iscte.pandionj.parser.variable.Gatherer;
import pt.iscte.pandionj.parser.variable.Variable;

public class ValueModel extends VariableModel<IJavaPrimitiveValue> {
	public enum Role {
		FIXED_VALUE,
		STEPPER,
		GATHERER,
		MOST_WANTED_HOLDER;
		
		
		static Role matchRole(Variable v) {
			if(v instanceof FixedValue)
				return FIXED_VALUE;
			else if(v instanceof Gatherer)
				return GATHERER;
			else
				return null;
		}
	}
	
	private Role role;
	
	public ValueModel(IJavaVariable variable, StackFrameModel model) throws DebugException {
		super(variable, model);
		assert variable.getValue() instanceof IJavaPrimitiveValue;
		Variable var = model.getLocalVariable(variable.getName());
		role = Role.matchRole(var);
	}

	@Override
	public IFigure createInnerFigure(Graph graph) {
		return new ValueFigure(this, role);
	}
	
	@Override
	public String toString() {
		try {
			return getName() + " = " + getContent().getValueString();
		} catch (DebugException e) {
			e.printStackTrace();
			return getClass().getSimpleName();
		}
	}
	
}
