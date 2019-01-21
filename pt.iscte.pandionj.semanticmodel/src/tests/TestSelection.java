package tests;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

import impl.machine.ProgramState;
import impl.program.Factory;
import model.machine.IExecutionData;
import model.program.IBinaryExpression;
import model.program.IBinaryOperator;
import model.program.IBlock;
import model.program.IDataType;
import model.program.IExpression;
import model.program.IFactory;
import model.program.IOperator;
import model.program.IProcedure;
import model.program.IProgram;
import model.program.ISelection;
import model.program.ISelectionWithAlternative;
import model.program.IVariableDeclaration;


// TODO if if
public class TestSelection {
	private static IProgram program;
	private static IProcedure maxFunc;
	
	@BeforeClass
	public static void setup() {
		IFactory factory = new Factory();
		program = factory.createProgram();
		maxFunc = program.addProcedure("max", IDataType.INT);
		IVariableDeclaration aParam = maxFunc.addParameter("a", IDataType.INT);
		IVariableDeclaration bParam = maxFunc.addParameter("b", IDataType.INT);
		
		IBinaryExpression e = factory.binaryExpression(IBinaryOperator.GREATER, aParam.expression(), bParam.expression());
		ISelectionWithAlternative ifElse = maxFunc.getBody().addSelectionWithAlternative(e);
		ifElse.addReturnStatement(aParam.expression());
		IBlock elseblock = ifElse.getAlternativeBlock();
		elseblock.addReturnStatement(bParam.expression());
		System.out.println(program);
	}

	@Test
	public void testFirst() {
		ProgramState state = new ProgramState(program);
		IExecutionData data = state.execute(maxFunc, "2","-1");
		assertTrue(data.getReturnValue().toString().equals("2"));
//		commonAsserts(data);
	}
	
	@Test
	public void testSecond() {
		ProgramState state = new ProgramState(program);
		IExecutionData data = state.execute(maxFunc, "2","4");
		assertTrue(data.getReturnValue().toString().equals("4"));
//		commonAsserts(data);
	}

	// TODO repor
	private static void commonAsserts(IExecutionData data) {
		assertEquals(0, data.getTotalAssignments());
		assertEquals(1, data.getOperationCount(IOperator.OperationType.RELATIONAL));
		assertEquals(1, data.getCallStackDepth());
	}
}
