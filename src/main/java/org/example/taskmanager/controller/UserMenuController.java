package org.example.taskmanager.controller;

import static org.example.taskmanager.controller.AdminMenuController.showNewScreen;
import static org.example.taskmanager.controller.UserDocumentAddController.addDocument;
import static org.example.taskmanager.controller.UserProfileController.showMyProfile;

/**
 * Controller class for the user menu in the task manager application.
 * Handles navigation to various user screens and functionalities.
 */
public class UserMenuController {
    /**
     * Logs out the current user and returns to the login screen.
     */
    public void logOut(){
        showNewScreen("/org/example/taskmanager/logIn.fxml");
    }

    /**
     * Displays the profile of the current user.
     */
    public void myProfile(){
        showMyProfile();
    }

    /**
     * Initiates the process of adding a new document for the current user.
     */
    public void myDocuments() {
        addDocument();
    }

    /**
     * Navigates to the task search screen for the user.
     */
    public void showTaskSearch(){
        showNewScreen("/org/example/taskmanager/userTaskSearch.fxml");
    }
}