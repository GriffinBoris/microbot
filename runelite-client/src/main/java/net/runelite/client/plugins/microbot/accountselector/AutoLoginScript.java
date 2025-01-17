package net.runelite.client.plugins.microbot.accountselector;

import net.runelite.api.GameState;
import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.security.Login;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import java.util.concurrent.TimeUnit;

public class AutoLoginScript extends Script {

    public boolean run(AutoLoginConfig autoLoginConfig) {
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            if (!Microbot.isLoggedIn() && Microbot.pauseAllScripts) {
                Microbot.pauseAllScripts = false;
            }

            if (Microbot.pauseAllScripts)
                return;

            Widget clickHereToPlayButton = Rs2Widget.getWidget(24772680); //on login screen
            if (Microbot.getClientThread().runOnClientThread(() -> clickHereToPlayButton != null && !clickHereToPlayButton.isHidden())) {
                Rs2Widget.clickWidget(clickHereToPlayButton.getId());
            }

            try {
                if (Microbot.getClient().getGameState() == GameState.LOGIN_SCREEN) {
                    if (autoLoginConfig.useRandomWorld()) {
                        new Login(Login.getRandomWorld(autoLoginConfig.isMember()));
                    } else {
                        new Login(autoLoginConfig.world());
                    }
                    if (Microbot.getClient().getLoginIndex() == 10) {
                        int loginScreenWidth = 804;
                        int startingWidth = (Microbot.getClient().getCanvasWidth() / 2) - (loginScreenWidth / 2);
                        Microbot.getMouse().click(365 + startingWidth, 250); //clicks a button "OK" when you've been disconnected
                    } else if (Microbot.getClient().getLoginIndex() == 9) {
                        int loginScreenWidth = 804;
                        int startingWidth = (Microbot.getClient().getCanvasWidth() / 2) - (loginScreenWidth / 2);
                        Microbot.getMouse().click(365 + startingWidth, 300); //clicks a button "OK" when you've been disconnected
                    }
                    sleep(5000);
                }


            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
        return true;
    }
}
