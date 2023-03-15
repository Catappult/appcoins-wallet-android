package com.asfoundation.wallet.util;

import android.text.TextUtils;
import androidx.annotation.NonNull;
import com.appcoins.wallet.commons.Logger;
import com.appcoins.wallet.core.utils.common.Log;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import org.jetbrains.annotations.NotNull;

public class LogInterceptor implements Interceptor {
  private static final String TAG = "HTTP_TRACE";
  private static final Charset UTF8 = StandardCharsets.UTF_8;

  // Dao is being used directly because otherwise we'll have a cyclic dependency with Repository
  //private LogsDao logsDao;
  @Inject Logger logger;

  public @Inject LogInterceptor() {
    //this.logger = logger;
  }

  private static String requestPath(HttpUrl url) {
    String path = url.encodedPath();
    String query = url.encodedQuery();
    return url.scheme() + "://" + url.host() + (query != null ? (path + '?' + query) : path);
  }

  @NotNull @Override public Response intercept(@NonNull Chain chain) throws IOException {
    StringBuilder logBuilder = new StringBuilder();
    Request request = chain.request();
    try {
      RequestBody requestBody = request.body();
      logBuilder.append(
          "<---------------------------BEGIN REQUEST---------------------------------->");
      logBuilder.append("\n");
      logBuilder.append("Request encoded url: ")
          .append(request.method())
          .append(" ")
          .append(requestPath(request.url()));
      logBuilder.append("\n");
      String decodeUrl = requestDecodedPath(request.url());
      if (!TextUtils.isEmpty(decodeUrl)) {
        logBuilder.append("Request decoded url: ")
            .append(request.method())
            .append(" ")
            .append(decodeUrl);
      }

      Headers headers = request.headers();
      logBuilder.append("\n=============== Headers ===============\n");
      for (int i = headers.size() - 1; i > -1; i--) {
        logBuilder.append(headers.name(i))
            .append(" : ")
            .append(headers.get(headers.name(i)))
            .append("\n");
      }
      logBuilder.append("\n=============== END Headers ===============\n");

      if (requestBody != null) {
        String requestSting = formatRequestBody(requestBody);
        logBuilder.append(requestSting);
      }
      long startNs = System.nanoTime();
      Response response = chain.proceed(request);
      long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);

      ResponseBody responseBody = response.body();
      logBuilder.append("\n");
      logBuilder.append("Response timeout: ")
          .append(tookMs)
          .append("ms");
      logBuilder.append("\n");
      logBuilder.append("Response message: ")
          .append(response.message());
      logBuilder.append("\n");
      logBuilder.append("Response code: ")
          .append(response.code());

      if (responseBody != null) {
        String responseString = formatResponseBody(responseBody);
        if (responseBody.contentLength() != 0) {
          logBuilder.append("\n");
          logBuilder.append("Response body: \n")
              .append(responseString);
        }
      }

      headers = response.headers();
      logBuilder.append("\n=============== Headers ===============\n");
      for (int i = headers.size() - 1; i > -1; i--) {
        logBuilder.append(headers.name(i))
            .append(" : ")
            .append(headers.get(headers.name(i)))
            .append("\n");
      }
      logBuilder.append("\n=============== END Headers ===============\n");

      logBuilder.append("\n");
      logBuilder.append(
          "<-----------------------------END REQUEST--------------------------------->");
      logBuilder.append("\n\n\n");
      Log.d(TAG, logBuilder.toString());

      if (response.code() >= 400) {
        sendErrorLogString(response, request);
      }
      return response;
    } catch (Exception exception) {
      logBuilder = new StringBuilder();
      logBuilder.append("Failed request url: ")
          .append(request.method())
          .append(" ")
          .append(requestPath(request.url()));
      Log.e(TAG, logBuilder.toString());
      throw exception;
    }
  }

  private void sendErrorLogString(Response response, Request request) throws IOException {
    StringBuilder logBuilder = new StringBuilder();
    logBuilder.append("HTTP ")
        .append(response.code())
        .append(" ")
        .append(request.method())
        .append(" ")
        .append(request.url())
        .append("\n")

        .append("Message: ")
        .append(response.message())
        .append("\n")

        .append("Request: ")
        .append(formatRequestBody(response.request().body()))
        .append("\n")

        .append("Response: ")
        .append(formatResponseBody(response.body()))
        .append("\n");

    logger.log("HTTP " + response.code(), logBuilder.toString(), true, true);
  }

  private String formatResponseBody(ResponseBody responseBody) throws IOException {
    if (responseBody != null) {
      BufferedSource source = responseBody.source();
      source.request(Long.MAX_VALUE);
      Buffer buffer = source.getBuffer();
      Charset charset = null;
      MediaType contentType = responseBody.contentType();
      if (contentType != null) {
        charset = contentType.charset(UTF8);
      }
      if (charset == null) {
        charset = UTF8;
      }
      return buffer.clone().readString(charset);
    } else {
      return "";
    }
  }

  private String formatRequestBody(RequestBody requestBody) throws IOException {
    if (requestBody != null) {
      Buffer buffer = new Buffer();
      requestBody.writeTo(buffer);

      MediaType contentType = requestBody.contentType();
      if (contentType != null) {
        contentType.charset(UTF8);
      }

      return buffer.readString(UTF8);
    }
    return "";
  }

  private String requestDecodedPath(HttpUrl url) {
    try {
      String path = URLDecoder.decode(url.encodedPath(), "UTF-8");
      String query = URLDecoder.decode(url.encodedQuery(), "UTF-8");
      return url.scheme() + "://" + url.host() + (query != null ? (path + '?' + query) : path);
    } catch (Exception ex) {
      /* Quality */
    }
    return null;
  }
}