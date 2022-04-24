package com.mygdx.game.Quests;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Entitys.Player;
import com.mygdx.game.Managers.GameManager;

public class KillDuckQuest extends Quest{
    public KillDuckQuest(){
        super();
        name = "Destroy Longboi!";
        description = "";
        reward = 150;
    }


    @Override
    public boolean checkCompleted(Player p) {
        isCompleted = !GameManager.getLongboi().isAlive();
        return isCompleted;
    }

    public Vector2 getLocation(){return GameManager.getLongboi().getPosition();}
}
