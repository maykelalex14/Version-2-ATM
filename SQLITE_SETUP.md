# Download SQLite JDBC Driver

## Manual Download
Download sqlite-jdbc from: https://github.com/xerial/sqlite-jdbc/releases

Place the JAR file in the `lib/` directory

Example:
- Download: sqlite-jdbc-3.44.2.0.jar
- Place in: VendingMachine/lib/sqlite-jdbc-3.44.2.0.jar

Then compile with:
```
javac -cp lib/*:src src/*.java src/core/*.java src/interfaces/*.java src/persistence/*.java src/services/*.java -d out
```

And run with:
```
cd out
java -cp .:../lib/* Main
cd ..
```
