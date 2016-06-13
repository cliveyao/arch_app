## Project Description

Integration of front-end portal sites on the Internet for Web Applications build mainstream fashion latest open source technology , infrastructure development framework HTMl5 mobile site and back-end management system integration , providing a major source J2EE related technology architecture to integrate enterprise applications , and some basic common features and components best practices and reference design prototype implementation.

### Projects hosted synchronization update site list :

** Https: //github.com/xautlx/s2jh4net**

** Http: //git.oschina.net/xautlx/s2jh4net**

### Personal Space : http://my.oschina.net/s2jh

> ** Tip **: In order to link the user to distinguish between external and internal resources, particularly in the document [link] (http://git.oschina.net/xautlx/s2jh4net/raw/master/src/main/webapp/! docs / markdown / images / link.gif) ID: link icon in front of this note is an external link if you are already familiar with the concept of negligible click; no explanation is this identification document internal links, it is recommended to click through to a complete inspection project documentation.

## Frame Features

* Common interface and basic functionality designed for mainstream enterprise application system to achieve WEB
* Based on the mainstream body (Spring MVC + Spring3 + Hibernate4 / MyBatis3) architecture
* The introduction of JPA, Spring-Data-JPA persistence layer to enhance the regulatory framework and development efficiency
* Based on the popular JQuery / Bootstrap framework and plug-ins and other UI integration, good browser compatibility and support for mobile devices
* Provide a basis for code generation framework, simplifying basic CRUD functions for fast development
* Maven-based project management and components depend, convenient and efficient, integrated and sustainable integrated development

## Technology Architecture

* [Technology List] (https://github.com/xautlx/s2jh4net/blob/master/src/main/webapp/docs/markdown/ technology list .md) - Technical Framework (Java / Web / Tool) Component List introduction
* [Technical features] (https://github.com/xautlx/s2jh4net/blob/master/src/main/webapp/docs/markdown/ technical characteristics .md) - Technical Selection and description
* [Exception handling] (https://github.com/xautlx/s2jh4net/blob/master/src/main/webapp/docs/markdown/ exception handling .md) - describes exception handling framework strategy design
* [Mobile support] (https://github.com/xautlx/s2jh4net/blob/master/src/main/webapp/docs/markdown/ mobile support .md) - Case Web App to Android Native App Integration and Application

## Development Guide
* [Development configuration] (https://github.com/xautlx/s2jh4net/blob/master/src/main/webapp/docs/markdown/ development configuration .md) - based development environment configuration instructions
* [Engineering Structures] (https://github.com/xautlx/s2jh4net/blob/master/src/main/webapp/docs/markdown/ engineering structures .md) - the code for the entire project structure is described schematically
* [Code Generator] (https://github.com/xautlx/s2jh4net/blob/master/src/main/webapp/docs/markdown/ code generation .md) - CURD basic framework for code generation tool
* [Basic Functions] (https://github.com/xautlx/s2jh4net/blob/master/src/main/webapp/docs/markdown/ basic functions .md) - has been achieved on the basis of the framework Features Description
* [UI assembly] (https://github.com/xautlx/s2jh4net/blob/master/src/main/webapp/docs/markdown/UI assembly .md) - Framework UI component design ideas and usage demo
* [Spreadsheet Component] (https://github.com/xautlx/s2jh4net/blob/master/src/main/webapp/docs/markdown/ Spreadsheet Component .md) - Powerful Grid Spreadsheet Component Extended enhanced
* [Forms Control] (https://github.com/xautlx/s2jh4net/blob/master/src/main/webapp/docs/markdown/ form controls .md) - describes the Web development process design the main form processing

> Since the project uses Lombok and other plug-in, if you want to get the git project code into the development environment, be sure to browse documents in advance [development configuration] (https://github.com/xautlx/s2jh4net/blob/master/src/main / webapp / docs / markdown / development configuration .md) for the IDE, Lombok and other plug-in configuration, otherwise it will be out a lot of compilation errors.

## Core modules
* [Data base] (https://github.com/xautlx/s2jh4net/blob/master/src/main/webapp/docs/markdown/ basic data .md) - Introduction and framework for the development of test data data base design ideas
* [Audit data] (https://github.com/xautlx/s2jh4net/blob/master/src/main/webapp/docs/markdown/ data auditing .md) - Change Audit records based on Hibernate Envers components to achieve business data
* [Scheduled Tasks] (https://github.com/xautlx/s2jh4net/blob/master/src/main/webapp/docs/markdown/ scheduled tasks .md) - Quartz component implementation plan based on the configuration monitoring and management tasks
* [Reptile Data Acquisition] (https://github.com/xautlx/s2jh4net/blob/master/src/main/webapp/docs/markdown/ reptile data acquisition .md) - Reference Nutch achieve a lightweight Web management reptile data acquisition analysis module

> In order to facilitate the development process reference project directly related to the development of a reference guide and sample documents to run embedded applications deployment, the specific content can be viewed online, as shown schematically screenshot Snapshot section.

## Online demo

** Http: //101.200.31.248: 8080 / s2jh4net / admin **

Front-end portals and HTML5 mobile site currently less content, the main demonstration focused on the management side. Account: admin, password: admin123, or simply click the "super administrator" login link.

Online demo site is low with a single point of Ali cloud server outage situation may slow access or update. Meanwhile, in order to prevent arbitrary data changes cause a system crash, individual features enabled demo disable control.
Recommendations refer to [ development configuration ] (https://github.com/xautlx/s2jh4net/blob/master/src/main/webapp/docs/markdown/%E5%BC%80%E5%8F%91%E9%85 % 8D% E7% BD% AE.md) locally run the full experience.

> The latest project to build automated continuous integration status Travis-CI Status: (https [[Build Status] (https://travis-ci.org/xautlx/s2jh4net.svg?branch=master)!]: // Travis-ci. org / xautlx / s2jh4net)

## Screenshot Gallery

! [Ui-signin] (http://git.oschina.net/xautlx/s2jh4net/raw/master/src/main/webapp/docs/markdown/images/img-0065.jpg)

! [Ui-example] (http://git.oschina.net/xautlx/s2jh4net/raw/master/src/main/webapp/docs/markdown/images/ui-example.jpg)

## S2JH4Net vs S2JH

** Important: ** Due to limited personal energy , are currently the main focus on S2JH4Net maintenance update version , the original version has been basically suspended S2JH updated !
This project S2JH (https://github.com/xautlx/s2jh or http://git.oschina.net/xautlx/s2jh) project Brothers project , the main difference profile :

* S2jh based Struts2, s2jh4net based on Spring MVC
* S2jh4net s2jh only in the original basis for enterprise application development , the reorganization module and support structures to support the typical Internet sites and HTML5 mobile site development ;
* In order to simplify the development and building of complexity, reducing the use of dynamic Web project model of a single all-in-one no longer complex Maven modular layout ( but Maven dependency management and build or retention ) ;
* Entity object attribute defines the use of Lombok to simplify the cumbersome getter and setter definitions ;
* JPA Hibernate and MyBatis application integration ;
* Permission to use the framework of Apache Shiro;
* Automatic generation based annotation defined menu , permissions and other data base configuration database using Java coding define basic data , discard the previous SQL script way ; while the development of coding , while the implementation of the project ;

### License Description

* Free License

The project code in addition to src / main / webapp / assets directory under the admin / app and w / app directory two small amount of confusion in the relevant Javascript code provided, its Yu Kaiyuan, while retaining the identity of the source of information on the project and to assure this project under the premise of unauthorized sales practices, can in any way free free use: open-source, non-open source, commercial and non-commercial.

If the project you have any technical questions or Issue feedback, discussion groups can be added QQ group: 303 438 676 or submit a question to the project site Issue or Git platform:
http://www.oschina.net/p/s2jh4net, http://git.oschina.net/xautlx/s2jh4net/issues, https://github.com/xautlx/s2jh4net/issues

* Service charges

If you are still interested in a collaboration to further obtain complete source / provide custom extension implementation / technical advisory services / guidance of graduate / secondary development guidance based on existing open source and other aspects, contact E-Mail: s2jh- dev@hotmail.com or QQ: 2414521719 (please specify plus Q: s2jh4net) negotiations. [Personal Information for the above-mentioned charge service channels, without providing free advice]


### References


Welcome to Follow On other projects:

* [Nutch 2.X AJAX Plugins (Active)] (https://github.com/xautlx/nutch-ajax) - Based on Apache Nutch 2.3 and Htmlunit, Selenium WebDriver components such as expansion , to achieve the type of the page AJAX loading Full Page content crawling, and parse and index -specific data items

* [S2JH4Net (Active)] (https://github.com/xautlx/s2jh4net) - Based on Spring MVC + Spring + JPA + Hibernate oriented Internet and enterprise Web application development framework

* [S2JH (Deprecated)] (https://github.com/xautlx/s2jh) - Based on Struts2 + Spring + JPA + Hibernate enterprise -oriented Web Application Development Framework
 
* [Nutch 1.X AJAX Plugins (Deprecated)] (https://github.com/xautlx/nutch-htmlunit) - Based on Apache Nutch 1.X and implement AJAX page Htmlunit extended reptiles crawl resolution plug
 
* [12306 Hunter (Deprecated)] (https://github.com/xautlx/12306-hunter) - ( function has failed unavailable, but you can also develop as a Swing -like column reference only ) Java Swing C / S version 12306 booking Assistant, you know usefulness
