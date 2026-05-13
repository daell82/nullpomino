/*
    Copyright (c) 2010, NullNoname
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions are met:

        * Redistributions of source code must retain the above copyright
          notice, this list of conditions and the following disclaimer.
        * Redistributions in binary form must reproduce the above copyright
          notice, this list of conditions and the following disclaimer in the
          documentation and/or other materials provided with the distribution.
        * Neither the name of NullNoname nor the names of its
          contributors may be used to endorse or promote products derived from
          this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
    AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
    IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
    ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
    LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
    CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
    SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
    INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
    CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
    ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
    POSSIBILITY OF SUCH DAMAGE.
*/
package mu.nu.nullpo.gui.net;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.log4j.Log4j;
import mu.nu.nullpo.game.types.Version;

/**
 * NewVersionChecker
 */
@Log4j
public class UpdateChecker implements Runnable {

	/** default のXMLのURL */
	/*
	 * TODO: Find an actual place to put the NullpoUpdate.xml file, possible on
	 * github pages. For now, just use the v7.5.0 file as a classpath resource.
	 */
	public static final String DEFAULT_XML_URL = UpdateChecker.class.getResource("NullpoUpdate.xml").toString();

	private static final Pattern TAG_VERSION = Pattern.compile("<Version>(?<content>.*)</Version>");
	private static final Pattern TAG_DATE = Pattern.compile("<Date>(?<content>.*)</Date>");
	private static final Pattern TAG_DOWNLOAD_URL = Pattern.compile("<DownloadURL>(?<content>.*)</DownloadURL>");
	private static final Pattern TAG_INSTALLER = Pattern.compile("<WindowsInstallerURL>(?<content>.*)</WindowsInstallerURL>");


	/** Constant statecount */
	public static final int STATUS_INACTIVE = 0;
	public static final int STATUS_LOADING = 1;
	public static final int STATUS_ERROR = 2;
	public static final int STATUS_COMPLETE = 3;

	/** Current State */
	private static volatile int status = 0;

	/** event Listener */
	private static final List<UpdateCheckerListener> listeners = new LinkedList<>();

	/** Update information has been writtenXMLOfURL */
	private static String strURLofXML = null;

	/** The latest version ofVersion number */
	private static Version latestVersion = Version.of("0");

	/** Release Date */
	private static String releaseDate = null;

	/** DownloadURL */
	private static String downloadURL = null;

	/** Installer for Windows URL */
	private static String windowsInstallerURL = null;

	/**
	 * XMLDownload theVersion numberAcquisition and
	 *
	 * @return true if successful
	 */
	private static boolean checkUpdate() {
		try {
			URL url = new URI(strURLofXML).toURL();
			var httpCon = url.openStream();
			BufferedReader httpIn = new BufferedReader(new InputStreamReader(httpCon));

			String line;
			while ((line = httpIn.readLine()) != null) {
				checkTag(line, TAG_VERSION).ifPresent(version -> latestVersion = Version.of(version.replace('_', '.')));
				checkTag(line, TAG_DATE).ifPresent(date -> releaseDate = date);
				checkTag(line, TAG_DOWNLOAD_URL).ifPresent(value -> downloadURL = value);
				checkTag(line, TAG_INSTALLER).ifPresent(value -> windowsInstallerURL = value);
			}

			httpIn.close();
		} catch (Exception e) {
			log.error("Failed to get latest version data", e);
			return false;
		}
		return true;
	}

	private static Optional<String> checkTag(String data, Pattern pattern) {
		Matcher matcher = pattern.matcher(data);
		if (matcher.find()) {
			return Optional.of(matcher.group("content"));
		}
		return Optional.ofNullable(null);
	}

	/**
	 * The latest version ofVersion numberOfStringGets the type representation
	 *
	 * @return The latest version ofVersion numberOfStringType
	 *         representation("7.0.0"Such as)
	 */
	public static String getLatestVersionFullString() {
		return latestVersion.toString();
	}

	/**
	 * Current versionThan the latest version ofVersionWho will determine whether
	 * the new
	 *
	 * @param nowMajor Current MajorVersion
	 * @param nowMinor Current MinorVersion
	 * @return The latest edition of the new and bettertrue
	 */
	public static boolean isNewVersionAvailable() {
		if (!isCompleted() || latestVersion == null) {
			return false;
		}
		return latestVersion.compareTo(Version.getCurrent()) > 0;
	}

	/**
	 * Version check
	 *
	 * @param strURL Latest information entersXMLIn the fileURL(nullWhen I or an
	 *               empty string default Using the value)
	 */
	public static void startCheckForUpdates(String strURL) {
		if (strURL == null || strURL.isEmpty()) {
			strURLofXML = DEFAULT_XML_URL;
		} else {
			strURLofXML = strURL;
		}
		Thread thread = new Thread(new UpdateChecker());
		thread.setDaemon(true);
		thread.start();
	}

	/**
	 * @return Thread is running(Loading)Iftrue
	 */
	public static boolean isRunning() {
		return status == STATUS_LOADING;
	}

	/**
	 * @return Completed readingtrue
	 */
	public static boolean isCompleted() {
		return status == STATUS_COMPLETE;
	}

	/**
	 * Current Gets the state
	 *
	 * @return Current State
	 */
	public static int getStatus() {
		return status;
	}

	/**
	 * Gets the date on which the latest version has been released
	 *
	 * @return Sun has released the latest version
	 */
	public static String getReleaseDate() {
		return releaseDate;
	}

	/**
	 * Where to download the latest versionURLGet the
	 *
	 * @return Where to download the latest versionURL
	 */
	public static String getDownloadURL() {
		return downloadURL;
	}

	/**
	 * Get the URL of Installer (*.exe) for Windows
	 *
	 * @return URL of Installer (*.exe) for Windows
	 */
	public static String getWindowsInstallerURL() {
		return windowsInstallerURL;
	}

	/**
	 * event Adds a listener(Nothing happens and another has been added)
	 *
	 * @param l Add event Listener
	 */
	public static void addListener(UpdateCheckerListener l) {
		if (listeners.contains(l)) {
			return;
		}
		listeners.add(l);
	}

	/**
	 * event Removes a listener
	 *
	 * @param l Remove event Listener
	 * @return Has been deletedtrue, It has not been registered from the
	 *         beginningfalse
	 */
	public static boolean removeListener(UpdateCheckerListener l) {
		return listeners.remove(l);
	}

	/*
	 * Update check Processing of the thread
	 */
	@Override
	public void run() {
		// Start
		status = STATUS_LOADING;
		for (UpdateCheckerListener l : listeners) {
			l.onUpdateCheckerStart();
		}

		// Update check
		if (checkUpdate()) {
			status = STATUS_COMPLETE;
		} else {
			status = STATUS_ERROR;
		}

		// End
		for (UpdateCheckerListener l : listeners) {
			l.onUpdateCheckerEnd(status);
		}
	}
}
