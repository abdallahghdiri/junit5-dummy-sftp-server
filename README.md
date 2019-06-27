# JUNIT5 DUMMY SFT SERVER
Junit5 dummy sftp server project contains a junit extension that starts an in-memory SFTP server for each test

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