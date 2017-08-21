package pt.iscte.pandionj;


import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.eclipse.draw2d.AbstractConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

import pt.iscte.pandionj.extensibility.IArrayModel;
import pt.iscte.pandionj.extensibility.IArrayWidgetExtension;
import pt.iscte.pandionj.extensibility.IEntityModel;
import pt.iscte.pandionj.extensibility.IObjectModel;
import pt.iscte.pandionj.extensibility.IObservableModel;
import pt.iscte.pandionj.extensibility.IReferenceModel;
import pt.iscte.pandionj.extensibility.IStackFrameModel;
import pt.iscte.pandionj.extensibility.IValueModel;
import pt.iscte.pandionj.figures.ArrayPrimitiveFigure2;
import pt.iscte.pandionj.figures.ArrayReferenceFigure;
import pt.iscte.pandionj.figures.NullFigure;
import pt.iscte.pandionj.figures.ObjectFigure;
import pt.iscte.pandionj.figures.PandionJFigure;
import pt.iscte.pandionj.figures.ReferenceFigure;
import pt.iscte.pandionj.figures.ValueFigure;

public class FigureProvider2  {
	private Map<IObservableModel<?>, PandionJFigure<?>> figCache = new WeakHashMap<>();

	private IStackFrameModel stackFrame;

	public FigureProvider2(IStackFrameModel stackFrame) {
		this.stackFrame = stackFrame;
	}

	public PandionJFigure<?> getFigure(Object element) {
		PandionJFigure<?> fig = figCache.get(element);
		if(fig == null) {
			IObservableModel<?> model = (IObservableModel<?>) element;
			//			fig = model.createFigure();
			fig = createFigure(model);
			figCache.put(model, fig);
		}
		return fig;		
	}

	private PandionJFigure<?> createFigure(IObservableModel<?> model) {
		PandionJFigure<?> fig = null;
		
		if(model instanceof IValueModel) {
			fig = new ValueFigure((IValueModel) model);
		}
		else if(model instanceof IReferenceModel) {
			fig = new ReferenceFigure((IReferenceModel) model);
		}
		else if(model instanceof IEntityModel) {
			IEntityModel entity = (IEntityModel) model;
			if(entity.isNull())
				fig = new NullFigure(entity);
			else {
				Set<String> tags = getEntityTags(entity);

				if(model instanceof IArrayModel) {
					IArrayModel aModel = (IArrayModel) model;
					IArrayWidgetExtension arrayExtension = ExtensionManager.getArrayExtension(aModel, tags);
					IFigure extFig = arrayExtension.createFigure(aModel);
					if(extFig == null) {
						if(aModel.isPrimitiveType()) {
							fig = new ArrayPrimitiveFigure2(aModel);
						}
						else {
							fig = new ArrayReferenceFigure(aModel);
						}
					}
					else {
						fig = new PandionJFigure.Extension(extFig, model);
					}
				}
				else if(model instanceof IObjectModel) {
					IObjectModel oModel = (IObjectModel) model; 
					IFigure extensionFigure = ExtensionManager.getObjectExtension(oModel).createFigure(oModel);
					fig = new ObjectFigure(oModel, extensionFigure, true);
				}
			}
		}
		assert fig != null : model;
		return fig;
	}


	private Set<String> getEntityTags(IEntityModel e) {
		Set<String> tags = new HashSet<String>();
		for(IReferenceModel r : stackFrame.getReferencesTo(e))
			tags.addAll(r.getTags());
		return tags;
	}

	//	protected IFigure createExtensionFigure(ObjectModel model) {
	////		if(model.hasAttributeTags()) {
	////			extension = ExtensionManager.createTagExtension(model);
	////		}
	////		else if(extension == null) {
	//			extension = ExtensionManager.getObjectExtension(model).createFigure(model);
	////		}
	//		
	//		return extension.createFigure(this);
	//	}


//	private void selfStyleConnection(Object element, GraphConnection connection) {
//		PolylineConnection conn = (PolylineConnection) connection.getConnectionFigure();
//		Pointer pointer = (Pointer) element;
//
//		if(pointer.isNull()) {
//			PolygonDecoration decoration = new PolygonDecoration();
//			PointList points = new PointList();
//			points.addPoint(0,-1); // 1
//			points.addPoint(0, 1); // -1
//			decoration.setTemplate(points);
//			decoration.setScale(Constants.ARROW_EDGE, Constants.ARROW_EDGE);
//			decoration.setLineWidth(Constants.ARROW_LINE_WIDTH);
//			decoration.setOpaque(true);
//			conn.setTargetDecoration(decoration);	
//		}
//		else {
//			PolylineDecoration decoration = new PolylineDecoration();
//			PointList points = new PointList();
//			points.addPoint(-1, -1);
//			points.addPoint(0, 0);
//			points.addPoint(-1, 1);
//			decoration.setTemplate(points);
//			decoration.setScale(Constants.ARROW_EDGE, Constants.ARROW_EDGE);
//			decoration.setLineWidth(Constants.ARROW_LINE_WIDTH);
//			decoration.setOpaque(true);
//			conn.setTargetDecoration(decoration);
//
//			//			fig.setSourceAnchor(new PositionAnchor(connection.getSource().getNodeFigure(), Position.RIGHT));
//			//			fig.setTargetAnchor(new PositionAnchor(connection.getDestination().getNodeFigure(), Position.LEFT));
//		}
//
//		IFigure sFig = ((BaseFigure) connection.getSource().getNodeFigure()).getInnerFigure();
//		IFigure tFig = ((BaseFigure) connection.getDestination().getNodeFigure()).getInnerFigure();
//
//		if(sFig instanceof ArrayReferenceFigure) {
//			String refName = ((Pointer) element).reference.getName();
//			refName = refName.substring(1, refName.length()-1);
//			conn.setSourceAnchor(((ArrayReferenceFigure) sFig).getAnchor(Integer.parseInt(refName)));
//		}
//
//		if(tFig instanceof ArrayPrimitiveFigure2 || tFig instanceof ArrayReferenceFigure) {
//			conn.setTargetAnchor(new PositionAnchor(tFig, Position.TOPLEFT));
//		}
//
//
//
////		handleIllustration(connection.getSource(), sFig, connection.getDestination(), tFig, pointer.reference);
//
//		// TODO repor
//		//		fig.setVisible(pointer.source.isWithinScope() && pointer.target.isWithinScope());
//	}

//	public void handleIllustration(IReferenceModel reference, IFigure sourceFig, IFigure targetFig) {
//		
//			if(targetFig instanceof ArrayPrimitiveFigure2 &&  reference.hasIndexVars()) {
//				IllustrationBorder b = new IllustrationBorder(reference, (ArrayPrimitiveFigure2) targetFig);
//				targetFig.setBorder(b);
//			}
		
//		else if(source.getData() instanceof ArrayReferenceModel && 
//				target.getData() instanceof ArrayPrimitiveModel) {
//
//			ArrayReferenceModel array = (ArrayReferenceModel) source.getData();
//			targetFig.setBorder(new IllustrationBorder(reference, (ArrayPrimitiveFigure2) targetFig));
//		}
//
//	}
//	} 

}
