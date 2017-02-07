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
var html = decodeURIComponent(req.body.skeleton);
    var filename = decodeURIComponent(req.body.filename) + ".html";
    fs.writeFile(filename, html, function(err) {
        if(err) {
            return console.log(err);
        }
        console.log(filename + " was saved!");
    });
    res.end();
});

var server = app.listen(8080, function () {
    var host = 'localhost';
    var port = 8080;
    currentBuilderModuleURI = process.argv[2];
    console.log("Example app listening at http://%s:%s", host, port);
});