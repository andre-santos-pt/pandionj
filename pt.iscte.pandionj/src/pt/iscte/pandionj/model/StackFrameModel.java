package pt.iscte.pandionj.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFile;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;

import pt.iscte.pandionj.ParserManager;
import pt.iscte.pandionj.extensibility.IArrayIndexModel;
import pt.iscte.pandionj.extensibility.IArrayIndexModel.IBound;
import pt.iscte.pandionj.extensibility.IVariableModel;
import pt.iscte.pandionj.parser.VarParser;
import pt.iscte.pandionj.parser.VariableInfo;



public class StackFrameModel extends DisplayUpdateObservable {
	private RuntimeModel runtime;
	private IJavaStackFrame frame;
	private Map<String, VariableModel<?>> vars;

	private IFile srcFile;
	private VarParser varParser;
	private IJavaProject javaProject;

	private String invExpression;

	private boolean obsolete;
	private String returnValue;
	private String exceptionType;

	private int step;
	private int stepPointer;
	private Map<Integer, Integer> stepLines;

	private int lastLine;

	public StackFrameModel(RuntimeModel runtime, IJavaStackFrame frame) {
		assert runtime != null && frame != null;
		this.runtime = runtime;
		this.frame = frame;

		vars = new LinkedHashMap<>();
		Object sourceElement = frame.getLaunch().getSourceLocator().getSourceElement(frame);
		if(sourceElement instanceof IFile) {
			srcFile = (IFile) frame.getLaunch().getSourceLocator().getSourceElement(frame);
			varParser = ParserManager.getVarParserResult(srcFile);
			javaProject = JavaCore.create(srcFile.getProject());
		}

		obsolete = false;
		exceptionType = null;

		step = 0;
		stepLines = new HashMap<>();
		try {
			lastLine = frame.getLineNumber();
			stepLines.put(step, lastLine);
		} catch (DebugException e) {
			e.printStackTrace();
		}
	}

	public RuntimeModel getRuntime() {
		return runtime;
	}

	public boolean isExecutionFrame() {
		return !runtime.isEmpty() && runtime.getTopFrame() == this;
	}

	public IJavaStackFrame getStackFrame() {
		return frame;
	}

	public IFile getSourceFile() {
		return srcFile;
	}

	public int getLineNumber() {
		try {
			return frame.getLineNumber();
		} catch (DebugException e) {
			e.printStackTrace();
			return -1;
		}
	}

	//	public Variable getLocalVariable(String name) {
	//		try {
	//			int line = frame.getLineNumber();
	//			for(Variable v : codeAnalysis.variableRoles.get(name))
	//				if(v.scopeIncludesLine(line))
	//					return v;
	//		}
	//		catch(DebugException e) {
	//			e.printStackTrace();
	//		}
	//		return null;
	//	}

	public void update() {
		List<VariableModel<?>> newVars = handleVariables(); // FIXME bug new variables int[][]
		if(hasChanged()) {
			step++;
			stepPointer = step;
			stepLines.put(step, lastLine);

			try {
				lastLine = frame.getLineNumber();
			} catch (DebugException e) {
				e.printStackTrace();
			}
		}
		notifyObservers(newVars);
	}



	public void setObsolete() {
		obsolete = true;
		setChanged();
		notifyObservers(Collections.emptyList());
	}

	private List<VariableModel<?>> handleVariables() {
		List<VariableModel<?>> newVars = new ArrayList<>();
		try {
			Iterator<Entry<String, VariableModel<?>>> iterator = vars.entrySet().iterator();
			while(iterator.hasNext()) {
				Entry<String, VariableModel<?>> e = iterator.next();
				String varName = e.getKey();
				boolean contains = false;
				for(IVariable v : frame.getVariables()) {
					if(v.getName().equals(varName))
						contains = true;
					else if(v.getName().equals("this")) {
						for (IVariable iv : v.getValue().getVariables())
							if(iv.getName().equals(varName))
								contains = true;
					}
				}
				if(!contains) {
					e.getValue().setOutOfScope();
					iterator.remove();
					setChanged();
				}
			}

			for(IVariable v : frame.getVariables()) {
				IJavaVariable jv = (IJavaVariable) v;
				String varName = v.getName();

				if(varName.equals("this")) {
					for (IVariable iv : jv.getValue().getVariables())
						if(!((IJavaVariable) iv).isSynthetic() && !((IJavaVariable) iv).isStatic()) {
							VariableModel<?> var = handleVar((IJavaVariable) iv, true);
							if(var != null)
								newVars.add(var);
						}
				}

				else if(!jv.isSynthetic()) {
					VariableModel<?> var = handleVar(jv, false);
					if(var != null)
						newVars.add(var);
				}
			}

			//			handleArrayIterators2();
		}
		catch(DebugException e) {
			e.printStackTrace();
		}

		if(!newVars.isEmpty())
			setChanged();

		return newVars;
	}


	private VariableModel<?> handleVar(IJavaVariable jv, boolean isInstance) throws DebugException {
		if(jv.getClass().getSimpleName().equals("JDIReturnValueVariable")) {
			runtime.setReturnOnFrame(this, (IJavaValue) jv.getValue());
			return null;
		}

		String varName = jv.getName();
		IJavaValue value = (IJavaValue) jv.getValue();

		if(vars.containsKey(varName) && vars.get(varName).variable == jv) { // FIXME bug same name!
			VariableModel<?> vModel = vars.get(varName);
			boolean change = vModel.update(step);
			if(change)
				setChanged();
			if(change && vModel instanceof ReferenceModel)
				return vModel;
			else
				return null;
		}
		else {
			VariableInfo info = varParser.locateVariable(varName, frame.getLineNumber());
			System.err.println(varName + ": " + info);

			VariableModel<?> newElement = null;

			if(value instanceof IJavaObject) {
				ReferenceModel refElement = new ReferenceModel(jv, isInstance, info, this);
				Collection<String> tags = ParserManager.getTags(srcFile, jv.getName(), frame.getLineNumber());
				refElement.setTags(tags);

				newElement = refElement;
			}
			else {
				newElement = new ValueModel(jv, isInstance, info, this);
			}

			vars.put(varName, newElement);
			return newElement;
		}
	}

	//	private void handleArrayIterators() throws DebugException {
	//		for (Entry<String, VariableModel<?>> e : vars.entrySet()) {
	//			if(e.getValue() instanceof ReferenceModel) {
	//				ReferenceModel refModel = (ReferenceModel) e.getValue();
	//				if(refModel.isPrimitiveArray() && !refModel.isNull()) {
	//					ArrayPrimitiveModel array = (ArrayPrimitiveModel) refModel.getModelTarget();
	//
	//					for(String v : vars.keySet()) {
	//						VariableInfo info = varParser.locateVariable(v, getLineNumber());
	//						if(info != null) {
	//							for (String arrayVar : info.getAccessedArrays()) {
	//								if(arrayVar.equals(refModel.getName())) {
	//									if(!array.hasVar(v)) {
	//										ArrayIndexVariableModel indexModel = new ArrayIndexVariableModel(vars.get(v), refModel);
	//										array.addVar(indexModel);
	//
	//										IBound bound = info.getBound();
	//										if(bound != null) {
	//											ArrayIndexBound iBound = handleBound(bound);
	//											indexModel.setBound(iBound);
	//										}
	//									}
	//
	//								}
	//							}
	//						}
	//						else
	//							System.err.println("info not found " + v + " " + getLineNumber());
	//					}
	//				}
	//			}
	//		}
	//	}


	//	private void handleArrayIterators2() throws DebugException {
	//		for (Entry<String, VariableModel<?>> e : vars.entrySet()) {
	//			if(e.getValue() instanceof ReferenceModel) {
	//				ReferenceModel refModel = (ReferenceModel) e.getValue();
	//				if(refModel.isPrimitiveArray() && !refModel.isNull()) {
	//					ArrayPrimitiveModel array = (ArrayPrimitiveModel) refModel.getModelTarget();
	//
	//					for(String v : vars.keySet()) {
	//						VariableInfo info = varParser.locateVariable(v, getLineNumber());
	//						if(info != null) {
	//							for (String indexVar : info.getArrayAccessVariables()) {
	//								VariableModel<?> vi = vars.get(indexVar);
	//								if(vi != null) {
	//									ArrayIndexVariableModel indexModel = new ArrayIndexVariableModel(vi, refModel);
	//									refModel.addVar(indexModel);
	//								
	//									IBound bound = info.getBound();
	//									if(bound != null && bound.getType() != null) {
	//										ArrayIndexBound iBound = handleBound(bound);
	//										indexModel.setBound(iBound);
	//									}
	//								}
	//							}
	//							
	//							for (String arrayVar : info.getAccessedArrays()) {
	//								if(arrayVar.equals(refModel.getName())) {
	//									ArrayIndexVariableModel indexModel = new ArrayIndexVariableModel(vars.get(v), refModel);
	//									refModel.addVar(indexModel);
	//
	//									IBound bound = info.getBound();
	//									if(bound != null && bound.getType() != null) {
	//										ArrayIndexBound iBound = handleBound(bound);
	//										indexModel.setBound(iBound);
	//									}
	//								}
	//							}
	//						}
	//						else
	//							System.err.println("info not found " + v + " " + getLineNumber());
	//					}
	//				}
	//			}
	//		}
	//	}

	//	private ArrayIndexBound handleBound(IArrayIndexModel.IBound bound) {
	//		ASTParser parser = ASTParser.newParser(AST.JLS8);
	//		parser.setKind(ASTParser.K_EXPRESSION);
	//		parser.setSource(bound.getExpression().toCharArray());
	//
	//		ASTNode node = (ASTNode) parser.createAST(null);
	//
	//		List<IVariableModel> deps = new ArrayList<>();
	//		node.accept(new ASTVisitor() {
	//			@Override
	//			public boolean visit(SimpleName node) {
	//				VariableModel<?> var = vars.get(node.toString());
	//				if(var != null)
	//					deps.add(var);
	//				return true;
	//			}
	//		});
	//
	//		ArrayIndexBound iBound = new ArrayIndexBound(bound.getExpression(), bound.getType());
	//		
	//		return iBound;
	//	}



	public Collection<VariableModel<?>> getInstanceVariables() {
		return vars.values().stream().filter((v) -> !v.isStatic()).collect(Collectors.toList());
	}


	public List<VariableModel<?>> getStaticVariables() {
		return vars.values().stream().filter((v) -> v.isStatic()).collect(Collectors.toList());
	}


	public Collection<ReferenceModel> getReferencesTo(EntityModel<?> object) {
		List<ReferenceModel> refs = new ArrayList<>(3);
		for (ModelElement<?> e : vars.values()) {
			if(e instanceof ReferenceModel && ((ReferenceModel) e).getModelTarget().equals(object))
				refs.add((ReferenceModel) e);
		}
		return refs;
	}


	private String calcString() {
		try {
			IJavaVariable[] localVariables = frame.getLocalVariables();
			int nArgs = frame.getArgumentTypeNames().size();
			List<String> args = new ArrayList<>(localVariables.length);
			for(int i = 0; i < localVariables.length && i < nArgs ; i++) {
				if(PrimitiveType.isPrimitive(localVariables[i].getReferenceTypeName())) {
					IJavaValue value = (IJavaValue) localVariables[i].getValue();
					args.add(valueToString(value));
				}
				else
					args.add(localVariables[i].getName());
			}

			if(frame.isStaticInitializer())
				return frame.getDeclaringTypeName() + " (static initializer)";
			else if(frame.isConstructor())
				return "new " + frame.getReferenceType().getName() + "(" + String.join(", ", args) + ")";
			else {
				String ret = frame.getMethodName() + "(" + String.join(", ", args) + ")";
				if(returnValue != null)
					ret += " = " + returnValue;
				return ret;
			}
		} catch (DebugException e) {
			e.printStackTrace();
			return super.toString();
		}
	}

	private String valueToString(IJavaValue value) {
		if(value instanceof IJavaArray) {
			try {
				IJavaArray array = (IJavaArray) value;
				IJavaValue[] values = array.getValues();
				String s = "";
				for(int i = 0; i < values.length; i++) {
					if(!s.isEmpty())
						s += ", ";
					s += valueSpecialChars(values[i]);
				}
				return "{" + s + "}";
			}
			catch (DebugException e) {
				e.printStackTrace();
				return null;
			}
		}
		else
			return valueSpecialChars(value);
	}

	private String valueSpecialChars(IJavaValue value) {
		try {
			if(value.getReferenceTypeName().equals("char"))
				return "'" + value.getValueString() + "'";

			if(value.getReferenceTypeName().equals(String.class.getName()))
				return "\"" + value.getValueString() + "\"";

			if(value instanceof IJavaObject) { // FIXME bug
				if(((IJavaObject) value).isNull())
					return "null";
				else
					return runtime.getObject((IJavaObject) value, false).toString();
			}
			return value.getValueString();
		}
		catch (DebugException e) {
			e.printStackTrace();
			return null;
		}
	}


	public void setReturnValue(IJavaValue v) {

		this.returnValue = valueToString(v);
		obsolete = true;
		setChanged();
		notifyObservers(Collections.emptyList());
	}

	public String getInvocationExpression() {
		if(isObsolete() && returnValue != null && !returnValue.equals("(void)"))
			return invExpression + " = " + returnValue;
		else if(invExpression == null && !isObsolete()) {
			invExpression = calcString();
			return invExpression;
		}
		else if(invExpression != null)
			return invExpression;
		else
			return toString();
	}

	public void processException(String exceptionType, int line) {
		this.exceptionType = exceptionType;

		//		Collection<JavaException> collection = codeAnalysis.lineExceptions.get(line);
		//		for(JavaException e : collection)
		//			if(e instanceof ArrayOutOfBounds) {
		//				ArrayOutOfBounds ae = (ArrayOutOfBounds) e;
		//				ArrayPrimitiveModel arrayModel = (ArrayPrimitiveModel) ((ReferenceModel) vars.get(ae.arrayName)).getModelTarget();
		//				arrayModel.setVarError(ae.arrayAccess);
		//			}

		setChanged();
		notifyObservers(Collections.emptyList());
	}

	public IJavaProject getJavaProject() {
		return javaProject;
	}


	public boolean isObsolete() {
		return obsolete;
	}

	public boolean exceptionOccurred() {
		return exceptionType != null;
	}

	public String getExceptionMessage() {
		return exceptionType;
	}

	public int getRunningStep() {
		return step;
	}

	public int getStepPointer() {
		return stepPointer;
	}

	public void setStep(int step) {
		if(step != stepPointer) {
			stepPointer = step;
			for(VariableModel<?> var : vars.values())
				var.setStep(stepPointer);
		}
		setChanged();
		notifyObservers(Collections.emptyList());
	}

	public int getStepLine() {
		return stepLines.get(stepPointer);
	}

	public VariableModel<?> getVariable(String varName) {
		return vars.get(varName);
	}
}