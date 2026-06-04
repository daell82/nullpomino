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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Getter;
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


	/** Constant state count */
	public enum Status {
		INACTIVE, LOADING, COMPLETE, ERROR;
	}

	/** Current State */
	@Getter
	private volatile Status status = Status.INACTIVE;

	/** event Listener */
	private final List<UpdateCheckerListener> listeners = new LinkedList<>();

	/** Update information has been writtenXMLOfURL */
	private String strURLofXML = null;

	/** The latest version ofVersion number */
	@Getter
	private Version latestVersion = Version.of("0");

	/** Release Date */
	@Getter
	private String releaseDate = null;

	/** DownloadURL */
	@Getter
	private String downloadURL = null;

	/** URL of Installer (*.exe) for Windows */
	@Getter
	private String windowsInstallerURL = null;

	/**
	 * XMLDownload theVersion numberAcquisition and
	 *
	 * @return true if successful
	 */
	private Status checkUpdate() {
		URL url;
		try {
			url = new URI(strURLofXML).toURL();
		} catch (URISyntaxException | MalformedURLException e) {
			log.error("invalid URL", e);
			return Status.ERROR;
		}
		try (var httpIn = new BufferedReader(new InputStreamReader(url.openStream()))) {
			String line;
			while ((line = httpIn.readLine()) != null) {
				checkTag(line, TAG_VERSION).ifPresent(version -> latestVersion = Version.of(version.replace('_', '.')));
				checkTag(line, TAG_DATE).ifPresent(date -> releaseDate = date);
				checkTag(line, TAG_DOWNLOAD_URL).ifPresent(value -> downloadURL = value);
				checkTag(line, TAG_INSTALLER).ifPresent(value -> windowsInstallerURL = value);
			}
		} catch (Exception e) {
			log.error("Failed to get latest version data", e);
			return Status.ERROR;
		}
		return Status.COMPLETE;
	}

	private static Optional<String> checkTag(String data, Pattern pattern) {
		Matcher matcher = pattern.matcher(data);
		if (matcher.find()) {
			return Optional.of(matcher.group("content"));
		}
		return Optional.ofNullable(null);
	}

	/**
	 * Current versionThan the latest version ofVersionWho will determine whether
	 * the new
	 *
	 * @param nowMajor Current MajorVersion
	 * @param nowMinor Current MinorVersion
	 * @return The latest edition of the new and bettertrue
	 */
	public boolean isNewVersionAvailable() {
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
	public void startCheckForUpdates(String strURL) {
		if (strURL == null || strURL.isEmpty()) {
			strURLofXML = DEFAULT_XML_URL;
		} else {
			strURLofXML = strURL;
		}
		Thread thread = new Thread(this);
		thread.setDaemon(true);
		thread.start();
	}

	/**
	 * @return Thread is running(Loading)Iftrue
	 */
	public boolean isRunning() {
		return status == Status.LOADING;
	}

	/**
	 * @return Completed readingtrue
	 */
	public boolean isCompleted() {
		return status == Status.COMPLETE;
	}

	/**
	 * event Adds a listener(Nothing happens and another has been added)
	 *
	 * @param l Add event Listener
	 */
	public void addListener(UpdateCheckerListener l) {
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
	public boolean removeListener(UpdateCheckerListener l) {
		return listeners.remove(l);
	}

	/*
	 * Update check Processing of the thread
	 */
	@Override
	public void run() {
		// Start
		status = Status.LOADING;
		for (UpdateCheckerListener listener : listeners) {
			listener.onUpdateCheckerStart();
		}

		// Update check
		status = checkUpdate();

		// End
		for (UpdateCheckerListener listener : listeners) {
			listener.onUpdateCheckerEnd(status);
		}
	}
}
