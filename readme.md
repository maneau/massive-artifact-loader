Massive Artifact Loader
=======================

## Origin of the project

I had the experience to work in a secure environment without any internet connexion. 
Maven is very power full except when you are isolate from internet
and getting artifact one by one from a computer to an other is becoming the hell.

Purpose
-------

This project provides two usage : 

* Exporting artifact recursively from internet _(ArtifactExporter job)_
* Importing Artifact from a list or parameter into nexus repository _(ArtifactImporter job)_

## How to run in command line 

The simplest way is using the fat-jar :

> java -cp .:massive-artifact-loader-0.1-jar-with-dependencies.jar **org.maneau.maventools.batch.(ArtifactExporter|ArtifactImporter)** **(PARAMETERS)**

_by adding . in the classpath the file config.properties and logack.xml can be find_

Configuration
-------------

All configuration can bt change with de properties file : _config.properties_ which have to be in the classpath

> enterprise.repository.user=admin
>
> enterprise.repository.password=admin123
>
> enterprise.repository.id=thirdparty
>
> enterprise.repository.url=http://localhost:8081/nexus/content/repositories/thirdparty
>
> enterprise.repository.isManager
>
> enterprise.repository.type=default
>
> central.repository.url=http://repo1.maven.org/maven2/
>
> local.repository.path=local-repo
>
> exported.artifacts.file=local_repo.list

### Usage of ArtifactExporter

* You can use by specify the artifact(s) in the parameter like :

> org.maneau.maventools.batch.ArtifactExporter **groupId:ArtifactId:version** _(groupId:ArtifactId:type:version:classifier)_ ...

* add _-r_ or _--recurse_ for downloading all dependencies as :

> org.maneau.maventools.batch.ArtifactExporter **-r** junit:junit:pom:3.8.2

As a result you get local directory containing the downloaded artifacts _./local-repo_ by default 
and the file with all downloaded artifacts : _./local_repo.list_ by default
    
### Usage of ArtifactImporter
The Importer job works pretty like the Exporter :

* You upload by parameters

> org.maneau.maventools.batch.ArtifactImporter **groupId:ArtifactId:version** _(groupId:ArtifactId:type:version:classifier)_ ...
>
> org.maneau.maventools.batch.ArtifactImporter junit:junit:pom:3.8.2

Note there is no recursive mode

* You can add a file containing the artifacts list

> org.maneau.maventools.batch.ArtifactImporter **-f filename.list** 

Typically you can use the generated file during the ArtifactExporter to import all

By Maneau(c)