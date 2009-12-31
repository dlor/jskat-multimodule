/*

@ShortLicense@

Authors: @JS@
         @MJL@

Released: @ReleaseDate@

 */

package de.jskat.gui.action.iss;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import de.jskat.control.JSkatMaster;
import de.jskat.gui.action.AbstractJSkatAction;

/**
 * Implements the action for leaving a skat table on ISS
 */
public class ChangeTableSeatsAction extends AbstractJSkatAction {

	private static final long serialVersionUID = 1L;

	/**
	 * @see AbstractJSkatAction#AbstractJSkatAction(JSkatMaster)
	 */
	public ChangeTableSeatsAction(JSkatMaster controller) {
		
		super(controller);
		
		putValue(Action.NAME, "Change table seats (3 <-> 4)");
	}
	
	/**
	 * @see AbstractAction#actionPerformed(ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {

		//this.jskat.getISSController().leaveTable(tableName, playerName);
	}
}
