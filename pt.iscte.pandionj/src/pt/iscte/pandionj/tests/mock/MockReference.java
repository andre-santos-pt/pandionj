package pt.iscte.pandionj.tests.mock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.jdt.debug.core.IJavaVariable;

import pt.iscte.pandionj.extensibility.IArrayIndexModel;
import pt.iscte.pandionj.extensibility.IEntityModel;
import pt.iscte.pandionj.extensibility.IReferenceModel;
import pt.iscte.pandionj.extensibility.IRuntimeModel;
import pt.iscte.pandionj.parser.VariableInfo;

public class MockReference implements IReferenceModel {
	final String type;
	final String name;
	final boolean isStatic;
	Collection<String> tags;
	IEntityModel target;
	
	public MockReference(String type, String name, IEntityModel target, boolean isStatic, String ... tags) {
		this.type = type;
		this.name = name;
		this.target = target;
		this.isStatic = isStatic;
		this.tags = new ArrayList<>();
		for(String t : tags)
			this.tags.add(t);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getTypeName() {
		return type;
	}

	@Override
	public boolean isInstance() {
		return false;
	}

//	@Override
//	public boolean isWithinScope() {
//		return true;
//	}

	@Override
	public Role getRole() {
		return null;
	}

	@Override
	public VariableInfo getVariableRole() {
		return null;
	}

	@Override
	public Collection<String> getTags() {
		return Collections.unmodifiableCollection(tags);
	}

	@Override
	public IEntityModel getModelTarget() {
		return target;
	}

	@Override
	public boolean hasIndexVars() {
		return false;
	}

	@Override
	public Collection<IArrayIndexModel> getIndexVars() {
		return Collections.emptyList();
	}

	@Override
	public Collection<IArrayIndexModel> getFixedIndexes() {
		return Collections.emptyList();
	}

	@Override
	public boolean isStatic() {
		return isStatic;
	}

	@Override
	public void setOutOfScope() {
		
	}

	@Override
	public boolean update(int step) {
		return false;
	}

	@Override
	public IJavaVariable getJavaVariable() {
		return null;
	}

	@Override
	public void setStep(int stepPointer) {
		
	}

	@Override
	public void setVariableRole(VariableInfo info) {
		
	}
	
	@Override
	public IRuntimeModel getRuntimeModel() {
		return null;
	}

	@Override
	public boolean isVisible() {
		return true;
	}

	@Override
	public int getIndex() {
		return -1;
	}

}
