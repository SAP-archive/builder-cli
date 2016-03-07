var YaaS = (function(){
    "use strict";

    var https = require("https");
    var readline = require("readline");
    var fs = require("fs");
    var _path = require("path");

    var state = {};

    var ctx = {};

    var settingsFileName;

    var qStack = [];
    var rl = readline.createInterface(process.stdin, process.stdout);


    var apiBaseDomain = "api.yaas.io";
    var accountSrv = "/hybris/account/v1";
    var oauthSrv = "/hybris/oauth2/v1";
    var marketPlaceSrv = "/hybris/marketplace/v3";
    var clientId = "Bin5mve9QLuEpYggjTeMzjQoklXr4aav";
    var scope = "hybris.configuration_manage hybris.account_manage hybris.account_view hybris.marketplace_submit hybris.media_manage hybris.org_manage hybris.org_view sap.bill_view sap.subscription_manage hybris.org_payment hybris.org_project_create hybris.org_members sap.subscription_cancel sap.subscription_provider_view hybris.showcase_manage hybris.org_project_manage";

    var Colors = {
        WHITE : "\x1b[37m",
        CYAN : "\x1b[36m",
        GREEN : "\x1b[32m",
        RED : "\x1b[31m",
        YELLOW : "\x1b[33m",
        BLUE : "\x1b[34m",
        RESET : '\x1b[0m'
    };

    function _error(str, noPrefix) {
        console.log(Colors.RED + (noPrefix?"":"Error: ") +  str + Colors.RESET);
        return {
            andExit : function() {
                process.exit(1);
            }
        };
    }

    function _success(str) {
        console.log(Colors.GREEN + str + Colors.RESET);
    }

    function proceedQStack() {
        if(qStack.length > 0) {
            var first = qStack.shift();
            first.fun.apply(first.fun, first.args ? first.args : []);
        }
    }

    function addToQStack(fun, args) {
        qStack.push({fun:fun,args:args});
    }

    function moduleDescriptorExists() {
        return fs.existsSync(process.cwd() + _path.sep + "builder" + _path.sep + "module.json");
    }


    function getJSON(url, path, callback) {
        var options = {
            'content-type' : 'application/json',
            'method': 'GET'
        };
        http(url, path, options, function(data, resp) {
            if(resp.statusCode < 400) {
                callback(JSON.parse(data));
            } else {
                _error(response.statusCode + ", " + response.statusMessage + "\nData: " + data).andExit();
            }
        });
    }

    function http(url, path, options, callback, noAuth, postData) {
        options.host = url;
        options.path = path;
        if(!noAuth) {
            if(!options.headers) {
                options.headers = {};
            }
            options.headers.Authorization = "Bearer " + ctx.token;
        }

        var req = https.request(options, function(res) {
            var data = "";
            res.on('data', function(d) {
                data += d;
            });
            res.on('end', function() {
                callback(data, res);
            });
        });
        req.on('error', function(e) {
            console.log('problem with request: ' + e.message);
        });

        if(postData) {
            req.write(postData);
        }
        req.end();
    }


    return {
        loadCTX : function(file) {
            settingsFileName = file ? file :
                (process.env.HOME || process.env.USERPROFILE || process.env.HOMEPATH) + _path.sep + ".builder" + _path.sep + "shellrc.json";

            try {
                var info;
                try {
                    info = JSON.parse(fs.readFileSync(settingsFileName, "UTF-8"));
                } catch (e) {}

                if(info) {
                    if(info.org && info.team) {
                        ctx = info;
                        return true;
                    }
                }
            } catch(error) {
                //ok
            }

            return false;
        },

        saveCTX : function() {
            fs.writeFileSync(settingsFileName, JSON.stringify(ctx), "utf8");
        },

        showCurrentCTX : function() {
            addToQStack(function() {
                console.log(Colors.WHITE + "Org: ", Colors.RESET, ctx.org.name, Colors.WHITE, "  Team: ", Colors.RESET, ctx.team.name);
                proceedQStack();
            });
            return YaaS;
        },

        generateAppId : function() {
            return _path.basename(process.cwd()).toLowerCase().replace(/\W/g, '');
        },

        createBuilderModule : function() {
            addToQStack(function() {
                if(moduleDescriptorExists()) {
                    var appId = ctx.team.id + "-" + YaaS.generateAppId();

                    var modulePayload = {
                        "appType": "UI_MODULE",
                        "redirectUris": ["https://builder.yaas.io/auth/callback.html", "https://localhost:8081/builder/auth.html"],
                        "name": appId,
                        "displayName": appId + " Test Module",
                        "description": "Generated " + appId + " Test Module",
                        "moduleUrl": "https://localhost:8081/builder/module.json",
                        "requiredScopes": (state.scopesArray ? state.scopesArray : [])
                    };

                    var postData = JSON.stringify(modulePayload);
                    process.stdout.write("Creating builder module '" + appId + "' ...");

                    http(apiBaseDomain, accountSrv + "/teams/" + ctx.team.id + "/apps", {
                            headers : {
                                'content-type' : 'application/json',
                                'content-length' : postData.length
                            },
                            'method': 'POST'
                        }, function(data, response) {
                            if(response.statusCode == 201) {
                                state.moduleId = JSON.parse(data).id;
                                _success(" Done.");
                                proceedQStack();
                            } else {
                                _error(" Failed. \n" + response.statusCode + ", " + response.statusMessage + "\nData: " + data, true).andExit();
                            }
                        },
                        false, postData);
                } else {
                    _error("'builder/module.json' not found in current directory.").andExit();
                }
            });
            return YaaS;
        },

        chooseOrCreateService : function() {
            addToQStack(function() {
                getJSON(apiBaseDomain, accountSrv + "/services/?team=" + ctx.team.id + "&pageSize=1000", function(data) {
                    console.log(Colors.WHITE, "\nChoose an existing service or create a new one:\n " , Colors.RESET);

                    console.log("0 [Create new service]");
                    for (var index = 0; index < data.length; index++) {
                        var element = data[index];
                        console.log((index+1) + " " + element.name);
                    }
                    rl.question(Colors.WHITE + "\nNumber: " + Colors.RESET, function(answer) {
                        var idx = parseInt(answer)-1;
                        if(idx === -1) { // CREATE
                            if(moduleDescriptorExists()) {
                                var appId = YaaS.generateAppId();

                                var servicePayload = {
                                    "team": ctx.team.id,
                                    "name": ctx.team.id + "-" + appId + "srv",
                                    "version": "v1"
                                };

                                var postData = JSON.stringify(servicePayload);

                                process.stdout.write("Creating service '" + servicePayload.name + "' ...");

                                http(apiBaseDomain, accountSrv + "/services", {
                                        headers : {
                                            'content-type' : 'application/json',
                                            'content-length' : postData.length
                                        },
                                        'method': 'POST'
                                    }, function(data, response) {
                                        if(response.statusCode == 201) {
                                            state.srvId = JSON.parse(data).id;
                                            _success(" Done.");
                                            proceedQStack();
                                        } else {
                                            _error(" Failed.\n" + response.statusCode + ", " + response.statusMessage + "\nData: " + data, true).andExit();
                                        }
                                    },
                                    false, postData);
                            }
                        } else { // SELECT
                            state.srvId = data[idx].id;
                            var scopes = data[idx].scopes;
                            if(scopes) {
                                state.scopesArray = [];
                                for(var s_i = 0; s_i < scopes.length; s_i++) {
                                    state.scopesArray.push(scopes[s_i].name);
                                }
                            }
                            proceedQStack();
                        }
                    });
                });

            });
            return YaaS;
        },

        createPackage : function() {
            addToQStack(function() {
                if(moduleDescriptorExists()) {
                    var appId = YaaS.generateAppId();

                    var packagePayload = {
                        "name" : {en : "gen_" + appId + "_pkg"},
                        "description" : {en : "Generated test package for " + appId + " builder module"}
                    };

                    var postData = JSON.stringify(packagePayload);

                    process.stdout.write("Creating package '" + packagePayload.name.en + "'...");

                    http(apiBaseDomain, marketPlaceSrv + "/packages/", {
                            headers : {
                                'content-type' : 'application/json',
                                'content-length' : postData.length
                            },
                            'method': 'POST'
                        }, function(data, response) {
                            if(response.statusCode == 201) {
                                state.pkgId = JSON.parse(data).id;
                                getJSON(apiBaseDomain, marketPlaceSrv + "/packages/" + state.pkgId, function(data) {
                                    if(data.length > 0) {
                                        state.pkgVersion = data[0].packageVersion;
                                        _success(" Done.");
                                        proceedQStack();
                                    } else {
                                        _error(" Failed.\n" + data, true).andExit();
                                    }
                                });
                            } else {
                                _error(" Failed.\n" + response.statusCode + ", " + response.statusMessage + "\nData: " + data, true).andExit();
                            }
                        },
                        false, postData);
                }
            });
            return YaaS;
        },

        updatePackage : function() {
            addToQStack(function() {
                var service = state.srvId;
                var builderModule = state.moduleId;

                if(moduleDescriptorExists()) {
                    var appId = YaaS.generateAppId();

                    var packagePayload = {
                        "includedServices" : [service],
                        "builderModules" : [builderModule]
                    };

                    var postData = JSON.stringify(packagePayload);

                    process.stdout.write("Updating package ...");

                    http(apiBaseDomain, marketPlaceSrv + "/packages/" + state.pkgId, {
                            headers : {
                                'content-type' : 'application/json',
                                'content-length' : postData.length
                            },
                            'method': 'PUT'
                        }, function(data, response) {
                            if(response.statusCode == 200) {
                                _success(" Done.");
                                proceedQStack();
                            } else {
                                _error(" Failed.\n" + response.statusCode + ", " + response.statusMessage + "\nData: " + data, true).andExit();
                            }
                        },
                        false, postData);
                }
            });
            return YaaS;
        },

        createSubscription : function() {
            addToQStack(function() {
                if(ctx.project.id && state.pkgVersion) {
                    var subscriptionPayload = {
                        "project": ctx.project.id,
                        "packageVersion": state.pkgVersion,
                        "termsAccepted": true,
                        "currency": "USD",
                        "region": "en",
                        "tierCeiling": 2
                    };

                    var postData = JSON.stringify(subscriptionPayload);

                    process.stdout.write("Creating subscription ...");

                    http(apiBaseDomain, marketPlaceSrv + "/subscriptions?testSubscription=true", {
                            headers : {
                                'content-type' : 'application/json',
                                'content-length' : postData.length
                            },
                            'method': 'POST'
                        }, function(data, response) {
                            if(response.statusCode == 201) {
                                _success(" Done.");
                                proceedQStack();
                            } else {
                                _error(" Failed.\n" + response.statusCode + ", " + response.statusMessage + "\nData: " + data, true).andExit();
                            }
                        },
                        false, postData);
                }
            });
            return YaaS;
        },

        chooseTeam : function() {
            addToQStack(function() {
                getJSON(apiBaseDomain, accountSrv + "/organizations/" + ctx.org.id + "/teams?pageSize=1000&member=" + ctx.account, function(data) {
                    console.log(Colors.WHITE, "\nChoose a team:\n " , Colors.RESET);
                    for (var index = 0; index < data.length; index++) {
                        var element = data[index];
                        console.log((index+1) + " " + element.name);
                    }
                    rl.question(Colors.WHITE + "\nNumber: " + Colors.RESET, function(answer) {
                        var idx = parseInt(answer)-1;
                        ctx.team = { id: data[idx].id, name : data[idx].name};
                        YaaS.saveCTX();
                        proceedQStack();
                    });
                });
            });
            return YaaS;
        },

        chooseProject : function() {
            addToQStack(function() {
                getJSON(apiBaseDomain, accountSrv + "/organizations/" + ctx.org.id + "/projects?pageSize=1000&member=" + ctx.account, function(data) {
                    console.log(Colors.WHITE, "\nChoose a project:\n " , Colors.RESET);
                    for (var index = 0; index < data.length; index++) {
                        var element = data[index];
                        console.log((index+1) + " " + element.name);
                    }
                    rl.question(Colors.WHITE + "\nNumber: " + Colors.RESET, function(answer) {
                        var idx = parseInt(answer)-1;
                        ctx.project = { id: data[idx].id, name : data[idx].name};
                        YaaS.saveCTX();
                        proceedQStack();
                    });
                });
            });
            return YaaS;
        },

        chooseOrganization : function() {
            addToQStack(function() {
                getJSON(apiBaseDomain, accountSrv + "/organizations" + "?transitive=true&member=" + ctx.account, function(data) {
                    console.log(Colors.WHITE, "\nChoose an organization:\n ", Colors.RESET);
                    for (var index = 0; index < data.length; index++) {
                        var element = data[index];
                        console.log((index+1) + " " + element.name);
                    }
                    rl.question(Colors.WHITE + "\nNumber: " + Colors.RESET, function(answer) {
                        var idx = parseInt(answer)-1;
                        ctx.org = { id : data[idx].id, name : data[idx].name };
                        YaaS.saveCTX();
                        proceedQStack();
                    });
                });
            });
            return YaaS;
        },

        provideUserName : function() {
            addToQStack(function(tenant) {
                rl.question("Username ["+(ctx.account || "-")+"]:", function(answer) {
                    if(answer.trim().length > 0) {
                        ctx.account = answer;
                        YaaS.saveCTX();
                    }
                    proceedQStack();
                });
            });
            return YaaS;
        },

        providePassword : function() {
            addToQStack(function(tenant) {
                var password = "";
                rl.close();
                process.stdin.setRawMode(true);
                process.stdin.resume();
                process.stdout.write("Password: ");

                var inputHandler = function(raw) {
                    var character = raw + "";

                    if(character === "\n" ||
                        character === "\r" ||
                        character === "\u0004") {
                        process.stdout.write("\n");
                        process.stdin.setRawMode(false);
                        process.stdin.pause();
                        process.stdin.removeListener('data', inputHandler);
                        rl = readline.createInterface(process.stdin, process.stdout);
                        YaaS.password = password;
                        proceedQStack();
                    } else if(character.charCodeAt(0) === 127 /* unix */ || character.charCodeAt(0) === 8 /* windows */) {
                        if(password.length > 0) {
                            password = password.slice(0, -1);
                            //process.stdout.write("<");
                        }
                    } else if(character.charCodeAt(0) === 3) {
                        process.exit();
                    } else {
                        password += character;
                        //process.stdout.write("*");
                    }
                };

                process.stdin.on('data', inputHandler);
            });
            return YaaS;
        },

        login : function(useCurrentTenant) {
            addToQStack(function() {
                var tenant = useCurrentTenant ? ctx.team.id : undefined;
                var postData = "grant_type=password&client_id=" + clientId +
                    "&username=" + ctx.account + "&password=" + YaaS.password +
                    "&scope=" + encodeURIComponent(scope + (tenant ? " hybris.tenant=" + tenant : " hybris.no_tenant"));

                http(apiBaseDomain, oauthSrv + "/token", {
                        headers : {
                            'content-type' : 'application/x-www-form-urlencoded',
                            'content-length' : postData.length
                        },
                        'method': 'POST'
                    }, function(data, response) {
                        if(response.statusCode == 200) {
                            ctx.token = JSON.parse(data).access_token;
                            console.log(Colors.GREEN + "Authentication successful"+(tenant ? " for tenant '" + tenant + "'" : "")+"!" + Colors.RESET);
                            proceedQStack();
                        } else {
                            _error("Authentication error: " + response.statusCode + ", " +
                                response.statusMessage + "\nData: " + data).andExit();
                        }
                    },
                    true, postData);
            });
            return YaaS;
        },

        showProjectUrl : function() {
            addToQStack(function() {
                console.log("\nYou can now execute "+Colors.WHITE+"'builder runModule'"+Colors.RESET+
                    " in the current directory and open "+Colors.WHITE+"https://builder.yaas.io/#?selectedPath=/Home/"+
                    ctx.org.id+"/Projects/"+ctx.project.id + "/Overview" + Colors.RESET + " in your browser.");
                proceedQStack();
            });

            return YaaS;
        },

        showAuthenticationDetails : function() {
            addToQStack(function() {
                getJSON(apiBaseDomain, accountSrv + "/teams/" + ctx.team.id + "/apps/" + state.moduleId, function(data) {
                    console.log("\n************************************************************");
                    console.log("Builder Module Client ID: "+Colors.WHITE+data.clientId+Colors.RESET);
                    console.log("Project ID: "+Colors.WHITE+ctx.project.id+Colors.RESET);
                    console.log("************************************************************");
                    proceedQStack();
                });
            });

            return YaaS;
        },

        end: function() {
            addToQStack(function() {
                process.exit();
            });
            proceedQStack();
        },

        sleep: function(ms) {
            addToQStack(function() {
                setTimeout(function() {
                    proceedQStack();
                },ms);
            });
            return YaaS;
        }
    };
})();

module.exports = YaaS;