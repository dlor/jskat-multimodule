/*

@ShortLicense@

Authors: @JS@
         @MJL@

Released: @ReleaseDate@

 */

package de.jskat.control.iss;

import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.jskat.ai.IJSkatPlayer;
import de.jskat.control.JSkatMaster;
import de.jskat.data.GameAnnouncement;
import de.jskat.data.JSkatApplicationData;
import de.jskat.data.SkatGameData;
import de.jskat.data.SkatGameData.GameState;
import de.jskat.data.iss.ISSChatMessage;
import de.jskat.data.iss.ISSGameStartInformation;
import de.jskat.data.iss.ISSLoginCredentials;
import de.jskat.data.iss.ISSMoveInformation;
import de.jskat.data.iss.ISSTablePanelStatus;
import de.jskat.gui.IJSkatView;
import de.jskat.gui.action.JSkatAction;
import de.jskat.util.Card;
import de.jskat.util.Player;

/**
 * Controls all ISS related actions
 */
public class ISSController {

	private static Log log = LogFactory.getLog(ISSController.class);

	private JSkatMaster jskat;
	private IJSkatView view;
	private JSkatApplicationData data;

	private Connector issConnect;

	private Map<String, SkatGameData> gameData;

	/**
	 * Constructor
	 * 
	 * @param controller
	 *            JSkat master controller
	 * @param newData
	 *            Application data
	 */
	public ISSController(JSkatMaster controller, JSkatApplicationData newData) {

		this.jskat = controller;
		this.data = newData;
		this.gameData = new HashMap<String, SkatGameData>();
	}

	/**
	 * Sets the view (MVC)
	 * 
	 * @param newView
	 *            View
	 */
	public void setView(IJSkatView newView) {

		this.view = newView;
	}

	/**
	 * Disconnects from ISS
	 */
	public void disconnect() {

		if (this.issConnect != null && this.issConnect.isConnected()) {

			log.debug("connection to ISS still open"); //$NON-NLS-1$

			this.issConnect.closeConnection();
		}
	}

	/**
	 * Shows the login panel for ISS
	 */
	public void showISSLoginPanel() {

		this.view.showISSLogin();
	}

	/**
	 * Connects to the ISS
	 * 
	 * @param e
	 *            Login credentials
	 * @return TRUE if the connection was established successfully
	 */
	public boolean connectToISS(ActionEvent e) {

		log.debug("connectToISS"); //$NON-NLS-1$

		if (this.issConnect == null) {

			this.issConnect = new Connector(this);
		}

		log.debug("connector created"); //$NON-NLS-1$

		Object source = e.getSource();
		String command = e.getActionCommand();
		String login = null;
		String password = null;

		if (JSkatAction.CONNECT_TO_ISS.toString().equals(command)) {
			if (source instanceof ISSLoginCredentials) {

				ISSLoginCredentials loginCredentials = (ISSLoginCredentials) source;
				login = loginCredentials.getLoginName();
				password = loginCredentials.getPassword();

				if (!this.issConnect.isConnected()) {

					this.issConnect.setConnectionData(login, password,
							loginCredentials.getPort());
					this.issConnect.establishConnection();
				}
			} else {

				log.error("Wrong source for " + command); //$NON-NLS-1$
			}
		}

		if (this.issConnect.isConnected()) {

			// show ISS lobby if connection was successfull
			this.view.closeTabPanel("ISS login"); //$NON-NLS-1$
			this.view.showISSLobby();
			this.jskat.setIssLogin(login);
		}

		return this.issConnect.isConnected();
	}

	/**
	 * Updates ISS player list
	 * 
	 * @param playerName
	 *            Player name
	 * @param language
	 *            Language
	 * @param gamesPlayed
	 *            Games played
	 * @param strength
	 *            Play strength
	 */
	public void updateISSPlayerList(String playerName, String language,
			long gamesPlayed, double strength) {

		this.jskat.updateISSPlayer(playerName, language, gamesPlayed, strength);
	}

	/**
	 * Removes a player from the ISS player list
	 * 
	 * @param playerName
	 *            Player name
	 */
	public void removeISSPlayerFromList(String playerName) {

		this.jskat.removeISSPlayer(playerName);
	}

	/**
	 * Updates ISS table list
	 * 
	 * @param tableName
	 *            Table name
	 * @param maxPlayers
	 *            Maximum number of players
	 * @param gamesPlayed
	 *            Games played
	 * @param player1
	 *            Player 1 (? for free seat)
	 * @param player2
	 *            Player 2 (? for free seat)
	 * @param player3
	 *            Player 3 (? for free seat)
	 */
	public void updateISSTableList(String tableName, int maxPlayers,
			long gamesPlayed, String player1, String player2, String player3) {

		this.view.updateISSLobbyTableList(tableName, maxPlayers, gamesPlayed,
				player1, player2, player3);
	}

	/**
	 * Removes a table from the ISS table list
	 * 
	 * @param tableName
	 *            Table name
	 */
	public void removeISSTableFromList(String tableName) {

		this.view.removeFromISSLobbyTableList(tableName);
	}

	/**
	 * Sends a chat message to the ISS
	 * 
	 * @param message
	 *            Chat message
	 */
	public void sendChatMessage(ISSChatMessage message) {

		this.issConnect.send(message);
	}

	/**
	 * Adds a chat message to a chat
	 * 
	 * @param messageType
	 *            Chat message type
	 * @param params
	 *            Chat message
	 */
	public void addChatMessage(ChatMessageType messageType, List<String> params) {

		switch (messageType) {
		case LOBBY:
			addLobbyChatMessage(params);
			break;
		case USER:
		case TABLE:
			// TODO implement it
			break;
		}
	}

	void addLobbyChatMessage(List<String> params) {

		log.debug("addLobbyChatMessage"); //$NON-NLS-1$

		StringBuffer message = new StringBuffer();

		// first the sender of the message
		message.append(params.get(0)).append(": "); //$NON-NLS-1$
		// then the text
		for (int i = 1; i < params.size(); i++) {
			message.append(params.get(i)).append(' ');
		}

		ISSChatMessage chatMessage = new ISSChatMessage("Lobby", //$NON-NLS-1$
				message.toString());

		this.view.appendISSChatMessage(ChatMessageType.LOBBY, chatMessage);
	}

	/**
	 * Requests the creation of a new table on the ISS
	 */
	public void requestTableCreation() {

		this.issConnect.requestTableCreation();
	}

	/**
	 * Creates a local representation of an ISS table
	 * 
	 * @param tableName
	 *            Table name
	 * @param creator
	 *            Table creator
	 * @param maxPlayers
	 *            Maximum number of players
	 */
	public void createTable(String tableName, String creator, int maxPlayers) {

		this.view.createISSTable(tableName);
		this.jskat.setActiveTable(tableName);
	}

	/**
	 * Destroys a local representation of an ISS table
	 * 
	 * @param tableName
	 *            Table name
	 */
	public void destroyTable(String tableName) {

		this.view.closeTabPanel(tableName);
		// TODO set to next table
		this.jskat.setActiveTable(null);
	}

	/**
	 * Joins a table on ISS
	 * 
	 * @param tableName
	 *            Table name
	 */
	public void joinTable(String tableName) {

		this.issConnect.joinTable(tableName);
	}

	/**
	 * Observes a table on ISS
	 * 
	 * @param tableName
	 *            Table name
	 */
	public void observeTable(String tableName) {

		this.issConnect.observeTable(tableName);
	}

	/**
	 * Leaves a table on ISS
	 * 
	 * @param tableName
	 *            Table name
	 */
	public void leaveTable(String tableName) {
		this.issConnect.leaveTable(tableName);
	}

	/**
	 * Updates a local representation of an ISS table
	 * 
	 * @param tableName
	 *            Table name
	 * @param status
	 *            New table status
	 */
	public void updateISSTableState(String tableName, ISSTablePanelStatus status) {

		this.view.updateISSTable(tableName, status);
	}

	/**
	 * Updates a local representation of an ISS table
	 * 
	 * @param tableName
	 *            Table name
	 * @param status
	 *            New game status
	 */
	public void updateISSGame(String tableName, ISSGameStartInformation status) {

		this.view
				.updateISSTable(tableName, this.data.getIssLoginName(), status);

		this.gameData.put(tableName, createSkatGameData(status));
	}

	private SkatGameData createSkatGameData(ISSGameStartInformation status) {

		SkatGameData result = new SkatGameData();

		result.setGameState(GameState.NEW_GAME);
		for (Player player : Player.values()) {
			result.setPlayerName(player, status.getPlayerName(player));
		}

		return result;
	}

	/**
	 * Starts a game on a local representation of an ISS table
	 * 
	 * @param tableName
	 *            Table name
	 */
	public void startGame(String tableName) {

		this.view.startGame(tableName);

		// TODO inform human player
	}

	/**
	 * Updates a move on a local representation of an ISS table
	 * 
	 * @param tableName
	 *            Table name
	 * @param moveInformation
	 *            Move information
	 */
	public void updateMove(String tableName, ISSMoveInformation moveInformation) {

		this.view.updateISSMove(tableName, moveInformation);

		SkatGameData currGame = this.gameData.get(tableName);
		updateGameData(currGame, moveInformation);

		if (isHumanOnNextMove(tableName, currGame, moveInformation)) {
			// FIXME (jan 02.11.2010) wait for user input
		}
	}

	private void updateGameData(SkatGameData currGame,
			ISSMoveInformation moveInformation) {

		switch (moveInformation.getType()) {
		case DEAL:
			currGame.setGameState(GameState.DEALING);
			break;
		case BID:
			currGame.setGameState(GameState.BIDDING);
			currGame.setBidValue(moveInformation.getBidValue());
			break;
		case HOLD_BID:
			currGame.setGameState(GameState.BIDDING);
			break;
		case PASS:
			currGame.setGameState(GameState.BIDDING);
			break;
		case SKAT_REQUEST:
			currGame.setGameState(GameState.DISCARDING);
			break;
		case SKAT_LOOKING:
			currGame.setGameState(GameState.DISCARDING);
			break;
		case GAME_ANNOUNCEMENT:
			currGame.setGameState(GameState.DECLARING);
			currGame.setAnnouncement(moveInformation.getGameAnnouncement());
			break;
		case CARD_PLAY:
			currGame.setGameState(GameState.TRICK_PLAYING);
			break;
		case TIME_OUT:
			currGame.setGameState(GameState.PRELIMINARY_GAME_END);
			break;
		}
	}

	private boolean isHumanOnNextMove(String tableName, SkatGameData currGame,
			ISSMoveInformation moveInformation) {
		// FIXME Here it must be decided whether the human player shall be
		// activated according the current game state
		// activation should only be done if neccessary

		boolean result = false;

		switch (currGame.getGameState()) {
		case BIDDING:
			if (currGame.getPlayerName(Player.FORE_HAND).equals(
					this.data.getIssLoginName())) {
				result = true;
			}
			break;
		}

		return result;
	}

	private void activateHumanPlayer(IJSkatPlayer human, String tableName,
			SkatGameData currGame, ISSMoveInformation moveInformation) {

		// FIXME (jan 24.10.2010) activate correct move on human player

		switch (currGame.getGameState()) {
		case BIDDING:
			break;
		case TRICK_PLAYING:
			human.cardPlayed(
					Player.valueOf(moveInformation.getMovePlayer().name()),
					moveInformation.getCard());
			human.playCard();
			// issConnect.sendCardMove();
			break;
		}
		// // Dealing
		// human.setUpBidding();
		// int bid = human.bidMore(18);
		// if (bid == -1) {
		// issConnect.sendPassMove(tableName);
		// } else {
		// issConnect.sendBidMove(tableName, bid);
		// }
		//
		// // Bidding
		//		log.debug("bid was done on ISS: " + moveInformation.getBidValue()); //$NON-NLS-1$
		// human.bidByPlayer(
		// Player.valueOf(moveInformation.getMovePlayer().name()),
		// moveInformation.getBidValue());
		// if (human.holdBid(moveInformation.getBidValue())) {
		// issConnect.sendHoldBidMove(tableName);
		// } else {
		// issConnect.sendPassMove(tableName);
		// }
		//
		// // Bidding hold/pass
		//		log.debug("bid was hold on ISS: " + moveInformation.getBidValue()); //$NON-NLS-1$
		// int nextBidValue = human.bidMore(SkatConstants
		// .getNextBidValue(moveInformation.getBidValue()));
		// if (nextBidValue > 0) {
		// issConnect.sendBidMove(tableName, nextBidValue);
		// } else {
		// issConnect.sendPassMove(tableName);
		// }
		//
		// // Discarding
		//		log.debug("discarding on ISS"); //$NON-NLS-1$
		// human.lookIntoSkat();
		//
		// // Skat looking
		//		log.debug("skat looking on ISS"); //$NON-NLS-1$
		// human.discardSkat();
		// human.announceGame();
		//
		// // Game announcement
		//		log.debug("game announcing on ISS"); //$NON-NLS-1$
		// human.announceGame();
		//
		// // Card play
		//		log.debug("card play on ISS"); //$NON-NLS-1$
		// human.cardPlayed(
		// Player.valueOf(moveInformation.getMovePlayer().name()),
		// moveInformation.getCard());
		// human.playCard();
		//
		// // Time out
		//		log.debug("time out on ISS"); //$NON-NLS-1$
		// human.finalizeGame();
	}

	/**
	 * Shows a message from ISS
	 * 
	 * @param messageType
	 * @param message
	 */
	public void showMessage(int messageType, String message) {

		this.view.showMessage(messageType, message);
	}

	/**
	 * Invites a player on ISS to play at a table on ISS
	 * 
	 * @param tableName
	 *            Table name
	 * @param invitee
	 *            Invited player
	 */
	public void invitePlayer(String tableName, String invitee) {
		this.issConnect.invitePlayer(tableName, invitee);
	}

	/**
	 * Sends ready to play signal to ISS
	 * 
	 * @param tableName
	 *            Table name
	 */
	public void sendReadySignal(String tableName) {
		this.issConnect.sendReadySignal(tableName);
	}

	/**
	 * Send talk enabled signal to ISS
	 * 
	 * @param tableName
	 *            Table name
	 */
	public void sendTalkEnabledSignal(String tableName) {
		this.issConnect.sendTalkEnabledSignal(tableName);
	}

	/**
	 * Send table seat change singal to ISS
	 * 
	 * @param tableName
	 *            Table name
	 */
	public void sendTableSeatChangeSignal(String tableName) {
		this.issConnect.sendTableSeatChangeSignal(tableName);
	}

	/**
	 * Send pass bid move to ISS
	 * 
	 * @param tableName
	 *            Table name
	 */
	public void sendPassBidMove(String tableName) {
		this.issConnect.sendPassMove(tableName);
	}

	/**
	 * Send hold bid move to ISS
	 * 
	 * @param tableName
	 *            Table name
	 */
	public void sendHoldBidMove(String tableName) {
		this.issConnect.sendHoldBidMove(tableName);
	}

	/**
	 * Send look into skat move to ISS
	 * 
	 * @param tableName
	 *            Table name
	 */
	public void sendLookIntoSkatMove(String tableName) {
		this.issConnect.sendLookIntoSkatMove(tableName);
	}

	/**
	 * Send game announcement to ISS
	 * 
	 * @param tableName
	 *            Table name
	 * @param gameAnnouncement
	 *            Game announcement
	 * @param discardedCards
	 *            Discarded cards
	 */
	public void sendGameAnnouncementMove(String tableName,
			GameAnnouncement gameAnnouncement, Card... discardedCards) {

		this.issConnect.sendGameAnnouncementMove(tableName,
				gameAnnouncement.getGameType(), gameAnnouncement.isHand(),
				gameAnnouncement.isOuvert(), discardedCards);

	}

	/**
	 * Send card move to ISS
	 * 
	 * @param tableName
	 *            Table name
	 * @param nextCard
	 *            Card
	 */
	public void sendCardMove(String tableName, Card nextCard) {
		this.issConnect.sendCardMove(tableName, nextCard);
	}
}
