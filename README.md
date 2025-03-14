#  GitHub Branch Difference Analyzer

This project provides a library that identifies files changed independently in two branches of a GitHub repository: one branch hosted remotely (`branchA`) and another created locally (`branchB`). The tool determines the common changed files without fetching `branchA` locally.

## Features

- Identifies the merge base (latest common commit) between two branches.
- Retrieves changed files from `branchA` using GitHub API.
- Retrieves changed files from `branchB` using local Git commands.
- Compares both lists and returns files that were modified in both branches.
- Handles API errors and Git command failures gracefully.

## Requirements

- A GitHub repository with `branchA` available remotely and `branchB` created locally.
- A valid GitHub access token with repository access.
- Git installed on your machine.
- Java 17+ installed.
- Maven installed for dependency management and testing.

## Installation & Setup

### 1. Clone the Repository

```sh
git clone https://github.com/conamiflo/git-branch-diff-analyzer.git
cd git-branch-analyzer
```

### 2. Set Up GitHub Access Token

Create a GitHub access token with repository permissions and set it as an environment variable:

```sh
export GITHUB_ACCESS_TOKEN=your_token_here # On Linux/macOS
set GITHUB_ACCESS_TOKEN=your_token_here    # On Windows (cmd)
```

Alternatively, you can define the token in your Java code:

```java
String accessToken = System.getenv("GITHUB_ACCESS_TOKEN");
```

### 3. Build the project

Compile the project and package it into a JAR file:

```java
mvn clean package
```

### 4. Run the program

Run the program with the required parameters:

```java
java -jar target/git-branch-analyzer-1.0-SNAPSHOT.jar <owner> <repo> <branchA> <branchB> <localRepoPath>
```

### 5. Output

If there are common modified files, they will be listed in the logs; otherwise, a message will indicate no conflicts.

## Running Tests

To execute unit tests, run:

```sh
mvn test
```

Tests ensure:

- Correct API request handling.
- Parsing of JSON responses.
- Execution of Git commands.
- Proper error handling.

## Technologies Used

- Java 17+
- JUnit 5 (for testing)
- Mockito (for mocking dependencies)
- AssertJ (for assertions)
- SLF4J (for logging)
- Git (executed via command line)
- GitHub REST API




