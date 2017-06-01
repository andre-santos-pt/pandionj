package pt.iscte.pandionj;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.zest.core.viewers.IGraphEntityRelationshipContentProvider;

import pt.iscte.pandionj.model.ArrayReferenceModel;
import pt.iscte.pandionj.model.EntityModel;
import pt.iscte.pandionj.model.ModelElement;
import pt.iscte.pandionj.model.NullModel;
import pt.iscte.pandionj.model.ObjectModel;
import pt.iscte.pandionj.model.ReferenceModel;
import pt.iscte.pandionj.model.StackFrameModel;
import pt.iscte.pandionj.model.ValueModel;
import pt.iscte.pandionj.model.ValueModel.Role;
import pt.iscte.pandionj.model.VariableModel;

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
		if(model == null)
			return EMPTY;

		List<ModelElement<?>> elements = new ArrayList<>(model.getVariables());
		Iterator<ModelElement<?>> it = elements.iterator();
		while(it.hasNext()) {
			VariableModel<?> v = (VariableModel<?>) it.next();
			if((v.isOutOfScope() || v instanceof ValueModel && ((ValueModel) v).getRole() == Role.ARRAY_ITERATOR))
				it.remove();
		}
		
		for(ModelElement<?> e : elements.toArray(new ModelElement[elements.size()])) {
			if(e instanceof ReferenceModel) {
				ReferenceModel r = (ReferenceModel) e;
				EntityModel<?> t = r.getModelTarget();
				if(t instanceof ObjectModel && r.getTags().isEmpty()) {
					((ObjectModel) t).traverseSiblings((o,p,i,d,f) -> elements.add(o), true);
				}
				
				// TODO more than 2 dims?
				else if(t instanceof ArrayReferenceModel && r.getTags().isEmpty()) {
					elements.add(t); 
					List<ReferenceModel> arrayElements = ((ArrayReferenceModel) t).getModelElements();
					for(ReferenceModel ref : arrayElements)
						elements.add(ref.getModelTarget());
				}
				else
					elements.add(t);
			}
		}
		
		elements.addAll(model.getLooseObjects());
		return elements.toArray();
	}

	@Override
	public Object[] getRelationships(Object source, Object dest) {
		if(source instanceof ReferenceModel && ((ReferenceModel) source).getModelTarget().equals(dest)) {
			return new Object[] { new Pointer((ModelElement<?>) source, (EntityModel<?>) dest) };
		}
		else if(source instanceof ObjectModel) {
			Map<String, ReferenceModel> pointers = ((ObjectModel) source).getReferences();
			List<Pointer> ret = new ArrayList<>();
			for(Entry<String, ReferenceModel> field : pointers.entrySet()) 
				if(dest.equals(field.getValue().getModelTarget()))
					ret.add(new Pointer(field.getKey(), (ObjectModel) source, (EntityModel<?>) dest));
			return ret.toArray();
		}
		else if(source instanceof ArrayReferenceModel && !((ArrayReferenceModel) source).hasWidgetExtension()) {
			List<Pointer> ret = new ArrayList<>();
			List<ReferenceModel> elements = ((ArrayReferenceModel) source).getModelElements();
			for(int i = 0; i < elements.size(); i++)
				if(dest.equals(elements.get(i).getModelTarget()))
					ret.add(new Pointer("[" + Integer.toString(i) + "]", (ModelElement<?>) source, (EntityModel<?>) dest));
			return ret.toArray();
		}
		else
			return EMPTY;
	}


	static class Pointer {
		final String refName;
		final ModelElement<?> source;
		final EntityModel<?> target;

		public Pointer(ModelElement<?> source, EntityModel<?> target) {
			this("", source, target);
		}

		public Pointer(String refName, ModelElement<?> source, EntityModel<?> target) {
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

		public boolean isTopLevel() {
			return refName == null;
		}
	}
}
