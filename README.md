# ArtBook

This project is a simple Android application that allows users to save, view, and manage their favorite works of art. Users can create their own personal digital art collection by adding an artwork's name, artist, year of creation, and a photograph.

The application uses an SQLite database for local data storage and incorporates modern Android development practices.

## ✨ Features

  * **List Artworks:** All saved artworks are listed on the main screen with a modern card view (`CardView`).
  * **Add New Artwork:** Users can create new records by entering the artwork's name, artist, year, and selecting a photo from the gallery.
  * **View Artwork Details:** When an artwork from the list is clicked, its information and image are displayed on a read-only detail page.
  * **Delete Artwork:** Records can be safely deleted via a confirmation dialog that appears upon a long-press of an item in the main list.
  * **Local Data Storage:** All data is permanently stored in the device's local memory using an SQLite database.
  * **Permission Management:** Permissions required for gallery access are managed using the modern `Activity Result API`.

## 🛠️ Tech Stack / Built With

  * **Language:** Java
  * **Platform:** Android SDK
  * **UI:** XML, View Binding
  * **Components:**
      * `RecyclerView` (For dynamic lists)
      * `CardView` (For a modern card design)
      * `AlertDialog` (For confirmation dialogs)
  * **Database:** SQLite (For local data storage)
  * **APIs:**
      * `Activity Result API` (For gallery access and permission management)


## 🚀 Getting Started / Installation

To get a local copy up and running, follow these simple steps:

1.  Clone the repo:
    ```sh
    git clone https://github.com/huseyinsan/ArtBook.git
    ```
2.  Open the project in Android Studio.
3.  Wait for the necessary SDK and Gradle dependencies to download.
4.  Run the project on an emulator or a physical device.


## Acknowledgement

This project was developed by following the tutorial series of [Atıl Samancıoğlu]
(https://github.com/atilsamancioglu).  
Many thanks to him for his educational content and resources.

---
