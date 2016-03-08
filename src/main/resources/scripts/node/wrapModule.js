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
    .showCurrentCTX()
    .chooseProject()
    .setCurrentTenant('project')
    .login(true)
    .showCurrentCTX()
    .createSubscription()
    .showAuthenticationDetails()
    .showProjectUrl()
.end();
