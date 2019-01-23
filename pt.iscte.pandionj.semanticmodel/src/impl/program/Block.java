package impl.program;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import impl.machine.ExecutionError;
import model.machine.ICallStack;
import model.machine.IValue;
import model.program.IArrayElementAssignment;
import model.program.IArrayType;
import model.program.IArrayVariableDeclaration;
import model.program.IBlock;
import model.program.IBreak;
import model.program.IContinue;
import model.program.IDataType;
import model.program.IExpression;
import model.program.IInstruction;
import model.program.ILoop;
import model.program.IProcedure;
import model.program.IProcedureCall;
import model.program.IProgramElement;
import model.program.IReturn;
import model.program.ISelection;
import model.program.ISelectionWithAlternative;
import model.program.IStatement;
import model.program.IStructMemberAssignment;
import model.program.IVariableAssignment;
import model.program.IVariableDeclaration;

class Block extends ProgramElement implements IBlock {
	private final ProgramElement parent;
	private final List<IInstruction> instructions;

	Block(ProgramElement parent, boolean addToParent) {
		this.parent = parent;
		this.instructions = new ArrayList<>();
		if(parent != null && addToParent)
			((Block) parent).addInstruction(this);
	}

	@Override
	public ProgramElement getParent() {
		return parent;
	}
	
	@Override
	public boolean isEmpty() {
		return instructions.isEmpty();
	}
	
	@Override
	public List<IInstruction> getInstructionSequence() {
		return Collections.unmodifiableList(instructions);
	}
	
	void addInstruction(IInstruction statement) {
		assert statement != null;
		instructions.add(statement);
	}

	IBlock addLooseBlock() {
		return new Block(this, false);
	}
	
	@Override
	public String toString() {
		String tabs = "";
		int d = getDepth();
//		int d = 0; // FIXME
		for(int i = 0; i < d; i++)
			tabs += "\t";
		String text = tabs.substring(1) + "{";
		for(IProgramElement s : instructions)
			text += tabs + s + (s instanceof IStatement ? ";" : "");
		return text + "}";
	}
	
	
	
	private int getDepth() {
		if(!(parent instanceof Block))
			return 1;
		else
			return 1 + ((Block) parent).getDepth();
	}
	
	private Procedure getProcedure() {
		if(parent instanceof Procedure)
			return (Procedure) parent;
		else if(parent == null)
			return null;
		else
			return ((Block)  parent).getProcedure();
	}

	@Override
	public IVariableDeclaration addVariableDeclaration(String name, IDataType type, Set<IVariableDeclaration.Flag> flags) {		
		VariableDeclaration var = new VariableDeclaration(this, name, type, flags);
		Procedure procedure = getProcedure();
		if(procedure != null)
			procedure.addVariableDeclaration(var);
		return var;
	}
	
	@Override
	public IArrayVariableDeclaration addArrayDeclaration(String name, IArrayType type, Set<IVariableDeclaration.Flag> flags) {
		ArrayVariableDeclaration var = new ArrayVariableDeclaration(this, name, type, flags);
		Procedure procedure = getProcedure();
		if(procedure != null)
			procedure.addVariableDeclaration(var);
		return var;
	}

	@Override
	public IBlock addBlock() {
		return new Block(this, true);
	}

	
	@Override
	public IVariableAssignment addAssignment(IVariableDeclaration variable, IExpression expression) {
		return new VariableAssignment(this, variable, expression);
	}

	@Override
	public IArrayElementAssignment addArrayElementAssignment(IArrayVariableDeclaration var, IExpression exp, List<IExpression> indexes) {
		return new ArrayElementAssignment(this, var, indexes, exp);
	}
	
	@Override
	public IStructMemberAssignment addStructMemberAssignment(IVariableDeclaration var, String memberId, IExpression exp) {
		return new StructMemberAssignment(this, var, memberId, exp);
	}
	
	@Override
	public ISelection addSelection(IExpression guard) {
		return new Selection(this, guard);
	}

	@Override
	public ISelectionWithAlternative addSelectionWithAlternative(IExpression guard) {
		return new SelectionWithAlternative(this, guard);
	}
	
	@Override
	public ILoop addLoop(IExpression guard) {
		return new Loop(this, guard);
	}

	@Override
	public IReturn addReturnStatement(IExpression expression) {
		return new Return(this, expression);
	}
	
	@Override
	public IProcedureCall addProcedureCall(IProcedure procedure, List<IExpression> args) {
		return new ProcedureCall(this, procedure, args);
	}
	@Override
	public IBreak addBreakStatement() {
		return new Break(this);
	}
	
	@Override
	public IContinue addContinueStatement() {
		return new Continue(this);
	}
	
	private static class Break extends Statement implements IBreak {
		public Break(IBlock parent) {
			super(parent, true);
		}
		
		@Override
		public String toString() {
			return "break";
		}
		
		@Override
		public boolean execute(ICallStack stack, List<IValue> expressions) throws ExecutionError {
			return true;
		}
	}
	
	private static class Continue extends Statement implements IContinue {
		public Continue(IBlock parent) {
			super(parent, true);
		}
		
		@Override
		public String toString() {
			return "continue";
		}
		
		@Override
		public boolean execute(ICallStack stack, List<IValue> expressions) throws ExecutionError {
			return true;
		}
	}
}
