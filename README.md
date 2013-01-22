XP-Framework Maven plugin
=======================================================================

xp-maven-plugin is a Maven 2/3 plugin to manage the lifecycle of an XP-Framework project:

* compile sources
* run tests
* package xar/zip artifacts


Build and install xp-maven-plugin
-----------------------------------------------------------------------

```sh
~/xp-maven-plugin $ mvn install
```

XP-framework project directory structure
-----------------------------------------------------------------------

The plugin can handle projects with sources written both in XP (.xp) and PHP (*.class.php) language. Source files written in PHP (src/main/php, src/test/php) are not sent to XCC, but are copied to the "target" directory untouched.

Check the "examples" directory for the "lib-common" and "app-hello" dummy projects.

    [app-hello]
    |- ChangeLog
    |- README.txt
    |- LICENSE.txt
    |- pom.xml                                      # Maven project configuration file
    |
    `- [src]                                        # Sourcecode, by Maven conventions
       |- [main]
       |  |- [etc]                                  # For web application, here are the configuration files
       |  |  |- web.ini
       |  |  `- ...
       |  |
       |  |- [php]                                  # Source files (**/*.class.php)
       |  |  `- ...
       |  |
       |  |- [resources]                            # Various project resources (stuff used via ClassLoader::getResource())
       |  |  |- resource.ini
       |  |  `- ...
       |  |
       |  |- [xp]
       |  |  `- [org/company/app/hello]             # Source files (**/*.xp)
       |  |     |- Hello.xp
       |  |     `- ...
       |  |
       |  |- [webapp]                               # For web application, here is the document root
       |  |  |- [image]
       |  |  |- [css]
       |  |  `- [js]
       |  |
       |  `- [xsl]
       |     |- layout.xsl
       |     `- ...
       |
       |
       `- [test]                                    # Project tests
          |- [config]
          |  `- [unittest]                          # Configuration files for unittesting
          |     |- test1.ini
          |     |- test2.ini
          |     `- ...
          |
          |- [php]                                  # Test source files (**/*Test.class.php)
          |  `- ...
          |
          `- [xp]                                   # Test source files (**/*Test.xp)
             `- [org/company/app/hello/unittest]
                |- HelloTest.xp
                `- ...

Example pom.xml file
-----------------------------------------------------------------------

```xml
<project>
  <modelVersion>4.0.0</modelVersion>

  <groupId>org.company.app</groupId>
  <artifactId>app-hello</artifactId>
  <version>1.0</version>
  <name>Hello world application</name>
  <packaging>xar</packaging>
  <properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
  </properties>

  <build>
    <plugins>
      <plugin>
        <groupId>net.xp-forge.maven.plugins</groupId>
        <artifactId>xp-maven-plugin</artifactId>
        <version>3.1.8</version>
        <extensions>true</extensions>
      </plugin>
    </plugins>
  </build>
</project>
```


Project properties
-----------------------------------------------------------------------

There are project properties that xp-maven-plugin is using to configure itself. These properties can be set:

- Either in `pom.xml` via `/project/properties/property`
- Or via command line via Maven's `-D` argument


### ${maven.test.skip} ###
- boolean, default FALSE
- applies to compile and test phases

xp-maven-plugin will honour the Maven skip test property and will not compile nor run the project tests


### ${xp.runtime.local} ###
- boolean, default FALSE
- applies to all phases

If this option is TRUE, xp-maven-plugin will use the locally installed XP-Framework runners by looking in the the `PATH` environment variable. If it cannot find the XP-runers (xcc, unittest, etc.), an exception will be thrown.

Information on how to install XP-Framework on your machine can be found here:
- https://github.com/xp-framework/xp-framework/wiki/setup.framework

If this option is FALSE, you don't need to have XP-Framework installed on your machine; however, you must have dependency for `net-xp-framework:core` defined in `pom.xml` in order to use this option. If you also have *.xp files that need to be compiled, you must also have the `net-xp-framework:compiler` dependency defined in `pom.xml`

xp-maven-plugin will download the needed XP-artifacts from repository (Maven central or as configured) and will create a local XP runtime environment in the `target/.runtime` directory.


### ${xp.runtime.timezone} ###
- string, default machine local. E.g. Europe/Berlin
- applies to all phases

This option is used only when `${xp.runtime.local}` is set to TRUE


### ${xp.runtime.php} ###
- file, auto-detected via the `PATH` enviroment variable. E.g. /usr/bin/php
- applies to all phases

This option is used only when `${xp.runtime.local}` is set to TRUE. It contains the path to the `php` executable


### ${xp.compile.verbose} ###
- boolean, default FALSE
- applies to the compile phase

This options sets `xcc` runner verbosity on or off (-v flag)


### ${xp.compile.emitter} ###
- string, default NULL
- applies to the compile phase

This options sets `xcc` runner emitter (-e flag)


### ${xp.test.verbose} ###
- boolean, default FALSE
- applies to the test phase

This options sets `unittest` runner verbosity on or off (-v flag)


### ${xp.test.iniDirectory} ###
- file, default /src/test/config/unittest
- applies to the test phase

This options specifies where unittest [*.ini] files are located


### ${xp.package.strategy} ###
- enum { 'lib', 'app' }, default 'lib'
- applies to the package phase

This options specifies how the project artifact is to be created:
- lib: classes are added to archive root and dependencies are merged
- lib: classes are added to the 'classes' directory inside archive and dependencies are added to the 'lib' directory


### ${xp.package.format} ###
- enum { 'xar', 'zip' }, default 'xar'
- applies to the package phase

This options specifies the format of the generated artifact


### ${xp.package.packDependencies} ###
- boolean, default FALSE
- applies to the package phase

This options specifies if project dependencies (excluding XP-artifacts) are to be packed in the generated artifact


### ${xp.package.packRuntime} ###
- boolean, default FALSE
- applies to the package phase

This option is used only when `${xp.runtime.strategy}` is set to 'app' and specifies if XP-artifacts (core) and the XP-runners should also be packed inside the generated artifact. Bootstrap will be packed inside /runtime/bootstrap and XP-artifacts will be packed inside /runtime/lib directory


### ${xp.package.mainClass} ###
- string, default NULL
- applies to the package phase

This option sets the mainClass property in the auto-generated 'META-INF/manifest.ini' file. Usefull used you want to start your application using `xp -xar artifact.xar`


Build a project
-----------------------------------------------------------------------

```sh
~/app-hello $ mvn package
```

This will:

1. Copy resources from "src/main/resources" to "target/classes"
2. Copy test resources from "src/test/resources" to "target/test-classes"
3. Copy PHP source files from "src/main/php" to "target/classes"
4. Compile XP source files from "src/main/xp" to "target/classes"
5. Copy test PHP source files from "src/test/php" to "target/test-classes"
6. Compile test XP source files from "src/test/xp" to "target/test-classes"
7. Run tests (if any)
8. Assemble the XAR package with the compiled sources into "target/my-project-1.0.xar"


Dependencies
-----------------------------------------------------------------------

If your XP-Framework project depends on other XP-Framework project, alter the "pom.xml" file as follow:

```xml
<project>
  ...
  <dependencies>
    <dependency>
      <groupId>org.company.lib</groupId>
      <artifactId>lib-common</artifactId>
      <version>1.0</version>
      <type>xar</type>
      <optional>false</optional>
    </dependency>
  </dependencies>
  ...
</project>
```


Running XP code
---------------

If you have the need to run an XP class (like w/ xp f.q.c.n or xp -e "code"), then you can use the "xp:run-fork" goal):

```xml
<build>
  <plugins>
    <plugin>
      <groupId>net.xp-forge.maven.plugins</groupId>
      <artifactId>xp-maven-plugin</artifactId>
      <version>3.0.0</version>
      <extensions>true</extensions>

      <executions>
        <execution>
          <id>runclass</id>
          <phase>test</phase>
          <configuration>
            <code>Console::writeLine('* Hello World from XP Framework.');</code>
          </configuration>
          <goals>
            <goal>run-fork</goal>
          </goals>
        </execution>
      </executions>
    </plugin>
  </plugin>
</build>
```

As configuration, you can either pass:

* `<code>` with inline source code (limitation: either single or double quotes may be used - mixing not supported)
* `<className>` runs the given class w/ "public static function main($args) {...}"
