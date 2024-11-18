# Server Testing Plan

## Unit Tests

We will write unit tests for each endpoint using the JUnit test framework. These
unit tests will run the server and send HTTP requests to each endpoint and inspect
the returned data to ensure that it is valid. This will cover most of the backend
code as all of it is a part of a code path called by an endpoint.

We may also implement some unit tests for smaller units of code that support an
endpoint if there are any that are sufficiently complex, such as the payment or
orders backend pieces.

## Integration/System/Acceptance Tests

These tests will be done manually and will be detailed further in our user manual.
Our user manual will outline the functionality that our app should support and we
will then manually be able to verify that by following the outlined steps we can
acheive that functionality. We may add some additional documentation for edge-case
testing and trying to break the system.
