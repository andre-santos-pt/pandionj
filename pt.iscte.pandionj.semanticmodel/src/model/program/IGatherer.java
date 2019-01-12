package model.program;

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
		public void visitVariableAssignment(IVariableAssignment assignment) {
			if(assignment.getVariable().equals(var)) {
				if(first)
					first = false; // FIXME
				else {
					ArithmeticOperator op = assignment.getAccumulationOperator();
					if(op == null || operator != null && op != operator)
						allSameAcc = false;
					else
						operator = op;
				}
			}
		}
	}

	static boolean isGatherer(IVariableDeclaration var) {
		Visitor v = new Visitor(var);
		var.getProcedure().accept(v);
		return v.allSameAcc && v.operator != null;
	}

	static IVariableRole createGatherer(IVariableDeclaration var) {
		assert isGatherer(var);
		Visitor v = new Visitor(var);
		var.getProcedure().accept(v);
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
}
