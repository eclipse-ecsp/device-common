[<img src="./images/logo.png" width="300" height="150"/>](logo.png)

[![Build](../../actions/workflows/maven-build.yml/badge.svg)](../../actions/workflows/maven-build.yml)
[![License Compliance](../../actions/workflows/license-compliance.yml/badge.svg)](../../actions/workflows/license-compliance.yml)
[![Documentation](../../actions/workflows/documentation.yml/badge.svg)](../../actions/workflows/documentation.yml)

# Device Common Library

The **Device Common Library** is a shared component designed to provide reusable utilities, configurations, and functionalities for device-related projects. It simplifies the development of device management systems by offering pre-built modules and integrations for common tasks such as logging, configuration management, and data processing.

# Table of Contents
* [Key Features](#key-features)
* [Use Cases](#use-cases)
* [Getting Started](#getting-started)
* [How to contribute](#how-to-contribute)
* [Code of Conduct](#code-of-conduct)
* [Contributors](#contributors)
* [Security Contact Information](#security-contact-information)
* [Support](#support)
* [Troubleshooting](#troubleshooting)
* [License](#license)
* [Announcements](#announcements)

## Key Features

- **Reusable Components**: Provides common utilities and helper classes for device-related projects.
- **Spring Integration**: Built with Spring Framework for seamless integration with Spring-based applications.
- **Logging Support**: Includes SLF4J and Logback for consistent and configurable logging.
- **JSON Handling**: Uses Jackson for efficient JSON serialization and deserialization.
- **Database Support**: Includes PostgreSQL JDBC driver for database connectivity.
- **Testing Utilities**: Integrated with JUnit and Mockito for unit testing.

## Use Cases

The **Device Common Library** can be used in projects that require:

1. **Device Activation**: Utilities for activating and reactivating devices such as Dongles, TCUs, and Dashcams.
2. **Data Processing**: Common data processing utilities for device-related operations.
3. **Configuration Management**: Centralized configuration for device management systems.
4. **Logging and Monitoring**: Pre-configured logging for consistent application monitoring.

## Getting Started

### Prerequisites

- **Java**: JDK 17 or higher.
- **Maven**: Apache Maven 3.8 or higher.

### Installation

To include the **Device Common Library** in your project, add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>org.eclipse.ecsp</groupId>
    <artifactId>device-common</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

### Coding style check configuration

[checkstyle.xml](./checkstyle.xml) is the coding standard to follow while writing new/updating existing code.

Checkstyle plugin [maven-checkstyle-plugin:3.2.1](https://maven.apache.org/plugins/maven-checkstyle-plugin/) is integrated in [pom.xml](./pom.xml) which runs in the `validate` phase and `check` goal of the maven lifecycle and fails the build if there are any checkstyle errors in the project.

There are a few coding guidelines to be followed manually before requesting for a pull request, the same can be found here:

[Manual coding guidelines](Link to page with coding guidelines)

## How to contribute

Please read [CONTRIBUTING.md](./CONTRIBUTING.md) for details on our contribution guidelines, and the process for submitting pull requests to us.

## Code of Conduct

Please read [CODE_OF_CONDUCT.md](./CODE_OF_CONDUCT.md) for details on our code of conduct, and the process for submitting pull requests to us.


## Contributors

The list of [contributors](../../graphs/contributors) who participated in this project.

## Security Contact Information

Please read [SECURITY.md](./SECURITY.md) to raise any security related issues.

## Support

Please write to us at [csp@harman.com](mailto:csp@harman.com)

## Troubleshooting

Please read [CONTRIBUTING.md](./CONTRIBUTING.md) for details on how to raise an issue and submit a pull request to us.

## License

This project is licensed under the Apache-2.0 License - see the [LICENSE](./LICENSE) file for details.

## Announcements

All updates to this component are present in our [releases page](../../releases).
For the versions available, see the [tags on this repository](../../tags).