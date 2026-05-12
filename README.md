# Ezshop

A feature-rich e-commerce Android application designed to provide a seamless shopping experience for buyers and powerful management tools for sellers. Powered entirely by a cloud-based Firebase backend, Ezshop leverages Artificial Intelligence to automate seller workflows, streamline inventory management, and enhance user decision-making.

## Features

**For Users:**
* **AI Review Summaries:** Make informed purchasing decisions quickly with AI-generated summaries of customer product reviews.
* **Chat with Seller:** Direct, real-time messaging capabilities to communicate with sellers for inquiries and support.
* **Dynamic Product Catalog:** Browse a wide variety of products with smooth scrolling interfaces powered by custom RecyclerView adapters.
* **Cart Management:** Easily add, remove, and update items in the shopping cart with reliable cloud synchronization.
* **User Authentication:** Secure login and registration functionality to keep user profiles and data safe.

**For Sellers:**
* **AI Forecast Dashboard:** Access smart forecasting and actionable insights that suggest trending products to add to your inventory.
* **Smart Product Listing:** Streamline adding new items with AI tools that automatically generate detailed product descriptions and instantly select the correct category based simply on the product name.

## Tech Stack

* **Platform:** Android
* **Language:** Java
* **UI/Design:** XML
* **Backend/Database:** Firebase (Authentication, Firestore / Realtime Database)
* **AI Integration:** LLM integration for forecasting, automated descriptions, categorization, and review summarization.

## Screenshots

*(Replace the placeholders below with actual screenshots of your app)*

* `[Home Screen & Review Summary Thumbnail]`
* `[Seller Dashboard & AI Forecast Thumbnail]`
* `[Smart Add Product Screen Thumbnail]`
* `[Chat with Seller Thumbnail]`

## Getting Started

To get a local copy up and running, follow these steps.

### Prerequisites

* Android Studio installed on your machine.
* An Android device or emulator running API level 24 or higher.

### Installation

1.  Clone the repository:
    ```bash
    git clone https://github.com/qasim/ezshop.git
    ```
2.  Open the project in **Android Studio**.
3.  Connect to Firebase:
    * Go to the Firebase Console and create a new project.
    * Add an Android app to the project and download the `google-services.json` file.
    * Place the `google-services.json` file in the `app/` directory of your project.
    * Ensure Authentication and your chosen Firebase Database (Firestore or Realtime Database) are enabled in the Firebase console.
4.  Sync the Gradle files to install all necessary dependencies.
5.  Build and run the application on your device or emulator.
