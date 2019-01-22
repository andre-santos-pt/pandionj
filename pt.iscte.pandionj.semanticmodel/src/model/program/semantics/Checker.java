package model.program.semantics;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import model.program.IBlock;
import model.program.IConstantDeclaration;
import model.program.IDataType;
import model.program.IFactory;
import model.program.ILiteral;
import model.program.IModule;
import model.program.semantics.java.JavaSemanticChecker;

public class Checker {
	static {
		// TODO
//		assert hasAllVisitMethods();
	}
	
	private ISemanticChecker checker;
	private List<Rule> rules;

	public Checker(ISemanticChecker checker) {
		this.checker = checker;
	}

	public List<ISemanticProblem> check(IModule program) {
		rules = new ArrayList<Rule>();
		for (Class<? extends Rule> r : checker.getRules()) {
			try {
				rules.add(r.newInstance());
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		VisitorAll v = new VisitorAll();
		program.accept(v);
		return v.getProblems();
	}

	private class VisitorAll implements IModule.IVisitor {
		List<ISemanticProblem> getProblems() {
			List<ISemanticProblem> list = new ArrayList<>();
			rules.forEach(r -> list.addAll(r.getProblems()));
			return list;
		}

		@Override
		public boolean visit(IConstantDeclaration constant) {
			rules.forEach(r -> r.visit(constant));
			return true;
		}
		
		
		@Override
		public boolean visit(IBlock block) {
			rules.forEach(r -> r.visit(block));
			return true;
		}

		@Override
		public void visit(ILiteral exp) {
			rules.forEach(r -> r.visit(exp));
		}
	}
	
	private static boolean hasAllVisitMethods() {
		boolean all = true;
		for (Method method : IModule.IVisitor.class.getMethods()) {
			try {
				VisitorAll.class.getDeclaredMethod(method.getName(), method.getParameterTypes()[0]);
			} catch (NoSuchMethodException e) {
				System.err.println("missing: " + method);
				all = false;
			} catch (SecurityException e) {
				e.printStackTrace();
			}
		}
		return all;
	}


	
	public static void main(String[] args) {
		IModule mod = IFactory.INSTANCE.createModule("test");
		mod.addConstant("a", IDataType.INT, IFactory.INSTANCE.literal(3));
		mod.addConstant("a", IDataType.INT, IFactory.INSTANCE.literal(3));
		
		Checker checker = new Checker(new JavaSemanticChecker());
		for (ISemanticProblem iSemanticProblem : checker.check(mod)) {
			System.out.println(iSemanticProblem);
		}
	}

}
