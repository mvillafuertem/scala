#!/usr/bin/env bash

set -o errexit
set -o pipefail
set -o nounset
set -o xtrace

BASE="$(cd "$(dirname "${0}")" && pwd)"
source ${BASE}/../.env

sbt "benchmarks/jmh:run -i 1 -wi 1 -f1 -t4"
#Which means "iterations 1" "warmup iterations 1" "fork 1" "thread 4"
