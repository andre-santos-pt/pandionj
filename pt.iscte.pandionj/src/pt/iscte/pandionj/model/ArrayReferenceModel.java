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
import org.eclipse.jdt.debug.core.IJavaValue;
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
			references = new ArrayList<>();
			IVariable[] variables = array.getVariables();
			for(int i = 0; i < variables.length; i++) {
				ReferenceModel referenceModel = new ReferenceModel((IJavaVariable) variables[i], true, getStackFrame());
				references.add(referenceModel);
				int ii = i;
				referenceModel.registerObserver(new Observer() {
					public void update(Observable o, Object arg) {
						setChanged();
						notifyObservers(new int[] {ii});
						getStackFrame().objectReferenceChanged();
					}
				});
				
			}
		}
		catch(DebugException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected IFigure createArrayFigure() {
		return new ArrayReferenceFigure(this);
	}

	public void update(int step) {
		try {
			IJavaValue[] values = entity.getValues();
			List<Integer> changes = new ArrayList<Integer>();
			for(int i = 0; i < elements.length; i++) {
				boolean equals = values[i].equals(references.get(i).getContent());
				if(!equals) {
					elements[i] = values[i];
					changes.add(i);
				}
			}
			if(!changes.isEmpty()) {
				setChanged();
				notifyObservers(changes);
			}

		}
		catch(DebugException e) {
			e.printStackTrace();
		}
				for(ReferenceModel r : references)
					r.update(step);
		//		Object[][] values = (Object[][]) getValues();

		// TODO review
		setChanged();
		notifyObservers();
	}

	public int getLength() {
		return references.size();
	}



	//	@Override // TODO refazer
	//	public Object[] getValues() {
	//		try {
	//			//			IJavaValue[] values = entity.getValues();
	//			//			int[] dims = new int[getDimensions()];
	//			//			for(int i = 0; i < dims.length; i++)
	//			//				dims[i] = getLength();
	//
	//			//			Object array = Array.newInstance(Object.class, dims);
	//			Object[] array = new Object[getLength()];
	//
	//			//			Object[][] v = new Object[values.length][];
	//
	//			IJavaType t = entity.getJavaType();
	//			while(t instanceof IJavaArrayType) {
	//				int len = entity.getLength();
	//				for(int i = 0; i < len; i++) {
	//					IJavaValue[] line = ((IJavaArray) entity.getValue(i)).getValues();
	//					t = ((IJavaArrayType) t).getComponentType();
	//					if(t instanceof IJavaArrayType) {
	//						array[i] = new Object[line.length];
	//					}
	//					else {
	//						if(PrimitiveType.isPrimitive(t.getName())) {
	//							Object[] values = ArrayPrimitiveModel.getPrimitiveWrapperValues(line, getComponentType());
	//							array[i] = values;
	//						}
	//						else {
	//							Object[] values = new Object[line.length];
	//							for(int j = 0; j < values.length; j++)
	//								values[j] = line[j].isNull() ? null : line[j].getValueString();
	//						}
	//					}
	//				}
	//				t = ((IJavaArrayType) t).getComponentType();
	//			}
	//
	//			for(int i = 0; i < getLength(); i++) {
	//				IJavaValue[] line = ((IJavaArray) elements[i]).getValues();
	//				Object[] values = ArrayPrimitiveModel.getPrimitiveWrapperValues(line, getComponentType());
	//				Array.set(array, i, values);
	//			}
	//			return (Object[]) array;
	//		} catch (DebugException e) {
	//			e.printStackTrace();
	//			return null;
	//		}
	//	}

	public String getElementString(int i) {
		assert i >= 0 && i < references.size();
		return references.get(i).toString();
	}


	public List<ReferenceModel> getModelElements() {
		return Collections.unmodifiableList(references);
	}

}
