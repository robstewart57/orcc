
package net.sf.orcc.analysis;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.ITextEditor;

import net.sf.orcc.backends.cal.InstancePrinter;
import net.sf.orcc.df.Actor;
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
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);

		IEditorPart editorPart = HandlerUtil.getActiveEditor(event);
		
		System.out.println("Petri net handler run");
		
		if (!(editorPart instanceof ITextEditor)) return null;
	    ITextEditor ite = (ITextEditor) editorPart;
	    IDocument doc = ite.getDocumentProvider().getDocument(ite.getEditorInput());
	    System.out.println(doc.get());
		
	    /* parse actor into an AST */
	    
	    /* map AST to Orcc IR */
	    Actor actor = null;
	    
	    /* print IR to Fiacre */
	    String srcPath = "/tmp/fiacre_out";
	    InstancePrinter instancePrinter = new InstancePrinter();
		instancePrinter.setActor(actor);
		FilesManager.writeFile(instancePrinter.getFileContent(), srcPath, actor.getSimpleName() + ".fcr");
	
		/* run TINA out the generated file */
	    
	    return null;
	}

}
