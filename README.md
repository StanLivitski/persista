<!--
 |  This file is part of Persista.
 |  Copyright © 2014 Konstantin "Stan" Livitski
 |
 |  Persista is free software: you can redistribute it and/or modify
 |  it under the terms of the GNU Affero General Public License as published by
 |  the Free Software Foundation, either version 3 of the License, or
 |  (at your option) any later version.
 |
 |  This program is distributed in the hope that it will be useful,
 |  but WITHOUT ANY WARRANTY; without even the implied warranty of
 |  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 |  GNU Affero General Public License for more details.
 |
 |  You should have received a copy of the GNU Affero General Public License
 |  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 |
 |  Additional permissions under GNU Affero GPL version 3 section 7:
 |
 |  1. If you modify this Program, or any covered work, by linking or combining
 |  it with any library or component covered by the terms of Eclipse Public
 |  License version 1.0 and/or Eclipse Distribution License version 1.0, the
 |  licensors of this Program grant you additional permission to convey the
 |  resulting work. Corresponding Source for a non-source form of such a
 |  combination shall include the source code for the aforementioned library or
 |  component as well as that of the covered work.
 |
 |  2. If you modify this Program, or any covered work, by linking or combining
 |  it with the Java Server Pages Expression Language API library (or a
 |  modified version of that library), containing parts covered by the terms of
 |  JavaServer Pages Specification License, the licensors of this Program grant
 |  you additional permission to convey the resulting work.
 |
 |=========================================================================== -->
<a name="sec-about"> </a>
What is persista?
==================

Persista is a framework for Java Standard Edition applications that store
relational data according to the [JPA specification][JPA-spec]. It helps
application developers use [JPA][] for data storage without the expense and
complexity of an Enterprise Edition deployment.  

Persista provides an application with database initialization and management
facilities, and sets up [Hibernate][] to enable [JPA][] data access.  

<a name="sec-depends"> </a>
Project's dependencies
======================

Persista uses the infrastructure of [Springlet][] framework and [proper2][]
library to provide the application with a container, command-line interface,
configuration, and logging. It relies on the [Hibernate ORM][Hibernate] to
generate database schema and provide [JPA][] data access.

The following table lists the framework's dependencies. You must have all
these components when you compile the framework. When running persista's tests,
tools, or applications, your classpath has to include their transitive
dependencies as well.

<table width="70%" border="1" cellpadding="0" cellspacing="0">
<col />
<col width="20%" />
<col width="20%" />
<col width="20%" />
<thead>
<tr>
<th>Library or framework</th>
<th>Importance</th>
<th>Module</th>
<th>Tested with version</th>
</tr>
</thead>
<tbody align="center" valign="middle">
<tr>
<td><a href="https://github.com/StanLivitski/Springlet">
Springlet</a></td>
<td>required</td>
<td>complete framework <small>(must be built with the proper2 dependency)</small></td>
<td>current stable version</td>
</tr>
<tr>
<td><a href="https://github.com/StanLivitski/proper2">
proper2</a></td>
<td>required</td>
<td>-</td>
<td>current stable version</td>
</tr>
<tr>
<td><a href="https://github.com/StanLivitski/JPA-PUB">
JPA PUB</a></td>
<td>required at compile time</td>
<td>-</td>
<td>current stable version</td>
</tr>
<tr>
<td><a href="http://hibernate.org/orm/">
Hibernate</a></td>
<td>required</td>
<td>hibernate-entitymanager</td>
<td>4.2.3 final</td>
</tr>
<tr>
<td><a href="http://commons.apache.org/proper/commons-logging/download_logging.cgi">
Apache Commons Logging</a></td>
<td>required</td>
<td>-</td>
<td>1.1.1</td>
</tr>
<tr>
<td>JDBC driver<br/>
<small>(e.g. <a href="http://dev.mysql.com/downloads/connector/j/">MySQL Connector/J</a>)</small></td>
<td>required</td>
<td>-</td>
<td>MySQL Connector/J 5.1.16</td>
</tr>
<tr>
<td><a href="http://junit.org/">
Junit</a></td>
<td>optional</td>
<td>-</td>
<td>4.5</td>
</tr>
</tbody>
</table>

Persista does not interact with a database server directly, and the
technologies it uses to access and manage data are portable across servers.
Thus, persista should work with any database management system compatible
with [JDBC][] and [Hibernate][]. Persista was written and tested with a
[MySQL][] server version 5.5 storing its databases. 

<a name="sec-download"> </a>
Downloading the binary and Javadoc
==================================

The binary and compressed Javadoc pages of the persista framework will be available at:

 - <https://github.com/StanLivitski/persista/wiki/Download>

<a name="sec-ivy"> </a>
Downloading dependencies with Ivy
---------------------------------

_This documentation section has yet to be written. If you would like to help writing it,
please [contact the project's team](#sec-contact)_

<a name="sec-use"> </a>
Using persista
==============

For detailed persista API information, please consult the project's [javadoc][].

<a name="sec-jpa"> </a>
Bootstrapping JPA
-----------------

_This documentation section has yet to be written. If you would like to help writing it,
please [contact the project's team](#sec-contact)_

<a name="sec-tran"> </a>
Transaction helpers 
-------------------

_This documentation section has yet to be written. If you would like to help writing it,
please [contact the project's team](#sec-contact)_

<a name="sec-raw"> </a>
Raw data access and SQL scripts
-------------------------------

_This documentation section has yet to be written. If you would like to help writing it,
please [contact the project's team](#sec-contact)_

<a name="sec-cmdtools"> </a>
Command-line tools 
------------------

_This documentation section has yet to be written. If you would like to help writing it,
please [contact the project's team](#sec-contact)_

<a name="sec-cmdtools"> </a>
Launching tools with Ant 
------------------------

_This documentation section has yet to be written. If you would like to help writing it,
please [contact the project's team](#sec-contact)_

<a name="sec-config"> </a>
Configuring persista 
---------------------

_This documentation section has yet to be written. If you would like to help writing it,
please [contact the project's team](#sec-contact)_

<a name="sec-schema"> </a>
Maintaining schema 
------------------

_This documentation section has yet to be written. If you would like to help writing it,
please [contact the project's team](#sec-contact)_

<a name="sec-db"> </a>
Database management
-------------------

_This documentation section has yet to be written. If you would like to help writing it,
please [contact the project's team](#sec-contact)_

<a name="sec-admin"> </a>
Server management and security
------------------------------

_This documentation section has yet to be written. If you would like to help writing it,
please [contact the project's team](#sec-contact)_

<a name="sec-testing"> </a>
Unit testing
------------

_This documentation section has yet to be written. If you would like to help writing it,
please [contact the project's team](#sec-contact)_

<a name="sec-repo"> </a>
About this repository
=====================

This repository contains the source code of persista. Its top-level components are:

        src/                persista's source files
        lib/                an empty directory for placing links or copies of
                             dependency libraries
        test/               persista's unit test helpers and examples
        config/              database configuration for use with unit test examples
        .settings/          Eclipse configuration files for JPA-PUB 
                             compile-time processing
        LICENSE             Document that describes the project's licensing terms
        NOTICE              A summary of license terms that apply to persista
        build.xml           Configuration file for the tool (Ant) that builds
                             the binary and Javadoc
        README.md           This document
        ivysettings-jboss.xml
                            Ivy configuration file for downloading dependencies
        .classpath          Eclipse configuration file    
        .factorypath        Eclipse configuration file
        .project            Eclipse configuration file

<a name="sec-building"> </a>
Building persista
================

To build the framework's binary from this repository, you need:

   - A **Java SDK**, also known as JDK, Standard Edition (SE), version 6 or
   later, available from OpenJDK <http://openjdk.java.net/> or Oracle
   <http://www.oracle.com/technetwork/java/javase/downloads/index.html>.

   Even though a Java runtime may already be installed on your machine
   (check that by running `java --version`), the build will fail if you
   don't have a complete JDK (check that by running `javac`).

   - **Apache Ant** version 1.7.1 or newer, available from the Apache Software
   Foundation <http://ant.apache.org/>.

   - [Dependency libraries](#sec-depends), or links thereto in the `lib/`
   subdirectory of your working copy.

To build the core of persista, go to the directory containing its working copy
and run:

     ant jar

The result is a file named `persista.jar` in the `dist` directory.

To build the unit test components of persista, go to the directory containing a
working copy of persista and run:

     ant test-jar

The result is a file named `persista-test.jar` in the `dist` directory. To build
this target, you must have the optional [Junit dependency](#sec-depends) in the
`lib` directory.

<a name="sec-javadoc"> </a>
Building Javadoc
================

To build Javadoc for the project, make sure you have met the prerequisites
[listed above](#sec-building), go to the directory containing a working copy of
project's sources and run:

     ant javadoc

The result will be placed in the `javadoc` subdirectory. 

<a name="sec-contact"> </a>
Contacting the project's team
=============================

You can send a message to the project's team via the
[Contact page](http://www.livitski.com/contact) at <http://www.livitski.com/>
or via *GitHub*. We will be glad to hear from you!

   [javadoc]: #sec-download
   [Springlet]: https://github.com/StanLivitski/Springlet
   [proper2]: https://github.com/StanLivitski/proper2
   [JPA]: http://docs.oracle.com/javaee/6/tutorial/doc/bnbpz.html
   [JPA-spec]: http://download.oracle.com/otndocs/jcp/persistence-2_1-fr-eval-spec/index.html
   [Hibernate]: http://hibernate.org/orm/
   [MySQL]: http://www.mysql.com/
   [JDBC]: http://docs.oracle.com/javase/6/docs/technotes/guides/jdbc/index.html