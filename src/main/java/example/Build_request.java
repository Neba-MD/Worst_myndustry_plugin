package example;

import arc.util.Timer;
import mindustry.entities.type.BaseUnit;
import mindustry.entities.type.Player;
import mindustry.gen.Call;

import java.util.TimerTask;

public class Build_request{
    public boolean building=false;
    public boolean traveling=false;
    public boolean interrupted=false;

    public int time;

    java.util.Timer countdownTimer=new java.util.Timer();



    String unitName;
    BaseUnit unitObj;
    ExamplePlugin plugin;
    UnitFactory factory;
    public Build_request(String unitName,BaseUnit unitObj,int time,ExamplePlugin plugin,UnitFactory factory){

        this.unitName=unitName;
        this.unitObj=unitObj;
        this.time=time;
        this.plugin=plugin;
        this.factory=factory;
        start_countdown();
    }
    public boolean info(Player player){
        if(interrupted){
            return false;
        }
        if (building){
            player.sendMessage("Factory is currently building [orange]" + unitName + "[white].It will be finished in " +
                    time / 60 + "min" + time % 60 + "sec.");
            return true;
        }
        if (traveling){
            player.sendMessage("[orange]" + unitName + " [white]will arrive in " +
                    time / 60 + "min" + time % 60 + "sec.");
            return true;
        }
        return false;
    }

    private void start_countdown() {
        TimerTask countdown=new TimerTask() {
            @Override
            public void run() {
                time--;
            }
        };
        countdownTimer.schedule(countdown,0,1000);
        building=true;
        Call.sendMessage("[green]Building of " + unitName + " just started.It will take " + time / 60 + "min.");
        Timer.schedule(() -> {
            if (interrupted) {
                factory.requests.remove(this);
                return;
            }

            building = false;
            traveling = true;
            time=plugin.transport_time;
            Call.sendMessage("[green]" + unitName + " is finished nad will arrive in " + time / 60 + " min.You can use factory egan.");
            Timer.schedule(()->{
                traveling = false;
                if (interrupted) {
                    Call.sendMessage("[red]" + unitName + " arrived but base is gone.");
                    return;
                }else {
                    Call.sendMessage("[green]" + unitName + " arrived.");
                    unitObj.add();
                }
                factory.requests.remove(this);
            },time);
        }, time);
    }
}
