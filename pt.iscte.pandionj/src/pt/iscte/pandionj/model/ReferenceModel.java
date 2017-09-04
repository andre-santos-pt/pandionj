package pt.iscte.pandionj.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

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
	private VariableInfo info;
	private Collection<String> tags;
	
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
	}

	public IEntityModel getModelTarget() {
		IJavaObject target = getContent();
		if(target == null || target.isNull())
			return getNullInstance();
		else {
			IEntityModel object = getRuntimeModel().getObject(target, false, this);
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
		if(this.tags == Collections.EMPTY_LIST)
			this.tags = new ArrayList<String>(tags.size());

		this.tags.addAll(tags);
	}

	public Collection<String> getTags() {
		return Collections.unmodifiableCollection(tags);
	}

	public boolean hasTags() {
		return !tags.isEmpty();
	}

	public void setVariableRole(VariableInfo info) {
		this.info = info;
	}
	
	@Override
	public VariableInfo getVariableRole() {
		return info;
	}

	public boolean hasIndexVars() {
		// info != null && !info.getArrayAccessVariables((i,v)->false).isEmpty();
		return !getIndexVars().isEmpty();
	}
	
	public Collection<IArrayIndexModel> getIndexVars() {
		if(info == null)
			return Collections.emptyList();
		
		StackFrameModel stackFrame = getRuntimeModel().getTopFrame();
		
		List<IArrayIndexModel> list = new ArrayList<>(3);
		Set<String> vars = info.getArrayAccessVariables((i,v)-> {
			IVariableModel<?> vi = stackFrame.getStackVariable(v);
			return vi instanceof IValueModel && ((IValueModel) vi).getCurrentValue().equals(i);
		});
		
		for (String indexVar : vars) {
			IVariableModel<?> vi = stackFrame.getStackVariable(indexVar);
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
		
		for (String indexVar : info.getArrayAccessVariables((i,v)->false)) {
			IVariableModel<?> vi = stackFrame.getStackVariable(indexVar);
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
					IVariableModel<?> vi = stackFrame.getStackVariable(var.getName());
					if(vi instanceof IValueModel)
						list.add(new ArrayIndexVariableModel((IValueModel) vi, ReferenceModel.this));
				}
			}
		});
		return list;
	}

	@Override
	public Role getRole() {
		return Role.NONE;
	}
}
