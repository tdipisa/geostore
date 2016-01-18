#!/bin/bash

cd src/

mvn clean install cobertura:cobertura -Pextjs,h2_disk,auditing -DrepoToken=$repoToken