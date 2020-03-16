package example;

import arc.util.Timer;
import mindustry.entities.type.Player;
import mindustry.gen.Call;

import java.util.ArrayList;
import java.util.TimerTask;

import static mindustry.Vars.playerGroup;

public class Vote {
    ExamplePlugin plugin;
    UnitFactory factory;
    Player player;

    String type;
    String unitType;

    static java.util.Timer bundletimer = new java.util.Timer();

    int bundletime = 0;

    boolean isvoting = false;

    ArrayList<String> list = new ArrayList<>();
    int require;

    public Vote(ExamplePlugin plugin,UnitFactory factory) {
        this.plugin = plugin;
        this.factory=factory;
    }

    public boolean isIsvoting() {
        return isvoting;
    }

    void launch_Vote(Player player, String type) {
        this.player = player;
        this.type = type;
        command();
    }
    void build_Vote(Player player,String unitType) {
        this.player = player;
        this.type = "build";
        this.unitType=unitType;
        command();
    }



    TimerTask alert = new TimerTask() {
        @Override
        public void run() {
            Thread.currentThread().setName("Vote alert timertask");
            String[] bundlename = {"vote-50sec", "vote-40sec", "vote-30sec", "vote-20sec", "vote-10sec"};

            if (bundletime <= 4) {
                if (playerGroup != null && playerGroup.size() > 0) {
                    Call.sendMessage(bundlename[bundletime]);
                }
                bundletime++;
            }
        }
    };

    public void cancel() {
        alert = new TimerTask() {
            @Override
            public void run() {
                Thread.currentThread().setName("Vote alert timertask");
                String[] bundlename = {"vote-50sec", "vote-40sec", "vote-30sec", "vote-20sec", "vote-10sec"};

                if (bundletime <= 4) {
                    if (playerGroup != null && playerGroup.size() > 0) {
                        Call.sendMessage(bundlename[bundletime]);
                    }
                    bundletime++;
                }
            }
        };
        isvoting = false;
        bundletimer.cancel();
        bundletimer.purge();
        bundletimer = new java.util.Timer();
        bundletime = 0;
        switch (type) {
            case "use":
                if (require <= 0) {
                    Call.sendMessage("vote-launch to core-done");
                    plugin.use_layout(player);
                } else {
                    Call.sendMessage("vote-launch to core-fail");
                }
                break;
            case "fill":
                if (require <= 0) {
                    Call.sendMessage("vote-launch to layout-done");
                    plugin.use_layout(player);
                } else {
                    Call.sendMessage("vote-launch to layout-fail");
                }
                break;
            case "build":
                if (require <= 0) {
                    Call.sendMessage("vote-build "+unitType+"-done");
                    factory.build_unit(player,unitType);
                } else {
                    Call.sendMessage("vote-build "+unitType+"-fail");
                }
                break;

        }
        list.clear();
    }

    public void command() {
        if(playerGroup.size()==1){
            require=1;
        }else if (playerGroup.size() <= 3) {

            require = 2;
        } else {
            require = (int) Math.ceil((double) playerGroup.size() / 2);
        }

        if (!isvoting) {
            String message = Integer.toString(plugin.launch_amount) + " " + (plugin.launch_item == null ? " of every resource" : plugin.launch_item.name);
            switch (type) {
                case "use":
                    Call.sendMessage("vote-to launch [orange]" + message + "[white] to core.Open chat window and sey [orange]'y' [white]to agree.");
                    break;
                case "fill":
                    Call.sendMessage("vote-to launch [orange]" + message + "[white] to loadout.Open chat window and sey [orange]'y' [white]to agree.");
                    break;
                case "build":
                    Call.sendMessage("vote-to build [orange]" +unitType+ "[white].Open chat window and sey [orange]'y' [white]to agree.");
            }
            isvoting = true;
            Timer.schedule(()->{
                if(isIsvoting()){
                    cancel();
                }
            },60);
            bundletimer.schedule(alert, 10000, 10000);
        } else {
            player.sendMessage("vote-in-processing");
        }
    }

    public void add_vote(Player player, int vote) {
        if (list.contains(player.uuid)) {
            player.sendMessage("You already voted,sit down!");
            return;
        }
        require -= vote;
        list.add(player.uuid);
        if (require <= 0) {
            cancel();
        } else {
            Call.sendMessage("[orange]" + Integer.toString(require) + " [white]more votes needet.");
        }
    }
}
