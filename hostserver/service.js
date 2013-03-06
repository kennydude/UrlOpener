console.log("TODO: setup");

var	otherPort = 80,
	bridgePort = 4000;

var	requests = {};

console.log("Connecting to application on :" + bridgePort);

var	WebSocket = require('ws'),
	net = require("net");
var socket = new WebSocket('ws://127.0.0.1:' + bridgePort + "/",{ "path" : "/", "transports" : ["websocket"] } );

socket.on("open", function(){
	console.log("Connected to application on device");
});

socket.on("message", function(msg){
	msg = JSON.parse(msg);
	if(msg['event'] == "close"){
		try{
			console.log("#" + msg['id'] + " requested destroying");
			try{ requests[ msg['id'] ].destroy(); } catch(e){}
			requests[reqid] = undefined;
		} catch(e){ console.log(e); }
	} else{
		try{
			var reqid = msg['id'];
			var req = requests[reqid];
			if(req == undefined){
				req = net.connect({ port : otherPort });
				req.on("data", function(data){
					console.log("#" + reqid + ": Response");
					for(var i = 0; i < data.length; i+= 30){
						var m = Math.min(data.length, i+30);
						socket.send(reqid + " " + data.slice( i, m ).toString("base64") + "\n");
					}
				});
				req.on("end", function(data){
					setTimeout(function(){
						console.log("#" + reqid + " END");
						socket.send("E" + reqid + "\n");
					}, 1000);
					requests[reqid] = undefined;
				});
				req.on("error", function(e){ console.log("socket error: " + e); });
				requests[reqid] = req;
				console.log("#" + reqid + " new");
			}

			//line = new Buffer(msg['c'], "base64");
			//l = line.toString("utf8").replace("\n", "\r\n");
			l = msg['c'];

			if(l.indexOf("Connection: keep-alive") != -1){ return; }
			req.write(l);
		} catch(e){ console.log(e); }
	}
});

socket.on("close", function(){
	console.log("Disconnected from application");
});

