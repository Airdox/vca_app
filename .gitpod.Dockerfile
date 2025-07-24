# Basierend auf einem gängigen Gitpod-Ubuntu-Image
FROM gitpod/workspace-full

USER root

# Installiere Java 17 und setze es als Standard (Alternative zu sdkman in .gitpod.yml)
RUN apt-get update && \
    apt-get install -y openjdk-17-jdk && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

ENV JAVA_HOME /usr/lib/jvm/java-17-openjdk-amd64
ENV PATH $JAVA_HOME/bin:$PATH

# Installiere Android SDK
ENV ANDROID_HOME /opt/android/sdk
ENV PATH "$PATH:${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools"

RUN mkdir -p ${ANDROID_HOME}/cmdline-tools && \
    wget -q https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -O /tmp/cmdline-tools.zip && \
    unzip -q /tmp/cmdline-tools.zip -d ${ANDROID_HOME}/cmdline-tools && \
    mv ${ANDROID_HOME}/cmdline-tools/cmdline-tools ${ANDROID_HOME}/cmdline-tools/latest && \
    rm /tmp/cmdline-tools.zip && \
    echo y | ${ANDROID_HOME}/cmdline-tools/latest/bin/sdkmanager --sdk_root=${ANDROID_HOME} "platform-tools" "build-tools;34.0.0" "platforms;android-34" && \
    echo y | ${ANDROID_HOME}/cmdline-tools/latest/bin/sdkmanager --sdk_root=${ANDROID_HOME} --licenses

# Setze den Benutzer zurück auf den Standard-Gitpod-Benutzer
USER gitpod