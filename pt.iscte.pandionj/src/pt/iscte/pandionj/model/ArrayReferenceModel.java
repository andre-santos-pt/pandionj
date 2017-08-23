package pt.iscte.pandionj.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaVariable;

import pt.iscte.pandionj.extensibility.IReferenceModel;
import pt.iscte.pandionj.parser.BlockInfo;
import pt.iscte.pandionj.parser.VariableInfo;
import pt.iscte.pandionj.parser.VariableOperation;


public class ArrayReferenceModel extends ArrayModel<IReferenceModel> {
	private List<IReferenceModel> references;
	
	public ArrayReferenceModel(IJavaArray array, RuntimeModel runtime, IReferenceModel sourceReference) {
		super(array, runtime);
		if(sourceReference != null) {
			VariableInfo info = sourceReference.getVariableRole();
			for(IReferenceModel ref : references) {
				VariableInfo copy = info.convertToArrayAccessDim(ref);
				System.out.println(ref.getName() + " ??? " + copy);
				ref.setVariableRole(copy);
			}
		}
	}

	@Override
	protected void initArray(IJavaArray array, int length) {
		try {
			references = new ArrayList<>(length);
			IVariable[] variables = array.getVariables();
			for(int i = 0; i < length - 1; i++)
				handlePosition((IJavaVariable) variables[i]);
			
			if(length > 0)
				handlePosition((IJavaVariable) variables[length-1]);
		}
		catch(DebugException e) {
			e.printStackTrace();
		}
	}

	private void handlePosition(IJavaVariable var) {
//		VariableInfo info = new VariableInfo("", BlockInfo.NONE);
//		info.addOperation(new VariableOperation("", VariableOperation.Type.ACCESS, "j","i")); // TODO hardcoded 
		ReferenceModel referenceModel = new ReferenceModel(var, true, null, getRuntimeModel());
		references.add(referenceModel);
	}

	@Override
	boolean updateInternal(int index, int step) {
		assert index >= 0 && index < references.size();
		IReferenceModel refModel = references.get(index);
		return refModel.update(step);
	}

	public List<IReferenceModel> getModelElements() {
		return Collections.unmodifiableList(references);
	}
	
	@Override
	public void setStep(int stepPointer) {
		for(IReferenceModel ref : references)
			ref.setStep(stepPointer);
	}

	@Override
	public boolean isDecimal() {
		return false;
	}

	@Override
	public IReferenceModel getElementModel(int index) {
		assert index >= 0 && index < references.size();
		return references.get(index);
	}
}
