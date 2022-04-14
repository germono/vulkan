package oliv.lib.vulkan.apiGraphique;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseWheelListener;

import oliv.lib.vulkan.ProcedureVulkan;
import oliv.lib.vulkan.v8.bufferUniforme.GestionBufferUniforme;

public class InteractionHumaine implements MouseListener, MouseMoveListener, MouseWheelListener ,KeyListener{
	GestionBufferUniforme modele;
	ProcedureVulkan pv;
	public InteractionHumaine(GestionBufferUniforme modele,ProcedureVulkan pv) {
		this.modele=modele;
		this.pv=pv;
	}

	@Override
	public void mouseScrolled(MouseEvent e) {
		modele.onScroll(e);
	}

	@Override
	public void mouseMove(MouseEvent e) {
		modele.onMouseMove(e);
	}

	@Override
	public void mouseDoubleClick(MouseEvent e) {
		switch (e.button) {
		case 1->modele.transReset();
		case 3->modele.rotReset();
		case 2->modele.transReset();
		}
	}

	@Override
	public void mouseDown(MouseEvent e) {
		switch (e.button) {
		case 1->modele.dragBegin();
		case 3->modele.viewBegin();
		case 2->modele.dragBegin();
		}
	}

	@Override
	public void mouseUp(MouseEvent e) {
		switch (e.button) {
		case 1->modele.dragEnd();
		case 3->modele.viewEnd();
		case 2->modele.dragEnd();
		}
	}
	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.character) {
		case SWT.TAB->modele.basculeProjetction();
		case '\''->pv.modeShader();
		case '('->pv.modeWire();
		case '-'->pv.modePoint();
		case '&'->modele.xpress(true);
		case 'é'->modele.ypress(true);
		case '"'->modele.zpress(true);
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		modele.xpress(false);
		modele.ypress(false);
		modele.zpress(false);
	}

}