package pt.iscte.pandionj;


import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.eclipse.draw2d.AbstractConnectionAnchor;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.PolylineDecoration;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.zest.core.viewers.IConnectionStyleProvider;
import org.eclipse.zest.core.viewers.IFigureProvider;
import org.eclipse.zest.core.viewers.ISelfStyleProvider;
import org.eclipse.zest.core.widgets.GraphConnection;
import org.eclipse.zest.core.widgets.GraphNode;
import org.eclipse.zest.core.widgets.ZestStyles;

import pt.iscte.pandionj.NodeProvider.Pointer;
import pt.iscte.pandionj.extensibility.IArrayIndexModel;
import pt.iscte.pandionj.extensibility.IArrayWidgetExtension;
import pt.iscte.pandionj.figures.ArrayPrimitiveFigure;
import pt.iscte.pandionj.figures.ArrayPrimitiveFigure2;
import pt.iscte.pandionj.figures.ArrayReferenceFigure;
import pt.iscte.pandionj.figures.BaseFigure;
import pt.iscte.pandionj.figures.IllustrationBorder;
import pt.iscte.pandionj.figures.NullFigure;
import pt.iscte.pandionj.figures.ObjectFigure;
import pt.iscte.pandionj.figures.ReferenceFigure;
import pt.iscte.pandionj.figures.ValueFigure;
import pt.iscte.pandionj.model.ArrayModel;
import pt.iscte.pandionj.model.ArrayPrimitiveModel;
import pt.iscte.pandionj.model.ArrayReferenceModel;
import pt.iscte.pandionj.model.EntityModel;
import pt.iscte.pandionj.model.ModelElement;
import pt.iscte.pandionj.model.NullModel;
import pt.iscte.pandionj.model.ObjectModel;
import pt.iscte.pandionj.model.ReferenceModel;
import pt.iscte.pandionj.model.StackFrameModel;
import pt.iscte.pandionj.model.ValueModel;

class FigureProvider extends LabelProvider implements IFigureProvider, IConnectionStyleProvider, ISelfStyleProvider {
	private Map<ModelElement<?>, IFigure> figCache = new WeakHashMap<>();

	private StackFrameModel stackFrame;

	public FigureProvider(StackFrameModel stackFrame) {
		this.stackFrame = stackFrame;
	}

	@Override
	public IFigure getFigure(Object element) {
		IFigure fig = figCache.get(element);
		if(fig == null) {
			ModelElement<?> model = (ModelElement<?>) element;
			//			fig = model.createFigure();
			fig = createFigure(model);
			figCache.put(model, fig);
		}
		return fig;		
	}

	private IFigure createFigure(ModelElement<?> model) {
		IFigure innerFig = null;

		if(model instanceof ValueModel) {
			ValueModel vModel = (ValueModel) model;
			innerFig = new ValueFigure(vModel, vModel.getRole());
		}
		else if(model instanceof ReferenceModel) {
			ReferenceModel rModel = (ReferenceModel) model;
			innerFig = new ReferenceFigure(rModel);
		}
		else if(model instanceof NullModel) {
			innerFig = new NullFigure();
		}
		else { // entities
			Set<String> tags = getEntityTags((EntityModel<?>) model);

			if(model instanceof ArrayModel) {
				ArrayModel aModel = (ArrayModel) model;
				IArrayWidgetExtension arrayExtension = ExtensionManager.getArrayExtension(aModel, tags);
				innerFig = arrayExtension.createFigure(aModel);
				if(innerFig == null) {
					if(model instanceof ArrayPrimitiveModel) {
						innerFig = new ArrayPrimitiveFigure2((ArrayPrimitiveModel) model);
					}
					else if(model instanceof ArrayReferenceModel) {
						innerFig = new ArrayReferenceFigure((ArrayReferenceModel) model);
					}
				}
			}
			else if(model instanceof ObjectModel) {
				ObjectModel oModel = (ObjectModel) model; 
				IFigure extensionFigure = ExtensionManager.getObjectExtension(oModel).createFigure(oModel);
				innerFig = new ObjectFigure(oModel, extensionFigure, true);
			}
		}
		assert innerFig != null : model;
		BaseFigure base = new BaseFigure(innerFig);
		return base;
	}


	private Set<String> getEntityTags(EntityModel<?> e) {
		Set<String> tags = new HashSet<String>();
		Collection<ReferenceModel> references = stackFrame.getReferencesTo(e);
		for(ReferenceModel r : references)
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



	@Override
	public String getText(Object e) {
		//		if(element instanceof EntityConnectionData)
		//			return "";
		//		else if(element instanceof Pointer)
		//			return ((Pointer) element).refName;
		//		else
		//			return element.toString();
		//		if(e instanceof Pointer && !((Pointer) e).isTopLevel())
		return null;
	}

	@Override
	public IFigure getTooltip(Object entity) {
		if(entity instanceof Pointer) {
			if(((Pointer) entity).isTopLevel())
				return new Label("");
			else
				return new Label(((Pointer) entity).reference.getName());
		}
		else
			return new Label(entity.toString());
	}

	@Override
	public int getConnectionStyle(Object rel) {
		return ZestStyles.CONNECTIONS_SOLID;
	}

	@Override
	public Color getColor(Object rel) {
		return ColorConstants.black;
	}

	@Override
	public Color getHighlightColor(Object rel) {
		return Constants.Colors.SELECT;
	}

	@Override
	public int getLineWidth(Object rel) {
		return Constants.ARROW_LINE_WIDTH;
	}

	@Override
	public void selfStyleNode(Object element, GraphNode node) {
		node.getNodeFigure().setSize(-1, -1);
	}

	@Override
	public void selfStyleConnection(Object element, GraphConnection connection) {
		PolylineConnection conn = (PolylineConnection) connection.getConnectionFigure();
		Pointer pointer = (Pointer) element;

		if(pointer.isNull()) {
			PolygonDecoration decoration = new PolygonDecoration();
			PointList points = new PointList();
			points.addPoint(0,-1); // 1
			points.addPoint(0, 1); // -1
			decoration.setTemplate(points);
			decoration.setScale(Constants.ARROW_EDGE, Constants.ARROW_EDGE);
			decoration.setLineWidth(Constants.ARROW_LINE_WIDTH);
			decoration.setOpaque(true);
			conn.setTargetDecoration(decoration);	
		}
		else {
			PolylineDecoration decoration = new PolylineDecoration();
			PointList points = new PointList();
			points.addPoint(-1, -1);
			points.addPoint(0, 0);
			points.addPoint(-1, 1);
			decoration.setTemplate(points);
			decoration.setScale(Constants.ARROW_EDGE, Constants.ARROW_EDGE);
			decoration.setLineWidth(Constants.ARROW_LINE_WIDTH);
			decoration.setOpaque(true);
			conn.setTargetDecoration(decoration);

			//			fig.setSourceAnchor(new PositionAnchor(connection.getSource().getNodeFigure(), Position.RIGHT));
			//			fig.setTargetAnchor(new PositionAnchor(connection.getDestination().getNodeFigure(), Position.LEFT));
		}

		IFigure sFig = ((BaseFigure) connection.getSource().getNodeFigure()).getInnerFigure();
		IFigure tFig = ((BaseFigure) connection.getDestination().getNodeFigure()).getInnerFigure();

		if(sFig instanceof ArrayReferenceFigure) {
			String refName = ((Pointer) element).reference.getName();
			refName = refName.substring(1, refName.length()-1);
			conn.setSourceAnchor(((ArrayReferenceFigure) sFig).getAnchor(Integer.parseInt(refName)));
		}

		if(tFig instanceof ArrayPrimitiveFigure2 || tFig instanceof ArrayReferenceFigure) {
			conn.setTargetAnchor(new PositionAnchor(tFig, Position.TOPLEFT));
		}
		
		

		handleIllustration(connection.getSource(), sFig, connection.getDestination(), tFig, pointer.reference);

		// TODO repor
		//		fig.setVisible(pointer.source.isWithinScope() && pointer.target.isWithinScope());
	}

	private void handleIllustration(GraphNode source, IFigure sourceFig, GraphNode target, IFigure targetFig, ReferenceModel reference) {
		if(source.getData() instanceof ReferenceModel && 
				target.getData() instanceof ArrayModel &&
				targetFig instanceof ArrayPrimitiveFigure2) {

			ReferenceModel r = (ReferenceModel) source.getData();

			if(r.hasIndexVars()) {
				IllustrationBorder b = new IllustrationBorder(r, (ArrayPrimitiveFigure2) targetFig);
				targetFig.setBorder(b);
			}
		}
		else if(source.getData() instanceof ArrayReferenceModel && 
				target.getData() instanceof ArrayPrimitiveModel) {

			ArrayReferenceModel array = (ArrayReferenceModel) source.getData();
			targetFig.setBorder(new IllustrationBorder(reference, (ArrayPrimitiveFigure2) targetFig));
		}

	}

	enum Position {
		TOP {
			@Override
			Point getPoint(Rectangle r) {
				return new Point(r.x + r.width/2, r.y);
			}
		},
		RIGHT {
			@Override
			Point getPoint(Rectangle r) {
				return new Point(r.x + r.width, r.y + r.height/2);
			}
		},
		BOTTOM {
			@Override
			Point getPoint(Rectangle r) {
				return new Point(r.x + r.width/2, r.y + r.height);
			}
		},
		LEFT {
			@Override
			Point getPoint(Rectangle r) {
				return new Point(r.x, r.y + r.height/2);
			}
		},
		TOPLEFT {
			@Override
			Point getPoint(Rectangle r) {
				return new Point(r.x, r.y + r.height/4);
			}
		};

		abstract Point getPoint(org.eclipse.draw2d.geometry.Rectangle r);
	}

	private class PositionAnchor extends AbstractConnectionAnchor {

		private Position position;

		public PositionAnchor(IFigure fig, Position position) {
			super(fig);
			this.position = position;
		}

		@Override
		public Point getLocation(Point reference) {
			org.eclipse.draw2d.geometry.Rectangle r =  org.eclipse.draw2d.geometry.Rectangle.SINGLETON;
			r.setBounds(getOwner().getBounds());
			r.translate(0, 0);
			r.resize(1, 1);
			getOwner().translateToAbsolute(r);
			return position.getPoint(r);
		}
	}
}
