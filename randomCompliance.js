var request = require("request");

var options = { method: 'POST',
  url: 'http://lab-dev.oneconnectft.com.sg:9000/compliance/_search/template',
  headers: 
   { 'postman-token': '911ee976-79b5-bea8-b533-8f5a4a832626',
     'cache-control': 'no-cache',
     'content-type': 'application/json' },
  body: 
   { id: 'randomCompliance',
     params: { size: 10, timestamp: '23134534377327' } },
  json: true };

request(options, function (error, response, body) {
  if (error) throw new Error(error);

  console.log(body);
});
