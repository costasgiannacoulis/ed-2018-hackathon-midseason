# European Dynamics - Code.hub DVD Store Mid-Season Hackathon
This project will host the code of the mid-season hackathon.

## Description
The document describes the goal of the Hackathon dealing with the management of a DVD Store. The aim of this document is to gather, analyze and give an insight of the DVD Store management system. Nevertheless, it also concentrates on the capabilities required by management and their customer needs.

###	Project Perspective
DVD Store management system is a platform managing DVDs organized in a series of categories, along with their corresponding rentals. It consists of three sub-systems; DVD Store, Administration/Management and Reporting Services. Based on the fact that we are not covering
- User Interface(browser)
- Web (Spring MVC)
- Databases (Spring Data)

features will be considered complete upon successful return of a valid file per action.

For every action needed to be addressed, we will use a group of directories and specific files with specific structure commands to trigger actions. E.g. in order to load list of DVDs, we need to define a specific directory (e.g. /dvdstore/dvds), a specific file name (e.g. load-00001.txt) with a specific file text format (e.g. CSV).

Details will be given inside the classroom before hackathon begins.

### Features
- Create a new Spring Boot project using latest library versions using the following naming convention **org.acme:dvdstore:2018.1.0.SNAPSHOT**.
- Define domain model.
- Implement core functionality per entity (Service Layer).
- Implement core data storage functionality (Repository Layer).
- Define the directory structure for all entities.
- Define file naming convention for every supported action.
- Define file format.
- Entire configuration must be externalized as properties.
- Define specific business actions (e.g. rent a DVD for a given customer).
- Implement specific logging policy (e.g. change logging mechanism implementation, and/or have specific files contain specific logs, rotation policy).
- Load existing data during application startup.
- Save existing data before application exits.
- Generate reports of specific format (XLSX). We need at least the list of the DVDs that are currently rented.

### Operating Environment
DVD Store should be able to run on every operating system supporting Java 8 or greater.

Its underlying data repository should be based on Java data structures.
