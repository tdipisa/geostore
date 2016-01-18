#!/bin/bash

echo $SOMEVAR

cd src/

mvn clean install -Pextjs,h2_disk,auditing -D$repoToken 