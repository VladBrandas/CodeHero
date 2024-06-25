package com.example.codehero;

import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.Serializable;
import java.util.List;
import java.util.Random;

public abstract class Character implements Serializable {
    protected int row;
    protected int col;
    protected int hp;
    protected transient ImageView imageView;
    protected transient Label messageLabel;
    protected Random random = new Random();
    private String imagePath;

    public Character(int row, int col, String imagePath, int hp) {
        this.row = row;
        this.col = col;
        this.hp = hp;
        this.imagePath = imagePath;
        this.imageView = new ImageView(new Image(imagePath));
        this.imageView.setFitWidth(50); // Set width to fit the grid cell
        this.imageView.setFitHeight(50); // Set height to fit the grid cell
    }

    public void increaseHealth(int amount) {
        hp += amount;
    }

    public int getRow() {
        return row;
    }

    public int getHealth() {
        return hp;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public void setMessageLabel(Label messageLabel) {
        this.messageLabel = messageLabel;
    }

    // Method to display a message in the label
    protected void displayMessage(String message) {
        CodeHero.updateMessageLabel(message);
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public void moveUp() {
        row = Math.max(0, row - 1);
    }

    public void moveDown() {
        row = Math.min(9, row + 1);
    }

    public void moveLeft() {
        col = Math.max(0, col - 1);
    }

    public void moveRight() {
        col = Math.min(9, col + 1);
    }

    public abstract void attack(List<Enemy> enemies, Maze maze);

    public abstract void block();

    public void takeDamage(int damage) {
        hp -= damage;
        if (hp <= 0) {
            displayMessage("Character has died!");
            hp = 0;
        }
        CodeHero.updateCharacterHpLabel(); // Update character HP label after taking damage
    }

    public abstract void rangedAttack(List<Enemy> enemies, Maze maze);
    public abstract void dodgeAndMoveAway(Maze maze);
    public boolean isGameOver() {
        return hp <= 0;
    }
    protected void attackEnemy(Enemy enemy, Maze maze) {
        //displayMessage("enemy attacks!");
        enemy.takeDamage(20); // Example: character's attack value is 20
        CodeHero.updateEnemyHpLabel(); // Update enemy HP label after taking damage
    }

    protected void blockEnemyAttack() {
        displayMessage("Blocked enemy attack!");
    }

    public void reinitializeTransientFields() {
        this.imageView = new ImageView(new Image(imagePath));
        this.imageView.setFitWidth(50); // Set width to fit the grid cell
        this.imageView.setFitHeight(50); // Set height to fit the grid cell
    }
}

class Knight extends Character {
    public Knight(int row, int col) {
        super(row, col, "knight.png", 150);
    }
    public void block() {
        if (random.nextBoolean()) {
            displayMessage("Block successful!");
        } else {
            displayMessage("Block failed!");
            takeDamage(20); // Example: character takes 20 damage on failed block
            CodeHero.updateCharacterHpLabel(); // Update character HP label after taking damage
        }
    }

    @Override
    public void attack(List<Enemy> enemies, Maze maze) {
        boolean attacked = false; // Flag to track if any enemy was attacked

        for (Enemy enemy : enemies) {
            // Check if the enemy is adjacent horizontally, vertically, or diagonally
            if (Math.abs(enemy.getRow() - getRow()) <= 1 && Math.abs(enemy.getCol() - getCol()) <= 1 &&
                    !(enemy.getRow() == getRow() && enemy.getCol() == getCol())) {

                attackEnemy(enemy, maze); // Attack the enemy
                attacked = true;

                if (enemy.isDead()) {
                    CodeHero.score += 50;
                    CodeHero.canBlock = false;
                    enemy.removeFromMaze(maze); // Remove the enemy if dead
                } else {
                    enemy.attack(this, maze); // Enemy retaliates if not dead
                }

                break; // Exit after attacking one enemy
            }
        }

        if (!attacked) {
            displayMessage("Move closer to the enemy to attack.");
            CodeHero.canBlock = false;
        }

        CodeHero.updateCharacterHpLabel(); // Update character HP label after the attack
    }

    public void dodgeAndMoveAway(Maze maze){}

    @Override
    public void rangedAttack(List<Enemy> enemies, Maze maze) {
        displayMessage("knight doesnt perform ranged attack");
    }
}

class Mage extends Character {
    public Mage(int row, int col) {
        super(row, col, "mage.png", 100);
    }
    public void block() {
        displayMessage("mage doesnt block");
    }

    @Override
    public void attack(List<Enemy> enemies, Maze maze) {
        displayMessage("mage doesnt perform attack");
    }
    @Override
    public void rangedAttack(List<Enemy> enemies, Maze maze) {
        boolean attacked = false; // Flag to track if any enemy was attacked

        for (Enemy enemy : enemies) {
            int enemyRow = enemy.getRow();
            int enemyCol = enemy.getCol();
            int distance = Math.abs(enemyRow - getRow()) + Math.abs(enemyCol - getCol());

            // Mage can attack from up to 2 squares away
            if (distance <= 2) {
                attackEnemy(enemy, maze); // Attack if adjacent or within range
                attacked = true;

                if (enemy.isDead()) {
                    CodeHero.score += 50;
                    CodeHero.canDodge = false;
                    enemy.removeFromMaze(maze); // Remove the enemy if dead
                } else {
                    // Check if enemy and mage are on adjacent diagonals
                    boolean onAdjacentDiagonal = isOnAdjacentDiagonal(enemyRow, enemyCol);

                    // Move enemy 1 square closer towards mage if not adjacent or on adjacent diagonal
                    if (distance == 2 && !onAdjacentDiagonal) {
                        moveEnemyTowardsMage(enemy, maze);
                    }

                    // Check distance again after potentially moving
                    enemyRow = enemy.getRow();
                    enemyCol = enemy.getCol();
                    distance = Math.abs(enemyRow - getRow()) + Math.abs(enemyCol - getCol());

                    // Enemy can attack if 1 square away and not on adjacent diagonal AND mage has not moved away
                    if (distance == 1 || onAdjacentDiagonal && !hasMageMovedAway(enemy)) {
                        enemy.attack(this, maze); // Enemy attacks mage
                    }
                    else {
                        displayMessage("maged moved away");
                        CodeHero.canDodge=false;
                    }
                }

                break; // Only attack one enemy per ranged attack
            }
        }

        if (!attacked) {
            displayMessage("Move closer to the enemy to perform a ranged attack.");
            CodeHero.canDodge = false;
        }

        CodeHero.updateCharacterHpLabel(); // Update character HP label after the attack
    }

    // Helper method to move enemy 1 square closer towards the mage
    private void moveEnemyTowardsMage(Enemy enemy, Maze maze) {
        int enemyRow = enemy.getRow();
        int enemyCol = enemy.getCol();
        int mageRow = getRow();
        int mageCol = getCol();

        // Calculate new position to move enemy closer to mage
        int newRow = enemyRow + Integer.compare(mageRow, enemyRow);
        int newCol = enemyCol + Integer.compare(mageCol, enemyCol);

        // Move the enemy if the cell is free
        if (maze.canMoveTo(newRow, newCol)) {
            maze.moveEnemy(enemy, newRow, newCol);
        } else {
            // Handle obstacles or other logic as needed if the cell is not free
            displayMessage("Cannot move enemy closer due to obstacle.");
        }
    }

    // Helper method to check if enemy and mage are on adjacent diagonals
    private boolean isOnAdjacentDiagonal(int enemyRow, int enemyCol) {
        int mageRow = getRow();
        int mageCol = getCol();

        // Check if enemy and mage are on adjacent diagonals
        return Math.abs(enemyRow - mageRow) == 1 && Math.abs(enemyCol - mageCol) == 1;
    }

    // Example method to check if mage has moved away from the enemy's adjacent square
    private boolean hasMageMovedAway(Enemy enemy) {
        int mageRow = getRow();
        int mageCol = getCol();
        int enemyRow = enemy.getRow(); // Replace with the actual method to get enemy's row
        int enemyCol = enemy.getCol(); // Replace with the actual method to get enemy's column

        // Check if mage is on adjacent squares of enemy
        boolean onAdjacentSquare = (Math.abs(mageRow - enemyRow) <= 1 && Math.abs(mageCol - enemyCol) <= 1);

        // Mage has moved away if not on adjacent squares
        return !onAdjacentSquare;
    }




    // Helper method to move enemy closer to the mage
    private void moveEnemyCloser(Enemy enemy, Maze maze) {
        // Calculate new position to move enemy closer
        int newRow = enemy.getRow() + (getRow() - enemy.getRow()) / 2;
        int newCol = enemy.getCol() + (getCol() - enemy.getCol()) / 2;

        // Move the enemy if the cell is free
        if (maze.canMoveTo(newRow, newCol)) {
            maze.moveEnemy(enemy, newRow, newCol);
        } else {
            // Handle obstacles or other logic as needed
        }
    }




    @Override
    public void dodgeAndMoveAway(Maze maze) {
        int mageRow = getRow();
        int mageCol = getCol();

        List<Enemy> enemies = maze.getEnemies();

        // Check if mage is on adjacent squares of any enemy
        boolean onAdjacentEnemySquare = false;
        for (Enemy enemy : enemies) {
            int enemyRow = enemy.getRow();
            int enemyCol = enemy.getCol();
            if (Math.abs(mageRow - enemyRow) <= 1 && Math.abs(mageCol - enemyCol) <= 1) {
                onAdjacentEnemySquare = true;
                break;
            }
        }

        if (onAdjacentEnemySquare) {
            // Mage is on adjacent square of enemy, attempt to dodge with 50% chance
            if (Math.random() < 0.5) {
                // Mage successfully dodges
                int[][] directions = {
                        { -1, 0 }, // Up
                        { 1, 0 },  // Down
                        { 0, -1 }, // Left
                        { 0, 1 }   // Right
                };

                for (int[] direction : directions) {
                    int newRow = mageRow + direction[0];
                    int newCol = mageCol + direction[1];

                    if (maze.canMoveTo(newRow, newCol)) {
                        // Move mage internally
                        setRow(newRow);
                        setCol(newCol);
                        // Update maze
                        maze.updateCharacterPosition(mageRow, mageCol, this);
                        displayMessage("Mage dodged and moved away from enemy.");
                        return;
                    }
                }

                // If no valid move was found, stay in place but display a message
                displayMessage("Mage could not move away and remains in place.");
            } else {
                // Mage fails to dodge
                displayMessage("Mage attempted to dodge but failed.");
            }
        } else {
            displayMessage("Mage is not on adjacent square of any enemy.");
        }
    }


}
