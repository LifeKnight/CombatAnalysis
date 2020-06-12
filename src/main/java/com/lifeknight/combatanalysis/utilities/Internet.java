package com.lifeknight.combatanalysis.utilities;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

public class Internet {
	
	public static void openWebsiteOnDefaultBrowser(String website) throws Exception {
		if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
			try {
				Desktop.getDesktop().browse(new URI(website));
			} catch (Exception e) {
				throw new Exception();
			}
		}
	}
	
	public static boolean websiteExists(String website) throws IOException {
		final URL url = new URL(website);
        HttpURLConnection.setFollowRedirects(false);
        final HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
        httpURLConnection.setRequestMethod("HEAD");
        httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US; rv:1.9.1.2) Gecko/20090729 Firefox/3.5.2 (.NET CLR 3.5.30729)");
        final int responseCode = httpURLConnection.getResponseCode();
        return responseCode == 200;
    }
	
	public static String getWebsiteContent(final String website) throws Exception {
		try {
			URLConnection connection = new URL(website).openConnection();
			connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
			connection.connect();

			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

			String line;

			StringBuilder sb = new StringBuilder();

			while ((line = reader.readLine()) != null) {
				sb.append(line);
				sb.append("\n");
			}

			return sb.toString();
		} catch (Exception e) {
			throw new Exception(e);
		}
	}
}
