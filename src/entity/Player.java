package entity;

import main.GamePanel;
import main.KeyHandler;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

public class Player extends Entity {

    GamePanel gp;
    KeyHandler keyH;

    public Player(GamePanel gp, KeyHandler keyH) {
        this.gp = gp;
        this.keyH = keyH;

        setDefaultValues();

        getPlayerImage();
    }

    public void setDefaultValues() {
        this.x = 100;
        this.y = 100;
        this.speed = 4;
        this.direction = "down";
    }

    public void getPlayerImage() {

        try {
            this.up1 = ImageIO.read(getClass().getResourceAsStream("/player/player-up1.png"));
            this.up2 = ImageIO.read(getClass().getResourceAsStream("/player/player-up2.png"));
            this.down1 = ImageIO.read(getClass().getResourceAsStream("/player/player-down1.png"));
            this.down2 = ImageIO.read(getClass().getResourceAsStream("/player/player-down2.png"));
            this.left1 = ImageIO.read(getClass().getResourceAsStream("/player/player-left1.png"));
            this.left2 = ImageIO.read(getClass().getResourceAsStream("/player/player-left2.png"));
            this.right1 = ImageIO.read(getClass().getResourceAsStream("/player/player-right1.png"));
            this.right2 = ImageIO.read(getClass().getResourceAsStream("/player/player-right2.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void update() {
        if (this.keyH.upPressed) {
            this.direction = "up";
            this.y -= this.speed;
        } else if (this.keyH.downPressed) {
            this.direction = "down";
            this.y += this.speed;
        } else if (this.keyH.leftPressed) {
            this.direction = "left";
            this.x -= this.speed;
        } else if (this.keyH.rightPressed) {
            this.direction = "right";
            this.x += this.speed;
        }

        spriteCounter++;
        if(spriteCounter>10) {
            if (spriteNum == 1) {
                spriteNum = 2;
            } else if (spriteNum == 2) {
                spriteNum = 1;
            }
            spriteCounter = 0;
        }
    }

    public void draw(Graphics2D g2) {

//        g2.setColor(Color.WHITE);
//        g2.fillRect(this.x, this.y, gp.tileSize, gp.tileSize);
        BufferedImage image = null;
        switch (direction) {
            case "up":
                if(spriteNum == 1) {
                    image = this.up1;
                }
                if(spriteNum == 2) {
                    image = this.up2;
                }
                break;
            case "down":
                if(spriteNum == 1) {
                image = this.down1;
                }
                if(spriteNum == 2) {
                    image = this.down2;
                }
                break;
            case "left":
                if(spriteNum == 1) {
                    image = this.left1;
                }
                if(spriteNum == 2) {
                    image = this.left2;
                }
                break;
            case "right":
                if(spriteNum == 1) {
                    image = this.right1;
                }
                if(spriteNum == 2) {
                    image = this.right2;
                }
                break;
        }

        g2.drawImage(image, this.x, this.y, gp.tileSize, gp.tileSize, null);
    }
}
