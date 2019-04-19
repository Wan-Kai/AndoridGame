package com.mikeriv.ssui_2016.puzzlegame;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.mikeriv.ssui_2016.puzzlegame.model.PuzzleGameBoard;
import com.mikeriv.ssui_2016.puzzlegame.model.PuzzleGameState;
import com.mikeriv.ssui_2016.puzzlegame.model.PuzzleGameTile;
import com.mikeriv.ssui_2016.puzzlegame.util.PuzzleImageUtil;
import com.mikeriv.ssui_2016.puzzlegame.view.PuzzleGameTileView;

import java.util.Random;

public class PuzzleGameActivity extends AppCompatActivity {




    // The default grid size to use for the puzzle game 4 => 4x4 grid
    private static int DEFAULT_PUZZLE_BOARD_SIZE = 4;
    private static int GAME_STEP = 0;
    private static int Score = 0;
    // The id of the image to use for our puzzle game
    private static int TILE_IMAGE_ID = R.drawable.kitty;


    // The size of our puzzle board (mPuzzleBoardSize x mPuzzleBoardSize grid)
    private int mPuzzleBoardSize = DEFAULT_PUZZLE_BOARD_SIZE;

    /**
     * Button Listener that starts a new game - this must be attached to the new game button
     */
    private final View.OnClickListener mNewGameButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // TODO start a new game if a new game button is clicked
            startNewGame();
            GAME_STEP = 0;
            timer.setBase(SystemClock.elapsedRealtime());//计时器清零
            timer.start();
            textView.setText("计步器：" + GAME_STEP);
        }
    };

    private final View.OnClickListener mChangeImage = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(TILE_IMAGE_ID == R.drawable.duck)
                TILE_IMAGE_ID = R.drawable.kitty;
            else
                TILE_IMAGE_ID = R.drawable.duck;

            change();
        }
    };

    private void change(){
        Bitmap fullImageBitmap = BitmapFactory.decodeResource(getResources(), TILE_IMAGE_ID);
        // Now scale the bitmap so it fits out screen dimensions and change aspect ratio (scale) to
        // fit a square
        int fullImageWidth = fullImageBitmap.getWidth();
        int fullImageHeight = fullImageBitmap.getHeight();
        int squareImageSize = (fullImageWidth > fullImageHeight) ? fullImageWidth : fullImageHeight;
        fullImageBitmap = Bitmap.createScaledBitmap(
                fullImageBitmap,
                squareImageSize,
                squareImageSize,
                false);
        int eachTileSize = squareImageSize / mPuzzleBoardSize;

        for(int i=0;i<mPuzzleBoardSize;i++){
            for(int j=0;j<mPuzzleBoardSize;j++){
                Bitmap image = PuzzleImageUtil.getSubdivisionOfBitmap(fullImageBitmap,
                        eachTileSize,eachTileSize,i,j);
                Drawable drawable = new BitmapDrawable(image);
                PuzzleGameTile mPuzzleTile = new PuzzleGameTile(1 + i*mPuzzleBoardSize + j,drawable);
                mPuzzleGameBoard.setTile(mPuzzleTile,i,j);
            }
        }

        int rowsCount = mPuzzleGameBoard.getRowsCount();
        int colsCount = mPuzzleGameBoard.getColumnsCount();

        for(int i=0;i<rowsCount;i++){
            for(int j=0;j<colsCount;j++){
                mPuzzleTileViews[i][j].setId(1 + i*mPuzzleBoardSize + j);
                mPuzzleTileViews[i][j].setOnClickListener(mGameTileOnClickListener);

                TableRow.LayoutParams param = new TableRow.LayoutParams(0,
                        240,1);
                mPuzzleTileViews[i][j].setLayoutParams(param);
                mPuzzleTileViews[i][j].updateWithTile(mPuzzleGameBoard.getTile(i,j));
            }
        }

        refreshGameBoardView();
    }
    /**
     * Click Listener that Handles Tile Swapping when we click on a tile that is
     * neighboring the empty tile - this must be attached to every tileView in the grid
     */
    private final View.OnClickListener mGameTileOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
           // TODO handle swapping tiles and updating the tileViews if there is a valid swap
            // with an empty tile
            // If any changes happen, be sure to update the state of the game to check for a win
            // condition
            for(int i=0;i<mPuzzleBoardSize;i++){
                for(int j=0;j<mPuzzleBoardSize;j++){
                    if(mPuzzleTileViews[i][j].getTileId() == 9){
                        int temp = view.getId();
                        int y = (temp - 1) % mPuzzleBoardSize;
                        int x = (temp - 1) / mPuzzleBoardSize;

                        if(mPuzzleGameBoard.areTilesNeighbors(x,y,i,j)){
                            mPuzzleGameBoard.swapTiles(x,y,i,j);
                            refreshGameBoardView();
                            resetEmptyTileLocation();
                            GAME_STEP++;
                            textView.setText("计步器：" + GAME_STEP);
                        }
                        //Log.d("得到了：：：：：：：：：","" + x + "+" + (y-1));
                    }
                }

            }

            if(hasWonGame()){
                Score++;
                mScoreTextView.setText("Score：" + Score);
            }
        }
    };

    // Game State - what the game is currently doin
    private PuzzleGameState mGameState = PuzzleGameState.NONE;



    // The puzzleboard model
    private PuzzleGameBoard mPuzzleGameBoard;

    // Views
    private TextView mScoreTextView;
    Button button_1 ;
    TableLayout mTable ;
    TextView textView;
    Chronometer timer;
    Button button_change;

    // The views for the puzzleboardtile models
    private PuzzleGameTileView[][] mPuzzleTileViews =
            new PuzzleGameTileView[mPuzzleBoardSize][mPuzzleBoardSize];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzle_game);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mScoreTextView = (TextView) findViewById(R.id.text_score);
        button_1 = (Button) findViewById(R.id.button);

        button_1.setVisibility(View.VISIBLE);
        button_1.setOnClickListener(mNewGameButtonOnClickListener);

        button_change = (Button) findViewById(R.id.button3) ;
        button_change.setVisibility(View.VISIBLE);
        button_change.setOnClickListener(mChangeImage);

        textView = (TextView) findViewById(R.id.textView);

        timer = (Chronometer) findViewById(R.id.timer);

        // TODO initialize references to any containers views or layout groups etc.
        mTable = (TableLayout) findViewById(R.id.TableLayout);
        mTable.setStretchAllColumns(false);
        mTable.setShrinkAllColumns(false);


        //TableRow row = new TableRow(getApplicationContext());


//        for(int i=0;i<mPuzzleBoardSize;i++){
//            TableRow rows = new TableRow(getApplicationContext());
//            for(int j=0;j<mPuzzleBoardSize;j++){
//                rows.addView(mPuzzleTileViews[i][j]);
//            }
//            mTable.addView(rows);
//        }

        // Initializes the game and updates the game state
        initGame();
        updateGameState();


        for(int i=0;i<mPuzzleBoardSize;i++){
            TableRow rows = new TableRow(getApplicationContext());

            for(int j=0;j<mPuzzleBoardSize;j++){


                rows.addView(mPuzzleTileViews[i][j]);
            }
            mTable.addView(rows);
        }

    }


    /**
     * Creates the puzzleboard and the PuzzleGameTiles that serve as the model for the game. It
     * does image slicing to get the appropriate bitmap subdivisions of the TILE_IMAGE_ID. It
     * then creates a set for PuzzleGameTileViews that are used to display the information in models
     */
    private void initGame() {
        mPuzzleGameBoard = new PuzzleGameBoard(mPuzzleBoardSize, mPuzzleBoardSize);

        // Get the original image bitmap
        Bitmap fullImageBitmap = BitmapFactory.decodeResource(getResources(), TILE_IMAGE_ID);
        // Now scale the bitmap so it fits out screen dimensions and change aspect ratio (scale) to
        // fit a square
        int fullImageWidth = fullImageBitmap.getWidth();
        int fullImageHeight = fullImageBitmap.getHeight();
        int squareImageSize = (fullImageWidth > fullImageHeight) ? fullImageWidth : fullImageHeight;
        fullImageBitmap = Bitmap.createScaledBitmap(
                fullImageBitmap,
                squareImageSize,
                squareImageSize,
                false);

        // TODO calculate the appropriate size for each puzzle tile
        int eachTileSize = squareImageSize / mPuzzleBoardSize;

        // TODO create the PuzzleGameTiles for the PuzzleGameBoard using sections of the bitmap.
        // You may find PuzzleImageUtil helpful for getting sections of the bitmap
        // Also ensure the last tile (the bottom right tile) is set to be an "empty" tile
        // (i.e. not filled with an section of the original image)
//
        for(int i=0;i<mPuzzleBoardSize;i++){
           for(int j=0;j<mPuzzleBoardSize;j++){
               Bitmap image = PuzzleImageUtil.getSubdivisionOfBitmap(fullImageBitmap,
                       eachTileSize,eachTileSize,i,j);
               Drawable drawable = new BitmapDrawable(image);
               PuzzleGameTile mPuzzleTile = new PuzzleGameTile(1 + i*mPuzzleBoardSize + j,drawable);
               mPuzzleGameBoard.setTile(mPuzzleTile,i,j);


           }
        }

        // TODO createPuzzleTileViews with the appropriate width, height
        createPuzzleTileViews(eachTileSize, eachTileSize);
//        for(int i=0;i<mPuzzleBoardSize;i++){
//            for(int j=0;j<mPuzzleBoardSize;j++){
//                mPuzzleTileViews[i][j].updateWithTile(mPuzzleGameBoard.getTile(i,j));
//            }
//        }

    }

    /**
     * Creates a set of tile views based on the tileWidth and height
     * @param minTileViewWidth the minium width of the tile
     * @param minTileViewHeight the minimum height of the tile
     */

    private void createPuzzleTileViews(int minTileViewWidth, int minTileViewHeight) {
        int rowsCount = mPuzzleGameBoard.getRowsCount();
        int colsCount = mPuzzleGameBoard.getColumnsCount();
        // TODO Set up TileViews (that will be what the user interacts with)
        // Make sure each tileView gets a click listener for interaction
        // Be sure to set the appropriate LayoutParams so that your tileViews
        // So that they fit your gameboard properly
        for(int i=0;i<rowsCount;i++){
            for(int j=0;j<colsCount;j++){
                mPuzzleTileViews[i][j] = new PuzzleGameTileView(this,1 + i*mPuzzleBoardSize + j,
                        minTileViewWidth,minTileViewHeight);
                mPuzzleTileViews[i][j].setId(1 + i*mPuzzleBoardSize + j);
                mPuzzleTileViews[i][j].setOnClickListener(mGameTileOnClickListener);

//                TableRow.LayoutParams param = (TableRow.LayoutParams) mPuzzleTileViews[i][j].getLayoutParams();
//                param.height = 400/mPuzzleBoardSize;
//                param.weight = 400/mPuzzleBoardSize;
//                mPuzzleTileViews[i][j].setLayoutParams(param);

                //TableRow.LayoutParams param = new TableRow.LayoutParams(0,
                //        ViewGroup.LayoutParams.MATCH_PARENT,1);
                TableRow.LayoutParams param = new TableRow.LayoutParams(0,
                        240,1);
                mPuzzleTileViews[i][j].setLayoutParams(param);
                mPuzzleTileViews[i][j].updateWithTile(mPuzzleGameBoard.getTile(i,j));
            }
        }


    }

    /**
     * Shuffles the puzzle tiles randomly such that tiles may only swap if they are swapping with
     * an empty tile to maintain solvability
     */
    private void shufflePuzzleTiles() {
        // TODO randomly shuffle the tiles such that tiles may only move spots if it is randomly?
        // swapped with a neighboring empty tile

        int i = mPuzzleBoardSize*mPuzzleBoardSize - 1;
        while(i>0){
            int j = (int)Math.floor(Math.random() * i);
            int xi = i % mPuzzleBoardSize;
            int yi = (int)Math.floor(i / mPuzzleBoardSize);
            int xj = j % mPuzzleBoardSize;
            int yj = (int)Math.floor(j / mPuzzleBoardSize);

            mPuzzleGameBoard.swapTiles(xi,yi,xj,yj);
            --i;
        }
    }

    /**
     * Places the empty tile in the lower right corner of the grid
     */
    private void resetEmptyTileLocation() {
        // TODO

        for(int i=0;i<mPuzzleBoardSize;i++){
            for(int j=0;j<mPuzzleBoardSize;j++){
                if(mPuzzleTileViews[i][j].getTileId() == 9){
                    PuzzleGameTile pm = new PuzzleGameTile();
                    ColorDrawable drawable = new ColorDrawable(Color.parseColor("#000000"));
                    pm.setDrawable(drawable);
                    mPuzzleTileViews[i][j].updateWithTile(pm);
                }
            }
        }


    }

    /**
     * Updates the game state by checking if the user has won. Also triggers the tileViews to update
     * their visuals based on the gameboard
     */
    private void updateGameState() {
        // TODO refresh tiles and handle winning the game and updating score

        refreshGameBoardView();

        updateScore();

    }

    private void refreshGameBoardView() {
        // TODO update the PuzzleTileViews with the data stored in the PuzzleGameBoard
        for(int i=0;i<mPuzzleBoardSize;i++){
            for(int j=0;j<mPuzzleBoardSize;j++){
                mPuzzleTileViews[i][j].updateWithTile(mPuzzleGameBoard.getTile(i,j));
                mPuzzleTileViews[i][j].setTileId(mPuzzleGameBoard.getTile(i,j).getOrderIndex());
            }
        }
    }


    /**
     * Checks the game board to see if the tile indices are in proper increasing order
     * @return true if the tiles are in correct order and the game is won
     */
    private boolean hasWonGame() {
        // TODO check if the user has won the game
        for(int i=0;i<mPuzzleBoardSize;i++){
            for(int j=0;j<mPuzzleBoardSize;j++){
                if(mPuzzleTileViews[i][j].getTileId() != 1 + i*mPuzzleBoardSize + j){
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Updates the score displayed in the text view
     */
    private void updateScore() {
        // TODO update a score to be displayed to the user
    }

    /**
     * Begins a new game by shuffling the puzzle tiles, changing the game state to playing
     * and showing a start message
     */
    private void startNewGame() {
        // TODO - handle starting a new game by shuffling the tiles and showing a start message,
        // and updating the game state
        shufflePuzzleTiles();
        updateGameState();
        resetEmptyTileLocation();
    }


}

