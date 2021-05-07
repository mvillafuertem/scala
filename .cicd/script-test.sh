#!/usr/bin/env bash

set -o errexit
set -o pipefail
set -o nounset
set -o xtrace

BASE="$(cd "$(dirname "${0}")" && pwd)"
source ${BASE}/../.env

# shellcheck disable=SC2010
ls -d modules/script/* | grep Spec.sc | xargs -I {} sbt --error ';project script;amm {}'