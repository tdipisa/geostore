#!/bin/bash

mvn clean install -Pextjs,h2_disk,auditing -DrepoToken=$repoToken 