package com.example.codehero;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.util.Random;
import javafx.scene.control.ProgressBar;
import javafx.util.Duration;

// Declare the ProgressBar at the class level



import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class CodeHero extends Application implements Serializable {
    private static final String SAVE_STATE_FILE = "game.ser";
    private static final String CODE_INPUT_FILE = "codeinput.ser";
    private Maze maze;
    private static Character character;
    private static List<Enemy> enemies;
    private HBox controls;
    private Scene gameScene;
    private Scene mainMenuScene;
    public static Label messageLabel;
    private Set<String> executedFunctions; // Set to keep track of executed functions
    public static boolean canBlock;
    public static boolean canDodge;
    private Map<String, Button> functionButtons;
    public static int score;
    private static Label characterHpLabel;
    private static Label enemyHpLabel;
    private static Label scoreLabel;
    private static final String SAVE_FILE = "game_state.ser";
    private double currentVolume = 1;
    private Scene previousScene;
    private MediaPlayer mediaPlayer; // Declare the MediaPlayer for controlling the music
    protected Random random = new Random();
    private static ProgressBar characterHpBar;
    private static ProgressBar enemyHpBar;
    public Stage primaryStage;








    Button submitButton = new Button("Submit Code");

    private TextArea codeInput; // Ensure codeInput is a class field so it's accessible in saveGame and loadGame


    public static void updateMessageLabel(String message) {
        if (messageLabel != null) {
            messageLabel.setText(message);
        }
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("CodeHero");
        //primaryStage.setMaximized(true);
        score = 0;
        canBlock = false;
        canDodge = false;
        // Main menu setup
        VBox mainMenu = new VBox(10);
        mainMenu.setAlignment(Pos.CENTER);
        Text codeHeroText = new Text("Code Hero");
        codeHeroText.setFont(Font.font("Arial", 60));
        Button startGameButton = new Button("Start Game");
        Button resumeGameButton = new Button("Resume Game");
        Button settingsButton = new Button("Settings");
        Button supportButton = new Button("Support");

        startGameButton.setOnAction(e -> showCharacterSelection(primaryStage));
        resumeGameButton.setOnAction(e -> resumeGame(primaryStage));// Resuming to the last state
       // settingsButton.setOnAction(e -> showSettings());
        settingsButton.setOnAction(e -> {
           // primaryStage.setMaximized(true);
            showSettings(primaryStage, primaryStage.getScene());
           // primaryStage.setMaximized(true);
        });
        // Set the desired width
        mainMenu.getChildren().addAll(codeHeroText,startGameButton, resumeGameButton, settingsButton, supportButton);
        mainMenuScene = new Scene(mainMenu, 1100, 600);

        // Show main menu initially
        primaryStage.setScene(mainMenuScene);

        primaryStage.show();
        this.primaryStage = primaryStage;

    }

    private void resumeGame(Stage primaryStage) {
        loadGame(primaryStage);
        primaryStage.setScene(gameScene);
        primaryStage.show();
    }


    private void showCharacterSelection(Stage primaryStage) {
        VBox characterSelection = new VBox(10);
        characterSelection.setAlignment(Pos.CENTER);
        Label chooseCharacterLabel = new Label("Choose your character:");
        HBox characterOptions = new HBox(20);
        characterOptions.setAlignment(Pos.CENTER);

        // Read high scores
        int knightHighScore = getHighScore("knight_scores.txt");
        int mageHighScore = getHighScore("mage_scores.txt");

        // Knight Option
        VBox knightOption = new VBox(5);
        knightOption.setAlignment(Pos.CENTER);
        ImageView knightImageView = new ImageView(new Image("knight.png"));
        knightImageView.setFitWidth(100);
        knightImageView.setFitHeight(100);
        Label knightLabel = new Label("Knight");
        Label knightDescription = new Label("A brave warrior with a strong shield.");
        Label knightHighScoreLabel = new Label("High Score: " + knightHighScore);
        Button knightButton = new Button("Choose Knight");
        knightButton.setOnAction(e -> startGame(primaryStage, "knight"));
        knightOption.getChildren().addAll(knightImageView, knightLabel, knightDescription, knightHighScoreLabel, knightButton);

        // Mage Option
        VBox mageOption = new VBox(5);
        mageOption.setAlignment(Pos.CENTER);
        ImageView mageImageView = new ImageView(new Image("mage.png"));
        mageImageView.setFitWidth(100);
        mageImageView.setFitHeight(100);
        Label mageLabel = new Label("Mage");
        Label mageDescription = new Label("A wise mage with powerful spells.");
        Label mageHighScoreLabel = new Label("High Score: " + mageHighScore);
        Button mageButton = new Button("Choose Mage");
        mageButton.setOnAction(e -> startGame(primaryStage, "mage"));
        mageOption.getChildren().addAll(mageImageView, mageLabel, mageDescription, mageHighScoreLabel, mageButton);

        characterOptions.getChildren().addAll(knightOption, mageOption);
        characterSelection.getChildren().addAll(chooseCharacterLabel, characterOptions);
        Scene characterSelectionScene = new Scene(characterSelection, 1100, 600);
        System.out.println("Starting game with character type: ");

        primaryStage.setScene(characterSelectionScene);
    }

    private int getHighScore(String filename) {
        int highScore = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            List<Integer> scores = reader.lines()
                    .map(line -> line.replaceAll("[^0-9]", ""))
                    .filter(line -> !line.isEmpty())
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
            highScore = scores.stream().max(Comparator.naturalOrder()).orElse(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return highScore;
    }

    private void startGame(Stage primaryStage, String characterType) {
        this.primaryStage = primaryStage; // Store the primary stage reference
        maze = new Maze(10, 10);
        executedFunctions = new HashSet<>(); // Initialize the set
        functionButtons = new HashMap<>();
        playGameMusic();

        if (characterType.equals("knight")) {
            character = new Knight(0, 0);
        } else if (characterType.equals("mage")) {
            character = new Mage(0, 0);
        }

        characterHpBar = new ProgressBar();
        characterHpBar.setProgress(character.getHp() / (characterType.equals("knight") ? 150.0 : 100.0)); // Adjust for character type
        characterHpBar.setPrefWidth(200);
        characterHpBar.setStyle(
                "-fx-accent: #7CFC00;" + // Set bar color
                        "-fx-background-color: linear-gradient(to right, #76c7c0, #63a4ff);" // Set gradient
        );

        GridPane topPanel = new GridPane();
        topPanel.setPadding(new Insets(10));
        topPanel.setHgap(10);
        topPanel.add(characterHpBar, 1, 0);

        Set<Position> occupiedPositions = new HashSet<>();
        occupiedPositions.add(new Position(0, 0)); // Character's starting position

        enemies = new ArrayList<>();
        for (int i = 0; i < 4; i++) { // Add as many enemies as needed
            Position pos = getRandomPosition(occupiedPositions);
            enemies.add(new Enemy(pos.row, pos.col));
            occupiedPositions.add(pos);
        }

        enemyHpBar = new ProgressBar(); // Health bar for the currently attacked enemy
        enemyHpBar.setProgress(150 / 150.0);
        enemyHpBar.setStyle(
                "-fx-accent: #E90000;" + // Set bar color
                        "-fx-background-color: linear-gradient(to right, #76c7c0, #63a4ff);" // Set gradient
        );

        List<Rock> rocks = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Position pos;
            do {
                pos = getRandomPosition(occupiedPositions);
            } while (pos.row == 0 && pos.col == 1 || pos.row == 1 && pos.col == 0);
            rocks.add(new Rock(pos.row, pos.col));
            occupiedPositions.add(pos);
        }

        List<Tree> trees = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Position pos;
            do {
                pos = getRandomPosition(occupiedPositions);
            } while (pos.row == 0 && pos.col == 1 || pos.row == 1 && pos.col == 0);
            trees.add(new Tree(pos.row, pos.col));
            occupiedPositions.add(pos);
        }

        List<Potion> potions = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Position pos = getRandomPosition(occupiedPositions);
            potions.add(new Potion(pos.row, pos.col));
            occupiedPositions.add(pos);
        }

        maze.placeCharacter(character);
        maze.placeEnemies(enemies);
        maze.placeRocks(rocks);
        maze.placeTrees(trees);
        maze.placePotions(potions);

        BorderPane root = new BorderPane();
        root.setCenter(maze);

        VBox rightPanel = new VBox(10);
        rightPanel.setAlignment(Pos.TOP_CENTER);

        characterHpLabel = new Label("Character HP: " + character.getHp());
        enemyHpLabel = new Label("Enemy HP: " + (enemies.isEmpty() ? "N/A" : enemies.get(0).getHp()));
        scoreLabel = new Label("Score: " + score);

        codeInput = new TextArea();
        codeInput.setPrefSize(200, 300);

        Label errorLabel = new Label();
        messageLabel = new Label();

        Button submitButton = new Button("Submit");
        submitButton.setOnAction(e -> {
            String code = codeInput.getText();
            try {
                validateSyntax(code); // Validate syntax before creating buttons
                parseCodeAndCreateButtons(code);
                errorLabel.setText("");
            } catch (Exception ex) {
                errorLabel.setText("Error: " + ex.getMessage());
            }
        });

        controls = new HBox(10);
        controls.setAlignment(Pos.CENTER);
        controls.getChildren().addAll(submitButton, errorLabel);

        boolean blockFunctionDefined = isBlockFunctionDefined(codeInput.getText());
        if (blockFunctionDefined) {
            Button blockButton = new Button("Block");
            blockButton.setOnAction(e -> character.block());
            controls.getChildren().add(blockButton);
        }

        topPanel.add(characterHpLabel, 1, 2);
        rightPanel.getChildren().addAll(scoreLabel, codeInput, controls, messageLabel);

        HBox bottomControls = new HBox(10);
        bottomControls.setAlignment(Pos.CENTER);

        Button settingsButton = new Button("Settings");
        settingsButton.setOnAction(e -> showSettings(primaryStage, primaryStage.getScene()));

        Button quitButton = new Button("Quit");
        quitButton.setOnAction(e -> {
            stopGameMusic();
            primaryStage.setScene(mainMenuScene);
        });

        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> saveGame());

        topPanel.add(enemyHpBar, 20, 0); // Adjust column as needed
        topPanel.add(enemyHpLabel, 20, 2);

        bottomControls.getChildren().addAll(saveButton, quitButton, settingsButton);

        VBox rightPanelWrapper = new VBox(10);
        rightPanelWrapper.getChildren().addAll(rightPanel, bottomControls);
        root.setTop(topPanel);
        root.setRight(rightPanelWrapper);

        BorderPane centeredLayout = new BorderPane();
        centeredLayout.setCenter(root);
        centeredLayout.setPadding(new Insets(20));
        BorderPane.setAlignment(root, Pos.CENTER);
        System.out.println("Starting game with character type: ");

        Scene gameScene = new Scene(centeredLayout, 1100, 700);
        primaryStage.setScene(gameScene);
    }

    // Helper method to get a random position that is not occupied
    private Position getRandomPosition(Set<Position> occupiedPositions) {
        List<Position> allPositions = new ArrayList<>();

        // Generate all possible positions in the 10x10 grid
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                Position pos = new Position(row, col);
                if (!isPositionAdjacentOrOccupied(pos, occupiedPositions)) {
                    allPositions.add(pos);
                }
            }
        }

        // Shuffle the list to randomize positions
        Collections.shuffle(allPositions);

        // Check if there is any valid position available
        if (allPositions.isEmpty()) {
            throw new RuntimeException("Failed to find a valid position");
        }

        // Return the first valid position
        return allPositions.get(0);
    }



    // Helper method to check if a position is adjacent or occupied
    private boolean isPositionAdjacentOrOccupied(Position pos, Set<Position> occupiedPositions) {
        for (Position occupied : occupiedPositions) {
            if (Math.abs(occupied.row - pos.row) <= 1 && Math.abs(occupied.col - pos.col) <= 1) {
                return true;
            }
        }
        return false;
    }

    // Simple Position class to hold row and column
    private static class Position {
        int row, col;

        Position(int row, int col) {
            this.row = row;
            this.col = col;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Position position = (Position) o;
            return row == position.row && col == position.col;
        }

        @Override
        public int hashCode() {
            return Objects.hash(row, col);
        }
    }
    private boolean isBlockFunctionDefined(String code) {
        String[] lines = code.split("\n");
        for (String line : lines) {
            if (line.trim().startsWith("void block")) {
                return true;
            }
        }
        return false;
    }

    private void validateSyntax(String code) throws Exception {
        // Basic validation: check if there are lines starting with "void"
        String[] lines = code.split("\n");
        boolean validSyntax = false;
        for (String line : lines) {
            if (line.trim().startsWith("void ")) {
                validSyntax = true;
                break;
            }
        }
        if (!validSyntax) {
            throw new Exception("Syntax Error: No valid functions (lines starting with 'void') found.");
        }
        // Additional checks can be added based on your syntax requirements
    }

    private void parseCodeAndCreateButtons(String code) throws Exception {
        Set<String> currentFunctions = new HashSet<>();
        String[] lines = code.split("\n");

        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("void ")) {
                String functionName = line.substring(5, line.indexOf("(")).trim();
                currentFunctions.add(functionName);
                if (!functionButtons.containsKey(functionName)) {
                    Button functionButton = new Button(functionName.substring(0, 1).toUpperCase() + functionName.substring(1));
                    functionButton.setOnAction(e -> {
                        try {
                            executeFunction(functionName);
                        } catch (Exception ex) {
                            System.out.println("Error executing function: " + ex.getMessage());
                        }
                    });
                    controls.getChildren().add(controls.getChildren().size(), functionButton);
                    functionButtons.put(functionName, functionButton);
                }
            }
        }

        // Remove buttons for functions that no longer exist
        for (String functionName : functionButtons.keySet()) {
            if (!currentFunctions.contains(functionName)) {
                Button button = functionButtons.get(functionName);
                controls.getChildren().remove(button);
                functionButtons.remove(functionName);
            }
        }
    }

    private boolean hasFunctionDefinitions(String code) {
        String[] lines = code.split("\n");
        for (String line : lines) {
            if (line.trim().startsWith("void ")) {
                return true;
            }
        }
        return false;
    }
    private void executeFunction(String functionName) throws Exception {
        int oldRow = character.getRow();
        int oldCol = character.getCol();
        if (character.isGameOver() || areAllEnemiesDefeated(enemies)) {
            if(character.isGameOver()){
                score = 0;
            }
            storeScore();
            updateMessageLabel("Game Over!");

            // Create a PauseTransition for 2 seconds
            PauseTransition pause = new PauseTransition(Duration.seconds(1));
            pause.setOnFinished(event -> {
                // Code to execute after the pause
               // startGame(); // Or any other action you want to take after the pause
                resetGame();
            });
            pause.play();

            return;
        }
        switch (functionName) {
            case "up":
                character.moveUp();
                break;
            case "down":
                character.moveDown();
                break;
            case "left":
                character.moveLeft();
                break;
            case "right":
                character.moveRight();
                break;
            case "attack":
                if (!canBlock) {
                    canBlock = true;
                    if (character instanceof Knight) {
                        ((Knight) character).attack(enemies, maze);
                    } else if (character instanceof Mage) {
                        updateMessageLabel("mage doesnt perform melee attack");
                        canBlock=false;
                    }
                   // System.out.println(canBlock);
                    updateEnemyHpLabel(); // Update enemy HP after an attack
                    updateScore();
                } else {
                    updateMessageLabel("Block the attack");
                }
                break;
            case "rangedAttack":
                if (!canDodge) {
                    canDodge = true;
                    if (character instanceof Mage) {
                        ((Mage) character).rangedAttack(enemies, maze);
                    } else {
                        throw new Exception("Character type does not support ranged attack.");
                    }
                    updateEnemyHpLabel(); // Update enemy HP after a ranged attack
                    updateScore();
                } else {
                    updateMessageLabel("dodge the attack");
                }
                break;
            case "block":
                if(character.getHp()<0)
                {
                    character.setHp(0);
                }
                if (character instanceof Knight) {
                    characterHpBar.setProgress(character.getHp() / 100.0); // Assuming max health is 100

                    if (canBlock) {

                        ((Knight) character).block();
                        canBlock = false;
                    } else {
                        updateMessageLabel("Attack the enemy first then it will block when enemy attacks");
                    }
                } else if (character instanceof Mage) {
                    updateMessageLabel("mage doesnt perform block");
                    canBlock=false;
                }


                break;
            case "dodge":
                if(canDodge) {
                    if (character instanceof Mage) {
                        characterHpBar.setProgress(character.getHp() / 100.0); // Assuming max health is 100

                            ((Mage) character).dodgeAndMoveAway(maze);
                            
                        canDodge= false;
                    } else {
                        throw new Exception("Character type does not support dodge attack.");
                    }
                }
                else {
                    updateMessageLabel("Attack the enemy first then it will block when enemy attacks");
                }
                break;
            default:
                updateMessageLabel("Function '" + functionName + "' not recognized.");
                throw new Exception("Function '" + functionName + "' not recognized.");
        }

        // Check if the new position is valid and handle potion collection
        if (!maze.canMoveTo(character.getRow(), character.getCol())) {
            character.setRow(oldRow);
            character.setCol(oldCol);
        } else if (maze.collectPotion(character.getRow(), character.getCol())) {
            // Handle potion collection only if character's health is below 50
            if (character.getHp() < 50) {
                character.setHp(character.getHp() + 50); // Example: increase HP by 50
                updateCharacterHpLabel(); // Update character HP after collecting a potion
                updateMessageLabel("Health increased");
            } else {
                updateMessageLabel("Character health is above 50");
            }
        }


        // Update character position in the maze
        maze.updateCharacterPosition(oldRow, oldCol, character);
    }
    private void resetGame() {
        Platform.runLater(() -> {
            stopGameMusic();
            maze = null;
            enemies.clear();
            //score = 0;
            String currentCharacterType;

            if (character instanceof Mage){
                currentCharacterType = "mage";
            }else{
                currentCharacterType = "knight";

            }

            character = null;
            executedFunctions.clear();
            functionButtons.clear();

            startGame(primaryStage, currentCharacterType);
        });
    }

    private boolean areAllEnemiesDefeated(List<Enemy> enemies) {
        for (Enemy enemy : enemies) {
            if (!enemy.isDead()) {
                return false;
            }
        }
        return true;
    }
    private void storeScore() {
        String filename = character instanceof Knight ? "knight_scores.txt" : "mage_scores.txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true))) {
            writer.write("Score: " + score + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void updateCharacterHpLabel() {
        characterHpLabel.setText("Character HP: " + character.getHp());
    }
    public static void updateScore() {
        scoreLabel.setText("Score: " + score);
    }

    public static void updateEnemyHpLabel() {
        StringBuilder sb = new StringBuilder();
        for (Enemy enemy : enemies) {
            if (Math.abs(enemy.getRow() - character.getRow()) <= 1 && Math.abs(enemy.getCol() - character.getCol()) <= 1 &&
                    !(enemy.getRow() == character.getRow() && enemy.getCol() == character.getCol())) {

                enemyHpBar.setProgress(enemy.getHp() / 100.0);

                // Assuming max enemy health is 100
                sb.append("Enemy HP: ").append(enemy.getHp()).append("\n");
            }

        }
        enemyHpLabel.setText(sb.toString().trim());
    }

    private void showSettings() {
        // Show settings screen (not implemented)
    }


    private void saveGame() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SAVE_STATE_FILE));
             FileWriter writer = new FileWriter(CODE_INPUT_FILE);
             FileOutputStream buttonsFile = new FileOutputStream("buttons.ser");
             ObjectOutputStream oosButtons = new ObjectOutputStream(buttonsFile)) {

            // Save game state
            oos.writeObject(character);
            oos.writeObject(enemies);
            oos.writeObject(maze);
            updateMessageLabel("Game state saved successfully.");

            // Save code input
            writer.write(codeInput.getText());
            writer.flush(); // Ensure all data is written

            // Save dynamically created buttons information
            List<ButtonInfo> buttonInfos = new ArrayList<>();
            for (String executedFunction : executedFunctions) {
                ButtonInfo buttonInfo = new ButtonInfo(executedFunction);
                buttonInfos.add(buttonInfo);
            }
            oosButtons.writeObject(buttonInfos);
        } catch (IOException e) {
            e.printStackTrace();
            updateMessageLabel("Error saving game state.");
        }
    }


    private void loadGame(Stage primaryStage) {
        playGameMusic();
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(SAVE_STATE_FILE));
             BufferedReader reader = new BufferedReader(new FileReader(CODE_INPUT_FILE));
             FileInputStream buttonsFile = new FileInputStream("buttons.ser");
             ObjectInputStream oisButtons = new ObjectInputStream(buttonsFile)) {

            if (codeInput == null) {
                codeInput = new TextArea();
                codeInput.setPrefSize(200, 300);
            }
            if(functionButtons == null)
            {
                functionButtons = new HashMap<>();


            }
            functionButtons = new HashMap<>();

            if (executedFunctions == null) {
                executedFunctions = new HashSet<>();
            }
            if (controls == null) {
                controls = new HBox(); // Initialize controls if null
            }

            // Load game state
            character = (Character) in.readObject();
            enemies = (List<Enemy>) in.readObject();
            maze = (Maze) in.readObject();
            updateMessageLabel("Game state loaded successfully.");

            // Initialize enemies if they are null
            if (enemies == null) {
                enemies = new ArrayList<>();
            }

            // Reinitialize transient fields
            characterHpBar = new ProgressBar();


            character.reinitializeTransientFields();
            maze.reinitializeTransientFields();
            GridPane topPanel = new GridPane();
            topPanel.setPadding(new Insets(10));
            topPanel.setHgap(10);
            // topPanel.add(new Label("Character"), 0, 0);
            topPanel.add(characterHpBar, 1, 0);

            // Load code input
            StringBuilder code = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                code.append(line).append("\n");
            }
            codeInput.setText(code.toString());

            // Clear existing buttons before recreating them
            controls.getChildren().clear();

            // Setup GUI components
            BorderPane root = new BorderPane();
            root.setCenter(maze);

            VBox rightPanel = new VBox(10);
            rightPanel.setAlignment(Pos.TOP_CENTER);

            // Adding padding to the right panel
            rightPanel.setPadding(new Insets(20, 20, 20, 20)); // top, right, bottom, left

            characterHpLabel = new Label("Character HP: " + character.getHp());
            enemyHpLabel = new Label("Enemy HP: " + (enemies.isEmpty() ? "N/A" : enemies.get(0).getHp()));
            scoreLabel = new Label("Score: " + score);
            if (character instanceof Mage)
            characterHpBar.setProgress(character.getHp() / 100.0); // Assuming max health is 100
            else
                characterHpBar.setProgress(character.getHp() / 150.0); // Assuming max health is 100


            characterHpBar.setStyle(
                    "-fx-accent: #7CFC00;" + // Set bar color
                            "-fx-background-color: linear-gradient(to right, #76c7c0, #63a4ff);" // Set gradient
            );

            //E90000
            enemyHpBar = new ProgressBar(); // Health bar for the currently attacked enemy
            topPanel.add(enemyHpBar, 20, 0); // Adjust column as needed
            enemyHpBar.setProgress(100 / 100.0); // Assuming max health is 100
            topPanel.add(characterHpLabel, 1, 2);
            enemyHpBar.setStyle(
                    "-fx-accent: #E90000;" + // Set bar color
                            "-fx-background-color: linear-gradient(to right, #76c7c0, #63a4ff);" // Set gradient
            );
            topPanel.add(enemyHpLabel, 20, 2);

            codeInput.setPrefSize(200, 300);

            Button submitButton = new Button("Submit Code");
            Label errorLabel = new Label();
            messageLabel = new Label();
            Button settingsButton = new Button("Settings");


            submitButton.setOnAction(e -> {
                String codeInputString = codeInput.getText();
                try {
                    validateSyntax(codeInputString);
                    parseCodeAndCreateButtons(codeInputString);
                    errorLabel.setText("");
                } catch (Exception ex) {
                    errorLabel.setText("Error: " + ex.getMessage());
                }
            });
            settingsButton.setOnAction(e -> {
                // primaryStage.setMaximized(true);
                showSettings(primaryStage, primaryStage.getScene());
                // primaryStage.setMaximized(true);
            });
            controls = new HBox(10);
            controls.setAlignment(Pos.CENTER);
            controls.getChildren().addAll(submitButton, errorLabel);

            rightPanel.getChildren().addAll(scoreLabel, codeInput, controls, messageLabel);

            // Create a separate VBox for the controls and quit button
            VBox controlPanel = new VBox(10);
            controlPanel.setAlignment(Pos.CENTER);
            controlPanel.getChildren().addAll(rightPanel);

            // Adding Quit button at the bottom
            Button quitButton = new Button("Quit");
            quitButton.setOnAction(e -> {
                stopGameMusic();
                //saveGame();
                primaryStage.setScene(mainMenuScene);
            });

            // Adding Save button
            Button saveButton = new Button("Save");
            saveButton.setOnAction(e -> {
                saveGame();
            });

            // Adding Load button
            controlPanel.getChildren().addAll(saveButton, quitButton,settingsButton);

            // Load dynamically created buttons
            List<ButtonInfo> buttonInfos = (List<ButtonInfo>) oisButtons.readObject();
            for (ButtonInfo buttonInfo : buttonInfos) {
                Button functionButton = new Button(buttonInfo.getButtonText());
                functionButton.setOnAction(e -> {
                    try {
                        executeFunction(buttonInfo.getFunctionName());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        updateMessageLabel("Error executing function: " + buttonInfo.getFunctionName());
                    }
                });
                controls.getChildren().add(functionButton);
            }

            // Parse and create buttons from loaded code
            parseCodeAndCreateButtons(code.toString());
            root.setTop(topPanel);

            root.setRight(controlPanel);

            gameScene = new Scene(root, 1100, 600);
            primaryStage.setScene(gameScene);

        } catch (Exception e) {
            e.printStackTrace();
            updateMessageLabel("Error loading game state.");
        }
    }




    private void parseCodeAndExecuteFunctions(String code) {
        String[] lines = code.split("\n");

        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("void ")) {
                String functionName = line.substring(5, line.indexOf("(")).trim();
                if (!executedFunctions.contains(functionName)) {
                    executedFunctions.add(functionName); // Mark function as executed
                    try {
                        executeFunction(functionName);
                    } catch (Exception e) {
                        e.printStackTrace();
                        updateMessageLabel("Error executing function: " + functionName);
                    }
                }
            }
        }
    }
    private void showSettings(Stage primaryStage, Scene previousScene) {
        // Store the previous scene
        this.previousScene = previousScene;



        VBox settingsMenu = new VBox(40);
        settingsMenu.setPadding(new Insets(10));

        Label settingsLabel = new Label("Settings Menu");
        settingsLabel.setFont(new Font(24));
        HBox settingsLabelBox = new HBox(settingsLabel);
        settingsLabelBox.setAlignment(Pos.CENTER);

        Button backButton = new Button("Back");
        HBox backButtonBox = new HBox(backButton);
        backButtonBox.setAlignment(Pos.CENTER);

        Slider volumeSlider = new Slider(0, 100, currentVolume * 100); // Use current volume value
        volumeSlider.setShowTickMarks(true);
        volumeSlider.setMajorTickUnit(25);
        volumeSlider.setMinorTickCount(5);
        volumeSlider.setBlockIncrement(10);
        Label volumeLabel = new Label("Music Volume");
        volumeLabel.setFont(new Font(18));

        Button customerButton = new Button("Contact Support");
        HBox customerButtonBox = new HBox(customerButton);
        customerButtonBox.setAlignment(Pos.CENTER);

        settingsMenu.getChildren().addAll(settingsLabelBox, volumeLabel, volumeSlider, customerButtonBox, backButtonBox);
        customerButton.setOnAction(e -> {
            try {
                String url = "mailto:george.brandas02@e-uvt.ro";
                java.awt.Desktop.getDesktop().mail(new java.net.URI(url));
            } catch (Exception ex) {
                ex.printStackTrace();

            }
        });

        // Add event listener to brightness slider to update brightness globally
//        brightnessSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
//            brightnessValue = newValue.doubleValue() / 100.0;
//            // Update brightness for all scenes
//            updateBrightnessForAllScenes();
//        });

        // Add event listener to volume slider to update music volume
        volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            currentVolume = newValue.doubleValue() / 100.0; // Update current volume
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(currentVolume); // Set the volume of the media player
            }
        });



        // Handle back button action
        backButton.setOnAction(e -> {
            // Set the previous scene when back button is clicked
            if (this.previousScene != null) {
                primaryStage.setScene(this.previousScene);
               // primaryStage.setMaximized(true);
            } else {
                primaryStage.setScene(mainMenuScene);
            }
        });

        // Create the settings scene and set it to the primary stage
        Scene settingsScene = new Scene(settingsMenu,1100,600);
        primaryStage.setScene(settingsScene);
        primaryStage.show();
       // primaryStage.setMaximized(true);
    }
    private void playGameMusic() {
        String musicFile = "src/main/resources/game_music.mp3"; // Change this to the path of your music file
        Media sound = new Media(new File(musicFile).toURI().toString());
        mediaPlayer = new MediaPlayer(sound);
        mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE); // Repeat indefinitely
        mediaPlayer.play();
    }
    private void stopGameMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.stop(); // Stop the media player
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}