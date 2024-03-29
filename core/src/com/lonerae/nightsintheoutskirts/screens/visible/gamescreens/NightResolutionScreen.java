package com.lonerae.nightsintheoutskirts.screens.visible.gamescreens;

import static java.lang.Thread.sleep;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.lonerae.nightsintheoutskirts.game.Player;
import com.lonerae.nightsintheoutskirts.game.roles.Role;
import com.lonerae.nightsintheoutskirts.game.roles.RoleName;
import com.lonerae.nightsintheoutskirts.network.MatchClient;
import com.lonerae.nightsintheoutskirts.network.requests.ProceedRequest;
import com.lonerae.nightsintheoutskirts.network.requests.ProceedType;
import com.lonerae.nightsintheoutskirts.screens.BaseScreen;
import com.lonerae.nightsintheoutskirts.screens.UIUtil;
import com.lonerae.nightsintheoutskirts.screens.customUI.CustomLabel;
import com.lonerae.nightsintheoutskirts.screens.customUI.CustomScrollPane;
import com.lonerae.nightsintheoutskirts.screens.customUI.CustomTable;
import com.lonerae.nightsintheoutskirts.screens.customUI.CustomTextButton;

import java.util.List;

public class NightResolutionScreen extends BaseScreen {

    private static boolean fourthCheck = true;

    public NightResolutionScreen(Game game) {
        super(game);
    }

    @Override
    public void show() {
        super.show();

        Table mainTable = new CustomTable(true);

        Label title = new CustomLabel("Night Resolution", getTitleStyle());
        UIUtil.title(title);
        mainTable.add(title).padBottom(PAD_VERTICAL_SMALL).row();

        Label description = new CustomLabel(getGameStrings().get("nightResolution"), getBlackStyle());
        mainTable.add(description).width(DEFAULT_ACTOR_WIDTH).padBottom(PAD_VERTICAL_BIG).row();

        Label countdownLabel = new CustomLabel("3", getBlackStyle());
        Table murderedTable = new Table(getSkin());
        waitToFillMurderedTable(murderedTable);

        new Thread(() -> {
            try {
                mainTable.add(countdownLabel).row();
                countdownLabel.setAlignment(Align.center);
                sleep(1500);
                countdownLabel.setText("2");
                sleep(1500);
                countdownLabel.setText("1");
                sleep(1500);
                mainTable.removeActor(countdownLabel);
            } catch (InterruptedException ignored) {
            }
            Gdx.app.postRunnable(() -> {
                mainTable.add(murderedTable).padBottom(PAD_VERTICAL_BIG).row();
                fourthCivilianCheck();
                if (Player.getPlayer().isAlive()) {
                    addContinueButton(mainTable);
                } else {
                    waitForAlivePlayers(new DayScreen(getGame()));
                }
            });
        }).start();

        ScrollPane scroll = new CustomScrollPane(mainTable, true);
        getStage().addActor(scroll);
    }

    private void fourthCivilianCheck() {
        if (Player.getPlayer().getRole().getName().equals(RoleName.FOURTH_CIVILIAN) &&
            !Player.getPlayer().isAbleToUseAbility() &&
            Player.getPlayer().isAlive() &&
            fourthCheck) {
            RoleName currentRole = MatchClient.getMatchClientInstance().getAlivePlayersMap().get(Player.getPlayer().getName());
            if (!currentRole.equals(RoleName.FOURTH_CIVILIAN)) Player.getPlayer().setRole(Role.getRole(currentRole));
            fourthCheck = false;
        }
    }

    private void addContinueButton(Table mainTable) {
        TextButton continueButton = new CustomTextButton(getStrings().get("continueToDay"), getButtonStyle());
        continueButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                continueToDay();
            }
        });
        mainTable.add(continueButton).width(DEFAULT_ACTOR_WIDTH).row();
    }

    private void continueToDay() {
        ProceedRequest request = new ProceedRequest();
        waitForOtherPlayers(request, ProceedType.END_NIGHT, new DayScreen(getGame()));
    }

    private void waitToFillMurderedTable(Table murderedTable) {
        while (true) {
            try {
                List<String> murderedList = MatchClient.getMatchClientInstance().getMurderedList();
                fillMurderedTable(murderedTable, murderedList);
                break;
            } catch (NullPointerException ignored) {
            }
        }
    }

    private void fillMurderedTable(Table murderedTable, List<String> murderedList) {
        for (String player : murderedList) {
            Label murderedLabel = new CustomLabel(player, getBlackStyle());
            murderedTable.add(murderedLabel).width(WIDTH / 5).row();
            if (Player.getPlayer().getName().equals(player)) {
                Player.getPlayer().setAlive(false);
            }
        }
    }
}
