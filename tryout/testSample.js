function send_info(msg){
    var data_type = {
             none : 0x00,
             led1 : 0x01,
             led2 : 0x02,
             lux_max : 0x03,
             lux_min : 0x04,
             lux_curr : 0x05,
             baro_max : 0x06,
             baro_min : 0x07,
             baro_curr : 0x08,
             temp_max : 0x09,
             temp__min : 0x0A,
             temp_curr : 0x0B,
             accel_max : 0x0C,
             accel_min : 0x0D,
             accel_curr : 0x0E,
             configuration : 0x0F,
             gpio_in : 0x10,
             gpio_out : 0x11,
             current_max : 0x12,
             current_min : 0x13,
             current_curr : 0x14,
             gps_latitude : 0x15,
             gps_longitude : 0x16,
             gps_time : 0x17,
             gps_date : 0x18,
             gps_lock : 0x19,
             qos_up : 0x1A,
             qos_dwn : 0x1B,
             rf_out : 0x1C,
             data_mark : 0x1D,
    };
​
    var data_struc = {
             data_valid : 0,
             block_start :0,
             temperature : 0,
             x_pos : 0,
             y_pos : 0,
             z_pos : 0,
             baro_pressure : 0,
             lux : 0,
             pkt_timer :0,
             rf_pwr : 0,
             sf_val : 0,
             rssi_up : 0,
             snr_up : 0,
             rssi_dwn : 0,
             snr_dwn :0 ,
             lat_deg : 0 ,
             lat_min : 0,
             long_deg : 0,
             long_min : 0,
             num_sats : 0 ,
             gps_status : 0,
    };
​
    context.global.data_out = context.global.data_out || data_struc;
    var pData = context.global.data_out;
    ​
    var msg_pntr = 0;
    var temp = 0;
    ​
    pData.sf_val = parseInt(msg.datr,10);
    ​
    while (msg_pntr < msg.payload.length){
             switch (msg.payload[msg_pntr]){
                 case data_type.lux_max:
                 case data_type.lux_min:
                 case data_type.lux_curr:
                         pData.lux = msg.payload[++msg_pntr] << 8 
                         pData.lux |= msg.payload[++msg_pntr];
                         pData.lux = pData.lux * 0.24;
                         msg_pntr++;
                         break;
                 case data_type.baro_max:
                 case data_type.baro_min:
                 case data_type.baro_curr:
                         pData.baro_pressure = msg.payload[++msg_pntr]<<16;
                         pData.baro_pressure |= msg.payload[++msg_pntr]<<8;
                         pData.baro_pressure |= msg.payload[++msg_pntr];
                         pData.baro_pressure = pData.baro_pressure * 0.25;
                         msg_pntr++;
                         break;
                 case data_type.accel_max:
                 case data_type.accel_min:
                 case data_type.accel_curr:
                         pData.x_pos = ((msg.payload[++msg_pntr] << 24) >> 24) * 0.0625;
                         pData.y_pos = ((msg.payload[++msg_pntr] << 24) >> 24) * 0.0625;
                         pData.z_pos = ((msg.payload[++msg_pntr] << 24) >> 24) * 0.0625;
                         msg_pntr++;
                         break;
                 case data_type.temp_min:
                 case data_type.temp_max:
                 case data_type.temp_curr:
                         pData.temperature = msg.payload[++msg_pntr] << 24;
                         pData.temperature |= msg.payload[++msg_pntr] << 16;
                         pData.temperature = (pData.temperature >> 16) * .0625;
                         msg_pntr++;
                         break;
                 case data_type.configuration:
                         pData.pkt_timer = msg.payload[++msg_pntr];
                         msg_pntr++
                         break;
                 case data_type.current_max:
                 case data_type.current_min:
                 case data_type.current_curr:
                         msg_pntr++;
                         msg_pntr++;
                         msg_pntr++;
                         break;
                 case data_type.gps_latitude:
                         pData.lat_deg = (msg.payload[++msg_pntr] << 24) >> 24;
                         pData.lat_min = msg.payload[++msg_pntr];
                         temp = msg.payload[++msg_pntr] << 8 
                         temp |= msg.payload[++msg_pntr];
                         pData.lat_min = pData.lat_min + (temp * 0.0001);
                         msg_pntr++;
                         break;
                 case data_type.gps_longitude:
                         pData.long_deg = (msg.payload[++msg_pntr] << 24);
                         pData.long_deg |= (msg.payload[++msg_pntr] << 16);
                         pData.long_deg = pData.long_deg >> 16;
                         pData.long_min = msg.payload[++msg_pntr];
                         temp = msg.payload[++msg_pntr] << 8 
                         temp |= msg.payload[++msg_pntr];
                         pData.long_min = pData.long_min + (temp * 0.0001);
                         msg_pntr++;
                         break;
                 case data_type.gps_time:
                         msg_pntr++;
                         msg_pntr++;
                         msg_pntr++;
                         msg_pntr++;
                         break;
                 case data_type.gps_date:
                         msg_pntr++;
                         msg_pntr++;
                         msg_pntr++;
                         msg_pntr++;
                         break;
                 case data_type.gps_lock:
                         msg_pntr++;
                         pData.gps_status = msg.payload[msg_pntr] & 0x0F;
                         pData.num_sats = msg.payload[msg_pntr++] >> 4;
                         break;
                 case data_type.qos_up:
                         pData.rssi_up = msg.payload[++msg_pntr] << 24;
                         pData.rssi_up |= msg.payload[++msg_pntr] << 16;
                         pData.rssi_up = pData.rssi_up >> 16;
                         pData.snr_up = msg.payload[++msg_pntr];
                         msg_pntr++;
                         break;
                 case data_type.qos_dwn:
                         pData.rssi_dwn = msg.payload[++msg_pntr] << 24;
                         pData.rssi_dwn |= msg.payload[++msg_pntr] << 16;
                         pData.rssi_dwn = pData.rssi_dwn >> 16;
                         pData.snr_dwn = msg.payload[++msg_pntr];
                         msg_pntr++;
                         break;
                 case data_type.rf_out:
                         pData.rf_pwr = (msg.payload[++msg_pntr] << 24) >> 24;
                         msg_pntr++;
                         break;
                 case data_type.data_mark:
                         if (msg_pntr == 0) {
                                  pData = data_struc;
                                  block_start = 1;
                                  msg_pntr++;
                         }
                         else if (msg_pntr == (msg.payload.length - 1) && (pData.block_start = 1)) {
                                           pData.data_valid = 1;
                                           msg_pntr++;
                                  }
                                  else {
                                           pData = data_struc;
                                           msg_pntr = msg.payload.length;
                                           }
                         break;
                 default:
                         pData = data_struc;
                         msg_pntr = msg.payload.length;
                 }
    }
    ​
    context.global.data_out = pData;
    ​
    return pData;    
}
