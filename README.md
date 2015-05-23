Enquiry [![Build Status](https://travis-ci.org/InspireNXE/Enquiry.svg?branch=master)](https://travis-ci.org/InspireNXE/Enquiry)
=======
A simple plugin that allows you to search the web within Minecraft. It is licensed under the [MIT License]. 

* [Discussion]
* [Source]
* [Issues]
* [Download]

### Prerequisites
* [Java] 6 (Running)
* [Gradle] 2.4+ (Building)

### Features
* Search [Bing]
* Search [Google]
* Click results and open them in the default web browser
* Add search engines through an API
* Specify a player name to send the results to that player

### Commands
**Note 1:** Values with `|` simply indicates another option following it. Arguments with `[]` are optional while arguments with `<>` are required. 
**Note 2:** Any engines registered through Enquiry's API will follow the same styling for permissions and commands. Simply replace one of the default engine names with one that a registered engine is using in the commands and permissions below.

|                        Command                       | Description                                 | Permission                             | Aliases                          |
|:----------------------------------------------------:|:--------------------------------------------|:---------------------------------------|:---------------------------------|
| `/enquiry <bing|google|b|g> [player] <search terms>` | Searches the engine for the query provided. | `enquiry.command.search.<bing|google>` | `enquiry|eq <bing|google|b|g>`   |
| `/<bing|google|b|g> [player] <search terms>`         | Searches the engine for the query provided. | `enquiry.command.search.<bing|google>` | `bing, google, g, b`             |

### Configuration
**Note:** In order to use each search engine, these values must have a valid value added to the in `./config/enquiry.conf`

```
bing {
    # The app ID from your Microsoft account <https://msdn.microsoft.com/en-us/library/dd251020.aspx>
    app-id=""
}
google {
    # The API key from your Google account <https://developers.google.com/console/help/#generatingdevkeys>
    api-key=""
    # The custom search engine ID from your Google account <https://support.google.com/customsearch/answer/2649143?hl=en>
    search-id=""
}
```

### Building
**Note:** If you do not have [Gradle] installed then use `./gradlew` for Unix systems or Git Bash and `gradlew.bat` for Windows systems in place of 
any `gradle` command.

To build Enquiry, simply run `gradle`. The compiled jar is located in `./libs/`.

[Bing]: https://www.bing.com
[Google]: https://www.google.com
[Gradle]: http://www.gradle.org
[Java]: http://www.java.com
[MIT License]: http://www.tldrlegal.com/license/mit-license
[Discussion]: https://forums.spongepowered.org/t/enquiry-search-to-your-hearts-content-v1-0/7332
[Source]: https://github.com/InspireNXE/Enquiry/
[Issues]: https://github.com/InspireNXE/Enquiry/issues
[Download]: http://assets.inspirenxe.org/files/enquiry/enquiry-latest.jar
