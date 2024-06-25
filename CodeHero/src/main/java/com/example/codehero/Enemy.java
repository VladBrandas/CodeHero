package com.example.codehero;

import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.Serializable;

public class Enemy implements Serializable {
    private static final long serialVersionUID = 1L;
    private int row;
    private int col;
    private int hp = 100; // Enemy starts with 100 HP
    private transient ImageView imageView; // Mark imageView as transient
    private String imagePath = "enemy2.png"; // Add this field to store the image path
    private transient Label messageLabel; // Mark messageLabel as transient
    private transient CodeHero codeHero = new CodeHero(); // Mark codeHero as transient

    public Enemy(int row, int col) {
        this.row = row;
        this.col = col;
        initializeImageView();
    }
    void setRow(int row){
        this.row= row;
    }
    void setCol(int col){
        this.col= col;
    }

    public Enemy(int row, int col, int hp) {
        this.row = row;
        this.col = col;
        this.hp = hp;
        initializeImageView();
    }

    private void initializeImageView() {
        this.imageView = new ImageView(new Image(imagePath));
        this.imageView.setFitWidth(50); // Set width to fit the grid cell
        this.imageView.setFitHeight(50); // Set height to fit the grid cell
    }

    // Add this method to reinitialize the transient fields after deserialization
    public void reinitializeTransientFields() {
        initializeImageView();
        this.messageLabel = new Label();
       // this.codeHero = new CodeHero();
    }

    public void setMessageLabel(Label messageLabel) {
        this.messageLabel = messageLabel;
    }

    void setHp(int hp) {
        this.hp = hp;
    }

    // Method to display a message in the label
    protected void displayMessage(String message) {
        CodeHero.updateMessageLabel(message);
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public int getHp() {
        return hp;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public void takeDamage(int damage) {
        hp -= damage;
        if (hp <= 0) {
            displayMessage("Enemy defeated!");
        }
    }

    public boolean isDead() {
        return hp <= 0;
    }

    public void removeFromMaze(Maze maze) {
        maze.getChildren().remove(this.imageView);
        maze.getEnemies().remove(this);
    }

    public void attack(Character character, Maze maze) {
        displayMessage("Enemy attacks!");
        // character.blockEnemyAttack(); // Trigger character's block action
    }
    public void moveCloser(Character target, Maze maze) {
        int targetRow = target.getRow();
        int targetCol = target.getCol();

        int rowDiff = targetRow - getRow();
        int colDiff = targetCol - getCol();

        if (rowDiff != 0) {
            setRow(getRow() + (rowDiff > 0 ? 1 : -1));
        } else if (colDiff != 0) {
            setCol(getCol() + (colDiff > 0 ? 1 : -1));
        }

        if (maze.canMoveTo(getRow(), getCol())) {
            maze.updateCharacterPosition(getRow(), getCol(), target);
            displayMessage("Enemy moved closer to the mage.");
        }
    }
}
