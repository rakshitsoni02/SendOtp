package com.msg91.sendotp.library.internal;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApiService {
  String mAppKey;
  Context context;
  public static final MediaType JSON
      = MediaType.parse("application/json; charset=utf-8");

  public ApiService(String appKey, Context context) {
    mAppKey = appKey;
    this.context = context;
  }

  public Response generateRequest(String mobileNumber, String countryCode) {

    Uri.Builder builder = new Uri.Builder();
    builder.scheme("http")
        .authority("54.169.180.68:8080")
        .appendPath("SendOTP-API")
        .appendPath("generateOTP");
    JSONObject jsonObject = new JSONObject();
    try {
      jsonObject.put("countryCode", countryCode);
      jsonObject.put("mobileNumber", mobileNumber);
    } catch (JSONException e) {
      e.printStackTrace();
    }
    String url = builder.build().toString();
    return getResponse(url, jsonObject);
  }

  public Response verifyRequest(String mobileNumber, String countryCode, String oneTimePassword) {

    Uri.Builder builder = new Uri.Builder();
    builder.scheme("http")
        .authority("54.169.180.68:8080")
        .appendPath("SendOTP-API")
        .appendPath("verifyOTP");
    JSONObject jsonObject = new JSONObject();
    try {
      jsonObject.put("oneTimePassword", oneTimePassword);
      jsonObject.put("countryCode", countryCode);
      jsonObject.put("mobileNumber", mobileNumber);
    } catch (JSONException e) {
      e.printStackTrace();
    }
    String url = builder.build().toString();
    return getResponse(url, jsonObject);
  }


  private String getDeviceId() {
    return Settings.Secure.getString(context.getContentResolver(),
        Settings.Secure.ANDROID_ID);
  }

  private String getSecretKey() {
    MessageDigest md = null;
    try {
      PackageInfo info = context.getPackageManager().getPackageInfo(
          context.getPackageName(),
          PackageManager.GET_SIGNATURES);
      for (Signature signature : info.signatures) {
        md = MessageDigest.getInstance("SHA");
        md.update(signature.toByteArray());
        //Log.e("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
      }
    } catch (PackageManager.NameNotFoundException e) {

    } catch (NoSuchAlgorithmException e) {

    }
    return Base64.encodeToString(md.digest(), Base64.DEFAULT);

  }

  private Response getResponse(String url, JSONObject json) {
    RequestBody body = RequestBody.create(JSON, String.valueOf(json));
    OkHttpClient client = new OkHttpClient();
    Request request = new Request.Builder()
        .url(url)
        .post(body)
        .addHeader("PackageName", context.getPackageName())
        .addHeader("DeviceId", getDeviceId())
        .addHeader("SecretKey", getSecretKey())
        .build();
    Response httpResponse = null;
    try {
      httpResponse = client.newCall(request).execute();
      httpResponse.code();
    } catch (IOException e) {
      e.printStackTrace();
    }
    try {
      Log.e("Response", "" + httpResponse.body().string());
    } catch (IOException e) {
      e.printStackTrace();
    }
    return httpResponse;
  }
}
