# Quizzlive Network Application Documentation

## Architecture Overview
Quizzlive is a real-time network application built with Java Maven multi-module architecture.

```
Quizzlive/
├── pom.xml
├── docs/
│   └── protocol_spec.md
├── quizzlive-shared/
│   ├── pom.xml
│   └── src/main/java/com/quizzlive/shared/
│       ├── protocol/ (OpCode, Packet)
│       └── model/ (UserDTO, QuestionDTO)
├── quizzlive-server/
│   ├── pom.xml
│   └── src/main/java/com/quizzlive/server/
│       ├── config/ (ServerConfig)
│       ├── network/ (SocketServer, ClientSession, SessionManager)
│       └── ServerApp.java
└── quizzlive-client/
    ├── pom.xml
    └── src/main/java/com/quizzlive/client/
        ├── network/ (SocketClient)
        └── ClientApp.java
```

## Network Protocol Specification
- **Transport**: TCP Sockets (ObjectOutputStream / ObjectInputStream or Byte framing)
- **Default Port**: `8888`
- **Data Unit**: `Packet` object containing an `OpCode` (integer enum) and `payloadJson` (JSON string).

### OpCodes Summary:
- `1`: `LOGIN_REQ`
- `2`: `LOGIN_RESP`
- `10`: `CREATE_ROOM_REQ`
- `11`: `CREATE_ROOM_RESP`
- `12`: `JOIN_ROOM_REQ`
- `13`: `JOIN_ROOM_RESP`
- `20`: `START_GAME_REQ`
- `21`: `QUESTION_SEND`
- `22`: `SUBMIT_ANSWER_REQ`
- `23`: `SUBMIT_ANSWER_RESP`
- `30`: `SCORE_UPDATE_BROADCAST`
- `31`: `GAME_OVER_BROADCAST`
- `99`: `ERROR_RESP`
