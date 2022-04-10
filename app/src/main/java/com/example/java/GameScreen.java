package com.example.java;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class GameScreen extends Activity {
    private int health; //Player health
    private int money;
    private List<TowerInterface> towerArray; //Arraylist of player towers
    private List<Enemy> enemyArray;
    private int currentTower; //Number of tower player wishes to place
    private int enemyPlaced; //Stagger enemy spawns

    @SuppressLint("SetTextI18n")
    private void updateHealth(TextView healthCounter) {
        healthCounter.setText("Health: " + health);
    }

    public int getHealth() {
        return this.health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public List<Enemy> getEnemyArray() {
        return enemyArray;
    }

    public void setEnemyPlaced(int delay) {
        enemyPlaced = delay;
    }

    @SuppressLint("SetTextI18n")
    private void updateMoney(TextView moneyCounter) {
        moneyCounter.setText("Money: " + money);
    }

    @SuppressLint("SetTextI18n")
    private int getPathCheck(int x, int y) {
        int pathCheck = 0;
        if (x == 0 && y == 300) {
            pathCheck = 1;
        } else if (y == 150 && ((x >= 600 && x <= 1050) || (x >= 1350 && x <= 1650))) {
            pathCheck = 1;
        } else if ((y >= 300 && y<=450) && (x == 150 || x == 600 || x == 1050
                || x == 1350 ||x == 1650)) {
            pathCheck = 1;
        } else if (y == 600 && ((x >= 150 && x <= 600) || (x >= 1050 && x <= 1350))) {
            pathCheck = 1;
        }
        return pathCheck;
    }

    @SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_screen);
        towerArray = new ArrayList<>();
        enemyArray = new ArrayList<>();
        currentTower = 0;
        enemyPlaced = 2;
        TextView healthCounter = findViewById(R.id.healthCounter); //Initializes health display
        TextView moneyCounter = findViewById(R.id.moneyCounter);
        TextView tower1cost = findViewById(R.id.Tower1Cost);
        TextView tower2cost = findViewById(R.id.Tower2Cost);
        TextView tower3cost = findViewById(R.id.Tower3Cost);
        ImageButton tower1button = findViewById(R.id.tower1button);
        ImageButton tower2button = findViewById(R.id.tower2button);
        ImageButton tower3button = findViewById(R.id.tower3button);
        Button waveButton = findViewById(R.id.waveButton);
        GameCanvas towermap = findViewById(R.id.gamecanvas); //Draws towers and processes clicks
        Bundle extras = getIntent().getExtras(); //Pulls all variables passed from config screen
        if (extras == null) {
            extras = new Bundle();
            extras.putInt("diff", 1);
        }
        int diff = extras.getInt("diff"); // Pulls difficulty from config screen
        initValues(diff);
        tower1cost.setText("Price: $" + Tower1.initCost(diff));
        tower2cost.setText("Price: $" + Tower2.initCost(diff));
        tower3cost.setText("Price: $" + Tower3.initCost(diff));
        updateHealth(healthCounter); //sets health display to starting health
        updateMoney(moneyCounter);
        tower1button.setOnClickListener(l -> {
            currentTower = 1; //Tower 1 now placeable
        });
        tower2button.setOnClickListener(l -> {
            currentTower = 2; //Tower 2 now placeable
        });
        tower3button.setOnClickListener(l -> {
            currentTower = 3; //Tower 3 now placeable
        });
        towermap.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) { //When play area is clicked
                    int x = (int) event.getX(); //Get x value of click
                    x = (x - (x % 150));  // Move x value to top-left corner of nearest box
                    int y = (int) event.getY(); //Get y value of click
                    y = y - (y % 150); // Move y value to top-left corner of nearest box
                    for (int i = 0; i < towerArray.size(); i++) {
                        if (x == towerArray.get(i).getxLoc()
                                && y == towerArray.get(i).getyLoc()) {
                            currentTower = 0; //If tower exists in box, do not place tower
                        }
                    }

                    int pathCheck = getPathCheck(x, y);
                    if (pathCheck == 0) {
                        if (money >= TowerFactory.getTowerCost(currentTower, diff)) {
                            money -= TowerFactory.getTowerCost(currentTower, diff);
                            updateMoney(moneyCounter);
                            towermap.addTower(x, y, 1);
                            TowerInterface newTower = TowerFactory.getTower(currentTower, x, y);
                            towerArray.add(newTower);
                        }
                    }
                }
                return true;
            }
        });

        waveButton.setOnClickListener(l -> {
            waveButton.setEnabled(false);
            waveButton.setBackgroundColor(Color.RED);
            waveButton.setText("Wave 1");
            enemyArray.add(new Enemy1());
            enemyWave(towermap, healthCounter, moneyCounter);
        });
        waveButton.setEnabled(true);
    }
    public void gameOver() {
        Intent i = new Intent(this, GameOverScreen.class);
        startActivity(i);
        finish();
    }
    public void initValues(int diff) {
        switch (diff) { //initializes game parameters based on difficulty parameter
        case 0:
            health = 150; //Easy health = 150
            money = 200;
            break;
        case 1:
            health = 100; //Normal health = 100
            money = 150;
            break;
        case 2:
            health = 50; //Hard health = 50
            money = 100;
            break;
        default:
            throw new IllegalStateException("Unexpected value: " + diff);
        }
    }
    public void enemyWave(GameCanvas towermap, TextView healthCounter, TextView moneyCounter) {
        towermap.setEnemyArray(enemyArray);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                enemyArray = towermap.getEnemyArray();
                for (int i = 0; i < enemyArray.size(); i++) {
                    if (enemyArray.get(i).getxLoc() > 1800) {
                        Enemy enemy = enemyArray.remove(i);
                        attack(enemy);
                        updateHealth(healthCounter);
                        towermap.setEnemyArray(enemyArray);
                    }
                }
                for (int i = 0; i < towerArray.size(); i++) {
                    towerArray.get(i).attack(enemyArray);
                }
                removeDeadEnemies();
                updateMoney(moneyCounter);
                addEnemy();
                towermap.setEnemyArray(enemyArray);
                handler.postDelayed(this, 1000);
                if (health <= 0) {
                    handler.removeCallbacks(this);
                    gameOver();
                }
            }
        }, 1000);
    }
    public void attack(Enemy enemy) {
        health = Math.max(0, health - enemy.getDamage());
    }

    private void removeDeadEnemies() {
        for (int i = 0; i < enemyArray.size(); i++) {
            if (enemyArray.get(i).getHealth() <= 0) {
                money += enemyArray.get(i).getMoney();
                enemyArray.remove(i);
            }
        }
    }

    public void addEnemy() {
        if (enemyPlaced == 0) {
            if (health % 2 == 0) {
                enemyArray.add(new Enemy2());
            } else {
                enemyArray.add(new Enemy3());
            }
            enemyPlaced = 2;
        } else {
            enemyPlaced--;
        }
    }
}