package pt.iscte.pandionj.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.draw2d.IFigure;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaVariable;

import pt.iscte.pandionj.figures.ArrayReferenceFigure;


// TODO: limit size?
public class ArrayReferenceModel extends ArrayModel {
	private List<ReferenceModel> references;

	public ArrayReferenceModel(IJavaArray array, RuntimeModel runtime) {
		super(array, runtime);
	}

	@Override
	protected void initArray(IJavaArray array, RuntimeModel runtime) {
		try {
			references = new ArrayList<>(array.getLength());
			IVariable[] variables = array.getVariables();
			for(int i = 0; i < variables.length; i++) {
				ReferenceModel referenceModel = new ReferenceModel((IJavaVariable) variables[i], true, runtime);
				references.add(referenceModel);
			}
		}
		catch(DebugException e) {
			e.printStackTrace();
		}
	}

	@Override
	boolean updateInternal(int i, int step) {
		ReferenceModel refModel = references.get(i);
		return refModel.update(step);
	}

//	@Override
//	protected IFigure createArrayFigure() {
//		return new ArrayReferenceFigure(this);
//	}

	public List<ReferenceModel> getModelElements() {
		return Collections.unmodifiableList(references);
	}
	
	@Override
	public void setStep(int stepPointer) {
		for(ReferenceModel ref : references)
			ref.setStep(stepPointer);

	}
}
