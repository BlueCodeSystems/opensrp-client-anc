<!-- JITPACK BADGES:START -->
[![JitPack Latest](https://jitpack.io/v/BlueCodeSystems/opensrp-client-anc.svg)](https://jitpack.io/#BlueCodeSystems/opensrp-client-anc)
[![master-SNAPSHOT](https://jitpack.io/v/BlueCodeSystems/opensrp-client-anc/master-SNAPSHOT.svg)](https://jitpack.io/#BlueCodeSystems/opensrp-client-anc/master-SNAPSHOT)
<!-- JITPACK BADGES:END -->

# opensrp-client-anc
The OpenSRP Antenatal Care (ANC) library packages WHO-aligned ANC contact workflows, configurable forms, and supporting data stores for OpenSRP-based Android clients.

## Project Status
- Tooling: Gradle 8.7 · Android Gradle Plugin 8.6.0 · Java 17 (no Kotlin plugin applied)
- CI: no hosted pipeline; run Gradle builds locally before raising a PR
- Default branch: `master`
- Latest tag: not yet published (first release pending)

## Features
- Digitised ANC register, contact, and counseling flows aligned with WHO guidelines
- Configurable site/population characteristics with device-to-server sync helpers
- JSON form rendering + rule-engine utilities for dynamic contact tasks and alerts
- Repositories for contact payloads, partial forms, and scheduled task management

### Detailed feature matrix
The table below retains the historical breakdown of library capabilities and upstream dependencies.

| Function                            | Feature                                                                                                                                                                                        | Dependencies                                                       |
|------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------|
| Server Settings                     | Set the site characteristics for your facility on first login                                                                                                                                  |                                                                   |
| Server Settings                     | View and edit the site characteristics for your facility                                                                                                                                       |                                                                   |
| Server Settings                     | Site characteristics sync between all devices of team members                                                                                                                                  | opensrp-client-core                                                |
| Server Settings                     | Set and update the population characteristics                                                                                                                                                  | opensrp-web, opensrp-server-web, opensrp-client-core               |
| Server Settings                     | View the population characteristics on the device                                                                                                                                              | opensrp-client-core                                                |
| Server Settings                     | Sync configured forms for the ANC contact visits from OpenSRP server (in progress)                                                                                                             | opensrp-server-web, opensrp-client-core, opensrp-client-native-form |
| Client Identification & Registration| Register a pregnant woman                                                                                                                                                                      | opensrp-client-core                                                |
| Client Identification & Registration| Search/View/Filter each client/Household                                                                                                                                                        | opensrp-client-core                                                |
| Client Identification & Registration| Multi-lingual support to register a client to the current local language                                                                                                                       | opensrp-client-core                                                |
| Client Identification & Registration| Download and sync unique IDs from the OpenSRP server                                                                                                                                           | opensrp-client-core                                                |
| Client Identification & Registration| Auto generate unique IDs                                                                                                                                                                       | opensrp-client-core                                                |
| Client Identification & Registration| Pull locations and assign providers to locations                                                                                                                                                | opensrp-client-core                                                |
| Client Identification & Registration| Add/Edit client location info                                                                                                                                                                  | opensrp-client-core                                                |
| Client Identification & Registration| Enrol client in ANC program                                                                                                                                                                    | opensrp-client-core                                                |
| Client Identification & Registration| Capture profile photo                                                                                                                                                                          | opensrp-client-core                                                |
| Client Identification & Registration| Attach QR code for each client                                                                                                                                                                 | opensrp-client-core                                                |
| Client Identification & Registration| Identify high-risk pregnancies at registration                                                                                                                                                 | opensrp-client-core                                                |
| Client Identification & Registration| Validate ANC contact scheduling rules                                                                                                                                                           | opensrp-client-core                                                |
| Client Identification & Registration| Start ANC contact for each visit                                                                                                                                                               | opensrp-client-native-form, opensrp-client-configurable-views      |
| Client Identification & Registration| Manage family members                                                                                                                                                                          | opensrp-client-core                                                |
| Contact Visits                      | Quick check for danger signs                                                                                                                                                                   | opensrp-client-native-form, opensrp-client-configurable-views      |
| Contact Visits                      | Profile contact                                                                                                                                                                                | opensrp-client-native-form, opensrp-client-configurable-views      |
| Contact Visits                      | Symptoms follow-up                                                                                                                                                                             | opensrp-client-native-form, opensrp-client-configurable-views      |
| Contact Visits                      | Physical exam                                                                                                                                                                                 | opensrp-client-native-form, opensrp-client-configurable-views      |
| Contact Visits                      | Tests                                                                                                                                                                                          | opensrp-client-native-form, opensrp-client-configurable-views      |
| Contact Visits                      | Counselling and treatment                                                                                                                                                                      | opensrp-client-native-form, opensrp-client-configurable-views      |
| Contact Visits                      | Tasks                                                                                                                                                                                          | opensrp-client-native-form, opensrp-client-configurable-views      |
| Contact Visits                      | Auto-populated quick check values                                                                                                                                                              | opensrp-client-native-form, opensrp-client-configurable-views      |
| Contact Visits                      | Auto-populated profile values                                                                                                                                                                  | opensrp-client-native-form, opensrp-client-configurable-views      |
| Contact Visits                      | Auto-populated tests values                                                                                                                                                                    | opensrp-client-native-form, opensrp-client-configurable-views      |
| Contact Visits                      | Contact schedule tracking                                                                                                                                                                      | opensrp-client-native-form, opensrp-client-configurable-views      |
| Visit Task Management               | Create, update, and count ANC visit tasks                                                                                                                                                      | opensrp-client-core                                                |
| Visit Task Management               | Auto-generate ANC visit tasks                                                                                                                                                                   | opensrp-client-core                                                |
| Visit Task Management               | Support for ANC referral tasks                                                                                                                                                                 | opensrp-client-core                                                |
| Population Characteristics          | Configure population characteristics on the device                                                                                                                                             | opensrp-client-core                                                |
| Population Characteristics          | Sync population characteristics between devices                                                                                                                                                | opensrp-client-core                                                |
| Data Management                     | Sync client profile updates                                                                                                                                                                    | opensrp-client-core                                                |
| Data Management                     | Sync contact visit updates                                                                                                                                                                     | opensrp-client-core                                                |
| Data Management                     | Sync partial contact data                                                                                                                                                                      | opensrp-client-core                                                |
| Data Management                     | Sync ANC register locally                                                                                                                                                                     | opensrp-client-core                                                |
| Analytics & Reporting               | Export aggregated monthly reports                                                                                                                                                              | opensrp-client-reporting                                           |
| Analytics & Reporting               | Export client-level data                                                                                                                                                                      | opensrp-client-reporting                                           |
| Analytics & Reporting               | Generate standard WHO ANC indicators                                                                                                                                                           | opensrp-client-reporting                                           |

## Requirements
- JDK 17 (Temurin or OpenJDK recommended)
- Android Gradle Plugin 8.6.0 with Gradle 8.7
- Kotlin: not required (Java codebase)
- Android Studio Koala / Flamingo or newer with SDK Manager access
- Android SDK: `compileSdk 35`, `targetSdk 35`, `minSdk 28`, Build Tools `35.0.0`

## Install
Add the library from Maven Central:

```groovy
repositories { mavenCentral() }
dependencies {
  implementation 'org.smartregister:opensrp-client-anc:<version>' // see Releases for the latest version
}
```

```kotlin
repositories { mavenCentral() }
dependencies {
  implementation("org.smartregister:opensrp-client-anc:<version>") // see Releases for the latest version
}
```

## Initialize
Initialise the library inside your OpenSRP application class after `CoreLibrary.init` and repository setup:

```java
public class AncApplication extends DrishtiApplication {
  @Override
  public void onCreate() {
    super.onCreate();

    Context opensrpContext = Context.getInstance();
    opensrpContext.updateApplicationContext(getApplicationContext());

    ActivityConfiguration configuration = new ActivityConfiguration();
    configuration.setHomeRegisterActivityClass(CustomHomeRegisterActivity.class);

    AncLibrary.init(opensrpContext, BuildConfig.DATABASE_VERSION, configuration, new ANCEventBusIndex());
  }
}
```

Replace `CustomHomeRegisterActivity` and `ANCEventBusIndex` with your concrete implementations.

## Usage examples
```java
// Retrieve and persist ANC contact tasks
ContactTasksRepository tasksRepository = AncLibrary.getInstance().getContactTasksRepository();
Task nutritionTask = new Task(baseEntityId, "anc_contact_nutrition", "pending", false, false);
tasksRepository.saveOrUpdateTasks(nutritionTask);
```

```java
// Load the ANC register form definition for custom preprocessing
JSONObject registerForm = AncLibrary.getJsonSpecHelper()
    .getFormJson(getApplicationContext(), ConstantsUtils.JsonFormUtils.ANC_REGISTER);
```

```java
// Launch the contact workflow for a client
Intent contactIntent = new Intent(context, MainContactActivity.class);
contactIntent.putExtra(ConstantsUtils.IntentKeyUtils.BASE_ENTITY_ID, baseEntityId);
contactIntent.putExtra(ConstantsUtils.IntentKeyUtils.CONTACT_NO, contactNumber);
contactIntent.putExtra(ConstantsUtils.IntentKeyUtils.CLIENT_MAP, clientDetailsMap);
context.startActivity(contactIntent);
```

## Sample app
A reference implementation lives in `sample/`.
- Install on a connected device or emulator: `./gradlew :sample:installDebug`
- Open the project in Android Studio to explore run configurations and tweak local properties.

## Build & test
- Full build (skipping the legacy reference app module): `./gradlew clean assemble -PskipReferenceApp=true`
- Unit tests: `./gradlew :opensrp-anc:testDebugUnitTest -PskipReferenceApp=true`

## Releases
Published versions and changelogs are listed at [GitHub Releases](https://github.com/BlueCodeSystems/opensrp-client-anc/releases). Use the latest stable tag when updating `<version>` in your build scripts.

## Contributing
Contributions and issue reports are welcome. Before opening a pull request:
- Ensure builds and tests pass with the toolchain noted above.
- Review the [OpenSRP developer guide](https://smartregister.atlassian.net/wiki/spaces/Documentation/pages/6619193/OpenSRP+Developer+s+Guide) and related setup notes.
- Discuss larger enhancements via issues for early feedback.

## License
Distributed under the Apache License 2.0. See [LICENSE](LICENSE) for details.
