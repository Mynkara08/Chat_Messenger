# Firebase Chat App (Kotlin)

Welcome to the Firebase Chat App repository! This app enables real-time chat functionality using Firebase as the backend. Below is a list of key files and their functionalities:

## Activity Files
- **ChatActivity.kt**: The main activity for individual chat conversations.
- **LoginOtpActivity.kt**: Handles user authentication using OTP.
- **LoginPhoneNumberActivity.kt**: Manages phone number-based user login.
- **LoginUsernameActivity.kt**: Controls user login using a username.
- **MainActivity.kt**: The app's entry point and primary navigation hub.
- **SearchUserActivity.kt**: Allows users to search for other users to initiate chats.
- **SplashActivity.kt**: Displays a splash screen while the app initializes.

## Fragment Files
- **ChatFragment.kt**: Manages chat UI and logic within the chat activity.
- **ProfileFragment.kt**: Handles user profile display and editing.
- **SearchUserFragment.kt**: Displays user search results and options for starting a chat.


## Features
- **Real-Time Chat**: Instant messaging between users with real-time updates.
- **User Authentication**: Secure user authentication using OTP, phone number, and username.
- **User Search**: Find and initiate chats with other users.
- **User Profiles**: Display and edit user profile information.
- **Push Notifications**: Receive notifications for new messages and other events.

## Technologies Used
- **Kotlin**: Primary programming language for the app.
- **Firebase Authentication**: For user login and authentication.
- **Firebase Realtime Database**: For storing and retrieving chat messages.
- **Firebase Cloud Messaging**: For sending push notifications.
- **Android SDK**: Core framework for building the app.

## Getting Started
To use this app:

1. **Clone or download the repository.**
2. **Set up your Firebase project and update the `google-services.json` file.**
    - Go to the Firebase Console.
    - Create a new project (or use an existing one).
    - Add an Android app to your project.
    - Download the `google-services.json` file.
    - Place the `google-services.json` file in the `app` directory of your Android project.
3. **Build and run the app on your Android device or emulator.**
    - Open the project in Android Studio.
    - Sync the project with Gradle files.
    - Run the app.



