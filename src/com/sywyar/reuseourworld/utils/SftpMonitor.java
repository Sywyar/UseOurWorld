package com.sywyar.reuseourworld.utils;

import com.jcraft.jsch.SftpProgressMonitor;
import com.sywyar.reuseourworld.event.StringUpdate;

import java.text.NumberFormat;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SftpMonitor implements SftpProgressMonitor, Runnable {

    private final long maxCount;
    private long startTime = 0L;
    private long uploaded = 0;
    private boolean isScheduled = false;
    private final StringUpdate output;
    ScheduledExecutorService executorService;

    public SftpMonitor(long maxCount, StringUpdate output) {
        this.maxCount = maxCount;
        this.output=output;
    }

    @Override
    public void run() {
        NumberFormat format = NumberFormat.getPercentInstance();
        format.setMaximumFractionDigits(2);
        format.setMinimumFractionDigits(2);
        String value = format.format((uploaded / (double) maxCount));
        if (value.equals("NaN")){
            value="100%";
        }
        output.updateString(Language.getString("useourworld_sftp_transferred")+":"+uploaded/1024+"KB,"+Language.getString("useourworld_sftp_transmission_progress")+":"+value);
    }

    @Override
    public boolean count(long count) {
        if (!isScheduled) {
            createTread();
        }
        uploaded += count;
        return count > 0;
    }
    @Override
    public void end() {
        run();
        long endTime = System.currentTimeMillis();
        output.updateString(Language.getString("useourworld_sftp_end")+":" + (endTime - startTime) / 1000 + "s");
        stop();
    }

    @Override
    public void init(int op, String src, String dest, long max) {
        startTime = System.currentTimeMillis();
    }

    public void createTread() {
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleWithFixedDelay(this, 0, 1, TimeUnit.SECONDS);
        isScheduled = true;
    }

    public void stop() {
        if (this.executorService!=null){
            boolean isShutdown = executorService.isShutdown();
            if (!isShutdown) {
                executorService.shutdown();
            }
        }
    }

}