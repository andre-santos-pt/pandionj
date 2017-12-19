package pt.iscte.pandionj;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.jdt.core.IType;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import pt.iscte.pandionj.extensibility.IArrayModel;
import pt.iscte.pandionj.extensibility.IArrayWidgetExtension;
import pt.iscte.pandionj.extensibility.IObjectModel;
import pt.iscte.pandionj.extensibility.ITypeWidgetExtension;
import pt.iscte.pandionj.extensibility.ModelObserver;
import pt.iscte.pandionj.extensibility.PandionJConstants;
import pt.iscte.pandionj.extensions.ColorWidget;
import pt.iscte.pandionj.extensions.IterableWidget;
import pt.iscte.pandionj.extensions.StringWidget;

public class ExtensionManager {

	private static Map<String, IArrayWidgetExtension> arrayExtensions;
	private static List<ITypeWidgetExtension> objectExtensions;

	static {
		IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();

		arrayExtensions = new HashMap<String, IArrayWidgetExtension>();
		IConfigurationElement[] extsArray = extensionRegistry.getConfigurationElementsFor(PandionJConstants.ARRAYTAG_EXTENSION_ID);
		for(IConfigurationElement e : extsArray) {
			try {
				String name = e.getAttribute("name");
				IArrayWidgetExtension ae = (IArrayWidgetExtension) e.createExecutableExtension("class");
				arrayExtensions.put("@" + name, ae);
			} catch (CoreException e1) {
				e1.printStackTrace();
			}
		}
		

		objectExtensions = new ArrayList<>();
		IConfigurationElement[] extsObj = extensionRegistry.getConfigurationElementsFor(PandionJConstants.TYPE_EXTENSION_ID);
		for(IConfigurationElement e : extsObj) {
			try {
				ITypeWidgetExtension tw = (ITypeWidgetExtension) e.createExecutableExtension("class");
				objectExtensions.add(tw);
			} catch (CoreException e1) {
				e1.printStackTrace();
			}
		}		
	}


	// TODO composite extension? (as TagExtension?)
	public static IArrayWidgetExtension getArrayExtension(IArrayModel<?> m, Collection<String> tags) {

		for (String tag : tags) {
			IArrayWidgetExtension ext = arrayExtensions.get(tag);
			if(ext != null && ext.accept(m))
				return ext;
		}
		return IArrayWidgetExtension.NULL_EXTENSION;
	}


	public static ITypeWidgetExtension getObjectExtension(IObjectModel m) {
		if(m.hasAttributeTags())
			return new TagExtension(m.getAttributeTags());

		IType type = m.getType();
		if(type != null) {
			for(ITypeWidgetExtension ext : ExtensionManager.objectExtensions)
				if(ext.accept(type))
					return ext;
		}
		return ITypeWidgetExtension.NULL_EXTENSION;
	}


	//	public static IObjectWidgetExtension createTagExtension(ObjectModel m) {
	//		return new TagExtension(m.getAttributeTags());
	//	}

	static class TagExtension implements ITypeWidgetExtension {

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
			m.registerDisplayObserver(new ModelObserver<Object>() {
				@Override
				public void update(Object arg) {
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
			for (Entry<String, Collection<String>> e : tags.asMap().entrySet())
				addChildFigures(m, e.getKey());

			return compositeFig;
		}

		// FIXME field values on null / undefined fields
		private void addChildFigures(IObjectModel m, String attName) {
			IArrayModel<?> array = m.getArray(attName);
			if(array != null) {
				IArrayWidgetExtension ext = getArrayExtension(array, tags.get(attName));
				if(ext != IArrayWidgetExtension.NULL_EXTENSION) {
					IFigure f = ext.createFigure(array);
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
