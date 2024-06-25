package com.example.codehero;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Maze extends GridPane implements Serializable {

    private static final long serialVersionUID = 1L;

    private int rows;
    private int cols;
    private transient Rectangle[][] cells;
    private Character character; // Character can be Knight or Mage
    private List<Enemy> enemies;
    private List<Rock> rocks;
    private List<Tree> trees;
    private List<Potion> potions;
    private transient Label statusLabel; // Add this to keep track of status messages
    private transient Label characterHpLabel;
    private transient Label enemyHpLabel;

    public Maze(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        cells = new Rectangle[rows][cols];
        enemies = new ArrayList<>();
        rocks = new ArrayList<>();
        trees = new ArrayList<>();
        potions = new ArrayList<>();
        createGrid();
    }

    public void updateCharacterHpLabel() {
        if (characterHpLabel != null && character != null) {
            characterHpLabel.setText("Character HP: " + character.getHp());
        }
    }

    public void updateEnemyHpLabel(Enemy enemy) {
        if (enemyHpLabel != null && enemies != null && enemies.contains(enemy)) {
            enemyHpLabel.setText("Enemy HP: " + enemy.getHp());
        }
    }

    public void setCharacter(Character character) {
        this.character = character;
        if (characterHpLabel != null && character != null) {
            characterHpLabel.setText("Character HP: " + character.getHp());
        }
    }

    private void createGrid() {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Rectangle cell = new Rectangle(50, 50);
                cell.setFill(Color.GREEN);
                cell.setStroke(Color.BLACK);
                cells[row][col] = cell;
                add(cell, col, row);
            }
        }
    }

    public void placeCharacter(Character character) {
        this.character = character;
        add(character.getImageView(), character.getCol(), character.getRow());
    }

    public void placeEnemies(List<Enemy> enemies) {
        this.enemies = enemies;
        for (Enemy enemy : enemies) {
            System.out.println(enemy);
            add(enemy.getImageView(), enemy.getCol(), enemy.getRow());
        }
    }

    public void placeRocks(List<Rock> rocks) {
        this.rocks = rocks;
        for (Rock rock : rocks) {
            add(rock.getImageView(), rock.getCol(), rock.getRow());
        }
    }

    public void placeTrees(List<Tree> trees) {
        this.trees = trees;
        for (Tree tree : trees) {
      //      if (tree != null && tree.getImageView() != null) {
//                GridPane.setRowIndex(tree.getImageView(), tree.getRow());
//                GridPane.setColumnIndex(tree.getImageView(), tree.getCol());
                add(tree.getImageView(), tree.getCol(), tree.getRow());
       //     }
        }
    }
    public void placePotions(List<Potion> potions) {
        this.potions = potions;
        for (Potion potion : potions) {
           // if (potion != null && potion.getImageView() != null) {
//                GridPane.setRowIndex(potion.getImageView(), potion.getRow());
//                GridPane.setColumnIndex(potion.getImageView(), potion.getCol());
                add(potion.getImageView(), potion.getCol(), potion.getRow());
         //   }
        }
    }


    public void updateCharacterPosition(int oldRow, int oldCol, Character character) {
        getChildren().removeIf(node -> GridPane.getRowIndex(node) == oldRow && GridPane.getColumnIndex(node) == oldCol && node instanceof ImageView && isCharacterImage((ImageView) node));
        placeCharacter(character);
    }

    private boolean isCharacterImage(ImageView imageView) {
        String imageUrl = imageView.getImage().getUrl();
        return imageUrl.contains("knight") || imageUrl.contains("mage");
    }

    public boolean canMoveTo(int row, int col) {
        if (enemies == null) {
            enemies = new ArrayList<>();
        }
        // Check if the cell is within bounds
        if (row < 0 || row >= rows || col < 0 || col >= cols) {
            return false;
        }
        // Check if there's an enemy in the target cell
        for (Enemy enemy : enemies) {
                     if (enemy.getRow() == row && enemy.getCol() == col) {
                return false;
            }
        }
        // Check if there's a rock or tree in the target cell
        for (Rock rock : rocks) {
            if (rock.getRow() == row && rock.getCol() == col) {
                return false;
            }
        }
        for (Tree tree : trees) {
            if (tree.getRow() == row && tree.getCol() == col) {
                return false;
            }
        }
        return true;
    }
    public void updateEnemyPosition(int oldRow, int oldCol, int newRow, int newCol, Enemy enemy) {
        // Remove enemy from its old position in the grid
        getChildren().remove(enemy.getImageView());

        // Update enemy's position
        enemy.setRow(newRow);
        enemy.setCol(newCol);

        // Add enemy to the new position in the grid
        add(enemy.getImageView(), newCol, newRow);
    }

    public void moveEnemy(Enemy enemy, int newRow, int newCol) {
        // Remove enemy from current position in the grid
        getChildren().remove(enemy.getImageView());
        // Update enemy's position
        enemy.setRow(newRow);
        enemy.setCol(newCol);
        // Add enemy to the new position in the grid
        add(enemy.getImageView(), newCol, newRow);
    }
    public boolean collectPotion(int row, int col) {
        for (Potion potion : potions) {
            if (potion.getRow() == row && potion.getCol() == col) {
                if (character != null && character.getHp() < 50) {
                    getChildren().remove(potion.getImageView());
                    potions.remove(potion);
                }
                return true;
            }
        }
        return false;
    }

    public void removeEnemy(Enemy enemy) {
        getChildren().remove(enemy.getImageView());
        enemies.remove(enemy);
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }

    public List<Potion> getPotions() {
        return potions;
    }

    public void clearPosition(int row, int col) {
        getChildren().removeIf(node -> GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == col);
    }

    public void removePotion(Potion potion) {
        getChildren().remove(potion.getImageView());
        potions.remove(potion);
    }

    public void update() {
        // Update the maze (could involve redrawing the grid or updating certain elements)
    }


    // Method to simulate an enemy's attack
    public void enemyAttack(Character character, Label statusLabel) {
        // Randomly determine if the attack hits or not
        boolean attackHits = Math.random() > 0.5; // 50% chance of hitting
        if (attackHits) {
            character.takeDamage(20); // Example damage value
            updateStatusLabel(statusLabel, "Enemy attacks! You took damage.");
        } else {
            updateStatusLabel(statusLabel, "Enemy attacks! But you blocked the attack.");
        }
    }

    private void updateStatusLabel(Label statusLabel, String message) {
        Platform.runLater(() -> {
            statusLabel.setText(message);
        });
    }

    public int reinitializeTransientFields() {
        this.cells = new Rectangle[rows][cols];
        createGrid();

        // Re-add character
        if (this.character != null) {
            placeCharacter(this.character);
        }

        // Re-add enemies
        if (this.enemies != null) {
            List<Enemy> enemiesCopy = new ArrayList<>(this.enemies); // Create a new list to avoid modifying the original list
            for (Enemy enemy : enemiesCopy) {
                enemy.reinitializeTransientFields();
                add(enemy.getImageView(), enemy.getCol(), enemy.getRow());
            }
        }

        // Re-add rocks
        for (Rock rock : rocks) {
            rock.reinitializeTransientFields();
            add(rock.getImageView(), rock.getCol(), rock.getRow());
        }
        for (Tree tree : trees) {
            tree.reinitializeTransientFields();
            add(tree.getImageView(), tree.getCol(), tree.getRow());
        }

        // Re-add potions
        if (this.potions != null) {
            List<Potion> potionsCopy = new ArrayList<>(this.potions); // Create a new list to avoid modifying the original list
            for (Potion potion : potionsCopy) {
                potion.reinitializeTransientFields();
                add(potion.getImageView(), potion.getCol(), potion.getRow());
            }
        }

        // Initialize the labels
        this.characterHpLabel = new Label("Character HP: " + (character != null ? character.getHp() : "N/A"));
        this.enemyHpLabel = new Label("Enemy HP: " + (enemies != null && !enemies.isEmpty() ? enemies.get(0).getHp() : "N/A"));
        this.statusLabel = new Label("Status: ");
        return 1;
    }


}
