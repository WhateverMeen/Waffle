make:
	javac Server/*.java
	javac Client/*.java

runServer:
	java -cp .:Server/sqlite-jdbc-3.53.0.0.jar Server/runServer

clean:
	find . -name "*.class" -type f -delete

