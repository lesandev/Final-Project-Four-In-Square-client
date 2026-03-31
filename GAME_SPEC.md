# Four in a Square (Arba BaRibua)

## Overview

`Four in a Square` is a turn-based board game where a human player (`Red`) plays against an AI opponent (`Blue`).

The project is designed as a client-server game:
- the Android app is the game UI;
- the Python backend validates moves, manages game state, and performs AI turns.

## Board Structure

The board is made of a `3 x 3` grid of large squares:
- total large squares: `9`
- square indexes: `0..8`

Each large square contains a `2 x 2` mini-grid:
- total mini-slots in each square: `4`
- slot indexes: `0..3`

Mini-slot layout inside a square:

```text
 0 | 1
---|---
 2 | 3
```

Square index layout on the main board:

```text
 0 | 1 | 2
---|---|---
 3 | 4 | 5
---|---|---
 6 | 7 | 8
```

There are `36` total mini-slots on the whole board.

One large square is always empty. This square is called the `hole`.
- At the start of the game, the hole is at square `4` (the center square).

## Players

- `Red (R)`: human player
- `Blue (B)`: AI / server player

Red always starts first.

## Pieces

Each player has a limited number of pieces.

Default:
- `8` pieces for Red
- `8` pieces for Blue

This amount can be configured per game with `piecesPerPlayer`.

## Turn Rules

Players alternate turns.

A turn has two mandatory actions during the placement part of the game:

1. Place one piece
2. Slide one neighboring square into the hole

### 1. Place One Piece

The current player places one piece into any empty mini-slot on the board.

Restrictions:
- the slot must be empty;
- the slot cannot be inside the current hole square.

### 2. Slide One Square

After placing a piece, the current player must slide one large square into the hole.

Rules for sliding:
- only a square adjacent to the hole may slide;
- adjacency is only `up`, `down`, `left`, or `right`;
- diagonal movement is not allowed;
- when a square slides, all pieces inside that square move with it;
- the moved square swaps places with the hole.

## Game Phases

The game has three phases.

### `placement`

The player must place a piece into an empty slot.

### `placementSlide`

The player has already placed a piece and must now slide one adjacent square into the hole.

### `movement`

All pieces of both players have already been placed.

In this phase:
- no more pieces are placed;
- each turn consists only of sliding one adjacent square into the hole.

## Phase Progression

- The game starts in `placement`.
- After a player places a piece, the state becomes `placementSlide`.
- After the required slide is performed, the turn passes to the other player.
- Once both players have placed all of their pieces, the game enters `movement`.

## Win Condition

A player wins immediately when they create a solid `2 x 2` block of their own color anywhere on the global board.

This winning block can appear:
- completely inside one large square;
- across two neighboring squares;
- across the corner intersection of four squares.

The win check is done on the full logical `6 x 6` mini-grid formed by the board:
- `3` large squares across, each with `2` mini-columns;
- `3` large squares down, each with `2` mini-rows.

Any `2 x 2` sub-grid of the same color is a win.

## Global Coordinate Mapping

To convert a piece from square coordinates into the full `6 x 6` grid:

```text
globalRow = squareRow * 2 + miniRow
globalCol = squareCol * 2 + miniCol

squareRow = floor(squareIndex / 3)
squareCol = squareIndex % 3
miniRow   = floor(slotIndex / 2)
miniCol   = slotIndex % 2
```

## Draw Condition

In the `movement` phase, if the current player has no legal slide, the game ends in a draw.

On a normal `3 x 3` board this should almost never happen, but the rule exists for completeness.

## Game State Model

The game state is represented by the following structure:

```json
{
  "board": [[null, "R", null, null], "..."],
  "phase": "placement",
  "currentPlayer": "R",
  "placed": { "R": 0, "B": 0 },
  "piecesPerPlayer": 8,
  "holeSquareIndex": 4,
  "selectedSquareIndex": null,
  "winner": null,
  "drawReason": null
}
```

### Field Meaning

- `board`: 9 squares, each square containing 4 mini-slots
- `phase`: current phase of the turn
- `currentPlayer`: whose turn it is now
- `placed`: number of pieces already placed by each player
- `piecesPerPlayer`: maximum pieces per player
- `holeSquareIndex`: which large square is currently the hole
- `selectedSquareIndex`: optional client-side UI state
- `winner`: `null`, `"R"`, or `"B"`
- `drawReason`: optional explanation if the game ends in a draw

### Slot Values

Each mini-slot may contain:
- `null` for empty
- `"R"` for a red piece
- `"B"` for a blue piece

## REST API Contract

Base URL:

```text
http://127.0.0.1:8000
```

### `POST /games`

Creates a new game.

Request:

```json
{
  "piecesPerPlayer": 8
}
```

Response:

```json
{
  "gameId": "abc123",
  "playerToken": "tok_xxx",
  "state": {
    "board": [[null, null, null, null], "..."],
    "phase": "placement",
    "currentPlayer": "R",
    "placed": { "R": 0, "B": 0 },
    "piecesPerPlayer": 8,
    "holeSquareIndex": 4,
    "selectedSquareIndex": null,
    "winner": null,
    "drawReason": null
  }
}
```

### `GET /games/{gameId}`

Returns the current game state.

Response:

```json
{
  "state": { "...": "GameState" }
}
```

### `POST /games/{gameId}/move`

Submits one human action.

Server behavior:
1. validate the human move;
2. apply the human move;
3. check win or draw;
4. if the game is not over and it is Blue's turn, perform the AI move;
5. check win or draw again;
6. return the updated state.

Placement request:

```json
{
  "action": "place",
  "squareIndex": 0,
  "slotIndex": 2,
  "playerToken": "tok_xxx"
}
```

Slide request:

```json
{
  "action": "slide",
  "squareIndex": 3,
  "playerToken": "tok_xxx"
}
```

Success response:

```json
{
  "state": { "...": "GameState" }
}
```

Error response:

```json
{
  "error": "Invalid move: slot is occupied"
}
```

Note:
- in a `slide` action, `squareIndex` is the square that moves into the current hole.
- the chosen square must be adjacent to `holeSquareIndex`.

### `POST /games/{gameId}/restart`

Restarts the game and returns a fresh state.

Response:

```json
{
  "state": { "...": "GameState" }
}
```

## Typical Client Flow

### During Placement

1. The human taps an empty mini-slot.
2. Client sends `place`.
3. The human then chooses a valid adjacent square to slide.
4. Client sends `slide`.
5. The server applies the slide and then performs the AI turn.
6. The updated state is returned to the Android client.

### During Movement

1. The human selects one highlighted square adjacent to the hole.
2. Client sends `slide`.
3. The server applies the move and performs the AI move.
4. The updated state is returned.

## UI Requirements

The Android client should provide:
- a visual `3 x 3` board;
- visible red and blue pieces;
- a clearly visible hole square;
- highlighted legal slide squares next to the hole;
- a turn indicator;
- the current phase;
- placed piece counts;
- a restart button.

Recommended UI behavior:
- empty hole square shown with a dashed border;
- legal slide targets shown with a green dashed border;
- tap empty slot during `placement`;
- tap highlighted square during `placementSlide` or `movement`.

## Project Goal

This project demonstrates:
- client-server game architecture;
- state modeling;
- move validation;
- turn-based game logic;
- AI integration through a Python backend;
- Android UI rendering based on server-driven state.
