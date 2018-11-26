import { NativeEventEmitter, NativeModules } from 'react-native';

const { RNGooglecloudspeech } = NativeModules;
const emitter = new NativeEventEmitter(RNGooglecloudspeech)

class Googlecloudspeech {
  constructor () {
    this._loaded = false
    this._listeners = null
    this._events = {
      'onError': this._onError.bind(this),
      'onResult': this._onResult.bind(this),
    }
  }

  destroy () {
    if (this._listeners) {
      this._listeners.map((listener, index) => listener.remove())
      this._listeners = null
    }
  }

  initialize () {
    if (!this._loaded && !this._listeners) {
      this._listeners = Object.keys(this._events)
        .map((key, index) => emitter.addListener(key, this._events[key]))
    }
  }

  start () {
    RNGooglecloudspeech.start()
  }

  stop () {
    RNGooglecloudspeech.stop()
  }

  // Events

  _onError (e) {
    this.onError(e)
  }

  _onResult (e) {
    this.onResult(e)
  }
}

module.exports = new Googlecloudspeech()