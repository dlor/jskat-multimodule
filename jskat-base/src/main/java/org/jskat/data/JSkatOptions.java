/**
 * JSkat - A skat program written in Java
 * by Jan Schäfer, Markus J. Luzius and Daniel Loreck
 *
 * Version 0.13.0-SNAPSHOT
 * Copyright (C) 2013-05-10
 *
 * Licensed under the Apache License, Version 2.0. You may
 * obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jskat.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Properties;

import org.jskat.data.SkatTableOptions.RamschSkatOwner;
import org.jskat.data.SkatTableOptions.RuleSet;
import org.jskat.gui.img.CardFace;
import org.jskat.gui.img.CardSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holds all options of JSkat
 */
public class JSkatOptions {

	private final static String PROPERTIES_FILENAME = "jskat.properties"; //$NON-NLS-1$

	/**
	 * Languages supported by JSkat
	 */
	public enum SupportedLanguage {
		/**
		 * English
		 */
		ENGLISH,
		/**
		 * German
		 */
		GERMAN;
	}

	private enum Option {
		bockEventContraReAnnounced, bockEventLostAfterContra, bockEventLostGrand, bockEventLostWith60, bockEventPlayerHasX00Points, @Deprecated
		cardFace, cardSet, cheatDebugMode, checkForNewVersionAtStartUp, gameShortCut, issAddress, issPort, language, maxPlayerCount, playBock, playContra, playRamsch, playRevolution, ramschEventNoBid, ramschEventRamschAfterBock, ramschGrandHandPossible, ramschSkatOwner, rules, savePath, schieberRamsch, schieberRamschJacksInSkat, showTipsAtStartUp, trickRemoveAfterClick, trickRemoveDelayTime, contraAfterBid18;
	}

	private static Logger log = LoggerFactory.getLogger(JSkatOptions.class);

	static private JSkatOptions optionsInstance = null;

	/**
	 * Returns the instance of the singleton JSkatOptions<br />
	 * This method must be called at the very beginning
	 * 
	 * @return Options
	 */
	static public JSkatOptions instance(final SavePathResolver pathResolver) {

		if (optionsInstance == null) {
			optionsInstance = new JSkatOptions(pathResolver);
		}

		return optionsInstance;
	}

	/**
	 * Returns the instance of the singleton {@link JSkatOptions}<br />
	 * This methods throws a {@link IllegalStateException} if
	 * {@link #instance(SavePathResolver)} was not called before
	 * 
	 * @return Options
	 */
	static public JSkatOptions instance() {

		if (optionsInstance == null) {
			throw new IllegalStateException("Options not intialized, yet."); //$NON-NLS-1$
		}

		return optionsInstance;
	}

	private final Properties options = new Properties();

	/** Creates a new instance of JSkatOptions */
	private JSkatOptions(final SavePathResolver pathResolver) {

		setDefaultProperties(pathResolver);

		try {
			loadOptions();

		} catch (FileNotFoundException e) {

			log.debug("No properties file found. Using standard values."); //$NON-NLS-1$

			setOption(Option.showTipsAtStartUp, Boolean.TRUE);
			File dir = new File(pathResolver.getDefaultSavePath());
			dir.mkdir();
			String filename = pathResolver.getDefaultSavePath()
					+ PROPERTIES_FILENAME;
			File file = new File(filename);
			try {
				file.createNewFile();

				log.debug("Property file jskat.properties created: <" //$NON-NLS-1$
						+ filename + ">"); //$NON-NLS-1$
			} catch (IOException e1) {
				log.warn("Could not create property file <" + filename //$NON-NLS-1$
						+ "> due to " + e1.getClass() + ": " + e1.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
			}

			setDefaultProperties(pathResolver);

		} catch (IOException e) {
			log.warn("Could not load properties: " + e.getClass() + ": " //$NON-NLS-1$ //$NON-NLS-2$
					+ e.getMessage());
		}
	}

	/**
	 * Getter for property cardFace
	 * 
	 * @return Value of property cardFace
	 */
	public CardSet getCardSet() {
		return CardSet.valueOf(getOption(Option.cardSet));
	}

	/**
	 * Gets the address of the ISS
	 * 
	 * @return Address
	 */
	public String getIssAddress() {
		return getOption(Option.issAddress);
	}

	/**
	 * Gets the port of the ISS
	 * 
	 * @return Port
	 */
	public Integer getIssPort() {
		return Integer.valueOf(getOption(Option.issPort));
	}

	/**
	 * Getter for property language.
	 * 
	 * @return Value of property language.
	 */
	public SupportedLanguage getLanguage() {
		return SupportedLanguage.valueOf(getOption(Option.language));
	}

	/**
	 * Gets the maximum number of players allowed in a skat series
	 * 
	 * @return Maximum number
	 */
	public Integer getMaxPlayerCount() {
		return getIntegerOption(Option.maxPlayerCount);
	}

	/**
	 * Getter for property ramschSkatOwner.
	 * 
	 * @return Value of property ramschSkatOwner.
	 */
	public RamschSkatOwner getRamschSkatOwner() {
		return RamschSkatOwner.valueOf(getOption(Option.ramschSkatOwner));
	}

	/**
	 * Getter for property rules.
	 * 
	 * @return Value of property rules.
	 */
	public RuleSet getRules() {
		return RuleSet.valueOf(getOption(Option.rules));
	}

	/**
	 * Getter for property savePath.
	 * 
	 * @return Value of property savePath.
	 */
	public String getSavePath() {
		return getOption(Option.savePath);
	}

	/**
	 * Gets the current skat table options
	 * 
	 * @return The current skat table options
	 */
	public SkatTableOptions getSkatTableOptions() {

		SkatTableOptions result = new SkatTableOptions();

		result.setMaxPlayerCount(getMaxPlayerCount());

		result.setPlayBock(isPlayBock());

		result.setBockEventContraReAnnounced(isBockEventContraReAnnounced());
		result.setBockEventLostAfterContra(isBockEventLostAfterContra());
		result.setBockEventLostGrand(isBockEventLostGrand());
		result.setBockEventLostWith60(isBockEventLostWith60());
		result.setBockEventPlayerHasX00Points(isBockEventPlayerHasX00Points());

		result.setPlayContra(isPlayContra());
		result.setPlayRamsch(isPlayRamsch());
		result.setPlayRevolution(isPlayRevolution());

		return result;
	}

	/**
	 * Getter for property trickRemoveDelayTime.
	 * 
	 * @return Value of property trickRemoveDelayTime.
	 */
	public Integer getTrickRemoveDelayTime() {
		return getIntegerOption(Option.trickRemoveDelayTime);
	}

	/**
	 * Getter for property bockEventContraReAnnounced
	 * 
	 * @return Value of property bockEventContraReAnnounced
	 */
	public Boolean isBockEventContraReAnnounced() {
		return isBockEventContraReAnnounced(true);
	}

	public Boolean isBockEventContraReAnnounced(final boolean checkParentOption) {
		return getBooleanOptionWithParentCheck(
				Option.bockEventContraReAnnounced, checkParentOption,
				isPlayBock(checkParentOption));
	}

	/**
	 * Getter for property bockEventLostAfterContra
	 * 
	 * @return Value of property bockEventLostAfterContra
	 */
	public Boolean isBockEventLostAfterContra() {
		return isBockEventLostAfterContra(true);
	}

	public Boolean isBockEventLostAfterContra(final boolean checkParentOption) {
		return getBooleanOptionWithParentCheck(Option.bockEventLostAfterContra,
				checkParentOption, isPlayBock(checkParentOption));
	}

	/**
	 * Getter for property bockEventLostGrand
	 * 
	 * @return Value of property bockEventLostGrand
	 */
	public Boolean isBockEventLostGrand() {
		return isBockEventLostGrand(true);
	}

	public Boolean isBockEventLostGrand(final boolean checkParentOption) {
		return getBooleanOptionWithParentCheck(Option.bockEventLostGrand,
				checkParentOption, isPlayBock(checkParentOption));
	}

	/**
	 * Getter for property bockEventLostWith60
	 * 
	 * @return Value of property bockEventLostWith60
	 */
	public Boolean isBockEventLostWith60() {
		return isBockEventLostWith60(true);
	}

	public Boolean isBockEventLostWith60(final boolean checkParentOption) {
		return getBooleanOptionWithParentCheck(Option.bockEventLostWith60,
				checkParentOption, isPlayBock(checkParentOption));
	}

	/**
	 * Getter for property bockEventPlayerHasX00Points
	 * 
	 * @return Value of property bockEventPlayerHasX00Points
	 */
	public Boolean isBockEventPlayerHasX00Points() {
		return isBockEventPlayerHasX00Points(true);
	}

	public Boolean isBockEventPlayerHasX00Points(final boolean checkParentOption) {
		return getBooleanOptionWithParentCheck(
				Option.bockEventPlayerHasX00Points, checkParentOption,
				isPlayBock(checkParentOption));
	}

	/**
	 * Checks whether at least a bid of 18 has to be done to say contra
	 * 
	 * @return TRUE, if the check succeeds
	 */
	public Boolean isContraAfterBid18() {
		return isContraAfterBid18(true);
	}

	/**
	 * Checks whether at least a bid of 18 has to be done to say contra
	 * 
	 * @return TRUE, if the check succeeds
	 */
	public Boolean isContraAfterBid18(final boolean checkParentOption) {
		return getBooleanOptionWithParentCheck(Option.contraAfterBid18,
				checkParentOption, isPlayContra(checkParentOption));
	}

	/**
	 * Getter for property cheatDebugMode.
	 * 
	 * @return Value of property cheatDebugMode.
	 */
	public Boolean isCheatDebugMode() {
		return getBooleanOption(Option.cheatDebugMode);
	}

	/**
	 * Gets the flag for checking for a new version of JSkat at start up
	 * 
	 * @return TRUE, if the check should be performed at start up
	 */
	public Boolean isCheckForNewVersionAtStartUp() {
		return getBooleanOption(Option.checkForNewVersionAtStartUp);
	}

	/**
	 * Getter for property gameShortCut.
	 * 
	 * @return Value of property gameShortCut.
	 */
	public Boolean isGameShortCut() {
		return getBooleanOption(Option.gameShortCut);
	}

	/**
	 * Getter for property playBock.
	 * 
	 * @return Value of property playBock.
	 */
	public Boolean isPlayBock() {
		return isPlayBock(true);
	}

	/**
	 * Getter for property playBock.
	 * 
	 * @return Value of property playBock.
	 */
	public Boolean isPlayBock(final boolean checkParentOption) {
		return getBooleanOptionWithParentCheck(Option.playBock,
				checkParentOption, RuleSet.PUB.equals(getRules()));
	}

	private Boolean getBooleanOptionWithParentCheck(final Option option,
			final boolean checkParentOption, final boolean parentOptionActivated) {

		Boolean result = getBooleanOption(option);

		if (checkParentOption) {
			if (!parentOptionActivated) {
				result = Boolean.FALSE;
			}
		}

		return result;
	}

	/**
	 * Getter for property playKontra.
	 * 
	 * @return Value of property playKontra.
	 */
	public Boolean isPlayContra() {
		return isPlayContra(true);
	}

	/**
	 * Getter for property playKontra.
	 * 
	 * @return Value of property playKontra.
	 */
	public Boolean isPlayContra(final boolean checkParentOption) {
		return getBooleanOptionWithParentCheck(Option.playContra,
				checkParentOption, RuleSet.PUB.equals(getRules()));
	}

	/**
	 * Getter for property playRamsch.
	 * 
	 * @return Value of property playRamsch.
	 */
	public Boolean isPlayRamsch() {
		return isPlayRamsch(true);
	}

	/**
	 * Checks whether Ramsch should be played
	 * 
	 * @param checkParentOption
	 *            TRUE, if parent option should be checked too
	 * @return TRUE, if Ramsch should be played
	 */
	public Boolean isPlayRamsch(final boolean checkParentOption) {
		return getBooleanOptionWithParentCheck(Option.playRamsch,
				checkParentOption, RuleSet.PUB.equals(getRules()));
	}

	/**
	 * Getter for property playRevolution.
	 * 
	 * @return Value of property playRevolution.
	 */
	public Boolean isPlayRevolution() {
		return getBooleanOption(Option.playRevolution);
	}

	public Boolean isPlayRevolution(final boolean checkParentOption) {
		return getBooleanOptionWithParentCheck(Option.playRevolution,
				checkParentOption, RuleSet.PUB.equals(getRules()));
	}

	/**
	 * Getter for property ramschEventNoBid
	 * 
	 * @return Value of property ramschEventNoBid
	 */
	public Boolean isRamschEventNoBid() {
		return isRamschEventNoBid(true);
	}

	public Boolean isRamschEventNoBid(final boolean checkParentOption) {
		return getBooleanOptionWithParentCheck(Option.ramschEventNoBid,
				checkParentOption, isPlayRamsch(checkParentOption));
	}

	/**
	 * Getter for property ramschEventRamschAfterBock
	 * 
	 * @return Value of property ramschEventRamschAfterBock
	 */
	public Boolean isRamschEventRamschAfterBock() {
		return isRamschEventRamschAfterBock(true);
	}

	public Boolean isRamschEventRamschAfterBock(final boolean checkParentOption) {
		return getBooleanOptionWithParentCheck(
				Option.ramschEventRamschAfterBock, checkParentOption,
				isPlayRamsch(checkParentOption));
	}

	/**
	 * Getter for property ramschGrandHandPossible
	 * 
	 * @return Value of property ramschGrandHandPossible
	 */
	public Boolean isRamschGrandHandPossible() {
		return getBooleanOption(Option.ramschGrandHandPossible);
	}

	/**
	 * Getter for property schieberRamsch
	 * 
	 * @return Value of property schieberRamsch
	 */
	public Boolean isSchieberRamsch() {
		return isSchieberRamsch(true);
	}

	public Boolean isSchieberRamsch(final boolean checkParentOption) {
		return getBooleanOptionWithParentCheck(Option.schieberRamsch,
				checkParentOption, isPlayRamsch(checkParentOption));
	}

	/**
	 * Getter for property schieberRamschJacksInSkat
	 * 
	 * @return Value of property schieberRamschJacksInSkat
	 */
	public Boolean isSchieberRamschJacksInSkat() {
		return isSchieberRamschJacksInSkat(true);
	}

	public Boolean isSchieberRamschJacksInSkat(final boolean checkParentOption) {
		return getBooleanOptionWithParentCheck(
				Option.schieberRamschJacksInSkat, checkParentOption,
				isSchieberRamsch(checkParentOption));
	}

	/**
	 * Flag that is set if JSkat seems to be used for the first time. The flag
	 * is set if no option file is found.
	 * 
	 * @return true, if this is the first use of JSkat
	 */
	public Boolean isShowTipsAtStartUp() {
		return getBooleanOption(Option.showTipsAtStartUp);
	}

	/**
	 * Getter for property trickRemoveAfterClick.
	 * 
	 * @return Value of property trickRemoveAfterClick.
	 */
	public Boolean isTrickRemoveAfterClick() {
		return getBooleanOption(Option.trickRemoveAfterClick);
	}

	/**
	 * Saves the options to a file .jskat in user home
	 * 
	 */
	public void saveJSkatProperties() {

		FileOutputStream stream = null;
		try {
			stream = new FileOutputStream(System.getProperty("user.home") //$NON-NLS-1$
					+ System.getProperty("file.separator") + ".jskat" //$NON-NLS-1$//$NON-NLS-2$
					+ System.getProperty("file.separator") + "jskat.properties"); //$NON-NLS-1$//$NON-NLS-2$

			options.store(stream, "JSkat options"); //$NON-NLS-1$
			stream.close();
			log.debug("Saved rules: " + getRules()); //$NON-NLS-1$

		} catch (FileNotFoundException e1) {

			log.debug("No properties file found. Saving of JSkat options failed."); //$NON-NLS-1$
		} catch (IOException e) {
			log.debug("Saving of JSkat options failed."); //$NON-NLS-1$
			log.debug(e.toString());
		}
	}

	/**
	 * Setter for property bockEventContraReAnnounced
	 * 
	 * @param bockEventContraReAnnounced
	 *            New value of property bockEventContraReAnnounced
	 */
	public void setBockEventContraReAnnounced(
			final Boolean bockEventContraReAnnounced) {
		setOption(Option.bockEventContraReAnnounced, bockEventContraReAnnounced);
	}

	/**
	 * Setter for property bockEventLostAfterContra
	 * 
	 * @param bockEventLostAfterContra
	 *            New value of property bockEventLostAfterContra
	 */
	public void setBockEventLostAfterContra(
			final Boolean bockEventLostAfterContra) {
		setOption(Option.bockEventLostAfterContra, bockEventLostAfterContra);
	}

	/**
	 * Setter for property bockEventLostGrand
	 * 
	 * @param bockEventLostGrand
	 *            New value of property bockEventLostGrand
	 */
	public void setBockEventLostGrand(final Boolean bockEventLostGrand) {
		setOption(Option.bockEventLostGrand, bockEventLostGrand);
	}

	/**
	 * Setter for property bockEventLostWith60
	 * 
	 * @param bockEventLostWith60
	 *            New value of property bockEventLostWith60
	 */
	public void setBockEventLostWith60(final Boolean bockEventLostWith60) {
		setOption(Option.bockEventLostWith60, bockEventLostWith60);
	}

	/**
	 * Setter for property bockEventPlayerHasX00Points
	 * 
	 * @param bockEventPlayerHasX00Points
	 *            New value of property bockEventPlayerHasX00Points
	 */
	public void setBockEventPlayerHasX00Points(
			final Boolean bockEventPlayerHasX00Points) {
		setOption(Option.bockEventPlayerHasX00Points,
				bockEventPlayerHasX00Points);
	}

	/**
	 * Sets the flag for bid at least 18 to say contra
	 * 
	 * @param contraAfterBid18
	 */
	public void setContraAfterBid18(final Boolean contraAfterBid18) {
		setOption(Option.contraAfterBid18, contraAfterBid18);
	}

	/**
	 * Setter for property cardFace
	 * 
	 * @param cardFace
	 *            New value of property cardFace
	 */
	public void setCardSet(final CardSet cardSet) {
		setOption(Option.cardSet, cardSet);
	}

	/**
	 * Setter for property cheatDebugMode.
	 * 
	 * @param isCheatDebugMode
	 *            New value of property cheatDebugMode.
	 */
	public void setCheatDebugMode(final Boolean isCheatDebugMode) {
		setOption(Option.cheatDebugMode, isCheatDebugMode);
	}

	/**
	 * Sets the flag for checking for a new version of JSkat at start up
	 * 
	 * @param isCheckForNewVersionAtStartUp
	 *            TRUE, if the check should be performed at start up
	 */
	public void setCheckForNewVersionAtStartUp(
			final Boolean isCheckForNewVersionAtStartUp) {
		setOption(Option.checkForNewVersionAtStartUp,
				isCheckForNewVersionAtStartUp);
	}

	/**
	 * Setter for property gameShortCut.
	 * 
	 * @param isGameShortCut
	 *            New value of property gameShortCut.
	 */
	public void setGameShortCut(final Boolean isGameShortCut) {
		setOption(Option.gameShortCut, isGameShortCut);
	}

	/**
	 * Sets the address of the ISS
	 * 
	 * @param address
	 *            Address
	 */
	public void setIssAddress(final String address) {
		setOption(Option.issAddress, address);
	}

	/**
	 * Sets the port of the ISS
	 * 
	 * @param port
	 *            Port
	 */
	public void setIssPort(final Integer port) {
		setOption(Option.issPort, port);
	}

	/**
	 * Setter for property language.
	 * 
	 * @param language
	 *            New value of property language.
	 */
	public void setLanguage(final SupportedLanguage language) {
		setOption(Option.language, language);
	}

	/**
	 * Sets the maximum number of players in a skat series
	 * 
	 * @param count
	 *            Maximumn number
	 */
	public void setMaxPlayerCount(final Integer count) {
		setOption(Option.maxPlayerCount, count);
	}

	/**
	 * Setter for property playBock.
	 * 
	 * @param playBock
	 *            New value of property playBock.
	 */
	public void setPlayBock(final Boolean playBock) {
		setOption(Option.playBock, playBock);
	}

	/**
	 * Setter for property playKontra.
	 * 
	 * @param playContra
	 *            New value of property playKontra.
	 */
	public void setPlayContra(final Boolean playContra) {
		setOption(Option.playContra, playContra);
	}

	/**
	 * Setter for property playRamsch.
	 * 
	 * @param playRamsch
	 *            New value of property playRamsch.
	 */
	public void setPlayRamsch(final Boolean playRamsch) {
		setOption(Option.playRamsch, playRamsch);
	}

	/**
	 * Setter for property playRevolution.
	 * 
	 * @param playRevolution
	 *            New value of property playRevolution.
	 */
	public void setPlayRevolution(final Boolean playRevolution) {
		setOption(Option.playRevolution, playRevolution);
	}

	/**
	 * Setter for property ramschEventNoBid
	 * 
	 * @param ramschEventNoBid
	 *            New value of property ramschEventNoBid
	 */
	public void setRamschEventNoBid(final Boolean ramschEventNoBid) {
		setOption(Option.ramschEventNoBid, ramschEventNoBid);
	}

	/**
	 * Setter for property ramschEventRamschAfterBock
	 * 
	 * @param ramschEventRamschAfterBock
	 *            New value of property ramschEventRamschAfterBock
	 */
	public void setRamschEventRamschAfterBock(
			final Boolean ramschEventRamschAfterBock) {
		setOption(Option.ramschEventRamschAfterBock, ramschEventRamschAfterBock);
	}

	/**
	 * Setter for property ramschGrandHandPossible
	 * 
	 * @param ramschGrandHandPossible
	 *            New value of property ramschGrandHandPossible
	 */
	public void setRamschGrandHandPossible(final Boolean ramschGrandHandPossible) {
		setOption(Option.ramschGrandHandPossible, ramschGrandHandPossible);
	}

	/**
	 * Setter for property ramschSkatOwner.
	 * 
	 * @param ramschSkatOwner
	 *            New value of property ramschSkatOwner.
	 */
	public void setRamschSkatOwner(final RamschSkatOwner ramschSkat) {
		setOption(Option.ramschSkatOwner, ramschSkat);
	}

	/**
	 * Setter for property rules.
	 * 
	 * @param ruleSet
	 *            New value of property rules.
	 */
	public void setRules(final RuleSet ruleSet) {
		setOption(Option.rules, ruleSet.name());
	}

	/**
	 * Setter for property savePath.
	 * 
	 * @param savePath
	 *            New value of property savePath.
	 */
	public void setSavePath(final String savePath) {
		setOption(Option.savePath, savePath);
	}

	/**
	 * Setter for property schieberRamsch
	 * 
	 * @param schieberRamsch
	 *            New value of property schieberRamsch
	 */
	public void setSchieberRamsch(final Boolean schieberRamsch) {
		setOption(Option.schieberRamsch, schieberRamsch);
	}

	/**
	 * Setter for property schieberRamschJacksInSkat
	 * 
	 * @param schieberRamschJacksInSkat
	 *            New value of property schieberRamschJacksInSkat
	 */
	public void setSchieberRamschJacksInSkat(
			final Boolean schieberRamschJacksInSkat) {
		setOption(Option.schieberRamschJacksInSkat, schieberRamschJacksInSkat);
	}

	/**
	 * Setter for property trickRemoveAfterClick.
	 * 
	 * @param trickRemoveAfterClick
	 *            New value of property trickRemoveAfterClick.
	 */
	public void setTrickRemoveAfterClick(final Boolean trickRemoveAfterClick) {
		setOption(Option.trickRemoveAfterClick, trickRemoveAfterClick);
	}

	/**
	 * Setter for property trickRemoveDelayTime.
	 * 
	 * @param trickRemoveDelayTime
	 *            New value of property trickRemoveDelayTime.
	 */
	public void setTrickRemoveDelayTime(final Integer trickRemoveDelayTime) {
		setOption(Option.trickRemoveDelayTime, trickRemoveDelayTime);
	}

	private Boolean getBooleanOption(final Option option) {
		return Boolean.valueOf(options.getProperty(option.name()));
	}

	private static SupportedLanguage getDefaultLanguage() {

		SupportedLanguage result = SupportedLanguage.ENGLISH;

		if (Locale.getDefault().getLanguage()
				.equals(Locale.GERMAN.getLanguage())) {
			result = SupportedLanguage.GERMAN;
		}

		return result;
	}

	private static FileInputStream getFileStream() throws FileNotFoundException {
		FileInputStream stream = new FileInputStream(getDefaultSaveDir()
				+ "jskat.properties"); //$NON-NLS-1$
		return stream;
	}

	static String getDefaultSaveDir() {
		return System.getProperty("user.home") //$NON-NLS-1$
				+ System.getProperty("file.separator") + ".jskat" //$NON-NLS-1$ //$NON-NLS-2$
				+ System.getProperty("file.separator"); //$NON-NLS-1$
	}

	private Integer getIntegerOption(final Option option) {
		return Integer.valueOf(options.getProperty(option.name()));
	}

	private String getOption(final Option option) {
		return options.getProperty(option.name());
	}

	private void loadOptions() throws FileNotFoundException, IOException {

		FileInputStream stream = getFileStream();

		Properties loadedOptions = new Properties();
		loadedOptions.load(stream);

		Enumeration<Object> props = loadedOptions.keys();
		String property;
		String value;

		while (props.hasMoreElements()) {

			property = (String) props.nextElement();
			value = loadedOptions.getProperty(property);

			Option option = null;
			try {
				option = Option.valueOf(property);
			} catch (IllegalArgumentException e) {
				log.error("Unknown option " + property + " with value " + value); //$NON-NLS-1$ //$NON-NLS-2$

				// handle obsolete or renamed options
				if ("ramschSkat".equals(property)) { //$NON-NLS-1$
					option = Option.ramschSkatOwner;
				}
			}

			if (option == null) {
				continue;
			}

			parseAndSetOptionValue(option, value);
		}
	}

	private void parseAndSetOptionValue(final Option option, final String value) {
		switch (option) {
		case bockEventContraReAnnounced:
			setBockEventContraReAnnounced(Boolean.valueOf(value));
			break;
		case bockEventLostAfterContra:
			setBockEventLostAfterContra(Boolean.valueOf(value));
			break;
		case bockEventLostGrand:
			setBockEventLostGrand(Boolean.valueOf(value));
			break;
		case bockEventLostWith60:
			setBockEventLostWith60(Boolean.valueOf(value));
			break;
		case bockEventPlayerHasX00Points:
			setBockEventPlayerHasX00Points(Boolean.valueOf(value));
			break;
		case cardFace:
			try {
				CardFace cardFace = CardFace.valueOf(value);
				switch (cardFace) {
				case FRENCH:
					setCardSet(CardSet.ISS_FRENCH);
					break;
				case GERMAN:
					setCardSet(CardSet.ISS_GERMAN);
					break;
				case TOURNAMENT:
					setCardSet(CardSet.ISS_TOURNAMENT);
					break;
				}
			} catch (IllegalArgumentException e) {
				// parsing of older options failed
				logEnumParseError(option, getCardSet().name());
			}
			break;
		case cardSet:
			try {
				setCardSet(CardSet.valueOf(value));
			} catch (IllegalArgumentException e) {
				// parsing of older options failed
				logEnumParseError(option, getCardSet().name());
			}
			break;
		case cheatDebugMode:
			setCheatDebugMode(Boolean.valueOf(value));
			break;
		case checkForNewVersionAtStartUp:
			setCheckForNewVersionAtStartUp(Boolean.valueOf(value));
			break;
		case contraAfterBid18:
			break;
		case gameShortCut:
			setGameShortCut(Boolean.valueOf(value));
			break;
		case issAddress:
			setIssAddress(value);
			break;
		case issPort:
			setIssPort(Integer.valueOf(value));
			break;
		case language:
			try {
				setLanguage(SupportedLanguage.valueOf(value));
			} catch (IllegalArgumentException e) {
				// parsing of older options failed
				logEnumParseError(option, getLanguage().name());
			}
			break;
		case maxPlayerCount:
			setMaxPlayerCount(Integer.valueOf(value));
			break;
		case playBock:
			setPlayBock(Boolean.valueOf(value));
			break;
		case playContra:
			setPlayContra(Boolean.valueOf(value));
			break;
		case playRamsch:
			setPlayRamsch(Boolean.valueOf(value));
			break;
		case playRevolution:
			setPlayRevolution(Boolean.valueOf(value));
			break;
		case ramschEventNoBid:
			setRamschEventNoBid(Boolean.valueOf(value));
			break;
		case ramschEventRamschAfterBock:
			setRamschEventRamschAfterBock(Boolean.valueOf(value));
			break;
		case ramschGrandHandPossible:
			setRamschGrandHandPossible(Boolean.valueOf(value));
			break;
		case ramschSkatOwner:
			try {
				setRamschSkatOwner(RamschSkatOwner.valueOf(value));
			} catch (IllegalArgumentException e) {
				// parsing of older options failed
				logEnumParseError(option, getRamschSkatOwner().name());
			}
			break;
		case rules:
			try {
				setRules(RuleSet.valueOf(value));
			} catch (IllegalArgumentException e) {
				// parsing of older options failed
				logEnumParseError(option, getRules().name());
			}
			break;
		case savePath:
			if ("".equals(value)) { //$NON-NLS-1$
				setSavePath(getDefaultSaveDir());
			} else {
				setSavePath(value);
			}
			break;
		case schieberRamsch:
			setSchieberRamsch(Boolean.valueOf(value));
			break;
		case schieberRamschJacksInSkat:
			setSchieberRamschJacksInSkat(Boolean.valueOf(value));
			break;
		case showTipsAtStartUp:
			setShowTipsAtStartUp(Boolean.valueOf(value));
			break;
		case trickRemoveAfterClick:
			setTrickRemoveAfterClick(Boolean.valueOf(value));
			break;
		case trickRemoveDelayTime:
			setTrickRemoveDelayTime(Integer.valueOf(value));
			break;
		}
	}

	private static void logEnumParseError(final Option option,
			final String defaultValue) {
		log.warn("Parsing of option " + option.name() + " failed. Using default value: " + defaultValue); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Gets the language code for the language
	 * 
	 * @return Language code
	 */
	public String getI18NCode() {

		String result = "en"; //$NON-NLS-1$

		if (SupportedLanguage.GERMAN.equals(getLanguage())) {
			result = "de"; //$NON-NLS-1$
		}

		return result;
	}

	private void setOption(final Option option, final Boolean value) {
		options.setProperty(option.name(), value.toString());
	}

	private void setOption(final Option option, final CardSet value) {
		options.setProperty(option.name(), value.name());
	}

	private void setOption(final Option option, final Integer value) {
		options.setProperty(option.name(), value.toString());
	}

	private void setOption(final Option option, final RamschSkatOwner value) {
		options.setProperty(option.name(), value.name());
	}

	private void setOption(final Option option, final String value) {
		options.setProperty(option.name(), value);
	}

	private void setOption(final Option option, final SupportedLanguage value) {
		options.setProperty(option.name(), value.name());
	}

	/**
	 * Sets the standard properties
	 * 
	 */
	void setDefaultProperties(final SavePathResolver pathResolver) {

		setOption(Option.showTipsAtStartUp, Boolean.TRUE);
		setOption(Option.language, getDefaultLanguage().name());
		setOption(Option.checkForNewVersionAtStartUp, Boolean.FALSE);
		setOption(Option.savePath, pathResolver.getDefaultSavePath());
		setOption(Option.cardSet, CardSet.ISS_TOURNAMENT.name());
		setOption(Option.trickRemoveDelayTime, 2000);
		setOption(Option.trickRemoveAfterClick, Boolean.FALSE);
		setOption(Option.gameShortCut, Boolean.FALSE);
		setOption(Option.cheatDebugMode, Boolean.FALSE);
		setOption(Option.maxPlayerCount, Integer.valueOf(3));
		setOption(Option.rules, RuleSet.ISPA.name());
		setOption(Option.playContra, Boolean.TRUE);
		setOption(Option.contraAfterBid18, Boolean.TRUE);
		setOption(Option.playBock, Boolean.TRUE);
		setOption(Option.bockEventLostGrand, Boolean.TRUE);
		setOption(Option.bockEventLostWith60, Boolean.TRUE);
		setOption(Option.bockEventLostAfterContra, Boolean.TRUE);
		setOption(Option.bockEventContraReAnnounced, Boolean.FALSE);
		setOption(Option.bockEventPlayerHasX00Points, Boolean.FALSE);
		setOption(Option.playRamsch, Boolean.TRUE);
		setOption(Option.playRevolution, Boolean.FALSE);
		setOption(Option.ramschSkatOwner, RamschSkatOwner.LAST_TRICK.name());
		setOption(Option.schieberRamsch, Boolean.TRUE);
		setOption(Option.schieberRamschJacksInSkat, Boolean.FALSE);
		setOption(Option.ramschEventNoBid, Boolean.TRUE);
		setOption(Option.ramschEventRamschAfterBock, Boolean.FALSE);
		setOption(Option.ramschGrandHandPossible, Boolean.TRUE);
		setOption(Option.issAddress, "skatgame.net"); //$NON-NLS-1$
		setOption(Option.issPort, Integer.valueOf(7000));
	}

	/**
	 * Sets the flag for showing the welcome dialog with first steps at startup
	 * 
	 * @param isShowTips
	 *            TRUE, if first steps should be shown
	 */
	public void setShowTipsAtStartUp(final Boolean isShowTips) {
		setOption(Option.showTipsAtStartUp, isShowTips);
	}
}
