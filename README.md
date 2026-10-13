# orchestrator-demo

A miniature dependency-injection container built with a custom `@Inject` annotation and reflection.

## Run the tests
    mvn test

## Run the application
    mvn compile
    java -cp target/classes com.demo.Main

Expected output:

    [Orchestrator] TypeA -> ClientA.objectA
    Result      : objA hello
    objectA set : true
    Singleton   : true

## Notes
- Test fixtures live in the `fixtures.*` packages (outside `com.demo`) so they never pollute the scan of the real app.
- Fixtures are top-level public classes: the scanner skips nested classes (`$`).
- Requires Java 17+.
