# Grow
Grow is a text-based adventure game which can be modified during play. It was inspired by the article on [Page 90](https://archive.org/stream/creativecomputing-1980-01/Creative_Computing_v06_n01_1980_Jan?ui=embed#page/n93/mode/1up) of the January 1980 edition of the magazine *Creative Computing*.

While this version of the game is backwards compatible with the version described in the original article, it adds several new features to enrich the experience:

1. A GUI which displays the text-based game, as well as an optional image for each scene. The game can be played in a text only mode by running it with the `-t` flag.
2. A drag and drop interface for importing images to scenes.
3. A drag and drop interface for exporting and importing adventures, which are stored in ZIP files.
2. More commands which allow for easier editing of the adventure.

# Usage
## Launching
### Basic Usage

1. `java -jar Grow.jar`: launches the GUI version
1. `java -jar Grow.jar -t`: launches the text based version

### Advanced Options

1. `--grow-root <path to root folder>`: sets the grow root to the specified folder for the current session. Can be used with any of the forms above.
2. `--reset-root` deletes the location of the grow root. When you run grow again, it will pick a new root. Must be used alone.
3. `--set-root <path to root folder>`: sets the grow root to the specified folder. The folder will be made if it does not exist. Must be used alone.
4. `--help`: prints helpful messages. Must be used alone.

## Game Control
### Text Commands
To enter a command, prefix it with `:`.

1. `quit`: exits the game
2. `restart`: sets your score to 0 and takes you back to the first scene of the adventure
3. `extend`: adds a rule to the current scene
4. `remove`: removes a rule from the current scene
5. `edit`: edits a rule from the current scene
5. `reorder`: reorders the rules of the current scene.
6. `description`: changes the description of the current scene
7. `cancel`: cancels the current edit
8. `view`: displays all the rules for the current scene
9. `change story`: changes the story
10. `new`: creates a new story

### Graphical Commands
1. Drag and drop JPEG image files onto the image to set the image for the current scene.
2. Drag and drop the `.zip` file for the current adventure from the bottom right corner of the screen. This is useful for sharing.
3. Drag and drop `.zip` files into the text input area to import adventures.
