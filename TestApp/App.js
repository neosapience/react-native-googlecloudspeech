import React from 'react';
import { StyleSheet, Text, View, Platform } from 'react-native';
import { Permissions } from 'expo';

const RNGooglecloudspeech = Platform.select({
  android: require("react-native-googlecloudspeech")
})

export default class App extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      spoken: "",
      recording: false,
      message: null
    };
  }

  componentDidMount() {
    if (RNGooglecloudspeech && Platform.OS === "android") {

      RNGooglecloudspeech.onSpeechRecognized = this.speechDetected;
    }
  }

  speechDetected = e => {
    if (e.transcript.length > 0) {
      this.setState({ spoken: e.transcript });
    }
  };

  render() {
    const { recording, message, spoken } = this.state;
    return (
      <View style={styles.container}>
        <Text style={styles.welcome}>Welcome to React Native!</Text>
        <Text style={styles.instructions}>To get started, edit App.js</Text>
        <Text style={styles.instructions}>{instructions}</Text>
        <Button
          title={recording ? "Stop Listening" : "Start Listening"}
          style={styles.button}
          onPress={async () => {
            if (recording) {
              RNGooglecloudspeech.stop();
              this.setState({ recording: false });
            } else if (await requestMicrophone()) {
              this.setState({ recording: true }, RNGooglecloudspeech.start);
            } else {
              const message =
                Platform.OS !== "android"
                  ? "RNGooglecloudspeech only supported on Android."
                  : "Turn on your microphone.";
              this.setState({ message });
            }
          }}
        />
        {recording && <Text>Heard: "{spoken}"</Text>}
        {message && <Text style={styles.message}>{message}</Text>}
      </View>
    );
  }

  requestMicrophone() {
    if (Platform.OS !== "android") {
      return false;
    }
    const recordPermission = await Permissions.askAsync(Permissions.AUDIO_RECORDING);

    if (recordPermission.status === 'granted') {
      return true;
    } else {
      console.log("no granted");
    }
  }

}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: "center",
    alignItems: "center",
    backgroundColor: "#F5FCFF"
  },
  welcome: {
    fontSize: 20,
    textAlign: "center",
    margin: 10
  },
  instructions: {
    textAlign: "center",
    color: "#333333",
    marginBottom: 5
  },
  button: {
    marginVertical: 10
  },
  message: {
    color: "red"
  }
});
