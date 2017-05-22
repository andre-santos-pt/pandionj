package pt.iscte.pandionj;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.core.IJavaObject;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import pt.iscte.pandionj.extensibility.IArrayModel;
import pt.iscte.pandionj.extensibility.IArrayWidgetExtension;
import pt.iscte.pandionj.extensibility.IEntityModel;
import pt.iscte.pandionj.extensibility.IObjectModel;
import pt.iscte.pandionj.extensibility.IObjectWidgetExtension;
import pt.iscte.pandionj.extensibility.IWidgetExtension;
import pt.iscte.pandionj.extensions.ColorAguia;
import pt.iscte.pandionj.extensions.ColorWidget;
import pt.iscte.pandionj.extensions.ImageAguia;
import pt.iscte.pandionj.extensions.GrayscaleImageWidget;
import pt.iscte.pandionj.extensions.IterableWidget;
import pt.iscte.pandionj.extensions.StringWidget;
import pt.iscte.pandionj.figures.NullFigure;
import pt.iscte.pandionj.model.ArrayModel;
import pt.iscte.pandionj.model.ObjectModel;

public class ExtensionManager {

	private static Map<String, IArrayWidgetExtension> arrayExtensions;
	private static List<IObjectWidgetExtension> objectExtensions;
	
	static {
		arrayExtensions = new HashMap<String, IArrayWidgetExtension>();
		arrayExtensions.put("image", new GrayscaleImageWidget());
		
		objectExtensions = new ArrayList<>();
		objectExtensions.add(new StringWidget());
		objectExtensions.add(new ColorWidget());
		objectExtensions.add(new IterableWidget());
		objectExtensions.add(new ColorAguia());
		objectExtensions.add(new ImageAguia());
	}
	
	
//	public static <T extends IEntityModel> IWidgetExtension<?> getExtension(T e) {
//		if(e instanceof ArrayModel)
//			return getArrayExtension((ArrayModel) e);
//		else if(e instanceof ObjectModel)
//			return getObjectExtension((ObjectModel) e);
//		else
//			return NO_EXTENSION;
//	}
	
	public static IArrayWidgetExtension getArrayExtension(ArrayModel m) {
		for (String tag : m.getTags()) {
			IArrayWidgetExtension ext = arrayExtensions.get(tag);
			if(ext != null && ext.accept(m))
				return ext;
		}
		return IArrayWidgetExtension.NULL_EXTENSION;
	}


	public static IObjectWidgetExtension getObjectExtension(ObjectModel m) {
		IType type = m.getType();
		for(IObjectWidgetExtension ext : ExtensionManager.objectExtensions)
			if(ext.accept(type))
				return ext;
		return IObjectWidgetExtension.NULL_EXTENSION;
	}


	public static IObjectWidgetExtension createTagExtension(ObjectModel m) {
		return new TagExtension(m.getAttributeTags());
	}
	
	static class TagExtension implements IObjectWidgetExtension {
		
		private Multimap<String, String> tags;
		private Multimap<String, IFigure> figs;
		private IFigure compositeFig;
		public TagExtension(Multimap<String, String> tags) {
			assert tags != null;
			this.tags = tags;
			figs = ArrayListMultimap.create();
		}

		@Override
		public IFigure createFigure(IObjectModel m) {
			// TODO observe ref change
			m.registerDisplayObserver(new Observer() {
				@Override
				public void update(Observable o, Object arg) {
					String attName = (String) arg;
					if(figs.containsKey(attName)) {
						for (IFigure f : figs.get(attName))
							compositeFig.remove(f);
						
						figs.removeAll(attName);
						addChildFigures(m, attName);
					}
				}
			});
			
			compositeFig = new Figure();
			compositeFig.setLayoutManager(new FlowLayout());
			for (Entry<String, Collection<String>> e : tags.asMap().entrySet()) {
				addChildFigures(m, e.getKey());
//				String attName = e.getKey();
//				for(String tag : e.getValue()) {
//					IArrayWidgetExtension ext = arrayExtensions.get(tag);
//					if(ext != null) {
//						IFigure f = ext.createFigure(m.getArray(attName)); // TODO check if array
//						f.setToolTip(new Label(attName));
//						figs.put(e.getKey(), f);
//						compositeFig.add(f);
//					}
//				}
			}
			
			compositeFig.setBackgroundColor(ColorConstants.blue);
			return compositeFig;
		}

		private void addChildFigures(IObjectModel m, String attName) {
			for (String tag : tags.get(attName)) {
				IArrayWidgetExtension ext = arrayExtensions.get(tag);
				if(ext != null) {
					IArrayModel array = m.getArray(attName);
					IFigure f = array == null ? new NullFigure() : ext.createFigure(array);// TODO check if array
					f.setToolTip(new Label(attName));
					figs.put(attName, f);
					compositeFig.add(f);
				}
			}
		}
		
		@Override
		public boolean accept(IType objectType) {
			return true;
		}
		
	}

	public static Set<String> validTags() {
		return Collections.unmodifiableSet(arrayExtensions.keySet());
	}
}
