import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SudokuSolver extends Application {

    static int[][] board= new int[][]{
            {0,4,0,0,6,0,7,0,0},
            {0,0,0,0,0,0,0,8,3},
            {0,1,0,0,0,3,9,6,0},
            {0,0,6,0,8,0,0,0,9},
            {0,7,0,0,4,1,0,2,0},
            {0,0,0,6,2,0,0,0,8},
            {0,0,0,0,0,0,0,0,0},
            {1,0,7,0,0,5,0,0,0},
            {0,6,2,0,0,0,0,9,1}
    };
    static int iterations = 0;

    @Override
    public void start(Stage stage) throws Exception {


        printBoard(stage);
        System.out.println("Is solvable: " + solve());

        for(int i = 0; i < 9; i++){
            for(int j = 0; j < 9; j++){
                System.out.print(board[i][j] + " ");
            }
            System.out.print("\n");
        }

        System.out.println("iterations: " + iterations);

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.schedule(() -> printBoard(stage), 1, TimeUnit.SECONDS);
    }

    void printBoard(Stage stage){

        //Creating a Grid Pane
        GridPane gridPane = new GridPane();
        gridPane.setMinSize(200, 200);
        gridPane.setPadding(new Insets(10, 10, 10, 10));
        gridPane.setVgap(5);
        gridPane.setHgap(5);
        gridPane.setAlignment(Pos.CENTER);

        for(int i = 0; i<9; i++){
            for(int j=0; j < 9; j++){
                //creating label email
                Text text = new Text(board[j][i] + "");
                gridPane.add(text, i, j);
            }
        }

        Scene scene = new Scene(gridPane);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);

    }

    boolean solve(){
        for(int i = 0; i < 9; i++){
            for(int j = 0; j < 9; j++){
                if(board[j][i]==0){
                    for(int val = 1; val < 10; val++){
                        if(isValidAddition(j, i, val)){
                            board[j][i] = val;
                            iterations++;
                            if(solve()){
                                return true;
                            }
                            else
                                board[j][i] = 0;
                        }
                    }
                    return false;
                }
            }
        }
        return true;
    }

    boolean isValidAddition(int col, int row, int val){

        if(board[col][row]!=0)
            return false;

        for(int i = 0; i < 9; i++) {//checks the horizontal and vertical boxes
            if (board[col][i] == val) {
                return false;
            }
            if(board[i][row] == val){
                return false;
            }
        }

        int boxX = row / 3;//uses integer divide
        int boxY = col / 3;
        int sudokuX = boxX * 3;
        int sudokuY = boxY * 3;//both of these map to the top left corner of the specific box

        for(int i = sudokuX; i <= sudokuX+2; i++){
            for(int j = sudokuY; j <= sudokuY+2; j++){
                if(board[j][i]==val)
                    return false;
            }
        }


        return true;
    }


}