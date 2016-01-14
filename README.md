# Grow
Grow is a text-based adventure game which can be modified during play. It was inspired by the article on [Page 90](https://archive.org/stream/creativecomputing-1980-01/Creative_Computing_v06_n01_1980_Jan?ui=embed#page/n93/mode/1up) of the January 1980 edition of the magazine *Creative Computing*.

While this version of the game is backwards compatiable with the version described in the original article, it adds several new features to enrich the experience:

1. A GUI which displays the text-based game, as well as an optional image for each scene. The game can be played in a text only mode by running it with the `-t` flag.
2. A drag and drop interface for importing images to scenes.
3. A drag and drop interface for exporting and importing adventures, which are stored in ZIP files.
2. More commands which allow for easier editing of the adventure.

# Usage
## Basic Usage

1. `java -jar Grow.jar`: launches the GUI version
1. `java -jar Grow.jar -t`: launches the text based version

## Advanced Options

1. `--grow-root <path to root folder>`: sets the grow root to the specified folder for the current session. Can be used with any of the forms above.
2. `--reset-root` deletes the location of the grow root. When you run grow again, it will pick a new root. Must be used alone.
3. `--set-root <path to root folder>`: sets the grow root to the specified folder. The folder will be made if it does not exist. Must be used alone.
4. `--help`: prints helpful messages. Must be used alone.
