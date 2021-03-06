# Grow
Grow is a text-based adventure game which can be modified during play. It was inspired by the article on [Page 90](https://archive.org/stream/creativecomputing-1980-01/Creative_Computing_v06_n01_1980_Jan?ui=embed#page/n93/mode/1up) of the January 1980 edition of the magazine *Creative Computing*.

While this version of the game is backwards compatible with the version described in the original article, it adds several new features to enrich the experience:

1. A GUI which displays the text-based game, as well as an optional image and soundtrack for each scene. The game can be played in a text only mode by running it with the `-t` flag.
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

#### Basic Commands
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

#### Advanced Commands
1. `import image`: imports an image (jpeg or png) from the URI specified on the next line.
2. `import music`: imports a music file (mp3, wav, or aac) from the URI specified on the next line.
3. `import adventure`: imports an adventure (a zip file) from the URI specified on the next line.
4. `clear image`: clears the saved image for the current scene
5. `clear music`: clears the saved music for the current scene
6. `save`: saves the current adventure. The game saves automatically when needed, so there is no need to use this command during normal use.

#### Other Commands
1. `about`: prints information about the program.
2. `license`: prints license information.

### Graphical Commands
1. Drag and drop JPEG image files onto the image to set the image for the current scene.
2. Drag and drop WAV, MP3, or AAC files onto the audio player to set the sound for the current scene.
2. Drag and drop the `.zip` file for the current adventure from the bottom right corner of the screen. This is useful for sharing.
3. Drag and drop `.zip` files into the text input area to import adventures.

## Game Mechanics
A Grow adventure is comprised of scenes and begins at the starting scene. Each scene must have a description, and may have an image and music. Each scene also has a list of rules to process user input. A rule is a list of patterns and a list of actions. When any of the patterns occur in the user's input, the game executes all of the rule's actions.

The patterns are case insensitive, and support regular expressions. A rule matches the user's input if any of the patterns for that rule can be found in the input. Because in most cases, a pattern is intended to match a whole word, before matching, all patterns are substituted into the regular expression `.*\b(%s)\b.*`, where `%s` is the uppercase version of the pattern. The program than searches for that regular expression in the uppercase version of the user's input. While it is not possible not make matching case sensitive, it is still possible to match parts of words by adding `.*` as a suffix or prefix to a pattern. For example, if you want to match any word that has `foo` in it, you could use the pattern `foo.*`, which when inserted into the expression above, becomes `.*\b(foo.*)\b.*`, and will match any word which starts with `foo`.

There are four main types of actions: print, go, change score, and display score. The print action prints text out to the user. It is denoted by the prefix `p`. For example, the action `pRight on!` would print out "Right on!". The go action changes the scene to a different scene. It is indicated by the prefix `g`. Thus, the action `gforest` would cause the game to go to the forest scene. The change score action causes the user's score to change. It has two prefixes: `+` and `-`. The action `+100` will increase the score by 100, and the action `-100` will decrease the score by 100. The display score action causes the program to display the player's current score. It is simply `s`.

## Gameplay
When a player first visits a scene, the program displays the description for that scene, along with the associated media. The player can then type anything, and hit enter to submit their response. Then, the game analyzes the response. First, the game checks to see if the response is a command. If so, it executes that command. If the response is not a command, the game checks to see if any of the scene's rules match the command. The game then executes all the actions of the first rule that matches the user's input.

### Editing
While playing Grow is fun, the real power of Grow is that it allows the player to modify the game during play. The main command for this is `extend`. When the player types the extend command, the program will ask the player for patterns to match. He can enter as many patterns as he wants, one per line, and then type a blank line when he is done. Then, the program will ask the player for actions to take when those patterns are matched in the user's input. Once again, the player can enter the actions, one per line, in the format described above, and then type a blank line when done.

Grow has several more editing commands which allow for editing the description of the current scene, changing the order of rules, and editing rules. The most useful are `edit` and `description`.

#### Edit
The `edit` command allows the player to edit rules from the current scene. It first asks the user to pick a rule to edit. Then it displays three choices: 

1. Edit patterns
2. Edit actions
3. Reorder actions

The pattern and action editing work in the same way. First, the game asks the user to pick a pattern or action to edit. Then, it asks for a replacement for that pattern or action. If the user is using the GUI mode, the game will put the text of the last pattern or action into the input field so the user can edit it. If the user is using the console mode, this will not happen because Java does not support it. Then, once the user hits enter, the program saves the new pattern or action. If the user deletes all the text and hits enter, the game removes that pattern or action. After the editing is complete, the game asks the user to enter any new patterns or actions, in the same way as the `extend` command.

The reordering of actions is simple: the program prints out a numbered list of the actions, and asks the user for a new order as a list of space-separated integers. For example, consider the following actions:

```
1.    pWhat is so hard about this?
2.    pI told you: you have to destroy the death star!!
3.    pIf you do not destroy the death star, the Empire may win
```
If the user typed in `1 2 3`, the order would not change, but if the user typed in `3 2 1` the order would be reversed.

#### Description
The `description` command allows the user to change the description for the scene. If the user is using the GUI, the game will fill the input area with the last description.

#### Other Commands
All the other commands operate in a similar way to one of the commands already described.

#Building
Run `ant` to produce a runnable jar. If you also want the documentation, you can use `ant doc`.

# License
This program is licensed under GPLv3.0. A copy of the license is in `LICENSE.txt`.

## Attribution
1.  Font Awesome by Dave Gandy - [http://fontawesome.io](http://fontawesome.io).
2.  Ubuntu Mono - see the Ubuntu Mono license at `attribution/ubuntu-font-licence-1.0.txt`.
