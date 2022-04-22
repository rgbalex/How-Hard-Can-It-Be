package com.mygdx.game.UI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.SerializationException;
import com.mygdx.game.Managers.GameManager;
import com.mygdx.game.Managers.ResourceManager;
import com.mygdx.game.PirateGame;

import java.io.InputStream;

import static com.mygdx.utils.Constants.VIEWPORT_HEIGHT;

/**
 * Contains widgets defining the start-of-game menu screen.
 */
public class MenuScreen extends Page {
    public MenuScreen(PirateGame parent) {
        super(parent);
    }

    /**
     * Create menu widgets such as start button, labels, etc.
     */
    @Override
    protected void CreateActors() {
        Table t = new Table();
        t.setFillParent(true);

        float space = VIEWPORT_HEIGHT * 0.125f;

        t.setBackground(new TextureRegionDrawable(ResourceManager.getTexture("menuBG.jpg")));
        Label l = new Label("Pirates the movie the game", parent.skin);
        l.setFontScale(2);
        t.add(l).top().spaceBottom(space * 0.5f);
        t.row();

        TextButton play = new TextButton("Play", parent.skin);
        play.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                parent.setScreen(parent.game);
            }
        });
        t.add(play).top().size(100, 25).spaceBottom(space);
        t.row();

        TextButton loadData = new TextButton("Load Game", parent.skin);
        loadData.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Loading Data...");
//                Loading JSON to working memory
                String userprofile = System.getenv("USERPROFILE");
                String fileLoc = userprofile + "\\saved_data.json";

                JsonReader json = new JsonReader();
                try {
                    JsonValue base = json.parse(Gdx.files.internal(fileLoc));
                    JsonValue factions = base.get("factions");
                    JsonValue ships = base.get("ships");
                    JsonValue colleges = base.get("colleges");
                    JsonValue quests = base.get("quests");
//                Set the loaded flag to true and pass these variables to the game
                    GameManager.load_game(factions, ships, colleges, quests, base);
//                Do all the loading before this point.
                    System.out.println("Load Complete.");
                    parent.setScreen(parent.game);
                } catch (SerializationException e) {
                    System.out.println("The System Cannot find the File Specified.");
                    System.out.println(e);
                    loadData.setText("No Save File");
                }
            }
        });
        t.add(loadData).top().size(100, 25).spaceBottom(space);
        t.row();

        TextButton difficulty = new TextButton("nOOB", parent.skin);
        difficulty.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
//                System.out.println(difficulty.getText());
                if (difficulty.getText().toString().equals("nOOB")) {
                    difficulty.setText("pRO");
                }
                else {
                    difficulty.setText("nOOB");
                }
            }
        });
        t.add(difficulty).top().size(100, 25).spaceBottom(space);
        t.row();

        TextButton quit = new TextButton("Quit", parent.skin);
        quit.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.exit();
                System.exit(0);
            }
        });
        t.add(quit).size(100, 25).top().spaceBottom(space);

        t.top();

        actors.add(t);
    }

    @Override
    public void show() {
        super.show();
    }


    @Override
    public void hide() {
        super.hide();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        Table t = (Table) actors.get(0);
        t.setBackground(new TextureRegionDrawable(ResourceManager.getTexture("menuBG.jpg"))); // prevent the bg being stretched
    }
}
