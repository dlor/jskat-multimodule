/**
 * JSkat - A skat program written in Java
 * by Jan SchÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¤fer and Markus J. Luzius
 *
 * Version 0.11.0-SNAPSHOT
 * Copyright (C) 2012-03-13
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jskat.ai.newalgorithm;

import org.apache.log4j.Logger;
import org.jskat.ai.algorithmic.AlgorithmicOpponentPlayer;
import org.jskat.ai.algorithmic.AlgorithmicRamschPlayer;
import org.jskat.data.GameAnnouncement;
import org.jskat.data.GameAnnouncement.GameAnnouncementFactory;
import org.jskat.player.AbstractJSkatPlayer;
import org.jskat.player.PlayerKnowledge;
import org.jskat.util.Card;
import org.jskat.util.CardList;
import org.jskat.util.GameType;

/**
 * @author Markus J. Luzius <br>
 *         created: 13.05.2011 17:33:05
 * 
 */
public class AlgorithmAI extends AbstractJSkatPlayer {
	private static final Logger log = Logger.getLogger(AlgorithmAI.class);

	private AbstractAlgorithmAI aiPlayer = null;
	BidEvaluator bidEvaluator = null;
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jskat.ai.IJSkatPlayer#preparateForNewGame()
	 */
	@Override
	public void preparateForNewGame() {
		log.debug("New game preparation for player <" + playerName + ">");
		aiPlayer = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jskat.ai.IJSkatPlayer#finalizeGame()
	 */
	@Override
	public void finalizeGame() {
		bidEvaluator = null; // not necessry any more
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jskat.ai.IJSkatPlayer#bidMore(int)
	 */
	@Override
	public int bidMore(final int nextBidValue) {
		if(bidEvaluator == null)
			bidEvaluator = new BidEvaluator(knowledge.getOwnCards(), knowledge.getPlayerPosition());
		if(bidEvaluator.getMaxBid() >= nextBidValue) 
			return nextBidValue;
		return -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jskat.ai.IJSkatPlayer#holdBid(int)
	 */
	@Override
	public boolean holdBid(final int currBidValue) {
		if(bidEvaluator == null)
			bidEvaluator = new BidEvaluator(knowledge.getOwnCards(), knowledge.getPlayerPosition());
		return (bidEvaluator.getMaxBid() >= currBidValue);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jskat.ai.IJSkatPlayer#pickUpSkat()
	 */
	@Override
	public boolean pickUpSkat() {
		if(bidEvaluator == null)
			bidEvaluator = new BidEvaluator(knowledge.getOwnCards(), knowledge.getPlayerPosition());
		return !bidEvaluator.canPlayHandGame();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jskat.ai.IJSkatPlayer#discardSkat()
	 */
	@Override
	public CardList getCardsToDiscard() {
		// Wenn Ramschspiel
		if(knowledge.getGameType() == GameType.RAMSCH) {
//			aiPlayer = new AlgorithmRamsch(this, GameType.RAMSCH);
		}
		else {
			// Ermitteln was gespielt werden soll
			// normalerweise muss der BidEvaluator gesetzt sein, sonst kann er eigentlich garnicht hier reinlaufen
			if(bidEvaluator == null)
				bidEvaluator = new BidEvaluator(knowledge.getOwnCards(), knowledge.getPlayerPosition());
			else
				bidEvaluator.eval(knowledge.getOwnCards());
			
			// Wenn Null-Spiel
			if(bidEvaluator.getSuggestedGameType() == GameType.NULL) {
//				aiPlayer = new AlgorithmNull(this, bidEvaluator.getSuggestedGameType());
				System.out.println(this.playerName+ " ist AlgorithmNull-Spieler / getCardsToDiscard");
			}
			// Wenn Grand
			else if(bidEvaluator.getSuggestedGameType() == GameType.GRAND) {
				aiPlayer = new AlgorithmGrand(this, bidEvaluator.getSuggestedGameType());
				System.out.println(this.playerName+ " ist AlgorithmGrand-Spieler / getCardsToDiscard");
			}
			// Wenn Farb-Spiel
			else if(bidEvaluator.getSuggestedGameType() == GameType.CLUBS
					|| bidEvaluator.getSuggestedGameType() == GameType.SPADES
					|| bidEvaluator.getSuggestedGameType() == GameType.HEARTS
					|| bidEvaluator.getSuggestedGameType() == GameType.DIAMONDS) {
				aiPlayer = new AlgorithmSuit(this, bidEvaluator.getSuggestedGameType());
				System.out.println(this.playerName+ " ist AlgorithmSuit-Spieler / getCardsToDiscard");
			}
		}
		
		return aiPlayer.discardSkat(bidEvaluator);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jskat.ai.IJSkatPlayer#announceGame()
	 */
	@Override
	public GameAnnouncement announceGame() {
		if (bidEvaluator == null)
			bidEvaluator = new BidEvaluator(knowledge.getOwnCards(), knowledge.getPlayerPosition());
		else
			bidEvaluator.eval(knowledge.getOwnCards());
		
		// Wenn Null-Spiel
		if(bidEvaluator.getSuggestedGameType() == GameType.NULL) {
//			aiPlayer = new AlgorithmNull(this, bidEvaluator.getSuggestedGameType());
			System.out.println(this.playerName+ " ist AlgorithmNull-Spieler / announceGame");
		}
		// Wenn Grand
		else if(bidEvaluator.getSuggestedGameType() == GameType.GRAND) {
			aiPlayer = new AlgorithmGrand(this, bidEvaluator.getSuggestedGameType());
			System.out.println(this.playerName+ " ist AlgorithmGrand-Spieler / announceGame");
		}
		// Wenn Farb-Spiel
		else if(bidEvaluator.getSuggestedGameType() == GameType.CLUBS
				|| bidEvaluator.getSuggestedGameType() == GameType.SPADES
				|| bidEvaluator.getSuggestedGameType() == GameType.HEARTS
				|| bidEvaluator.getSuggestedGameType() == GameType.DIAMONDS) {
			aiPlayer = new AlgorithmSuit(this, bidEvaluator.getSuggestedGameType());
			System.out.println(this.playerName+ " ist AlgorithmSuit-Spieler / announceGame");
		}
		
		GameAnnouncementFactory factory = GameAnnouncement.getFactory();
		factory.setGameType(bidEvaluator.getSuggestedGameType());
		return factory.getAnnouncement();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jskat.ai.IJSkatPlayer#playCard()
	 */
	@Override
	public Card playCard() {
		// Wenn Ramsch-Spiel abgeloest wurde
//		if(aiPlayer instanceof AlgorithmRamsch
//				&& knowledge.getGameType() != GameType.RAMSCH) {
//			aiPlayer = new AlgorithmOpponentGrand(this, GameType.GRAND);
//			System.out.println(this.playerName+ " ist AlgorithmOpponentGrand-Spieler / playCard");
//		}
		
		Card c = aiPlayer.playCard();
		if(c != null) {
			return c;
		}
		
		log.warn("no card returned from AI player!");
		if(knowledge.getTrickCards().size() < 1) {
			return knowledge.getOwnCards().get(0);
		}
		for(Card c2 : knowledge.getOwnCards()) {
			if(c2.isAllowed(knowledge.getGameType(), knowledge.getTrickCards().get(0), knowledge.getOwnCards())) {
				return c2;
			}
		}
		log.warn("no valid card found!");
		return knowledge.getOwnCards().get(0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jskat.ai.IJSkatPlayer#isAIPlayer()
	 */
	@Override
	public final boolean isAIPlayer() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jskat.ai.AbstractJSkatPlayer#startGame()
	 */
	@Override
	public void startGame() {
		if(aiPlayer == null) {
			// Wenn RAMSCH-Spiel
			if(knowledge.getGameType() == GameType.RAMSCH) {
//				aiPlayer = new AlgorithmRamsch(this, knowledge.getGameType());
				System.out.println(this.playerName+ " ist AlgorithmRamsch-Spieler / startGame");
			}
			// Wenn Null-Spiel
			if(knowledge.getGameType() == GameType.NULL) {
				aiPlayer = new AlgorithmOpponentNull(this, knowledge.getGameType());
				System.out.println(this.playerName+ " ist AlgorithmOpponentNull-Spieler / startGame");
			}
			// Wenn Grand
			else if(knowledge.getGameType() == GameType.GRAND) {
				aiPlayer = new AlgorithmOpponentGrand(this, knowledge.getGameType());
				System.out.println(this.playerName+ " ist AlgorithmOpponentGrand-Spieler / startGame");
			}
			// Wenn Farb-Spiel
			else if(knowledge.getGameType() == GameType.CLUBS
					|| knowledge.getGameType() == GameType.SPADES
					|| knowledge.getGameType() == GameType.HEARTS
					|| knowledge.getGameType() == GameType.DIAMONDS) {
				aiPlayer = new AlgorithmOpponentSuit(this, knowledge.getGameType());
				System.out.println(this.playerName+ " ist AlgorithmOpponentSuit-Spieler / startGame");
			}
		}
	}

	protected PlayerKnowledge getKnowledge() {
		return knowledge;
	}

}
