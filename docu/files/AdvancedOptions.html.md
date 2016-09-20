---
title: 'Advanced options'
service: 'Builder SDK'
type: Tutorial
layout: 'tools'
order: 5
---

### Authorization Manager

The Builder contains a built-in authorization mechanism for REST calls coming from external Builder modules.
This mechanism is based on the [Restangular](https://github.com/mgonto/restangular) plug-in and uses its request and error interceptors to add access token headers to your requests, as well as to handle authorization errors.
It is recommended to use Restangular because of this out-of-the-box feature. However, if this does not work for your situation, you can also use the **Authorization Manager** to implement custom authorization for your Builder module.

The Authorization Manager is a JavaScript object designed for authorization-related purposes.

|Function| Description |Service Call Authentication|
|--------|-----------------------|----------------|
|Builder.authManager().getAccessToken()|Gets the access token that should be used when making REST calls to services. It contains scopes based on the current user and the package to which your module is registered.|Use the function to get a valid access token and include this token in the authorization header, which is required by the request.|
|Builder.authManager().handleAuthError(navigationState, response)|Using this function, you notify the Builder that there was an authorization problem during a REST call (a `401` response code was generated), which usually means that the access token has expired. The Builder handles that example by getting a new token or by redirecting you to the login screen.|Use the function to properly handle the `401 Unauthorized` response caused by the fact that your token is no longer valid.|
