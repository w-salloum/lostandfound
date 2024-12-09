# Getting Started with Lost and Found Application

This guide will help you run the Lost and Found application, which uses Docker and a mock User service.

### Prerequisites

- **Docker** and **Docker Compose** installed on your machine.
- **Gradle** installed to build the project.

### Running the Application

Follow these steps to get the application up and running:

#### 1. Set up the MySQL database
Run the following command to start MySQL in a Docker container:

```bash
sudo docker-compose up -d mysql
````

This will start the MySQL database in the background.

#### 2. Build the application
Use Gradle to build the application:

```bash
./gradlew clean build
```
This will compile the source code and prepare it for deployment.

#### 3. Start the application
Run the following command to start the application (including all services defined in docker-compose.yml):

```bash
sudo docker-compose up -d --build
```
This will start the application and all necessary services, including the API, and bind them to ports on localhost.

#### 4. Access the Lost and Found API
The application will now be running, and you can access the API at:

```bash
http://localhost:8080/api/lost-items
```
#### 5. Mocked User Service
A mocked User Service is running on port 8081.
It has a predefined user with ID 123.
You can get details about this user by calling the following endpoint:

```bash
GET http://localhost:8081/users/123
```
#### 6. Mocked User Query
You can also query a list of users using this endpoint:

```bash
POST http://localhost:8081/users/query
```
The body of the request can be any valid payload for querying users.

### Stopping the Application
To stop the application and remove the running containers, use the following command:

```bash
sudo docker-compose down
```
This will gracefully stop and clean up all the services, including the MySQL database and the application.


### Needs to be updated

The integration tests are not covering all the scenarios. It needs to be improved and more tests need to be added.
