# Builder-CLI
Written by: Team Swinka

The builder command line interface generates a builder and builder.exe file.
These executable files allow you to generate a builder module template and run it locally in the builder in developers mode.

# Build builder-cli
mvn clean install

For generation of the Builder executable file for unix/linux/mac machines run the following maven command
mvn install -PcreateShBin

For generation the Builder installer for Windows run the following maven command
mvn install -PcreateBuilder4Win
