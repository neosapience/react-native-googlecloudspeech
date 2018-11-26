package com.neosapience.reactnativelib.googlecloudspeech;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.neosapience.reactnativelib.googlecloudspeech.speechrecognition.SpeechRecognition;
import com.neosapience.reactnativelib.googlecloudspeech.speechrecognition.OnRecognitionResult;
import javax.annotation.Nullable;

public class RNGooglecloudspeechModule extends ReactContextBaseJavaModule {
  private SpeechRecognition speechRecognition = null;
  private final ReactApplicationContext reactContext;

  public RNGooglecloudspeechModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  @Override
  public String getName() {
    return "RNGooglecloudspeech";
  }

  private void sendEvent(String eventName, @Nullable WritableMap params) {
    this.reactContext
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
      .emit(eventName, params);
  }

  @ReactMethod
  public void init(String locale, String credential) {
    speechRecognition = new SpeechRecognition(locale, credential, new OnRecognitionResult() {
      @Override
      public void onError(@Nullable WritableMap error) {
        sendEvent("onError", error);
      }

      @Override
      public void onResult(@Nullable WritableMap result) {
        sendEvent("onResult", result);
      }

    });
  }

  @ReactMethod
  public void start() {
    speechRecognition.start();
  }

  @ReactMethod
  public void stop() {
    speechRecognition.stop();
  }
}