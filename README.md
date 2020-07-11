# Logging WebClient Request and Response Payloads
This project accompanies a blog found at https://andrew-flower.com/blog/webclient-body-logging

Sometimes it's valuable to be able to log the serialized form of data.  It is not
intuitive how to do this with WebClient, but the blog above explains one method.

This repository provides a custom JSON encoder and decoder, as well as JUnit 5 tests
that verify these codecs.

## Running the tests

    ./gradlew clean test