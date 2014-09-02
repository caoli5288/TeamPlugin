package com.mengcraft.team;

import static java.lang.Thread.sleep;

public class THChestThread implements Runnable {
    @Override
    public void run() {
        try {
            sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        THUtils.setChest();
    }
}
