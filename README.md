Snowizard
=========
[![Build Status](https://travis-ci.org/smoketurner/snowizard.svg?branch=master)](https://travis-ci.org/smoketurner/snowizard)
[![Coverage Status](https://coveralls.io/repos/smoketurner/snowizard/badge.svg)](https://coveralls.io/r/smoketurner/snowizard)
[![Maven Central](https://img.shields.io/maven-central/v/com.smoketurner.snowizard/snowizard-parent.svg?style=flat-square)](https://maven-badges.herokuapp.com/maven-central/com.smoketurner.snowizard/snowizard-parent/)
[![GitHub license](https://img.shields.io/github/license/smoketurner/snowizard.svg?style=flat-square)](https://github.com/smoketurner/snowizard/tree/master)

Snowizard is an HTTP-based service for generating unique ID numbers at high scale with some simple guarantees.

## Motivation

Snowizard is a Java port of Twitter's [Snowflake](https://github.com/twitter/snowflake/tree/snowflake-2010) thrift service presented as an HTTP-based [Dropwizard](http://dropwizard.io/) service. Snowizard supports returning ID numbers as:

* JSON and JSONP
* Google's [Protocol Buffers](https://code.google.com/p/protobuf/)
* Plain text

At GE, we were more interested in the uncoordinated aspects of Snowflake than its throughput requirements, so HTTP was fine for our needs. We also exposed the core of Snowflake as an embeddable module so it can be directly integrated into our applications. We don't have the guarantees that the Snowflake-Zookeeper integration was providing, but that was also acceptable to us. In places where we really needed high throughput, we leveraged the snowizard-core embeddeable module directly.

## Requirements

### Uncoordinated

For high availability within and across data centers, machines generating ids should not have to coordinate with each other.

### (Roughly) Time Ordered

We have a number of API resources that assume an ordering (they let you look things up "since this id").

However, as a result of a large number of asynchronous operations, we already don't guarantee in-order delivery.

We can guarantee, however, that the id numbers will be k-sorted (references: http://portal.acm.org/citation.cfm?id=70413.70419 and http://portal.acm.org/citation.cfm?id=110778.110783) within a reasonable bound (we're promising 1s, but shooting for 10's of ms).

### Directly Sortable

The ids should be sortable without loading the full objects that they represent. This sorting should be the above ordering.

### Compact

There are many otherwise reasonable solutions to this problem that require 128bit numbers. For various reasons, we need to keep our ids under 64bits.

### Highly Available

The id generation scheme should be at least as available as our related services.

## Solution
* [Dropwizard](http://dropwizard.io/) service written in Java
* id is composed of:
  * time - 41 bits (millisecond precision w/ a custom epoch gives us 69 years)
  * configured machine id - 10 bits - gives us up to 1024 machines
  * sequence number - 12 bits - rolls over every 4096 per machine (with protection to avoid rollover in the same ms)

### System Clock Dependency

You should use NTP to keep your system clock accurate. Snowizard protects from non-monotonic clocks, i.e. clocks that run backwards. If your clock is running fast and NTP tells it to repeat a few milliseconds, Snowizard will refuse to generate ids until a time that is after the last time we generated an id. Even better, run in a mode where ntp won't move the clock backwards. See http://wiki.dovecot.org/TimeMovedBackwards#Time_synchronization for tips on how to do this.

# Contributing

To contribute:

1. fork the project
2. make a branch for each thing you want to do (don't put everything in your master branch: we don't want to cherry-pick and we may not want everything)
3. send a pull request to jplock

## Building

To build and test, run `mvn test`.
