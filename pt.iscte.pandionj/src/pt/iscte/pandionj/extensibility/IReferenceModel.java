package pt.iscte.pandionj.extensibility;

import java.util.Collection;

public interface IReferenceModel extends IVariableModel {
	IEntityModel getModelTarget();
	boolean hasIndexVars();
	Collection<IArrayIndexModel> getIndexVars();
	Collection<IArrayIndexModel> getFixedIndexes();
	int getIndex();
	void setIndex(int i);
}
