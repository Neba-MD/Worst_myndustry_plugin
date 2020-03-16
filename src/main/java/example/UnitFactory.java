package example;

import arc.util.Timer;
import mindustry.content.UnitTypes;
import mindustry.entities.type.BaseUnit;
import mindustry.entities.type.Player;
import mindustry.gen.Call;
import mindustry.type.Item;
import static mindustry.Vars.*;
import java.util.HashMap;
import java.util.TimerTask;

public class UnitFactory {
    ExamplePlugin plugin;
    boolean building=false;
    java.util.Timer countdownTimer=new java.util.Timer();
    int time;
    int currentBuildTime=0;
    String currentUnit;
    TimerTask countdown;
    HashMap<String,int[]> unitStats=new HashMap<>();
    int[] reaperCost={10000,10000,4000,3000,5000,1000,5000,500,500,500,10};
    int[] lichCost={5000,5000,2000,1500,2500,500,2500,250,250,250,5};
    int[] eradCost={15000,15000,1000,5000,10000,5000,1000,1000,1000,1000,15};
    private static final int buildTimeIdx=10;

    public UnitFactory(ExamplePlugin plugin){
        unitStats.put("reaper",reaperCost);
        unitStats.put("lich",lichCost);
        unitStats.put("eradicator",eradCost);
        this.plugin=plugin;
    }
    public boolean verify_request(Player player,String unitName) {
        if (building) {
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
            int requres = unitStats.get(unitName)[idx];
            int stoerd = plugin.layout[idx];
            if (requres > stoerd) {
                can_build = false;
                player.sendMessage("You are missing [red]" + (requres - stoerd) + " " + item.name + "[white].");
            }
            idx++;
        }
        if (!can_build) {
            player.sendMessage("Not enough resources!");
            return false;
        }
        boolean solid = false;
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
    public void build_unit(Player player,String unitName){
        currentUnit = unitName;
        currentBuildTime = unitStats.get(unitName)[buildTimeIdx] * 60;
        int idx=0;
        for(Item item:content.items()){
            if(plugin.verify_item(item)){continue;}
            int requres=unitStats.get(unitName)[idx];
            plugin.layout[idx]-=requres;
            idx++;
        }
        BaseUnit unit=UnitTypes.reaper.create(player.getTeam());;
        switch (unitName){
            case "lich":
                unit=UnitTypes.lich.create(player.getTeam());
                break;
            case "eradicator":
                unit=UnitTypes.eradicator.create(player.getTeam());
                break;
        }
        unit.set(player.x, player.y);
        building=true;
        Call.sendMessage("[green]Building of "+currentUnit+" just started.It will take "+currentBuildTime/60+" minutes.");
        start_countdown(currentBuildTime/2);
        BaseUnit finalUnit = unit;
        String finalCurrentUnit=currentUnit;
        Timer.schedule(()->{
            Call.sendMessage("[green]"+currentUnit+" is finished and on its wey.You can use factory egan.");
            building=false;
            countdownTimer.cancel();
            countdownTimer.purge();
            countdownTimer=new java.util.Timer();
            building=false;
        },(int)(currentBuildTime/2));
        Timer.schedule(()->{
            finalUnit.add();
            Call.sendMessage("[green]"+finalCurrentUnit+" arrived.");
        },currentBuildTime);
    }
    public void start_countdown(int duration){
        time=duration;
        countdown=new java.util.TimerTask() {
            @Override
            public void run() {
                time--;
            }
        };
        countdownTimer.schedule(countdown,0,1000);
    }
}
