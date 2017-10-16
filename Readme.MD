Welcome to MathSpring.

This repo can be cloned or forked but you will still need to do some setup.

This repo contains the Java source files that make up the server side of mathspring and the Javascript/HTML/CSS
that makes up the client side.

The project is set up to work with Intellij IDEA 2016 Ultimate edition.

The file woServer/META-INF/context.xml is included but contains incorrect user/password to our public database.
If you are using your own database, this file should be modified to contain the correct info for the localhost version
of mysql.

There is a large amount of math content that is not part of this repo.  You can still develop and run the tutor
without it but if you need to see math problems as part of your testing, then you may need this.
It can be downloaded from a shared
google drive and unzipped in your woServer/webResources folder so that you get a result like:
 /woServer/webResources/mscontent/...
 To get access to this content you need to email marshall@cs.umass.edu so that I can share the drive with you.

Many IntelliJ files have been submitted, and this code is easiest to work with in IntelliJ.

---INSTALLATION INSTRUCTIONS---

1. Fork the repository on Github, and clone the forked repository under your user.
    ADMINS: Clone the repository on your local machine.
   - Git Bash
   - cd to dir where you want it
   - git clone git@github.com:marshall62/mathspring.git


4. You will need to install Tomcat 8 and configure IntelliJ to find its installation directory.

Getting MathSpring to Local

(For Mac OS)

1. Open a GIT hub account at https://github.com

2. Fork the repository on Github, and clone the forked repository under your user.

4. Download and install IntelliJ IDEA. You can start with a 30 day free trial - https://

www.jetbrains.com/idea/download/

5. Get JDK if you don't have one already https://www.java.com/en/download/

> java -version

should display your java version

A. Get tar.gz from here - http://tomcat.apache.org/download-70.cgi (Binary Distributions /

B. Unarchive the download

C. Move to /usr/local

D. Create a symbolic link to refer to this tomcat

E. Change ownership of this folder hierarchy:

F. Make all scripts executable:

7. Install MySQL driver for Mac OS

> sudo mv ~/Downloads/apache-tomcat-7.0.64 /usr/local

> sudo ln -s /usr/local/apache-tomcat-7.0.47 /Library/Tomcat

> sudo chown -R <username> /Library/Tomcat

> sudo chmod +x /Library/Tomcat/bin/*.sh

A. Download DMG from https://dev.mysql.com/downloads/mysql/ and follow through the

instructions to install.

B. Set up your user and password-

> cd /usr/local/mysql/bin/

> ./mysql -u root -h localhost -p

mysql> update mysql.user SET Password = PASSWORD(‘< >’) WHERE User = 'root’;

mysql> FLUSH PRIVILEGES;

Mathspring Set up

1. Open IntelliJ IDEA Environment

2. In the welcome screen, select “Checkout from Version Control” > GitHub

3. Select the mathspring repository

4. The project should be ready to use. But if you see an error in the BaseServlet.java, you

need to fix a dependency -

Right click on project > Open Module Settings > Modules > + > Module dependency >

select emailer module > OK

5. Create wayangoutpost DB in local

> ./mysqldump --single-transaction -uWayangServer -p -h rose.cs.umass.edu

wayangoutpostdb > "/Users/S****/Doc***/local.sql”

> ./mysql -u root -h localhost -p

mysql> create database wayangoutpostdb;

> ./mysql -u root -p wayangoutpostdb < "/Users/S****/Doc***/local.sql”


Note: Some versions of MySQL are not happy with the import.  You may 
have to search from strings like ROW_FORMAT=FIXED in the .sql file and replace
with empty string if MySQL complains about these options not being available

A. View > Tools Window > Databases > + > datasource > MySQL

B. URL: jdbc:mysql://localhost:3306/wayangoutpostdb

C. Enter your user and password

D. Run basic SQL queries to validate

7. Update context.xml with the values of your local datasource
