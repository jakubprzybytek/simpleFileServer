## File based http server

To speed up the work I decided to use some low level and lightweight library for the connectivity. I've used http://undertow.io/ as it already contains thread pool for blocking operations.

In contrast, to synchronise operations on files I decided to write it from scratch using Java 8 API - `ConcurrentHashMap` and `ReadWriteLock`.

### Synchronisation

In order to synchronize concurrent requests to the same file I am using `ReadWriteLock`. It allows to process GET operations in parallel as long there is no write access requested to that file.
Write operations (POST, PUT, DELETE) require exclusive access and therefor is only allowed when there is no other read nor write requests at a time. 

### Operations

Because there was no specific requirements on how should the server work I have assumed that following operations should be implemented:

#### Receive file (GET)
Inputs:
* _Request path_ - exact file name and location on server to download

CURL example:
`curl -i localhost:8080/test.txt`

Output:
* **200** - file download succeeded
* **400** - invalid request, f.e.: no file name
* **404** - requested file not found

#### Create new file (POST)
Inputs:
* _Request path_ - exact file name and location of the file to be created
* _Multi part form data_ - file content

CURL example:
`curl -i -F "data=@test.pdf" localhost:8080/test.txt`

Output:
* **201** - file created
* **400** - invalid request, f.e.: no file name
* **409** - file with that name and location already exist

#### Update file (PUT)
Inputs:
* _Request path_ - exact file name and location of the file to be created
* _Multi part form data_ - file content

CURL example:
`curl -i -X "PUT" -F "data=@test.pdf" localhost:8080/one20.pdf`

Output:
* **201** - file updated
* **400** - invalid request, f.e.: no file name
* **404** - requested file to update not found

#### Delete file (DELETE)
Inputs:
* _Request path_ - exact file name and location of the file to be created

CURL example:
`curl -i -X "DELETE" localhost:8080/test.txt`

Output:
* **200** - file deleted
* **400** - invalid request, f.e.: no file name
* **404** - requested file to delete not found

### Usage

###### Build fat jar

``mvn clean package -DskipTests``

###### Run server

`./start.bat`

or

``java -jar .\target\simpleFileServer-1.0-SNAPSHOT-jar-with-dependencies.jar``

(Tested on Win 10)

###### Run integration tests

``mvn test``