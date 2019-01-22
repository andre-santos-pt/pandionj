package model.program.semantics.java;

import model.program.IArrayElementAssignment;
import model.program.IDataType;
import model.program.ILoop;
import model.program.IReturn;
import model.program.ISelection;
import model.program.IStructMemberAssignment;
import model.program.IVariableAssignment;
import model.program.semantics.ISemanticProblem;
import model.program.semantics.Rule;

public class Typing extends Rule {

	@Override
	public boolean visit(IReturn returnStatement) {
		if(!returnStatement.getProcedure().getReturnType().equals(returnStatement.getExpression().getType()))
			addProblem(ISemanticProblem.create("incompatible return", returnStatement, returnStatement.getProcedure().getReturnType()));
		return false;
	}
	
	@Override
	public boolean visit(ISelection selection) {
		if(!selection.getGuard().getType().equals(IDataType.BOOLEAN))
			addProblem(ISemanticProblem.create("guard expression must evaluate to a boolean", selection.getGuard()));
		return false;
	}
	
	@Override
	public boolean visit(ILoop loop) {
		return super.visit(loop);
	}
	
	
	@Override
	public boolean visit(IVariableAssignment assignment) {
		if(!assignment.getVariable().getType().equals(assignment.getExpression().getType()))
			addProblem(ISemanticProblem.create("incompatible expression", assignment.getExpression(), assignment.getVariable()));
		return super.visit(assignment);
	}
	
	@Override
	public boolean visit(IArrayElementAssignment assignment) {
		// TODO Auto-generated method stub
		return super.visit(assignment);
	}
	
	@Override
	public boolean visit(IStructMemberAssignment assignment) {
		// TODO Auto-generated method stub
		return super.visit(assignment);
	}
	
	
	
	
}
