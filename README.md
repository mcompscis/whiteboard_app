# Real-Time Collaborative Whiteboard App (CS346 Project)

## Goal

The goal of this project is to build a real-time, collaborative whiteboard desktop application using Kotlin.
Features include: real-time collaboration, drawing using various pen styles, ability to employ drawing tools etc.

## Team members

Team 210:

- Chirag Jindal (cjindal@uwaterloo.ca)
- Mayank Shrivastava (mdshrivastava@uwaterloo.ca))
- Anh Huy Nguyen Do (hdanguyen@uwaterloo.ca)
- Pranav Sai Vadrevu (psvadrev@uwaterloo.ca)

## Quick-start

Clone this project and build it with `./gradlew build`.

> Note: The server is running on an EC2 instance but if you want to spin up the server locally,
> use `./gradlew server:run`

The app project can be run in two ways:

- directly by calling `./gradlew app:run`
- using the dist file provided in Release Notes 4

> Note: After the zip file is downloaded, extract it and go into app/bin. Right click on the executable `app` and click
> open. Click open again on the prompt.

The tests can be run using `./gradew test`.

## Screenshots/videos
[Demo video of the app in action](https://drive.google.com/file/d/1RILolBfUtWo4CeV_DDTRDUwpJEH87-Sa/view?usp=share_link)

### Explanation of Select Functionality
Users should select the select tool and then they can click on a shape to select/highlight it. Selected shapes will be highlighted and users can change the shapes' attributes such as size (resizability), position (draggability), line width, color (for text boxes, users can bold, italicize, change fonts and change font size too). 

Once the users are on the select tool, they need to double click on the textbox to edit the text.

### Keyboard shortcuts

- ctrl + q = quit app
- ctrl + l = log out


- ctrl + n = new whiteboard
- ctrl + w = remove whiteboard
- ctrl + f = maximize whiteboard
- ctrl + m = minimize whiteboard
- ctrl + r = rename whiteboard


- ctrl + c = copy shape
- ctrl + v = paste shape


- backspace = delete selected shape


- ctrl + 1 = select pen tool
- ctrl + 2 = select rectangle tool
- ctrl + 3 = select circle tool
- ctrl + 4 = select line tool
- ctrl + 5 = select text tool
- ctrl + esc = select select tool
- ctrl + backspace = select eraser tool

## Releases

Whiteboard App Release v0.4.0 (Final Release)

- Setup an AWS EC2 to host our web service with synchronized decorators with locking.
- Moved persistence of whiteboard data and login/signup data to the server which was earlier on the client data as a
  Proof of Concept.
- Added functionality to allow copy & pasting of currently selected shape along with supporting deleting using keyboard
  shortcuts to provide a more complete experience.
- Added real-time cursor display to our app so that all users can see where other users are on a given whiteboard to
  give a more immersive collaborative experience.
- Added real-time broadcasting for editing of shapes so that any changes being made to the whiteboard are visible to all
  people using sockets (previously we just had this for new shapes being drawn but now it’s for editing as well)
- Added real-time broadcasting for handling renaming of whiteboards events to keep the data synchronized across all
  users and the server
- We added styles and colors to textboxes -> now you have multiple fonts, colors, sizes and styles like bold and italics

- Accessibility: Added shortcuts for all tools and added hotkeys for other operations to our menus
- We persist each user’s tab configuration when they logout or close the app, so whenever the user logs back on, their
  list of previously open tabs load up by default

- We persist each user’s last used window size and position and use that when the user opens the app again (along with
  their tool configurations like their last selected tool and line width and color)
- Made UX/UI improvements using CSS and improving our components to be more accessible by expanding the styles section
  on the right by default vs having to expand them manually
- Other quality of life improvements - opening a whiteboard that is already open puts that whiteboard on focus vs.
  opening another instance that can potentially confuse the user.
- Added server side and persistence testing to make our app more robust and bug-free

[Installer](https://drive.google.com/file/d/1073pNphIIBkXgHEg_mMKMicNH2WYNF2e/view?usp=sharing)

Whiteboard App Release v0.3.0

- Colors and stroke selector: You can choose different colors and stroke widths for the shapes that you are drawing on
  the whiteboard
- Resizable shapes: You can now resize any existing shapes on the whiteboard and also modify the color/stroke for the
  selected existing shapes
- Fully functional text tool: You can now draw custom sized text boxes with overflow protection and scrolling and also
  edit text on the whiteboard.
- Login and Signup screen: The app now has a login/signup screen for added security and authenticated access to
  sensitive business data
- Live Collaboration: we provide live updates for new shapes being drawn and erased/deleted : The app will broadcast the
  new updates to all users part of the whiteboard and these changes are reflected in real time using a web-service and
  socket layer
- Extra - some extension features -> backspace to delete current selected shape, rendering functionality for restored
  whiteboards to provide a complete user experience.

[Installer](https://drive.google.com/file/d/1073pNphIIBkXgHEg_mMKMicNH2WYNF2e/view?usp=sharing)

Whiteboard App Release v0.2.0

- Added serializable data models and view models to allow for persistent storage using the newly created persisting
  layer.
- Implemented in-memory storage for all shape data, which can be serialized for persistence.
- Added persistence for the whiteboard state (including user preferences like last used tool) allowing users to resume
  work from where they left off.
- Created an Infinite Pane that resizes with the window, giving users an immersive experience
- Implemented dynamic Cursor changing logic with an extended observer pattern, resulting in a more efficient and
  reliable user experience.
- Implemented the Eraser tool for easy deletion of shapes.
- Added Basic Text Tool to allow users to add placeholder text to their drawings.
- Improved testing framework for black box testing using robotfx
- Implemented Move Tool and Circle Tool testing using Robot framework

[Installer](https://drive.google.com/file/d/1073pNphIIBkXgHEg_mMKMicNH2WYNF2e/view?usp=sharing)

Whiteboard App Release v0.1.0

- Four shapes are available: circle, line, pen, rectangle
- Shapes are now draggable
- Keyboard shortcuts are implemented to quickly create and manage whiteboards

    - Ctrl-N: Create a new whiteboard
    - Ctrl-W: Remove current whiteboard
    - Ctrl-R: Rename current whiteboard

- A new menu bar and toolbar have been added to provide quick access to all possible features

[Installer](https://drive.google.com/file/d/1073pNphIIBkXgHEg_mMKMicNH2WYNF2e/view?usp=sharing)

## Tests

To run the tests, developers should run `./gradlew clean` before `./gradlew test` to see the test results in the terminal (or for repeated testing).

## Troubleshooting

At the root directory:

- rm \*/database/\*.db to clean up dbs for a fresh start
- `./gradlew clean`

