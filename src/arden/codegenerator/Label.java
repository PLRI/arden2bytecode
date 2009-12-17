package arden.codegenerator;

/**
 * Repr�sentiert eine Sprungmarke im Bytecode.
 * 
 * Verwendung: <br> 
 * {@code Label marke = new Label(); } <br> 
 * {@code methodWriter.jump(marke); // Springe vorw�rts (zu noch nicht definierter
 * Marke) } <br> 
 * {@code ... // Generiere mehr Code } <br>
 * {@code methodWriter.mark(marke); // Plaziere Sprungmarke }
 * 
 * @author daniel
 * 
 */
public class Label {
	/** Zielposition, auf die das Label zeigt, -1=noch nicht gesetzt */
	int markedPosition = -1;
	/** Stackgr��e an Zielposition, -1=noch unbekannt */
	int stackSize = -1;
	/** Am Anfang true, wird von markForwardOnly() auf false gesetzt um zu signalisieren, dass R�ckspr�nge verboten sind */
	boolean allowJumps = true;
}
