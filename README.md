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
<br><br>
## Screenshots
<br><br>
<h4>Seller Side<h4>

<img width="233" height="460" alt="Screenshot 2026-05-12 214151" src="https://github.com/user-attachments/assets/d158b87f-fc1a-498b-9f79-f86563610562" />
<img width="233" height="460" alt="Screenshot 2026-05-12 214825" src="https://github.com/user-attachments/assets/5dd0f9ee-5d5b-4708-8455-1775aa276321" />
<img width="233" height="460" alt="Screenshot 2026-05-12 214840" src="https://github.com/user-attachments/assets/dec325ad-208a-456c-8fe3-83dfd175292b" />
<br><br>
<img width="233" height="460" alt="Screenshot 2026-05-12 215034" src="https://github.com/user-attachments/assets/186ec7fc-f8bb-467b-94d7-fdbf6de1181d" />

<img width="233" height="460" alt="Screenshot 2026-05-12 215058" src="https://github.com/user-attachments/assets/b02fef78-b23e-4c52-8c5f-7cf10afc54b1" />
<br><br>
<br><br>
<h4>User Side<h4>

<img width="233" height="460" alt="Screenshot 2026-05-12 215143" src="https://github.com/user-attachments/assets/effa6f68-aded-44c7-934b-350e04605db5" />
<img width="233" height="460" alt="Screenshot 2026-05-12 215506" src="https://github.com/user-attachments/assets/0ab17c3f-9204-42a6-8dae-dea9f9046873" />
<img width="233" height="460" alt="Screenshot 2026-05-12 215724" src="https://github.com/user-attachments/assets/af78cffd-55e6-444e-9e82-765d1e86f53c" />

<br><br>
<img width="233" height="460" alt="Screenshot 2026-05-12 215205" src="https://github.com/user-attachments/assets/a2f739ca-0d37-4b0d-ada2-36467f3b59ff" />
<img width="233" height="460" alt="Screenshot 2026-05-12 215400" src="https://github.com/user-attachments/assets/c2922483-fe88-4ab1-9ee0-e198680c7098" />
<img width="233" height="460" alt="Screenshot 2026-05-12 215444" src="https://github.com/user-attachments/assets/147dc6b2-d0fb-4929-a0a5-ee066a9ed04e" />

<br><br>
## Getting Started

To get a local copy up and running, follow these steps.

### Prerequisites

* Android Studio installed on your machine.
* An Android device or emulator running API level 24 or higher.

### Installation

1.  Clone the repository:
    ```bash
    git clone https://github.com/iamrazii/ezshop.git
    ```
2.  Open the project in **Android Studio**.
3.  Connect to Firebase:
    * Go to the Firebase Console and create a new project.
    * Add an Android app to the project and download the `google-services.json` file.
    * Place the `google-services.json` file in the `app/` directory of your project.
    * Ensure Authentication and your chosen Firebase Database (Firestore or Realtime Database) are enabled in the Firebase console.
4.  Sync the Gradle files to install all necessary dependencies.
5.  Build and run the application on your device or emulator.
