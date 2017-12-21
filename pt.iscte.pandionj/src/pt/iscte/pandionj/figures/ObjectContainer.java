package pt.iscte.pandionj.figures;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MarginBorder;

import pt.iscte.pandionj.RuntimeViewer;
import pt.iscte.pandionj.extensibility.ExceptionType;
import pt.iscte.pandionj.extensibility.IArrayModel;
import pt.iscte.pandionj.extensibility.IEntityModel;
import pt.iscte.pandionj.extensibility.IReferenceModel;
import pt.iscte.pandionj.extensibility.PandionJConstants;

public class ObjectContainer extends Figure {

	boolean useExtensions;
	Map<Figure, Integer> indexes;
	
	private ObjectContainer(boolean useExtensions) {
		this.useExtensions = useExtensions;
		setOpaque(true);
		setLayoutManager(new GridLayout(1, true));
		indexes = new HashMap<>();
	}

	public static ObjectContainer create(boolean useExtensions) {
		ObjectContainer c = new ObjectContainer(useExtensions);
		RuntimeViewer.getInstance().addObjectContainer(c);
		return c;
	}

	public void addObjectAndPointer(IReferenceModel ref, ConnectionAnchor sourceAnchor) {
		IEntityModel target = ref.getModelTarget();
		PandionJFigure<?> targetFig = null;
		ConnectionAnchor targetAnchor = null;
		if(!target.isNull()) {
			targetFig = addObject(target, ref.getIndex());
			targetAnchor = targetFig.getIncommingAnchor();
		}
		RuntimeViewer.getInstance().addPointer(ref, sourceAnchor, targetAnchor, this, this);
		updateIllustration(ref, null);
	}

	public PandionJFigure<?> addObject(IEntityModel e) {
		return addObject(e, -1);
	}

	public PandionJFigure<?> addObject(IEntityModel e, int index) {
		PandionJFigure<?> fig = locateFigureInContainers(e);
		if(fig == null) {
			fig = RuntimeViewer.getInstance().getFigureProvider().getFigure(e, useExtensions);
			if(index != -1) {
				List<?> children = getChildren();
				int i = 0;
				for(; i < children.size(); i++) {
					Integer j = indexes.get(children.get(i));
					if(j != null && j > index)
						break;
				}
				add(fig, i);
				indexes.put(fig, index);
			}
			else
				add(fig);
			getLayoutManager().layout(this);
		}
		else
			fig.setBackgroundColor(PandionJConstants.Colors.HIGHLIGHT);
		return fig;
	}

	private PandionJFigure<?> locateFigureInContainers(IEntityModel e) {
		for(ObjectContainer oc : RuntimeViewer.getInstance().getObjectContainers()) {
			for (Object object : oc.getChildren()) {
				if(object instanceof PandionJFigure && ((PandionJFigure<?>) object).getModel() == e)
					return (PandionJFigure<?>) object;
			}
		}
		return null;
	}


	public void updateIllustration(IReferenceModel v, ExceptionType exception) {
		IEntityModel target = v.getModelTarget();
		PandionJFigure<?> fig = locateFigureInContainers(target); //getChild(target);
		if(fig != null) {
			handleIllustration(v, fig.getInnerFigure(), exception);

			if(target instanceof IArrayModel && ((IArrayModel<?>) target).isReferenceType()) {
				IArrayModel<?> a = (IArrayModel<?>) target;
				for (Object e : a.getModelElements())
					updateIllustration((IReferenceModel) e, exception);
			}
		}
	}


	private void handleIllustration(IReferenceModel reference, IFigure targetFig, ExceptionType exception) {
		if(targetFig instanceof AbstractArrayFigure) {
			if(reference.hasIndexVars()) {
				IllustrationBorder b = new IllustrationBorder(reference, (AbstractArrayFigure<?>) targetFig, exception);
				targetFig.setBorder(b);
			}
			else {
				targetFig.setBorder(new MarginBorder(IllustrationBorder.getInsets(targetFig, targetFig instanceof ArrayPrimitiveFigure)));
			}
		}
	} 

//	public void setPointersVisible(boolean visible) {
//	RuntimeViewer.getInstance().showPointers(this, visible); // 2D pointers
//}
//
//public void setVisible(Collection<IReferenceModel> refs, boolean visible) {
//	for(IReferenceModel r : refs)
//		setVisible(r, visible);
//}
//
//public void setVisible(IReferenceModel ref, boolean visible) {
//	IEntityModel t = ref.getModelTarget();
//	if(!t.isNull()) {
//		PandionJFigure<?> f = getChild(t);
//		if(f != null)
//			f.setVisible(visible);
//	}
//}
	
	
//	public Dimension getVisibleBounds() {
//		int w = 0;
//		int h = 0;
//		for (Object object : getChildren()) {
//			IFigure f = (IFigure) object;
//			if(f.isVisible()) {
//				Rectangle r = f.getBounds();
//				w = Math.max(w, r.x+r.width);
//				h = Math.max(h, r.y+r.height);
//			}
//		}
//		return new Dimension(w, h);
//	}

//	private PandionJFigure<?> getChild(IEntityModel e) {
//	for (Object object : getChildren()) {
//		if(object instanceof PandionJFigure && ((PandionJFigure<?>) object).getModel() == e)
//			return (PandionJFigure<?>) object;
//	}
//	return null;
//}
	
//	private PandionJFigure<?> getDeepChild(IEntityModel e) {
//		return getDeepChildRef(this, e);
//	}
//
//	private PandionJFigure<?> getDeepChildRef(IFigure f, IEntityModel e) {
//		for (Object object : f.getChildren()) {
//			if(object instanceof PandionJFigure && ((PandionJFigure<?>) object).getModel() == e)
//				return (PandionJFigure<?>) object;
//
//			PandionJFigure<?> ret = getDeepChildRef((IFigure) object, e);
//			if(ret != null)
//				return ret;
//		}
//		return null;
//	}

	

//	private static boolean containsChild(IFigure parent, IFigure child) {
//		for (Object object : parent.getChildren()) {
//			if(object == child)
//				return true;
//		}
//		return false;
//	}

	//	class Container2d extends Figure {
	//	Container2d(IArrayModel<?> a) {
	//		setOpaque(true);
	//		setLayoutManager(new GridLayout(1, false));
	//
	//		Iterator<Integer> it = a.getValidModelIndexes(); 
	//		while(it.hasNext()) {
	//			add(new Label());
	//			it.next();
	//		}
	//	}
	//
	//	public void addAt(int index, IFigure figure) {
	//		int i = Math.min(index, PandionJView.getMaxArrayLength()-1);
	//		if(!containsChild(this, figure)) {
	//			List<?> children = getChildren();
	//			remove((IFigure) children.get(i));
	//		}
	//		add(figure, i);
	//	}
	//}


	//	public ObjectFigure findObject(IObjectModel obj) {
	//		for (Object object : getChildren()) {
	//			if(object instanceof ObjectFigure && ((ObjectFigure) object).getModel() == obj)
	//				return (ObjectFigure) object;
	//		}
	//		return null;
	//	}
}