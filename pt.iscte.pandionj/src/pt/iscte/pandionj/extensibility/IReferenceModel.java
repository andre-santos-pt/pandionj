package pt.iscte.pandionj.extensibility;

import java.util.Collection;

public interface IReferenceModel extends IVariableModel<IEntityModel> {
	IEntityModel getModelTarget();
	boolean hasIndexVars();
	Collection<IArrayIndexModel> getIndexVars();
	Collection<IArrayIndexModel> getFixedIndexes();
	Collection<String> getTags();
	int getIndex();
	void setIndex(int i);
}
