// God help me for i'm about to write the most cursed code in the history of programming


package com.govsim.govsim.gui;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.util.Timer;
import com.govsim.govsim.ministry.Ministry;
import com.govsim.govsim.president.President;
import com.govsim.govsim.ministry.DefenseMinistry;
import com.govsim.govsim.ministry.HealthMinistry;


// controller for the gui and the colors and everything else 
// mind you i'm doing this in a sober and with zero idea how to do this in a good way, so please don't judge 
//  the code i will refactor this later, but for now, this is the only way i can do it without breaking my brain


public class AlertController {
    
    @FXML
    private Label alertLabel;

    private Timer timer;
    private Ministry defenseMinistry;
    private Ministry healthMinistry;
    private President president;

    public AlertController() {

        defenseMinistry = new DefenseMinistry(defenseMinistry);
        healthMinistry = new HealthMinistry(healthMinistry);
        president = new President(defenseMinistry, healthMinistry);

    }

    public void showAlert(String message, String severity) {
        alertLabel.setText(message);
        switch (severity.toLowerCase()) {
            case "normal":
                alertLabel.setStyle("-fx-background-color: green; -fx-text-fill: white;");
                break;
            case "dangerous":
                alertLabel.setStyle("-fx-background-color: red; -fx-text-fill: white;");
                break;
            default:
                alertLabel.setStyle("-fx-background-color: gray; -fx-text-fill: white;");
                break;


        }

    }
    public void clearAlert() {
        alertLabel.setText("");
        alertLabel.setStyle("");
    }


}