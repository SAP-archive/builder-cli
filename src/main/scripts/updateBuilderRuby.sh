#!/usr/bin/env bash
CURRENTPATH=$(pwd)
BUILDERSHA1VALUE=$(shasum $CURRENTPATH/dist/homebrew-builder/builder.tgz | grep -Eo '^[^ ]+')
sed -e "s/sha1.*/sha1 '$BUILDERSHA1VALUE'/" $CURRENTPATH/builder.rb > $CURRENTPATH/builder2.rb
mv $CURRENTPATH/builder2.rb $CURRENTPATH/builder.rb
