#!/bin/bash

echo "[ INFO ] check if the jar file is executable.."

jar_file=$(find ./jar/ -type f -name "*.jar")

num_jars=$(echo "$jar_file" | grep -c "jar$")

if [[ $num_jars -ne 1 ]]; then
    echo "[ ERROR ] there must be exactly one jar file."
    exit 1
fi

# a manifest is a necessary condition for it to be an executable jar file
if jar -tf "$jar_file" | grep -q "^META-INF/MANIFEST.MF$"; then
    echo "[ INFO ] jar file contains a manifest."
else
    echo "[ ERROR ] jar file does not contain a manifest."
    exit 1
fi

# timeout the application because sometimes the process waits for user input
message=$(timeout --foreground 5s java -jar "$jar_file" 2>&1)
# the exit code of the jar execution
exit_code=$?

# the exit code 124 indicates that the process was terminated by timeout -> jar is executable
if [[ $exit_code -eq 124 ]]; then
    echo "[ INFO ] process was terminated by timeout."
    exit_code=0
fi

if [[ $exit_code -eq 0 ]]; then
    echo "[ OK ] jar file is executable."
    exit 0
else
    echo "[ WARN ] exit code is $exit_code"
    echo "[ INFO ] error message is:"
    echo "$message"
fi

if grep -iqF "javafx" <<< "$message"; then
    echo "[ INFO ] error message mentions javafx."
else
    echo "[ INFO ] error message does not mention javafx."
    echo "[ ERROR ] jar file is not executable."
    exit 1
fi

echo "[ INFO ] jar file seems to require javafx libraries."
echo "[ INFO ] starting a new virtual display.."

# start the virtual display
Xvfb :99 -screen 0 1024x768x24 &

# try to run the jar with the javafx libraries
timeout --foreground 5s java -Djava.library.path=/opt/java/openjdk/ --module-path "/javafx-sdk-21.0.9/lib/" --add-modules "javafx.base,javafx.controls,javafx.graphics,javafx.fxml,javafx.media,javafx.swing" -jar "$jar_file"
# the exit code of the jar execution
exit_code=$?

# the exit code 124 indicates that the process was terminated by timeout -> jar is executable
if [[ $exit_code -eq 124 ]]; then
    echo "[ INFO ] process was terminated by timeout."
    exit_code=0
fi

if [[ $exit_code -eq 0 ]]; then
    echo "[ OK ] jar file is executable."
    exit 0
else
    echo "[ ERROR ] jar file is not executable."
    exit 1
fi
