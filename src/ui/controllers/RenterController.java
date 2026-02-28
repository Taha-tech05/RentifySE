package ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class RenterController {

	@FXML
	private Label dashboardLabel;

	// This method runs when the scene is loaded
	@FXML
	public void initialize() {
		dashboardLabel.setText("Welcome to Renter Dashboard!");
	}
}