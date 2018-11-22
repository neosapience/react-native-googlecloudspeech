
package com.neosapience.reactnativelib.googlecloudspeech;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;

public class RNGooglecloudspeechModule extends ReactContextBaseJavaModule {

  private final ReactApplicationContext reactContext;

  public RNGooglecloudspeechModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  @Override
  public String getName() {
    return "RNGooglecloudspeech";
  }
}