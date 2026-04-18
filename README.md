# Waffle
A client-server chat app with peer-to-peer video conferences written in **Java**

## How to setup locally

### Linux/Mac Os
#### Dependencies
SQLite is required to run the server, you can easily install it by running
```
apt install sqlite3
```
on linux\
\
There is a make file provided, simply type **make** and then **make clean** to delete all class files
you can also do make **runServer** to run the server

### Windows
Install SQLite and add it to PATH, make sure the driver jar file is
in the same location as the Server.class file once compiled
then run\ 
```
java -cp .:sqlite-jbdc-3.53.0.0.jar Server
```
Server should then start after which clients can connect.
