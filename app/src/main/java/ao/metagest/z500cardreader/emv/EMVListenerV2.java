//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package ao.metagest.z500cardreader.emv;

import android.os.IInterface;
import android.os.RemoteException;

import com.ciontek.hardware.aidl.bean.EMVCandidateV2;

import java.util.List;

public interface EMVListenerV2 extends IInterface {
    void onWaitAppSelect(List<EMVCandidateV2> var1, boolean var2) throws RemoteException;

    void onAppFinalSelect(String var1) throws RemoteException;

    void onConfirmCardNo(String var1) throws RemoteException;

    void onRequestShowPinPad(int var1, int var2) throws RemoteException;

    void onRequestSignature() throws RemoteException;

    void onCertVerify(int var1, String var2) throws RemoteException;

    void onOnlineProc() throws RemoteException;

    void onCardDataExchangeComplete() throws RemoteException;

    void onTransResult(int var1, String var2) throws RemoteException;

    void onConfirmationCodeVerified() throws RemoteException;

    void onRequestDataExchange(String var1) throws RemoteException;

    void onTermRiskManagement() throws RemoteException;

    void onPreFirstGenAC() throws RemoteException;

    void onDataStorageProc(String[] var1, String[] var2) throws RemoteException;

}
