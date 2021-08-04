# SchoolDigitalSignage

## Goals

To be a FOSS, easy to use but also secure digital signage platform

## About the server

This server is written in kotlin using ktor.io. There are two authentication methods that you can use:

###- Digest authentication

This is what the signage clients use to get the current configuration. - This is arguably less secure, but it doesn't need to be as secure as the other method.

#### Permissions

- Getting the current configuration

###- OAuth authentication

OAuth is currently handled by Microsoft Azure AAD (this is because my school uses Office 365) - This is very secure and it needs to be since this is what you need to authenticate with to change rules

#### Permissions

- Setting the configuration with the GUI

## Setting Up

Place the files in example_config into the bin directory in a folder called "config"