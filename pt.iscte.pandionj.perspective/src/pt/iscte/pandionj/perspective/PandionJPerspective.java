package pt.iscte.pandionj.perspective;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.internal.WorkbenchWindow;


/**
 *  This class is meant to serve as an example for how various contributions 
 *  are made to a perspective. Note that some of the extension point id's are
 *  referred to as API constants while others are hardcoded and may be subject 
 *  to change. 
 */
public class PandionJPerspective implements IPerspectiveFactory {

	public void createInitialLayout(IPageLayout factory) {
		setupPrefs();
		factory.setEditorAreaVisible(true);
		factory.setFixed(false);
		
//		factory.addStandaloneView("org.eclipse.jdt.ui.PackageExplorer", false, IPageLayout.LEFT, 0.15f, factory.getEditorArea());
		factory.addView("org.eclipse.jdt.ui.PackageExplorer", IPageLayout.LEFT, 0.15f, factory.getEditorArea());
		factory.addView("pt.iscte.pandionj.view", IPageLayout.RIGHT, 0.5f, factory.getEditorArea());
		
//		factory.addStandaloneView(IConsoleConstants.ID_CONSOLE_VIEW, false, IPageLayout.BOTTOM, 0.8f, "pt.iscte.pandionj.view");
		factory.addPlaceholder("test", IPageLayout.BOTTOM, 0.8f, "pt.iscte.pandionj.view");
		
		IFolderLayout bottom = factory.createFolder("topLeft",  IPageLayout.BOTTOM, 0.8f, "pt.iscte.pandionj.view");
		
		bottom.addView(IConsoleConstants.ID_CONSOLE_VIEW);
		bottom.addView("org.eclipse.debug.ui.BreakpointView");
		//factory.addView(IConsoleConstants.ID_CONSOLE_VIEW, IPageLayout.BOTTOM, 0.8f, "pt.iscte.pandionj.view");
		//factory.addView("org.eclipse.debug.ui.BreakpointView", IPageLayout.BOTTOM, 0.8f, "pt.iscte.pandionj.view");
		
		factory.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.JavaProjectWizard");
		factory.addNewWizardShortcut("pt.iscte.perspective.wizards.NewPackageWizard");
		factory.addNewWizardShortcut("pt.iscte.perspective.wizards.NewFileWizard");
	}
	
	
	private void setupPrefs() {
		IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode("org.eclipse.debug.ui");
		prefs.put("org.eclipse.debug.ui.activate_debug_view", "false");
		prefs.put("org.eclipse.debug.ui.switch_perspective_on_suspend", "never");
		IEclipsePreferences prefsUi = InstanceScope.INSTANCE.getNode("org.eclipse.ui.ide");
		prefsUi.put("SWITCH_PERSPECTIVE_ON_PROJECT_CREATION", "never");
	
		IEclipsePreferences prefsJdt = InstanceScope.INSTANCE.getNode("org.eclipse.jdt.ui");
		prefsJdt.put("hoverModifiers", " org.eclipse.jdt.ui.BestMatchHover;!0;org.eclipse.jdt.internal.debug.ui.JavaDebugHover;!0;org.eclipse.jdt.ui.ProblemHover;!0;org.eclipse.jdt.ui.NLSStringHover;Command+Alt;org.eclipse.jdt.ui.JavadocHover;Command+Shift;org.eclipse.jdt.ui.AnnotationHover;!0;org.eclipse.jdt.ui.JavaSourceHover;Shift;");

		char c = '\u0000';
		prefsJdt.put("content_assist_disabled_computers", 
				"org.eclipse.jdt.ui.swtProposalCategory" + c + 
				"org.eclipse.jdt.ui.javaNoTypeProposalCategory" + c + 
				"org.eclipse.jdt.ui.javaTypeProposalCategory" + c + 
				"org.eclipse.jdt.ui.textProposalCategory" + c +  
				"org.eclipse.jdt.ui.javaAllProposalCategory" + c + 
				"org.eclipse.mylyn.java.ui.javaAllProposalCategory" + c +
				"org.eclipse.e4.tools.jdt.templates.e4ProposalCategory" + c + 
				"org.eclipse.pde.api.tools.ui.apitools_proposal_category" + c + 
				"org.eclipse.recommenders.calls.rcp.proposalCategory.templates" + c + 
				"org.eclipse.recommenders.chain.rcp.proposalCategory.chain" + c + 
				"org.eclipse.recommenders.completion.rcp.proposalCategory.intelligent" + c + 
				"org.eclipse.jdt.ui.templateProposalCategory" + c);
		
		prefsJdt.put("closeBraces", "false");
		prefsJdt.put("closeBrackets", "false");
		prefsJdt.put("closeJavaDocs", "false");
		prefsJdt.put("closeStrings", "false");
		prefsJdt.put("smart_opening_brace", "false");
		prefsJdt.put("content_assist_fill_method_arguments", "false");
		prefsJdt.put("editor_folding_enabled", "false");

		IEclipsePreferences prefsWb = InstanceScope.INSTANCE.getNode("org.eclipse.ui.workbench");
		prefsWb.put("org.eclipse.debug.ui.consoleFont","1|Monaco|14.0|0|COCOA|1|Monaco");

//		IHandlerService handlerService = (IHandlerService) PlatformUI.getWorkbench().getService(IHandlerService.class);
//		try {
//			Object executeCommand = handlerService.executeCommand("org.eclipse.ui.ToggleCoolbarAction", null);
//			System.out.println(executeCommand);
//		} catch (Exception e) {
//			e.printStackTrace();
//		} 
		WorkbenchWindow window = (WorkbenchWindow) PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		boolean coolBarVisible = window.getCoolBarVisible();
		if(coolBarVisible)
			window.toggleToolbarVisibility();
	}
}
