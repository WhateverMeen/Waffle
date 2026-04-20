make:
	javac Server/src/*.java
	javac -cp .:Client/Dependencies/*.jar Client/src/*.java

runServer:
	java -cp .:Server/sqlite-jdbc-3.53.0.0.jar Server/runServer

clean:
	find . -name "*.class" -type f -delete

