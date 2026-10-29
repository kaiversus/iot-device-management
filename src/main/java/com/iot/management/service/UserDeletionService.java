package com.iot.management.service;

import com.iot.management.entity.Device;
import com.iot.management.entity.User;
import com.iot.management.repository.DeviceDataRepository;
import com.iot.management.repository.DeviceLogRepository;
import com.iot.management.repository.DeviceRepository;
import com.iot.management.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class UserDeletionService {
 private final UserRepository users;
 private final DeviceRepository devices;
 private final DeviceDataRepository data;
 private final DeviceLogRepository logs;
 private final CloudinaryService images;

 public UserDeletionService(UserRepository users,DeviceRepository devices,DeviceDataRepository data,
                            DeviceLogRepository logs,CloudinaryService images) {
  this.users=users;this.devices=devices;this.data=data;this.logs=logs;this.images=images;
 }

 public void delete(User user) {
  List<Device> owned=devices.findByOwnerId(user.getId());
  List<String> imageIds=new ArrayList<>();
  for(Device device:owned) {
   data.deleteByDeviceId(device.getId());
   logs.deleteByDeviceId(device.getId());
   if(device.getImagePublicId()!=null) imageIds.add(device.getImagePublicId());
  }
  data.flush();
  logs.flush();
  devices.deleteAll(owned);
  devices.flush();
  users.delete(user);
  users.flush();
  if(!imageIds.isEmpty()) TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
   @Override public void afterCommit() {imageIds.forEach(images::deleteQuietly);}
  });
 }
}
