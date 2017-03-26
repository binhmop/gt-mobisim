// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class FileHelper {
  public static int timeout = 20 * 1000; // [ms]

  public static String getContentsFromInputStream(InputStream in) throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
    StringBuffer strBuffer = new StringBuffer();

    String line;
    while ((line = reader.readLine()) != null) {
      strBuffer.append(line);
      strBuffer.append(System.getProperty("line.separator"));
    }

    reader.close();
    return strBuffer.toString();
  }

  public static InputStream openFileOrUrl(String urlString) throws IOException {
    URL url;
    try {
      url = new URL(urlString);
    } catch (MalformedURLException e) {
      return new FileInputStream(urlString);
    }

    URLConnection conn = url.openConnection();
    conn.setConnectTimeout(timeout);
    conn.setReadTimeout(timeout);
    return conn.getInputStream();
  }

  public static boolean isNonEmptyFileOrUrl(String urlString) {
    try {
      InputStream in = FileHelper.openFileOrUrl(urlString);
      int b = in.read();
      in.close();
      return (b != -1);
    } catch (IOException e) {
      return false;
    }
  }

  public static URL getAsUrl(String urlString) throws IOException {
    try {
      return new URL(urlString);
    } catch (MalformedURLException e) {
      return new File(urlString).toURI().toURL();
    }
  }
}
