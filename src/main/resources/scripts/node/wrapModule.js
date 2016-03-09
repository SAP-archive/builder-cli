var YaaS = require("./yaasCommon.js");

YaaS.loadCTX();

YaaS
    .provideUserName()
    .providePassword()
    .login()
    .chooseOrganization()
    .chooseTeam()
    .setCurrentTenant('team')
    .login(true)
    .chooseOrCreateService()
    .createBuilderModule()
    .createPackage()
    .updatePackage()
    .chooseProject()
    .setCurrentTenant('project')
    .login(true)
    .createSubscription()
    .setCurrentTenant('team')
    .login(true)
    .showAuthenticationDetails()
    .showProjectUrl()
.end();
