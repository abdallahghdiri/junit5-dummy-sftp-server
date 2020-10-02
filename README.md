# JUNIT5 DUMMY SFTP SERVER

[![Build Status](https://travis-ci.org/abdallahghdiri/junit5-dummy-sftp-server.svg?branch=master)](https://travis-ci.org/abdallahghdiri/junit5-dummy-sftp-server) [![Coverage](https://sonarcloud.io/api/project_badges/measure?project=abdallahghdiri_junit5-dummy-sftp-server&metric=coverage)](https://sonarcloud.io/dashboard?id=abdallahghdiri_junit5-dummy-sftp-server)

Junit5 dummy sftp server project contains a junit extension that starts an in-memory SFTP server for each test

This project is heavily inspired by [stefanbirkner](https://github.com/stefanbirkner)'s [In Memory JUNIT 4 SFTP rule](https://github.com/stefanbirkner/fake-sftp-server-rule)

## Dependency

The library can be pulled from the central maven repository, this dependency should be added to the pom.xml file:

```xml
<dependency>
  <groupId>com.ghdiri.abdallah</groupId>
  <artifactId>junit5-dummy-sftp-server</artifactId>
  <version>0.2.0</version>
</dependency>
```

## Usage 

### Declarative definition

Declarative definition starts a server on a random port and adds the user **username** with password **password** 

```java
@ExtendWith(DummySftpServerExtension.class)
public class TestClass{
}

```

### Programmatic definition

Programmatic definition adds flexibility of port selection and custom credentials

```java
public class TestClass{
      @RegisterExtension
      static final DummySftpServerExtension extension = DummySftpServerExtension.Builder.create()
              .port(CUSTOM_PORT)
              .addCredentials(CUSTOM_USER, CUSTOM_PASSWORD)
              .addCredentials(CUSTOM_USER_1, CUSTOM_PASSWORD_1)
              .build();
}
```

An optional HOST Key can be configured for the in-memory SFTP server by passing the Key path to the ```hostKey(Path hostKeyPath)``` ```DummySftpServerExtension.Builder``` method

### Sample test

An **SftpGateway** instance can be injected as a test method parameter providing basic SFTP operations.

```java
@ExtendWith(DummySftpServerExtension.class)
public class TestClass{

    @Test
    public void testPayloadUpload(SftpGateway gateway) throws Exception {
        String content = "sample content";
        String path = "text.txt";

        // TODO upload a file to started sftp server

        // check that file was uploaded
        Assertions.assertTrue(gateway.fileExists(path), "Failed to upload file to SFTP server");

        //get remote file contents
        String remoteFileContents = gateway.getFile(path);
        
        // check remote file contents
        assertThat("Wrong contents for remote file", remoteFileContents, equalTo(content));
    }
}
```