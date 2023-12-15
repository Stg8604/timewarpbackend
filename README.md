# TimeWarp Server 2023

### Prerequisites

1. JDK 17
2. MongoDB
3. Intellij IDEA Ultimate/Community

### Project Installation

1. Clone the repository and open it in Intellij IDEA
2. Copy contents of `./src/main/resources/application.example.yml` to a new
   file `./src/main/resources/application.yml` and fill in your db details
3. Build the project and
   run [TimeWarpServerApplication](./src/main/kotlin/delta/timewarp/server/TimewarpServerApplication.kt)

### Running

1. Run `./gradlew bootRun` in the project root to start the server
2. Run `./gradlew spotlessApply` to format the code

### Env Settings

1. `production: false` disables sendgrid and email verification. Set to true
   when deploying to production
2. `recaptcha.keys.toggle: false` disables recaptcha. Set to true when deploying
   to production or testing auth routes
