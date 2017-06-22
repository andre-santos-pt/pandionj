package pt.iscte.pandionj.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaArrayType;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaReferenceType;
import org.eclipse.jdt.debug.core.IJavaVariable;

import pt.iscte.pandionj.extensibility.PandionJUI;

public class ReferenceModel extends VariableModel<IJavaObject> {
	private NullModel nullModel;
	private boolean isPrimitiveArray;
	private Collection<String> tags;

	ReferenceModel(IJavaVariable variable, boolean isInstance, StackFrameModel stackFrame) {
		super(variable, isInstance, stackFrame);
		init(variable);
	}
	
	ReferenceModel(IJavaVariable var, boolean isInstance, RuntimeModel runtime) {
		super(var, isInstance, runtime);
		init(var);
	}

	private void init(IJavaVariable var) {
		tags = Collections.emptyList();
		PandionJUI.execute(() -> {
			isPrimitiveArray = 
					var.getValue() instanceof IJavaArray &&
					!(((IJavaArrayType) var.getJavaType()).getComponentType() instanceof IJavaReferenceType); 
		});
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
}
