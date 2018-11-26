#!/bin/bash

./gradlew ${1:-installDevMinSdkDevKernelDebug} --stacktrace && adb shell am start -n com.neosapience.library.rngcspeech/host.exp.exponent.MainActivity
