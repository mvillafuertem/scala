#!/usr/bin/env bash

set -o errexit
set -o pipefail
set -o nounset
set -o xtrace

BASE="$(cd "$(dirname "${0}")" && pwd)"
source ${BASE}/../.env

sudo sh -c '(echo "#!/usr/bin/env sh" && curl -L https://github.com/lihaoyi/Ammonite/releases/download/2.2.0/2.13-2.2.0) > /usr/local/bin/amm && chmod +x /usr/local/bin/amm' \
&& amm `pwd`/modules/script/SQSConsumerSpec.sc

sbt clean coverage test dependencyUpdates coverageReport
#sbt clean test dependencyUpdates coverageAggregate