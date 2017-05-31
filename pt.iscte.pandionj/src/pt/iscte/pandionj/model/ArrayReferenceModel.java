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

public class ArrayReferenceModel extends ArrayModel {

	private List<ReferenceModel> references;

	public ArrayReferenceModel(IJavaArray array, StackFrameModel model) {
		super(array, model);
	}

	@Override
	protected void initArray(IJavaArray array) {
		try {
			references = new ArrayList<>(array.getLength());
			IVariable[] variables = array.getVariables();
			for(int i = 0; i < variables.length; i++) {
				ReferenceModel referenceModel = new ReferenceModel((IJavaVariable) variables[i], true, getStackFrame());
				references.add(referenceModel);
//				if(getDimensions() > 1) {
//					ArrayModel line = (ArrayModel) referenceModel.getModelTarget();
//				}
				//				int ii = i;
				//				referenceModel.registerObserver(new Observer() {
				//					public void update(Observable o, Object arg) {
				//						setChanged();
				//						notifyObservers(new int[] {ii});
				//						getStackFrame().objectReferenceChanged();
				//					}
				//				});

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

	@Override
	protected IFigure createArrayFigure() {
		return new ArrayReferenceFigure(this);
	}


	public List<ReferenceModel> getModelElements() {
		return Collections.unmodifiableList(references);
	}


	//	public boolean update(int step) {
	//		try {
	//			IJavaValue[] values = entity.getValues();
	//			List<Integer> changes = new ArrayList<Integer>();
	//			for(int i = 0; i < elements.length; i++) {
	//				IJavaObject array = references.get(i).getContent();
	//				boolean equals = values[i].equals(array);
	//				if(!equals) {
	//					elements[i] = values[i];
	//					changes.add(i);
	//				}
	//			}
	//			if(!changes.isEmpty()) {
	//				setChanged();
	//				notifyObservers(changes);
	//			}
	//
	//		}
	//		catch(DebugException e) {
	//			e.printStackTrace();
	//		}
	//		long refChanges = references.stream().filter(ref -> ref.update(step)).count();
	//		for(ReferenceModel r : references)
	//			r.update(step);
	//		// TODO review
	//		setChanged();
	//		notifyObservers();
	//		
	//		return refChanges != 0;
	//	}



	//	public String getElementString(int i) {
	//		assert i >= 0 && i < references.size();
	//		return references.get(i).toString();
	//	}





}
