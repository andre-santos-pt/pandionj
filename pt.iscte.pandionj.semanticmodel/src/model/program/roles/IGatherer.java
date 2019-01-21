package model.program.roles;

import model.program.IBinaryExpression;
import model.program.IBlock;
import model.program.IExpression;
import model.program.IProcedure;
import model.program.IVariableAssignment;
import model.program.IVariableDeclaration;
import model.program.IVariableExpression;
import model.program.operators.ArithmeticOperator;

public interface IGatherer extends IVariableRole {
	
	default String getName() {
		return "Gatherer";
	}
	
	IVariableDeclaration getSource();
	ArithmeticOperator getOperator();
	
	class Visitor implements IBlock.IVisitor {
		final IVariableDeclaration var;
		boolean allSameAcc = true;
		ArithmeticOperator operator = null;
		boolean first = true;
		
		Visitor(IVariableDeclaration var) {
			this.var = var;
		}
		
		@Override
		public boolean visitVariableAssignment(IVariableAssignment assignment) {
			if(assignment.getVariable().equals(var)) {
				if(first)
					first = false; // FIXME
				else {
					ArithmeticOperator op = getAccumulationOperator(assignment);
					if(op == null || operator != null && op != operator)
						allSameAcc = false;
					else
						operator = op;
				}
			}
			return true;
		}
	}

	static boolean isGatherer(IVariableDeclaration var) {
		Visitor v = new Visitor(var);
		((IProcedure) var.getParent()).getBody().accept(v);
		return v.allSameAcc && v.operator != null;
	}

	static IVariableRole createGatherer(IVariableDeclaration var) {
		assert isGatherer(var);
		Visitor v = new Visitor(var);
		((IProcedure) var.getParent()).getBody().accept(v);
		return new IGatherer() {
			@Override
			public IVariableDeclaration getSource() {
				return null;
			}
			
			@Override
			public ArithmeticOperator getOperator() {
				return v.operator;
			}
			
			@Override
			public String toString() {
				return getName() + "(" + getOperator() + ")";
			}
		};
	}
	
	static ArithmeticOperator getAccumulationOperator(IVariableAssignment var) {
		IExpression expression = var.getExpression();
		if(expression instanceof IBinaryExpression) {
			IBinaryExpression e = (IBinaryExpression) expression;
			IExpression left = e.getLeftExpression();
			IExpression right = e.getRightExpression();
			if(e.getOperator() instanceof ArithmeticOperator && 
				(
				left instanceof IVariableExpression && ((IVariableExpression) left).getVariable().equals(var.getVariable()) ||
				right instanceof IVariableExpression && ((IVariableExpression) right).getVariable().equals(var.getVariable()))
				)
				return (ArithmeticOperator) e.getOperator();
		}
		return null;
	}
	
	
}
