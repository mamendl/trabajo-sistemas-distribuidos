package entity;

import main.GamePanel;
import main.KeyHandler;

import java.awt.*;

public class Player extends Entity{

    GamePanel gp;
    KeyHandler keyH;

    public Player(GamePanel gp, KeyHandler keyH) {
        this.gp = gp;
        this.keyH = keyH;
    }

    public void setDefaultValues() {
        this.x = 100;
        this.y = 100;
        this.speed = 4;
    }

    public void update() {
        if (this.keyH.rightPressed) {
            this.y -= this.speed;
        } else if(this.keyH.rightPressed) {
            this.y += this.speed;
        } else if (this.keyH.rightPressed) {
            this.x -= this.speed;
        } else if(this.keyH.rightPressed) {
            this.x += this.speed;
        }
    }

    public void draw(Graphics2D g2) {

        g2.setColor(Color.WHITE);

        //g2.fillRect(this.x, this.y, titleSize, titleSize);

    }
}
