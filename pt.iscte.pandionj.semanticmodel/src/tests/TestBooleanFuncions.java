package tests;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

import impl.machine.ProgramState;
import impl.program.Factory;
import model.machine.IExecutionData;
import model.machine.IValue;
import model.program.IBinaryExpression;
import model.program.IDataType;
import model.program.IFactory;
import model.program.IOperator;
import model.program.IProcedure;
import model.program.IProgram;
import model.program.IUnaryExpression;
import model.program.IVariableDeclaration;

public class TestBooleanFuncions {
	private static IProgram program;
	private static IProcedure evenFunc;
	private static IProcedure oddFunc;
	private static IProcedure oddNotEvenFunc;
	
	private static IProcedure withinIntervalFunc;
	private static IProcedure outsideIntervalFunc;

	
	private static IFactory factory = new Factory();
	
	@BeforeClass
	public static void setup() {
		program = factory.createProgram();
		evenFunc = createIsEven();
		oddFunc = createIsOdd();
		oddNotEvenFunc = createIsOddNotEven();
		withinIntervalFunc = createWithinInterval();
		outsideIntervalFunc = createOutsideInterval();
		System.out.println(program);
	}

	

	private static IProcedure createIsEven() {
		IProcedure f = program.createProcedure("isEven", IDataType.BOOLEAN);
		IVariableDeclaration nParam = f.addParameter("n", IDataType.INT);
		
		IBinaryExpression e = factory.binaryExpression(IOperator.EQUAL,
				factory.binaryExpression(IOperator.MOD, nParam.expression(), factory.literal(2)),
				factory.literal(0));
		
		f.addReturnStatement(e);
		return f;
	}
	
	@Test
	public void testIsEven() {
		ProgramState state = new ProgramState(program);
		IExecutionData dataTrue = state.execute(evenFunc, "6");
		assertTrue(dataTrue.getReturnValue() == IValue.TRUE);
		
		IExecutionData dataFalse = state.execute(evenFunc, "7");
		assertTrue(dataFalse.getReturnValue() == IValue.FALSE);
	}
	

	private static IProcedure createIsOdd() {
		IProcedure f = program.createProcedure("isOdd", IDataType.BOOLEAN);
		IVariableDeclaration nParam = f.addParameter("n", IDataType.INT);
		
		IBinaryExpression e = factory.binaryExpression(IOperator.DIFFERENT,
				factory.binaryExpression(IOperator.MOD, nParam.expression(), factory.literal(2)),
				factory.literal(0));
		
		f.addReturnStatement(e);
		return f;
	}
	
	private static IProcedure createIsOddNotEven() {
		IProcedure f = program.createProcedure("isOddNotEven", IDataType.BOOLEAN);
		IVariableDeclaration nParam = f.addParameter("n", IDataType.INT);
		
		IUnaryExpression e = factory.unaryExpression(IOperator.NOT, evenFunc.callExpression(nParam.expression()));
		f.addReturnStatement(e);
		return f;
	}
	
	@Test
	public void testIsOdd() {
		ProgramState state = new ProgramState(program);
		IExecutionData dataTrue = state.execute(oddFunc, "7");
		assertTrue(dataTrue.getReturnValue() == IValue.TRUE);
		
		IExecutionData dataFalse = state.execute(oddFunc, "6");
		assertTrue(dataFalse.getReturnValue() == IValue.FALSE);
	}
	
	@Test
	public void testIsOddNotEven() {
		ProgramState state = new ProgramState(program);
		IExecutionData dataTrue = state.execute(oddNotEvenFunc, "7");
		assertTrue(dataTrue.getReturnValue() == IValue.TRUE);
		
		IExecutionData dataFalse = state.execute(oddNotEvenFunc, "6");
		assertTrue(dataFalse.getReturnValue() == IValue.FALSE);
	}
	
	
	private static IProcedure createWithinInterval() {
		IProcedure f = program.createProcedure("withinInterval", IDataType.BOOLEAN);
		IVariableDeclaration nParam = f.addParameter("n", IDataType.INT);
		IVariableDeclaration aParam = f.addParameter("a", IDataType.INT);
		IVariableDeclaration bParam = f.addParameter("b", IDataType.INT);
		
		IBinaryExpression lower = factory.binaryExpression(IOperator.GREATER_EQ, nParam.expression(), aParam.expression());
		IBinaryExpression upper = factory.binaryExpression(IOperator.SMALLER_EQ, nParam.expression(), bParam.expression());
		IBinaryExpression e = factory.binaryExpression(IOperator.AND, lower, upper);
		f.addReturnStatement(e);
		return f;
	}
	
	@Test
	public void testWithinInterval() {
		ProgramState state = new ProgramState(program);
		IExecutionData dataTrue = state.execute(withinIntervalFunc, "6", "4", "8");
		assertTrue(dataTrue.getReturnValue() == IValue.TRUE);
		
		IExecutionData dataFalse = state.execute(withinIntervalFunc, "6", "7", "10");
		assertTrue(dataFalse.getReturnValue() == IValue.FALSE);
	}
	
	private static IProcedure createOutsideInterval() {
		IProcedure f = program.createProcedure("ousideInterval", IDataType.BOOLEAN);
		IVariableDeclaration nParam = f.addParameter("n", IDataType.INT);
		IVariableDeclaration aParam = f.addParameter("a", IDataType.INT);
		IVariableDeclaration bParam = f.addParameter("b", IDataType.INT);
		
		IBinaryExpression lower = factory.binaryExpression(IOperator.SMALLER, nParam.expression(), aParam.expression());
		IBinaryExpression upper = factory.binaryExpression(IOperator.GREATER, nParam.expression(), bParam.expression());
		IBinaryExpression e = factory.binaryExpression(IOperator.OR, lower, upper);
		f.addReturnStatement(e);
		return f;
	}
	
	@Test
	public void testOutsideInterval() {
		ProgramState state = new ProgramState(program);
		IExecutionData dataTrue = state.execute(outsideIntervalFunc, "3", "4", "8");
		assertTrue(dataTrue.getReturnValue() == IValue.TRUE);
		
		IExecutionData dataFalse = state.execute(outsideIntervalFunc, "7", "7", "10");
		assertTrue(dataFalse.getReturnValue() == IValue.FALSE);
	}
	
}
