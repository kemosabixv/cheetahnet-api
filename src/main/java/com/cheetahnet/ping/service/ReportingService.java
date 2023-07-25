package com.cheetahnet.ping.service;

import com.cheetahnet.ping.model.DeviceConnectionHistoryEntity;
import com.cheetahnet.ping.model.DeviceCountHistoryEntity;
import com.cheetahnet.ping.model.DeviceEntity;
import com.cheetahnet.ping.repository.DeviceConnectionHistoryRepository;
import com.cheetahnet.ping.repository.DeviceCountHistoryRepository;
import com.cheetahnet.ping.repository.DeviceRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@Service
public class ReportingService {
    private final DeviceRepository deviceRepository;
    private final DeviceConnectionHistoryRepository deviceConnectionHistoryRepository;
    private final DeviceCountHistoryRepository deviceCountHistoryRepository;

    public ReportingService(DeviceRepository deviceRepository, DeviceConnectionHistoryRepository deviceConnectionHistoryRepository, DeviceCountHistoryRepository deviceCountHistoryRepository) {
        this.deviceRepository = deviceRepository;
        this.deviceConnectionHistoryRepository = deviceConnectionHistoryRepository;
        this.deviceCountHistoryRepository = deviceCountHistoryRepository;
    }

    @PostConstruct
    public void init() {
        Timer deviceCountTimer = new Timer();
        Timer deviceConnectionsHistoryTimer = new Timer();
        long delay = 0; // Initial delay
        long period1 = 21600000; // Period (6 hours in milliseconds)
        long period2 = 1800000; // Period (30 minutes in milliseconds)
//        long period1 = 10000;
//        long period2 = 10000;
        deviceCountTimer.schedule(new updateDeviceCount(), delay, period1);
        deviceConnectionsHistoryTimer.schedule(new updateDeviceConnectionsHistory(), delay, period2);
    }

    private class updateDeviceCount extends TimerTask {
        @Override
        public void run() {
            // Code to be executed periodically
            List<DeviceEntity> deviceEntities = deviceRepository.findAll();

            int stationCount = 0;
            int apCount = 0;
            int totalCount = 0;
            for (DeviceEntity deviceEntity : deviceEntities) {
                if (deviceEntity.getWirelessMode().equals("Station")) {
                    stationCount++;
                } else if (deviceEntity.getWirelessMode().equals("AP")) {
                    apCount++;

                }
                totalCount++;
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String dateCreated = LocalDateTime.now().format(formatter);

            DeviceCountHistoryEntity deviceCountHistoryEntity = new DeviceCountHistoryEntity();
            deviceCountHistoryEntity.setStationCount(stationCount);
            System.out.println("stationCount: " + stationCount);
            deviceCountHistoryEntity.setAPCount(apCount);
            System.out.println("apCount: " + apCount);
            deviceCountHistoryEntity.setTotalCount(totalCount);
            System.out.println("totalCount: " + totalCount);
            deviceCountHistoryEntity.setDateCreated(LocalDateTime.parse(dateCreated, formatter));
            System.out.println("dateCreated: " + dateCreated);
            deviceCountHistoryRepository.save(deviceCountHistoryEntity);
        }
    }


    private class updateDeviceConnectionsHistory extends TimerTask {
        @Override
        public void run() {
            // Code to be executed periodically
            List<DeviceEntity> deviceEntities = deviceRepository.findAll();

            int onlineCount = 0;
            int offlineCount = 0;

            for (DeviceEntity deviceEntity : deviceEntities) {

                if (deviceEntity.getConnectionStatus().equals("Online")){
                    onlineCount++;
                } else {
                    offlineCount++;
                }
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String dateCreated = LocalDateTime.now().format(formatter);

            DeviceConnectionHistoryEntity deviceConnectionHistoryEntity = new DeviceConnectionHistoryEntity();
            deviceConnectionHistoryEntity.setOnlineCount(onlineCount);
            System.out.println("Online Count:" + onlineCount);
            deviceConnectionHistoryEntity.setOfflineCount(offlineCount);
            System.out.println("Offline Count:" + offlineCount);
            deviceConnectionHistoryEntity.setDateCreated(LocalDateTime.parse(dateCreated, formatter));
            System.out.println("dateCreated: " + dateCreated);
            deviceConnectionHistoryRepository.save(deviceConnectionHistoryEntity);
        }
    }

}
