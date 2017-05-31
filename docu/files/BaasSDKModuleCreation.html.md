---
title: 'Create your own Builder module using the Builder SDK CLI'
service: 'Builder SDK'
type: Tutorial
layout: 'tools'
order: 2
---

### Prerequisites

<div class="panel note">
Before following these steps, you must download and install the Builder SDK CLI.
</div>

<!-- Outer div defines maximum space the player can take -->
<div style="width: 50%;display: inline-block;position: relative;">
    <!--  inner pusher div defines aspect ratio: in this case 16:9 ~ 56.25% -->
    <div id="dummy" style="margin-top: 56.25%;"></div>
    <!--  the player embed target, set to take up available absolute space   -->
    <div id="kaltura_player_1398862018" style="position:absolute;top:0;left:0;left: 0;right: 0;bottom:0;">
    </div>
</div>
<script src="https://cdnapisec.kaltura.com/p/1673981/sp/167398100/embedIframeJs/uiconf_id/32240031/partner_id/1673981"></script>
<script>
kWidget.embed({
    'targetId': 'kaltura_player_1398862018',
    'wid': '_1673981',
    'uiconf_id' : '32240031',
    'entry_id' : '1_w7nc7h2l'
});
</script>
<br>
1. Change to a directory where you want to develop your new module, such as **${homeDir}/dev/myBuilderModules**.

2. Use the `builder createModule [title_of_your_module]` command to create a Builder module skeleton. You can choose from several templates, e.g. list. This skeleton is located in a new folder named [title_of_your_module] and includes view-related files, such as **index.html** and a directory called **builder** with the **module.json** file inside, which contains all the necessary meta-information needed by the builder, such as:
```
{
    "widgets" : [{
        "id" : "wishlist#1",
        "title" : "Wishlist",
        "settings" : {
            "viewUrl" : "index.html",
            "example_setting" : "some value"
        }
    }]
}
```

3. Use the `builder runModule` command to start a web server running your Builder Module locally. Please make sure that you run the command in the root directory of your module (the one with index.html file inside). After starting the server, open a browser and go to https://localhost:8081/ to accept the self-signed certificate. After that, go to https://builder.yaas.io, navigate to your project and create a Builder Module with **Module Location** set to https://localhost:8081/builder/module.json and **Use this Builder Module for my Project** enabled. After that, your builder module should be visible in the left navigation, so just click on it.

4. Now you can edit the **index.html** file and adjust it to your needs. Changes will be immediately applied after reloading the page in your browser.

