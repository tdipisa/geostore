#!/bin/bash

cd src/

mvn clean install cobertura:cobertura coveralls:report -Pextjs,h2_disk,auditing -D$repoToken 