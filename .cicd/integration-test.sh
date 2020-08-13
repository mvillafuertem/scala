#!/usr/bin/env bash


BASE="$(cd "$(dirname "${0}")" && pwd)"
source ${BASE}/../.env

sbt clean it:test