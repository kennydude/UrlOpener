console.log("Host Server Client tool.");
console.log("This allows you to easily use the software on your device");
console.log("You should have already installed the latest version of Dev Tools+ and");
console.log("Enabled the server");

var	execSync = require('execSync'),
	nconf = require('nconf');

console.log("------------------------------------------------");
console.log("Setting up port forwarding...");

var code = execSync.code('adb forward tcp:4000 tcp:4000');
if(code != 0){
	console.log("Failure");
	process.exit(-1);
}

nconf.env().argv();

var	otherPort = nconf.get("proxy:port") || 80,
	bridgePort = 4000;

console.log("Proxy is set to run from port :" + otherPort);

var	requests = {};

console.log("Connecting to device on :" + bridgePort);

var	WebSocket = require('ws'),
	net = require("net");
var socket = new WebSocket('ws://127.0.0.1:' + bridgePort + "/",{ "path" : "/", "transports" : ["websocket"] } );

socket.on("open", function(){
	console.log("Connected to device on device");
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

			l = msg['c'];

			l = l.replace("Connection: keep-alive\r\n", "");
			req.write(l);
			
		} catch(e){ console.log(e); }
	}
});

socket.on("close", function(){
	console.log("Disconnected from device");
});

