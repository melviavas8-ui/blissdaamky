package dev.daamky;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.imageio.ImageIO;

public final class CaptchaSolver {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private static volatile String storedApiKey = "GjvpaVusIevOpsDcwPXfnHDIl";
   private static volatile String storedServiceBaseUrl = "http://5.42.211.111/";
   private static final int connectTimeoutMillis = 10000;
   private static final int readTimeoutMillis = 30000;
   private static final long pollIntervalMillis = 500L;
   private static final long pollTimeoutMillis = 120000L;
// https://t.me/pasterplace

   private CaptchaSolver() {
   }
// https://t.me/pasterplace

   public static void setApiKey(String configuredApiKey) {
      if (configuredApiKey != null && !configuredApiKey.isBlank()) {
         storedApiKey = configuredApiKey.trim();
      }
   }
// https://t.me/pasterplace

   public static String getApiKey() {
      return storedApiKey;
   }
// https://t.me/pasterplace

   public static void setServiceBaseUrl(String configuredServiceBaseUrl) {
      if (configuredServiceBaseUrl != null && !configuredServiceBaseUrl.isBlank()) {
         storedServiceBaseUrl = configuredServiceBaseUrl.endsWith("/") ? configuredServiceBaseUrl : configuredServiceBaseUrl + "/";
      }
   }
// https://t.me/pasterplace

   public static String solveImageCaptcha(BufferedImage captchaImage) throws Exception {
      String apiKey = storedApiKey;
      String serviceBaseUrl = storedServiceBaseUrl;
      long startedAtMillis = System.currentTimeMillis();
      String encodedCaptchaImage = encodePngAsBase64(captchaImage);
      String submissionFormBody = "key=" + encodeFormValue(apiKey) + "&method=base64&body=" + encodeFormValue(encodedCaptchaImage);
      String submissionResponse = postForm("http://5.42.211.111/in.php", submissionFormBody);
      BotCaptchaLog.II1Il11l111II11IIl("  in.php (img b64=" + encodedCaptchaImage.length() + ") -> " + BotCaptchaLog.l1I1IlllIlI(submissionResponse));
      if (submissionResponse != null && submissionResponse.startsWith("OK|")) {
         String[] submissionFields = submissionResponse.split("\\|", 2);
         if (submissionFields.length >= 2 && !submissionFields[1].isBlank()) {
            String captchaJobId = submissionFields[1].trim();
            String resultUrl = serviceBaseUrl + "res.php?key=" + encodeFormValue(apiKey) + "&action=get&id=" + encodeFormValue(captchaJobId);
            long pollDeadlineMillis = startedAtMillis + 120000L;
            int pollCount = 0;

            while (System.currentTimeMillis() < pollDeadlineMillis) {
               Thread.sleep(500L);
               String pollResponse = getResponseText(resultUrl);
               pollCount++;
               if (pollResponse != null) {
                  if (pollResponse.startsWith("OK|")) {
                     String[] resultFields = pollResponse.split("\\|", 2);
                     String captchaAnswer = resultFields.length < 2 ? null : resultFields[1];
                     BotCaptchaLog.II1Il11l111II11IIl("  res.php answer='" + captchaAnswer + "' polls=" + pollCount + " took=" + (System.currentTimeMillis() - startedAtMillis) + "ms");
                     return captchaAnswer;
                  }
// https://t.me/pasterplace

                  if (!pollResponse.contains("CAPCHA_NOT_READY")) {
                     BotCaptchaLog.II1Il11l111II11IIl("  res.php error poll#" + pollCount + " -> " + BotCaptchaLog.l1I1IlllIlI(pollResponse));
                     return null;
                  }
               }
            }

            BotCaptchaLog.II1Il11l111II11IIl("  res.php TIMEOUT polls=" + pollCount + " after " + (System.currentTimeMillis() - startedAtMillis) + "ms");
            return null;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }
// https://t.me/pasterplace

   private static String encodePngAsBase64(BufferedImage image) throws Exception {
      ByteArrayOutputStream pngBytes = new ByteArrayOutputStream();
      ImageIO.write(image, "png", pngBytes);
      return Base64.getEncoder().encodeToString(pngBytes.toByteArray());
   }
// https://t.me/pasterplace

   private static String encodeFormValue(String value) {
      return URLEncoder.encode(value, StandardCharsets.UTF_8);
   }
// https://t.me/pasterplace

   private static String postForm(String requestUrl, String formBody) throws Exception {
      HttpURLConnection connection = openHttpConnection(requestUrl);
      connection.setRequestMethod("POST");
      connection.setDoOutput(true);

      try (OutputStream requestBodyStream = connection.getOutputStream()) {
         requestBodyStream.write(formBody.getBytes(StandardCharsets.UTF_8));
      }

      return readResponseBody(connection);
   }
// https://t.me/pasterplace

   private static String getResponseText(String requestUrl) throws Exception {
      HttpURLConnection connection = openHttpConnection(requestUrl);
      connection.setRequestMethod("GET");
      return readResponseBody(connection);
   }
// https://t.me/pasterplace

   private static HttpURLConnection openHttpConnection(String requestUrl) throws Exception {
      HttpURLConnection connection = (HttpURLConnection)URI.create(requestUrl).toURL().openConnection();
      connection.setConnectTimeout(10000);
      connection.setReadTimeout(30000);
      return connection;
   }
// https://t.me/pasterplace

   private static String readResponseBody(HttpURLConnection connection) throws Exception {
      try {
         int statusCode = connection.getResponseCode();
         InputStream responseStream = statusCode >= 400 ? connection.getErrorStream() : connection.getInputStream();
         if (responseStream == null) {
            return null;
         }

         StringBuilder responseTextBuilder = new StringBuilder();

         try (InputStream inputStream = responseStream) {
            byte[] readBuffer = new byte[1024];

            int bytesRead;
            while ((bytesRead = inputStream.read(readBuffer)) != -1) {
               responseTextBuilder.append(new String(readBuffer, 0, bytesRead, StandardCharsets.UTF_8));
            }
         }

         return statusCode >= 400 ? null : responseTextBuilder.toString();
      } finally {
         connection.disconnect();
      }
   }
}

