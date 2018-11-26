/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.neosapience.reactnativelib.googlecloudspeech.speechrecognition

import android.util.Log
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableMap
import com.google.api.gax.rpc.ApiStreamObserver
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.speech.v1.RecognitionConfig
import com.google.cloud.speech.v1.SpeechClient
import com.google.cloud.speech.v1.SpeechSettings
import com.google.cloud.speech.v1.StreamingRecognitionConfig
import com.google.cloud.speech.v1.StreamingRecognizeRequest
import com.google.cloud.speech.v1.StreamingRecognizeResponse
import java.util.concurrent.atomic.AtomicBoolean

private const val TAG = "Speech"

/**
 * This example demonstrates calling the Cloud Speech-to-Text bidirectional
 * streaming API.
 */
class SpeechRecognition(private val locale: String, private val credential: String, private val callback: OnRecognitionResult) {
    private var isStarted = false
    private var mSpeechClient : SpeechClient? = null
    private var mAudioEmitter: AudioEmitter? = null

    fun start() {
        if (!isStarted) {
            val isFirstRequest = AtomicBoolean(true)
            mAudioEmitter = AudioEmitter()

            mSpeechClient = SpeechClient.create(SpeechSettings.newBuilder().setCredentialsProvider {
                GoogleCredentials.fromStream(credential.byteInputStream())
            }.build())

            // start streaming the data to the server and collect responses
            val requestStream = mSpeechClient?.streamingRecognizeCallable()?.bidiStreamingCall(object : ApiStreamObserver<StreamingRecognizeResponse> {
                override fun onNext(value: StreamingRecognizeResponse) {
                    when {
                        value.resultsCount > 0 -> {
                            val event: WritableMap = Arguments.createMap()
                            event.putBoolean("isFinal", value.getResults(0).isFinal)
                            event.putString("transcript", value.getResults(0).getAlternatives(0).transcript)
                            callback.onResult(event)
                        }
                        else -> {
                            val event: WritableMap = Arguments.createMap()
                            event.putInt("code", value.error.code)
                            event.putString("message", value.error.message)
                            callback.onError(event)
                        }
                    }
                }

                override fun onError(t: Throwable) {
                    Log.e(TAG, "an error occurred", t)
                }

                override fun onCompleted() {
                    Log.d(TAG, "stream closed")
                }
            })

            // monitor the input stream and send requests as audio data becomes available
            mAudioEmitter!!.start { bytes ->
                val builder = StreamingRecognizeRequest.newBuilder()
                        .setAudioContent(bytes)

                // if first time, include the config
                if (isFirstRequest.getAndSet(false)) {
                    builder.streamingConfig = StreamingRecognitionConfig.newBuilder()
                            .setConfig(RecognitionConfig.newBuilder()
                                    .setLanguageCode(locale)
                                    .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                                    .setSampleRateHertz(16000)
                                    .build())
                            .setInterimResults(false)
                            .setSingleUtterance(false)
                            .build()
                }

                // send the next request
                requestStream?.onNext(builder.build())
            }
            isStarted = true
        }
    }

    fun stop() {
        if (isStarted) {
            mAudioEmitter?.stop()
            mAudioEmitter = null
            mSpeechClient?.close()
            mSpeechClient?.shutdownNow()
            mSpeechClient = null
            isStarted = false
        }
    }
}