package com.tuttlen.android_sqrl;

import java.net.URISyntaxException;

/**
 * Created by in805 on 2/7/2016.
 */
public interface IAuthorizationRequest {
    boolean isHTTPS();
    String getURL();
    String getBlueToothURL();
    String getReturnURL();
    String getNonce();
    String getDomain();
    boolean IsValid();
    String getBluetoothId();
    boolean IsValidBluetooth(String address);
    String removeScheme(String URL) throws URISyntaxException;
    boolean ValidateUrl(String URL);
    boolean ValidateBTooth(String address);

}
