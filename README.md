Enquiry [![Build Status](https://travis-ci.org/InspireNXE/Enquiry.svg?branch=master)](https://travis-ci.org/InspireNXE/Enquiry)
=======
A simple plugin that allows you to search the web within Minecraft. It is licensed under the [MIT License]. 

* [Discussion]
* [Source]
* [Issues]
* [Wiki](wiki)
* [Commands](wiki/Commands)
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

### Configuration
**Note:** For how to get the appropriate values for each search engine, please visit our [wiki](wiki/).

```
# For help getting the app id, please reference the wiki guide for setting up Bing <https://github.com/InspireNXE/Enquiry/wiki/Bing-(Setup)>
bing {
    app-id=""
}
# For help getting the api key and search engine id, please reference the wiki guide for setting up Google <https://github.com/InspireNXE/Enquiry/wiki/Google-(Setup)>
google {
    api-key=""
    search-id=""
}
```

### Building
**Note:** If you do not have [Gradle] installed then use `./gradlew` for Unix systems or Git Bash and `gradlew.bat` for Windows systems in place of any `gradle` command.

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
