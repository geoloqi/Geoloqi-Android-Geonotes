This is the official Android client application for Geoloqi. It consumes
the Geoloqi Android SDK and is a good example of what can be accomplished
using the library.

## Getting Started

The ActionBarSherlock library is included as a Git submodule. You'll need to
import this project into Eclipse and make sure the Geoloqi-Android-Official
project references it as a library.

First make sure you've updated the submodule repositories:

```
    $ git submodule init
    $ git submodule update
```

Then, using Eclipse, select `File -> New -> Android Project` and choose
"Create project from existing source." Name the project "ActionBarSherlock"
and click finish.

Next, right-click on the parent project Geoloqi-Android-Official and select
"Preferences." Navigate to the "Android" section in the sidebar. If necessary,
add the new "ActionBarSherlock" project as a library to the "Library" section
near the bottom.

> Note: The previous step may have already been completed for you.

## License

Copyright 2011 by [Geoloqi.com][geoloqi-site] and contributors.

See LICENSE.

[geoloqi-site]: https://geoloqi.com/
[geoloqi-dev-site]: https://developers.geoloqi.com/
[android-managing-projects]: http://developer.android.com/guide/developing/projects/projects-cmdline.html
