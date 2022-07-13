# lacework-agent-analysis

Analyze Lacework agent report generating a CSV for analysis and graphing.

## Agent Report Format

Uses the Resources > Agents report exported in CSV format.

## Build and Package

Using maven and a modern Java runtime build and package with:

```
    mvn clean package
```

This will build a fat jar in the target directory.

## Run

There is help available:

```
    java -jar target/lacework-agent-analysis-1.0-SNAPSHOT.jar --help
```