package pt.iscte.pandionj.model;

import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaVariable;

import pt.iscte.pandionj.extensibility.IReferenceModel;
import pt.iscte.pandionj.parser.VariableInfo;


public class ArrayReferenceModel extends ArrayModel<IReferenceModel> {

	public ArrayReferenceModel(IJavaArray array, RuntimeModel runtime, IReferenceModel sourceReference) {
		super(array, runtime);
		if(sourceReference != null) {
			VariableInfo info = sourceReference.getVariableRole();
			if(info != null) {
				for(IReferenceModel ref : getModelElements()) {
					VariableInfo copy = info.convertToArrayAccessDim(ref);
					ref.setVariableRole(copy);
				}
			}
		}
	}

	IReferenceModel createElement(IJavaVariable var) {
		return new ReferenceModel(var, true, null, getRuntimeModel());
	}

	@Override
	public boolean isDecimal() {
		return false;
	}

}
