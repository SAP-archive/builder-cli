var express = require('express');
var bodyParser = require('body-parser');
var fs = require('fs');
var url = require('url');
var app = express();

app.set('port', process.env.PORT || 8082);
var currentBuilderModuleURI;
app.use(express.static(__dirname + '/public'));

var bodyParser=require("body-parser");
app.use(bodyParser.urlencoded({ extended: false }));
app.use(bodyParser.json());

app.get('/', function(req, res){
    res.send();
});

app.post('/download', function(req, res){
    var html = req.body.skeleton;
    var filename = req.body.filename + ".html";
    fs.writeFile(currentBuilderModuleURI+"/"+filename, html, function(err) {
        if(err) {
            return console.log(err);
        }
        console.log(filename + " was saved to " + currentBuilderModuleURI);
    });
    res.end();
});

app.post('/checkFilename', function(req, res){
    var filename = decodeURIComponent(req.body.filename) + ".html";
    if (fs.existsSync(currentBuilderModuleURI+'/'+filename)) {
        res.send('exist');
    }else{
        res.send('not exist');
    }
    res.end();
});

var server = app.listen(app.get('port'), function () {
    var host = 'localhost';
    currentBuilderModuleURI = process.argv[2];
    console.log("Example app listening at http://%s:%s", host, app.get('port'));
});