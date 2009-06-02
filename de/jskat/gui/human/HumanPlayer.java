package de.jskat.gui.human;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JOptionPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.jskat.ai.AbstractJSkatPlayer;
import de.jskat.ai.JSkatPlayer;
import de.jskat.data.GameAnnouncement;
import de.jskat.gui.JSkatView;
import de.jskat.gui.action.JSkatActions;
import de.jskat.util.Card;
import de.jskat.util.CardList;

/**
 * Human player
 */
public class HumanPlayer extends AbstractJSkatPlayer
							implements ActionListener {

	private static Log log = LogFactory.getLog(HumanPlayer.class);

    private Idler idler = null;
    
    private JSkatView view = null;
    
	private boolean holdBid;
    private int bidValue;
    private boolean lookIntoSkat;
    private CardList discardSkat;
    private GameAnnouncement gameAnnouncement;
    private Card nextCard;

    /**
     * Sets the view
     * 
     * @param newView
     */
    public void setView(JSkatView newView) {
    	
    	this.view = newView;
    }
    
	/**
	 * @see JSkatPlayer#isAIPlayer()
	 */
	@Override
	public boolean isAIPlayer() {

		return false;
	}

    /**
     * @see JSkatPlayer#announceGame()
     */
	@Override
	public GameAnnouncement announceGame() {
		
		log.debug("Waiting for human game announcing...");
		
		waitForUserInput();
		
		return this.gameAnnouncement;
	}

	/**
	 * @see JSkatPlayer#bidMore(int)
	 */
	@Override
	public int bidMore(int nextBidValue) {
		
		log.debug("Waiting for human next bid value...");
		
		waitForUserInput();
		
		if (this.holdBid) {
			
			this.bidValue = nextBidValue;
		}
		else {
			
			this.bidValue = -1;
		}
		
		return this.bidValue;
	}

	/**
	 * @see JSkatPlayer#discardSkat()
	 */
	@Override
	public CardList discardSkat() {
		
		log.debug("Waiting for human discarding...");
		
		waitForUserInput();
		
		return this.discardSkat;
	}

	/**
	 * @see JSkatPlayer#preparateForNewGame()
	 */
	@Override
	public void preparateForNewGame() {
		
		this.holdBid = false;
	    this.bidValue = 0;
	    this.lookIntoSkat = false;
	    this.discardSkat = null;
	    this.gameAnnouncement = null;
	    this.nextCard = null;
	}

	/**
	 * @see JSkatPlayer#finalizeGame()
	 */
	@Override
	public void finalizeGame() {
		// TODO implement it
	}

	/**
	 * @see JSkatPlayer#holdBid(int)
	 */
	@Override
	public boolean holdBid(int currBidValue) {
		
		log.debug("Waiting for human holding bid...");
		
		waitForUserInput();
		
		return this.holdBid;
	}

	/**
	 * @see JSkatPlayer#lookIntoSkat()
	 */
	@Override
	public boolean lookIntoSkat() {
		
		log.debug("Waiting for human looking into skat...");
		
		waitForUserInput();

		return this.lookIntoSkat;
	}

	/**
	 * @see JSkatPlayer#playCard()
	 */
	@Override
	public Card playCard() {
		
		log.debug("Waiting for human playing next card...");
		
		waitForUserInput();
		
		return this.nextCard;
	}

	/**
	 * Starts waiting for user input
	 */
	public void waitForUserInput() {
		
		this.idler = new Idler();
		this.idler.setMonitor(this);
		
		this.idler.start();
		try {
			this.idler.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 *  @see ActionListener#actionPerformed(ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		
		Object source = e.getSource();
		String command = e.getActionCommand();
		boolean interrupt = true;
		
		if (JSkatActions.PASS_BID.toString().equals(command)) {
			// player passed
			this.holdBid = false;
		}
		else if (JSkatActions.HOLD_BID.toString().equals(command)) {
			// player hold bid
			this.holdBid = true;
		}
		else if (JSkatActions.LOOK_INTO_SKAT.toString().equals(command)) {
			// player wants to look into the skat
			this.lookIntoSkat = true;
		}
		else if (JSkatActions.PLAY_HAND_GAME.toString().equals(command)) {
			// player wants to play a hand game
			this.lookIntoSkat = false;
		}
		else if (JSkatActions.DISCARD_CARDS.toString().equals(command)) {
			
			if (source instanceof CardList) {
				// player discarded cards
				this.discardSkat = (CardList) source;
				
				this.cards.remove(this.discardSkat.get(0));
				this.cards.remove(this.discardSkat.get(1));
			}
			else {
				
				log.error("Wrong source for " + command);
				interrupt = false;
			}
		}
		else if (JSkatActions.ANNOUNCE_GAME.toString().equals(command)) {

			if (source instanceof JButton) {
				log.debug("ONLY JBUTTON");
				interrupt = false;
			}
			else {
				// player did game announcement
				this.gameAnnouncement = (GameAnnouncement) source;
			}
		}
		else if (JSkatActions.PLAY_CARD.toString().equals(command) &&
					source instanceof Card) {
			// player played card
			// check card first
			Card card = (Card) source;
			if (this.getPlayableCards(this.knowledge.getTrickCards()).contains(card)) {
				// card is playable
				this.nextCard = card;
			}
			else {
				
				log.debug("Card " + card + " is not allowed to be played...");
				this.view.showMessage(JOptionPane.ERROR_MESSAGE, "Card " + card + " is not allowed to be played!");
				interrupt = false;
			}
		}
		else {
			
			log.error("Unknown action event occured: " + command + " from " + source);
		}
		
		if (interrupt) {
		
			this.idler.interrupt();
		}
	}

/*-------------------------------------------------------------------
 * Inner class
 *-------------------------------------------------------------------*/
	
    /**
     * Protected class implementing the waiting thread for user input
     */
    protected class Idler extends Thread {

    	/**
    	 * Sets the monitoring object
    	 * 
    	 * @param newMonitor Monitor
    	 */
    	public void setMonitor(Object newMonitor) {
    		
    		this.monitor = newMonitor;
    	}
    	
    	/**
    	 * Stops the waiting
    	 */
    	public void stopWaiting() {
    		
    		this.doWait = false;
    	}
    	
    	/**
    	 * @see Thread#run()
    	 */
    	@Override
		public void run() {
    		
    		synchronized(this.monitor) {
    			
    			while(this.doWait) {
	    			try {
						this.monitor.wait();
					} catch (InterruptedException e) {
						this.doWait = false;
					}
    			}
    		}
    	}
    	
    	private boolean doWait = true;
    	private Object monitor = null;
    }

	/* (non-Javadoc)
	 * @see de.jskat.ai.AbstractJSkatPlayer#startGame()
	 */
	@Override
	protected void startGame() {
		// CHECK Auto-generated method stub
		
	}
}
