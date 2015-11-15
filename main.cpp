/**
 * @file    main.cpp
 * @brief   Main application for mDot-EVB demo
 * @author  Tim Barr  MultiTech Systems Inc.
 * @version 1.03
 * @see
 *
 * Copyright (c) 2015
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * 1.01 TAB 7/6/15 Removed NULL pointer from evbAmbientLight creation call.
 *
 * 1.02 TAB 7/8/15 Send logo to LCD before attempting connection to LoRa network. Added
 *                  information on setting up for public LoRa network. Moved SW setup to
 *                  beginning of main. Removed printf call from ISR functions. Added
 *                  additional checks for exit_program.
 *
 * 1.03 TAB 7/15/15 Added threads for push button switch debounce.
 *
 * 1.04 TAB 10/22/15 Fixed format error in temperature print to LCD. Corrected error in
 *                  public netework setup. Swapped \n and \r in prinf calls because
 *                  Windows seems to be picky about the order
 *
 */
 
#include "mbed.h"
#include "MMA845x.h"
#include "MPL3115A2.h"
#include "ISL29011.h"
#include "NCP5623B.h"
#include "DOGS102.h"
#include "font_6x8.h"
#include "MultiTech_Logo.h"
#include "mDot.h"
#include "rtos.h"
#include <string>
#include <vector>
 
enum LED1_COLOR {
    RED = 0,
    GREEN = 1
};
 
/*
 * union for converting from 32-bit to 4 8-bit values
 */
union convert32 {
    int32_t f_s;        // convert from signed 32 bit int
    uint32_t f_u;       // convert from unsigned 32 bit int
    uint8_t t_u[4];     // convert to 8 bit unsigned array
};
 
/*
 * union for converting from 16- bit to 2 8-bit values
 */
union convert16 {
    int16_t f_s;        // convert from signed 16 bit int
    uint16_t f_u;       // convert from unsigned 16 bit int
    uint8_t t_u[2];     // convert to 8 bit unsigned array
};
 
//DigitalIn mDot02(PA_2);                       //  GPIO/UART_TX
//DigitalOut mDot03(PA_3);                      //  GPIO/UART_RX
//DigitalIn mDot04(PA_6);                       //  GPIO/SPI_MISO
//DigitalIn mDot06(PA_8);                       //  GPIO/I2C_SCL
//DigitalIn mDot07(PC_9);                       //  GPIO/I2C_SDA
 
InterruptIn mDot08(PA_12);                      //  GPIO/USB       PB S1 on EVB
InterruptIn mDot09(PA_11);                      //  GPIO/USB       PB S2 on EVB
 
//DigitalIn mDot11(PA_7);                       //  GPIO/SPI_MOSI
 
InterruptIn mDot12(PA_0);                       //  GPIO/UART_CTS  PRESSURE_INT2 on EVB
DigitalOut mDot13(PC_13,1);                     //  GPIO           LCD_C/D
InterruptIn mDot15(PC_1);                       //  GPIO           LIGHT_PROX_INT on EVB
InterruptIn mDot16(PA_1);                       //  GPIO/UART_RTS  ACCEL_INT2 on EVB
DigitalOut mDot17(PA_4,1);                      //  GPIO/SPI_NCS   LCD_CS on EVB
 
//DigitalIn mDot18(PA_5);                       //  GPIO/SPI_SCK
 
//DigitalInOut mDot19(PB_0,PIN_INPUT,PullNone,0); // GPIO         PushPull LED Low=Red High=Green set MODE=INPUT to turn off
AnalogIn mDot20(PB_1);                          //  GPIO          Current Sense Analog in on EVB
 
Serial debugUART(PA_9, PA_10);              // mDot debug UART
 
//Serial mDotUART(PA_2, PA_3);                  // mDot external UART mDot02 and mDot03
 
I2C mDoti2c(PC_9,PA_8);                         // mDot External I2C mDot6 and mDot7
 
SPI mDotspi(PA_7,PA_6,PA_5);                    // mDot external SPI mDot11, mDot4, and mDot18
 
/* **** replace these values with the proper public or private network settings ****
 * config_network_nameand config_network_pass are for private networks.
 */
//settings for my bench
static std::string config_network_name = "TheThingsNetwork";
static std::string config_network_pass = "helloworld";
static uint8_t config_frequency_sub_band = 2;
 
//Default network server settings
//static std::string config_network_name = "YOUR-NETWORK-NAME";
//static std::string config_network_pass = "YOUR-NETWORK-PASSPHRASE";
//static uint8_t config_frequency_sub_band = 3;
 
/*  config_app_id and config_app_key are for public networks.
static uint8_t app_id[8] = {0x00,0x01,0x02,0x03,0x0A,0x0B,0x0C,0x0D};
std::vector<uint8_t> config_app_id;
static uint8_t app_key[16] = {0x00,0x01,0x02,0x03,0x0A,0x0B,0x0C,0x0D};
std::vector<uint8_t> config_app_key;
*/
 
uint8_t result, pckt_time=10;
char data;
unsigned char test;
char txtstr[17];
int32_t num_whole, mdot_ret;
uint32_t pressure;
int16_t num_frac;
 
bool exit_program = false;

uint16_t  lux_data;
ISL29011* evbAmbLight;
mDot* mdot_radio;
 
convert32 convertl;
convert16 converts;
 
// flags for pushbutton debounce code
bool pb1_low = false;
bool pb2_low = false;
 
void pb1ISR(void);
void pb2ISR(void);
void pb1_debounce(void const *args);
void pb2_debounce(void const *args);
Thread* thread_3;
 
void log_error(mDot* dot, const char* msg, int32_t retval);
 
void config_pkt_xmit (void const *args);
 
int main()
{
 
    std::vector<uint8_t> mdot_data;
    std::vector<uint8_t> mdot_EUI;
    uint16_t i = 0;
 
    debugUART.baud(921600);
 
    Thread thread_1(pb1_debounce);                      // threads for de-bouncing pushbutton switches
    Thread thread_2(pb2_debounce);
 
    thread_3 = new Thread(config_pkt_xmit);             // start thread that sends LoRa packet when SW2 pressed
    
    evbAmbLight = new ISL29011(mDoti2c);                // Setup Ambient Light Sensor
    //////evbBackLight = new NCP5623B(mDoti2c);               // setup backlight and LED 2 driver chip
 
    /*
     *  Setup SW1 as program stop function
     */
    mDot08.disable_irq();
    mDot08.fall(&pb1ISR);
 
    /*
     *  need to call this function after rise or fall because rise/fall sets
     *  mode to PullNone
     */
    mDot08.mode(PullUp);
 
    mDot08.enable_irq();
 
    /*
     *  Setup SW2 as packet time change
     */
    mDot09.disable_irq();
    mDot09.fall(&pb2ISR);
 
    /*
     *  need to call this function after rise or fall because rise/fall sets
     *  mode to PullNone
     */
    mDot09.mode(PullUp);
 
    mDot09.enable_irq();
 
    /*
    * Setting other InterruptIn pins with Pull Ups
    */
    mDot12.mode(PullUp);
    mDot15.mode(PullUp);
    mDot16.mode(PullUp);

    // get a mDot handle
    mdot_radio = mDot::getInstance();
 
    if (mdot_radio) {
        // reset to default config so we know what state we're in
        mdot_radio->resetConfig();
 
        // Setting up LED1 as activity LED
        mdot_radio->setActivityLedPin(PB_0);
        mdot_radio->setActivityLedEnable(true);
 
        // Read node ID
        mdot_EUI = mdot_radio->getDeviceId();
        printf("mDot EUI = ");
 
        for (i=0; i<mdot_EUI.size(); i++) {
            printf("%02x ", mdot_EUI[i]);
        }
        printf("\r\n");
 
 
// Setting up the mDot with network information.
 
        /*
         * This call sets up private or public mode on the MTDOT. Set the function to true if
         * connecting to a public network
         */
        printf("setting Private Network Mode\r\n");
        if ((mdot_ret = mdot_radio->setPublicNetwork(false)) != mDot::MDOT_OK) {
            log_error(mdot_radio, "failed to set Public Network Mode", mdot_ret);
        }
 
        /*
         * Frequency sub-band is valid for NAM only and for Private networks should be set to a value
         * between 1-8 that matches the the LoRa gateway setting. Public networks use sub-band 0 only.
         * This function can be commented out for EU networks
         */
        printf("setting frequency sub band\r\n");
        if ((mdot_ret = mdot_radio->setFrequencySubBand(config_frequency_sub_band)) != mDot::MDOT_OK) {
            log_error(mdot_radio, "failed to set frequency sub band", mdot_ret);
        }
 
        /*
         * setNetworkName is used for private networks.
         * Use setNetworkID(AppID) for public networks
         */
 
//      config_app_id.assign(app_id,app_id+7);
 
        printf("setting network name\r\n");
        if ((mdot_ret = mdot_radio->setNetworkName(config_network_name)) != mDot::MDOT_OK) {
//      if ((mdot_ret = mdot_radio->setNetworkId(config_app_id)) != mDot::MDOT_OK) {
            log_error(mdot_radio, "failed to set network name", mdot_ret);
        }
 
        /*
         * setNetworkPassphrase is used for private networks
         * Use setNetworkKey for public networks
         */
 
//      config_app_key.assign(app_key,app_key+15);
 
        printf("setting network password\r\n");
        if ((mdot_ret = mdot_radio->setNetworkPassphrase(config_network_pass)) != mDot::MDOT_OK) {
//      if ((mdot_ret = mdot_radio->setNetworkKey(config_app_key)) != mDot::MDOT_OK) {
            log_error(mdot_radio, "failed to set network password", mdot_ret);
        }
 
        // attempt to join the network
        printf("joining network\r\n");
        while (((mdot_ret = mdot_radio->joinNetwork()) != mDot::MDOT_OK) && (!exit_program)) {
            log_error(mdot_radio,"failed to join network:", mdot_ret);
            if (mdot_radio->getFrequencyBand() == mDot::FB_868) {
                mdot_ret = mdot_radio->getNextTxMs();
            } else {
                mdot_ret = 0;
            }
 
            printf("delay = %lu\r\n",mdot_ret);
            osDelay(mdot_ret + 1);
        }
 
        /*
         * Check for PB1 press during network join attempt
         */
        if (exit_program) {
            printf("Exiting program\r\n");
            sprintf(txtstr,"Exiting Program");
            exit(1);
        }
 
    } else {
        printf("radio setup failed\r\n");
        //exit(1);
    }
 
    osDelay(200);
    //evbBackLight->setPWM(NCP5623B::LED_3,16); // enable LED2 on EVB and set to 50% PWM
 
    // sets LED2 to 50% max current
    //evbBackLight->setLEDCurrent(16);
 
    printf("Start of Test\r\n");
 
    osDelay (500);          // allows other threads to process
    printf("shutdown LED:\r\n");
    //evbBackLight->shutdown();
 
    osDelay (500);          // allows other threads to process
    printf("Turn on LED2\r\n");
    //evbBackLight->setLEDCurrent(16);
 
    /*
     * Setup the Ambient Light Sensor for continuous Ambient Light Sensing, 16 bit resolution,
     * and 16000 lux range
     */
 
    evbAmbLight->setMode(ISL29011::ALS_CONT);
    evbAmbLight->setResolution(ISL29011::ADC_16BIT);
    evbAmbLight->setRange(ISL29011::RNG_16000);
   
    /*
     * Check for PB1 press during network join attempt
     */
    if (exit_program) {
        printf("Exiting program\r\n");
        sprintf(txtstr,"Exiting Program");
        //////////evbLCD->writeText(0,4,font_6x8,txtstr,strlen(txtstr));
        exit(1);
    }
 
    /*
     * Main data acquisition loop
     */
    pckt_time = 2;
    i = 0;
    
    uint16_t  lux_thresh = 50;
    uint16_t  dash_delay = 800;
    bool hit_thresh = false;    
    
    do {
	hit_thresh = false;
	while(!hit_thresh){
	  lux_data = evbAmbLight->getData();
	  num_whole = lux_data * 24 / 100;        // 16000 lux full scale .24 lux per bit
	  num_frac = lux_data * 24 % 100;
	  
	  if(lux_data >= lux_thresh)
	    hit_thresh = true;
	}
	osDelay(dash_delay); //if on after this, has to be a dot
	lux_data = evbAmbLight->getData();
	num_whole = lux_data * 24 / 100;        // 16000 lux full scale .24 lux per bit
	num_frac = lux_data * 24 % 100;
	
	if(lux_data >= lux_thresh){
	  lux_data = 0xffff;
	  printf("DASH !\r\n");
	}else{
	  lux_data = 0xaaaa;
	  printf("DOT !\r\n");
	}		
	
        if (++i % pckt_time == 0) { // check packet counter will send packet every 2-5-10 data collection loops
            mdot_data.clear();
            mdot_data.push_back(0x05);          // key for Current Ambient Light Value
            converts.f_u = lux_data;                // data is 16 bits unsigned            
            if ((mdot_ret = mdot_radio->send(mdot_data)) != mDot::MDOT_OK) {
                log_error(mdot_radio, "failed to send", mdot_ret);
            } else {
                printf("successfully sent data to gateway\r\n");
            }
        }
    } while(!exit_program && (i < 65000));
 
    printf("End of Test\r\n");
}
 
/*
 * Sets pb1_low flag. Slag is cleared in pb1_debounce thread
 */
void pb1ISR(void)
{
    if (!pb1_low)
        pb1_low = true;
}
 
/*
 * Debounces pb1. Also exits program if pushbutton 1 is pressed
 */
void pb1_debounce(void const *args)
{
 
    static uint8_t count = 0;
 
    while (true) {
 
        if (pb1_low && (mDot08 == 0))
            count++;
        else {
            count = 0;
            pb1_low = false;
        }
 
        if (count == 5)
            exit_program = true;
 
        Thread::wait(5);
    }
}
 
/*
 * Sets pb2_low flag. Flag is cleared in pb2_debounce thread
 */
void pb2ISR(void)
{
    if (!pb2_low)
        pb2_low = true;
}
 
/*
 * Debounces pb2. Also changes packet transmit time to every other,
 * every fifth, or every tenth sample when SW2 pushed
 * Also triggers a thread to transmit a configuration packet
 */
void pb2_debounce(void const *args)
{
 
    static uint8_t count = 0;
 
    while (true) {
 
        if (pb2_low && (mDot09 == 0))
            count++;
        else {
            count = 0;
            pb2_low = false;
        }
 
        if (count == 5) {
 
            if (pckt_time >= 5)
                pckt_time /= 2;
            else pckt_time = 20;
 
            thread_3->signal_set(0x10);     // signal config_pkt_xmit to send packet
        }
 
        Thread::wait(5);
    }
}
 
/*
 *  Function that print clear text verion of mDot errors
 */
void log_error(mDot* dot, const char* msg, int32_t retval)
{
    printf("%s - %ld:%s, %s\r\n", msg, retval, mDot::getReturnCodeString(retval).c_str(), dot->getLastError().c_str());
}
 
/*
 * Thread that is triggered by SW2 ISR. Sends a packet to the LoRa server with the new Packet Transmission time setting
 */
void config_pkt_xmit (void const *args)
{
 
    std::vector<uint8_t> data;
 
    while (true) {
        Thread::signal_wait(0x10);      // wait for pb2ISR to signal send
        data.clear();
        data.push_back(0x0F);           // key for Configuration data (packet transmission timer)
        data.push_back(pckt_time);
 
        if ((mdot_ret = mdot_radio->send(data)) != mDot::MDOT_OK) {
            log_error(mdot_radio, "failed to send config data", mdot_ret);
        } else {
            printf("sent config data to gateway\r\n");
        }
    }
}