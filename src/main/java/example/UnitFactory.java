package example;

import arc.util.Timer;
import mindustry.content.UnitTypes;
import mindustry.entities.type.BaseUnit;
import mindustry.entities.type.Player;
import mindustry.gen.Call;
import mindustry.type.Item;
import static mindustry.Vars.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TimerTask;

public class UnitFactory {
    ExamplePlugin plugin;
    HashMap<String, int[]> unitStats = new HashMap<>();
    int[] reaperCost = {10000, 10000, 4000, 3000, 5000, 1000, 5000, 500, 500, 500, 7};
    int[] lichCost = {5000, 5000, 2000, 1500, 2500, 500, 2500, 250, 250, 250, 2};
    int[] eradCost = {15000, 15000, 1000, 5000, 10000, 5000, 1000, 1000, 1000, 1000, 10};
    private static final int buildTimeIdx = 10;
    ArrayList<Build_request> requests = new ArrayList<>();

    public UnitFactory(ExamplePlugin plugin) {
        unitStats.put("reaper", reaperCost);
        unitStats.put("lich", lichCost);
        unitStats.put("eradicator", eradCost);
        this.plugin = plugin;
    }

    public boolean verify_request(Player player, String unitName) {
        String currentUnit = is_building();
        if (currentUnit != null) {
            int time=currentUnit_buidtime();
            player.sendMessage("Factory is currently building [orange]" + currentUnit + "[white].It will be finished in " +
                    time / 60 + "min" + time % 60 + "sec.");
            return false;
        }
        if (!unitName.equals("reaper") && !unitName.equals("lich") && !unitName.equals("eradicator")) {
            player.sendMessage("Factory can not build [red]" + unitName + "[white]. It can build oni reaper,lich and eradicator.");
            return false;
        }
        boolean can_build = true;
        int idx = 0;
        for (Item item : content.items()) {
            if (plugin.verify_item(item)) {
                continue;
            }
            int requires = unitStats.get(unitName)[idx];
            int stored = plugin.layout[idx];
            if (requires > stored) {
                can_build = false;
                player.sendMessage("You are missing [red]" + (requires - stored) + " " + item.name + "[white].");
            }
            idx++;
        }
        if (!can_build) {
            player.sendMessage("Not enough resources!");
            return false;
        }
        int x = (int) player.x;
        int y = (int) player.y;
        if (world.tile(x / 8, y / 8).solid()) {
            if (unitName.equals("eradicator")) {
                player.sendMessage("Land unit cant be dropped on a solid block.");
                return false;
            }
        }
        return true;

    }

    private int currentUnit_buidtime() {
        for (Build_request b : requests) {
            if (b.building) {
                return b.time;
            }
        }
        return 0;
    }

    private String is_building() {
        for (Build_request b : requests) {
            if (b.building && !b.interrupted) {
                return b.unitName;
            }
        }
        return null;
    }

    public void info(Player player) {
        boolean inProgress=false;
        for (Build_request b : requests) {
            inProgress=b.info(player);
        }
        if(!inProgress){
            player.sendMessage("Factory is not building anything nor is any unit travelling.");
        }
    }

    public void interrupted() {
        for (Build_request b : requests) {
            b.interrupted = true;
        }
    }


    public void build_unit(Player player, String unitName) {
        int idx = 0;
        for (Item item : content.items()) {
            if (plugin.verify_item(item)) {
                continue;
            }
            int requires = unitStats.get(unitName)[idx];
            plugin.layout[idx] -= requires;
            idx++;
        }
        BaseUnit unit = UnitTypes.reaper.create(player.getTeam());
        ;
        switch (unitName) {
            case "lich":
                unit = UnitTypes.lich.create(player.getTeam());
                break;
            case "eradicator":
                unit = UnitTypes.eradicator.create(player.getTeam());
                break;
        }
        unit.set(player.x, player.y);
        Build_request b = new Build_request(unitName, unit, unitStats.get(unitName)[buildTimeIdx] * 60, plugin, this);
        requests.add(b);

    }
}


