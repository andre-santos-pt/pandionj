package model.program.semantics;

import java.util.ArrayList;
import java.util.List;

import model.program.IBlock;
import model.program.IExpression;
import model.program.ILiteral;
import model.program.IProcedure;
import model.program.IModule;
import model.program.IModule.IVisitor;

public class Checker {

	ISemanticChecker checker;
	List<ISemanticProblem> problems;

	public Checker(IModule program, ISemanticChecker checker) {
		// TODO Auto-generated constructor stub
	}

	void run(IModule program) {
		program.accept(new Visitor());
	}

	class Visitor implements IModule.IVisitor {
		@Override
		public boolean visit(IBlock block) {
			for (ISemanticRule<IBlock> rule : checker.getRules(IBlock.class)) {
				ISemanticProblem check = rule.check(block);
				if(check != null)
					problems.add(check);
			}
			return true;
		}
		@Override
		public boolean visit(IProcedure constant) {
			// TODO Auto-generated method stub
			return IVisitor.super.visit(constant);
		}
	}


	void check(IModule program, List<IRule> rules) {
		VisitorAll v = new VisitorAll(rules);
		program.accept(v);
		v.getProblems();
	}

	class VisitorAll implements IModule.IVisitor {
		List<IRule> rules;

		public VisitorAll(List<IRule> rules) {
			this.rules = rules;
		}

		List<ISemanticProblem> getProblems() {
			List<ISemanticProblem> list = new ArrayList<>();
			rules.forEach(r -> list.addAll(r.getProblems()));
			return list;
		}

		@Override
		public boolean visit(IBlock block) {
			rules.forEach(r -> r.visit(block));
			return true;
		}

		@Override
		public void visit(ILiteral exp) {
			rules.forEach(r -> r.visit(exp));
			IVisitor.super.visit(exp);
		}
	}


	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
