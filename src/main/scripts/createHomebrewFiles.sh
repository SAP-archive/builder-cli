#!/usr/bin/env bash
echo 'Start homebrew action'

echo 'create tmpDirectories and cp shell script to it'
mkdir -p target/tmpBuilderDir
cp target/builder target/tmpBuilderDir

echo 'create tgz and copy to dist/homebrew-builder'
cd target/tmpBuilderDir
tar -cvzf builder.tgz builder
cd ../..
cp target/tmpBuilderDir/builder.tgz dist/homebrew-builder/builder.tgz

(shasum dist/homebrew-builder/builder.tgz | grep -Eo '^[^ ]+') > dist/homebrew-builder/builder.sha1


