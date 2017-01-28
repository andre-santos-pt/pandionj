package pt.iscte.pandionj;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.zest.core.viewers.IGraphEntityContentProvider;
import org.eclipse.zest.core.viewers.IGraphEntityRelationshipContentProvider;

import pt.iscte.pandionj.model.ModelElement;
import pt.iscte.pandionj.model.NullModel;
import pt.iscte.pandionj.model.ObjectModel;
import pt.iscte.pandionj.model.ReferenceModel;
import pt.iscte.pandionj.model.StackFrameModel;

class NodeProvider implements IGraphEntityRelationshipContentProvider { // IGraphEntityContentProvider
	private static final Object[] EMPTY = new Object[0];

	private StackFrameModel model;
	
	
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		model = (StackFrameModel) newInput;
	}

	@Override
	public void dispose() {

	}
	
	@Override
	public Object[] getElements(Object input) {
		List<ModelElement> elements = new ArrayList<>(model.getVariables());
		for(ModelElement obj : model.getObjects()) {
			if(obj instanceof ObjectModel)
				addReferences((ObjectModel) obj, elements);
			else
				elements.add(obj);
		}
		return elements.toArray();
	}

	private void addReferences(ObjectModel obj, List<ModelElement> elements) {
		if(!elements.contains(obj)) {
			elements.add(obj);
			for(ModelElement e : obj.getReferences().values())
				if(e instanceof ObjectModel)
					addReferences((ObjectModel) e, elements);
		}
	}
	
//	@Override
//	public Object[] getConnectedTo(Object entity) {
//		if(entity instanceof ReferenceModel)
//			return new Object[] { ((ReferenceModel) entity).getTarget() };
//		else if(entity instanceof ObjectModel)
//			return ((ObjectModel) entity).getReferences().values().toArray();
//		else
//			return EMPTY;
//	}

	@Override
	public Object[] getRelationships(Object source, Object dest) {
		if(source instanceof ReferenceModel && ((ReferenceModel) source).getTarget().equals(dest))
			return new Object[] { new Pointer((ModelElement) source, (ModelElement) dest) };
		else if(source instanceof ObjectModel) {
			Map<String, ModelElement> pointers = ((ObjectModel) source).getReferences();
			List<Pointer> ret = new ArrayList<>();
			for(Entry<String, ModelElement> field : pointers.entrySet())
				if(dest.equals(field.getValue()))
					ret.add(new Pointer(field.getKey(), (ObjectModel) source, (ModelElement) dest));
			return ret.toArray();
		}
		else
			return EMPTY;
	}
	
	static class Pointer {
		final String refName;
		final ModelElement source;
		final ModelElement target;
		
		public Pointer(ModelElement source, ModelElement target) {
			this("", source, target);
		}
		
		public Pointer(String refName, ModelElement source, ModelElement target) {
			this.refName = refName;
			this.source = source;
			this.target = target;
		}
		
		@Override
		public String toString() {
			return source + " -> " + target;
		}
		
		public boolean isNull() {
			return target instanceof NullModel;
		}
	}
}
