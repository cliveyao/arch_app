## Outline

From the target framework for the development will be possible to extract the basic package of CURD base class implementation, but for all business development functions still need some basic framework code as a basis for adding business logic implementation,
There is a basic framework for code generation tool, not only can effectively enhance the development efficiency, but also can improve the consistency of coding style and potential post-reconstruction needs.

Framework to promote a Code First development model, according to JPA Entity Annotation specification defines basic object class implements a framework based code generation tool based Freemarker defined templates,
Search all the entity classes Entity annotation, generate basic DAO, Service, Test, Contoller, JSP page code file generation and other frameworks. Generally described as follows:

* Template file location: lab.s2jh.tool.builder.freemarker, self-adjust based on the project template definition format
* The generated code in: The next target / generated-codes directory, which is a standalone Entity a directory for generating a replica of the occasional repeat one Entity relevant code purposes, integrate the directory is integrated into the structure together. Note: The generated code is in the current engineering tools, the need to own the required relevant code needed to copy the project directory under the corresponding service or Package, and then add the relevant business logic code based on the framework of the code of business projects

## Basic Usage

The entire code means all the source code files and templates in lab.s2jh.tool.builder package , if there is a problem or are interested in running improved to optimize their implementation, can modify the code logic or template definition to suit your project needs ,
The following briefly explain the procedures for using the specific principle can be combined with relevant configuration files and source code to understand.

* According to JPA annotations specification defines basic Entity Object Class
* Direct operates in main ways and means lab.s2jh.tool.builder.SourceCodeBuilder
* After the execution is completed to refresh the commencement of works under the project target / generated-codes directory , copy the code to find the desired frame corresponding service project code directory
* Refresh engineering business if the new code does not compile errors , then generate code basically available, you can try to verify the basic CRUD functions through UI interface
* Then append code to a specific business on the basis of framework code