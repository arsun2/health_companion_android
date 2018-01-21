/**
 * Source: David Gassner, Android App Development: RESTful Web Services
 * https://gist.github.com/davidgassner/71734d7ab5703cbd34c094de5add7819
 */

package com.example.health_companion_uiuc_mobile.utils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * helper class handling content downloading
 */
public class HttpHelper {

    /**
     * send request to download content from the given url
     * @param url the url address to download content from;
     */
    public static String downloadUrl(String url)
            throws IOException {
        RequestPackage requestPackage = new RequestPackage();
        requestPackage.setEndPoint(url);
        return downloadFromFeed(requestPackage);
    }

    /**
     * send request to download content using the request package
     * @param requestPackage the request package containing all information for the http request
     */
    public static String downloadFromFeed(RequestPackage requestPackage)
            throws IOException {

        String address = requestPackage.getEndpoint();
        String encodedParams = requestPackage.getEncodedParams();

        if (requestPackage.getMethod().equals("GET") &&
                encodedParams.length() > 0) {
            address = String.format("%s?%s", address, encodedParams);
        }

        InputStream is = null;
        try {
            URL url = new URL(address);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod(requestPackage.getMethod());
            conn.setDoInput(true);

            if (requestPackage.getMethod().equals("POST") &&
                    encodedParams.length() > 0) {
                conn.setDoOutput(true);
                OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                writer.write(requestPackage.getEncodedParams());
                writer.flush();
                writer.close();
            }
            conn.connect();

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                throw new IOException("Got response code " + responseCode);
            }
            is = conn.getInputStream();
            return readStream(is);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                is.close();
            }
        }
        return null;
    }

    /**
     * transform InputStream into String
     * @param stream the inputStream that needs to be transformed into String
     */
    private static String readStream(InputStream stream) throws IOException {

        byte[] buffer = new byte[1024];
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        BufferedOutputStream out = null;
        try {
            int length = 0;
            out = new BufferedOutputStream(byteArray);
            while ((length = stream.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
            out.flush();
            return byteArray.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

}
