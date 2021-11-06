# MiniTestFramework

Integration Test Framework for Paper!

## Usage

* Install plugin
* Create Test Structure
  * `/test create <filename.testname> [width]`
  * Build contraption
  * Save schematic
* Create Test Script
  * create <mytest>.js in plugin folder
  * register a new test case into the registry 
    ```js
    registry.register("test", (helper) => {
      helper.pressButton(3, 3, 3);
      helper.succeedWhenEntityPresent(EntityType.MINECART, 1, 2, 3);
    });
    ```
  * use helper to do actions and assertions
  * use `/test pos` ingame to find relation locations
* Reload script changes with `/mtest reload`
* Run test via command block ingame or `/test run*` commands

Example: https://streamable.com/e/k6kngh

## Running in CI

When the plugin detects that the CI env var is set to true, it will automatically run all tests, write a test-results.xml (in junit format) and stop the server.  
If exit code > 0, then X number of required tests failed. If exit code < 0, then X number of optional test failed. Exit code = 0 means happy day :)

## Contribution

Best to hit me up on the paper discord: https://discord.gg/papermc  
`gradlew runServer` to test locally.

## Licence

MIT
