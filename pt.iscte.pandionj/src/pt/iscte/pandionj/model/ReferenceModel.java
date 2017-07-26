package pt.iscte.pandionj.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaArrayType;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaReferenceType;
import org.eclipse.jdt.debug.core.IJavaVariable;

import pt.iscte.pandionj.extensibility.IArrayIndexModel;
import pt.iscte.pandionj.extensibility.IVariableModel;
import pt.iscte.pandionj.extensibility.PandionJUI;
import pt.iscte.pandionj.extensibility.IArrayIndexModel.IBound;
import pt.iscte.pandionj.parser.VariableInfo;

public class ReferenceModel extends VariableModel<IJavaObject> {
	private NullModel nullModel;
	private boolean isPrimitiveArray;
	private Collection<String> tags;

	private Map<String, ArrayIndexVariableModel> varsRoles;

	private VariableInfo info;
	
	ReferenceModel(IJavaVariable variable, boolean isInstance, VariableInfo info, StackFrameModel stackFrame) {
		super(variable, isInstance, stackFrame);
		this.info = info;
		init(variable);
	}

	ReferenceModel(IJavaVariable var, boolean isInstance, VariableInfo info, RuntimeModel runtime) {
		super(var, isInstance, runtime);
		this.info = info;
		init(var);
	}

	private void init(IJavaVariable var) {
		tags = Collections.emptyList();
		PandionJUI.execute(() -> {
			isPrimitiveArray = 
					var.getValue() instanceof IJavaArray &&
					!(((IJavaArrayType) var.getJavaType()).getComponentType() instanceof IJavaReferenceType); 
		});

		varsRoles = new LinkedHashMap<>();
	}

	public EntityModel<?> getModelTarget() {
		IJavaObject target = getContent();
		return target == null || target.isNull() ? getNullInstance() : getRuntimeModel().getObject(target, false);
	}

	public boolean isNull() {
		return getContent().isNull();
	}

	private NullModel getNullInstance() {
		if(nullModel == null)
			nullModel = new NullModel(getRuntimeModel());
		return nullModel;
	}


	public boolean isPrimitiveArray() {
		return isPrimitiveArray;
	}

	@Override
	public String toString() {
		return getName() + " -> " + getModelTarget();
	}

	public void setTags(Collection<String> tags) {
		if(!tags.isEmpty()) {
			this.tags = new ArrayList<String>(tags.size());
			this.tags.addAll(tags);
		}
	}

	public Collection<String> getTags() {
		return Collections.unmodifiableCollection(tags);
	}

	public boolean hasTags() {
		return !tags.isEmpty();
	}

	@Override
	public VariableInfo getVariableRole() {
		return info;
	}

//	public void addVar(ArrayIndexVariableModel v) {
//		ArrayIndexVariableModel i = varsRoles.get(v.getName());
//		if(i == null || !i.sameAs(v)) {
//			System.out.println("add var " + v);
//			varsRoles.put(v.getName(), v);
//			v.registerObserver((o,a) -> {
//				if(!v.isWithinScope()) {
//					varsRoles.remove(v.getName()); 
//					setChanged();
//					notifyObservers(v);
//				}
//			});
//			setChanged();
//			notifyObservers(v);
//		}
//	}
	
	public boolean hasIndexVars() {
		return !info.getArrayAccessVariables().isEmpty();
	}
	
	public Collection<IArrayIndexModel> getIndexVars() {
		StackFrameModel stackFrame = getStackFrame();
		assert stackFrame != null;
		
		List<IArrayIndexModel> list = new ArrayList<>(3);
		for (String indexVar : info.getArrayAccessVariables()) {
			VariableModel<?> vi = stackFrame.getVariable(indexVar);
			if(vi != null) {
				ArrayIndexVariableModel indexModel = new ArrayIndexVariableModel(vi, this);
				IBound bound = vi.getVariableRole().getBound();
				if(bound != null && bound.getType() != null) {
					ArrayIndexBound iBound = new ArrayIndexBound(bound.getExpression(), bound.getType(), getRuntimeModel() );
					indexModel.setBound(iBound);
				}
				list.add(indexModel);
			}
		}
		return list;
	}
	
//	private ArrayIndexBound handleBound(IArrayIndexModel.IBound bound, StackFrameModel stackFrame) {
//		ASTParser parser = ASTParser.newParser(AST.JLS8);
//		parser.setKind(ASTParser.K_EXPRESSION);
//		parser.setSource(bound.getExpression().toCharArray());
//
//		ASTNode node = (ASTNode) parser.createAST(null);
//		List<IVariableModel> deps = new ArrayList<>();
//		node.accept(new ASTVisitor() {
//			@Override
//			public boolean visit(SimpleName node) {
//				VariableModel<?> var = stackFrame.getVariable(node.toString());
//				if(var != null)
//					deps.add(var);
//				return true;
//			}
//		});
//
//		ArrayIndexBound iBound = new ArrayIndexBound(bound.getExpression(), bound.getType());
//		return iBound;
//	}
}
