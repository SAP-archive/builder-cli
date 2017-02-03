var express = require('express');
var bodyParser = require('body-parser');
var fs = require('fs');
var url = require('url');
var app = express();
var currentBuilderModuleURI;
app.use(express.static(__dirname + '/public'));

var bodyParser=require("body-parser");
app.use(bodyParser.urlencoded({ extended: false }));
app.use(bodyParser.json());

app.get('/', function(req, res){
    res.send();
});

app.post('/download', function(req, res){
    var html = decodeURIComponent(JSON.stringify(req.body.skeleton));
    fs.writeFile(currentBuilderModuleURI+"/index.html", html, function(err) {
        if(err) {
            return console.log(err);
        }
        console.log("index.html was saved to " + currentBuilderModuleURI);
    });
    res.end();
});

var server = app.listen(8080, function () {
    var host = 'localhost';
    var port = 8080;
    currentBuilderModuleURI = process.argv[2];
    console.log("Example app listening at http://%s:%s", host, port);
});