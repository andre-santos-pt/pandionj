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


	public void updateIllustration(IReferenceModel v, Object illustrationArg) {
		IEntityModel target = v.getModelTarget();
		PandionJFigure<?> fig = locateFigureInContainers(target);
		if(fig != null) {
			handleIllustration(v, fig.getInnerFigure(), illustrationArg);

			if(target instanceof IArrayModel && ((IArrayModel<?>) target).isReferenceType()) {
				IArrayModel<?> a = (IArrayModel<?>) target;
				for (Object e : a.getModelElements())
					updateIllustration((IReferenceModel) e, illustrationArg);
			}
		}
	}


	private void handleIllustration(IReferenceModel reference, IFigure targetFig, Object illustrationArg) {
		if(targetFig instanceof AbstractArrayFigure) {
			if(reference.hasIndexVars()) {
				Integer indexOutOfBounds = null;
				if(illustrationArg instanceof Integer)
					indexOutOfBounds = (Integer) illustrationArg;
				IllustrationBorder b = new IllustrationBorder(reference, (AbstractArrayFigure<?>) targetFig, indexOutOfBounds);
				targetFig.setBorder(b);
			}
			else {
				targetFig.setBorder(new MarginBorder(IllustrationBorder.getInsets(targetFig, targetFig instanceof ArrayPrimitiveFigure)));
			}
		}
	} 

}