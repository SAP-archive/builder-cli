---
title: 'Visibility Restrictions in the Builder'
service: 'Builder SDK'
area: 'Core'
type: Tutorial
order: 7
---


The Builder module is a UI client application that uses access tokens to call YaaS services. After a user logs in and selects a project, they can open a Builder module that will call services on their behalf. An access token is issued for the module, representing the Builder module's authority to perform actions on behalf of the current user within the selected tenant.
The scope of those actions depends on the **required scopes** declared by the Builder module itself and on the **user roles** the current user has for the given project.

The set of scopes that the Builder module can perform on behalf of the current user within the current project is called **effective scopes**.
<div class="panel info">
Read more about <a href="/overview/security/index.html#Scopes">scopes</a> and <a href="/overview/security/index.html#AuthorizationFlowsintheBuilder">authorization flows in the Builder</a>.
</div>

Based on the set of effective scopes, you can influence the visibility of elements in your module depending on user permissions.

For example, if a **DELETE WISHLIST** button in your module should trigger a DELETE request that is allowed only for members whose roles contain the `hybris.wishlist_delete` scope, you can hide the button for members with roles that don't include such scope.

The following tutorial describes how to use effective scopes to hide whole navigation nodes, hide specific components, or show form elements in read-only mode.

### Visibility restriction of nodes in navigation tree

If you want one of your nodes to only be visible to project members with certain permissions (perhaps because the view requires these permission in order to work properly), you can do that by setting the `scopes` property.

Widget configuration is shown in the example:

```
{
    "widgets": [
        {
            "id": "wishlist#1",
            "title": "Wishlist",
            "virtualChildren":"true",
            "settings": {
                "description" : "Main widget for the Wishlist.",
                "viewUrl": "/#!/wishlist/",
                "example_setting": "some value"
            },
            "scopes" : [
                "hybris.wishlist_view" , "hybris.wishlist_manage"
            ],
            "widgets": [
                {
                    "id": "dynamicNodeExample",
                    "title": "dynamicNode example",
                    "dynamic":"productId",
                    "settings": {
                        "viewUrl": "productDetails.html",
                        "example_setting": "some value"
                    }
                }
            ]
        }
    ]
}
```

In this example, the `wishlist#1` node (and its subnodes) is only visible for project members with roles containing the `hybris.wishlist_view` **or** `hybris.wishlist_manage` scopes.

### Display or hide Components

Using the scoped actions, you can display or hide view components depending on what scopes the current user has, which improves the user's experience in the Builder.

In this guideline, you can learn more about how to use scoped actions in your Builder module.

#### Import the file

In order to use the scoped actions functionality, you need to import the **componentRestrictions.js** file:

```html
<script src="https://builder.yaas.io/public/js/componentRestrictions.js"></script>
```

<div class="panel note">
The **builder.js** file must be included in the page as well, prior to the **componentRestrictions.js** file.
</div>

The **componentRestrictions.js** file introduces the **builder.componentRestrictions** module, which you need to include as a dependency in your Angular module.

```
var myBuilderModuleApp = angular.module('myBuilderModuleApp', ["builder", "builder.componentRestrictions"]);
```

#### Use the directive

The easiest way to use this is to use the **if-actions-allowed** directive. For example, if you have an action button that deletes a wishlist item:

```html
<button ng-click="deleteWishlistItem()">Delete</button>
```

This button should only be shown if the user that is currently signed in has the right to delete a wishlist item. This usually means that the user must have a certain scope, such as `hybris.wishlist_delete`. You can express it using the **if-actions-allowed** directive:

```html
<button if-actions-allowed="hybris.wishlist_delete" ng-click="deleteWishlistItem()">Delete</button>
```

You can also use more than one scope and use logical operators to create more complex conditions like this:
 
```html
<button if-actions-allowed="hybris.wishlist_delete || hybris.wishlist_manage" ng-click="deleteWishlistItem()">Delete</button>
```

In this case, the user that has any of the specified scopes (`hybris.wishlist_delete` or `hybris.wishlist_manage`, in this example) can delete a wishlist item.

You can use these logical operators:
 
<ul>
<li> && - logical AND</li>
<li> || - logical OR</li>
<li> brackets - to group sub-expressions to build even more complex expressions like: '(scope1 && scope2) || scope3'</li>
</ul>

#### Use the service

In your controller, you can inject the **scopedActionsSrv** service and do a scope-based check like this:

```js
myApp.controller('myCtrl', function($scope, scopedActionsSrv){
        if(scopedActionsSrv.isActionAllowed('hybris.wishlist_delete')) {
            // allowed
            ...
        } else {
            //not allowed
            ...
        }
        ...
    });
```

### Display 'read-only' elements

If you want to show read-only form inputs, depending on the current token restrictions, or any custom condition, you can use **editable** directive.
**Editable** element allows you to restrict modification of children elements. For example :

```html
<editable having="hybris.wishlistitem_manage" unless="{{wishlistItem.status === 'BLOCKED'}}">
   <div class="col-sm-6 col-xs-12">
        <div class="form-group">
            <label for="wihlistItemName" class="control-label">Name *</label>
            <input type="text"
                   class="form-control input-lg"
                   id="wihlistItemName"
                   ng-model="wishlistItem.name" name="Wishlist Item Name" required/>
        </div>
        <div class="form-group">
            <div class="checkbox">
                <input type="checkbox" id="wihlistItemActive" ng-model="wishlistItem.active">
                <label for="wihlistItemActive" class="control-label">Active</label>
            </div>
        </div>
    </div>
</editable>
```

Only users bearing access token with the `hybris.wishlistitem_manage` scope can modify wishlist items, unless the item is BLOCKED.
In other words, if the status of wishlist item is **BLOCKED** or, the user has no `hybris.wishlistitem_manage` scope within his access token, the input will be transformed into a paragraph element and the check box will be disabled and rendered in read-only mode.

Similarly to the **if-actions-allowed** directive, you can combine scopes in the *having* attribute using logical operators.

Alternatively, you can use **editable-for-actions** directive. This directive relies only on the access token restrictions.
So you wouldn't be able force read-only mode depending on the wishlist item status.

The example using **editable-for-actions** directive would look as follows:

```html
<form role="form" name="editWishlistItemForm" editable-for-actions="hybris.wishlistitem_manage">
    <div class="col-sm-6 col-xs-12">
        <div class="form-group">
            <label for="wihlistItemName" class="control-label">Name *</label>
            <input type="text"
                   class="form-control input-lg"
                   id="wihlistItemName"
                   ng-model="wishlistItem.name" name="Wishlist Item Name" required/>
        </div>
        <div class="form-group">
            <div class="checkbox">
                <input type="checkbox" id="wihlistItemActive" ng-model="wishlistItem.active">
                <label for="wihlistItemActive" class="control-label">Active</label>
            </div>
        </div>
    </div>
</form>
```

Here, you can also combine scopes using logical operators.
You can apply **editable-for-actions** directive on a single input elements. For example:

```html
<input type="text"
       class="form-control input-lg"
       id="wihlistItemName"
       ng-model="wishlistItem.name" name="Wishlist Item Name" required
       editable-for-actions="hybris.wishlistitem_manage"/>
```


#### Two modes of processing of 'read-only' elements

For the **editable** and **editable-for-actions** directives a `mode` can be specified that controls how read-only elements are rendered. These two modes are possible:

<ul>
<li> replace - this is default and applies also when you do not specify any mode. It turns all input elements into paragraph elements with the input value as text content.</li>
<li> disable - sets all input elements to readonly and disabled.</li>
</ul>

In this example the `disable` mode is used:

```html
<editable having="hybris.wishlistitem_manage" mode="disable">
   <div class="col-sm-6 col-xs-12">
        <div class="form-group">
            <label for="wihlistItemName" class="control-label">Name *</label>
            <input type="text"
                   class="form-control input-lg"
                   id="wihlistItemName"
                   ng-model="wishlistItem.name" name="Wishlist Item Name" required/>
        </div>
        <div class="form-group">
            <div class="checkbox">
                <input type="checkbox" id="wihlistItemActive" ng-model="wishlistItem.active">
                <label for="wihlistItemActive" class="control-label">Active</label>
            </div>
        </div>
    </div>
</editable>
```