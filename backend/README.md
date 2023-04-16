# Eon
Eon is a simple time management platform that allows users to easily control their time and plan schedule.

# Features
* REST API service
* User registration and login with JWT authentication
* Password encryption using BCrypt
* Role-based authorization with Spring Security
* Customized access denied handling
* NoSql integration
* Email sending via SMTP
* Bean validation via DTO layer
* User can enable notifications for the task, which will be sent to his email adress for the desired amount of time before the task (in days, hours, minutes)
* New task cannot be added if its time interrupt another task's time
* Tasks received via api can be sorted in various ways(e.g. descending/ascending order, both methods support data sorted by days, months, years, names et c.)
* User can create groups and add users to them. Group has public tasks that are shown for all members. Only admins can manage group's tasks and add/delete users.
* There is ability to add many users to group and delete many users from group.


# Technologies
* Spring Boot 3.0
* Spring Security
* JSON Web Tokens (JWT)
* BCrypt
* Gradle
* MongoDB
* Docker

## How to use (via docker)
Download .zip file with code, extract it, change directory to the root:
```bash
cd Agregator-master/Agregator-master
```
And create in root directory .env file with the following demo-credentials(notice that
you have to provide your own smtp application credentials!):
```text
MDB_USERNAME=rootuser
MDB_PASSWORD=rootpass
SMTP_USERNAME=yourapplicationemail@somemail.com
SMTP_PASSWORD=your_super_secret_password
```


Then run docker-compose.yml:
```bash
docker-compose up
```
Now application is available on **localhost:8091**

## Contributing

Pull requests are welcome. For major changes, please open an issue first
to discuss what you would like to change.

Please make sure to update tests as appropriate.



 
 
