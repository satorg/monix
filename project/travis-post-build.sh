#!/usr/bin/env bash

set -e

cd `dirname $0`/..

if [ -z "$MAIN_SCALA_VERSION" ]; then
    >&2 echo "Environment MAIN_SCALA_VERSION is not set. Check .travis.yml."
    exit 1
fi

if [ "$TRAVIS_SCALA_VERSION" = "$MAIN_SCALA_VERSION" ]; then
    echo "Uploading coverage for Scala $TRAVIS_SCALA_VERSION"
    sbt ";coverageAggregate;coverageReport"
    bash <(curl -s https://codecov.io/bash)
else
    echo "Skipping uploading coverage for Scala $TRAVIS_SCALA_VERSION"
fi
