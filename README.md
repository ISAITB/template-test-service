[![Maven Central](https://img.shields.io/maven-central/v/eu.europa.ec.itb/gitb-types.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22eu.europa.ec.itb%22%20AND%20a:%22template-test-service%22)

# Introduction

The **template-test-service** is a [Maven Archetype](https://maven.apache.org/guides/introduction/introduction-to-archetypes.html) 
acting as a supporting resource for the GITB test bed software and specifications. It can be used as template from which
to build a fully working GITB-compliant test service application that implements the GITB 
[messaging](https://www.itb.ec.europa.eu/docs/services/latest/messaging/), 
[validation](https://www.itb.ec.europa.eu/docs/services/latest/validation/) and 
[processing](https://www.itb.ec.europa.eu/docs/services/latest/processing/) APIs.
Such a service can then be used from [GITB TDL test cases](https://www.itb.ec.europa.eu/docs/tdl/latest/) running in the
GITB test bed. 

The GITB specifications are the result of the
[CEN Global eBusiness Interoperability Test bed (GITB) Workshop Agreement](http://www.cen.eu/work/areas/ict/ebusiness/pages/ws-gitb.aspx).
Evolutive maintenance of the GITB specifications and software is now performed by the European Commission's DIGIT under
the [Interoperable Europe](https://joinup.ec.europa.eu/interoperable-europe) initiative. For more information please
check the [Interoperability Test Bed's site](https://joinup.ec.europa.eu/solution/interoperability-test-bed/about) on Joinup.

# Build instructions

This Archetype is developed in Java and is built using Maven 3+, To build issue the following:

```
mvn clean install
```  

To perform a release and deploy to the Central Repository the profile `release` needs to be specified:

```
mvn clean deploy -P release
``` 

This profile triggers in addition the following:
* Generation of the source JAR.
* Generation of the Javadocs.
* PGP signature of all artefacts. To do this you need a GPG local installation. In addition if multiple keys are
  defined you can specify the appropriate one using the `gpg.keyname` system property. Furthermore, the passphrase 
  entry mode is set to use system property `gpg.passphrase`. These properties can be provided either on the command 
  line or in Maven's `settings.xml` by means of a profile.
* Deploy to the Central repository's staging environment and automatically promote the release.

Using the Archetype to generate a new project is through [Maven's Archetype Plugin](https://maven.apache.org/archetype/maven-archetype-plugin/index.html).
To use it issue (replacing `VERSION` with the version you want to use):

```
mvn archetype:generate -DarchetypeGroupId=eu.europa.ec.itb -DarchetypeArtifactId=template-test-service -DarchetypeVersion=VERSION
```

# Licence

This tool is shared using the [European Union Public Licence (EUPL) version 1.2](https://interoperable-europe.ec.europa.eu/collection/eupl/eupl-text-eupl-12).
Please refer to [NOTICE.md](NOTICE.md) for more information.

# Legal notice

The authors of this tool waive any and all liability linked to its usage, or the interpretation of results from the 
downstream software it may be used to produce.

# Contact

For feedback or questions you are invited to post issues in the current repository. In addition,
feel free to contact the Test Bed team via email at [DIGIT-ITB@ec.europa.eu](mailto:DIGIT-ITB@ec.europa.eu).
