#!/usr/bin/env bash

gitroot="$(git rev-parse --show-toplevel)"
currentdir="$(pwd)"

if [ -d "$gitroot/testserver" ]; then
  echo Test server already exists.
  exit
fi

mvn package
mkdir "$gitroot/testserver"
wget -O "$gitroot/testserver/BuildTools.jar" https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar
git config --global --unset core.autocrlf
cd "$gitroot/testserver" # cd into testserver to work around buildtools placing files in the current dir rather than
                         # the dir buildtools is in.
java -jar "$gitroot/testserver/BuildTools.jar" -rev 1.13.2
cd "$currentdir"
mkdir "$gitroot/testserver/plugins"
ln -sv "$gitroot/target/CivSpellAPI.jar" "$gitroot/testserver/plugins/CivSpellAPI.jar"
# TODO: Download civmodcore into plugins folder when it's in the devoted buildserver.
