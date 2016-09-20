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

1. Create a directory where you want to develop your new module, such as **${homeDir}/dev/builderModule/wishlist**.

2. Use the `builder createModule [title_of_your_module]` command to create a Builder module skeleton in created folder. This skeleton includes an **index.html** file and a new directory called **builder** with the **module.json** file inside, which contains all the necessary information, such as:
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

3. Use the `builder runDevServer` command to start a Jetty server running Builder locally in developer mode. Please make sure that you run the command in the root directory of your module (the one with index.html file inside). After starting the server, open a browser and go to http://localhost:8080/ to see your newly created module in the Builder.

4. To make changes in your Builder module, you can edit the **index.html** file. Use the style sheet provided in the browser to align with general design conventions.

5. If you need to delete cached dependencies, use the `builder clearCache` command.
   Run this command, if you have any problems with builder in **developer mode**.
