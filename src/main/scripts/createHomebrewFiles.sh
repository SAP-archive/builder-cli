#!/usr/bin/env bash
echo 'Start homebrew action'

echo 'create tmpDirectories and cp shell script to it'
mkdir -p target/tmpBuilderDir/builder
cp target/builder target/tmpBuilderDir/builder

echo 'create tgz and copy to dist/homebrew-builder'
cd target/tmpBuilderDir
tar -cvzf builder/builder.tgz builder/builder
cd ../..
cp target/tmpBuilderDir/builder/builder.tgz dist/homebrew-builder/builder.tgz
