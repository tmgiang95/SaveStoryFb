package com.example.savestoryfb;

public interface OnHandleCheckTypeToDownload {
    void onSuccessCheckType(String type);
    void onSuccessCheckStorySite(int type);
    void onDownloading(String url,int type);
}
