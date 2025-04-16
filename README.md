# KtorConnect - Android Ktor Server-Client Applications

![Status](https://img.shields.io/badge/Status-Development-yellow)
![Purpose](https://img.shields.io/badge/Purpose-Educational-blue)

## Overview

KtorConnect is a project demonstrating a client-server architecture entirely on Android devices. It consists of two separate Android applications:

1. **Server Application**: An Android app that hosts a Ktor server using a foreground service
2. **Client Application**: An Android app that connects to the server via REST API and WebSocket

This system allows Android devices to communicate with each other directly over a local network, without requiring an external server.

## Architecture

![project-architecture-final](https://github.com/user-attachments/assets/96ff540b-38a7-45b2-b6f6-8f314e221bce)

*Architecture diagram showing the components and communication flow between the server and client applications.*

## Applications

### Server Application

The server app runs a Ktor HTTP and WebSocket server directly on the Android device, making it accessible over the local network.

**Key Components:**
- `MainActivity`: UI for controlling the server (start/stop)
- `ServerManager`: Manages server lifecycle and maintains server state
- `ServerService`: Foreground service that runs the Ktor server
- `DataRepository`: Manages the sample data
- `ServerModule`: Configures Ktor routes and endpoints

**Features:**
- Start/stop server control
- Server status monitoring
- Port configuration
- IP address display for connection
- Real-time logs
- REST API endpoints for CRUD operations
- WebSocket support for real-time updates

### Client Application

The client app connects to the server app and demonstrates data exchange capabilities.

**Key Components:**
- `MainActivity`: Client UI
- `KtorApiClient`: Handles HTTP requests and WebSocket connection
- `MainViewModel`: Manages UI state and server communication

**Features:**
- Server connection configuration
- Data retrieval and display
- Submitting new data
- Real-time updates via WebSocket

## Communication

The applications communicate through:

1. **REST API**: For standard CRUD operations
   - GET `/api/items`: Get all items
   - GET `/api/items/{id}`: Get specific item
   - POST `/api/items`: Add new item
   - PUT `/api/items/{id}`: Update an item
   - DELETE `/api/items/{id}`: Delete an item
   - POST `/api/broadcast`: Broadcast a message to all WebSocket clients

2. **WebSocket**: For real-time communication
   - Endpoint: `/ws`
   - Provides instant data updates to all connected clients

## Requirements

- Android SDK 28+
- Kotlin 1.8+
- Ktor 3.1.2
- Android Studio Arctic Fox or later

## Setup and Installation

1. Clone the repository
2. Open both projects in Android Studio
3. Build and run the server application on one device
4. Note the IP address displayed in the server application
5. Build and run the client application on another device
6. Enter the server's IP address and port in the client app
7. Connect and begin data exchange

## Use Cases

- Local data sharing between Android devices
- Simple client-server demonstration
- Temporary local API server for testing
- IoT control systems
- Local network applications without internet dependency

## Technical Details

- **Networking**: Uses Ktor for both server and client side
- **Concurrency**: Utilizes Kotlin Coroutines for asynchronous operations
- **UI**: Built with Jetpack Compose
- **Architecture**: MVVM pattern with repository
- **Serialization**: Kotlin Serialization for JSON handling

## Project Status

⚠️ **This project is currently in development and is intended for demonstration purposes only.**

This is a proof-of-concept application showcasing Ktor server-client architecture on Android devices. It is not intended for production use in its current state. Features may be incomplete, and breaking changes might occur in future updates.

Feel free to use this codebase for learning and experimentation, but be aware of the following limitations:
- Security features are minimal
- Error handling may not cover all edge cases
- Performance optimizations are limited
- Documentation is still in progress

Contributions, suggestions, and feedback are welcome!

## Future Enhancements

The following features are planned for future development:

- [ ] **Modularization**: Restructure the application using a modular architecture to improve maintainability, enable parallel development, and facilitate feature isolation
- [ ] **Dependency Injection with Dagger Hilt**: Implement proper dependency injection to improve code organization, testability, and maintainability
- [ ] **Enhanced Security**: Implement proper authentication and encryption for server-client communication
- [ ] **Offline Support**: Add local caching and synchronization when reconnecting
- [ ] **UI Improvements**: Enhance the user interface with more interactive elements and better visual feedback
- [ ] **Comprehensive Testing**: Add unit tests, integration tests, and UI tests
- [ ] **Multi-Client Support**: Improve handling of multiple simultaneous client connections
- [ ] **Configuration Persistence**: Save server and client settings between sessions
      
## License

[MIT License](LICENSE)

## Acknowledgements

[![Android Studio](https://img.shields.io/badge/Android%20Studio-Meerkat-3DDC84)](https://developer.android.com/studio)

This project was developed with assistance from Gemini AI Assistant, an integrated AI feature in Android Studio Meerkat that provides code suggestions and development guidance.

[![Ktor](https://img.shields.io/badge/Ktor-3.1.2-7848AA)](https://ktor.io/) - Asynchronous web framework for Kotlin  
[![Kotlin](https://img.shields.io/badge/Kotlin-1.8.0-F88909)](https://kotlinlang.org/) - Modern programming language for Android  
[![Coroutines](https://img.shields.io/badge/Coroutines-1.6.4-683DBA)](https://kotlinlang.org/docs/coroutines-overview.html) - Asynchronous programming framework  
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-1.4.0-4285F4)](https://developer.android.com/jetpack/compose) - Modern UI toolkit for Android  
[![Android](https://img.shields.io/badge/Android-SDK%2028+-3DDC84)](https://developer.android.com/) - Mobile application platform  
[![Architecture](https://img.shields.io/badge/Architecture-MVVM-CD9834)](https://developer.android.com/topic/libraries/architecture) - For MVVM implementation
