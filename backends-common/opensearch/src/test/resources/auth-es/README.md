## Resources explanation

### nginx-conf

Contains nginx configuration files:
 - reverse_elasticsearch.conf: allow nginx to be the proxy connecting to OpenSearch
 - passwd: Nginx credentials file store, each record follows the format: `username:encrypted-password`

TODO This set up is inherited from ElasticSearch where the security plugin was proprietary and this was our only way to 
test things like SSL, auth. Now that we use OpenSearch we could instead rely on OpenSearch native features.

### default.crt & default.key

public (.crt) and private (.key) of the self signed SSL certification. It will be loaded by nginx

### server.jks

Once you use a http client connect to nginx by `https` protocol, 
the default behavior of the client is rejecting the connection because 
self signed SSL certification provided by nginx is not recognized by the 
client. To deal with this problem, there are two ways to configure the client:

 - Not recommended in production, ignore SSL Validation
 - Configure the client to use local TrustStore file containing the .crt, 
 then it should trust nginx.
 
The `server.jks` is generated by the command
```
keytool -import -v -trustcacerts -file default.crt -keystore server.jks -keypass mypass -storepass mypass
```

With: 
 - password: `mypass`