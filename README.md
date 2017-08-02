# Memoria
> Time-window in memory cache API

# Introduction

Develop a restful API that generates statistics data of uploads.

The main use case is to calculate real time statistics of uploads to our stitching service in the last **60 seconds**.

To accomplish this, two API endpoints need to be implemented.

One is responsible for the input of the data, the other to retrieve the relevant statistics.

### POST /upload

Every time a users uploads a batch of panoramas, this endpoint will be called.

Body, e.g.:

```
{
  "count": 3,
  "timestamp": 12890212
}
```
where:

* count - Number of uploaded panoramas
* timestamp - Epoch time of upload in UTC

Always Returns with an empty body, and the status code is with either 201 or 204.

* 201 - in case of success
* 204 - timestamp if older than 60 seconds

### GET /statistics

Since the API has to be scalable, it is important that it execute in constant time and memory O(1).
The endpoint returns the statistics of uploads in the last 60 seconds.

It returns:

```

{
  "sum": 3,
  "avg": 1.0,
  "max": 2,
  "min": 1,
  "count: 2
}
```

where:

* sum - Total amount of uploads
* avg - Average amount of uploads per batch
* max - Maximum of uploaded panoramas per batch
* min - Minimum amount of per batch
* count - Total amount of uniq uploads?

## Requirements

* The API is required to run in constant time and space to be scalable
* The API has to be threadsafe
* The API has to function properly over a longer period of time
* The project should be easily buildable
* The API should be able to deal with time discrepancy, since timing issues can always occur
* Do not use any database, including only-memory databases
* The endpoints have to execute in constant time and memory, O(1)
* API has to be fully tested including unit tests and end to end tests
