package com.luk4w.usbhost;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity
{
    private static final String ACTION_USB_PERMISSION = BuildConfig.APPLICATION_ID + ".USB_PERMISSION";
    private PendingIntent permissionIntent;
    private UsbManager usbManager;
    private UsbDevice device;
    private UsbInterface usbInterface;
    private UsbEndpoint endpointOut;
    private UsbEndpoint endpointIn;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button buttonConnectDevice = findViewById(R.id.buttonConnectDevice);
        Button buttonSendSignal = findViewById(R.id.buttonSendSignal);

        TextView textDeviceInfo = findViewById(R.id.textDeviceInfo);
        TextView textSendSignal = findViewById(R.id.textSendSignal);

        textSendSignal.setTextIsSelectable(true);
        textDeviceInfo.setTextIsSelectable(true);

        permissionIntent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S)
            permissionIntent = PendingIntent.getActivity(this, 0, new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_MUTABLE);
        else
            permissionIntent = PendingIntent.getActivity (this, 0, new Intent(ACTION_USB_PERMISSION), 0);

        buttonConnectDevice.setOnClickListener(view ->
        {
            usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
            HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
            Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
            if (deviceList.size() > 0)
            {
                device = deviceIterator.next();
                usbManager.requestPermission(device, permissionIntent);
                if (usbManager.hasPermission(device))
                {
                    String info =
                            "DeviceID: " + device.getDeviceId() + "\n" +
                            "DeviceName: " + device.getDeviceName() + "\n" +
                            "DeviceClass: " + getUsbClassName(device.getDeviceClass()) + "\n" +
                            "DeviceSubClass: " + device.getDeviceSubclass() + "\n" +
                            "VendorID: " + device.getVendorId() + "\n" +
                            "ProductID: " + device.getProductId() + "\n" +
                            "InterfaceCount: " + device.getInterfaceCount();

                    textDeviceInfo.setText(info);

                    usbInterface = device.getInterface(0);
                    for (int i = 0; i < usbInterface.getEndpointCount(); i++)
                    {
                        UsbEndpoint endpoint = usbInterface.getEndpoint(i);
                        if (endpoint.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK)
                        {
                            if (endpoint.getDirection() == UsbConstants.USB_DIR_OUT)
                            {
                                endpointOut = endpoint;
                            }
                            else
                            {
                                endpointIn = endpoint;
                            }
                        }
                    }
                }
            }
            else
            {
                textDeviceInfo.setText(R.string.usb_device_not_found);
            }
        });

        buttonSendSignal.setOnClickListener(view -> {
            if (device != null)
            {
                try
                {
                    UsbDeviceConnection connection = usbManager.openDevice(device);
                    connection.claimInterface(usbInterface, true);

                    byte[] data = new byte[]{0x01, 0x02};
                    int sentBytes= connection.bulkTransfer(endpointOut, data, data.length, 0);
                    textSendSignal.setText(MessageFormat.format("Bytes Sent: {0}", sentBytes ));

                    connection.close();
                }
                catch (Exception e)
                {
                    textSendSignal.setText(R.string.error);
                }
            }
            else
            {
                textSendSignal.setText(R.string.usb_device_not_found);
            }
        });

    }

    private String getUsbClassName(int id)
    {
        switch (id) {
            case UsbConstants.USB_CLASS_APP_SPEC:
                return "USB_CLASS_APP_SPEC";
            case UsbConstants.USB_CLASS_AUDIO:
                return "USB_CLASS_AUDIO";
            case UsbConstants.USB_CLASS_CDC_DATA:
                return "USB_CLASS_CDC_DATA";
            case UsbConstants.USB_CLASS_COMM:
                return "USB_CLASS_COMM";
            case UsbConstants.USB_CLASS_CONTENT_SEC:
                return "USB_CLASS_CONTENT_SEC";
            case UsbConstants.USB_CLASS_CSCID:
                return "USB_CLASS_CSCID";
            case UsbConstants.USB_CLASS_HID:
                return "USB_CLASS_HID";
            case UsbConstants.USB_CLASS_HUB:
                return "USB_CLASS_HUB";
            case UsbConstants.USB_CLASS_MASS_STORAGE:
                return "USB_CLASS_MASS_STORAGE";
            case UsbConstants.USB_CLASS_MISC:
                return "USB_CLASS_MISC";
            case UsbConstants.USB_CLASS_PER_INTERFACE:
                return "USB_CLASS_PER_INTERFACE";
            case UsbConstants.USB_CLASS_PHYSICA:
                return "USB_CLASS_PHYSICA";
            case UsbConstants.USB_CLASS_PRINTER:
                return "USB_CLASS_PRINTER";
            case UsbConstants.USB_CLASS_STILL_IMAGE:
                return "USB_CLASS_STILL_IMAGE";
            case UsbConstants.USB_CLASS_VENDOR_SPEC:
                return "USB_CLASS_VENDOR_SPEC";
            case UsbConstants.USB_CLASS_VIDEO:
                return "USB_CLASS_VIDEO";
            case UsbConstants.USB_CLASS_WIRELESS_CONTROLLER:
                return "USB_CLASS_WIRELESS_CONTROLLER";
            default:
                return "UNKNOWN USB CLASS!";
        }
    }
}