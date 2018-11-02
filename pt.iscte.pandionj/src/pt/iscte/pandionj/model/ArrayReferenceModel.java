package pt.iscte.pandionj.model;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaVariable;

import pt.iscte.pandionj.extensibility.IEntityModel;
import pt.iscte.pandionj.extensibility.IObjectModel;
import pt.iscte.pandionj.extensibility.IReferenceModel;
import pt.iscte.pandionj.extensibility.IVariableModel;
import pt.iscte.pandionj.parser.VariableInfo;


public class ArrayReferenceModel extends ArrayModel<IReferenceModel> {

	public ArrayReferenceModel(IJavaArray array, RuntimeModel runtime, IReferenceModel sourceReference) throws DebugException {
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

	IReferenceModel createElement(IJavaVariable var, int index) throws DebugException {
		ReferenceModel r = new ReferenceModel(var, true, false, null, getRuntimeModel());
		r.setIndex(index);
		return r;
	}

	
	@Override
	public boolean isDecimal() {
		return false;
	}

	@Override
	public String getElementString(IReferenceModel v) throws DebugException {
		IEntityModel target = v.getModelTarget();
		if(target.isNull())
			return "null";
		else if(target instanceof IObjectModel)
			return ((IObjectModel) target).getStringValue();
		else
			return target.toString();
	}



}
