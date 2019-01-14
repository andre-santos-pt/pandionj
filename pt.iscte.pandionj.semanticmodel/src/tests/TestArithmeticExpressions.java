package tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import model.machine.IArray;
import model.machine.ICallStack;
import model.machine.IStackFrame;
import model.machine.IValue;
import model.machine.impl.ProgramState;
import model.program.ExecutionError;
import model.program.IBinaryExpression;
import model.program.IDataType;
import model.program.IExpression;
import model.program.IFactory;
import model.program.ILiteral;
import model.program.IOperator;
import model.program.IProcedure;
import model.program.IStatement;
import model.program.impl.Factory;

public class TestArithmeticExpressions {
	
	IFactory factory = new Factory();
	
	final int EXP = 0;
	final int TYPE = 1;
	final int RES = 2;

	ILiteral L1 = factory.literal(1);
	ILiteral L3 = factory.literal(3);
	ILiteral L6 = factory.literal(6);
	
	ILiteral L3_3 = factory.literal(3.3);
	ILiteral L6_4 = factory.literal(6.4);
	
	IBinaryExpression L3_ADD_L6 = factory.binaryExpression(IOperator.ADD, L3, L6);

//	Object[][] ADD_CASES = {
//			{L3_ADD_L6, IDataType.INT, 9},
//			{factory.binaryExpression(IOperator.ADD, L3_ADD_L6, L1), IDataType.INT, 10}, 
//			{factory.binaryExpression(IOperator.ADD, L1, L3_ADD_L6), IDataType.INT, 10}, 
//			{factory.binaryExpression(IOperator.ADD, L3_3, L6), IDataType.DOUBLE, 9.3},
//			{factory.binaryExpression(IOperator.ADD, L6, L3_3), IDataType.DOUBLE, 9.3},
//			{factory.binaryExpression(IOperator.ADD, L3_3, L6_4), IDataType.DOUBLE, 9.7}
//	};

	@Test
	public void testAddCases() throws ExecutionError {
		test(L3_ADD_L6, IDataType.INT, 9);
		test(factory.binaryExpression(IOperator.ADD, L3_ADD_L6, L1), IDataType.INT, 10);
		test(factory.binaryExpression(IOperator.ADD, L1, L3_ADD_L6), IDataType.INT, 10);
		test(factory.binaryExpression(IOperator.ADD, L3_3, L6), IDataType.DOUBLE, 9.3);
		test(factory.binaryExpression(IOperator.ADD, L6, L3_3), IDataType.DOUBLE, 9.3);
		test(factory.binaryExpression(IOperator.ADD, L3_3, L6_4), IDataType.DOUBLE, 9.7);
	}



//	private void testCase(Object[] c) throws ExecutionError {
//		IExpression exp = (IExpression) c[EXP];
//		IValue value = exp.evaluate(mockFrame);
//		String text = exp + " = " + value;
//		assertEquals(c[TYPE], value.getType(), text);
//		assertEquals(c[RES], value.getValue(), text);
//		System.out.println(text);
//	}

	
	
//	Object[][] PROD_CASES = {
//			{factory.binaryExpression(IOperator.PROD, L1, L3), IDataType.INT, 3},
//			{factory.binaryExpression(IOperator.PROD, L3, L6), IDataType.INT, 18},
//			{factory.binaryExpression(IOperator.PROD, L3, L3_3), IDataType.DOUBLE, 9.9}
//	};
	
	@Test
	public void testProdCases() throws ExecutionError {
		test(factory.binaryExpression(IOperator.MUL, L1, L3), IDataType.INT, 3);
		test(factory.binaryExpression(IOperator.MUL, L3, L6), IDataType.INT, 18);
		test(factory.binaryExpression(IOperator.MUL, L3, L3_3), IDataType.DOUBLE, 9.9);
	}
	
	private void test(IExpression expression, IDataType type, Number result) throws ExecutionError {
		IValue value = expression.evaluate(mockFrame);
		String text = expression + " = " + value;
		assertEquals(type, value.getType(), text);
		assertEquals(new BigDecimal(result.toString()), value.getValue(), text);
		System.out.println(text);
	}
	
	IStackFrame mockFrame = new IStackFrame() {
		ProgramState state = new ProgramState(factory.createProgram());
		public void terminateFrame() { }
		public void setVariable(String identifier, IValue value) { }
		public void setReturn(IValue value) { }
		public IStackFrame newChildFrame(IProcedure procedure, List<IValue> args) { return null; }
		public Map<String, IValue> getVariables() {
			return null;
		}

		@Override
		public IValue getVariable(String id) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public IValue getValue(Object object) {
			// TODO Auto-generated method stub
			return null;
		}


		@Override
		public IValue getReturn() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public IProcedure getProcedure() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public IStackFrame getParent() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int getMemory() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public ICallStack getCallStack() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public IArray getArray(IDataType baseType, int length) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void execute(IStatement statement) throws ExecutionError {
			// TODO Auto-generated method stub

		}


		@Override
		public void addVariable(String identifier, IDataType type) {
			// TODO Auto-generated method stub

		}

		@Override
		public void addListener(IListener listener) {
			// TODO Auto-generated method stub

		}

		@Override
		public IValue evaluate(IExpression expression) throws ExecutionError {
			return expression.evaluate(this);
		}


		@Override
		public IValue getValue(String literal) {
			return state.getValue(literal);
		}


	};
}