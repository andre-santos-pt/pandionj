package pt.iscte.pandionj.model;

import java.util.Map;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaArrayType;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaReferenceType;
import org.eclipse.jdt.debug.core.IJavaType;

class ObjectSet {

	private Map<Long, EntityModel<?>> objects;
	private Map<Long, EntityModel<?>> looseObjects;
	

	public EntityModel<? extends IJavaObject> getObject(IJavaObject obj, boolean loose, StackFrameModel stack) {
		assert !obj.isNull();
		try {
			EntityModel<? extends IJavaObject> e = objects.get(obj.getUniqueId());
//			if(e == null)
//				e = parent.getStaticReference();
			
			if(e == null) {
				if(obj.getJavaType() instanceof IJavaArrayType) {
					IJavaType componentType = ((IJavaArrayType) obj.getJavaType()).getComponentType();
					if(componentType instanceof IJavaReferenceType)
						e = new ArrayReferenceModel((IJavaArray) obj, stack);
					else
						e = new ArrayPrimitiveModel((IJavaArray) obj, stack);
				}
				else {
					e = new ObjectModel(obj, stack);
				}

				if(loose) {
					looseObjects.put(obj.getUniqueId(), e);
				}
				else {
					objects.put(obj.getUniqueId(), e);
				}
				setChanged();
			}
			return e;
		}
		catch(DebugException e) {
			e.printStackTrace();
			return null;
		}
	}

}
