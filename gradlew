#!/usr/bin/env sh

# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

#
# This script is used to execute the Gradle build system.
#
# When the script is run (e.g. ./gradlew build), it will download
# and install the appropriate Gradle distribution if necessary.
#

APP_NAME="Gradle"
APP_BASE_NAME=`basename "$0"`

# OS specific support (must be 'true' or 'false').
cygwin=false
darwin=false
linux=false
case "`uname`" in
  CYGWIN*) cygwin=true ;;
  Darwin*) darwin=true ;;
  Linux*)  linux=true ;;
esac

# For Cygwin, ensure paths are in UNIX format before anything else.
if $cygwin; then
  [ -n "$GRADLE_HOME" ] && GRADLE_HOME=`cygpath --unix "$GRADLE_HOME"`
  [ -n "$JAVA_HOME" ] && JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
  [ -n "$HOME" ] && HOME=`cygpath --unix "$HOME"`
fi

# Determine the Java command to use to start the JVM.
if [ -n "$JAVA_HOME" ] ; then
  if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
    # IBM's JDK on AIX uses "$JAVA_HOME/jre/sh/java" as the system JVM.
    JAVACMD="$JAVA_HOME/jre/sh/java"
  else
    JAVACMD="$JAVA_HOME/bin/java"
  fi
  if [ ! -x "$JAVACMD" ] ; then
    die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
  fi
else
  JAVACMD="java"
  which java >/dev/null || die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation or add 'java' to your PATH."
fi

# Increase the maximum file descriptor number if possible.
if [ "$cygwin" = "false" ]; then
    MAX_FD_LIMIT=`ulimit -n`
    if [ `ulimit -n` -ne `ulimit -Hn` ]; then
        ulimit -n `ulimit -Hn`
    fi
fi

# OS specific support
if $cygwin ; then
  CLASSPATH=`cygpath --path --unix "$CLASSPATH"`
  JAVA_HOME=`cygpath --path --unix "$JAVA_HOME"`
fi

# Assume that the Java application is in this directory.
APP_HOME=`dirname "$0"`
APP_HOME=`cd "$APP_HOME"; pwd`

# Find the path to the wrapper JAR.
if [ -f "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" ]; then
    GRADLE_WRAPPER_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
else
    # Fallback to the current directory (when gradlew is executed outside the repo root)
    if [ -f "gradle/wrapper/gradle-wrapper.jar" ]; then
        GRADLE_WRAPPER_JAR="gradle/wrapper/gradle-wrapper.jar"
    else
        echo "ERROR: Could not find gradle-wrapper.jar in $APP_HOME/gradle/wrapper or ./gradle/wrapper."
        exit 1
    fi
fi

# Check if there's a custom daemon JVM args file
if [ -f "$APP_HOME/.gradle/gradle.properties" ]; then
    . "$APP_HOME/.gradle/gradle.properties"
fi

# Add default JVM options for Gradle if not set
if [ -z "$DEFAULT_JVM_OPTS" ]; then
    DEFAULT_JVM_OPTS="-Xmx512m -XX:+HeapDumpOnOutOfMemoryError"
fi

# Set the class path
CLASSPATH="$GRADLE_WRAPPER_JAR"

# Ensure that the JVM is loaded with the proper encoding to avoid issues with non-ASCII characters
if [ -z "$JAVA_OPTS" ]; then
    JAVA_OPTS="-Dfile.encoding=UTF-8"
fi

exec "$JAVACMD" $DEFAULT_JVM_OPTS "$JAVA_OPTS" -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"