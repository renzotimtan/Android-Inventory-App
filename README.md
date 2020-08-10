# Android-Inventory-App

This is an inventory management Android application that allows users to log in and view the current status of their inventory.

Description:
Multiple users can be registered within the application, and each user can add their own items, consisting of its image, details, and current quantity. 
The user may add, edit, and delete items, depending on the status of their inventory. They may also increase and decrease the quantity of each items as well.

Features:
- Consists of a register and login page where users can store their own accounts in a Realm database. 
- Users may edit their own account username and password.
- An admin page is present as well for admins to see which users are currently registered within their application. 
- Once the user is logged in, the user has an inventory interface where they can add, edit, and delete their items, depending on the status of their inventory.
- The user has an option to add an image to the item of his/her inventory for easy tracking. Picasso is used to assist this feature. 
- The interface will be executed through a RecyclerView, where they can scroll through their items without too many TextViews bloated on the screen. 
