---
title: 'Builder SDK CLI'
service: 'Builder SDK'
area: 'Core'
type: Download
order: 1
---

If you would like to create your own user interfaces to manage your services in the Builder, use the **Builder SDK CLI**. The Builder SDK is a command-line interface that runs the Builder in **developer** mode. This mode implements a Builder module faster and more efficiently.

Topics in this section include:

* Installation process for the application
* Extension, customization, and development of your own Builder module using the Builder
* Builder module descriptor and navigation inside the module
* Security of the Builder and utilities for authorization, notification, and linking
* The localized editor, which is a tool to add the names of your products in languages used by your target market

## Download the Builder SDK CLI

With the Builder SDK CLI, you can:

- Generate new Builder modules
- Run the Builder in developer mode rather than running the full application to maximize your available resources

You can download the latest Builder SDK CLI version for Windows or UNIX/Linux/MacOS.

<div class="panel note">
The latest release version of the Builder SDK CLI is currently 3.10.0.
</div>

<style>
    a.download {
        background-image: -webkit-linear-gradient(top, #2faddb, #09c);
        background-image: -moz-linear-gradient(top, #2faddb, #09c);
        background-image: -o-linear-gradient(top, #2faddb, #09c);
        background-image: -ms-linear-gradient(top, #2faddb, #09c);
        background-image: linear-gradient(top, #2faddb, #09c);
        border-radius: 5px;
        color: #eee;
        line-height: 20px;
        padding: 20px;
        display: inline-block;
        margin-right: 10px;
    }
</style>
<div>
    <a class="download" href="https://github.com/SAP/builder-cli/blob/master/dist/windows/builder4win.zip?raw=true" download>Download for Windows</a>
    <a class="download" version="1.1" href="https://github.com/SAP/builder-cli/blob/master/dist/maclinux/builder.zip?raw=true" download>Download for UNIX/Linux/MacOS</a>
</div>

### Prerequisites

<div class="panel note">
Ensure that you have Java 7 or higher and Apache Maven 3.x installed. For more information, see <a href="/gettingstarted/setuptheenvironment/index.html#1">Set Up the Environment</a>.
</div>

### Install the Builder SDK CLI on Windows

 - Unzip the **builder4win.zip** file.
 - Run the `installerBuilder-SDK-CLI.bat` command with administrator permissions. This batch file creates the **C:\hybris\BuilderSDK** directory structure, and copies the **builder-cli-shade.jar** file into that directory. The installer also adds `BuilderSDK` to the **PATH** variable.

<div>
    Checksum for `builder4win.zip`:
        <ul><li><a href="https://github.com/SAP/builder-cli/blob/master/dist/windows/builder4win.sha256">checksum</a></li></ul>
</div>

### Install the Builder SDK CLI on UNIX/Linux/MacOS

 - Unzip the **builder.zip** file.
 - Change the **builder** file into an executable file using the `chmod +x builder` command. Then, copy the file with admin rights to the **/usr/local/bin** directory so that the `builder` command is found in the **PATH** variable.

<div>
    Checksum for `builder.zip`:
        <ul><li><a href="https://github.com/SAP/builder-cli/blob/master/dist/maclinux/builder.sha256">checksum</a></li></ul>
</div>

## Make the Builder SDK CLI available as Homebrew-Tap

The Builder SDK CLI is available as Homebrew-Tap **for Mac users only**. If you are a Mac user, you can use Homebrew to download the Builder CLI with the following steps:

1. Install <a href="http://brew.sh/">Homebrew</a> and make sure you have the latest version by running `brew update` command in a terminal.
2. In command prompt, run `brew tap hybris/builder` command to download Builder CLI.
3. Run `brew install builder`command to install the Builder SDK.

<div class="panel note">
Read the <a href="https://github.com/hybris/homebrew-builder/blob/master/README.md">README</a> file if you encounter any problems.
</div>
