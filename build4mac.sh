#!/bin/sh
mvn clean install -PcreateBuilder4Win && mvn install -PcreateShBin && chmod 755 target/builder && sudo cp target/builder /usr/local/bin/
