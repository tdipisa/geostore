#!/bin/bash

echo $repoToken

cd src/

mvn clean install -Pextjs,h2_disk,auditing -DrepoToken=$repoToken 