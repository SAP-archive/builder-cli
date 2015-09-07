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

echo 'update sha-value and version in builder.rb file'
BUILDERSHA1VALUE=$(shasum dist/homebrew-builder/builder.tgz | grep -Eo '^[^ ]+')
sed -e "s/sha1.*/sha1 '$BUILDERSHA1VALUE'/" builder.rb > builder2.rb
MVN_VERSION=$(mvn -q -Dexec.executable="echo" -Dexec.args='${project.version}' --non-recursive org.codehaus.mojo:exec-maven-plugin:1.3.1:exec)
sed -e "s/version.*/version '$MVN_VERSION'/" builder2.rb > builder3.rb
mv builder3.rb builder.rb
rm builder2.rb
rm -rf target/tmpBuilderDir
