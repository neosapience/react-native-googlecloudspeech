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
import com.google.api.gax.rpc.ResponseObserver
import com.google.api.gax.rpc.StreamController
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
    private var mResponseObserver: ResponseObserver<StreamingRecognizeResponse>? = null
    private val SCOPE = listOf("https://www.googleapis.com/auth/cloud-platform")

    fun start() {
        if (!isStarted) {
            val isFirstRequest = AtomicBoolean(true)
            mAudioEmitter = AudioEmitter()

            mSpeechClient = SpeechClient.create(SpeechSettings.newBuilder().setCredentialsProvider {
                GoogleCredentials.fromStream(credential.byteInputStream()).createScoped((SCOPE))
            }.build())

            mResponseObserver = object : ResponseObserver<StreamingRecognizeResponse> {
                override fun onComplete() {
                    Log.d(TAG, "stream closed")
                }

                override fun onResponse(response: StreamingRecognizeResponse?) {
                    when {
                        response?.resultsCount!! > 0 -> {
                            val event: WritableMap = Arguments.createMap()
                            event.putBoolean("isFinal", response.getResults(0).isFinal)
                            event.putString("transcript", response.getResults(0).getAlternatives(0).transcript)
                            callback.onResult(event)
                        }
                        else -> {
                            val event: WritableMap = Arguments.createMap()
                            event.putInt("code", response.error.code)
                            event.putString("message", response.error.message)
                            callback.onError(event)
                        }
                    }
                    Log.d(TAG, response.toString())
                }

                override fun onError(t: Throwable?) {
                    Log.e(TAG, "an error occurred", t)
                }

                override fun onStart(controller: StreamController?) {
                    Log.e(TAG, "stream is started")
                }

            }


            // start streaming the data to the server and collect responses
            val requestStream = mSpeechClient?.streamingRecognizeCallable()?.splitCall(mResponseObserver)

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
                            .setInterimResults(true)
                            .setSingleUtterance(false)
                            .build()
                }

                // send the next request
                requestStream?.send(builder.build())
            }
            isStarted = true
        }
    }

    fun stop() {
        try {
            if (isStarted) {
                mAudioEmitter?.stop()
                mResponseObserver?.onComplete()
                mSpeechClient?.shutdownNow()
            }
        } catch (e: Throwable) {
            Log.e(TAG, "exception: ", e)
        } finally {
            mAudioEmitter = null
            mResponseObserver = null
            mSpeechClient = null
            isStarted = false
        }
    }
}