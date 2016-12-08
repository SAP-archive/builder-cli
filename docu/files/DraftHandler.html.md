---
title: 'Draft and dirty state handling in the Builder'
service: 'Builder SDK'
area: 'Core'
type: Tutorial
order: 6
---

You can react on unsaved states in the Builder module you are developing using draft and dirty state handling in the Builder. This helps to prevent data loss and improve the user experience in the Builder. Draft and dirty state handling are two different terms, but they are interrelated:
<ul>
<li>You can implement dirty state handling without draft handling.</li>
<li>In order to implement draft handling, you need to set the dirty flag first.</li>
</ul>

In this guideline, you can learn more about implementing draft and dirty state handling in your Builder module.

### Set a dirty flag

Set a dirty flag for all the places where a user can change the state in your Builder module. By doing this, you prevent data loss. The following example demonstrates how to set a dirty flag properly:

```js
    Builder.notificationManager.markDirty(true);
```

The following example demonstrates how to set a dirty flag for an AngularJS form:

```js
    $scope.$watch("form.$dirty", function(isDirty) {
        Builder.notificationManager.markDirty(isDirty);
    });
```

If the dirty flag is set to `true` and the user wants to leave a page, they receive a modal confirmation dialog about a possible data loss from the Builder side.

### Handle drafts

Draft handling in the Builder enables you to react on user unsaved states. If you turn on draft handling in your Builder module, users can leave a page marked as dirty and come back later without losing data. When the user returns, the Builder sends a message to ask if they want to continue editing the previously unsaved draft or not.

To enable draft handling, you must specify exactly the place and data in the Builder for which the functionality should work. After loading data/state, add a **DraftHandler** as shown in the example:

```js
    Builder.notificationManager.addDraftHandler([DraftOptionsA, DraftOptionsB, ...], DraftHandlerCallback);
```

You can only add one **DraftHandler** per view. For each draft you want to save, you must pass a **DraftOptions** object which must include the following properties:  

<ul>
<li>**id** - The ID of the draft that should be saved.</li>
<li>**ref** - A local reference to an object meant to be a draft.</li>
<li>**props** - The properties of the reference to the object which will be saved.</li>
<li>**DraftHandlerCallback** - If the user confirms that they want to continue editing their unsaved state, a function is called with the **draft** object. The **draft** object creates a sub-object with the ID as the name of the draft that has the properties defined in **props**.</li>
</ul>

#### Example implementation

```js
    var currentStateObject = {
        firstname : "Flappy",
        lastname : "Swinka"
    };

    Builder.notificationManager.addDraftHandler([{"id":"myDraftIdForMyUiModule", "ref": currentStateObject,"props":["firstname", "lastname"]}], function(draft) {
        currentStateObject.firstname = draft.myDraftIdForMyUiModule.firstname;
        currentStateObject.lastname = draft.myDraftIdForMyUiModule.lastname;

        Builder.notificationManager.markDirty(false);

        /* Other things you have to do to apply a draft to your current Builder module state 
        e.g. for AngularJS you have to digest the scope since we're in an asynchronous callback : 
        
        $scope.$digest();
        or
        $scope.$apply();        
        
        */
    });
```
