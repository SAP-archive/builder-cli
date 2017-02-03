var tree;
var nodeIndex = 0;
var editMode = true;
var tmpCmp;
var downloadAvailable = false;

var rootNode = {
    "name": "rootNode",
    "type" : "main",
    "settings": {
    }
};

function Node(data){
    this.settings = {};
    this.type = data;
    if(this.type === 'main'){
        this.id = 'rootNode';
    }else{
        this.id = name;
    }
    this.children = [];
};

function Tree(node){
    var rootNode = new Node(node);
    this._root = rootNode;
    this._index = 0;
};

function init(){
    if(localStorage.getItem('dataModel')){
        tree = JSON.parse(localStorage.getItem('dataModel'));
        $("#preview").empty().append(renderHTML(tree._root, true));
    }else{
        addNodeToTree('main');
    }
    var downloadLink = document.createElement("a");
    if(downloadLink.download !== undefined){
        downloadAvailable=true;
    }else{
        downloadAvailable=false;
    }
    $('.btn-download').hide();

};

function updateView() {
    if(editMode) {
        $("#treeString").empty().append(JSON.stringify(tree, null, 4).replace(/\n/g, "<br>").replace(/ /g, "&nbsp;"));
        $("#preview").empty().append(renderHTML(tree._root, editMode));
        localStorage.setItem('dataModel', JSON.stringify(tree));
    }
    else {
        var $body = $('body', $("#preview").get(0).contentWindow.document);
        $body.html(renderHTML(tree._root, editMode));
        var iframe = document.getElementById('preview');
        if(iframe){
            var frameDoc = iframe.contentDocument || iframe.contentWindow.document;
            if(frameDoc){
                var el = frameDoc.getElementById('slot1');
            }
        }
    }
};

function createFromTemplate(parentNodeName, typeTree, callback) {
    var id = addNodeTypeToTree(typeTree.type, parentNodeName, function() {});
    if(typeTree.children) {
        for(var i = 0; i < typeTree.children.length; i++) {
            createFromTemplate(id, typeTree.children[i], function() {})
        }
    }
    if(typeTree.template) {
        callback();
    }
}

function addNodeToTree(nodeType, parentNodeName){
    addNodeTypeToTree(nodeType, parentNodeName, updateView);
}

function addNodeTypeToTree(nodeType, parentNodeName, callback){
    if(!tree){
        tree = new Tree(nodeType);
        callback();
    }else{
        if(parentNodeName){
            var newNode = new Node(nodeType);
            newNode.id = "" + tree._index++;
            if(newNode) {
                $.ajax({
                    method : "GET",
                    url : "snippets/" + newNode.type + ".json",
                    async : false,
                    success : function(response) {
                        if(response.template) {
                            createFromTemplate(parentNodeName, response, callback);
                        } else {
                            var parentNode = searchNode(tree._root, parentNodeName);
                            parentNode.children.push(newNode);
                            newNode.settings = response;
                            callback();
                        }
                    },
                    error : function(error){
                        console.log('Something went wrong');
                        callback();
                    }
                });
            }
            return newNode.id;
        }
    }
    return null;
};

function searchNode(tree, parentNodeName){
    if(tree.id === parentNodeName) {
        return tree;
    }
    if (tree.children != null){
        var parentNode = null;
        for(var i=0; parentNode == null && i < tree.children.length; i++){
            if(tree.children[i].id === parentNodeName){
                return tree.children[i];
            }else{
                parentNode = searchNode(tree.children[i], parentNodeName);
            }
        }
        return parentNode;
    }
    return null;
};

function downloadHTML() {
    var html = renderHTML(tree._root);
    var htmlFileAsBlob = new Blob([html], {
        type: 'data:application/octet-stream,'
    });
    var fileNameToSaveAs = "index.html";

    var downloadLink = document.createElement("a");
    downloadLink.download = fileNameToSaveAs;
    downloadLink.innerHTML = "Download File";
    if (window.webkitURL !== null) {
        //Chrome stuff
        downloadLink.href = window.webkitURL.createObjectURL(htmlFileAsBlob);
    } else {
        //necessary for FF
        downloadLink.href = window.URL.createObjectURL(htmlFileAsBlob);
        downloadLink.onclick = deleteClickedElement;
        downloadLink.style.display = "none";
        document.body.appendChild(downloadLink);
    }
    downloadLink.click();
}

function downloadSkeleton(){
    var html = encodeURIComponent(renderHTML(tree._root));
    $.ajax({
        method: "POST",
        contentType: "application/json",
        url: 'http://localhost:8080/download',
        data: '{"skeleton" : "'+html+'"}',
        success: function(){console.log("file successfully saved")},
        dataType: 'html'
    });
}

function saveAsFunction(){
    var content = renderHTML(tree._root);
    var uriContent = "data:application/octet-stream," + encodeUriComponent(content);
    var newWindow = window.open(uriContent, 'neuesDokument');
}

function deleteClickedElement(event) {
    document.body.removeChild(event.target);
}

//TODO use new variables
function removeNode(nodeName, parentNode){
    $('.y-settings').empty();
    $('#sidebar-tabs a[href="#components"]').tab('show');
    nodeName = nodeName.toString();
    var typeOfParentNode = typeof parentNode;
    if(typeOfParentNode === 'object'){
        for(var i=0; i<tree._root.children.length; i++){
            if(tree._root.children[i]){
                if(tree._root.children[i].id === nodeName.toString()){
                    tree._root.children.splice(i, 1);
                }
            }
        }
        updateView();
        return;
    }
    parentNode = parentNode.toString();
    if(parentNode!==undefined){
        var parentNode = searchNode(tree._root, parentNode);
        if(parentNode!==null){
            for(var i=0; i<parentNode.children.length; i++){
                if(parentNode.children[i]){
                    if(parentNode.children[i].id === nodeName){
                        parentNode.children.splice(i, 1);
                    }
                }
            }
        }
        updateView();
    }
};

function selectCmp(nodeId, parentNodeId){
    $('.cmpCtn').removeClass('cmpCtn--active');
    $('.y-editor-builder').addClass('y-editor-builder--active');
    var node = searchNode(tree._root, "" + nodeId);
    var nodeClass='.y-name-'+nodeId;
    $(nodeClass).addClass('cmpCtn--active');
    if(node) {
        showSettings(node);
        $('#sidebar-tabs a[href="#settings"]').tab('show'); // Select tab by name
    }
};

function showSettings(node) {
    var tmpSettings = JSON.parse(JSON.stringify(node.settings));
    $('.y-settings').empty();

    var keys = Object.keys(tmpSettings);
    $('.y-settings').append("<h3>"+ node.type + "("+ node.id +")" +" - Settings</h3><br/>");

    if(keys.length > 0) {

        for(var i = 0; i < keys.length; i++) {
            var key = keys[i];
            (function(key){
                var row = $("<div class='form-group'></div>");
                $('.y-settings').append(row);
                row.append("<label>"+key+"</label>");
                var input = $("<input type='text' class='form-control input-lg' value='" + tmpSettings[key] + "' key='"+key+"'>");
                row.append(input);
                input.change(function(){
                    tmpSettings[key] = input.val();
                });
            })(key);
        }

        $('.y-settings').append('<button class="btn btn-primary btn-lg btn-block">Save Settings</button>');
        $('.y-settings .btn').click(function() {
            node.settings = tmpSettings;
            updateView();
            $('#sidebar-tabs a[href="#components"]').tab('show');
        });
    }
    else{
        $('.y-settings').append("<code>Koi Säddings ahwähiläbbl!</code>");
    }
};

function preview() {
    if(editMode){
        $('.y-editor-builder').addClass('y-editor-builder--preview');
        $('.btn-preview .glyphicon').removeClass('glyphicon-eye-open');
        $('.btn-preview .glyphicon').addClass('glyphicon-eye-close');
        $('.btn-trash').hide();
        if(downloadAvailable){
            $('.btn-download').show();
        }
        $('#treeString').hide();
        editMode = false;
    }else{
        $('#treeString').show();
        $('.btn-download').hide();
        $('.btn-trash').show();
        $('.y-editor-builder').removeClass('y-editor-builder--preview');
        $('.btn-preview .glyphicon').removeClass('glyphicon-eye-close');
        $('.btn-preview .glyphicon').addClass('glyphicon glyphicon-eye-open');
        editMode = true;
    }
};

function showHTMLCode(){
    $('.y-code-inspector').addClass('y-code-inspector--active');
    $('.y-code-inspector .y-code-inspector-content').text(renderHTML(tree._root));
    hljs.initHighlighting.called = false;
    hljs.initHighlighting();
};

function closeHTMLCode(){
    $('.y-code-inspector').removeClass('y-code-inspector--active');
};

function renderHTML(node, useEditMode){
    if(node){
        var html;
        $.ajax({
            method : "GET",
            url : "snippets/" + node.type + ".html",
            async : false,
            success : function(response) {
                html = response;
            }
        });

        if(!tmpCmp){
            for(var setting in node.settings) {
                html = html.replace("{{SETTINGS." + setting + "}}", node.settings[setting]);
            }
        }else{
            for(var setting in tmpCmp.settings) {
                html = html.replace("{{SETTINGS." + setting + "}}", tmpCmp.settings[setting]);
            }
        }

        var childrenHtml ="";
        for(var i = 0; i < node.children.length; i++) {
            if(useEditMode){
                if(node.children[i].settings.SHOW_CMP_CTN !== false) {
                    childrenHtml += '<div class="cmpCtn y-name-'+ node.children[i].id+'" onclick="selectCmp(' + node.children[i].id + ','+ node.id + '); event.stopPropagation(); return false;"><div class="cmp-ghost"></div>';
                    childrenHtml += '<button class="btn btn-link cmp-btn" onclick="removeNode(' + node.children[i].id + ','+ node.id + ')"><span class="hyicon hyicon-remove"></span>';

                    childrenHtml += '</button>';
                    childrenHtml += renderHTML(node.children[i], useEditMode);

                    childrenHtml += '</div>';
                } else {
                    childrenHtml += renderHTML(node.children[i], useEditMode);

                    var ghostHtml = '<div style="width:100%; height: 100%; position: absolute" class="cmpCtn y-name-'+ node.children[i].id+'" onclick="selectCmp(' + node.children[i].id + ','+ node.id + '); event.stopPropagation(); return false;"><div class="cmp-ghost"></div>';
                    ghostHtml += '<button class="btn btn-link cmp-btn" onclick="removeNode(' + node.children[i].id + ','+ node.id + ')"><span class="hyicon hyicon-remove"></span>';
                    ghostHtml += '</button></div>';
                    childrenHtml = childrenHtml.replace("{{GHOST_CMP}}", ghostHtml);
                }
            }else{
                childrenHtml += renderHTML(node.children[i], useEditMode);
            }
        }

        if(useEditMode) {
            childrenHtml += '<div style="clear:both" class="y-dropzone" ondrop="drop(event, \''+ node.id +'\')" ondragover="allowDrop(event)"></div>';
        }

        if(useEditMode && node.type==="main") {
            html = childrenHtml;
        } else {
            html = html.replace("{{SLOT1}}", childrenHtml);
        }

        $('.y-editor-builder').removeClass('y-editor-builder--active');
        return html;
    }
};


function deleteDataModel(){
    localStorage.removeItem('dataModel');
    tree = null;
    $('.y-settings').empty();
    $('#sidebar-tabs a[href="#components"]').tab('show');
    addNodeToTree('main');
};