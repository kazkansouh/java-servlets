# Java Serverlet Notes

**Primarily, this repo is for recording notes for personal use so is
very rough!**

This repository contains a minimal setup of a webapp in both Jetty and
Tomcat. I created it while trying to understand how they deal with
user input (e.g. query parameters vs path parameters vs post
parameters) and how they handle authentication w.r.t.
`j_security_token`. The webapp exposes a simple servlet `/dump` which
simply prints lots of information about the `HttpServletRequest`
(e.g. headers, cookies, parameters, etc.). The same servlet is also
exposed as `/dump-auth`, which requires form based authentication to
access it.


## Setup

The comparisons were made using

* `apache-tomcat-9.0.41`
* `jetty-home-11.0.0`

First download both of these. The `tomcat` directory needs to be
superimposed/copied into the tomcat directory. The `jetty-base`
directory should be sufficient and work as is.

Its recommended to change Tomcat's `server.xml` to only bind to local
interface. In the following its assumed that its bound to
`localhost:8001`.

As these two versions require slightly different import paths (jetty
11 has migrated to using `jakata` instead of `javax` import paths the
webapp has been provided twice. Apart from this, they are identical.

To compile the `dump` class, issue the following from the respective
`WEB-INF/classes` directories:

```sh
javac -cp .:/path/to/apache-tomcat-9.0.41/lib/servlet-api.jar pig/dump.java
javac -cp .:/path/to/jetty-home-11.0.0/lib/jetty-jakarta-servlet-api-5.0.1.jar pig/dump.java
```

To start the apps:

From within tomcat directory (should start service on `localhost:8000`):
```sh
./bin/catalina.sh run
```

From within `jetty-base` directory (should start service on `localhost:8001`):
```sh
java -jar /path/to/jetty-home-11.0.0/start.jar
```

## Curl Requests

The following show a number of `curl` requests to both versions of the
webapp. Standard `GET` and `POST` requests, and a number of attempts
at authentication to the service.

### Jetty 

Nominal get:
```
$ curl 'http://localhost:8000/jettyapp/dump;a=1?b=2' -i
HTTP/1.1 200 OK
Date: Thu, 28 Jan 2021 19:52:14 GMT
Content-Type: text/plain;charset=iso-8859-1
Set-Cookie: JSESSIONID=node0193lfsjgyqgpu1lcok6kxttjnl2.node0; Path=/jettyapp
Expires: Thu, 01 Jan 1970 00:00:00 GMT
Content-Length: 405
Server: Jetty(11.0.0)

method: GET
query: b=2
path translated: null
path info: null
request uri: /jettyapp/dump;a=1
servlet path: /dump
context path: /jettyapp
requested session id: null
session id: node0193lfsjgyqgpu1lcok6kxttjnl2
remote user: null
number params: 1
 * b: 2
number trailers: 0
number cookies: 0
 * org.eclipse.jetty.server.newSessionId
headers:
 * Accept: */*
 * User-Agent: curl/7.68.0
 * Host: localhost:8000
```

Nominal post:
```
$ curl 'http://localhost:8000/jettyapp/dump;a=1?b=2' -i -d c=3
HTTP/1.1 200 OK
Date: Thu, 28 Jan 2021 19:52:31 GMT
Content-Type: text/plain;charset=iso-8859-1
Set-Cookie: JSESSIONID=node0bput93ogjfko1a71gu6uvgqb03.node0; Path=/jettyapp
Expires: Thu, 01 Jan 1970 00:00:00 GMT
Content-Length: 485
Server: Jetty(11.0.0)

method: POST
query: b=2
path translated: null
path info: null
request uri: /jettyapp/dump;a=1
servlet path: /dump
context path: /jettyapp
requested session id: null
session id: node0bput93ogjfko1a71gu6uvgqb03
remote user: null
number params: 2
 * b: 2
 * c: 3
number trailers: 0
number cookies: 0
 * org.eclipse.jetty.server.newSessionId
headers:
 * Accept: */*
 * User-Agent: curl/7.68.0
 * Host: localhost:8000
 * Content-Length: 3
 * Content-Type: application/x-www-form-urlencoded
```

Setting session cookie by path parameter:
```
$ curl 'http://localhost:8000/jettyapp/dump;jsessionid=node0bput93ogjfko1a71gu6uvgqb03' -i
HTTP/1.1 200 OK
Date: Thu, 28 Jan 2021 19:56:56 GMT
Set-Cookie: JSESSIONID=node0bput93ogjfko1a71gu6uvgqb03.node0; Path=/jettyapp
Expires: Thu, 01 Jan 1970 00:00:00 GMT
Content-Type: text/plain;charset=iso-8859-1
Content-Length: 422
Server: Jetty(11.0.0)

method: GET
query: null
path translated: null
path info: null
request uri: /jettyapp/dump;jsessionid=node0bput93ogjfko1a71gu6uvgqb03
servlet path: /dump
context path: /jettyapp
requested session id: node0bput93ogjfko1a71gu6uvgqb03
session id: node0bput93ogjfko1a71gu6uvgqb03
remote user: null
number params: 0
number trailers: 0
number cookies: 0
headers:
 * Accept: */*
 * User-Agent: curl/7.68.0
 * Host: localhost:8000
```

Request page that needs authentication:
```
$ curl 'http://localhost:8000/jettyapp/dump-auth;a=1?b=2' -i -d c=3
HTTP/1.1 303 See Other
Date: Thu, 28 Jan 2021 19:59:18 GMT
Set-Cookie: JSESSIONID=node01jnhl92qsyse86tqwzs1e28925.node0; Path=/jettyapp
Expires: Thu, 01 Jan 1970 00:00:00 GMT
Location: http://localhost:8000/jettyapp/login.html;jsessionid=node01jnhl92qsyse86tqwzs1e28925.node0
Content-Length: 0
Server: Jetty(11.0.0)


$ curl 'http://localhost:8000/jettyapp/login.html;jsessionid=node01jnhl92qsyse86tqwzs1e28925.node0' -i
HTTP/1.1 200 OK
Date: Thu, 28 Jan 2021 19:59:55 GMT
Last-Modified: Thu, 28 Jan 2021 10:22:09 GMT
Content-Type: text/html
Accept-Ranges: bytes
Content-Length: 469
Server: Jetty(11.0.0)

<html>
  <head>
    <title>Login Page</title>
  </head>
  <body>
    <p>Authentication required for resource:</p>
    <form method="POST" action="j_security_check">
      <input type="hidden" name="clienttype" value="html">
      <p><input type="text" name="j_username" placeholder="Username"></p>
      <p><input type="password" name="j_password" placeholder="Password"></p>
      <p><input type="submit" name="submit" value="Login"></p>
    </form>
  </body>
</html>


$ curl 'http://localhost:8000/jettyapp/j_security_check;jsessionid=node01jnhl92qsyse86tqwzs1e28925.node0' -i -d 'j_username=user&j_password=password&some=param'
HTTP/1.1 303 See Other
Date: Thu, 28 Jan 2021 20:01:22 GMT
Set-Cookie: JSESSIONID=node0c8gq9t7qb9f76yyugoivblgi6.node0; Path=/jettyapp
Expires: Thu, 01 Jan 1970 00:00:00 GMT
Location: http://localhost:8000/jettyapp/dump-auth;a=1;jsessionid=node0c8gq9t7qb9f76yyugoivblgi6.node0?b=2
Content-Length: 0
Server: Jetty(11.0.0)


$ curl 'http://localhost:8000/jettyapp/dump-auth;a=1?b=2' -i -b JSESSIONID=node0c8gq9t7qb9f76yyugoivblgi6.node0
HTTP/1.1 200 OK
Date: Thu, 28 Jan 2021 20:03:26 GMT
Content-Type: text/plain;charset=iso-8859-1
Content-Length: 524
Server: Jetty(11.0.0)

method: POST
query: b=2
path translated: null
path info: null
request uri: /jettyapp/dump-auth;a=1
servlet path: /dump-auth
context path: /jettyapp
requested session id: node0c8gq9t7qb9f76yyugoivblgi6.node0
session id: node0c8gq9t7qb9f76yyugoivblgi6
remote user: user
number params: 2
 * b: 2
 * c: 3
number trailers: 0
number cookies: 1
 * JSESSIONID: node0c8gq9t7qb9f76yyugoivblgi6.node0
headers:
 * Cookie: JSESSIONID=node0c8gq9t7qb9f76yyugoivblgi6.node0
 * Accept: */*
 * User-Agent: curl/7.68.0
 * Host: localhost:8000
```

Notice, the redirect received from posting to `j_security_check`
returned a path `a=1;jsessionid=....`. This resulted in an error when
requesting from Jetty.

Also, it completely cached the first request. so the final request
result was correct despite being a `GET` without parameter `c`.

Unsolicited authentication without going to `/dump-auth`:
```
$ curl 'http://localhost:8000/jettyapp/j_security_check' -i -d 'j_username=user&j_password=password'
HTTP/1.1 303 See Other
Date: Thu, 28 Jan 2021 20:11:47 GMT
Set-Cookie: JSESSIONID=node0tsayspxuiya73tx01ypl164j10.node0; Path=/jettyapp
Expires: Thu, 01 Jan 1970 00:00:00 GMT
Location: http://localhost:8000/jettyapp;jsessionid=node0tsayspxuiya73tx01ypl164j10.node0
Content-Length: 0
Server: Jetty(11.0.0)
```

Authentication by only using `GET` requests:
```
$ curl 'http://localhost:8000/jettyapp/j_security_check?j_username=user&j_password=password' -i
HTTP/1.1 303 See Other
Date: Thu, 28 Jan 2021 20:13:12 GMT
Set-Cookie: JSESSIONID=node0ev8qs5ozn77o1i8mnealx3a5z11.node0; Path=/jettyapp
Expires: Thu, 01 Jan 1970 00:00:00 GMT
Location: http://localhost:8000/jettyapp;jsessionid=node0ev8qs5ozn77o1i8mnealx3a5z11.node0
Content-Length: 0
Server: Jetty(11.0.0)


$ curl 'http://localhost:8000/jettyapp/dump-auth;jsessionid=node0ev8qs5ozn77o1i8mnealx3a5z11.node0' -i
HTTP/1.1 200 OK
Date: Thu, 28 Jan 2021 20:13:45 GMT
Content-Type: text/plain;charset=iso-8859-1
Content-Length: 447
Server: Jetty(11.0.0)

method: GET
query: null
path translated: null
path info: null
request uri: /jettyapp/dump-auth;jsessionid=node0ev8qs5ozn77o1i8mnealx3a5z11.node0
servlet path: /dump-auth
context path: /jettyapp
requested session id: node0ev8qs5ozn77o1i8mnealx3a5z11.node0
session id: node0ev8qs5ozn77o1i8mnealx3a5z11
remote user: user
number params: 0
number trailers: 0
number cookies: 0
headers:
 * Accept: */*
 * User-Agent: curl/7.68.0
 * Host: localhost:8000
```

### Tomcat



Nominal get:
```
$ curl 'localhost:8001/tomcatapp/dump;a=1?b=2' -i 
HTTP/1.1 200 
Set-Cookie: JSESSIONID=5E3CF05A77F999CAB126D4E7950096F1; Path=/tomcatapp; HttpOnly
Content-Type: text/plain;charset=ISO-8859-1
Content-Length: 366
Date: Thu, 28 Jan 2021 20:26:45 GMT

method: GET
query: b=2
path translated: null
path info: null
request uri: /tomcatapp/dump;a=1
servlet path: /dump
context path: /tomcatapp
requested session id: null
session id: 5E3CF05A77F999CAB126D4E7950096F1
remote user: null
number params: 1
 * b: 2
number trailers: 0
number cookies: 0
headers:
 * host: localhost:8001
 * user-agent: curl/7.68.0
 * accept: */*
```

Nominal post:
```
$ curl 'localhost:8001/tomcatapp/dump;a=1?b=2' -i -d 'c=3'
HTTP/1.1 200 
Set-Cookie: JSESSIONID=325256AE66AF28CCCD6A176E1E67715D; Path=/tomcatapp; HttpOnly
Content-Type: text/plain;charset=ISO-8859-1
Content-Length: 447
Date: Thu, 28 Jan 2021 20:27:11 GMT

method: POST
query: b=2
path translated: null
path info: null
request uri: /tomcatapp/dump;a=1
servlet path: /dump
context path: /tomcatapp
requested session id: null
session id: 325256AE66AF28CCCD6A176E1E67715D
remote user: null
number params: 2
 * b: 2
 * c: 3
number trailers: 0
number cookies: 0
headers:
 * host: localhost:8001
 * user-agent: curl/7.68.0
 * accept: */*
 * content-length: 3
 * content-type: application/x-www-form-urlencoded
```

Setting session cookie by path parameter:
```
$ curl 'localhost:8001/tomcatapp/dump;a=1;jsessionid=325256AE66AF28CCCD6A176E1E67715D?b=2' -i -d 'c=3'
HTTP/1.1 200 
Content-Type: text/plain;charset=ISO-8859-1
Content-Length: 519
Date: Thu, 28 Jan 2021 20:30:05 GMT

method: POST
query: b=2
path translated: null
path info: null
request uri: /tomcatapp/dump;a=1;jsessionid=325256AE66AF28CCCD6A176E1E67715D
servlet path: /dump
context path: /tomcatapp
requested session id: 325256AE66AF28CCCD6A176E1E67715D
session id: 325256AE66AF28CCCD6A176E1E67715D
remote user: null
number params: 2
 * b: 2
 * c: 3
number trailers: 0
number cookies: 0
headers:
 * host: localhost:8001
 * user-agent: curl/7.68.0
 * accept: */*
 * content-length: 3
 * content-type: application/x-www-form-urlencoded
```

Request page that needs authentication:
```
$ curl 'localhost:8001/tomcatapp/dump-auth;a=1?b=2' -i -d 'c=3'
HTTP/1.1 200
Set-Cookie: JSESSIONID=28D71687B911303B9125A82E3CC0442B; Path=/tomcatapp; HttpOnly
Accept-Ranges: bytes
ETag: W/"469-1611848580700"
Last-Modified: Thu, 28 Jan 2021 15:43:00 GMT
Content-Type: text/html
Content-Length: 469
Date: Thu, 28 Jan 2021 20:35:59 GMT

<html>
  <head>
    <title>Login Page</title>
  </head>
  <body>
    <p>Authentication required for resource:</p>
    <form method="POST" action="j_security_check">
      <input type="hidden" name="clienttype" value="html">
      <p><input type="text" name="j_username" placeholder="Username"></p>
      <p><input type="password" name="j_password" placeholder="Password"></p>
      <p><input type="submit" name="submit" value="Login"></p>
    </form>
  </body>
</html>


$ curl 'localhost:8001/tomcatapp/j_security_check' -i -d 'j_username=user&j_password=password&some=param' -b JSESSIONID=28D71687B911303B9125A82E3CC0442B
HTTP/1.1 303
Set-Cookie: JSESSIONID=1D68EF00C2D6DD40A41F95CDE84F8AFF; Path=/tomcatapp; HttpOnly
Location: /tomcatapp/dump-auth;a=1?b=2
Content-Length: 0
Date: Thu, 28 Jan 2021 20:37:52 GMT


$ curl 'localhost:8001/tomcatapp/dump-auth;a=1?b=2' -i -b JSESSIONID=1D68EF00C2D6DD40A41F95CDE84F8AFF
HTTP/1.1 200
Cache-Control: private
Expires: Thu, 01 Jan 1970 00:00:00 GMT
Content-Type: text/plain;charset=ISO-8859-1
Content-Length: 485
Date: Thu, 28 Jan 2021 20:39:03 GMT

method: POST
query: b=2
path translated: null
path info: null
request uri: /tomcatapp/dump-auth;a=1
servlet path: /dump-auth
context path: /tomcatapp
requested session id: 1D68EF00C2D6DD40A41F95CDE84F8AFF
session id: 1D68EF00C2D6DD40A41F95CDE84F8AFF
remote user: user
number params: 2
 * b: 2
 * c: 3
number trailers: 0
number cookies: 0
headers:
 * content-length: 3
 * host: localhost:8001
 * content-type: application/x-www-form-urlencoded
 * user-agent: curl/7.68.0
 * accept: */*
```

Just like Jetty, it completely cached the first request. so the final
request result was correct despite being a `GET` without parameter
`c`.

Unsolicited authentication without going to `/dump-auth`:
```
$ curl 'localhost:8001/tomcatapp/j_security_check' -d 'j_username=user&j_password=password' -i 
HTTP/1.1 408 
Content-Type: text/html;charset=utf-8
Content-Language: en
Content-Length: 873
Date: Thu, 28 Jan 2021 20:33:40 GMT
Connection: close

<!doctype html><html lang="en"><head><title>HTTP Status 408 – Request Timeout</title>
...snip...
```

Authentication by only using `GET` requests:
```
$ curl 'localhost:8001/tomcatapp/dump-auth;a=1?b=2' -i
HTTP/1.1 200
Cache-Control: private
Expires: Thu, 01 Jan 1970 00:00:00 GMT
Set-Cookie: JSESSIONID=5A229A9709809B649271EFB9AFFDBAD8; Path=/tomcatapp; HttpOnly
Accept-Ranges: bytes
ETag: W/"469-1611848580700"
Last-Modified: Thu, 28 Jan 2021 15:43:00 GMT
Content-Type: text/html
Content-Length: 469
Date: Thu, 28 Jan 2021 20:52:11 GMT

<html>
  <head>
    <title>Login Page</title>
  </head>
  <body>
    <p>Authentication required for resource:</p>
    <form method="POST" action="j_security_check">
      <input type="hidden" name="clienttype" value="html">
      <p><input type="text" name="j_username" placeholder="Username"></p>
      <p><input type="password" name="j_password" placeholder="Password"></p>
      <p><input type="submit" name="submit" value="Login"></p>
    </form>
  </body>
</html>


$ curl 'localhost:8001/tomcatapp/j_security_check;jsessionid=5A229A9709809B649271EFB9AFFDBAD8?j_username=user&j_password=password' -i
HTTP/1.1 303
Set-Cookie: JSESSIONID=DE004295EEB24CE49207856D9F386A9D; Path=/tomcatapp; HttpOnly
Location: /tomcatapp/dump-auth;a=1;jsessionid=DE004295EEB24CE49207856D9F386A9D?b=2
Content-Length: 0
Date: Thu, 28 Jan 2021 20:53:13 GMT


$ curl 'localhost:8001/tomcatapp/dump-auth;a=1;jsessionid=DE004295EEB24CE49207856D9F386A9D?b=2' -i
HTTP/1.1 200
Cache-Control: private
Expires: Thu, 01 Jan 1970 00:00:00 GMT
Content-Type: text/plain;charset=ISO-8859-1
Content-Length: 448
Date: Thu, 28 Jan 2021 20:54:01 GMT

method: GET
query: b=2
path translated: null
path info: null
request uri: /tomcatapp/dump-auth;a=1;jsessionid=DE004295EEB24CE49207856D9F386A9D
servlet path: /dump-auth
context path: /tomcatapp
requested session id: DE004295EEB24CE49207856D9F386A9D
session id: DE004295EEB24CE49207856D9F386A9D
remote user: user
number params: 1
 * b: 2
number trailers: 0
number cookies: 0
headers:
 * host: localhost:8001
 * user-agent: curl/7.68.0
 * accept: */*
```




## Musings

Its quite odd that there is no differentiation between `GET` and
`POST` parameters. Even with logins. This is probably defined
somewhere in the servlet spec.

The main difference between both servlet is in the authentication
process. Tomcat directly returns the login page where Jetty returns a
redirect to the login page. The login page is configured in the
`web.xml` file. Again, after login is successful Tomcat directly
returns the result page and Jetty returns a redirect to the initially
requested page.

Also, by just looking at the format of the cookie its easy to
differentiate what the backend server is. I have also confirmed that
Tomcat 10 uses same format as Tomcat 9.

Jetty seems to struggle when multiple path parameters are present in
the request (Tomcat did not have this issue). E.g.:

* `/jettyapp/dump-auth;a=1;jsessionid=node01gglzkoq66uqdtsmhmzqwahq2.node0` results in a 404
* `/jettyapp/dump-auth;jsessionid=node01gglzkoq66uqdtsmhmzqwahq2.node0` succeeds
