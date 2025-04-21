## FIXES:

- [x] Board dimensions
- [x] Migrate visuals to scene builder
- [x] Username password problem
- [x] Restore moves only does remote player color
- [x] Player can play for the AI if theyre fast enough
- [x] Resign on empty board softlocks (at least) local multi
- [x] Button size fix
- [x] disconnect no longer shows notif

## QOL / Cleanup
- [x] Send AI message every 2nd or third message, not based on rand
- [x] Option to disable custom cursors
- [x] Notification Manager
- [ ] Check friend online behavior (client on game screen)
    - [ ] Generally handle sending messages between clients managed by a game and those managed by the lobby (Client manager)
        - We probably need to change the server structure for this. If client 1 is in the lobby (managed by ClientManager) and the other client is in game (Managed by Game) there is no currently (safe) way clients to be written to or read from by different threads

## Features
- [x] Add AI with levels of difficulty
- [x] Leaderboard
- [x] Friends
- [x] Turn indicator
- [x] Profile pictures

- [ ] Message pane to choose message box from friends
- [ ] Text emoji drawer
- [ ] Art overhaul

## Client

- [ ] Add sound-effects 
    - [x] Background music
    - [x] Chip on drop
    - [ ] Button on click
    - [x] Chat on send
    - [x] Chat recived

### Game Screen
- [x] Design background (ART) 
- [ ] Add move effects
- [x] Add on-win animations
- [x] clouds
- [x] Rethink cloud animation mechanism


### Chat
- [x] Design background (ART) 
- [x] Design message UI 
    - [x] Background images (ART)
    - [x] Message info layout, etc.
- [ ] Add sender to chat message
- [x] Move send to other side
- [x] Add buttons for forfeit, offer draw, request forfeit
    - [x] with confirmation buttons
- [x] Style all control elements with pixel art (ART)
- [ ] When playing AI, chat should show AI icon as user img
- [ ] Chat should display player name next to icon

### Menu Screen
- [x] Style all control elements with pixel art (ART)
- [x] Design background (ART) 
- [x] Make settings controller + UI
    - [x] AI difficulty in settings
- [ ] Full quit button

### Connections
- [x] Create FXML scene + Controller
- [x] Style all control elements with pixel art  (ART)
- [x] Save connections on load and quit
- [x] Add option to play locally (against AI)
- [x] Be able to delete a connection

### Loading Screen
- [x] Create a neat connect-four based animation (ART)
- [x] Design background (ART) 


## Server
- [x] Handle main menu button in settings

## Other
- [x] Settings save on quit
