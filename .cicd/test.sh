#!/usr/bin/env bash

set -o errexit
set -o pipefail
set -o nounset
set -o xtrace

BASE="$(cd "$(dirname "${0}")" && pwd)"
source ${BASE}/../.env

amm `pwd`/modules/script/SQSConsumerSpec.sc
sbt clean coverage test dependencyUpdates coverageReport
#sbt clean test dependencyUpdates coverageAggregate