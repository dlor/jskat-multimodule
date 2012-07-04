/**
 * JSkat - A skat program written in Java
 * by Jan Schäfer and Markus J. Luzius
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
package org.jskat.control.iss;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jskat.data.GameAnnouncement;
import org.jskat.data.GameAnnouncement.GameAnnouncementFactory;
import org.jskat.data.SkatGameData;
import org.jskat.data.Trick;
import org.jskat.data.iss.ChatMessage;
import org.jskat.data.iss.GameStartInformation;
import org.jskat.data.iss.MoveInformation;
import org.jskat.data.iss.MovePlayer;
import org.jskat.data.iss.MoveType;
import org.jskat.data.iss.PlayerStatus;
import org.jskat.data.iss.TablePanelStatus;
import org.jskat.util.Card;
import org.jskat.util.CardList;
import org.jskat.util.GameType;
import org.jskat.util.Player;
import org.jskat.util.rule.SkatRule;
import org.jskat.util.rule.SkatRuleFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parses ISS messages
 */
public class MessageParser {

	private static Logger log = LoggerFactory.getLogger(MessageParser.class);

	/**
	 * table .1 bar state --> was cut away before <br>
	 * --> params: <br>
	 * 3 <br>
	 * bar xskat xskat:2 . <br>
	 * bar . 0 0 0 0 0 0 1 0 <br>
	 * xskat $ 0 0 0 0 0 0 1 1 <br>
	 * xskat:2 $ 0 0 0 0 0 0 1 1 <br>
	 * . . 0 0 0 0 0 0 0 0 false <br>
	 */
	static TablePanelStatus getTableStatus(final String loginName, final List<String> params) {

		TablePanelStatus status = new TablePanelStatus();

		status.setLoginName(loginName);

		status.setMaxPlayers(Integer.parseInt(params.get(0)));

		// get player status
		for (int i = 0; i < status.getMaxPlayers(); i++) {
			// parse only non empty seats
			if (!(".".equals(params.get(i * 10 + 5)))) { //$NON-NLS-1$
				// there is player information
				PlayerStatus playerStatus = parsePlayerStatus(params.subList(i * 10 + 5, i * 10 + 16));
				// has player left already
				if (".".equals(params.get(i + 1))) { //$NON-NLS-1$
					playerStatus.setPlayerLeft(true);
				}
				status.addPlayer(playerStatus.getName(), playerStatus);
			}
		}

		return status;
	}

	/**
	 * table .1 bar state <br>
	 * 3 <br>
	 * bar xskat xskat:2 . <br>
	 * bar . 0 0 0 0 0 0 1 0 <br>
	 * --> params:<br>
	 * xskat $ 0 0 0 0 0 0 1 1 <br>
	 * xskat:2 $ 0 0 0 0 0 0 1 1 <br>
	 * . . 0 0 0 0 0 0 0 0 false <br>
	 */
	private static PlayerStatus parsePlayerStatus(final List<String> params) {

		PlayerStatus status = new PlayerStatus();

		status.setName(params.get(0));
		status.setIP(params.get(1));
		status.setGamesPlayed(Integer.parseInt(params.get(2)));
		status.setGamesWon(Integer.parseInt(params.get(3)));
		status.setLastGameResult(Integer.parseInt(params.get(4)));
		status.setTotalPoints(Integer.parseInt(params.get(5)));
		status.setSwitch34(Integer.parseInt(params.get(6)) == 1);
		// ignore next information, unknown purpose
		status.setTalkEnabled(Integer.parseInt(params.get(8)) == 1);
		status.setReadyToPlay(Integer.parseInt(params.get(9)) == 1);

		return status;
	}

	static GameStartInformation getGameStartStatus(final String loginName, final List<String> params) {

		log.debug("game start parameter: " + params); //$NON-NLS-1$

		GameStartInformation status = new GameStartInformation();

		status.setLoginName(loginName);

		status.setGameNo(Integer.parseInt(params.get(0)));
		status.putPlayerName(Player.FOREHAND, params.get(1));
		status.putPlayerTime(Player.FOREHAND, Double.valueOf(params.get(2)));
		status.putPlayerName(Player.MIDDLEHAND, params.get(3));
		status.putPlayerTime(Player.MIDDLEHAND, Double.valueOf(params.get(4)));
		status.putPlayerName(Player.REARHAND, params.get(5));
		status.putPlayerTime(Player.REARHAND, Double.valueOf(params.get(6)));

		return status;
	}

	static MoveInformation getMoveInformation(final List<String> params) {

		MoveInformation info = new MoveInformation();

		getMovePlayer(params.get(0), info);

		// FIXME Unhandled moves
		String move = params.get(1);
		log.debug("Move: " + move); //$NON-NLS-1$
		if ("y".equals(move)) { //$NON-NLS-1$
			// holding bid move
			info.setType(MoveType.HOLD_BID);
		} else if ("p".equals(move)) { //$NON-NLS-1$
			// pass move
			info.setType(MoveType.PASS);
		} else if ("s".equals(move)) { //$NON-NLS-1$
			// skat request move
			info.setType(MoveType.SKAT_REQUEST);
		} else if ("??.??".equals(move)) { //$NON-NLS-1$
			// hidden skat given to a player
			info.setType(MoveType.PICK_UP_SKAT);
		} else if (move.startsWith("TI.")) { //$NON-NLS-1$
			// time out for player
			info.setType(MoveType.TIME_OUT);
			info.setTimeOutPlayer(parseTimeOut(move));
		} else if (move.equals("RE")) { //$NON-NLS-1$
			// resigning of player
			info.setType(MoveType.RESIGN);
		} else if (move.startsWith("SC")) { //$NON-NLS-1$
			// declarer shows cards
			info.setType(MoveType.SHOW_CARDS);
			if (move.length() > 2) {
				// declarer cards follow, SC could also stand allone
				info.setOuvertCards(parseSkatCards(move.substring(move.indexOf(".") + 1))); //$NON-NLS-1$
			}
		} else if (move.startsWith("LE.")) { //$NON-NLS-1$
			// one player left the table during the game
			info.setType(MoveType.LEAVE_TABLE);
			info.setLeavingPlayer(parseLeaveTable(move));
		} else {
			// extensive parsing needed

			// test card move
			Card card = Card.getCardFromString(move);
			if (card != null) {
				// card play move
				info.setType(MoveType.CARD_PLAY);
				info.setCard(card);
			} else {
				// card parsing failed

				// test bidding
				int bid = -1;
				try {

					bid = Integer.parseInt(move);

				} catch (NumberFormatException e) {

					bid = -1;
				}
				if (bid > -1) {
					// bidding
					info.setType(MoveType.BID);
					info.setBidValue(Integer.parseInt(move));
				} else {

					if (move.length() == 95) {
						// card dealing
						info.setType(MoveType.DEAL);
						info.setDealCards(parseCardDeal(move));
					} else if (move.length() == 5) {
						// open skat given to a player
						info.setType(MoveType.PICK_UP_SKAT);
						info.setSkat(parseSkatCards(move));
					} else {
						// game announcement
						info.setType(MoveType.GAME_ANNOUNCEMENT);
						parseGameAnnoucement(info, move);
					}
				}
			}
		}

		return info;
	}

	static void parsePlayerTimes(final List<String> params, final MoveInformation info) {
		// parse player times
		info.putPlayerTime(Player.FOREHAND, new Double(params.get(params.size() - 3)));
		info.putPlayerTime(Player.MIDDLEHAND, new Double(params.get(params.size() - 2)));
		info.putPlayerTime(Player.REARHAND, new Double(params.get(params.size() - 1)));
	}

	private static CardList parseSkatCards(final String move) {

		StringTokenizer token = new StringTokenizer(move, "."); //$NON-NLS-1$
		CardList result = new CardList();

		while (token.hasMoreTokens()) {
			result.add(Card.getCardFromString(token.nextToken()));
		}

		return result;
	}

	private static void getMovePlayer(final String movePlayer, final MoveInformation info) {

		log.debug("Move player: " + movePlayer); //$NON-NLS-1$
		if ("w".equals(movePlayer)) { //$NON-NLS-1$
			// world move
			info.setMovePlayer(MovePlayer.WORLD);
		} else if ("0".equals(movePlayer)) { //$NON-NLS-1$
			// fore hand move
			info.setMovePlayer(MovePlayer.FOREHAND);
		} else if ("1".equals(movePlayer)) { //$NON-NLS-1$
			// middle hand move
			info.setMovePlayer(MovePlayer.MIDDLEHAND);
		} else if ("2".equals(movePlayer)) { //$NON-NLS-1$
			// hind hand move
			info.setMovePlayer(MovePlayer.REARHAND);
		}
	}

	/**
	 * <game-type> :: (G | C | S | H | D | N) (type Grand .. Null) [O] (ouvert)
	 * [H] (hand, not given if O + trump game) [S] (schneider announced, only in
	 * H games, not if O or Z) [Z] (schwarz announced, only in H games)
	 */
	private static GameAnnouncement parseGameAnnoucement(final MoveInformation info, final String move) {

		StringTokenizer annToken = new StringTokenizer(move, "."); //$NON-NLS-1$
		String gameType = annToken.nextToken();

		GameAnnouncementFactory factory = GameAnnouncement.getFactory();

		// at first the game type
		if (gameType.startsWith("G")) { //$NON-NLS-1$

			factory.setGameType(GameType.GRAND);

		} else if (gameType.startsWith("C")) { //$NON-NLS-1$

			factory.setGameType(GameType.CLUBS);

		} else if (gameType.startsWith("S")) { //$NON-NLS-1$

			factory.setGameType(GameType.SPADES);

		} else if (gameType.startsWith("H")) { //$NON-NLS-1$

			factory.setGameType(GameType.HEARTS);

		} else if (gameType.startsWith("D")) { //$NON-NLS-1$

			factory.setGameType(GameType.DIAMONDS);

		} else if (gameType.startsWith("N")) { //$NON-NLS-1$

			factory.setGameType(GameType.NULL);
		}

		boolean handGame = false;
		boolean ouvertGame = false;
		// parse other game modifiers
		for (int i = 1; i < gameType.length(); i++) {

			char mod = gameType.charAt(i);

			if (mod == 'O') {

				factory.setOuvert(Boolean.TRUE);
				ouvertGame = true;
			} else if (mod == 'H') {

				factory.setHand(Boolean.TRUE);
				handGame = true;
			} else if (mod == 'S') {

				factory.setSchneider(Boolean.TRUE);
			} else if (mod == 'Z') {

				factory.setSchwarz(Boolean.TRUE);
			}
		}

		if (annToken.hasMoreTokens()) {

			if (ouvertGame) {

				CardList ouvertCards = new CardList();

				while (annToken.hasMoreTokens()) {
					// player has shown the cards
					// ouvert game
					ouvertCards.add(Card.getCardFromString(annToken.nextToken()));
				}

				info.setOuvertCards(ouvertCards);

			} else if (!handGame) {

				Card discardCard0 = Card.getCardFromString(annToken.nextToken());
				Card discardCard1 = Card.getCardFromString(annToken.nextToken());

				CardList discardedCards = new CardList();
				discardedCards.add(discardCard0);
				discardedCards.add(discardCard1);

				factory.setDiscardedCards(discardedCards);
			}
		}

		GameAnnouncement ann = factory.getAnnouncement();
		info.setGameAnnouncement(ann);
		return ann;
	}

	private static List<CardList> parseCardDeal(final String move) {

		if (move.contains("|") && move.contains("??")) { //$NON-NLS-1$
			return parseCardDealFromISSMessage(move);
		}

		return parseCardDealFromSummary(move);
	}

	/**
	 * During playing on ISS the deal is send in the following format<br />
	 * ??.??.??.??.??.??.??.??.??.??|D9.S9.ST.S8.C9.DT.DQ.CJ.SA.HA|??.??.??.??.?
	 * ?.??.??.??.??.??|??.??<br />
	 * ?? - hidden card<br />
	 * fore hand cards|middle hand cards|rear hand cards|skat
	 */
	private static List<CardList> parseCardDealFromISSMessage(final String move) {

		StringTokenizer handTokens = new StringTokenizer(move, "|"); //$NON-NLS-1$
		List<CardList> result = new ArrayList<CardList>();

		while (handTokens.hasMoreTokens()) {
			result.add(parseHand(handTokens.nextToken()));
		}

		return result;
	}

	/**
	 * In game summary the deal is send in a different format<br />
	 * CQ.H7.C8.C9.CT.SQ.DK.H8.H9.SA.S9.HK.HQ.DJ.CJ.S8.DT.HT.ST.D7.CK.S7.C7.DQ.
	 * SJ.HA.CA.DA.D8.D9.SK.HJ<br />
	 * no hidden cards<br />
	 * first 10 cards fore hand<br />
	 * next 10 cards middle hand<br />
	 * next 10 cards rear hand<br />
	 * last 2 cards skat
	 * 
	 * @param move
	 */
	private static List<CardList> parseCardDealFromSummary(final String move) {
		List<CardList> result = new ArrayList<CardList>();

		// fore hand
		result.add(parseHand(move.substring(0, 29)));
		// middle hand
		result.add(parseHand(move.substring(30, 59)));
		// rear hand
		result.add(parseHand(move.substring(60, 89)));
		// skat
		result.add(parseHand(move.substring(90)));

		return result;
	}

	private static CardList parseHand(final String hand) {

		StringTokenizer cardTokens = new StringTokenizer(hand, "."); //$NON-NLS-1$
		CardList result = new CardList();

		while (cardTokens.hasMoreTokens()) {
			result.add(Card.getCardFromString(cardTokens.nextToken()));
		}

		return result;
	}

	/**
	 * TI.0
	 */
	private static Player parseTimeOut(final String timeOut) {

		return extractPlayer(timeOut);
	}

	/**
	 * LE.0
	 */
	private static Player parseLeaveTable(final String leaveTable) {

		return extractPlayer(leaveTable);
	}

	private static Player extractPlayer(final String playerAtLastCharacter) {
		Player result = null;

		switch (playerAtLastCharacter.charAt(3)) {
		case '0':
			result = Player.FOREHAND;
			break;
		case '1':
			result = Player.MIDDLEHAND;
			break;
		case '2':
			result = Player.REARHAND;
			break;
		}

		return result;
	}

	static SkatGameData parseGameSummary(final String gameSummary) {

		SkatGameData result = new SkatGameData();

		Pattern summaryPartPattern = Pattern.compile("(\\w+)\\[(.*?)\\]"); //$NON-NLS-1$
		Matcher summaryPartMatcher = summaryPartPattern.matcher(gameSummary);

		while (summaryPartMatcher.find()) {

			log.debug(summaryPartMatcher.group());

			String summaryPartMarker = summaryPartMatcher.group(1);
			String summeryPart = summaryPartMatcher.group(2);

			parseSummaryPart(result, summaryPartMarker, summeryPart);
		}

		return result;
	}

	private static void parseSummaryPart(final SkatGameData result, final String summaryPartMarker,
			final String summaryPart) {

		if ("P0".equals(summaryPartMarker)) { //$NON-NLS-1$

			result.setPlayerName(Player.FOREHAND, summaryPart);

		} else if ("P1".equals(summaryPartMarker)) { //$NON-NLS-1$

			result.setPlayerName(Player.MIDDLEHAND, summaryPart);

		} else if ("P2".equals(summaryPartMarker)) { //$NON-NLS-1$

			result.setPlayerName(Player.REARHAND, summaryPart);

		} else if ("MV".equals(summaryPartMarker)) { //$NON-NLS-1$

			parseMoves(result, summaryPart);

		} else if ("R".equals(summaryPartMarker)) { //$NON-NLS-1$

			parseGameResult(result, summaryPart);
		}
	}

	private static void parseMoves(final SkatGameData result, final String summaryPart) {

		// FIXME (jansch 12.02.2012) parse moves correctly
		GameAnnouncementFactory factory = GameAnnouncement.getFactory();
		factory.setGameType(GameType.PASSED_IN);
		result.setAnnouncement(factory.getAnnouncement());

		StringTokenizer token = new StringTokenizer(summaryPart);

		while (token.hasMoreTokens()) {

			List<String> moveToken = new ArrayList<String>();
			moveToken.add(token.nextToken());
			moveToken.add(token.nextToken());

			MoveInformation moveInfo = getMoveInformation(moveToken);

			switch (moveInfo.getType()) {
			case DEAL:
				result.setDealtSkatCards(moveInfo.getSkat().get(0), moveInfo.getSkat().get(1));
				break;
			case BID:
				result.setBidValue(moveInfo.getBidValue());
				result.setPlayerBid(moveInfo.getPlayer(), moveInfo.getBidValue());
				break;
			case HOLD_BID:
				result.setPlayerBid(moveInfo.getPlayer(), result.getBidValue());
				break;
			case PASS:
				result.setPlayerPass(moveInfo.getPlayer(), true);
				break;
			case GAME_ANNOUNCEMENT:
				result.setAnnouncement(moveInfo.getGameAnnouncement());
				break;
			case CARD_PLAY:
				if (result.getTricks().size() == 0) {
					// no tricks played so far
					result.addTrick(new Trick(0, Player.FOREHAND));
				} else if (result.getCurrentTrick().getThirdCard() != null) {
					// last card of trick is played
					// set trick winner
					result.getCurrentTrick().setTrickWinner(moveInfo.getPlayer());
					// create next trick
					result.addTrick(new Trick(result.getTricks().size(), moveInfo.getPlayer()));
				}
				result.setTrickCard(moveInfo.getPlayer(), moveInfo.getCard());

				if (result.getTricks().size() == 10 && result.getCurrentTrick().getThirdCard() != null) {
					// set the trick winner of the last trick
					SkatRule skatRules = SkatRuleFactory.getSkatRules(result.getGameType());
					result.setTrickWinner(9,
							skatRules.calculateTrickWinner(result.getGameType(), result.getCurrentTrick()));
				}
				break;
			}
		}
	}

	private static void parseGameResult(final SkatGameData result, final String summaryPart) {

		StringTokenizer token = new StringTokenizer(summaryPart);

		while (token.hasMoreTokens()) {

			parseResultToken(result, token.nextToken());
		}
	}

	private static void parseResultToken(final SkatGameData gameData, final String token) {

		// from ISS source code
		// return "d:"+declarer + (penalty ? " penalty" : (declValue > 0 ? "
		// win" : " loss"))
		// + " v:" + declValue
		// + " m:" + matadors + (overbid ? " overbid" : " bidok")
		// + " p:" + declCardPoints + " t:" + declTricks
		// + " s:" + (schneider ? '1' : '0') + " z:" + (schwarz ? '1' :
		// '0')
		// + " p0:" + penalty0 + " p1:" + penalty1 + " p2:" + penalty2
		// + " l:" + this.left + " to:" + this.timeout + " r:" + (resigned
		// ? '1' : '0');

		if (token.startsWith("d:")) { //$NON-NLS-1$

			parseDeclarerToken(gameData, token);

		} else if ("penalty".equals(token)) { //$NON-NLS-1$

			// FIXME (jan 07.12.2010) handle this token

		} else if ("loss".equals(token)) { //$NON-NLS-1$

			gameData.getResult().setWon(false);

		} else if ("win".equals(token)) { //$NON-NLS-1$

			gameData.getResult().setWon(true);

		} else if (token.startsWith("v:")) { //$NON-NLS-1$

			gameData.getResult().setGameValue(Integer.parseInt(token.substring(2)));

		} else if (token.startsWith("p:")) { //$NON-NLS-1$

			int declarerPoints = Integer.parseInt(token.substring(2));
			gameData.setDeclarerScore(declarerPoints);
			gameData.getResult().setFinalDeclarerPoints(declarerPoints);
			gameData.getResult().setFinalOpponentPoints(120 - declarerPoints);

		} else if ("overbid".equals(token)) { //$NON-NLS-1$

			gameData.getResult().setOverBidded(true);

		} else if ("s:1".equals(token)) { //$NON-NLS-1$

			gameData.getResult().setSchneider(true);

		} else if ("z:1".equals(token)) { //$NON-NLS-1$

			gameData.getResult().setSchwarz(true);
		}
	}

	private static void parseDeclarerToken(final SkatGameData result, final String token) {

		if ("d:0".equals(token)) { //$NON-NLS-1$
			result.setDeclarer(Player.FOREHAND);
		} else if ("d:1".equals(token)) { //$NON-NLS-1$
			result.setDeclarer(Player.MIDDLEHAND);
		} else if ("d:2".equals(token)) { //$NON-NLS-1$
			result.setDeclarer(Player.REARHAND);
		}
	}

	/**
	 * table .5 foo tell foo asdf jklö<br>
	 * <br>
	 * table .5 foo tell --> was cut before<br>
	 * params: foo -> first is talker<br>
	 * remainings is the chat message<br>
	 * asdf jklö
	 */
	static ChatMessage getTableChatMessage(final String tableName, final List<String> detailParams) {

		StringBuffer text = new StringBuffer();

		text.append(detailParams.get(0)).append(": "); //$NON-NLS-1$
		for (int i = 1; i < detailParams.size(); i++) {
			text.append(detailParams.get(i)).append(' ');
		}

		return new ChatMessage(tableName, text.toString());
	}
}
