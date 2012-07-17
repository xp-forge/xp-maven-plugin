XP-Framework plugin for Maven
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
    |- pom.xml                                      # Maven2 project configuration file
    `- src                                          # Sourcecode, by Maven conventions
       |- main
       |  |- resources                              # Various project resources
       |  |  |- META-INF
       |  |  |  `- manifest.ini                     # XAR Manifest file
       |  |  |- resource.ini
       |  |  `- ...
       |  |- xp                                     # Source files (**/*.xp)
       |  |  `- org
       |  |     `- company
       |  |        `- app
       |  |           `- hello
       |  |              |- Hello.xp
       |  |              `- ...
       |  `- php                                    # Source files (**/*.class.php)
       |     `- ...
       `- test                                      # Project tests
          |- resources                              # Various project test resources
          |  `- etc
          |     `- unittest                         # Configuration files for unittesting
          |        |- test1.ini
          |        |- test2.ini
          |        `- ...
          |- xp                                     # Test source files (**/*Test.xp)
          |  `- org
          |     `- company
          |        `- app
          |           `- hello
          |             `- unittest
          |                |- HelloTest.xp
          |                `- ...
          `- php                                    # Test source files (**/*Test.class.php)
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
        <version>3.1.0</version>
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


### ${xp.runtime.local} ###
- boolean, default FALSE
- applies to all phases

If this option is TRUE, xp-maven-plugin will use the locally installed XP-Framework runners by looking in the the `PATH` environment variable. If it cannot find the XP-runers (xcc, unittest, etc.), an exception will be thrown.

If this option is FALSE, xp-maven-plugin will create a local XP-runtime environment in the `target/.runtime` directory. You must have dependencies for `net-xp-framework:core` and `net-xp-framework:tools` defined in `pom.xml` in order to use this option. If you also have *.xp files that need to be compiled, you must also have the `net-xp-framework:language` dependency defined in `pom.xml`


### ${xp.runtime.timezone} ###
- string, default machine local
- applies to all phases

This option is used only when `${xp.runtime.local}` is set to TRUE


### ${xp.runtime.php} ###
- file, auto-detected via the `PATH` enviroment variable
- applies to all phases

This option is used only when `${xp.runtime.local}` is set to TRUE. It contains the path to the `php` executable. E.g.: /usr/bin/php


### ${xp.compile.verbose} ###
- boolean, default FALSE
- applies to the compile phase

This options sets `xcc` verbosity on or off (-v flag)


### ${xp.compile.classpaths} ###
- string[], default NULL
- applies to the compile phase

This options adds more chasspaths to `xcc` (-cp flag)


### ${xp.compile.sourcepaths} ###
- string[], default NULL
- applies to the compile phase

This options adds more sourcepaths to `xcc` (-sp flag)


### ${xp.compile.emitter} ###
- string, default NULL
- applies to the compile phase

This options sets `xcc` emitter (-e flag)


### ${xp.compile.profiles} ###
- string[], default ['default']
- applies to the compile phase

This options sets `xcc` profiles (-p flag)


### ${xp.test.verbose} ###
- boolean, default FALSE
- applies to the test phase

This options sets `unittest` verbosity on or off (-v flag)


### ${xp.test.classpaths} ###
- string[], default NULL
- applies to the compile phase

This options adds more chasspaths to `unittest` (-cp flag)


### ${xp.test.iniDirectory} ###
- file, default /target/test-classes/etc/unittest
- applies to the test phase

This options specifies where unittest [*.ini] files are located


### ${xp.test.iniDirectories} ###
- file, default NULL
- applies to the test phase

This options specifies the location for additional directories to be scanned for [*.ini] files


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

This option is used only when `${xp.runtime.strategy}` is set to 'app' and specifies if XP-artifacts (core & tools) and the XP-runners should also be packed inside the generated artifact. Bootstrap will be packed inside /runtime/bootstrap and XP-artifacts will be packed inside /runtime/lib directory


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
9. Assemble the uber-XAR package with the compiled sources and all dependencies into
   "target/my-project-1.0-uber.xar" (only if run with -Dxp.xar.merge)


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

If you have the need to run an XP class (like w/ xp f.q.c.n or xp -e "code"), then you can use the "xp" goal):

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
            <goal>xp</goal>
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
