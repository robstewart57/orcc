
package net.sf.orcc.analysis;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.Model;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;

import com.google.inject.Injector;

import net.sf.orcc.backends.fiacre.InstancePrinter;
import net.sf.orcc.cal.CalStandaloneSetup;
import net.sf.orcc.cal.cal.AstActor;
import net.sf.orcc.cal.cal.AstEntity;
import net.sf.orcc.df.Actor;
import net.sf.orcc.frontend.ActorTransformer;
import net.sf.orcc.util.FilesManager;


/**
 * 
 * @author Rob Stewart
 *
 */
public class PetriNetHandler extends AbstractHandler {
	
	@Override
	public boolean isEnabled() { return true; }
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		/* check for the existance of the fiacre and tina executables */
		// TODO
		
		// get workbench window
		IEditorPart editorPart = HandlerUtil.getActiveEditor(event);
		System.out.println("Petri net handler run");
		if (!(editorPart instanceof ITextEditor)) return null;
	    ITextEditor ite = (ITextEditor) editorPart;
	    IDocument doc = ite.getDocumentProvider().getDocument(ite.getEditorInput());
		
		new org.eclipse.emf.mwe.utils.StandaloneSetup().setPlatformUri("../");
	    Injector injector = new CalStandaloneSetup().createInjectorAndDoEMFRegistration();
	    //LLVM_IRPackage.eINSTANCE.class();
	    XtextResourceSet resourceSet = injector.getInstance(XtextResourceSet.class);
	    resourceSet.addLoadOption(XtextResource.OPTION_RESOLVE_ALL, Boolean.TRUE);
	    Resource resource = resourceSet.createResource(URI.createURI("dummy:/example.cal"));
	    InputStream in = new ByteArrayInputStream(doc.get().getBytes(StandardCharsets.UTF_8));
	    try {
			resource.load(in, resourceSet.getLoadOptions());
		} catch (IOException e) {
			e.printStackTrace();
		}
	    AstEntity entity = (AstEntity) resource.getContents().get(0);
	    ActorTransformer trans = new ActorTransformer();
	    Actor actor = (Actor) trans.doSwitch(entity.getActor());
	    System.out.println(actor.getName());

	    
	    /* print IR to Fiacre */
	    String srcPath = "/tmp/fiacre_out";
	    InstancePrinter instancePrinter = new InstancePrinter();
		instancePrinter.setActor(actor);
		FilesManager.writeFile(instancePrinter.getFileContent(), srcPath, actor.getSimpleName() + ".fcr");
	
		/* run TINA out the generated file */
	    
	    return null;
	}

}
