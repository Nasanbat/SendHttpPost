package httpPost;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class httpPost {

	public static void main(String[] args) throws UnsupportedEncodingException {
		// TODO Auto-generated method stub
		String url = "https://stackoverflow.com/";
		String contentType = "application/x-www-form-urlencoded; charset=UTF-8";
		HashMap<String, String> headerParams = new HashMap<String, String>();
		headerParams.put("key1", "value1");
		byte[] outputBytes = null;
		int connectTimeout = 3;
		int readTimeout = 60;
		String in = "Send Data";
		outputBytes = in.getBytes("UTF-8");
		String str = sendHttpPost(url, contentType, headerParams, outputBytes, connectTimeout, readTimeout);

		System.out.print("res = " + str);
	}

	/**
	 * HTTP Post method-оор хүсэлт шидэж, ирсэн хариуг буцаана
	 * 
	 * @param url            URL
	 * @param contenType     Контентын төрөл
	 * @param headerParams   Толгой утгууд
	 * @param outputBytes    Хүсэлтийн бие
	 * @param connectTimeout Холбогдохыг хүлээх хугацаа
	 * @param readTimeout    Хариу хүлээх хугацаа
	 * @return Хүсэлтийн хариу эсвэл алдааны мэдээлэл
	 */
	public static String sendHttpPost(String url, String contentType, HashMap<String, String> headerParams,
			byte[] outputBytes, int connectTimeout, int readTimeout) {
		String strResponse = "";

		HttpURLConnection con = null;

		try {

			URL u = new URL(url);

			if (url.contains("https://")) {
				try {
					TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
						public X509Certificate[] getAcceptedIssuers() {
							return new X509Certificate[0];
						}

						@Override
						public void checkClientTrusted(X509Certificate[] chain, String authType)
								throws CertificateException {
							// Do nothing
						}

						@Override
						public void checkServerTrusted(X509Certificate[] arg0, String arg1)
								throws CertificateException {
							// Do nothing
						}
					} };

					HostnameVerifier hv = (hostname, session) -> hostname.equalsIgnoreCase(session.getPeerHost());

					SSLContext sc = SSLContext.getInstance("SSL");
					sc.init(null, trustAllCerts, new SecureRandom());

					HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
					HttpsURLConnection.setDefaultHostnameVerifier(hv);

				} catch (Exception e) {

				}
			}

			con = (HttpURLConnection) u.openConnection();
			con.setRequestProperty("Connection", "close");
			if (connectTimeout > 0) {
				con.setConnectTimeout(connectTimeout);
			}

			if (readTimeout > 0)
				con.setReadTimeout(readTimeout);

			// Set headers
			con.setRequestMethod("POST");
			if (contentType != null) {
				con.setRequestProperty("Content-Type", contentType);
			}
			if (outputBytes != null) {
				con.setRequestProperty("Content-Length", "" + outputBytes.length);
			}

			if (headerParams != null) {
				Set<String> keys = headerParams.keySet();
				for (String key : keys) {
					con.setRequestProperty(key, headerParams.get(key));
				}
			}
			con.setDoOutput(true);
			try (OutputStream os = con.getOutputStream()) {
				if (outputBytes == null) {
					strResponse = "Мэдээлэл хоосон байна!";
					return strResponse;
				}
				os.write(outputBytes);
				os.flush();
			}

			// Call
			int responseCode = con.getResponseCode();

			try (ByteArrayOutputStream outStream = new ByteArrayOutputStream()) {
				final int BUFFER_SIZE = 1024;
				byte[] buffer = new byte[BUFFER_SIZE];
				int len;

				if (responseCode >= 200 && 300 > responseCode) {
					try (DataInputStream is = new DataInputStream(con.getInputStream())) {
						while (-1 != (len = is.read(buffer))) {
							outStream.write(buffer, 0, len);
						}
						strResponse = outStream.toString("UTF-8");
					}
				} else {
					try (DataInputStream is = new DataInputStream(con.getErrorStream())) {
						while (-1 != (len = is.read(buffer))) {
							outStream.write(buffer, 0, len);
						}
						strResponse = outStream.toString("UTF-8");
					}
				}
			}
		} catch (SocketTimeoutException se) {
			if (se.getMessage() != null) {
				if (se.getMessage().equalsIgnoreCase("connect timed out")) {
					strResponse = "connect timed out";
				} else if (se.getMessage().equalsIgnoreCase("Read timed out")) {
					strResponse = "Read timed out";
				} else {
					strResponse = se.getMessage();
				}
			} else {
				strResponse = se.toString();
			}
		} catch (Exception e) {
			strResponse = e.getMessage();
		} finally {
			if (null != con) {
				con.disconnect();
			}
		}

		return strResponse;

	}
}
