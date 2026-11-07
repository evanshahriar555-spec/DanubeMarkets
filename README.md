# Danube Markets

Java 25 Spring Boot coursework: MNB exchange rates and OANDA practice trading.

## Team

- Shahriar Mir Munem — C2TB9P — @evanshahriar555-spec
- Tamer Al Fares — Q542ZU — @tameralfares130-ui

## Run locally

Download homework.jar from this repository root and run with Java 25:

```sh
java -jar homework.jar
```

Open http://localhost:8080. H2 data is stored under C:\data on Windows.

## Published status

The imported application source and rebuilt homework.jar are now at the repository root. The Java 25 publication workflow passed on 7 November 2026, including the Maven tests and React build. Both creator names are in the default application configuration.

The source was imported from an existing assisted draft. Repository setup and automated publication commits do not establish five collaborative development stages or individual authorship. Actual contributions and reviews must still be recorded by the team.

## Features

Bootswatch Darkly responsive theme; MNB SOAP with Chart.js; OANDA practice account/prices/history/open/positions/close; calculator with JUnit and MockMvc tests; built React/Axios CRUD with file-backed H2.

## Remaining submission work

- [ ] Tamer accepts the collaborator invitation.
- [ ] Configure OANDA v20 practice access locally and capture successful results for all six Forex functions. Never commit tokens or passwords.
- [ ] Record genuine implementation changes and reviews from both members, with task ownership and meaningful development stages.
- [ ] Complete the illustrated PDF (at least ten pages), including the repository URL, actual contribution record, test evidence and OANDA screenshots.
- [ ] Each member submits their own Name-NeptunCode.pdf to Teams. Keep private evaluation credentials out of this public repository.

Project board: https://github.com/users/evanshahriar555-spec/projects/2

## Build and test

Use JDK 25, Maven and Node.js. In frontend, run npm ci and npm run build, then run mvn clean verify at the repository root. Copy target/homework.jar to homework.jar after rebuilding. The manual Publish homework source and JAR workflow performs the same build and commits the matching executable.
