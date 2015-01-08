package com.ip.notifier;

import java.util.Timer;
import java.util.TimerTask;

public class TimerService {

    private Timer timer;
    
	public TimerService() {
         timer = new Timer("TimerService", true);
    }

	public void registerForOnce(TimerServiceListener listener, long delay){
		timer.schedule(new TimerServiceTask(listener), delay);
	}
	
	public void registerForRepetetive(TimerServiceListener listener, long delay, long period){
		timer.scheduleAtFixedRate(new TimerServiceTask(listener), delay, period);
	}
	
	/**
     * @author igor.s
     */
    public class TimerServiceTask extends TimerTask {
    	private TimerServiceListener listener;
    	
        public TimerServiceTask(TimerServiceListener listener) {
        	this.listener = listener;
		}

		@Override
        public void run() {
			if(listener != null){
				listener.timerNotify();
			}
        }
    }
}