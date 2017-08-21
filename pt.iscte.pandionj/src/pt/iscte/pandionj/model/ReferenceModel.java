package pt.iscte.pandionj.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaArrayType;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaReferenceType;
import org.eclipse.jdt.debug.core.IJavaVariable;

import pt.iscte.pandionj.extensibility.IArrayIndexModel;
import pt.iscte.pandionj.extensibility.IArrayIndexModel.IBound;
import pt.iscte.pandionj.extensibility.IEntityModel;
import pt.iscte.pandionj.extensibility.IReferenceModel;
import pt.iscte.pandionj.extensibility.IValueModel;
import pt.iscte.pandionj.extensibility.IVariableModel;
import pt.iscte.pandionj.extensibility.PandionJUI;
import pt.iscte.pandionj.parser.BlockInfo;
import pt.iscte.pandionj.parser.VariableInfo;

public class ReferenceModel extends VariableModel<IJavaObject, IEntityModel> implements IReferenceModel {
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

	ReferenceModel(IJavaVariable variable, boolean isInstance, VariableInfo info, RuntimeModel runtime) {
		super(variable, isInstance, runtime);
		this.info = info;
		init(variable);
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
		if(target == null || target.isNull())
			return getNullInstance();
		else {
			EntityModel<? extends IJavaObject> object = getRuntimeModel().getObject(target, false);
			return object == null ? getNullInstance() : object;
		}
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

	public boolean hasIndexVars() {
		return info != null && !info.getArrayAccessVariables().isEmpty();
	}
	
	public Collection<IArrayIndexModel> getIndexVars() {
		StackFrameModel stackFrame = getRuntimeModel().getTopFrame();
		
		List<IArrayIndexModel> list = new ArrayList<>(3);
		for (String indexVar : info.getArrayAccessVariables()) {
			IVariableModel vi = stackFrame.getStackVariable(indexVar);
			if(vi instanceof IValueModel && !vi.getVariableRole().isFixedValue()) {
				ArrayIndexVariableModel indexModel = new ArrayIndexVariableModel((IValueModel) vi, this);
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

	public Collection<IArrayIndexModel> getFixedIndexes() {
		StackFrameModel stackFrame = getRuntimeModel().getTopFrame();
		List<IArrayIndexModel> list = new ArrayList<>(3);
		
		for (String indexVar : info.getArrayAccessVariables()) {
			IVariableModel vi = stackFrame.getStackVariable(indexVar);
			if(vi instanceof IValueModel && vi.getVariableRole().isFixedValue()) {
				ArrayIndexVariableModel indexModel = new ArrayIndexVariableModel((IValueModel) vi, this);
				list.add(indexModel);
			}
		}
		BlockInfo root = info.getDeclarationBlock().getRoot();
		root.accept(new BlockInfo.BlockInfoVisitor() {
			@Override
			public void visit(VariableInfo var) {
				if(var.getArrayFixedVariables().contains(getName()) && info.getDeclarationBlock().getVariable(var.getName()) == var) {
					IVariableModel vi = stackFrame.getStackVariable(var.getName());
					if(vi instanceof IValueModel)
						list.add(new ArrayIndexVariableModel((IValueModel) vi, ReferenceModel.this));
				}
			}
		});
		return list;
	}

	@Override
	public Role getRole() {
		return null;
	}

	
	
	
	
}
