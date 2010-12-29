/*

@ShortLicense@

Authors: @JS@
         @MJL@

Released: @ReleaseDate@

 */

package de.jskat.control;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.jskat.ai.IJSkatPlayer;
import de.jskat.data.SkatSeriesData;
import de.jskat.data.SkatSeriesData.SeriesState;
import de.jskat.gui.IJSkatView;
import de.jskat.gui.human.HumanPlayer;
import de.jskat.util.Player;

/**
 * Controls a series of skat games
 */
public class SkatSeries extends JSkatThread {

	private static Log log = LogFactory.getLog(SkatSeries.class);

	private int maxSleep = 0;
	private SkatSeriesData data;
	private int roundsToGo = 0;
	private boolean unlimitedRounds = false;
	private Map<Player, IJSkatPlayer> player;
	private SkatGame currSkatGame;

	private IJSkatView view;

	/**
	 * Constructor
	 * 
	 * @param tableName
	 *            Table name
	 */
	public SkatSeries(String tableName) {

		data = new SkatSeriesData();
		data.setState(SeriesState.WAITING);
		data.setTableName(tableName);
		player = new HashMap<Player, IJSkatPlayer>();
	}

	/**
	 * Sets the skat players
	 * 
	 * @param newPlayer
	 *            New skat series player
	 */
	public void setPlayer(List<IJSkatPlayer> newPlayer) {

		if (newPlayer.size() != 3) {
			throw new IllegalArgumentException(
					"Only three players are allowed at the moment."); //$NON-NLS-1$
		}

		// set players in random order
		Collections.shuffle(newPlayer);
		player.put(Player.FORE_HAND, newPlayer.get(0));
		player.put(Player.MIDDLE_HAND, newPlayer.get(1));
		player.put(Player.HIND_HAND, newPlayer.get(2));

		log.debug("Player order: " + player); //$NON-NLS-1$
	}

	/**
	 * Checks whether a series is running
	 * 
	 * @return TRUE if the series is running
	 */
	public boolean isRunning() {

		return data.getState() == SeriesState.RUNNING;
	}

	/**
	 * Starts the series
	 * 
	 * @param rounds
	 *            Number of rounds to be played
	 */
	public void startSeries(int rounds, boolean newUnlimitedRound) {

		roundsToGo = rounds;
		unlimitedRounds = newUnlimitedRound;
		data.setState(SeriesState.RUNNING);
	}

	/**
	 * @see Thread#run()
	 */
	@Override
	public void run() {

		int roundsPlayed = 0;
		int gameNumber = 0;

		while (roundsToGo > 0 || unlimitedRounds) {

			log.debug("Playing round " + (roundsPlayed + 1)); //$NON-NLS-1$

			for (int j = 0; j < 3; j++) {

				if (j > 0 || roundsPlayed > 0) {
					// change player positions after first game
					IJSkatPlayer helper = player.get(Player.HIND_HAND);
					player.put(Player.HIND_HAND, player.get(Player.FORE_HAND));
					player.put(Player.FORE_HAND, player.get(Player.MIDDLE_HAND));
					player.put(Player.MIDDLE_HAND, helper);
				}

				gameNumber++;
				view.setGameNumber(data.getTableName(), gameNumber);

				currSkatGame = new SkatGame(data.getTableName(),
						player.get(Player.FORE_HAND),
						player.get(Player.MIDDLE_HAND),
						player.get(Player.HIND_HAND));

				setViewPositions();

				currSkatGame.setView(view);

				log.debug("Playing game " + (j + 1)); //$NON-NLS-1$

				data.addGame(currSkatGame);
				currSkatGame.start();
				try {
					currSkatGame.join();

					log.debug("Game ended: join"); //$NON-NLS-1$

					sleep(maxSleep);

				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if (isHumanPlayerInvolved()) {
					// wait for human to start next game
					startWaiting();
				}

				checkWaitCondition();
			}

			roundsToGo--;
			roundsPlayed++;

			checkWaitCondition();
		}

		data.setState(SeriesState.SERIES_FINISHED);
		view.setSeriesState(data.getTableName(), SeriesState.SERIES_FINISHED);

		log.debug(data.getState());
	}

	private void setViewPositions() {

		if (player.get(Player.FORE_HAND) instanceof HumanPlayer) {
			view.setPositions(data.getTableName(), Player.MIDDLE_HAND,
					Player.HIND_HAND, Player.FORE_HAND);
		} else if (player.get(Player.MIDDLE_HAND) instanceof HumanPlayer) {
			view.setPositions(data.getTableName(), Player.HIND_HAND,
					Player.FORE_HAND, Player.MIDDLE_HAND);
		} else {
			view.setPositions(data.getTableName(), Player.FORE_HAND,
					Player.MIDDLE_HAND, Player.HIND_HAND);
		}
	}

	private boolean isHumanPlayerInvolved() {

		boolean result = false;

		for (IJSkatPlayer currPlayer : player.values()) {

			if (currPlayer instanceof HumanPlayer) {

				result = true;
			}
		}

		return result;
	}

	/**
	 * Gets the state of the series
	 * 
	 * @return State of the series
	 */
	public SeriesState getSeriesState() {

		return data.getState();
	}

	/**
	 * Gets the ID of the current game
	 * 
	 * @return ID of the current game
	 */
	public int getCurrentGameID() {

		return data.getCurrentGameID();
	}

	/**
	 * Pauses the current game
	 */
	public void pauseSkatGame() {

		synchronized (currSkatGame) {

			currSkatGame.startWaiting();
		}
	}

	/**
	 * Resumes the current game
	 */
	public void resumeSkatGame() {

		synchronized (currSkatGame) {

			currSkatGame.stopWaiting();
			currSkatGame.notify();
		}
	}

	/**
	 * Checks whether the current skat game is paused
	 * 
	 * @return TRUE if the current skat game is paused
	 */
	public boolean isSkatGameWaiting() {

		return currSkatGame.isWaiting();
	}

	/**
	 * Sets the view for the series
	 * 
	 * @param newView
	 *            View
	 */
	public void setView(IJSkatView newView) {

		view = newView;
	}
}
