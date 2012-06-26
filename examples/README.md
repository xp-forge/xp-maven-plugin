Build and install the xp-maven-plugin
----------------------------------------------

    ~/xp-maven-plugin $ mvn install


Build and install the "lib-common" artifact
-------------------------------------------

    ~/xp-maven-plugin $ cd examples/lib-common
    ~/xp-maven-plugin/examples/lib-common $ mvn install


Build the "app-hello" application artifact
------------------------------------------

    ~/xp-maven-plugin/examples/lib-common $ cd ../app-hello
    ~/xp-maven-plugin/examples/app-hello $ mvn package


Build and run the Uber-XAR
--------------------------

    ~/xp-maven-plugin/examples/app-hello $ mvn -Dxpframework.xar.mergeDependencies package
    ~/xp-maven-plugin/examples/app-hello $ xp -xar target/app-hello-1.0-uber.xar
