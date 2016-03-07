var YaaS = require("./yaasCommon.js");

YaaS.loadCTX();

YaaS
    .provideUserName()
    .providePassword()
    .login()
    .chooseOrganization()
    .chooseTeam()
    .login(true)
    .chooseOrCreateService()
    .createBuilderModule()
    .createPackage()
    .updatePackage()
    .chooseProject()
    .createSubscription()
    .showAuthenticationDetails()
    .showProjectUrl()
.end();
