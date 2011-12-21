package org.jskat.util.version;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.jskat.data.JSkatOptions;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Checks the version of JSkat
 */
public class VersionChecker {

	/**
	 * Gets the latest version of JSkat from JSkat website
	 * 
	 * @return Latest version
	 */
	public static String getLatestVersion() {

		String result = ""; //$NON-NLS-1$

		try {
			VersionHandler handler = new VersionHandler();
			XMLReader xmlReader = XMLReaderFactory.createXMLReader();
			xmlReader.setContentHandler(handler);
			xmlReader.parse(new InputSource(new URL("http://jskat.org/pad/jskat.xml").openStream())); //$NON-NLS-1$
			result = handler.versionString;
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * Checks whether a new version is available or not
	 * 
	 * @param remoteVersion
	 *            Remote JSkat version from the JSkat website
	 * 
	 * @return TRUE, if a new version is available
	 */
	public static boolean isHigherVersionAvailable(String remoteVersion) {
		boolean result = false;

		List<Integer> localVersionParts = getVersionParts(JSkatOptions.getVersion());
		List<Integer> remoteVersionParts = getVersionParts(remoteVersion);

		int index = 0;
		for (Integer localVersionPart : localVersionParts) {
			if (remoteVersionParts.size() > index) {
				if (localVersionPart.intValue() < remoteVersionParts.get(index).intValue()) {
					result = true;
				}
			}
			index++;
		}

		return result;
	}

	private static List<Integer> getVersionParts(String version) {
		List<Integer> result = new ArrayList<Integer>();
		StringTokenizer token = new StringTokenizer(version, "."); //$NON-NLS-1$
		while (token.hasMoreTokens()) {
			result.add(Integer.valueOf(token.nextToken()));
		}
		return result;
	}
}