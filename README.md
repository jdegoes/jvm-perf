## Overview

The JVM is a rock-solid, battle-proven platform for developing and deploying applications. Thousands of engineering years have been invested into the facilities available on it, resulting in sophisticated garbage collection and just-in-time compilation mechanisms. The interaction between this platform and our applications is nuanced and often misunderstood.

In this 5-day workshop, the participants will learn about the two major runtime mechanisms of the JVM affecting performance - the JIT compiler and the garbage collector; techniques for writing performant JVM code and effective use of tools for analyzing performance.

## Who Should Attend

Engineers, SREs and tech leads responsible for production JVM applications.

## Prerequisites

Basic knowledge of the JVM; basic experience running JVM applications in production.

## Topics

 - Overview of the JVM
 - Garbage collection on the JVM
 - Types of garbage collectors and choosing a garbage collector for a workload
 - Analyzing allocations in JVM code
 - Writing JVM code that is lean on allocations
 - Analyzing bytecode to pinpoint allocations
 - Monitoring garbage collector performance at runtime
 - Understanding JIT compilation on the JVM
 - Benchmarking code using JMH
 - Analyzing JIT behaviour at runtime
 - Writing JIT-friendly JVM code

## Daily Structure

5 days, 4 hours a day starting.

## Attendance

Attendance at this workshop is fully remote. Attendees will be provided with a link to a remote meeting session the day before the event, in which they can see and hear the workshop, ask the instructor questions, and chat with other attendees.

## Usage

### From the UI

1. Download the repository as a [zip archive](https://github.com/jdegoes/jvm-perf/archive/master.zip).
2. Unzip the archive, usually by double-clicking on the file.
3. Configure the source code files in the IDE or text editor of your choice.

### From the Command Line

1. Open up a terminal window.

2. Clone the repository.

    ```bash
    git clone https://github.com/jdegoes/jvm-perf
    ```
5. Launch project provided `sbt`.

    ```bash
    cd jvm-perf; ./sbt
    ```
6. Enter continuous compilation mode.

    ```bash
    sbt:jvm-perf> ~ test:compile
    ```

Hint: You might get the following error when starting sbt:

> [error] 	typesafe-ivy-releases: unable to get resource for com.geirsson#sbt-scalafmt;1.6.0-RC4: res=https://repo.typesafe.com/typesafe/ivy-releases/com.geirsson/sbt-scalafmt/1.6.0-RC4/jars/sbt-scalafmt.jar: javax.net.ssl.SSLHandshakeException: sun.security.validator.ValidatorException: PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested targe

It's because you have an outdated Java version, missing some newer certificates. Install a newer Java version, e.g. using [Jabba](https://github.com/shyiko/jabba), a Java version manager. See [Stackoverflow](https://stackoverflow.com/a/58669704/1885392) for more details about the error.

## Legal

Copyright&copy; 2023 John A. De Goes. All rights reserved.