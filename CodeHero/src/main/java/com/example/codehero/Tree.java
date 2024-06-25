package com.example.codehero;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.Serializable;

public class Tree implements Serializable {
    private static final long serialVersionUID = 1L;
    private  int row;
    private  int col;
    private transient ImageView imageView;

    public Tree(int row, int col) {
        this.row = row;
        this.col = col;
        initializeImageView();
    }

    private void initializeImageView() {
        this.imageView = new ImageView(new Image("tree.png"));
        this.imageView.setFitWidth(50); // Set width to fit the grid cell
        this.imageView.setFitHeight(50); // Set height to fit the grid cell
    }

    // Add this method to reinitialize the transient fields after deserialization
    public void reinitializeTransientFields() {
        initializeImageView();
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public ImageView getImageView() {
        return imageView;
    }
}
