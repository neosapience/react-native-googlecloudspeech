package com.neosapience.reactnativelib.googlecloudspeech.speechrecognition

import com.facebook.react.bridge.WritableMap

interface OnRecognitionResult {
    fun onResult(result: WritableMap?)
    fun onError(error: WritableMap?)
}